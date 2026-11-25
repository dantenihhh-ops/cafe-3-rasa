/**
 * Cafe 3 Rasa - Web E-Menu API & Socket.io Client Service
 * Author: Maharewa
 */

// Jika web dibuka dari server backend (port 5000) -> pakai alamat yang sama.
// Jika dibuka dari Live Server (port 5500) di laptop/HP -> arahkan ke port 5000 di host yang sama.
const BACKEND_PORT = '5000';
const SERVER_ORIGIN = window.location.port === BACKEND_PORT || window.location.port === ''
  ? window.location.origin
  : `${window.location.protocol}//${window.location.hostname}:${BACKEND_PORT}`;

const API_BASE_URL = `${SERVER_ORIGIN}/api`;
const SOCKET_SERVER_URL = SERVER_ORIGIN;

let socket = null;

function normalizeTableNumber(tableNum) {
  if (!tableNum) return '04';
  const clean = String(tableNum).replace(/\D/g, '');
  return clean ? clean.padStart(2, '0') : '04';
}

/**
 * Inisialisasi koneksi Socket.io untuk meja saat ini
 */
function initTableSocket(tableNumber) {
  const tableNum = normalizeTableNumber(tableNumber);
  if (typeof io === 'undefined') {
    console.warn('[Socket.io] Library socket.io belum dimuat di HTML.');
    return;
  }

  try {
    socket = io(SOCKET_SERVER_URL, {
      transports: ['websocket', 'polling'],
      reconnectionAttempts: 5,
    });

    socket.on('connect', () => {
      console.log(`[Socket.io] Terhubung ke server, bergabung ke room meja: table_${tableNum}`);
      socket.emit('join_table', tableNum);
    });

    // Dengarkan saat kasir mengonfirmasi pesanan meja ini
    socket.on('order_confirmed', (data) => {
      console.log('[Socket.io] Pesanan dikonfirmasi oleh kasir:', data);
      const event = new CustomEvent('cafe3rasa:order_confirmed', { detail: data });
      window.dispatchEvent(event);
    });

    // Dengarkan saat kasir mengirim balasan bantuan / panggil pelayan
    socket.on('service_confirmed', (data) => {
      console.log('[Socket.io] Balasan bantuan diterima dari kasir:', data);
      const event = new CustomEvent('cafe3rasa:service_confirmed', { detail: data });
      window.dispatchEvent(event);
    });

    // Kasir menandai permintaan bantuan sudah selesai ditangani
    socket.on('service_completed', (data) => {
      window.dispatchEvent(new CustomEvent('cafe3rasa:service_completed', { detail: data }));
    });

    // Admin/kasir mengubah menu (harga, stok habis, menu baru)
    socket.on('menu_changed', (data) => {
      window.dispatchEvent(new CustomEvent('cafe3rasa:menu_changed', { detail: data }));
    });

    // Setiap perubahan pesanan meja ini (konfirmasi, disajikan, ubah item, bayar, batal)
    socket.on('order_updated', (data) => {
      window.dispatchEvent(new CustomEvent('cafe3rasa:order_updated', { detail: data }));
    });

    // Kasir menandai pesanan sudah disajikan / diantar ke meja
    socket.on('order_served', (data) => {
      window.dispatchEvent(new CustomEvent('cafe3rasa:order_served', { detail: data }));
    });

    // Meja dikosongkan / dipindah oleh kasir -> halaman status dimuat ulang
    socket.on('table_status_changed', (data) => {
      window.dispatchEvent(new CustomEvent('cafe3rasa:table_status_changed', { detail: data }));
    });

    // Dengarkan saat kasir memproses pembayaran lunas
    socket.on('payment_completed', (data) => {
      console.log('[Socket.io] Pembayaran lunas dikonfirmasi:', data);
      const event = new CustomEvent('cafe3rasa:payment_completed', { detail: data });
      window.dispatchEvent(event);
    });
  } catch (err) {
    console.error('[Socket.io] Gagal menghubungkan ke WebSocket server:', err);
  }
}

/**
 * Kirim pesanan baru ke backend API
 */
