/**
 * Oasis National Bank of India - Enterprise NetBanking & Digital ATM
 * Client Engine & Dual-Mode Connector
 */

// --- Indian Commercial Bank Accounts Ledger ---
const bankLedger = {
  '1001': {
    userId: '1001',
    pin: '1234',
    name: 'Rajesh Kumar Sharma',
    accountNo: '501004928172',
    ifsc: 'ONBI0001089',
    accType: 'Privilege Savings',
    branch: 'Fort Mumbai Main (001089)',
    balance: 125000.00,
    locked: false,
    failedAttempts: 0,
    history: [
      {
        utr: 'ONBI9948210381',
        type: 'Credit',
        amount: 125000.00,
        runningBal: 125000.00,
        date: new Date(Date.now() - 86400000 * 2).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
        narration: 'Salary Credit / NEFT - Tata Consultancy Services Ltd',
        channel: 'NEFT'
      }
    ]
  },
  '1002': {
    userId: '1002',
    pin: '4321',
    name: 'Priya Ramesh Patel',
    accountNo: '501008392103',
    ifsc: 'ONBI0001089',
    accType: 'Classic Savings',
    branch: 'Fort Mumbai Main (001089)',
    balance: 65500.00,
    locked: false,
    failedAttempts: 0,
    history: [
      {
        utr: 'ONBI8812903348',
        type: 'Credit',
        amount: 65500.00,
        runningBal: 65500.00,
        date: new Date(Date.now() - 86400000 * 3).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
        narration: 'Opening Deposit / Cash Deposit Machine',
        channel: 'CDM'
      }
    ]
  },
  '1003': {
    userId: '1003',
    pin: '9999',
    name: 'Amit Vikram Verma',
    accountNo: '501001192847',
    ifsc: 'ONBI0001089',
    accType: 'Corporate Salary',
    branch: 'Fort Mumbai Main (001089)',
    balance: 250000.00,
    locked: false,
    failedAttempts: 0,
    history: [
      {
        utr: 'ONBI7719284729',
        type: 'Credit',
        amount: 250000.00,
        runningBal: 250000.00,
        date: new Date(Date.now() - 86400000 * 4).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
        narration: 'Dividend Payout / BSE Securities',
        channel: 'ACH'
      }
    ]
  },
  '1004': {
    userId: '1004',
    pin: '1111',
    name: 'Ananya Sundaram Iyer',
    accountNo: '501006543219',
    ifsc: 'ONBI0001089',
    accType: 'Student Savings',
    branch: 'Fort Mumbai Main (001089)',
    balance: 42000.00,
    locked: false,
    failedAttempts: 0,
    history: [
      {
        utr: 'ONBI6639201948',
        type: 'Credit',
        amount: 42000.00,
        runningBal: 42000.00,
        date: new Date(Date.now() - 86400000 * 5).toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
        narration: 'UPI Received from sundaram.iyer@okhdfcbank',
        channel: 'UPI'
      }
    ]
  }
};

let currentCustomer = null;
let lastSlipData = null;

// Web Audio Synthesizer for Authentic ATM Haptics
const audioContext = (window.AudioContext || window.webkitAudioContext) ? new (window.AudioContext || window.webkitAudioContext)() : null;

function playTone(frequency, duration, type = 'sine') {
  if (!audioContext) return;
  try {
    if (audioContext.state === 'suspended') audioContext.resume();
    const osc = audioContext.createOscillator();
    const gain = audioContext.createGain();
    osc.type = type;
    osc.frequency.value = frequency;
    gain.gain.setValueAtTime(0.06, audioContext.currentTime);
    gain.gain.exponentialRampToValueAtTime(0.0001, audioContext.currentTime + duration);
    osc.connect(gain);
    gain.connect(audioContext.destination);
    osc.start();
    osc.stop(audioContext.currentTime + duration);
  } catch (e) {}
}

