import { Request, Response } from 'express';
import { z } from 'zod';
import prisma from '../config/prisma.js';
import { orderInclude, serializeOrder } from '../utils/orderHelpers.js';

/**
 * Laporan penjualan untuk Panel Admin.
 * Hanya pesanan LUNAS yang dihitung sebagai pendapatan.
 * Waktu dihitung dalam zona WITA (UTC+8) agar "hari ini" sesuai jam kafe di Makassar.
 */

const TZ_OFFSET_MIN = Number(process.env.TZ_OFFSET_MINUTES ?? 480);
const OPEN_HOUR = 8;
const CLOSE_HOUR = 22;
const MONTHS = ['Jan', 'Feb', 'Mar', 'Apr', 'Mei', 'Jun', 'Jul', 'Agu', 'Sep', 'Okt', 'Nov', 'Des'];

/** Komponen tanggal lokal (WITA) dari sebuah Date */
function local(d: Date) {
  const t = new Date(d.getTime() + TZ_OFFSET_MIN * 60_000);
  return { y: t.getUTCFullYear(), m: t.getUTCMonth(), d: t.getUTCDate(), h: t.getUTCHours() };
}

/** Date UTC untuk awal jam lokal tertentu */
function fromLocal(y: number, m: number, d = 1) {
  return new Date(Date.UTC(y, m, d) - TZ_OFFSET_MIN * 60_000);
}

type Period = 'HARIAN' | 'BULANAN' | 'TAHUNAN';

function rangeFor(period: Period, ref: Date, shift = 0) {
  const { y, m, d } = local(ref);
  if (period === 'HARIAN') {
    const start = fromLocal(y, m, d + shift);
    return { start, end: new Date(start.getTime() + 86_400_000) };
  }
  if (period === 'BULANAN') {
    return { start: fromLocal(y, m + shift, 1), end: fromLocal(y, m + shift + 1, 1) };
  }
  return { start: fromLocal(y + shift, 0, 1), end: fromLocal(y + shift + 1, 0, 1) };
}

async function paidOrders(start: Date, end: Date) {
  return prisma.order.findMany({
    where: { paymentBillStatus: 'LUNAS', paidAt: { gte: start, lt: end } },
    select: { total: true, paidAt: true, items: { select: { name: true, quantity: true } } },
  });
}

const querySchema = z.object({
  period: z.enum(['HARIAN', 'BULANAN', 'TAHUNAN']).default('HARIAN'),
  // geser periode: 0 = sekarang, -1 = sebelumnya, dst.
  offset: z.coerce.number().int().min(-120).max(0).default(0),
});

/** GET /api/reports/summary?period=HARIAN|BULANAN|TAHUNAN&offset=0 */
export const getSummary = async (req: Request, res: Response) => {
  const { period, offset } = querySchema.parse(req.query);
  const now = new Date();
  const { start, end } = rangeFor(period, now, offset);
  const prev = rangeFor(period, now, offset - 1);

  const [orders, prevOrders, logOrders] = await Promise.all([
    paidOrders(start, end),
    paidOrders(prev.start, prev.end),
    // Log pesanan pada periode ini (semua status), terbaru dulu
    prisma.order.findMany({
      where: { createdAt: { gte: start, lt: end } },
      include: orderInclude,
      orderBy: { createdAt: 'desc' },
      take: 500,
    }),
  ]);

  // Label & keranjang grafik
  const s = local(start);
  let labels: string[];
  let bucketOf: (d: Date) => number;
  if (period === 'HARIAN') {
    labels = Array.from({ length: CLOSE_HOUR - OPEN_HOUR + 1 }, (_, i) => `${String(OPEN_HOUR + i).padStart(2, '0')}:00`);
    bucketOf = (d) => Math.min(Math.max(local(d).h, OPEN_HOUR), CLOSE_HOUR) - OPEN_HOUR;
  } else if (period === 'BULANAN') {
    const days = new Date(Date.UTC(s.y, s.m + 1, 0)).getUTCDate();
    labels = Array.from({ length: days }, (_, i) => String(i + 1));
    bucketOf = (d) => local(d).d - 1;
  } else {
    labels = MONTHS;
    bucketOf = (d) => local(d).m;
  }

  const values = labels.map(() => 0);
  const menuQty = new Map<string, number>();
  let total = 0;
  for (const o of orders) {
    const amount = Number(o.total);
    total += amount;
    if (o.paidAt) values[bucketOf(o.paidAt)] += amount;
    for (const it of o.items) menuQty.set(it.name, (menuQty.get(it.name) ?? 0) + it.quantity);
  }
  const prevTotal = prevOrders.reduce((a, o) => a + Number(o.total), 0);

  const rangeLabel =
    period === 'HARIAN'
      ? offset === 0
        ? 'Hari Ini'
        : `${s.d} ${MONTHS[s.m]} ${s.y}`
      : period === 'BULANAN'
        ? `${MONTHS[s.m]} ${s.y}`
        : `Tahun ${s.y}`;

  const unit = period === 'HARIAN' ? 'kemarin' : period === 'BULANAN' ? 'bulan lalu' : 'tahun lalu';
  let delta: string;
  if (prevTotal === 0) delta = total > 0 ? `Belum ada data ${unit}` : 'Belum ada transaksi';
  else {
    const pct = ((total - prevTotal) / prevTotal) * 100;
    delta = `${pct >= 0 ? '+' : ''}${pct.toFixed(1).replace('.', ',')}% dibanding ${unit}`;
  }

  const topMenus = [...menuQty.entries()]
    .sort((a, b) => b[1] - a[1])
    .slice(0, 5)
    .map(([name, quantity]) => ({ name, quantity }));

  return res.json({
    success: true,
    data: {
      period,
      offset,
      rangeLabel,
      labels,
      values,
      totalRevenue: total,
      orderCount: orders.length,
      avgPerOrder: orders.length ? Math.round(total / orders.length) : 0,
      delta,
      topMenus,
      orders: logOrders.map(serializeOrder),
    },
  });
};
