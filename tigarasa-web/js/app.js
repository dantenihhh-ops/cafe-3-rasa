// ================================================================
// Tiga Rasa — Web Pelanggan
// Nomor meja dibaca dari link QR, contoh: index.html?meja=04
// Keranjang & pesanan disimpan di localStorage (per meja) untuk sementara.
// ================================================================

// ---------- Nomor meja ----------
const params = new URLSearchParams(location.search);
const MEJA = (params.get('meja') || sessionStorage.getItem('meja') || '04').padStart(2, '0');
sessionStorage.setItem('meja', MEJA);
const withMeja = (page) => `${page}?meja=${MEJA}`;

// ---------- Penyimpanan ----------
const KEY_CART = `tigarasa_cart_${MEJA}`;
const KEY_ORDERS = `tigarasa_orders_${MEJA}`;
const KEY_REQ = `tigarasa_requests_${MEJA}`;
const load = (k, def) => { try { return JSON.parse(localStorage.getItem(k)) ?? def; } catch { return def; } };
const save = (k, v) => { try { localStorage.setItem(k, JSON.stringify(v)); } catch {} };

let cart = load(KEY_CART, []);            // [{ id, qty, note }]
const saveCart = () => save(KEY_CART, cart);

// ---------- Utilitas ----------
const rupiah = (n) => 'Rp ' + Math.round(n).toLocaleString('id-ID');
// ID menu dibandingkan sebagai teks (ID dari database bisa berupa angka atau kode)
const findMenu = (id) => MENU.find((m) => String(m.id) === String(id));
const calcTax = (sub) => Math.round(sub * (typeof TAX_RATE === 'number' ? TAX_RATE : 0));
const taxLabel = () => `Pajak PB1 (${Math.round((typeof TAX_RATE === 'number' ? TAX_RATE : 0) * 100)}%)`;
const cartCount = () => cart.reduce((a, c) => a + c.qty, 0);
const cartSubtotal = () => cart.reduce((a, c) => a + (findMenu(c.id)?.price || 0) * c.qty, 0);
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

function addToCart(id) {
  const item = cart.find((c) => String(c.id) === String(id));
  if (item) item.qty += 1; else cart.push({ id, qty: 1, note: '' });
  saveCart();
}
function changeQty(id, delta) {
  const item = cart.find((c) => String(c.id) === String(id));
  if (!item) return;
  item.qty += delta;
  if (item.qty <= 0) cart = cart.filter((c) => String(c.id) !== String(id));
  saveCart();
}

let toastTimer;
function toast(msg) {
  let el = document.querySelector('.toast');
  if (!el) { el = document.createElement('div'); el.className = 'toast'; el.setAttribute('role', 'status'); document.body.appendChild(el); }
  el.textContent = msg;
  el.classList.add('show');
  clearTimeout(toastTimer);
  toastTimer = setTimeout(() => el.classList.remove('show'), 2200);
}

// ---------- Ikon (SVG inline) ----------
const ICON = {
  user: '<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="8" r="3.5"/><path d="M5 20v-1a5 5 0 0 1 5-5h4a5 5 0 0 1 5 5v1z"/></svg>',
  plus: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" aria-hidden="true"><path d="M12 5v14M5 12h14"/></svg>',
  arrowR: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12h14M13 6l6 6-6 6"/></svg>',
  arrowL: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.3" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M19 12H5M11 6l-6 6 6 6"/></svg>',
  bag: '<svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.4" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M6 7h12l1 14H5z"/><path d="M9 7a3 3 0 0 1 6 0"/></svg>',
  edit: '<svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="#318380" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M11 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-6"/><path d="M18.5 2.5a2.1 2.1 0 0 1 3 3L12 15l-4 1 1-4z"/></svg>',
  check: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M5 12.5l4.5 4.5L19 7"/></svg>',
  cash: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><rect x="3" y="6.5" width="18" height="11" rx="2"/><circle cx="12" cy="12" r="2.2"/></svg>',
  menu: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4 4l2.5-.5L15 12l-3 3-8.5-8.5z"/><path d="M16.5 3.5c2 0 4 2 4 4s-1.5 3.5-3.5 3.5-3-1-3-3 .5-4.5 2.5-4.5z"/><path d="M15 10L4.5 20.5"/><path d="M13.5 15.5l5 5"/></svg>',
  receipt: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M7 3h11v15a3 3 0 0 1-3 3H6a3 3 0 0 1-3-3v-2h4z"/><path d="M7 16v2a3 3 0 0 1-3 3"/><path d="M10.5 8h4.5M10.5 12h4.5"/></svg>',
  cloche: '<svg width="22" height="22" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M4.5 16.5a7.5 7.5 0 0 1 15 0z"/><path d="M12 9V7.5"/><path d="M10.5 7.5h3"/><path d="M3 19.5h18"/></svg>',
  pot: '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M3 12h14v2a5 5 0 0 1-5 5H8a5 5 0 0 1-5-5z"/><path d="M17 13.5h4"/><path d="M7 4c-1 1.5 1 2.5 0 4.5M11 4c-1 1.5 1 2.5 0 4.5"/></svg>'
};

// ---------- Komponen bersama ----------
function renderHeader(subtitle) {
  const el = document.getElementById('header');
  if (!el) return;
  el.innerHTML = `
    <div class="brand">
      <img src="img/logo.png" alt="Logo Tiga Rasa by Maharewa">
      <div><div class="brand-name">Tiga Rasa</div><div class="brand-sub">${subtitle}</div></div>
    </div>
    <div class="header-right">
      <span class="table-badge">MEJA ${MEJA}</span>
      <button class="icon-btn" type="button" aria-label="Profil">${ICON.user}</button>
    </div>`;
}