async function apiCreateOrder(tableNumber, cartItems, customerNotes = '') {
  try {
    const tableNum = normalizeTableNumber(tableNumber);
    const payload = {
      tableNumber: tableNum,
      orderSource: 'EMENU',
      customerNotes: customerNotes || undefined,
      // Cukup kirim ID menu, jumlah & catatan. Nama & harga diambil server dari database.
      items: cartItems.map((item) => ({
        menuItemId: String(item.id),
        quantity: Number(item.qty || 1),
        note: item.note || undefined,
      })),
    };

    const res = await fetch(`${API_BASE_URL}/orders`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    const data = await res.json().catch(() => ({ success: false, message: `Server error (${res.status})` }));
    return data;
  } catch (err) {
    console.error('[API] Gagal mengirim pesanan ke server:', err);
    return { success: false, message: 'Tidak dapat terhubung ke server kasir. Periksa koneksi internet.' };
  }
}

/**
 * Kirim permintaan bantuan / panggil pelayan ke backend
 */
async function apiCreateServiceRequest(tableNumber, type, message = '') {
  try {
    const tableNum = normalizeTableNumber(tableNumber);
    const payload = {
      tableNumber: tableNum,
      type: type || 'PANGGIL_PELAYAN',
      message: message || undefined,
    };

    const res = await fetch(`${API_BASE_URL}/requests`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    const data = await res.json().catch(() => ({ success: false, message: `Server error (${res.status})` }));
    return data;
  } catch (err) {
    console.error('[API] Gagal mengirim permintaan bantuan:', err);
    return { success: false, message: err.message };
  }
}

/**
 * Kirim rating dan ulasan tamu ke backend.
 * Jika reviewId ada (pelanggan menekan "Ubah rating"), ulasan lama diperbarui
 * sehingga satu pelanggan tidak terhitung dua kali. Jika ulasan lama sudah
 * tidak bisa diubah (lewat 6 jam / tidak ditemukan), dibuat ulasan baru.
 * @returns {Promise<{success:boolean, data?:{id:string}, message?:string}>}
 */
async function apiCreateReview(tableNumber, rating, aspects = [], comment = '', reviewId = null) {
  const payload = {
    tableNumber: normalizeTableNumber(tableNumber),
    rating: Number(rating),
    aspects: aspects || [],
    comment: comment || undefined,
  };
  const send = async (method, url) => {
    const res = await fetch(url, {
      method,
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });
    const data = await res.json().catch(() => ({ success: false, message: `Server error (${res.status})` }));
    return { status: res.status, data };
  };
  try {
    if (reviewId) {
      const r = await send('PATCH', `${API_BASE_URL}/reviews/${encodeURIComponent(reviewId)}`);
      if (r.status !== 404) return r.data;
    }
    return (await send('POST', `${API_BASE_URL}/reviews`)).data;
  } catch (err) {
    console.error('[API] Gagal mengirim review:', err);
    return { success: false, message: 'Tidak dapat terhubung ke server.' };
  }
}

/**
 * Ambil daftar kategori dari database (urutan sesuai admin).
 * @returns {Promise<string[]|null>} null jika gagal
 */
async function apiLoadCategories() {
  try {
    const res = await fetch(`${API_BASE_URL}/categories`, { cache: 'no-store' });
    const json = await res.json();
    if (!json.success || !Array.isArray(json.data)) return null;
    return json.data.map((c) => c.name).filter(Boolean);
  } catch {
    return null;
  }
}

// Ekspor ke window agar bisa diakses di semua script halaman HTML
/**
 * Ambil pesanan aktif meja dari server (sumber kebenaran halaman Status)
 * @returns {Promise<{success:boolean, data?:object|null, message?:string}>}
 */
async function apiGetActiveOrder(tableNumber) {
  try {
    const tableNum = normalizeTableNumber(tableNumber);
    const res = await fetch(`${API_BASE_URL}/orders/table/${tableNum}/active`, { cache: 'no-store' });
    return await res.json().catch(() => ({ success: false, message: `Server error (${res.status})` }));
  } catch (err) {
    return { success: false, message: 'Tidak dapat terhubung ke server kasir.' };
  }
}

/**
 * Ambil menu tersedia dari database lalu ubah ke format yang dipakai halaman web.
 * @returns {Promise<Array|null>} null jika gagal (web memakai data bawaan data.js)
 */
async function apiLoadMenu() {
  try {
    const res = await fetch(`${API_BASE_URL}/menu?availableOnly=true`, { cache: 'no-store' });
    const json = await res.json();
    if (!json.success || !Array.isArray(json.data)) return null;
    return json.data.map((m) => {
      const filters = Array.isArray(m.filters) ? m.filters : [];
      const label = filters[0] || m.categoryName || 'Menu';
      return {
        id: String(m.id),
        name: m.name,
        category: m.categoryName,
        price: Number(m.price) || 0,
        img: m.image || 'img/logo.png',
        desc: m.description || '',
        tag: label.toUpperCase(),
        tagStyle: filters.includes('Terlaris') || filters.includes('Rekomendasi') ? 'coral' : 'navy',
        curated: filters,
      };
    });
  } catch (err) {
    console.warn('[API] Menu dari server gagal dimuat, memakai data bawaan:', err);
    return null;
  }
}

window.Cafe3RasaAPI = {
  apiGetActiveOrder,
  apiLoadMenu,
  apiLoadCategories,
  initTableSocket,
  apiCreateOrder,
  apiCreateServiceRequest,
  apiCreateReview,
};