function playCashChime() {
  playTone(523.25, 0.1);
  setTimeout(() => playTone(659.25, 0.1), 120);
  setTimeout(() => playTone(783.99, 0.2), 240);
}

// --- Indian Currency Formatter (Lakhs / Thousands) ---
function formatINR(number) {
  return Number(number).toLocaleString('en-IN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  });
}

// --- Scrambled Virtual Keypad (Indian NetBanking Standard) ---
function generateScrambledKeypad() {
  const container = document.getElementById('keypad-btn-container');
  if (!container) return;

  const numbers = [0, 1, 2, 3, 4, 5, 6, 7, 8, 9];
  // Fisher-Yates shuffle for anti-keylogger scrambling
  for (let i = numbers.length - 1; i > 0; i--) {
    const j = Math.floor(Math.random() * (i + 1));
    [numbers[i], numbers[j]] = [numbers[j], numbers[i]];
  }

  container.innerHTML = '';
  numbers.forEach(num => {
    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'vkey';
    btn.textContent = num;
    btn.onclick = () => {
      playTone(800, 0.04);
      const pinInput = document.getElementById('cust-pin');
      if (pinInput && pinInput.value.length < 4) {
        pinInput.value += num;
      }
    };
    container.appendChild(btn);
  });

  // Clear button
  const clearBtn = document.createElement('button');
  clearBtn.type = 'button';
  clearBtn.className = 'vkey vkey-action';
  clearBtn.textContent = 'CLEAR';
  clearBtn.onclick = () => {
    playTone(400, 0.06);
    document.getElementById('cust-pin').value = '';
  };
  container.appendChild(clearBtn);

  // Backspace button
  const backBtn = document.createElement('button');
  backBtn.type = 'button';
  backBtn.className = 'vkey vkey-action';
  backBtn.textContent = '⌫';
  backBtn.onclick = () => {
    playTone(500, 0.04);
    const pinInput = document.getElementById('cust-pin');
    pinInput.value = pinInput.value.slice(0, -1);
  };
  container.appendChild(backBtn);
}

function toggleVirtualKeypad() {
  const keypad = document.getElementById('virtual-keypad');
  if (keypad) {
    keypad.classList.toggle('hidden');
    if (!keypad.classList.contains('hidden')) {
      generateScrambledKeypad();
    }
  }
}

// --- Customer Selection / Auto-Fill ---
function selectDemoCustomer(userId, pin) {
  playTone(880, 0.05);
  const cust = bankLedger[userId];
  if (!cust) return;

  document.getElementById('cust-userid').value = userId;
  document.getElementById('cust-pin').value = pin;

  // Update RuPay Card Preview
  const cardNo = document.getElementById('card-cardnumber');
  const cardName = document.getElementById('card-holdername');
  if (cardNo) cardNo.textContent = cust.accountNo.slice(0, 4) + ' •••• •••• ' + cust.accountNo.slice(-4);
  if (cardName) cardName.textContent = cust.name.toUpperCase();
}

// --- Authentication & 3-Attempt Security Lockout ---
function submitLogin() {
  const userId = document.getElementById('cust-userid').value.trim();
  const pin = document.getElementById('cust-pin').value.trim();
  const alertEl = document.getElementById('login-alert');

  alertEl.classList.add('hidden');
  alertEl.textContent = '';

  const cust = bankLedger[userId];

  if (!cust) {
    alertEl.textContent = 'Invalid Customer User ID. Please check your credentials or enter registered ID.';
    alertEl.classList.remove('hidden');
    playTone(220, 0.3, 'sawtooth');
    return;
  }

  if (cust.locked) {
    alertEl.textContent = 'ACCOUNT BLOCKED: 3 consecutive incorrect PIN attempts. Please visit your home branch with KYC documents or call 1800-425-3800.';
    alertEl.classList.remove('hidden');
    playTone(180, 0.4, 'sawtooth');
    return;
  }

  if (cust.pin === pin) {
    cust.failedAttempts = 0;
    currentCustomer = cust;
    playTone(1046, 0.15);

    renderDashboard();
  } else {
    cust.failedAttempts++;
    const remaining = 3 - cust.failedAttempts;
    playTone(250, 0.3, 'sawtooth');

    if (cust.failedAttempts >= 3) {
      cust.locked = true;
      alertEl.textContent = 'SECURITY LOCKOUT: 3 consecutive incorrect PIN attempts! Your account has been temporarily frozen for your protection.';
    } else {
      alertEl.textContent = `Incorrect PIN! ${remaining} attempt(s) remaining before security lockout.`;
    }
    alertEl.classList.remove('hidden');
  }
}

// --- Render Authenticated Dashboard ---
function renderDashboard() {
  document.getElementById('auth-portal').classList.add('hidden');
  document.getElementById('dashboard-portal').classList.remove('hidden');

  document.getElementById('dash-cust-name').textContent = currentCustomer.name;
  document.getElementById('dash-acc-no').textContent = currentCustomer.accountNo;
  document.getElementById('dash-acc-type').textContent = currentCustomer.accType;
  document.getElementById('dash-ifsc').textContent = currentCustomer.ifsc;
  document.getElementById('dash-bal-amt').textContent = formatINR(currentCustomer.balance);

  switchBankTab('tab-passbook');
  renderPassbook();
}

// --- Tab Switching ---
function switchBankTab(tabId) {
  playTone(700, 0.04);
  document.querySelectorAll('.tab-pane').forEach(pane => pane.classList.remove('active-pane'));
  document.querySelectorAll('.bank-tab-btn').forEach(btn => btn.classList.remove('active'));

  const targetPane = document.getElementById(tabId);
  if (targetPane) targetPane.classList.add('active-pane');

  const activeBtn = Array.from(document.querySelectorAll('.bank-tab-btn')).find(b => b.getAttribute('onclick')?.includes(tabId));
  if (activeBtn) activeBtn.classList.add('active');
}

// --- Passbook & Statement Rendering (ArrayList History) ---
function renderPassbook() {
  const tbody = document.getElementById('passbook-rows');
  tbody.innerHTML = '';

  currentCustomer.history.forEach(tx => {
    const tr = document.createElement('tr');
    const isCredit = tx.type === 'Credit';
    const drCell = isCredit ? '-' : `<span class="text-debit">₹${formatINR(tx.amount)}</span>`;
    const crCell = isCredit ? `<span class="text-credit">₹${formatINR(tx.amount)}</span>` : '-';

    tr.innerHTML = `
      <td>${tx.date}</td>
      <td><strong>${tx.utr}</strong></td>
      <td>${tx.narration}</td>
      <td><span class="channel-badge">${tx.channel}</span></td>
      <td>${drCell}</td>
      <td>${crCell}</td>
      <td class="running-bal">₹${formatINR(tx.runningBal)}</td>
    `;
    tbody.appendChild(tr);
  });

  const countEl = document.getElementById('passbook-entry-count');
  if (countEl) countEl.textContent = `Total Ledger Entries: ${currentCustomer.history.length}`;
}

// --- Cash Withdrawal Logic ---
function processWithdrawal(amount) {
  const statusEl = document.getElementById('withdraw-status-msg');
  statusEl.className = 'portal-alert hidden';

  // Strict Balance Check (Requirement: display 'Insufficient Funds')
  if (amount > currentCustomer.balance) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.innerHTML = `<strong>Insufficient Funds:</strong> Requested withdrawal of ₹${formatINR(amount)} exceeds your available balance of ₹${formatINR(currentCustomer.balance)}.`;
    playTone(200, 0.3, 'sawtooth');
    return;
  }

  currentCustomer.balance -= amount;
  const utr = 'ONBIATM' + Math.random().toString(36).substring(2, 8).toUpperCase();
  const tx = {
    utr: utr,
    type: 'Debit',
    amount: amount,
    runningBal: currentCustomer.balance,
    date: new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
    narration: 'Cash Dispense / ATM MUMBAI FORT',
    channel: 'ATM'
  };

  currentCustomer.history.unshift(tx);
  lastSlipData = tx;
  document.getElementById('dash-bal-amt').textContent = formatINR(currentCustomer.balance);
  renderPassbook();

  // Dispenser Animation & Note Breakdown
  animateDispenser(amount);
  playCashChime();

  statusEl.className = 'portal-alert success-alert';
  statusEl.innerHTML = `<strong>Cash Dispensed:</strong> ₹${formatINR(amount)} successfully dispensed. Please collect cash from shutter tray.`;
}

