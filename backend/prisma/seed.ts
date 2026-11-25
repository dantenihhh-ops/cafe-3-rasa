import 'dotenv/config';
import { Role } from '@prisma/client';
import prisma from '../src/config/prisma.js';
import bcrypt from 'bcryptjs';


/**
 * Data awal Cafe 3 Rasa — disamakan dengan menu di web pelanggan (tigarasa-web/js/data.js).
 * ID menu sengaja "1".."5" agar cocok dengan ID yang dikirim web saat memesan.
 * Seed aman dijalankan berulang kali (upsert): data yang sudah ada ikut diperbarui.
 */

const USERS = [
  { username: 'kasir', password: process.env.SEED_KASIR_PASSWORD || 'kasir123', fullName: 'Kasir Utama', role: Role.KASIR },
  { username: 'admin', password: process.env.SEED_ADMIN_PASSWORD || 'admin123', fullName: 'Administrator', role: Role.ADMIN },
];

const CATEGORIES = [
  { name: 'Makanan', slug: 'makanan', displayOrder: 1 },
  { name: 'Minuman', slug: 'minuman', displayOrder: 2 },
  { name: 'Camilan', slug: 'camilan', displayOrder: 3 },
];

const MENU = [
  {
    id: '1',
    name: 'Nasi Campur 3 Rasa Spesial',
    category: 'makanan',
    price: 45000,
    image: 'img/nasi-campur.jpg',
    description: 'Ayam rempah Madura, cumi hitam pedas, dan sambal matah khas Maharewa disajikan hangat.',
    filters: ['Terlaris', 'Best Seller'],
  },
  {
    id: '2',
    name: 'Kopi Susu Gula Aren 3 Rasa',
    category: 'minuman',
    price: 22000,
    image: 'img/kopi-susu.jpg',
    description: 'Single origin espresso, susu segar, gula aren organik.',
    filters: ['Best Seller', 'Terlaris'],
  },
  {
    id: '3',
    name: 'Pisang Goreng Wijen Karamel',
    category: 'camilan',
    price: 18000,
    image: 'img/pisang-goreng.jpg',
    description: 'Pisang kepok renyah dengan taburan wijen dan siraman karamel gula aren.',
    filters: ['Best Seller'],
  },
  {
    id: '4',
    name: 'Bakmi Ayam Jamur Maharewa',
    category: 'makanan',
    price: 32000,
    image: 'img/bakmi.jpg',
    description: 'Mie kenyal gurih dengan kuah kaldu ayam kampung & pangsit goreng renyah.',
    filters: ['Rekomendasi', 'Best Seller'],
  },
  {
    id: '5',
    name: 'Teh Tarik Rempah Hangat',
    category: 'minuman',
    price: 16000,
    image: 'img/teh-tarik.jpg',
    description: 'Racikan teh hitam, kapulaga, cengkeh & susu kental manis.',
    filters: ['Rekomendasi'],
  },
];

const TABLE_COUNT = 12;

async function main() {
  console.log('[Seed] Mengisi data awal Cafe 3 Rasa...');

  // 1. Akun kasir & admin
  for (const u of USERS) {
    const passwordHash = await bcrypt.hash(u.password, 10);
    await prisma.user.upsert({
      where: { username: u.username },
      update: {},
      create: { username: u.username, passwordHash, fullName: u.fullName, role: u.role },
    });
  }
  console.log(`[Seed] ${USERS.length} akun (kasir, admin)`);

  // 2. Kategori
  const categoryIdBySlug: Record<string, string> = {};
  for (const c of CATEGORIES) {
    const cat = await prisma.category.upsert({
      where: { slug: c.slug },
      update: { name: c.name, displayOrder: c.displayOrder },
      create: c,
    });
    categoryIdBySlug[c.slug] = cat.id;
  }
  console.log(`[Seed] ${CATEGORIES.length} kategori`);

  // 3. Meja 01..12 (01-08 Indoor, 09-12 Outdoor)
  for (let i = 1; i <= TABLE_COUNT; i++) {
    const number = String(i).padStart(2, '0');
    const area = i <= 8 ? 'Indoor' : 'Outdoor';
    await prisma.table.upsert({
      where: { number },
      update: { area },
      create: { number, area, capacity: 4 },
    });
  }
  console.log(`[Seed] ${TABLE_COUNT} meja`);

  // 4. Menu
  for (const m of MENU) {
    const data = {
      name: m.name,
      categoryId: categoryIdBySlug[m.category],
      price: BigInt(m.price),
      image: m.image,
      description: m.description,
      filters: m.filters,
    };
    await prisma.menuItem.upsert({
      where: { id: m.id },
      update: data,
      create: { id: m.id, ...data },
    });
  }
  console.log(`[Seed] ${MENU.length} menu`);

  console.log('[Seed] Selesai!');
}

main()
  .catch((e) => {
    console.error('[Seed] Gagal:', e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
