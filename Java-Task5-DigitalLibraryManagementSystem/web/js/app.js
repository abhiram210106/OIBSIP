/**
 * BiblioTech - Digital Library Management System
 * Frontend Application Controller
 */

const API_BASE = '/api';

// App State
let currentUser = null;
let allBooks = [];
let allCategories = [];
let activeCategory = 'All';
let currentAdminIssuesFilter = 'ALL';

// Initialize App
document.addEventListener('DOMContentLoaded', () => {
  initClock();
  restoreSession();
  loadCategories();
  loadBooks();
  loadStats();

  // Polling for live clock & stats every 30s
  setInterval(loadStats, 30000);
});

// Clock Display
function initClock() {
  const clockEl = document.getElementById('liveClock');
  function update() {
    const now = new Date();
    clockEl.innerHTML = `<i class="fa-regular fa-clock"></i> ${now.toLocaleDateString('en-IN', { weekday: 'short', month: 'short', day: 'numeric' })} | ${now.toLocaleTimeString('en-IN', { hour: '2-digit', minute: '2-digit', second: '2-digit' })}`;
  }
  update();
  setInterval(update, 1000);
}

// Session Management
function restoreSession() {
  const saved = localStorage.getItem('bibliotech_user');
  if (saved) {
    try {
      currentUser = JSON.parse(saved);
      applyUserRoleUI();
    } catch (e) {
      localStorage.removeItem('bibliotech_user');
    }
  } else {
    applyUserRoleUI();
  }
}

function applyUserRoleUI() {
  const authGuest = document.getElementById('authGuest');
  const authUser = document.getElementById('authUser');
  const userOnlyElems = document.querySelectorAll('.user-only');
  const adminOnlyElems = document.querySelectorAll('.admin-only');

  if (currentUser) {
    authGuest.style.display = 'none';
    authUser.style.display = 'block';

    document.getElementById('navUserName').textContent = currentUser.fullName || currentUser.username;
    document.getElementById('navUserRole').textContent = currentUser.role;
    document.getElementById('navAvatar').textContent = (currentUser.fullName || currentUser.username).charAt(0).toUpperCase();
    document.getElementById('dropdownFullName').textContent = currentUser.fullName || currentUser.username;
    document.getElementById('dropdownEmail').textContent = currentUser.email || '';

    // Autofill query form
    const qName = document.getElementById('queryName');
    const qEmail = document.getElementById('queryEmail');
    if (qName) qName.value = currentUser.fullName || '';
    if (qEmail) qEmail.value = currentUser.email || '';

    if (currentUser.role === 'ADMIN') {
      userOnlyElems.forEach(el => el.style.display = 'none');
      adminOnlyElems.forEach(el => el.style.display = 'inline-flex');
    } else {
      userOnlyElems.forEach(el => el.style.display = 'inline-flex');
      adminOnlyElems.forEach(el => el.style.display = 'none');
      loadUserLoans();
      loadUserReservations();
    }
  } else {
    authGuest.style.display = 'flex';
    authUser.style.display = 'none';
    userOnlyElems.forEach(el => el.style.display = 'none');
    adminOnlyElems.forEach(el => el.style.display = 'none');
  }
}

function toggleUserDropdown() {
  const dd = document.getElementById('userDropdown');
  dd.classList.toggle('show');
}

// Close dropdown if clicked outside
document.addEventListener('click', (e) => {
  if (!e.target.closest('.auth-user')) {
    const dd = document.getElementById('userDropdown');
    if (dd) dd.classList.remove('show');
  }
});