function processCustomWithdrawal() {
  const input = document.getElementById('custom-wth-amt');
  const val = parseFloat(input.value);
  const statusEl = document.getElementById('withdraw-status-msg');

  if (isNaN(val) || val <= 0) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.textContent = 'Please enter a valid numeric amount greater than ₹0.00.';
    return;
  }

  if (val % 100 !== 0) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.textContent = 'ATM dispenses in multiples of ₹100, ₹200, and ₹500 notes only.';
    return;
  }

  processWithdrawal(val);
  input.value = '';
}

function animateDispenser(totalAmount) {
  const shutter = document.getElementById('atm-shutter');
  const shutterLabel = document.getElementById('shutter-label');
  const notesDisplay = document.getElementById('dispensed-notes-display');

  // Indian Note Breakdown Calculation
  let amt = Math.floor(totalAmount);
  const fiveHundreds = Math.floor(amt / 500);
  amt %= 500;
  const twoHundreds = Math.floor(amt / 200);
  amt %= 200;
  const hundreds = Math.floor(amt / 100);

  let badges = '';
  if (fiveHundreds > 0) badges += `<span class="note-badge">₹500 x ${fiveHundreds}</span> `;
  if (twoHundreds > 0) badges += `<span class="note-badge">₹200 x ${twoHundreds}</span> `;
  if (hundreds > 0) badges += `<span class="note-badge">₹100 x ${hundreds}</span> `;

  if (shutter) shutter.classList.add('shutter-open');
  if (shutterLabel) shutterLabel.textContent = 'CASH SHUTTER OPEN • PLEASE COLLECT NOTES';
  if (notesDisplay) {
    notesDisplay.innerHTML = `<div><strong>Dispensed:</strong> ${badges}</div>`;
  }

  setTimeout(() => {
    if (shutter) shutter.classList.remove('shutter-open');
    if (shutterLabel) shutterLabel.textContent = 'CASH SHUTTER CLOSED';
    if (notesDisplay) {
      notesDisplay.innerHTML = `<span class="standby-text">ATM Ready • Multiples of ₹500, ₹200, ₹100 available</span>`;
    }
  }, 5000);
}

