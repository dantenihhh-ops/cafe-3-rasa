import 'dotenv/config';
import prisma from '../src/config/prisma.js';

/**
 * Hapus 11 menu lama dari seed versi sebelumnya (ID m1-m4, d1-d4, s1-s3).
 * Menu yang dipakai sekarang (ID "1"-"5") TIDAK disentuh.
 *
 * Cara pakai (dari folder backend):
 *   npx tsx prisma/cleanup-old-menu.ts            -> hanya menampilkan daftar (tidak menghapus)
 *   npx tsx prisma/cleanup-old-menu.ts --confirm  -> benar-benar menghapus
 *
 * Riwayat pesanan tetap aman: nama & harga item pesanan sudah disalin saat memesan,
 * kolom menuItemId pada pesanan lama otomatis menjadi kosong (null).
 */

const OLD_MENU_IDS = ['m1', 'm2', 'm3', 'm4', 'd1', 'd2', 'd3', 'd4', 's1', 's2', 's3'];


async function main() {
  const confirm = process.argv.includes('--confirm');

  const oldMenus = await prisma.menuItem.findMany({
    where: { id: { in: OLD_MENU_IDS } },
    include: { _count: { select: { orderItems: true } } },
    orderBy: { id: 'asc' },
  });

  if (oldMenus.length === 0) {
    console.log('[Cleanup] Tidak ada menu lama. Database sudah bersih.');
    return;
  }

  console.log(`[Cleanup] Ditemukan ${oldMenus.length} menu lama:`);
  for (const m of oldMenus) {
    const used = m._count.orderItems > 0 ? `  (dipakai di ${m._count.orderItems} item pesanan lama)` : '';
    console.log(`  - ${m.id.padEnd(3)} ${m.name}${used}`);
  }

  if (!confirm) {
    console.log('\n[Cleanup] Mode cek saja, belum ada yang dihapus.');
    console.log('[Cleanup] Untuk menghapus jalankan: npx tsx prisma/cleanup-old-menu.ts --confirm');
    return;
  }

  const result = await prisma.menuItem.deleteMany({ where: { id: { in: OLD_MENU_IDS } } });
  console.log(`\n[Cleanup] ${result.count} menu lama berhasil dihapus.`);

  const remaining = await prisma.menuItem.findMany({ orderBy: { id: 'asc' }, select: { id: true, name: true } });
  console.log(`[Cleanup] Menu yang tersisa (${remaining.length}):`);
  for (const m of remaining) console.log(`  - ${m.id.padEnd(3)} ${m.name}`);
}

main()
  .catch((e) => {
    console.error('[Cleanup] Gagal:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
