import { PrismaClient, Role, TableStatus } from '@prisma/client';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function main() {
  console.log('[Seed] Memulai pengisian data awal Cafe 3 Rasa...');

  // 1. Akun Pengguna (Kasir & Admin)
  const salt = await bcrypt.genSalt(10);
  const kasirPassword = await bcrypt.hash('kasir123', salt);
  const adminPassword = await bcrypt.hash('admin123', salt);

  await prisma.user.upsert({
    where: { username: 'kasir' },
    update: {},
    create: {
      username: 'kasir',
      passwordHash: kasirPassword,
      fullName: 'Kasir Utama',
      role: Role.KASIR,
    },
  });

  await prisma.user.upsert({
    where: { username: 'admin' },
    update: {},
    create: {
      username: 'admin',
      passwordHash: adminPassword,
      fullName: 'Administrator',
      role: Role.ADMIN,
    },
  });

  console.log('[Seed] User kasir dan admin berhasil dibuat');

  // 2. Kategori Menu
  const catMakanan = await prisma.category.upsert({
    where: { slug: 'makanan' },
    update: {},
    create: { name: 'Makanan', slug: 'makanan', displayOrder: 1 },
  });

  const catMinuman = await prisma.category.upsert({
    where: { slug: 'minuman' },
    update: {},
    create: { name: 'Minuman', slug: 'minuman', displayOrder: 2 },
  });

  const catCamilan = await prisma.category.upsert({
    where: { slug: 'camilan' },
    update: {},
    create: { name: 'Camilan', slug: 'camilan', displayOrder: 3 },
  });

  console.log('[Seed] Kategori menu berhasil dibuat');

  // 3. Meja (01 s/d 12)
  for (let i = 1; i <= 12; i++) {
    const tableNumber = i < 10 ? `0${i}` : `${i}`;
    await prisma.table.upsert({
      where: { number: tableNumber },
      update: {},
      create: {
        number: tableNumber,
        capacity: 4,
        status: TableStatus.KOSONG,
      },
    });
  }

  console.log('[Seed] 12 Meja Cafe 3 Rasa berhasil dibuat');

  // 4. Menu Items
  const menuList = [
    // Makanan
    {
      id: 'm1',
      name: 'Nasi Goreng Spesial 3 Rasa',
      categoryId: catMakanan.id,
      price: BigInt(28000),
      description: 'Nasi goreng racikan bumbu khas 3 Rasa dengan suwiran ayam, telur mata sapi, dan acar segar.',
      filters: ['Best Seller', 'Rekomendasi'],
    },
    {
      id: 'm2',
      name: 'Mie Goreng Aceh Spesial',
      categoryId: catMakanan.id,
      price: BigInt(26000),
      description: 'Mie kuning tebal dengan bumbu rempah kari Aceh, daging sapi empuk, dan emping renyah.',
      filters: ['Terlaris'],
    },
    {
      id: 'm3',
      name: 'Ayam Geprek Sambal Korek',
      categoryId: catMakanan.id,
      price: BigInt(24000),
      description: 'Ayam krispi renyah digeprek dengan sambal bawang korek super pedas gurih.',
      filters: ['Best Seller'],
    },
    {
      id: 'm4',
      name: 'Nasi Ayam Bakar Madu',
      categoryId: catMakanan.id,
      price: BigInt(30000),
      description: 'Ayam bakar dengan lumuran madu manis gurih, disajikan dengan lalapan dan sambal terasi.',
      filters: ['Rekomendasi'],
    },
    // Minuman
    {
      id: 'd1',
      name: 'Kopi Sanger Khas Aceh',
      categoryId: catMinuman.id,
      price: BigInt(16000),
      description: 'Kopi saring robusta dengan sentuhan susu kental manis berbusa lembut.',
      filters: ['Best Seller', 'Rekomendasi'],
    },
    {
      id: 'd2',
      name: 'Kopi Tubruk Robusta Gayo',
      categoryId: catMinuman.id,
      price: BigInt(14000),
      description: 'Kopi hitam murni pekat dengan aroma tajam dan cita rasa autentik.',
      filters: [],
    },
    {
      id: 'd3',
      name: 'Teh Tarik Panas / Dingin',
      categoryId: catMinuman.id,
      price: BigInt(15000),
      description: 'Teh susu buih khas melayu yang ditarik hingga menghasilkan busa halus.',
      filters: ['Terlaris'],
    },
    {
      id: 'd4',
      name: 'Es Cendol Durian 3 Rasa',
      categoryId: catMinuman.id,
      price: BigInt(22000),
      description: 'Cendol kenyal dengan santan segar, gula aren asli, dan topping daging durian legit.',
      filters: ['Best Seller'],
    },
    // Camilan
    {
      id: 's1',
      name: 'Pisang Goreng Keju Coklat',
      categoryId: catCamilan.id,
      price: BigInt(18000),
      description: 'Pisang kepok manis renyah dengan taburan keju cheddar parut dan susu coklat kental.',
      filters: ['Best Seller'],
    },
    {
      id: 's2',
      name: 'Kentang Goreng Crispy',
      categoryId: catCamilan.id,
      price: BigInt(16000),
      description: 'French fries garing gurih dengan cocolan saus sambal dan mayones.',
      filters: [],
    },
    {
      id: 's3',
      name: 'Roti Bakar Coklat Keju',
      categoryId: catCamilan.id,
      price: BigInt(20000),
      description: 'Roti tawar tebal panggang renyah isi selai coklat lumer dan keju leleh.',
      filters: ['Rekomendasi'],
    },
  ];

  for (const m of menuList) {
    await prisma.menuItem.upsert({
      where: { id: m.id },
      update: {},
      create: m,
    });
  }

  console.log('[Seed] Menu items berhasil dibuat');
  console.log('[Seed] Data awal Cafe 3 Rasa selesai disiapkan!');
}

main()
  .catch((e) => {
    console.error(e);
    process.exit(1);
  })
  .finally(async () => {
    await prisma.$disconnect();
  });