// --- Cash Deposit (CDM Recycler) Logic ---
function processDeposit() {
  const input = document.getElementById('deposit-bill-amt');
  const statusEl = document.getElementById('deposit-status-msg');
  const val = parseFloat(input.value);

  if (isNaN(val) || val <= 0) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.textContent = 'Deposit amount must be strictly greater than ₹0.00.';
    playTone(200, 0.25, 'sawtooth');
    return;
  }

  currentCustomer.balance += val;
  const utr = 'ONBICDM' + Math.random().toString(36).substring(2, 8).toUpperCase();
  const tx = {
    utr: utr,
    type: 'Credit',
    amount: val,
    runningBal: currentCustomer.balance,
    date: new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
    narration: 'Cash Deposit Machine (CDM) / Fort Main',
    channel: 'CDM'
  };

  currentCustomer.history.unshift(tx);
  lastSlipData = tx;
  document.getElementById('dash-bal-amt').textContent = formatINR(currentCustomer.balance);
  renderPassbook();
  playCashChime();

  statusEl.className = 'portal-alert success-alert';
  statusEl.innerHTML = `<strong>Deposit Accepted:</strong> ₹${formatINR(val)} credited to ${currentCustomer.accType}. New Balance: ₹${formatINR(currentCustomer.balance)}`;
  input.value = '';
}

