// ================================================================
// Data Menu & Kategori Tiga Rasa
// ================================================================

// 1. Kategori Kurasi / Label Pilihan Admin (Ditampilkan di chips atas)
const CURATED_TAGS = ['Semua', 'Best Seller', 'Terlaris', 'Rekomendasi'];

// 2. Kategori Menu Pokok (Ditampilkan di filter bawah: Makanan, Minuman, Camilan, dll)
const DEFAULT_CATEGORIES = ['Semua', 'Makanan', 'Minuman', 'Camilan'];

const MENU = [
  {
    id: 1,
    name: 'Nasi Campur 3 Rasa Spesial',
    category: 'Makanan',
    price: 45000,
    img: 'img/nasi-campur.jpg',
    desc: 'Ayam rempah Madura, cumi hitam pedas, dan sambal matah khas Maharewa disajikan hangat.',
    tag: 'TERLARIS',
    tagStyle: 'coral',
    curated: ['Terlaris', 'Best Seller'],
    featured: true
  },
  {
    id: 2,
    name: 'Kopi Susu Gula Aren 3 Rasa',
    category: 'Minuman',
    price: 22000,
    img: 'img/kopi-susu.jpg',
    desc: 'Single origin espresso, susu segar, gula aren organik.',
    tag: 'BEST SELLER',
    tagStyle: 'navy',
    curated: ['Best Seller', 'Terlaris']
  },
  {
    id: 3,
    name: 'Pisang Goreng Wijen Karamel',
    category: 'Camilan',
    price: 18000,
    img: 'img/pisang-goreng.jpg',
    desc: 'Pisang kepok renyah dengan taburan wijen dan siraman karamel gula aren.',
    tag: 'BEST SELLER',
    tagStyle: 'coral',
    curated: ['Best Seller']
  },
  {
    id: 4,
    name: 'Bakmi Ayam Jamur Maharewa',
    category: 'Makanan',
    price: 32000,
    img: 'img/bakmi.jpg',
    desc: 'Mie kenyal gurih dengan kuah kaldu ayam kampung & pangsit goreng renyah.',
    tag: 'REKOMENDASI',
    tagStyle: 'navy',
    curated: ['Rekomendasi', 'Best Seller']
  },
  {
    id: 5,
    name: 'Teh Tarik Rempah Hangat',
    category: 'Minuman',
    price: 16000,
    img: 'img/teh-tarik.jpg',
    desc: 'Racikan teh hitam, kapulaga, cengkeh & susu kental manis.',
    tag: 'REKOMENDASI',
    tagStyle: 'coral',
    curated: ['Rekomendasi']
  }
];

const TAX_RATE = 0.10; // PB1 / pajak resto 10%