function renderNav(active) {
  const el = document.getElementById('bottom-nav');
  if (!el) return;
  const items = [
    ['index.html', 'Menu', ICON.menu, 'menu'],
    ['status.html', 'Pesanan', ICON.receipt, 'status'],
    ['bantuan.html', 'Bantuan', ICON.cloche, 'bantuan']
  ];
  el.innerHTML = items.map(([href, label, icon, key]) =>
    `<a href="${withMeja(href)}" class="${key === active ? 'active' : ''}" ${key === active ? 'aria-current="page"' : ''}>${icon}${label}</a>`
  ).join('');
}

function renderCartBar() {
  const el = document.getElementById('cart-bar');
  if (!el) return;
  const n = cartCount();
  document.body.classList.toggle('has-cart', n > 0);
  el.hidden = n === 0;
  el.innerHTML = n === 0 ? '' : `
    <div class="inner">
      <div>
        <div style="display:flex;align-items:center;gap:10px">
          <span class="cart-count">${ICON.bag}${n} Item</span>
          <span class="cart-total">${rupiah(cartSubtotal())}</span>
        </div>
        <small class="muted">Meja ${MEJA} • Sedang Aktif</small>
      </div>
      <a class="cta" href="${withMeja('pesanan.html')}">Lihat Pesanan ${ICON.arrowR}</a>
    </div>`;
}

// ================================================================
// Halaman: MENU (index.html)
// ================================================================
function pageMenu() {
  renderHeader('Menu Catalog');
  renderNav('menu');
  document.getElementById('welcome-text').textContent = `Selamat Datang di Tiga Rasa • Meja ${MEJA}`;

  // Muat kategori dari DEFAULT_CATEGORIES + kategori tambahan admin (localStorage)
  const extraCats = (() => {
    try { return JSON.parse(localStorage.getItem('tigarasa_categories')) || []; } catch { return []; }
  })();
  const ALL_CATEGORIES = [...new Set([...DEFAULT_CATEGORIES, ...extraCats, ...MENU.map((m) => m.category).filter(Boolean)])];

  // Filter tag dari admin (CURATED_TAGS tanpa 'Semua')
  const FILTER_TAGS = CURATED_TAGS.filter((t) => t !== 'Semua');

  // State filter
  let activeCategory = 'Semua'; // kategori produk (chips)
  let activeSort     = null;    // 'asc' | 'desc' | null
  let activeTag      = null;    // nama tag e.g. 'Best Seller' | null

  const categoryEl     = document.getElementById('category-chips');
  const listEl         = document.getElementById('menu-list');
  const filterBtn      = document.getElementById('filter-btn');
  const filterLabel    = document.getElementById('filter-label');
  const filterDropdown = document.getElementById('filter-dropdown');
  const filterTagOpts  = document.getElementById('filter-tag-options');
  const filterReset    = document.getElementById('filter-reset');

  function qtyOf(id) { return cart.find((c) => String(c.id) === String(id))?.qty || 0; }

  // ── Render tag filter dari admin ke dalam dropdown ──
  filterTagOpts.innerHTML = FILTER_TAGS.map((t) =>
    `<button class="filter-option" type="button" data-tag="${esc(t)}">${esc(t)}</button>`
  ).join('');

  function getFilterLabel() {
    const parts = [];
    if (activeTag) parts.push(activeTag);
    if (activeSort === 'asc') parts.push('Termurah');
    if (activeSort === 'desc') parts.push('Termahal');
    return parts.length ? parts.join(', ') : 'Filter';
  }

  function updateFilterBtn() {
    const hasFilter = activeSort || activeTag;
    filterLabel.textContent = getFilterLabel();
    filterBtn.classList.toggle('filter-active', !!hasFilter);
    filterDropdown.querySelectorAll('[data-sort]').forEach((b) =>
      b.classList.toggle('selected', b.dataset.sort === activeSort));
    filterDropdown.querySelectorAll('[data-tag]').forEach((b) =>
      b.classList.toggle('selected', b.dataset.tag === activeTag));
  }

  function closeDropdown() {
    filterDropdown.hidden = true;
    filterBtn.setAttribute('aria-expanded', 'false');
  }

  function draw() {
    // ── Render chips kategori ──
    categoryEl.innerHTML = ALL_CATEGORIES.map((c) =>
      `<button type="button" class="chip ${c === activeCategory ? 'active' : ''}" data-cat="${esc(c)}" aria-pressed="${c === activeCategory}">${esc(c)}</button>`
    ).join('');

    updateFilterBtn();

    // ── Filter item menu berdasarkan kategori + tag (semua item termasuk yang sebelumnya featured) ──
    let items = MENU.filter((m) => {
      const matchCat = activeCategory === 'Semua' || m.category === activeCategory;
      const matchTag = !activeTag || (m.curated || []).includes(activeTag);
      return matchCat && matchTag;
    });

    // ── Sort harga ──
    if (activeSort === 'asc')  items = [...items].sort((a, b) => a.price - b.price);
    if (activeSort === 'desc') items = [...items].sort((a, b) => b.price - a.price);

    document.getElementById('menu-count').textContent = `${items.length} Menu`;
    listEl.innerHTML = items.length ? items.map((m) => {
      const q = qtyOf(m.id);
      return `
      <article class="card menu-item">
        <img src="${m.img}" alt="${esc(m.name)}" loading="lazy">
        <div class="info">
          <span class="tag ${m.tagStyle}">${esc(m.tag)}</span>
          <h4>${esc(m.name)}</h4>
          <p class="clamp2">${esc(m.desc)}</p>
          <div class="price-row">
            <div><div class="price">${rupiah(m.price)}</div>${q ? `<span class="in-cart">${q} di keranjang</span>` : ''}</div>
            <button class="add-btn sm" type="button" data-add="${m.id}" aria-label="Tambah ${esc(m.name)}">${ICON.plus}</button>
          </div>
        </div>
      </article>`;
    }).join('') : '<div class="card empty">Belum ada menu di kategori ini.</div>';

    renderCartBar();
  }

  // ── Toggle dropdown filter ──
  filterBtn.addEventListener('click', (e) => {
    e.stopPropagation();
    const isOpen = !filterDropdown.hidden;
    filterDropdown.hidden = isOpen;
    filterBtn.setAttribute('aria-expanded', String(!isOpen));
  });

  // ── Global click handler (tutup dropdown + navigasi chip + keranjang) ──
  document.addEventListener('click', (e) => {
    // Tutup dropdown jika klik di luar filter-wrap
    if (!document.getElementById('filter-wrap')?.contains(e.target)) {
      closeDropdown();
    }

    // Klik chip kategori
    const cat = e.target.closest('[data-cat]');
    if (cat) { activeCategory = cat.dataset.cat; draw(); return; }

    // Klik sort di dropdown
    const sort = e.target.closest('[data-sort]');
    if (sort) {
      activeSort = activeSort === sort.dataset.sort ? null : sort.dataset.sort;
      closeDropdown(); draw(); return;
    }

    // Klik tag filter di dropdown
    const tag = e.target.closest('[data-tag]');
    if (tag) {
      activeTag = activeTag === tag.dataset.tag ? null : tag.dataset.tag;
      closeDropdown(); draw(); return;
    }

    // Tambah ke keranjang
    const add = e.target.closest('[data-add]');
    if (add) {
      const m = findMenu(add.dataset.add);
      addToCart(m.id); draw(); toast(`${m.name} ditambahkan`);
    }
  });

  // ── Reset semua filter ──
  filterReset.addEventListener('click', (e) => {
    e.stopPropagation();
    activeSort = null; activeTag = null;
    closeDropdown(); draw();
  });

  draw();
}

