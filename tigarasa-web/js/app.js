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
const findMenu = (id) => MENU.find((m) => m.id === id);
const cartCount = () => cart.reduce((a, c) => a + c.qty, 0);
const cartSubtotal = () => cart.reduce((a, c) => a + (findMenu(c.id)?.price || 0) * c.qty, 0);
const esc = (s) => String(s).replace(/[&<>"']/g, (c) => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));

function addToCart(id) {
  const item = cart.find((c) => c.id === id);
  if (item) item.qty += 1; else cart.push({ id, qty: 1, note: '' });
  saveCart();
}
function changeQty(id, delta) {
  const item = cart.find((c) => c.id === id);
  if (!item) return;
  item.qty += delta;
  if (item.qty <= 0) cart = cart.filter((c) => c.id !== id);
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

  let active = 'Semua';
  const chipsEl = document.getElementById('chips');
  const listEl = document.getElementById('menu-list');
  const heroEl = document.getElementById('hero');

  function qtyOf(id) { return cart.find((c) => c.id === id)?.qty || 0; }

  function draw() {
    chipsEl.innerHTML = CATEGORIES.map((c) =>
      `<button type="button" class="chip ${c === active ? 'active' : ''}" data-cat="${c}" aria-pressed="${c === active}">${c}</button>`).join('');

    const featured = MENU.find((m) => m.featured);
    const showHero = featured && (active === 'Semua' || active === featured.category);
    heroEl.hidden = !showHero;
    if (showHero) {
      const q = qtyOf(featured.id);
      heroEl.innerHTML = `
        <div class="hero-img"><img src="${featured.img}" alt="${esc(featured.name)}"><span class="tag coral">TERLARIS</span></div>
        <div class="hero-body">
          <h3>${esc(featured.name)}</h3>
          <p class="muted clamp2" style="margin:0;font-size:14px;line-height:21px">${esc(featured.desc)}</p>
          <div class="price-row">
            <div><small class="muted">Harga per porsi</small><div class="price" style="font-size:19px">${rupiah(featured.price)}</div>
              ${q ? `<span class="in-cart">${q} di keranjang</span>` : ''}</div>
            <button class="add-btn" type="button" data-add="${featured.id}" aria-label="Tambah ${esc(featured.name)}">${ICON.plus}</button>
          </div>
        </div>`;
    }

    const items = MENU.filter((m) => !m.featured && (active === 'Semua' || m.category === active));
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

  document.addEventListener('click', (e) => {
    const chip = e.target.closest('[data-cat]');
    if (chip) { active = chip.dataset.cat; draw(); return; }
    const add = e.target.closest('[data-add]');
    if (add) {
      const m = findMenu(Number(add.dataset.add));
      addToCart(m.id); draw(); toast(`${m.name} ditambahkan`);
    }
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
    const tax = Math.round(sub * TAX_RATE);
    summaryEl.innerHTML = `
      <div style="display:flex;justify-content:space-between;padding-bottom:12px;border-bottom:1px solid #EEF0F3">
        <span class="label-caps" style="color:var(--text)">RINGKASAN TAGIHAN</span><small class="muted">Kalkulasi Otomatis</small>
      </div>
      <div style="padding:10px 0;border-bottom:1px solid #EEF0F3">
        <div class="row"><span>Subtotal (${n} Item)</span><b>${rupiah(sub)}</b></div>
        <div class="row"><span>PB1 / Pajak Resto (10%)</span><b>${rupiah(tax)}</b></div>
        <div class="row"><span>Biaya Layanan Meja</span><b style="color:var(--teal)">GRATIS (Rp 0)</b></div>
      </div>
      <div class="total"><b>TOTAL TAGIHAN</b><b>${rupiah(sub + tax)}</b></div>
      <small class="muted">Termasuk pajak restoran 10%</small>`;

    sendBtn.disabled = n === 0;
    document.getElementById('send-amount').textContent = rupiah(sub + tax);
  }

  listEl.addEventListener('click', (e) => {
    const minus = e.target.closest('[data-minus]');
    const plus = e.target.closest('[data-plus]');
    if (minus) { changeQty(Number(minus.dataset.minus), -1); draw(); }
    if (plus) { changeQty(Number(plus.dataset.plus), +1); draw(); }
  });
  listEl.addEventListener('input', (e) => {
    const input = e.target.closest('[data-note]');
    if (!input) return;
    const item = cart.find((c) => c.id === Number(input.dataset.note));
    if (item) { item.note = input.value; saveCart(); }
  });

  sendBtn.addEventListener('click', () => {
    if (!cart.length) return;
    const sub = cartSubtotal();
    const order = {
      id: `#TR-${MEJA}-${String(Date.now()).slice(-4)}`,
      createdAt: Date.now(),
      items: cart.map((c) => ({ ...c })),
      subtotal: sub,
      tax: Math.round(sub * TAX_RATE)
    };
    const orders = load(KEY_ORDERS, []);
    orders.unshift(order);
    save(KEY_ORDERS, orders);
    cart = []; saveCart();
    // TODO: kirim `order` ke Firebase agar masuk ke aplikasi kasir
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
  const orders = load(KEY_ORDERS, []);
  const wrap = document.getElementById('status-wrap');

  if (!orders.length) {
    wrap.innerHTML = `
      <div class="card empty"><img src="img/logo.png" alt="">Belum ada pesanan untuk Meja ${MEJA}.</div>
      <a class="cta block teal" style="justify-content:center" href="${withMeja('index.html')}">Lihat Menu</a>`;
    return;
  }
  if (params.get('baru')) toast('Pesanan terkirim ke kasir');

  const o = orders[0];
  const ETA_MIN = 15;
  const allItems = orders.flatMap((x) => x.items);
  const sub = orders.reduce((a, x) => a + x.subtotal, 0);
  const tax = orders.reduce((a, x) => a + x.tax, 0);

  wrap.innerHTML = `
    <section class="card live">
      <div class="live-top"><span class="tag navy">LIVE STATUS</span><small class="muted" style="font-weight:700">${o.id} • MEJA ${MEJA}</small></div>
      <h3 id="live-title">Pesanan Diterima Kasir</h3>
      <small class="muted" id="live-sub">Dapur sedang memproses hidangan Anda</small>
      <div class="eta"><span style="display:inline-flex;align-items:center;gap:8px">${ICON.pot}Estimasi Masak: ${ETA_MIN} Menit</span><span class="timer" id="timer">00:00</span></div>
      <div class="progress"><span class="done"></span><span class="now" id="p2"></span><span id="p3"></span></div>
    </section>

    <section class="card pad">
      <div style="display:flex;justify-content:space-between;align-items:center;padding-bottom:12px;border-bottom:1px solid var(--border)">
        <b style="font-size:16px">Rincian Menu</b><small class="muted">${orders.length} Pesanan</small>
      </div>
      ${allItems.map((c) => {
        const m = findMenu(c.id);
        return `<div class="detail-item">
          <div class="line"><span><span class="qty">${c.qty}x</span> ${esc(m.name)}</span><b>${rupiah(m.price * c.qty)}</b></div>
          ${c.note ? `<span class="note-chip">Catatan: ${esc(c.note)}</span>` : ''}
        </div>`;
      }).join('')}
      <hr class="divider" style="margin:6px 0 8px">
      <div class="summary" style="padding:0">
        <div class="row"><span>Subtotal Pesanan</span><b>${rupiah(sub)}</b></div>
        <div class="row"><span>Resto Tax PB1 (10%)</span><b>${rupiah(tax)}</b></div>
      </div>
      <hr class="divider" style="margin:8px 0">
      <div style="display:flex;justify-content:space-between;align-items:center"><b>Total Pembayaran</b><b style="font-size:21px;color:var(--teal)">${rupiah(sub + tax)}</b></div>
    </section>

    <section class="card pay-info">
      <div class="pay-icon">${ICON.cash}</div>
      <div><b>Metode Bayar: <span class="tag navy" style="font-size:11px">CASH / TRANSFER</span></b>
        <p>Pembayaran dapat dilakukan langsung di kasir atau kepada staf saat pesanan diantar. Tekan tombol Minta Bill di halaman Bantuan bila sudah selesai.</p></div>
    </section>

    <a class="cta block" style="justify-content:center;background:var(--coral-dark)" href="${withMeja('index.html')}">${ICON.plus} Tambah Menu / Pesanan Lain (Add-on)</a>`;

  // Timer berjalan sejak pesanan terakhir dibuat (simulasi status dapur)
  const timerEl = document.getElementById('timer');
  function tick() {
    const sec = Math.floor((Date.now() - o.createdAt) / 1000);
    timerEl.textContent = `${String(Math.floor(sec / 60)).padStart(2, '0')}:${String(sec % 60).padStart(2, '0')} BERJALAN`;
    if (sec >= ETA_MIN * 60) {
      document.getElementById('live-title').textContent = 'Pesanan Siap Diantar';
      document.getElementById('live-sub').textContent = 'Staf akan segera mengantar ke meja Anda';
      document.getElementById('p2').className = 'done';
      document.getElementById('p3').className = 'now';
    }
  }
  tick(); setInterval(tick, 1000);
}

// ================================================================
// Halaman: BANTUAN (bantuan.html)
// ================================================================
function pageBantuan() {
  renderHeader('Cashier Sync');
  renderNav('bantuan');
  document.getElementById('meja-title').textContent = `Meja ${MEJA} Terhubung`;

  const sent = load(KEY_REQ, {});
  const buttons = document.querySelectorAll('[data-req]');
  function draw() {
    buttons.forEach((b) => {
      const done = sent[b.dataset.req];
      b.classList.toggle('sent', !!done);
      b.querySelector('small').textContent = done ? `Terkirim pukul ${done} — staf segera datang` : b.dataset.desc;
    });
  }
  buttons.forEach((b) => b.addEventListener('click', () => {
    const note = document.getElementById('req-note').value.trim();
    sent[b.dataset.req] = new Date().toLocaleTimeString('id-ID', { hour: '2-digit', minute: '2-digit' });
    save(KEY_REQ, sent);
    // TODO: kirim permintaan { type, note, meja } ke Firebase → muncul di "Permintaan Meja" kasir
    toast(`${b.querySelector('b').textContent} terkirim${note ? ' dengan catatan' : ''}`);
    draw();
  }));
  draw();
}

// ---------- Jalankan sesuai halaman ----------
const PAGES = { menu: pageMenu, pesanan: pagePesanan, status: pageStatus, bantuan: pageBantuan };
PAGES[document.body.dataset.page]?.();