// --- Inter-Bank Fund Transfer (IMPS 24x7) ---
function lookupBeneficiary() {
  const input = document.getElementById('beneficiary-id');
  const preview = document.getElementById('beneficiary-preview-text');
  const val = input.value.trim();

  if (val === currentCustomer.userId) {
    preview.style.color = '#dc2626';
    preview.textContent = '⚠️ Self-transfer not allowed. Please enter another beneficiary.';
    return;
  }

  const rec = bankLedger[val];
  if (rec) {
    preview.style.color = '#15803d';
    preview.textContent = `✔ Verified Beneficiary: ${rec.name} (${rec.accountNo.slice(0, 4)}••••${rec.accountNo.slice(-4)})`;
  } else if (val) {
    preview.style.color = '#94a3b8';
    preview.textContent = `Intra-Bank Account ID "${val}" will be routed via National Clearing.`;
  } else {
    preview.textContent = '';
  }
}

function processFundTransfer() {
  const recId = document.getElementById('beneficiary-id').value.trim();
  const amt = parseFloat(document.getElementById('transfer-amt-input').value);
  const remarks = document.getElementById('transfer-remarks').value.trim();
  const statusEl = document.getElementById('transfer-status-msg');

  if (recId === currentCustomer.userId) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.textContent = 'Self-transfer is not permitted. Please enter a different beneficiary account.';
    return;
  }

  if (isNaN(amt) || amt <= 0) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.textContent = 'Transfer amount must be strictly greater than ₹0.00.';
    return;
  }

  // Strict Balance Check (Requirement: display 'Insufficient Funds')
  if (amt > currentCustomer.balance) {
    statusEl.className = 'portal-alert error-alert';
    statusEl.innerHTML = `<strong>Insufficient Funds:</strong> Required ₹${formatINR(amt)}, but your available balance is ₹${formatINR(currentCustomer.balance)}.`;
    playTone(200, 0.3, 'sawtooth');
    return;
  }

  const recipient = bankLedger[recId];
  const recName = recipient ? recipient.name : 'Beneficiary ' + recId;

  // Debit sender
  currentCustomer.balance -= amt;
  const utr = 'ONBIIMPS' + Math.random().toString(36).substring(2, 8).toUpperCase();
  const senderTx = {
    utr: utr,
    type: 'Debit',
    amount: amt,
    runningBal: currentCustomer.balance,
    date: new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
    narration: `IMPS to ${recName} - ${remarks || 'Funds Transfer'}`,
    channel: 'IMPS'
  };
  currentCustomer.history.unshift(senderTx);

  // Credit recipient if in ledger
  if (recipient) {
    recipient.balance += amt;
    recipient.history.unshift({
      utr: utr,
      type: 'Credit',
      amount: amt,
      runningBal: recipient.balance,
      date: new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
      narration: `IMPS from ${currentCustomer.name} - ${remarks || 'Funds Transfer'}`,
      channel: 'IMPS'
    });
  }

  lastSlipData = senderTx;
  document.getElementById('dash-bal-amt').textContent = formatINR(currentCustomer.balance);
  renderPassbook();
  playTone(1046, 0.15);

  statusEl.className = 'portal-alert success-alert';
  statusEl.innerHTML = `<strong>IMPS Successful:</strong> ₹${formatINR(amt)} transferred to ${recName}. UTR: <strong>${utr}</strong>.`;

  document.getElementById('transfer-amt-input').value = '';
  document.getElementById('transfer-remarks').value = '';
}

// --- RuPay Card Controls ---
function notifySetting(name) {
  playTone(800, 0.05);
  alert(`Oasis NetBanking Security Alert: ${name} preference updated successfully for your RuPay Platinum Card.`);
}