// ================================================================
// Halaman: KONFIRMASI PESANAN (pesanan.html)
// ================================================================
function pagePesanan() {
  const listEl = document.getElementById('order-list');
  const summaryEl = document.getElementById('summary');
  const sendBtn = document.getElementById('send-btn');
  document.getElementById('table-badge').textContent = `MEJA ${MEJA}`;
  document.getElementById('back-link').href = withMeja('index.html');
  document.getElementById('more-link').href = withMeja('index.html');
  document.getElementById('lock-table').textContent = `Meja ${MEJA}`;

  function draw() {
    const n = cartCount();
    document.getElementById('menu-chosen').textContent = `${cart.length} Menu Dipilih`;

    listEl.innerHTML = cart.length ? cart.map((c) => {
      const m = findMenu(c.id);
      return `
      <article class="card order-card">
        <div class="order-top">
          <div class="left">
            <span class="tag ${m.tagStyle}">${esc(m.tag)}</span>
            <h4>${esc(m.name)}</h4>
            <small class="muted">${esc(m.desc)}</small>
          </div>
          <div class="right"><div class="price">${rupiah(m.price)}</div><small class="muted">per porsi</small></div>
        </div>
        <div class="qty-row">
          <span class="muted" style="font-size:13.5px">Jumlah:</span>
          <div style="display:flex;align-items:center;gap:14px">
            <div class="stepper">
              <button type="button" data-minus="${m.id}" aria-label="Kurangi ${esc(m.name)}">−</button>
              <span>${c.qty}</span>
              <button type="button" data-plus="${m.id}" aria-label="Tambah ${esc(m.name)}">+</button>
            </div>
            <b style="min-width:78px;text-align:right">${rupiah(m.price * c.qty)}</b>
          </div>
        </div>
        <label class="note">${ICON.edit}
          <input type="text" maxlength="80" data-note="${m.id}" value="${esc(c.note)}" placeholder="Tambah catatan untuk dapur..." aria-label="Catatan untuk ${esc(m.name)}">
        </label>
      </article>`;
    }).join('') : `
      <div class="card empty"><img src="img/logo.png" alt="">Keranjang masih kosong.<br>Yuk pilih menu dulu.</div>`;

    const sub = cartSubtotal();
    const tax = calcTax(sub);
    const total = sub + tax;
    summaryEl.innerHTML = `
      <div style="display:flex;justify-content:space-between;padding-bottom:12px;border-bottom:1px solid #EEF0F3">
        <span class="label-caps" style="color:var(--text)">RINGKASAN TAGIHAN</span><small class="muted">Kalkulasi Otomatis</small>
      </div>
      <div style="padding:10px 0;border-bottom:1px solid #EEF0F3">
        <div class="row"><span>Subtotal (${n} Item)</span><b>${rupiah(sub)}</b></div>
        ${tax > 0 ? `<div class="row"><span>${taxLabel()}</span><b>${rupiah(tax)}</b></div>` : ''}
        <div class="row"><span>Biaya Layanan Meja</span><b style="color:var(--teal)">GRATIS (Rp 0)</b></div>
      </div>
      <div class="total"><b>TOTAL TAGIHAN</b><b>${rupiah(total)}</b></div>
      <small class="muted">${tax > 0 ? 'Sudah termasuk pajak restoran' : 'Harga pas tanpa biaya tambahan'}</small>`;

    sendBtn.disabled = n === 0;
    document.getElementById('send-amount').textContent = rupiah(total);
  }

  listEl.addEventListener('click', (e) => {
    const minus = e.target.closest('[data-minus]');
    const plus = e.target.closest('[data-plus]');
    if (minus) { changeQty(minus.dataset.minus, -1); draw(); }
    if (plus) { changeQty(plus.dataset.plus, +1); draw(); }
  });
  listEl.addEventListener('input', (e) => {
    const input = e.target.closest('[data-note]');
    if (!input) return;
    const item = cart.find((c) => String(c.id) === String(input.dataset.note));
    if (item) { item.note = input.value; saveCart(); }
  });

  let sending = false;
  sendBtn.addEventListener('click', async () => {
    if (!cart.length || sending) return;
    if (!window.Cafe3RasaAPI || !window.Cafe3RasaAPI.apiCreateOrder) {
      toast('Gagal memuat layanan pesanan. Muat ulang halaman.');
      return;
    }

    // Kunci tombol agar pesanan tidak terkirim dua kali
    sending = true;
    sendBtn.disabled = true;
    const amountEl = document.getElementById('send-amount');
    const prevAmount = amountEl.textContent;
    amountEl.textContent = 'Mengirim...';

    const items = cart.map((c) => ({ ...c }));
    const result = await window.Cafe3RasaAPI.apiCreateOrder(MEJA, items);

    if (!result || !result.success) {
      // Pesanan TIDAK tersimpan di server -> keranjang dipertahankan
      sending = false;
      sendBtn.disabled = false;
      amountEl.textContent = prevAmount;
      toast((result && result.message) || 'Pesanan gagal dikirim. Coba lagi.');
      return;
    }

    const sub = items.reduce((a, c) => a + (findMenu(c.id)?.price || 0) * c.qty, 0);
    const order = {
      id: `#${result.data.orderNumber}`,
      serverId: result.data.id,
      createdAt: Date.now(),
      items,
      subtotal: sub,
      tax: calcTax(sub)
    };
    const orders = load(KEY_ORDERS, []);
    orders.unshift(order);
    save(KEY_ORDERS, orders);
    cart = []; saveCart();
    location.href = withMeja('status.html') + '&baru=1';
  });

  draw();
}