// View Navigation
function switchView(viewName) {
  const views = document.querySelectorAll('.content-view');
  views.forEach(v => v.classList.remove('active'));

  const navLinks = document.querySelectorAll('.nav-link');
  navLinks.forEach(l => l.classList.remove('active'));

  if (viewName === 'catalogue') {
    document.getElementById('viewCatalogue').classList.add('active');
    document.getElementById('navCatalogue').classList.add('active');
    loadBooks();
  } else if (viewName === 'my-loans') {
    if (!requireLogin()) return;
    document.getElementById('viewMyLoans').classList.add('active');
    const nav = document.getElementById('navMyLoans');
    if (nav) nav.classList.add('active');
    loadUserLoans();
  } else if (viewName === 'my-reservations') {
    if (!requireLogin()) return;
    document.getElementById('viewMyReservations').classList.add('active');
    const nav = document.getElementById('navMyReservations');
    if (nav) nav.classList.add('active');
    loadUserReservations();
  } else if (viewName === 'contact') {
    document.getElementById('viewContact').classList.add('active');
    document.getElementById('navContact').classList.add('active');
  } else if (viewName === 'admin-dashboard') {
    if (!requireAdmin()) return;
    document.getElementById('viewAdminDashboard').classList.add('active');
    document.getElementById('navAdminDash').classList.add('active');
    loadStats();
  } else if (viewName === 'admin-books') {
    if (!requireAdmin()) return;
    document.getElementById('viewAdminBooks').classList.add('active');
    document.getElementById('navAdminBooks').classList.add('active');
    renderAdminBooks();
  } else if (viewName === 'admin-issues') {
    if (!requireAdmin()) return;
    document.getElementById('viewAdminIssues').classList.add('active');
    document.getElementById('navAdminIssues').classList.add('active');
    loadAdminIssues(currentAdminIssuesFilter);
  } else if (viewName === 'admin-members') {
    if (!requireAdmin()) return;
    document.getElementById('viewAdminMembers').classList.add('active');
    document.getElementById('navAdminMembers').classList.add('active');
    loadAdminMembers();
  } else if (viewName === 'admin-queries') {
    if (!requireAdmin()) return;
    document.getElementById('viewAdminQueries').classList.add('active');
    document.getElementById('navAdminQueries').classList.add('active');
    loadAdminQueries();
  }

  // Scroll to top
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

function requireLogin() {
  if (!currentUser) {
    showToast('Please log in to access this feature.', 'info');
    openLoginModal('USER');
    return false;
  }
  return true;
}

function requireAdmin() {
  if (!currentUser || currentUser.role !== 'ADMIN') {
    showToast('Administrative privileges required.', 'error');
    openLoginModal('ADMIN');
    return false;
  }
  return true;
}

// ==============================================
// CATALOGUE & BOOKS
// ==============================================
async function loadCategories() {
  try {
    const res = await fetch(`${API_BASE}/books/categories`);
    allCategories = await res.json();
    renderCategories();
  } catch (e) {
    console.error('Error fetching categories:', e);
  }
}

function renderCategories() {
  const container = document.getElementById('categoryPills');
  let html = `<button class="cat-pill ${activeCategory === 'All' ? 'active' : ''}" onclick="filterByCategory('All')">All Books</button>`;
  allCategories.forEach(cat => {
    html += `<button class="cat-pill ${activeCategory === cat ? 'active' : ''}" onclick="filterByCategory('${cat}')">${cat}</button>`;
  });
  container.innerHTML = html;
}

function filterByCategory(cat) {
  activeCategory = cat;
  renderCategories();
  loadBooks();
}

let searchDebounce = null;
function handleSearch() {
  clearTimeout(searchDebounce);
  searchDebounce = setTimeout(() => {
    loadBooks();
  }, 250);
}

async function loadBooks() {
  const searchInput = document.getElementById('searchInput');
  const search = searchInput ? searchInput.value.trim() : '';

  let url = `${API_BASE}/books?`;
  if (activeCategory && activeCategory !== 'All') {
    url += `category=${encodeURIComponent(activeCategory)}&`;
  }
  if (search) {
    url += `search=${encodeURIComponent(search)}`;
  }

  try {
    const res = await fetch(url);
    allBooks = await res.json();
    renderBooksGrid();
  } catch (e) {
    console.error('Error loading books:', e);
  }
}

function sortBooks(type) {
  if (type === 'title-asc') {
    allBooks.sort((a, b) => a.title.localeCompare(b.title));
  } else if (type === 'title-desc') {
    allBooks.sort((a, b) => b.title.localeCompare(a.title));
  } else if (type === 'rating-desc') {
    allBooks.sort((a, b) => b.rating - a.rating);
  } else if (type === 'avail-desc') {
    allBooks.sort((a, b) => b.availableQuantity - a.availableQuantity);
  }
  renderBooksGrid();
}

function renderBooksGrid() {
  const grid = document.getElementById('booksGrid');
  const showingText = document.getElementById('booksShowingText');
  showingText.textContent = `Showing ${allBooks.length} book(s)${activeCategory !== 'All' ? ` in ${activeCategory}` : ''}`;

  if (allBooks.length === 0) {
    grid.innerHTML = `
      <div style="grid-column: 1/-1; text-align: center; padding: 60px 20px; color: var(--text-dim);">
        <i class="fa-solid fa-book-open" style="font-size: 3rem; margin-bottom: 16px; opacity: 0.4;"></i>
        <p style="font-size: 1.1rem; color: #cbd5e1;">No books match your criteria</p>
        <small>Try adjusting your search terms or selecting another category.</small>
      </div>
    `;
    return;
  }

  grid.innerHTML = allBooks.map(book => {
    const isAvail = book.availableQuantity > 0;
    const cover = book.coverUrl || 'https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80';

    return `
      <div class="book-card">
        <div class="book-cover-wrap">
          <img src="${cover}" alt="${book.title}" onerror="this.src='https://images.unsplash.com/photo-1532012164546-f432f2e3edd4?w=400&q=80'">
          <span class="book-category-tag">${book.category}</span>
          <span class="book-rating-tag"><i class="fa-solid fa-star"></i> ${book.rating.toFixed(1)}</span>
        </div>
        <div class="book-content">
          <h3 class="book-title" title="${book.title}">${book.title}</h3>
          <p class="book-author"><i class="fa-solid fa-feather"></i> ${book.author}</p>
          <div class="book-meta">
            <span class="book-isbn">ISBN: ${book.isbn}</span>
            <span class="stock-badge ${isAvail ? 'available' : 'out-of-stock'}">
              <i class="fa-solid ${isAvail ? 'fa-circle-check' : 'fa-circle-xmark'}"></i>
              ${isAvail ? `${book.availableQuantity} of ${book.totalQuantity} Left` : '0 Copies (All Issued)'}
            </span>
          </div>
          <div class="book-actions">
            ${isAvail ? `
              <button class="btn btn-primary btn-sm" onclick="handleIssueBook(${book.id}, '${escapeQuote(book.title)}')">
                <i class="fa-solid fa-arrow-down-to-bracket"></i> Issue Book
              </button>
            ` : `
              <button class="btn btn-warning btn-sm" onclick="handleReserveBook(${book.id}, '${escapeQuote(book.title)}')">
                <i class="fa-solid fa-calendar-check"></i> Advance Booking
              </button>
            `}
            ${currentUser && currentUser.role === 'ADMIN' ? `
              <button class="btn btn-outline btn-sm" title="Edit in Admin Panel" onclick="editBookModal(${book.id})">
                <i class="fa-solid fa-pen"></i>
              </button>
            ` : ''}
          </div>
        </div>
      </div>
    `;
  }).join('');
}

function escapeQuote(str) {
  return (str || '').replace(/'/g, "\\'").replace(/"/g, '&quot;');
}

// ==============================================
// BOOK ISSUING & ADVANCE BOOKING (USER)
// ==============================================
async function handleIssueBook(bookId, bookTitle) {
  if (!requireLogin()) return;

  if (currentUser.role === 'ADMIN') {
    showToast('Admin accounts cannot borrow books directly. Use a student account.', 'info');
    return;
  }

  const confirmMsg = `Borrow "${bookTitle}" for standard 14 days?\n(Due date will be set accordingly. Overdue charge: ₹5/day).`;
  if (!confirm(confirmMsg)) return;

  try {
    const res = await fetch(`${API_BASE}/issues/issue`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUser.id, bookId, days: 14 })
    });

    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadBooks();
      loadStats();
      loadUserLoans();
    } else {
      showToast(data.message || 'Could not issue book', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to library server', 'error');
  }
}

