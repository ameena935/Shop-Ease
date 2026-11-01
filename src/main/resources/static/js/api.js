// Shared helpers used by every page
const API = '/api';

const getToken = () => localStorage.getItem('token');
function currentUser() { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } }
function saveSession(data) { localStorage.setItem('token', data.token); localStorage.setItem('user', JSON.stringify(data.user)); }
function clearSession() { localStorage.removeItem('token'); localStorage.removeItem('user'); }

async function api(path, options = {}) {
  const headers = { 'Content-Type': 'application/json' };
  if (getToken()) headers['Authorization'] = 'Bearer ' + getToken();
  const res = await fetch(API + path, { ...options, headers, body: options.body ? JSON.stringify(options.body) : undefined });
  let data = null;
  try { data = await res.json(); } catch {}
  if (res.status === 401 && getToken()) { clearSession(); }
  if (!res.ok) throw new Error((data && data.message) || 'Request failed');
  return data;
}

function requireLogin() {
  if (!getToken()) { location.href = 'login.html?next=' + encodeURIComponent(location.pathname.slice(1) + location.search); return false; }
  return true;
}
function requireAdmin() {
  const u = currentUser();
  if (!u || u.role !== 'ADMIN') { location.href = 'login.html?next=admin.html'; return false; }
  return true;
}

const money = n => '₹' + Number(n).toLocaleString('en-IN', { maximumFractionDigits: 2 });
const esc = s => String(s ?? '').replace(/[&<>"']/g, c => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;' }[c]));
const param = k => new URLSearchParams(location.search).get(k);

function productImg(p, cls = '') {
  const icon = '<svg viewBox="0 0 24 24" width="40%" height="40%" fill="none" stroke="currentColor" stroke-width="1.5" opacity=".45"><rect x="3" y="4" width="18" height="16" rx="2"/><circle cx="9" cy="10" r="1.8"/><path d="M21 16l-5-5-8 9"/></svg>';
  const iconAttr = icon.replace(/"/g, '&quot;');
  const url = p.imageUrl;
  if (!url) return `<div class="img-ph ${cls}">${icon}</div>`;
  return `<img class="${cls}" src="${esc(url)}" alt="${esc(p.name || p.productName)}"
           onerror="this.outerHTML='<div class=&quot;img-ph ${cls}&quot;>${iconAttr}</div>'">`;
}

function stockLabel(stock) {
  if (stock <= 0) return '<span class="stock out">Out of stock</span>';
  if (stock < 10) return `<span class="stock low">Only ${stock} left</span>`;
  return '<span class="stock in">In stock</span>';
}

function toast(msg, type = '') {
  let t = document.getElementById('toast');
  if (!t) { t = document.createElement('div'); t.id = 'toast'; document.body.appendChild(t); }
  t.textContent = msg; t.className = 'show ' + type;
  clearTimeout(t._h); t._h = setTimeout(() => t.className = '', 2500);
}

async function addToCart(productId, quantity = 1) {
  if (!requireLogin()) return;
  try {
    await api('/cart', { method: 'POST', body: { productId, quantity } });
    toast('Added to cart', 'success');
    refreshCartCount();
  } catch (e) { toast(e.message, 'error'); }
}

async function refreshCartCount() {
  const el = document.getElementById('cartCount');
  if (!el || !getToken()) return;
  try {
    const items = await api('/cart');
    const n = items.reduce((s, i) => s + i.quantity, 0);
    el.textContent = n; el.style.display = n ? 'inline' : 'none';
  } catch {}
}

async function logout() {
  try { await api('/auth/logout', { method: 'POST' }); } catch {}
  clearSession(); location.href = 'index.html';
}

function renderNav() {
  const u = currentUser();
  const page = location.pathname.split('/').pop() || 'index.html';
  const link = (href, text) => `<a href="${href}" class="${page === href ? 'active' : ''}">${text}</a>`;
  document.getElementById('nav').outerHTML = `
    <header class="nav"><div class="container">
      <a class="logo" href="index.html">Shop<span>Ease</span></a>
      <nav class="nav-links">
        ${link('index.html', 'Home')}
        ${link('cart.html', 'Cart<span id="cartCount" class="badge" style="display:none">0</span>')}
        ${u ? link('orders.html', 'My Orders') : ''}
        ${u && u.role === 'ADMIN' ? link('admin.html', 'Admin') : ''}
        ${u ? `<span class="muted" style="color:#9ca3af">Hi, ${esc(u.name.split(' ')[0])}</span><a href="#" onclick="logout();return false">Logout</a>`
            : link('login.html', 'Login') + link('register.html', 'Register')}
      </nav>
    </div></header>`;
  document.body.insertAdjacentHTML('beforeend', '<footer>© 2026 ShopEase · Full-Stack E-Commerce with Spring Boot</footer>');
  refreshCartCount();
}
