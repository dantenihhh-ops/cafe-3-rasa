import { Prisma, PrismaClient } from '@prisma/client';
import { env } from '../config/env.js';
import { AppError } from './http.js';

type Tx = Prisma.TransactionClient;
type Db = Tx | PrismaClient;

/** Pesanan lengkap beserta item & meja, dipakai di semua response pesanan */
export const orderInclude = {
  items: { orderBy: { createdAt: 'asc' } },
  table: true,
} satisfies Prisma.OrderInclude;

export type FullOrder = Prisma.OrderGetPayload<{ include: typeof orderInclude }>;

/** Ubah pesanan dari database ke JSON yang rapi (BigInt -> number) */
export function serializeOrder(o: FullOrder) {
  return {
    id: o.id,
    orderNumber: o.orderNumber,
    tableId: o.tableId,
    tableNumber: o.table.number,
    status: o.status,
    paymentBillStatus: o.paymentBillStatus,
    paymentMethod: o.paymentMethod,
    orderSource: o.orderSource,
    customerNotes: o.customerNotes,
    subtotal: Number(o.subtotal),
    tax: Number(o.tax),
    total: Number(o.total),
    itemCount: o.items.reduce((sum, i) => sum + i.quantity, 0),
    items: o.items.map((i) => ({
      id: i.id,
      menuItemId: i.menuItemId,
      name: i.name,
      categoryName: i.categoryName,
      quantity: i.quantity,
      unitPrice: Number(i.unitPrice),
      subtotal: Number(i.subtotal),
      note: i.note,
    })),
    createdAt: o.createdAt,
    confirmedAt: o.confirmedAt,
    paidAt: o.paidAt,
  };
}

/** Pajak PB1 dibulatkan ke rupiah terdekat */
export function calcTax(subtotal: number): number {
  return Math.round((subtotal * env.taxRatePercent) / 100);
}

/** Hitung ulang subtotal, pajak & total pesanan dari item yang tersimpan di database */
export async function recalcOrderTotals(tx: Tx, orderId: string) {
  const items = await tx.orderItem.findMany({ where: { orderId }, select: { subtotal: true } });
  const subtotal = items.reduce((sum, i) => sum + Number(i.subtotal), 0);
  const tax = calcTax(subtotal);
  return tx.order.update({
    where: { id: orderId },
    data: { subtotal: BigInt(subtotal), tax: BigInt(tax), total: BigInt(subtotal + tax) },
    include: orderInclude,
  });
}

export interface ItemInput {
  menuItemId: string;
  quantity: number;
  note?: string;
}

/**
 * Ambil harga & nama menu LANGSUNG dari database (bukan dari client),
 * sehingga harga tidak bisa dimanipulasi dari browser.
 * Dipanggil SEBELUM transaksi (cukup baca data) agar transaksi tetap singkat.
 */
export async function buildOrderItems(db: Db, items: ItemInput[]) {
  const ids = [...new Set(items.map((i) => i.menuItemId))];
  const menus = await db.menuItem.findMany({
    where: { id: { in: ids } },
    include: { category: true },
  });
  const byId = new Map(menus.map((m) => [m.id, m]));

  return items.map((input) => {
    const menu = byId.get(input.menuItemId);
    if (!menu) throw new AppError(400, `Menu dengan ID "${input.menuItemId}" tidak ditemukan`);
    if (!menu.isAvailable) throw new AppError(409, `Maaf, "${menu.name}" sedang habis`);
    const unitPrice = Number(menu.price);
    return {
      menuItemId: menu.id,
      name: menu.name,
      categoryName: menu.category.name,
      unitPrice: BigInt(unitPrice),
      quantity: input.quantity,
      note: input.note?.trim() || null,
      subtotal: BigInt(unitPrice * input.quantity),
    };
  });
}

/** Nomor pesanan yang mudah dibaca: TR-20261118-0007 (urutan harian) */
export async function generateOrderNumber(tx: Tx): Promise<string> {
  const now = new Date();
  const dateStr = now.toISOString().slice(0, 10).replace(/-/g, '');
  const prefix = `TR-${dateStr}-`;
  const last = await tx.order.findFirst({
    where: { orderNumber: { startsWith: prefix } },
    orderBy: { orderNumber: 'desc' },
    select: { orderNumber: true },
  });
  const lastSeq = last ? Number.parseInt(last.orderNumber.slice(prefix.length), 10) || 0 : 0;
  return `${prefix}${String(lastSeq + 1).padStart(4, '0')}`;
}

/** Pesanan dianggap masih aktif di meja jika belum lunas dan belum dibatalkan/selesai */
export function isOrderActive(o: { status: string; paymentBillStatus: string }) {
  return o.paymentBillStatus === 'BELUM_BAYAR' && o.status !== 'DIBATALKAN';
}

/** Pastikan pesanan masih bisa diubah (belum lunas / dibatalkan) */
export function assertEditable(o: { status: string; paymentBillStatus: string; orderNumber: string }) {
  if (!isOrderActive(o)) {
    throw new AppError(409, `Pesanan ${o.orderNumber} sudah ditutup dan tidak bisa diubah`);
  }
}