async function handleReserveBook(bookId, bookTitle) {
  if (!requireLogin()) return;

  if (currentUser.role === 'ADMIN') {
    showToast('Admin accounts cannot place reservations.', 'info');
    return;
  }

  const confirmMsg = `All copies of "${bookTitle}" are currently issued.\n\nPlace an Advance Booking (Reservation) so you are waitlisted for the next returned copy?`;
  if (!confirm(confirmMsg)) return;

  try {
    const res = await fetch(`${API_BASE}/reservations`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: currentUser.id, bookId })
    });

    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadUserReservations();
      loadStats();
    } else {
      showToast(data.message || 'Could not place reservation', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to library server', 'error');
  }
}

// ==============================================
// USER LOANS & RETURNS
// ==============================================
async function loadUserLoans() {
  if (!currentUser) return;
  try {
    const res = await fetch(`${API_BASE}/issues?userId=${currentUser.id}`);
    const loans = await res.json();

    const tbody = document.getElementById('myLoansTbody');
    const badge = document.getElementById('myLoansCount');
    const alertBanner = document.getElementById('userFineAlert');
    const alertText = document.getElementById('userFineAlertText');

    let activeCount = 0;
    let totalFine = 0;
    let hasOverdue = false;

    tbody.innerHTML = loans.map(loan => {
      const isOverdue = loan.status === 'OVERDUE';
      const isReturned = loan.status === 'RETURNED';
      if (!isReturned) activeCount++;
      if (isOverdue) {
        hasOverdue = true;
        totalFine += loan.fineAmount;
      }

      let statusBadge = '';
      if (isOverdue) {
        statusBadge = `<span class="badge badge-overdue"><i class="fa-solid fa-triangle-exclamation"></i> OVERDUE</span>`;
      } else if (isReturned) {
        statusBadge = `<span class="badge badge-returned"><i class="fa-solid fa-check"></i> RETURNED</span>`;
      } else {
        statusBadge = `<span class="badge badge-issued"><i class="fa-solid fa-book-bookmark"></i> ISSUED</span>`;
      }

      let fineText = `₹${loan.fineAmount.toFixed(2)}`;
      if (loan.fineAmount > 0) {
        fineText += loan.finePaid ? ' <span class="badge badge-returned" style="font-size: 0.65rem">PAID</span>' : ' <span class="badge badge-overdue" style="font-size: 0.65rem">UNPAID</span>';
      }

      return `
        <tr>
          <td>
            <strong>${loan.bookTitle}</strong><br>
            <small style="color: var(--text-dim)">by ${loan.bookAuthor} | ISBN: ${loan.bookIsbn}</small>
          </td>
          <td>${loan.issueDate}</td>
          <td><strong style="color: ${isOverdue ? 'var(--rose)' : 'inherit'}">${loan.dueDate}</strong></td>
          <td>${statusBadge}</td>
          <td>${fineText}</td>
          <td>
            ${!isReturned ? `
              <button class="btn btn-outline btn-sm" onclick="handleReturnBook(${loan.id}, '${escapeQuote(loan.bookTitle)}')">
                <i class="fa-solid fa-rotate-left"></i> Return Book
              </button>
            ` : `
              <button class="btn btn-outline btn-sm" onclick="showReceipt(${JSON.stringify(loan).replace(/"/g, '&quot;')})">
                <i class="fa-solid fa-receipt"></i> View Receipt
              </button>
            `}
          </td>
        </tr>
      `;
    }).join('');

    if (badge) badge.textContent = activeCount;

    if (hasOverdue && alertBanner) {
      alertBanner.style.display = 'flex';
      alertText.textContent = `You have overdue books! Total fine accumulated: ₹${totalFine.toFixed(2)} (charged at ₹5 per day). Return books immediately to avoid further charges.`;
    } else if (alertBanner) {
      alertBanner.style.display = 'none';
    }

  } catch (e) {
    console.error('Error loading user loans:', e);
  }
}