// ================================================================
// Halaman: STATUS / CATATAN PESANAN (status.html)
// ================================================================
function pageStatus() {
  renderHeader('Order Summary');
  renderNav('status');
  const wrap = document.getElementById('status-wrap');
  const ETA_MIN = 15;
  let timerId = null;

  // Notifikasi "terkirim" hanya sekali; parameter &baru dihapus agar tidak muncul lagi saat refresh
  if (params.get('baru')) {
    toast('Pesanan terkirim ke kasir');
    params.delete('baru');
    history.replaceState(null, '', `${location.pathname}?${params.toString()}`);
  }

  const showEmpty = (text) => {
    if (timerId) { clearInterval(timerId); timerId = null; }
    wrap.innerHTML = `
      <div class="card empty"><img src="img/logo.png" alt="">${text}</div>
      <a class="cta block teal" style="justify-content:center" href="${withMeja('index.html')}">Lihat Menu</a>`;
  };

  // Tampilan progres dapur (terpisah dari status bayar)
  function progressView(o) {
    switch (o.status) {
      case 'MENUNGGU_KONFIRMASI':
        return { title: 'Menunggu Konfirmasi Kasir', sub: 'Pesanan Anda sudah masuk dan menunggu dicek kasir', bars: ['now', '', ''], timer: false };
      case 'DIPROSES':
        return { title: 'Pesanan Sedang Dimasak', sub: 'Dapur sedang menyiapkan hidangan Anda', bars: ['done', 'now', ''], timer: true };
      case 'DISAJIKAN':
      case 'SELESAI':
        return { title: 'Pesanan Telah Disajikan', sub: 'Selamat menikmati hidangan Cafe 3 Rasa!', bars: ['done', 'done', 'done'], timer: false };
      case 'DIBATALKAN':
        return { title: 'Pesanan Dibatalkan', sub: 'Silakan hubungi kasir bila ada pertanyaan', bars: ['', '', ''], timer: false };
      default:
        return { title: 'Pesanan Diterima', sub: '', bars: ['now', '', ''], timer: false };
    }
  }

  function render(o) {
    const pv = progressView(o);
    const isPaid = o.paymentBillStatus === 'LUNAS';
    const method = o.paymentMethod === 'TRANSFER' ? 'Transfer' : o.paymentMethod === 'CASH' ? 'Cash' : '';

    wrap.innerHTML = `
      <section class="card live">
        <div class="live-top"><span class="tag navy">LIVE STATUS</span><small class="muted" style="font-weight:700">#${esc(o.orderNumber)} • MEJA ${MEJA}</small></div>
        <h3 id="live-title">${pv.title}</h3>
        <small class="muted" id="live-sub">${pv.sub}</small>
        ${pv.timer ? `<div class="eta"><span style="display:inline-flex;align-items:center;gap:8px">${ICON.pot}Estimasi Masak: ${ETA_MIN} Menit</span><span class="timer" id="timer">00:00</span></div>` : ''}
        <div class="progress">${pv.bars.map((c) => `<span class="${c}"></span>`).join('')}</div>
        <div style="display:flex;justify-content:space-between;margin-top:6px;font-size:11px;font-weight:700" class="muted">
          <span>Diterima</span><span>Dimasak</span><span>Disajikan</span>
        </div>
      </section>

      <section class="card pad">
        <div style="display:flex;justify-content:space-between;align-items:center;padding-bottom:12px;border-bottom:1px solid var(--border)">
          <b style="font-size:16px">Rincian Menu</b><small class="muted">${o.itemCount} Item</small>
        </div>
        ${o.items.map((c) => `<div class="detail-item">
            <div class="line"><span><span class="qty">${c.quantity}x</span> ${esc(c.name)}</span><b>${rupiah(c.subtotal)}</b></div>
            ${c.note ? `<span class="note-chip">Catatan: ${esc(c.note)}</span>` : ''}
          </div>`).join('')}
        <hr class="divider" style="margin:6px 0 8px">
        <div class="summary" style="padding:0">
          <div class="row"><span>Subtotal Pesanan</span><b>${rupiah(o.subtotal)}</b></div>
          ${o.tax > 0 ? `<div class="row"><span>${taxLabel()}</span><b>${rupiah(o.tax)}</b></div>` : ''}
          <div class="row"><span>Layanan Meja</span><b style="color:var(--teal)">GRATIS (Rp 0)</b></div>
        </div>
        <hr class="divider" style="margin:8px 0">
        <div style="display:flex;justify-content:space-between;align-items:center"><b>Total Pembayaran</b><b style="font-size:21px;color:var(--teal)">${rupiah(o.total)}</b></div>
      </section>

      <section class="card pay-info"${isPaid ? ' style="background:#E8F2F1;border-color:var(--teal)"' : ''}>
        <div class="pay-icon"${isPaid ? ' style="background:var(--teal)"' : ''}>${ICON.cash}</div>
        <div>
          <b>Status Bayar: <span class="tag ${isPaid ? 'teal' : 'navy'}" style="font-size:11px${isPaid ? ';background:var(--teal);color:#fff' : ''}">${isPaid ? `LUNAS${method ? ' • ' + method.toUpperCase() : ''}` : 'BELUM BAYAR'}</span></b>
          <p>${isPaid
            ? 'Pembayaran sudah diterima kasir. Terima kasih telah bersantap di Cafe 3 Rasa!'
            : 'Bayar dengan Cash atau Transfer langsung di kasir atau kepada staf. Tekan Minta Bill di halaman Bantuan bila sudah selesai.'}</p>
        </div>
      </section>

      ${o.status !== 'DIBATALKAN' && !isPaid
        ? `<a class="cta block" style="justify-content:center;background:var(--coral-dark)" href="${withMeja('index.html')}">${ICON.plus} Tambah Menu / Pesanan Lain (Add-on)</a>`
        : `<a class="cta block teal" style="justify-content:center" href="${withMeja('index.html')}">Lihat Menu</a>`}`;

    if (timerId) { clearInterval(timerId); timerId = null; }
    if (pv.timer) {
      // Timer dihitung sejak kasir mengonfirmasi (mulai dimasak)
      const startMs = Date.parse(o.confirmedAt || o.createdAt) || Date.now();
      const timerEl = document.getElementById('timer');
      const tick = () => {
        const sec = Math.max(0, Math.floor((Date.now() - startMs) / 1000));
        timerEl.textContent = `${String(Math.floor(sec / 60)).padStart(2, '0')}:${String(sec % 60).padStart(2, '0')} BERJALAN`;
      };
      tick();
      timerId = setInterval(tick, 1000);
    }
  }

  // Ambil status terbaru dari server (dipakai saat halaman dibuka & saat ada event real-time)
  let loading = false;
  async function load() {
    if (loading) return;
    loading = true;
    try {
      if (!window.Cafe3RasaAPI || !window.Cafe3RasaAPI.apiGetActiveOrder) {
        showEmpty('Gagal memuat layanan pesanan. Muat ulang halaman.');
        return;
      }
      const res = await window.Cafe3RasaAPI.apiGetActiveOrder(MEJA);
      if (!res || !res.success) {
        showEmpty((res && res.message) || 'Tidak dapat memuat status pesanan.');
        return;
      }
      if (!res.data) {
        // Meja sudah dikosongkan kasir -> riwayat lokal tidak berlaku lagi
        save(KEY_ORDERS, []);
        showEmpty(`Belum ada pesanan aktif untuk Meja ${MEJA}.`);
        return;
      }
      render(res.data);
    } finally {
      loading = false;
    }
  }

  wrap.innerHTML = `<div class="card empty">Memuat status pesanan...</div>`;
  load();

  // Notifikasi real-time dari kasir, lalu tampilan disegarkan dari server
  window.addEventListener('cafe3rasa:order_confirmed', () => {
    toast('Pesanan dikonfirmasi kasir dan sedang dimasak');
    load();
  });
  window.addEventListener('cafe3rasa:order_served', () => {
    toast('Pesanan Anda sudah disajikan. Selamat menikmati!');
    load();
  });
  window.addEventListener('cafe3rasa:payment_completed', (e) => {
    if (e.detail && e.detail.paymentBillStatus === 'LUNAS') {
      toast(`Pembayaran lunas (${e.detail.paymentMethod === 'TRANSFER' ? 'Transfer' : 'Cash'}). Terima kasih!`);
    }
    load();
  });
  window.addEventListener('cafe3rasa:order_updated', () => load());
  window.addEventListener('cafe3rasa:table_status_changed', () => load());
  // Saat kembali ke tab ini (HP sempat dikunci), ambil status terbaru
  document.addEventListener('visibilitychange', () => { if (!document.hidden) load(); });
}

