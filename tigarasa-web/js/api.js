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
 * Kirim rating dan ulasan tamu ke backend
 */
async function apiCreateReview(tableNumber, rating, aspects = [], comment = '') {
  try {
    const tableNum = normalizeTableNumber(tableNumber);
    const payload = {
      tableNumber: tableNum,
      rating: Number(rating),
      aspects: aspects || [],
      comment: comment || undefined,
    };

    const res = await fetch(`${API_BASE_URL}/reviews`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    });

    const data = await res.json().catch(() => ({ success: false, message: `Server error (${res.status})` }));
    return data;
  } catch (err) {
    console.error('[API] Gagal mengirim review:', err);
    return { success: false, message: err.message };
  }
}

// Ekspor ke window agar bisa diakses di semua script halaman HTML
window.Cafe3RasaAPI = {
  initTableSocket,
  apiCreateOrder,
  apiCreateServiceRequest,
  apiCreateReview,
};