function toggleCardFreeze(btn) {
  playTone(400, 0.1);
  if (btn.textContent === 'Lock Card') {
    btn.textContent = 'Unlock Card';
    btn.style.background = '#059669';
    btn.style.color = '#fff';
    alert('Your RuPay Platinum Debit Card has been temporarily frozen. All ATM and online transactions are halted.');
  } else {
    btn.textContent = 'Lock Card';
    btn.style.background = '#fee2e2';
    btn.style.color = '#b91c1c';
    alert('Your RuPay Platinum Debit Card has been unblocked. Standard domestic limits restored.');
  }
}

// --- Thermal Receipt / Slip Modal ---
function printOfficialPassbook() {
  if (currentCustomer.history.length > 0) {
    lastSlipData = currentCustomer.history[0];
  }
  openReceiptModal();
}

function openReceiptModal() {
  playTone(800, 0.06);
  const modal = document.getElementById('receipt-modal');
  const container = document.getElementById('receipt-dynamic-content');

  const tx = lastSlipData || (currentCustomer.history.length > 0 ? currentCustomer.history[0] : {
    utr: 'ONBI000108932',
    type: 'Inquiry',
    amount: 0.00,
    runningBal: currentCustomer.balance,
    date: new Date().toLocaleString('en-IN', { timeZone: 'Asia/Kolkata' }),
    narration: 'Account Summary / Balance Inquiry',
    channel: 'ATM'
  });

  container.innerHTML = `
    <div class="receipt-field-row"><span>DATE &amp; TIME</span><span>${tx.date}</span></div>
    <div class="receipt-field-row"><span>BRANCH / TERMINAL</span><span>FORT MUMBAI (001089)</span></div>
    <div class="receipt-field-row"><span>ACCOUNT NUMBER</span><span>${currentCustomer.accountNo.slice(0, 4)}XXXX${currentCustomer.accountNo.slice(-4)}</span></div>
    <div class="receipt-field-row"><span>CARDHOLDER NAME</span><span>${currentCustomer.name.toUpperCase()}</span></div>
    <div class="receipt-field-row"><span>IFSC CODE</span><span>${currentCustomer.ifsc}</span></div>
    <div class="receipt-field-row"><span>UTR / REF NUMBER</span><span>${tx.utr}</span></div>
    <p class="dotted-sep">----------------------------------------------------</p>
    <div class="receipt-field-row"><span>TRANSACTION TYPE</span><span>${tx.type.toUpperCase()}</span></div>
    <div class="receipt-field-row"><span>CHANNEL</span><span>${tx.channel}</span></div>
    <div class="receipt-field-row"><span>AMOUNT</span><span>₹ ${formatINR(tx.amount)}</span></div>
    <div class="receipt-field-row"><span>AVAILABLE BALANCE</span><span>₹ ${formatINR(tx.runningBal)}</span></div>
    <div class="receipt-field-row"><span>RESPONSE CODE</span><span>00 (APPROVED)</span></div>
    <div class="receipt-field-row"><span>NARRATION</span><span>${tx.narration.substring(0, 30)}</span></div>
  `;

  modal.classList.remove('hidden');
}

function closeReceiptModal() {
  document.getElementById('receipt-modal').classList.add('hidden');
}

function simulateDownloadStatement() {
  playTone(880, 0.08);
  alert(`Official PDF Statement for Account ${currentCustomer.accountNo} has been generated and queued for download.`);
}

// --- Secure Logout ---
function performLogout() {
  playTone(440, 0.2);
  currentCustomer = null;
  lastSlipData = null;
  document.getElementById('cust-userid').value = '';
  document.getElementById('cust-pin').value = '';
  document.getElementById('login-alert').classList.add('hidden');

  document.getElementById('dashboard-portal').classList.add('hidden');
  document.getElementById('auth-portal').classList.remove('hidden');

  alert('You have been securely logged out. Thank you for banking with Oasis National Bank of India.');
}

// Initial Keypad Generation
window.addEventListener('DOMContentLoaded', () => {
  generateScrambledKeypad();
});
