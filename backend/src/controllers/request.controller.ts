import { Request, Response, NextFunction } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { emitToCashier, emitToTable } from '../socket/index.js';

const createRequestSchema = z.object({
  tableNumber: z.string().min(1, 'Nomor meja wajib diisi'),
  type: z.enum(['PANGGIL_PELAYAN', 'MINTA_BILL', 'PERTANYAAN', 'LAINNYA']).default('PANGGIL_PELAYAN'),
  message: z.string().optional(),
});

export const createRequest = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const data = createRequestSchema.parse(req.body);

    const table = await prisma.table.findUnique({
      where: { number: data.tableNumber },
    });

    if (!table) {
      return res.status(404).json({ success: false, message: 'Meja tidak ditemukan' });
    }

    const request = await prisma.serviceRequest.create({
      data: {
        tableId: table.id,
        type: data.type,
        message: data.message,
        status: 'MENUNGGU',
      },
      include: { table: true },
    });

    // Real-time broadcast ke kasir
    emitToCashier('call_waiter', {
      requestId: request.id,
      tableNumber: table.number,
      type: request.type,
      message: request.message,
      createdAt: request.createdAt.toISOString(),
    });

    return res.status(201).json({
      success: true,
      message: 'Permintaan bantuan telah diteruskan ke kasir/pelayan',
      data: request,
    });
  } catch (error) {
    return next(error);
  }
};

const respondRequestSchema = z.object({
  responseMessage: z.string().min(1, 'Pesan balasan konfirmasi wajib diisi'),
});

export const respondRequest = async (req: Request, res: Response, next: NextFunction) => {
  try {
    const { id } = req.params;
    const { responseMessage } = respondRequestSchema.parse(req.body);

    const request = await prisma.serviceRequest.findUnique({
      where: { id },
      include: { table: true },
    });

    if (!request) {
      return res.status(404).json({ success: false, message: 'Permintaan bantuan tidak ditemukan' });
    }

    const updated = await prisma.serviceRequest.update({
      where: { id },
      data: {
        status: 'DIKONFIRMASI',
        responseMessage,
        confirmedAt: new Date(),
      },
    });

    // Broadcast jawaban kasir ke web meja pelanggan
    emitToTable(request.table.number, 'service_confirmed', {
      requestId: updated.id,
      tableNumber: request.table.number,
      status: 'DIKONFIRMASI',
      responseMessage: updated.responseMessage,
      confirmedAt: updated.confirmedAt,
    });

    return res.json({
      success: true,
      message: `Balasan telah dikirimkan ke Meja ${request.table.number}`,
      data: updated,
    });
  } catch (error) {
    return next(error);
  }
};

export const getActiveRequests = async (_req: Request, res: Response, next: NextFunction) => {
  try {
    const requests = await prisma.serviceRequest.findMany({
      where: {
        status: { in: ['MENUNGGU', 'DIKONFIRMASI'] },
      },
      orderBy: { createdAt: 'desc' },
      include: { table: true },
    });

    return res.json({
      success: true,
      data: requests.map((r) => ({
        id: r.id,
        tableNumber: r.table.number,
        type: r.type,
        message: r.message,
        status: r.status,
        responseMessage: r.responseMessage,
        createdAt: r.createdAt,
      })),
    });
  } catch (error) {
    return next(error);
  }
};