async function handleReturnBook(issueId, bookTitle) {
  if (!confirm(`Confirm returning "${bookTitle}" to circulation desk?`)) return;

  try {
    const res = await fetch(`${API_BASE}/issues/return`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ issueId })
    });

    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadUserLoans();
      loadBooks();
      loadStats();

      // Show return receipt modal
      renderReturnReceiptModal(bookTitle, data);
    } else {
      showToast(data.message || 'Failed to return book', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to server', 'error');
  }
}

function renderReturnReceiptModal(bookTitle, data) {
  const container = document.getElementById('receiptContent');
  const now = new Date().toLocaleDateString('en-IN', { year: 'numeric', month: 'short', day: 'numeric' });

  container.innerHTML = `
    <div class="receipt-header">
      <h4>Central Digital Library</h4>
      <p style="font-size: 0.75rem; color: var(--text-dim);">Circulation Transaction Receipt</p>
      <small style="color: var(--text-dim);">${now}</small>
    </div>
    <div class="receipt-row">
      <span>Patron Name:</span>
      <strong>${currentUser.fullName}</strong>
    </div>
    <div class="receipt-row">
      <span>Book Returned:</span>
      <strong>${bookTitle}</strong>
    </div>
    <div class="receipt-row">
      <span>Return Date:</span>
      <span>${data.returnDate}</span>
    </div>
    <div class="receipt-row">
      <span>Overdue Days:</span>
      <span>${data.overdueDays} day(s)</span>
    </div>
    <div class="receipt-row ${data.fineAmount > 0 ? 'fine-row' : ''}">
      <span>Fine Rate:</span>
      <span>₹5.00 / day</span>
    </div>
    <div class="receipt-row total ${data.fineAmount > 0 ? 'fine-row' : ''}">
      <span>Total Fine Due:</span>
      <span>₹${data.fineAmount.toFixed(2)}</span>
    </div>
    ${data.reservationAlert ? `
      <div style="margin-top: 14px; padding: 10px; background: rgba(245, 158, 11, 0.15); border: 1px solid rgba(245, 158, 11, 0.3); border-radius: 8px; font-size: 0.8rem; color: #fde68a;">
        <i class="fa-solid fa-bell"></i> ${data.reservationAlert}
      </div>
    ` : ''}
    <div style="margin-top: 16px; font-size: 0.75rem; text-align: center; color: var(--text-dim);">
      Thank you for returning the book! Please keep this receipt for your records.
    </div>
  `;

  openModal('receiptModal');
}

function showReceipt(loan) {
  const container = document.getElementById('receiptContent');
  container.innerHTML = `
    <div class="receipt-header">
      <h4>Central Digital Library</h4>
      <p style="font-size: 0.75rem; color: var(--text-dim);">Transaction Summary</p>
    </div>
    <div class="receipt-row">
      <span>Book Title:</span>
      <strong>${loan.bookTitle}</strong>
    </div>
    <div class="receipt-row">
      <span>Issue Date:</span>
      <span>${loan.issueDate}</span>
    </div>
    <div class="receipt-row">
      <span>Due Date:</span>
      <span>${loan.dueDate}</span>
    </div>
    <div class="receipt-row">
      <span>Return Date:</span>
      <span>${loan.returnDate || 'Active Loan'}</span>
    </div>
    <div class="receipt-row total ${loan.fineAmount > 0 ? 'fine-row' : ''}">
      <span>Fine Amount:</span>
      <span>₹${loan.fineAmount.toFixed(2)} (${loan.finePaid ? 'PAID' : 'UNPAID'})</span>
    </div>
  `;
  openModal('receiptModal');
}

// ==============================================
// USER RESERVATIONS
// ==============================================
async function loadUserReservations() {
  if (!currentUser) return;
  try {
    const res = await fetch(`${API_BASE}/reservations?userId=${currentUser.id}`);
    const list = await res.json();

    const tbody = document.getElementById('myResTbody');
    const badge = document.getElementById('myResCount');

    let pendingCount = 0;
    tbody.innerHTML = list.map((r, idx) => {
      if (r.status === 'PENDING') pendingCount++;
      return `
        <tr>
          <td>
            <strong>${r.bookTitle}</strong><br>
            <small style="color: var(--text-dim)">by ${r.bookAuthor}</small>
          </td>
          <td>${r.reservationDate}</td>
          <td>
            <span class="badge ${r.status === 'PENDING' ? 'badge-pending' : 'badge-returned'}">
              ${r.status}
            </span>
          </td>
          <td>#${idx + 1} in priority queue</td>
          <td>
            ${r.status === 'PENDING' ? `
              <button class="btn btn-danger btn-sm" onclick="cancelReservation(${r.id})">
                <i class="fa-solid fa-xmark"></i> Cancel
              </button>
            ` : '-'}
          </td>
        </tr>
      `;
    }).join('');

    if (badge) badge.textContent = pendingCount;
  } catch (e) {
    console.error('Error loading reservations:', e);
  }
}