// ================================================================
// Halaman: BANTUAN (bantuan.html)
// 1 Tombol Multifungsi + Papan Notepad + Cooldown 3 Menit (180 detik)
// ================================================================
function pageBantuan() {
  renderHeader('Cashier Sync');
  renderNav('bantuan');
  document.getElementById('meja-title').textContent = `Meja ${MEJA} Terhubung`;

  const COOLDOWN_DURATION_MS = 3 * 60 * 1000; // Jeda 3 menit
  const KEY_COOLDOWN = `tigarasa_help_cooldown_${MEJA}`;
  const KEY_LAST_REQ = `tigarasa_help_last_req_${MEJA}`;
  const KEY_STATUS = `tigarasa_help_status_${MEJA}`;
  const KEY_REPLY = `tigarasa_help_reply_${MEJA}`;
  const KEY_REQ_TIME = `tigarasa_help_time_${MEJA}`;

  const noteInput = document.getElementById('req-note');
  const submitBtn = document.getElementById('btn-submit-help');
  const submitIcon = document.getElementById('btn-submit-icon');
  const submitText = document.getElementById('btn-submit-text');
  const charCurrent = document.getElementById('char-current');
  const notepadStatus = document.getElementById('notepad-status');
  const cooldownBanner = document.getElementById('cooldown-banner');
  const lastReqText = document.getElementById('last-req-text');
  const cooldownTimerEl = document.getElementById('cooldown-timer');
  const quickChips = document.querySelectorAll('.quick-chip');

  const feedbackBadge = document.getElementById('feedback-badge');
  const feedbackTime = document.getElementById('feedback-time');
  const feedbackTitle = document.getElementById('feedback-title');
  const cashierReplyStatus = document.getElementById('cashier-reply-status');
  const cashierReplyText = document.getElementById('cashier-reply-text');

  function getRemainingSeconds() {
    const expireTime = Number(localStorage.getItem(KEY_COOLDOWN) || 0);
    const diff = expireTime - Date.now();
    return diff > 0 ? Math.ceil(diff / 1000) : 0;
  }

  function formatTime(sec) {
    const m = Math.floor(sec / 60);
    const s = sec % 60;
    return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
  }

  const SVG_CLOCK = '<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><circle cx="12" cy="12" r="10"/><polyline points="12 6 12 12 16 14"/></svg>';
  const SVG_SEND = '<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><line x1="22" y1="2" x2="11" y2="13"/><polygon points="22 2 15 22 11 13 2 9 22 2"/></svg>';
  const SVG_NOTE = '<svg width="17" height="17" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="M12 20h9"/><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"/></svg>';

  function updateState() {
    const remaining = getRemainingSeconds();
    const text = noteInput.value.trim();
    charCurrent.textContent = noteInput.value.length;

    if (remaining > 0) {
      // Jeda 3 menit aktif
      const lastText = localStorage.getItem(KEY_LAST_REQ) || 'Bantuan staf meja';
      const status = localStorage.getItem(KEY_STATUS) || 'MENUNGGU';
      const reply = localStorage.getItem(KEY_REPLY) || 'Permintaan telah masuk di layar dashboard kasir.';
      const timeStr = localStorage.getItem(KEY_REQ_TIME) || 'Baru saja';

      cooldownBanner.hidden = false;
      lastReqText.textContent = lastText;
      cooldownTimerEl.textContent = formatTime(remaining);
      feedbackTime.textContent = timeStr;

      if (status === 'DIKONFIRMASI') {
        // Kasir telah mengkonfirmasi & memberi timbal balik!
        cooldownBanner.classList.add('confirmed');
        feedbackBadge.classList.add('confirmed');
        feedbackBadge.textContent = 'DIKONFIRMASI KASIR';
        feedbackTitle.textContent = 'Staf Sedang Menuju ke Meja Anda';
        cashierReplyStatus.textContent = 'Sedang Ditangani';
        cashierReplyText.textContent = reply;

        notepadStatus.textContent = 'Staf Menuju Meja';
        notepadStatus.style.borderColor = 'var(--teal)';
        notepadStatus.style.color = 'var(--teal)';

        submitBtn.disabled = true;
        submitBtn.classList.remove('active-ready');
        submitBtn.classList.add('in-cooldown');
        submitIcon.innerHTML = SVG_CLOCK;
        submitText.textContent = `Staf Menuju Meja (Jeda ${formatTime(remaining)})`;
      } else {
        // Masih menunggu konfirmasi kasir
        cooldownBanner.classList.remove('confirmed');
        feedbackBadge.classList.remove('confirmed');
        feedbackBadge.textContent = 'MENUNGGU KONFIRMASI KASIR';
        feedbackTitle.textContent = 'Permintaan Telah Diteruskan ke Kasir';
        cashierReplyStatus.textContent = 'Dalam Antrean';
        cashierReplyText.textContent = reply;

        notepadStatus.textContent = 'Menunggu Konfirmasi';
        notepadStatus.style.borderColor = 'var(--coral)';
        notepadStatus.style.color = 'var(--coral-dark)';

        submitBtn.disabled = true;
        submitBtn.classList.remove('active-ready');
        submitBtn.classList.add('in-cooldown');
        submitIcon.innerHTML = SVG_CLOCK;
        submitText.textContent = `Menunggu Kasir (Jeda ${formatTime(remaining)})`;
      }

      quickChips.forEach((btn) => btn.disabled = true);
      noteInput.disabled = true;
    } else {
      // ── Tidak dalam jeda (bisa meminta bantuan) ──
      cooldownBanner.hidden = true;
      quickChips.forEach((btn) => btn.disabled = false);
      noteInput.disabled = false;

      if (text.length > 0) {
        // Papan pesan terisi -> Tombol aktif!
        notepadStatus.textContent = 'Pesan Siap Dikirim';
        notepadStatus.style.borderColor = 'var(--teal)';
        notepadStatus.style.color = 'var(--teal)';

        submitBtn.disabled = false;
        submitBtn.classList.remove('in-cooldown');
        submitBtn.classList.add('active-ready');
        submitIcon.innerHTML = SVG_SEND;
        submitText.textContent = 'Kirim Permintaan Bantuan Sekarang';
      } else {
        // Papan pesan kosong -> Tombol nonaktif
        notepadStatus.textContent = 'Notepad Kosong';
        notepadStatus.style.borderColor = 'var(--border-subtle)';
        notepadStatus.style.color = 'var(--muted)';

        submitBtn.disabled = true;
        submitBtn.classList.remove('active-ready', 'in-cooldown');
        submitIcon.innerHTML = SVG_NOTE;
        submitText.textContent = 'Tuliskan Kebutuhan untuk Mengaktifkan Tombol';
      }
    }
  }

  // Event listener saat user mengetik di notepad
  noteInput.addEventListener('input', updateState);

  // Quick Chips: Mengisi notepad secara otomatis
  quickChips.forEach((btn) => {
    btn.addEventListener('click', () => {
      if (getRemainingSeconds() > 0) return;
      const chipText = btn.dataset.text;
      const cur = noteInput.value.trim();
      if (!cur) {
        noteInput.value = chipText;
      } else if (!cur.includes(chipText)) {
        noteInput.value = `${cur}, ${chipText}`;
      }
      noteInput.focus();
      updateState();
    });
  });

  // Klik 1 Tombol Multifungsi
  let requesting = false;
  submitBtn.addEventListener('click', async () => {
    const text = noteInput.value.trim();
    if (!text || getRemainingSeconds() > 0 || requesting) return;

    // Kirim ke server lebih dulu; cooldown hanya berjalan jika permintaan benar-benar tersimpan
    if (!window.Cafe3RasaAPI || !window.Cafe3RasaAPI.apiCreateServiceRequest) {
      toast('Gagal memuat layanan bantuan. Muat ulang halaman.');
      return;
    }
    requesting = true;
    submitBtn.disabled = true;
    const result = await window.Cafe3RasaAPI.apiCreateServiceRequest(MEJA, 'PANGGIL_PELAYAN', text);
    requesting = false;
    if (!result || !result.success) {
      submitBtn.disabled = false;
      toast((result && result.message) || 'Permintaan gagal dikirim. Coba lagi.');
      updateState();
      return;
    }

    const nowStr = new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' });
    const expireTime = Date.now() + COOLDOWN_DURATION_MS;

    localStorage.setItem(KEY_COOLDOWN, String(expireTime));
    localStorage.setItem(KEY_LAST_REQ, text);
    localStorage.setItem(KEY_STATUS, 'MENUNGGU');
    localStorage.setItem(KEY_REPLY, 'Permintaan telah masuk di layar dashboard kasir. Kasir sedang menugaskan staf.');
    localStorage.setItem(KEY_REQ_TIME, nowStr);

    // Simpan ke riwayat permintaan meja
    const reqHistory = load(KEY_REQ, []);
    reqHistory.unshift({
      id: `REQ-${Date.now()}`,
      meja: MEJA,
      text: text,
      status: 'MENUNGGU',
      time: nowStr,
      timestamp: Date.now()
    });
    save(KEY_REQ, reqHistory);

    toast('Permintaan bantuan berhasil dikirim ke kasir');
    noteInput.value = '';
    updateState();
    // Konfirmasi kasir datang secara real-time lewat Socket.io (event cafe3rasa:service_confirmed)
  });

  // Tombol simulasi toggle konfirmasi kasir (bisa dicoba langsung oleh penguji)
  const btnToggleConfirmed = document.getElementById('btn-toggle-confirmed');
  if (btnToggleConfirmed) {
    btnToggleConfirmed.addEventListener('click', () => {
      const curStatus = localStorage.getItem(KEY_STATUS);
      if (curStatus === 'DIKONFIRMASI') {
        localStorage.setItem(KEY_STATUS, 'MENUNGGU');
        localStorage.setItem(KEY_REPLY, 'Permintaan telah masuk di layar dashboard kasir. Kasir sedang menugaskan staf.');
        btnToggleConfirmed.textContent = 'Simulasikan Konfirmasi Kasir';
        toast('Status diubah ke: Menunggu Kasir');
      } else {
        localStorage.setItem(KEY_STATUS, 'DIKONFIRMASI');
        localStorage.setItem(KEY_REPLY, `Kasir mengkonfirmasi: Staf pelayan segera tiba di Meja ${MEJA} untuk melayani kebutuhan Anda.`);
        btnToggleConfirmed.textContent = 'Kembalikan ke Status Menunggu';
        toast(`Kasir mengkonfirmasi: Staf segera tiba di Meja ${MEJA}`);
      }
      updateState();
    });
  }

  // ── FITUR RATING & ULASAN PELANGGAN CAFE 3 RASA ──
  const KEY_RATING = `tigarasa_rating_${MEJA}`;
  const starBtns = document.querySelectorAll('.star-btn');
  const starScoreText = document.getElementById('star-score-text');
  const aspectChips = document.querySelectorAll('.aspect-chip');
  const ratingComment = document.getElementById('rating-comment');
  const ratingCharCount = document.getElementById('rating-char-count');
  const btnSubmitRating = document.getElementById('btn-submit-rating');
  const ratingFormWrap = document.getElementById('rating-form-wrap');
  const ratingSuccessBox = document.getElementById('rating-success-box');
  const ratingSummaryText = document.getElementById('rating-summary-text');
  const btnEditRating = document.getElementById('btn-edit-rating');

  let currentStars = 0;
  const STAR_LABELS = {
    1: '1/5 — Kurang Memuaskan',
    2: '2/5 — Cukup',
    3: '3/5 — Baik',
    4: '4/5 — Sangat Baik',
    5: '5/5 — Luar Biasa Memuaskan!'
  };

  function updateRatingUI() {
    starBtns.forEach((btn) => {
      const val = Number(btn.dataset.value);
      btn.classList.toggle('active', val <= currentStars);
    });
    if (currentStars > 0) {
      starScoreText.textContent = STAR_LABELS[currentStars] || `${currentStars} Bintang`;
      starScoreText.style.color = '#D97706';
      if (btnSubmitRating) btnSubmitRating.disabled = false;
    } else {
      starScoreText.textContent = 'Pilih Bintang (1 - 5)';
      starScoreText.style.color = 'var(--text)';
      if (btnSubmitRating) btnSubmitRating.disabled = true;
    }
  }

  starBtns.forEach((btn) => {
    btn.addEventListener('click', () => {
      currentStars = Number(btn.dataset.value);
      updateRatingUI();
    });
  });

  aspectChips.forEach((chip) => {
    chip.addEventListener('click', () => {
      chip.classList.toggle('selected');
    });
  });

  if (ratingComment && ratingCharCount) {
    ratingComment.addEventListener('input', () => {
      ratingCharCount.textContent = ratingComment.value.length;
    });
  }

  function loadSavedRating() {
    const saved = load(KEY_RATING, null);
    if (saved && saved.stars) {
      if (ratingFormWrap) ratingFormWrap.hidden = true;
      if (ratingSuccessBox) ratingSuccessBox.hidden = false;
      const aspectsStr = saved.aspects && saved.aspects.length > 0 ? ` • Fokus: ${saved.aspects.join(', ')}` : '';
      const commentStr = saved.comment ? ` "${saved.comment}"` : '';
      if (ratingSummaryText) {
        ratingSummaryText.textContent = `Anda memberikan rating ${saved.stars}/5 bintang${aspectsStr}.${commentStr}`;
      }
    } else {
      if (ratingFormWrap) ratingFormWrap.hidden = false;
      if (ratingSuccessBox) ratingSuccessBox.hidden = true;
    }
  }

  if (btnSubmitRating) {
    btnSubmitRating.addEventListener('click', () => {
      if (currentStars === 0) return;
      const selectedAspects = Array.from(document.querySelectorAll('.aspect-chip.selected')).map(c => c.dataset.aspect);
      const comment = ratingComment ? ratingComment.value.trim() : '';
      const ratingData = {
        meja: MEJA,
        stars: currentStars,
        aspects: selectedAspects,
        comment: comment,
        time: new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' }),
        timestamp: Date.now()
      };
      save(KEY_RATING, ratingData);
      // Kirim review ke backend API
      if (window.Cafe3RasaAPI && window.Cafe3RasaAPI.apiCreateReview) {
        window.Cafe3RasaAPI.apiCreateReview(MEJA, currentStars, selectedAspects, comment).catch(console.error);
      }
      toast('Terima kasih! Rating & ulasan Anda berhasil disimpan.');
      loadSavedRating();
    });
  }

  if (btnEditRating) {
    btnEditRating.addEventListener('click', () => {
      if (ratingFormWrap) ratingFormWrap.hidden = false;
      if (ratingSuccessBox) ratingSuccessBox.hidden = true;
    });
  }

  loadSavedRating();

  // Dengarkan balasan konfirmasi bantuan kasir secara real-time dari Socket.io
  window.addEventListener('cafe3rasa:service_confirmed', (e) => {
    const detail = e.detail;
    localStorage.setItem(KEY_STATUS, 'DIKONFIRMASI');
    localStorage.setItem(KEY_REPLY, detail.responseMessage || `Kasir mengonfirmasi: Staf pelayan sedang menuju ke Meja ${MEJA}.`);
    toast(`Kasir mengonfirmasi: ${detail.responseMessage || 'Staf sedang menuju meja'}`);
    updateState();
  });

  // Jalankan awal dan update timer setiap detik
  updateState();
  setInterval(updateState, 1000);
}

