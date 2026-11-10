// ================================================================
// Data menu (sementara). Nanti diganti data dari Firebase / server
// yang diisi admin lewat aplikasi Android.
// ================================================================
const CATEGORIES = ['Semua', 'Makanan', 'Minuman', 'Camilan'];

const MENU = [
  { id: 1, name: 'Nasi Campur 3 Rasa Spesial', category: 'Makanan', price: 45000, img: 'img/nasi-campur.jpg',
    desc: 'Ayam rempah Madura, cumi hitam pedas, dan sambal matah khas Maharewa disajikan hangat.',
    tag: 'SIGNATURE', tagStyle: 'cream', featured: true },
  { id: 2, name: 'Kopi Susu Gula Aren 3 Rasa', category: 'Minuman', price: 22000, img: 'img/kopi-susu.jpg',
    desc: 'Single origin espresso, susu segar, gula aren organik.', tag: 'ES / PANAS', tagStyle: '' },
  { id: 3, name: 'Pisang Goreng Wijen Karamel', category: 'Camilan', price: 18000, img: 'img/pisang-goreng.jpg',
    desc: 'Pisang kepok renyah dengan taburan wijen dan siraman karamel gula aren.', tag: 'CAMILAN', tagStyle: 'coral' },
  { id: 4, name: 'Bakmi Ayam Jamur Maharewa', category: 'Makanan', price: 32000, img: 'img/bakmi.jpg',
    desc: 'Mie kenyal gurih dengan kuah kaldu ayam kampung & pangsit goreng renyah.', tag: 'REKOMENDASI', tagStyle: 'navy' },
  { id: 5, name: 'Teh Tarik Rempah Hangat', category: 'Minuman', price: 16000, img: 'img/teh-tarik.jpg',
    desc: 'Racikan teh hitam, kapulaga, cengkeh & susu kental manis.', tag: 'HANGAT', tagStyle: 'coral' }
];

const TAX_RATE = 0.10;   // PB1 / pajak resto 10%