async function cancelReservation(id) {
  if (!confirm('Cancel this advance reservation?')) return;
  try {
    const res = await fetch(`${API_BASE}/reservations/${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast('Reservation cancelled', 'info');
      loadUserReservations();
      loadStats();
    }
  } catch (e) {
    showToast('Could not cancel reservation', 'error');
  }
}

// ==============================================
// CONTACT & QUERY FORM
// ==============================================
async function submitQuery(e) {
  e.preventDefault();
  const name = document.getElementById('queryName').value.trim();
  const email = document.getElementById('queryEmail').value.trim();
  const subject = document.getElementById('querySubject').value.trim();
  const message = document.getElementById('queryMessage').value.trim();

  try {
    const res = await fetch(`${API_BASE}/queries`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ name, email, subject, message })
    });

    const data = await res.json();
    if (data.success) {
      showToast('Inquiry submitted! Library staff will review it.', 'success');
      document.getElementById('contactForm').reset();
      loadStats();
    } else {
      showToast('Failed to submit inquiry', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to server', 'error');
  }
}

// ==============================================
// ADMIN MODULE
// ==============================================
async function loadStats() {
  try {
    const res = await fetch(`${API_BASE}/stats`);
    const stats = await res.json();

    // Catalogue hero stats
    safeSetText('statTotalTitles', `${stats.totalBookTitles || 0}+`);
    safeSetText('statAvailCopies', `${stats.availableCopies || 0}`);
    safeSetText('statActiveLoans', `${stats.activeIssues || 0}`);

    // Admin dashboard KPI cards
    safeSetText('kpiTotalTitles', stats.totalBookTitles || 0);
    safeSetText('kpiTotalCopies', `${stats.totalPhysicalCopies || 0} physical copies`);
    safeSetText('kpiActiveLoans', stats.activeIssues || 0);
    safeSetText('kpiOverdueLoans', stats.overdueIssues || 0);
    safeSetText('kpiPendingFines', `₹${(stats.finesPending || 0).toFixed(2)}`);
    safeSetText('kpiCollectedFines', `Collected: ₹${(stats.finesCollected || 0).toFixed(2)}`);
    safeSetText('kpiTotalMembers', stats.totalMembers || 0);
    safeSetText('kpiReservations', stats.pendingReservations || 0);
    safeSetText('adminQueryCount', stats.pendingQueries || 0);
    safeSetText('quickQueryCount', stats.pendingQueries || 0);

  } catch (e) {
    console.error('Error fetching stats:', e);
  }
}

function safeSetText(id, text) {
  const el = document.getElementById(id);
  if (el) el.textContent = text;
}

// Admin Book Management
function renderAdminBooks() {
  const filter = (document.getElementById('adminBookSearch').value || '').toLowerCase();
  const tbody = document.getElementById('adminBooksTbody');

  const filtered = allBooks.filter(b => 
    b.title.toLowerCase().includes(filter) ||
    b.author.toLowerCase().includes(filter) ||
    b.isbn.toLowerCase().includes(filter) ||
    b.category.toLowerCase().includes(filter)
  );

  tbody.innerHTML = filtered.map(b => `
    <tr>
      <td>#${b.id}</td>
      <td>
        <img src="${b.coverUrl}" style="width: 40px; height: 55px; object-fit: cover; border-radius: 4px;" onerror="this.src='https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=400&q=80'">
      </td>
      <td>
        <strong>${b.title}</strong><br>
        <small style="color: var(--text-dim)">by ${b.author}</small>
      </td>
      <td><span class="badge badge-issued">${b.category}</span></td>
      <td><code style="font-size: 0.8rem">${b.isbn}</code></td>
      <td><strong>${b.availableQuantity}</strong> / ${b.totalQuantity}</td>
      <td>
        <div style="display: flex; gap: 6px;">
          <button class="btn btn-outline btn-sm" onclick="editBookModal(${b.id})"><i class="fa-solid fa-pen"></i></button>
          <button class="btn btn-danger btn-sm" onclick="deleteBook(${b.id}, '${escapeQuote(b.title)}')"><i class="fa-solid fa-trash"></i></button>
        </div>
      </td>
    </tr>
  `).join('');
}

function openAddBookModal() {
  document.getElementById('bookModalTitle').innerHTML = '<i class="fa-solid fa-book"></i> Add New Book to Catalogue';
  document.getElementById('bookForm').reset();
  document.getElementById('bookFormId').value = '';
  openModal('bookModal');
}

function editBookModal(id) {
  const b = allBooks.find(x => x.id === id);
  if (!b) return;

  document.getElementById('bookModalTitle').innerHTML = '<i class="fa-solid fa-pen-to-square"></i> Edit Book Details';
  document.getElementById('bookFormId').value = b.id;
  document.getElementById('bookTitle').value = b.title;
  document.getElementById('bookAuthor').value = b.author;
  document.getElementById('bookIsbn').value = b.isbn;
  document.getElementById('bookCategory').value = b.category;
  document.getElementById('bookTotalQty').value = b.totalQuantity;
  document.getElementById('bookAvailQty').value = b.availableQuantity;
  document.getElementById('bookCoverUrl').value = b.coverUrl || '';
  document.getElementById('bookYear').value = b.publishedYear || '';
  document.getElementById('bookDescription').value = b.description || '';

  openModal('bookModal');
}

async function saveBook(e) {
  e.preventDefault();
  const idStr = document.getElementById('bookFormId').value;
  const isEdit = !!idStr;

  const bookData = {
    title: document.getElementById('bookTitle').value.trim(),
    author: document.getElementById('bookAuthor').value.trim(),
    isbn: document.getElementById('bookIsbn').value.trim(),
    category: document.getElementById('bookCategory').value.trim(),
    totalQuantity: parseInt(document.getElementById('bookTotalQty').value),
    availableQuantity: parseInt(document.getElementById('bookAvailQty').value),
    coverUrl: document.getElementById('bookCoverUrl').value.trim(),
    publishedYear: document.getElementById('bookYear').value.trim(),
    description: document.getElementById('bookDescription').value.trim(),
    rating: 4.8
  };

  if (isEdit) bookData.id = parseInt(idStr);

  try {
    const res = await fetch(`${API_BASE}/books`, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(bookData)
    });

    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      closeModal('bookModal');
      loadBooks();
      loadCategories();
      loadStats();
    } else {
      showToast(data.message || 'Error saving book', 'error');
    }
  } catch (e) {
    showToast('Connection error', 'error');
  }
}

async function deleteBook(id, title) {
  if (!confirm(`Are you sure you want to permanently delete "${title}" from catalogue?`)) return;

  try {
    const res = await fetch(`${API_BASE}/books/${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast('Book deleted successfully', 'success');
      loadBooks();
      loadStats();
    } else {
      showToast('Failed to delete book', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to server', 'error');
  }
}

// Admin Issues & Fines
function filterAdminIssues(filter) {
  currentAdminIssuesFilter = filter;
  const btns = document.querySelectorAll('.filter-btn');
  btns.forEach(b => b.classList.remove('active'));
  event.target.classList.add('active');
  loadAdminIssues(filter);
}

async function loadAdminIssues(statusFilter = 'ALL') {
  try {
    const res = await fetch(`${API_BASE}/issues?status=${statusFilter}`);
    const issues = await res.json();

    const tbody = document.getElementById('adminIssuesTbody');
    tbody.innerHTML = issues.map(iss => {
      const isOverdue = iss.status === 'OVERDUE';
      const isReturned = iss.status === 'RETURNED';

      let statusBadge = '';
      if (isOverdue) statusBadge = `<span class="badge badge-overdue"><i class="fa-solid fa-triangle-exclamation"></i> OVERDUE</span>`;
      else if (isReturned) statusBadge = `<span class="badge badge-returned"><i class="fa-solid fa-check"></i> RETURNED</span>`;
      else statusBadge = `<span class="badge badge-issued"><i class="fa-solid fa-book-bookmark"></i> ISSUED</span>`;

      let paymentBadge = '-';
      if (iss.fineAmount > 0) {
        paymentBadge = iss.finePaid 
          ? `<span class="badge badge-returned"><i class="fa-solid fa-check"></i> Paid</span>`
          : `<span class="badge badge-overdue"><i class="fa-solid fa-clock"></i> Pending</span>`;
      }

      return `
        <tr>
          <td>#${iss.id}</td>
          <td>
            <strong>${iss.bookTitle}</strong><br>
            <small style="color: var(--text-dim)">ISBN: ${iss.bookIsbn}</small>
          </td>
          <td>
            <strong>${iss.userName}</strong><br>
            <small style="color: var(--text-dim)">${iss.userEmail}</small>
          </td>
          <td>${iss.issueDate}</td>
          <td><strong style="color: ${isOverdue ? 'var(--rose)' : 'inherit'}">${iss.dueDate}</strong></td>
          <td>${statusBadge}</td>
          <td><strong>₹${iss.fineAmount.toFixed(2)}</strong></td>
          <td>${paymentBadge}</td>
          <td>
            <div style="display: flex; gap: 6px;">
              ${iss.fineAmount > 0 && !iss.finePaid ? `
                <button class="btn btn-success btn-sm" title="Mark Fine as Paid" onclick="markFinePaid(${iss.id})">
                  <i class="fa-solid fa-check-double"></i> Mark Paid
                </button>
              ` : ''}
              ${!isReturned ? `
                <button class="btn btn-outline btn-sm" title="Desk Return Book" onclick="adminReturnBook(${iss.id}, '${escapeQuote(iss.bookTitle)}')">
                  <i class="fa-solid fa-arrow-turn-down-left"></i> Check In
                </button>
              ` : ''}
            </div>
          </td>
        </tr>
      `;
    }).join('');
  } catch (e) {
    console.error('Error loading admin issues:', e);
  }
}

async function markFinePaid(issueId) {
  if (!confirm(`Mark fine of issue #${issueId} as officially collected and paid?`)) return;

  try {
    const res = await fetch(`${API_BASE}/issues/pay-fine`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ issueId })
    });

    const data = await res.json();
    if (data.success) {
      showToast('Fine marked as collected!', 'success');
      loadAdminIssues(currentAdminIssuesFilter);
      loadStats();
    } else {
      showToast('Could not update fine status', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to server', 'error');
  }
}

async function adminReturnBook(issueId, title) {
  if (!confirm(`Check in and process return for "${title}"?`)) return;

  try {
    const res = await fetch(`${API_BASE}/issues/return`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ issueId })
    });

    const data = await res.json();
    if (data.success) {
      showToast(data.message, 'success');
      loadAdminIssues(currentAdminIssuesFilter);
      loadBooks();
      loadStats();
    } else {
      showToast('Error processing return', 'error');
    }
  } catch (e) {
    showToast('Failed to connect to server', 'error');
  }
}