// Inisialisasi koneksi Socket.io ke room meja saat ini
if (window.Cafe3RasaAPI && window.Cafe3RasaAPI.initTableSocket) {
  window.Cafe3RasaAPI.initTableSocket(MEJA);
}

// ---------- Jalankan sesuai halaman ----------
const PAGES = { menu: pageMenu, pesanan: pagePesanan, status: pageStatus, bantuan: pageBantuan };

(async function boot() {
  // Ambil menu terbaru dari database (harga, stok, menu baru dari admin).
  // Jika server tidak bisa dihubungi, web tetap jalan dengan data bawaan data.js.
  if (window.Cafe3RasaAPI && window.Cafe3RasaAPI.apiLoadMenu) {
    const serverMenu = await window.Cafe3RasaAPI.apiLoadMenu();
    if (serverMenu) MENU.splice(0, MENU.length, ...serverMenu);
  }

  // Item keranjang yang menunya sudah habis / dihapus admin dibuang dari keranjang
  const before = cart.length;
  cart = cart.filter((c) => findMenu(c.id));
  if (cart.length !== before) {
    saveCart();
    setTimeout(() => toast('Beberapa menu di keranjang sudah tidak tersedia dan dihapus'), 300);
  }

  PAGES[document.body.dataset.page]?.();

  // Menu diubah admin/kasir saat halaman menu terbuka -> muat ulang agar data selalu terbaru
  window.addEventListener('cafe3rasa:menu_changed', () => {
    if (document.body.dataset.page === 'menu') location.reload();
  });
})();