// Admin Member Directory
async function loadAdminMembers() {
  try {
    const res = await fetch(`${API_BASE}/users`);
    const users = await res.json();

    const tbody = document.getElementById('adminMembersTbody');
    tbody.innerHTML = users.map(u => `
      <tr>
        <td>#${u.id}</td>
        <td><strong>${u.fullName}</strong></td>
        <td><code>${u.username}</code></td>
        <td>
          ${u.email}<br>
          <small style="color: var(--text-dim)">${u.phone || 'No phone'}</small>
        </td>
        <td>
          <span class="badge ${u.role === 'ADMIN' ? 'badge-amber' : 'badge-issued'}">
            ${u.role}
          </span>
        </td>
        <td>${u.createdAt}</td>
        <td>
          ${u.id !== currentUser.id ? `
            <button class="btn btn-danger btn-sm" title="Delete Member" onclick="deleteMember(${u.id}, '${escapeQuote(u.fullName)}')">
              <i class="fa-solid fa-trash"></i>
            </button>
          ` : '<small style="color: var(--text-dim)">Current Session</small>'}
        </td>
      </tr>
    `).join('');
  } catch (e) {
    console.error('Error loading members:', e);
  }
}

async function deleteMember(id, name) {
  if (!confirm(`Delete user "${name}" from system? This will also remove their issue history.`)) return;

  try {
    const res = await fetch(`${API_BASE}/users/${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast('User deleted', 'info');
      loadAdminMembers();
      loadStats();
    }
  } catch (e) {
    showToast('Failed to delete member', 'error');
  }
}

// Admin Queries
async function loadAdminQueries() {
  try {
    const res = await fetch(`${API_BASE}/queries`);
    const list = await res.json();

    const tbody = document.getElementById('adminQueriesTbody');
    tbody.innerHTML = list.map(q => `
      <tr>
        <td>#${q.id}</td>
        <td>
          <strong>${q.name}</strong><br>
          <small style="color: var(--text-dim)">${q.email}</small>
        </td>
        <td><strong>${q.subject}</strong></td>
        <td style="max-width: 250px;">
          <p style="font-size: 0.82rem; margin-bottom: 4px;">${q.message}</p>
          ${q.adminReply ? `<small style="color: #6ee7b7;"><i class="fa-solid fa-reply"></i> <strong>Replied:</strong> ${q.adminReply}</small>` : ''}
        </td>
        <td>
          <span class="badge ${q.status === 'PENDING' ? 'badge-pending' : 'badge-returned'}">
            ${q.status}
          </span>
        </td>
        <td>${q.createdAt}</td>
        <td>
          <div style="display: flex; gap: 6px;">
            <button class="btn btn-primary btn-sm" onclick="openReplyModal(${q.id}, '${escapeQuote(q.name)}', '${escapeQuote(q.subject)}', '${escapeQuote(q.message)}')">
              <i class="fa-solid fa-reply"></i> Reply
            </button>
            <button class="btn btn-danger btn-sm" onclick="deleteQuery(${q.id})">
              <i class="fa-solid fa-trash"></i>
            </button>
          </div>
        </td>
      </tr>
    `).join('');
  } catch (e) {
    console.error('Error loading queries:', e);
  }
}

function openReplyModal(id, name, subject, message) {
  document.getElementById('replyQueryId').value = id;
  document.getElementById('queryDetailsBox').innerHTML = `
    <strong>From:</strong> ${name}<br>
    <strong>Subject:</strong> ${subject}<br>
    <div style="margin-top: 6px; padding: 8px; background: rgba(0,0,0,0.2); border-radius: 6px; font-size: 0.85rem;">${message}</div>
  `;
  document.getElementById('queryReplyText').value = '';
  openModal('queryReplyModal');
}

async function sendQueryReply(e) {
  e.preventDefault();
  const id = document.getElementById('replyQueryId').value;
  const reply = document.getElementById('queryReplyText').value.trim();

  try {
    const res = await fetch(`${API_BASE}/queries/${id}`, {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ reply })
    });

    const data = await res.json();
    if (data.success) {
      showToast('Resolution reply sent and inquiry marked as resolved!', 'success');
      closeModal('queryReplyModal');
      loadAdminQueries();
      loadStats();
    }
  } catch (e) {
    showToast('Failed to send reply', 'error');
  }
}

async function deleteQuery(id) {
  if (!confirm('Delete this inquiry?')) return;
  try {
    const res = await fetch(`${API_BASE}/queries/${id}`, { method: 'DELETE' });
    const data = await res.json();
    if (data.success) {
      showToast('Inquiry deleted', 'info');
      loadAdminQueries();
      loadStats();
    }
  } catch (e) {
    showToast('Failed to delete query', 'error');
  }
}

// ==============================================
// AUTHENTICATION (LOGIN, REGISTER, LOGOUT)
// ==============================================
function openLoginModal(rolePreference) {
  const title = document.getElementById('loginModalTitle');
  if (rolePreference === 'ADMIN') {
    title.innerHTML = '<i class="fa-solid fa-shield-halved"></i> Administrator Authentication';
    document.getElementById('loginUsername').value = 'admin';
    document.getElementById('loginPassword').value = 'admin123';
  } else {
    title.innerHTML = '<i class="fa-solid fa-arrow-right-to-bracket"></i> Student & Faculty Sign In';
    document.getElementById('loginUsername').value = 'student1';
    document.getElementById('loginPassword').value = 'user123';
  }
  document.getElementById('loginError').style.display = 'none';
  openModal('loginModal');
}

function openRegisterModal() {
  closeModal('loginModal');
  document.getElementById('registerForm').reset();
  document.getElementById('regError').style.display = 'none';
  openModal('registerModal');
}

function switchToRegister() {
  openRegisterModal();
}

async function handleLogin(e) {
  e.preventDefault();
  const username = document.getElementById('loginUsername').value.trim();
  const password = document.getElementById('loginPassword').value.trim();
  const errEl = document.getElementById('loginError');

  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });

    const data = await res.json();
    if (data.success) {
      currentUser = data.user;
      localStorage.setItem('bibliotech_user', JSON.stringify(currentUser));
      closeModal('loginModal');
      applyUserRoleUI();
      showToast(data.message, 'success');

      if (currentUser.role === 'ADMIN') {
        switchView('admin-dashboard');
      } else {
        switchView('catalogue');
      }
    } else {
      errEl.textContent = data.message || 'Invalid credentials';
      errEl.style.display = 'block';
    }
  } catch (e) {
    errEl.textContent = 'Server connection failed';
    errEl.style.display = 'block';
  }
}

async function handleRegister(e) {
  e.preventDefault();
  const fullName = document.getElementById('regFullName').value.trim();
  const username = document.getElementById('regUsername').value.trim();
  const email = document.getElementById('regEmail').value.trim();
  const phone = document.getElementById('regPhone').value.trim();
  const password = document.getElementById('regPassword').value.trim();
  const errEl = document.getElementById('regError');

  try {
    const res = await fetch(`${API_BASE}/auth/register`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fullName, username, email, phone, password })
    });

    const data = await res.json();
    if (data.success) {
      currentUser = data.user;
      localStorage.setItem('bibliotech_user', JSON.stringify(currentUser));
      closeModal('registerModal');
      applyUserRoleUI();
      showToast(data.message, 'success');
      switchView('catalogue');
    } else {
      errEl.textContent = data.message || 'Registration failed';
      errEl.style.display = 'block';
    }
  } catch (e) {
    errEl.textContent = 'Server connection error';
    errEl.style.display = 'block';
  }
}

function logout() {
  currentUser = null;
  localStorage.removeItem('bibliotech_user');
  applyUserRoleUI();
  showToast('You have signed out successfully.', 'info');
  switchView('catalogue');
}

// 1-Click Quick Examiner Demo
async function quickLogin(username, password) {
  try {
    const res = await fetch(`${API_BASE}/auth/login`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username, password })
    });
    const data = await res.json();
    if (data.success) {
      currentUser = data.user;
      localStorage.setItem('bibliotech_user', JSON.stringify(currentUser));
      applyUserRoleUI();
      showToast(`Logged in as ${currentUser.fullName} (${currentUser.role})`, 'success');
      if (currentUser.role === 'ADMIN') {
        switchView('admin-dashboard');
      } else {
        switchView('my-loans');
      }
    }
  } catch (e) {
    showToast('Failed quick demo login', 'error');
  }
}

// ==============================================
// MODAL & TOAST HELPERS
// ==============================================
function openModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.add('show');
}

function closeModal(id) {
  const m = document.getElementById(id);
  if (m) m.classList.remove('show');
}

// Close modal when clicking outside card
document.querySelectorAll('.modal-overlay').forEach(overlay => {
  overlay.addEventListener('click', (e) => {
    if (e.target === overlay) {
      overlay.classList.remove('show');
    }
  });
});

function showToast(msg, type = 'info') {
  const container = document.getElementById('toastContainer');
  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  let icon = 'fa-circle-info';
  if (type === 'success') icon = 'fa-circle-check';
  if (type === 'error') icon = 'fa-circle-xmark';

  toast.innerHTML = `<i class="fa-solid ${icon}"></i> <span>${msg}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(100%)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
