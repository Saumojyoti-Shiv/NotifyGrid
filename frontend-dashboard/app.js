'use strict';

// ─── APP STATE ────────────────────────────────────────────────────
const state = {
    contacts: [],           // { name, phone, email }
    campaigns: [],          // { id, name, group, message, schedule, status, sentAt, recipientCount }
    groups: [],             // { id, name, filter, contacts: [] }
    deliveryLogs: {},       // { [campaignId]: [{ name, phone, status, time }] }
    currentUser: null,
    currentFilter: 'all',
};

// ─── UTILS ───────────────────────────────────────────────────────
const $ = (id) => document.getElementById(id);
const qs = (sel, ctx = document) => ctx.querySelector(sel);
const qsa = (sel, ctx = document) => [...ctx.querySelectorAll(sel)];
const fmt = (n) => n.toLocaleString();
const fmtDT = (iso) => {
    if (!iso) return '—';
    const d = new Date(iso);
    return d.toLocaleString('en-IN', { dateStyle: 'medium', timeStyle: 'short' });
};

let toastTimer;
function toast(msg, type = 'info') {
    const el = $('toast');
    el.textContent = msg;
    el.className = `toast ${type} show`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => el.classList.remove('show'), 4000);
}

function showModal(html) {
    $('modal-content').innerHTML = html;
    $('modal-overlay').style.display = 'flex';
}
function closeModal() {
    $('modal-overlay').style.display = 'none';
    $('modal-content').innerHTML = '';
}

// ─── API HELPERS ──────────────────────────────────────────────────
const API_BASE = 'http://localhost:8080/api';

async function apiFetch(endpoint, options = {}) {
    const token = localStorage.getItem('jwt');
    const headers = {
        'Content-Type': 'application/json',
        ...(token && { 'Authorization': `Bearer ${token}` }),
        ...options.headers
    };
    const res = await fetch(`${API_BASE}${endpoint}`, { ...options, headers });
    if (!res.ok) {
        const error = await res.text();
        throw new Error(error || `API Error: ${res.status}`);
    }
    const text = await res.text();
    return text ? JSON.parse(text) : null;
}

async function refreshData() {
    try {
        const [contacts, groups, campaigns] = await Promise.all([
            apiFetch('/contacts'),
            apiFetch('/contacts/groups'),
            apiFetch('/campaigns')
        ]);
        
        state.contacts = contacts || [];
        state.groups = (groups || []).map(g => ({
            id: g.id,
            name: g.name,
            filter: g.filterPrefix,
            contacts: g.contacts || []
        }));
        state.campaigns = (campaigns || []).map(c => ({
            id: c.id,
            name: c.name,
            group: c.groupId === 'all' ? 'All Contacts' : (state.groups.find(g => String(g.id) === String(c.groupId))?.name || 'Unknown Group'),
            message: c.message,
            schedule: c.schedule,
            status: c.status,
            sentAt: c.sentAt,
            recipientCount: c.recipientCount || 0
        }));
        
        renderContactsList();
        renderGroups();
        renderCampaignsList();
        renderDashboard();
    } catch (err) {
        console.error('Failed to refresh data:', err);
        toast('Failed to load data from backend.', 'error');
    }
}

function persist() {
    localStorage.setItem('nexussms_state', JSON.stringify({
        campaigns: state.campaigns,
        deliveryLogs: state.deliveryLogs,
    }));
}
async function hydrate() {
    try {
        const raw = localStorage.getItem('nexussms_state');
        if (raw) {
            const s = JSON.parse(raw);
            state.deliveryLogs = s.deliveryLogs || {};
        }
        
        if (localStorage.getItem('jwt')) {
            await refreshData();
        }
    } catch (_) {}
}

// ─── ROUTING ─────────────────────────────────────────────────────
const VIEW_TITLES = {
    'view-dashboard':   'Dashboard',
    'view-contacts':    'Contacts',
    'view-campaigns':   'Campaigns',
    'view-delivery':    'Delivery Log',
    'view-billing':     'Billing & Credits',
    'view-architecture':'Architecture',
};

let deliveryRefreshTimer;

function switchView(viewId) {
    qsa('.view').forEach(v => v.classList.remove('active'));
    qsa('.nav-link').forEach(n => n.classList.remove('active'));
    $(viewId)?.classList.add('active');
    qs(`.nav-link[data-view="${viewId}"]`)?.classList.add('active');
    $('view-title').textContent = VIEW_TITLES[viewId] || '';
    
    clearInterval(deliveryRefreshTimer);
    
    if (viewId === 'view-dashboard')  renderDashboard();
    if (viewId === 'view-contacts')   renderContactsList();
    if (viewId === 'view-campaigns')  renderCampaignsList();
    if (viewId === 'view-delivery') {
        renderDeliveryLog();
        deliveryRefreshTimer = setInterval(() => renderDeliveryLog(), 5000);
    }
    if (viewId === 'view-billing') renderBilling();
}
window.switchView = switchView;  // expose for inline onclick

// ─── LOGIN ───────────────────────────────────────────────────────
function initLogin() {
    // Toggle password visibility
    $('toggle-pass').addEventListener('click', () => {
        const inp = $('login-pass');
        const icon = $('toggle-pass').querySelector('i');
        if (inp.type === 'password') { inp.type = 'text'; icon.className = 'fa-solid fa-eye-slash'; }
        else { inp.type = 'password'; icon.className = 'fa-solid fa-eye'; }
    });

    $('login-form').addEventListener('submit', (e) => {
        e.preventDefault();
        let valid = true;
        const user = $('login-user').value.trim();
        const pass = $('login-pass').value.trim();
        if (!user) { $('fg-user').classList.add('error'); valid = false; } else $('fg-user').classList.remove('error');
        if (!pass) { $('fg-pass').classList.add('error'); valid = false; } else $('fg-pass').classList.remove('error');
        if (!valid) return;

        const btnText = $('login-btn-text');
        const btnSpin = $('login-btn-spin');
        const btn = $('login-btn');
        btnText.style.display = 'none'; btnSpin.style.display = 'inline-flex'; btn.disabled = true;

        console.log('Attempting login for:', user);
        fetch('http://localhost:8080/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ username: user, password: pass })
        })
        .then(async res => {
            if (!res.ok) {
                const errText = await res.text();
                console.error('Login Response Not OK:', res.status, errText);
                throw new Error('Login failed: ' + res.status);
            }
            return res.json();
        })
        .then(data => {
            console.log('Login Success, received data:', data);
            if (data.token) localStorage.setItem('jwt', data.token);
            localStorage.setItem('username', user);
            state.currentUser = user;
            $('sidebar-username').textContent = user;
            $('login-screen').classList.remove('active');
            $('app-screen').classList.add('active');
            btnText.style.display = 'inline-flex'; btnSpin.style.display = 'none'; btn.disabled = false;
            toast(`Welcome, ${user}! Real token received from Auth Service.`, 'success');
            switchView('view-dashboard');
        })
        .catch(err => {
            console.error(err);
            btnText.style.display = 'inline-flex'; btnSpin.style.display = 'none'; btn.disabled = false;
            toast('Login failed. Are the backend services running?', 'error');
        });
    });
}

// ─── SIDEBAR ─────────────────────────────────────────────────────
function initSidebar() {
    $('sidebar-toggle').addEventListener('click', () => $('sidebar').classList.toggle('collapsed'));
    qsa('.nav-link').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            switchView(link.dataset.view);
        });
    });
    $('logout-btn').addEventListener('click', () => {
        $('app-screen').classList.remove('active');
        $('login-screen').classList.add('active');
        $('login-form').reset();
        $('fg-user').classList.remove('error');
        $('fg-pass').classList.remove('error');
        toast('Logged out successfully.', 'info');
    });
}

// ─── DASHBOARD ───────────────────────────────────────────────────
const SERVICES = [
    { name: 'API Gateway',        port: 8080, icon: 'fa-route' },
    { name: 'Auth Service',       port: 8081, icon: 'fa-lock' },
    { name: 'User Service',       port: 8082, icon: 'fa-user' },
    { name: 'Contact Service',    port: 8083, icon: 'fa-address-book' },
    { name: 'Campaign Service',   port: 8084, icon: 'fa-bullhorn' },
    { name: 'Messaging Service',  port: 8085, icon: 'fa-comment-sms' },
    { name: 'Delivery Report',    port: 8086, icon: 'fa-chart-line' },
    { name: 'Billing Service',    port: 8087, icon: 'fa-wallet' },
    { name: 'Notification Svc',   port: 8088, icon: 'fa-bell' },
    { name: 'Scheduler Service',  port: 8089, icon: 'fa-clock' },
    { name: 'Eureka Registry',    port: 8761, icon: 'fa-globe' },
];

function renderDashboard() {
    const hasData = state.contacts.length > 0 || state.campaigns.length > 0;
    $('dashboard-empty').style.display = hasData ? 'none' : 'flex';
    $('dashboard-data').style.display = hasData ? 'block' : 'none';
    if (!hasData) return;

    const sent = state.campaigns.filter(c => c.status === 'sent');
    const totalMessages = sent.reduce((a, c) => a + (c.recipientCount || 0), 0);
    // Since messaging service has a 5% simulated failure rate
    const estimatedFailed = Math.round(totalMessages * 0.05); 

    $('st-contacts').textContent  = fmt(state.contacts.length);
    $('st-campaigns').textContent = fmt(sent.length);
    $('st-messages').textContent  = fmt(totalMessages);
    $('st-failed').textContent    = fmt(estimatedFailed);

    // Campaign history table
    const tbody = $('dash-campaign-list');
    tbody.innerHTML = state.campaigns.length ? '' : '<tr><td colspan="4" style="text-align:center;padding:20px;color:var(--muted)">No campaigns yet.</td></tr>';
    [...state.campaigns].reverse().slice(0, 10).forEach(c => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>${c.name}</td>
            <td>${fmt(c.recipientCount || 0)}</td>
            <td><span class="status-badge ${c.status}">${c.status}</span></td>
            <td>${fmtDT(c.sentAt || c.schedule)}</td>
        `;
        tbody.appendChild(tr);
    });

    // Health list
    const hl = $('health-list');
    hl.innerHTML = '';
    SERVICES.forEach(s => {
        const div = document.createElement('div');
        div.className = 'health-item';
        div.id = `health-${s.port}`;
        div.innerHTML = `
            <div class="health-item-name">
                <i class="fa-solid ${s.icon}"></i>
                <span>${s.name}</span>
                <span style="font-size:11px;color:var(--muted)">:${s.port}</span>
            </div>
            <div class="health-dot loading" title="Checking..."></div>
        `;
        hl.appendChild(div);
        
        // Real check (via gateway proxy if possible, or direct)
        // Since we are on localhost, we can try direct fetch
        fetch(`http://localhost:${s.port}/actuator/health`).then(res => {
            const dot = div.querySelector('.health-dot');
            dot.className = `health-dot ${res.ok ? 'up' : 'down'}`;
            dot.title = res.ok ? 'UP' : 'DOWN';
        }).catch(() => {
            const dot = div.querySelector('.health-dot');
            dot.className = `health-dot down`;
            dot.title = 'DOWN';
        });
    });
}

// ─── CONTACTS ────────────────────────────────────────────────────
function parseCSV(text) {
    const lines = text.trim().split('\n').map(l => l.trim()).filter(Boolean);
    if (lines.length < 2) return null;
    const headers = lines[0].split(',').map(h => h.trim().replace(/['"]/g, '').toLowerCase());
    return lines.slice(1).map(line => {
        const vals = line.match(/(".*?"|[^,]+)/g)?.map(v => v.replace(/^"|"$/g, '').trim()) || line.split(',');
        const obj = {};
        headers.forEach((h, i) => { obj[h] = vals[i] || ''; });
        return obj;
    });
}

function normalizeContacts(rows) {
    return rows.map(r => ({
        name:  r.name  || r.Name  || r.NAME  || '',
        phone: (r.phone || r.Phone || r.PHONE || r.mobile || r.Mobile || '').replace(/\s/g, ''),
        email: r.email || r.Email || r.EMAIL || '',
    })).filter(r => r.name || r.phone);
}

let previewData = [];

function renderContactPreview(rows, fileName) {
    previewData = rows;
    const all = normalizeContacts(rows);
    $('contact-file-name').textContent = fileName;
    $('contact-row-count').textContent = `${all.length} valid contacts`;
    $('contact-preview-wrap').style.display = 'block';

    const thead = $('contact-preview-head');
    const tbody = $('contact-preview-body');
    thead.innerHTML = '<tr><th>Name</th><th>Phone</th><th>Email</th></tr>';
    tbody.innerHTML = '';
    const preview = all.slice(0, 20);
    preview.forEach(r => {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td>${r.name}</td><td>${r.phone}</td><td>${r.email || '—'}</td>`;
        tbody.appendChild(tr);
    });
    if (all.length > 20) {
        const tr = document.createElement('tr');
        tr.innerHTML = `<td colspan="3" style="text-align:center;color:var(--muted);font-size:12px">... and ${all.length - 20} more rows</td>`;
        tbody.appendChild(tr);
    }
}

function handleFile(file) {
    if (!file) return;
    const ext = file.name.split('.').pop().toLowerCase();
    if (ext === 'csv') {
        const reader = new FileReader();
        reader.onload = (e) => {
            const rows = parseCSV(e.target.result);
            if (!rows) { toast('Could not parse CSV. Check the file format.', 'error'); return; }
            renderContactPreview(rows, file.name);
        };
        reader.readAsText(file);
    } else if (ext === 'xlsx' || ext === 'xls') {
        const reader = new FileReader();
        reader.onload = (e) => {
            const wb = XLSX.read(e.target.result, { type: 'array' });
            const ws = wb.Sheets[wb.SheetNames[0]];
            const rows = XLSX.utils.sheet_to_json(ws, { defval: '' });
            if (!rows.length) { toast('Empty spreadsheet or unsupported format.', 'error'); return; }
            renderContactPreview(rows, file.name);
        };
        reader.readAsArrayBuffer(file);
    } else {
        toast('Unsupported file type. Please upload a CSV or XLSX file.', 'error');
    }
}

function updateSelectionBar() {
    const checked = qsa('#contacts-list-body input[type="checkbox"]:checked');
    const bar = $('selection-bar');
    bar.style.display = checked.length ? 'flex' : 'none';
    $('selection-count').textContent = `${checked.length} contact${checked.length !== 1 ? 's' : ''} selected`;
}

function renderContactsList(filter = '') {
    const contacts = filter
        ? state.contacts.filter(c => c.name.toLowerCase().includes(filter) || c.phone.includes(filter))
        : state.contacts;

    const panel = $('saved-contacts-panel');
    panel.style.display = state.contacts.length ? 'grid' : 'none';
    $('contacts-count-badge').textContent = fmt(state.contacts.length);
    $('contacts-page-info').textContent =
        filter
            ? `Showing ${contacts.length} of ${fmt(state.contacts.length)} contacts`
            : `${fmt(state.contacts.length)} contacts total`;

    const tbody = $('contacts-list-body');
    tbody.innerHTML = '';
    contacts.forEach((c, i) => {
        const realIdx = state.contacts.indexOf(c);
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><input type="checkbox" class="row-cb" data-idx="${realIdx}"></td>
            <td>${i + 1}</td>
            <td>${escHtml(c.name)}</td>
            <td>${escHtml(c.phone)}</td>
            <td>${escHtml(c.email || '—')}</td>`;
        tbody.appendChild(tr);
    });
    if (!contacts.length) {
        tbody.innerHTML = '<tr><td colspan="5" style="text-align:center;padding:16px;color:var(--muted)">No contacts match your search.</td></tr>';
    }

    // Row checkboxes → update bar
    tbody.querySelectorAll('.row-cb').forEach(cb => {
        cb.addEventListener('change', () => {
            const allCbs = qsa('#contacts-list-body .row-cb');
            const allChecked = allCbs.every(c => c.checked);
            $('select-all-cb').checked = allChecked;
            $('select-all-cb').indeterminate = !allChecked && allCbs.some(c => c.checked);
            updateSelectionBar();
        });
    });

    // Select-all
    const selectAll = $('select-all-cb');
    selectAll.checked = false;
    selectAll.indeterminate = false;
    selectAll.onchange = () => {
        tbody.querySelectorAll('.row-cb').forEach(cb => cb.checked = selectAll.checked);
        updateSelectionBar();
    };

    populateGroupSelect();
    renderGroups();
}


function initContacts() {
    const dropZone = $('contact-drop');
    const fileInput = $('contact-file');

    dropZone.addEventListener('click', (e) => { if (!e.target.closest('button')) fileInput.click(); });
    fileInput.addEventListener('change', () => { if (fileInput.files[0]) handleFile(fileInput.files[0]); });

    dropZone.addEventListener('dragover', (e) => { e.preventDefault(); dropZone.classList.add('drag-over'); });
    dropZone.addEventListener('dragleave', () => dropZone.classList.remove('drag-over'));
    dropZone.addEventListener('drop', (e) => {
        e.preventDefault(); dropZone.classList.remove('drag-over');
        const file = e.dataTransfer.files[0];
        if (file) handleFile(file);
    });

    $('contact-clear-btn').addEventListener('click', () => {
        previewData = [];
        $('contact-preview-wrap').style.display = 'none';
        fileInput.value = '';
    });

    $('contact-save-btn').addEventListener('click', async () => {
        const normalized = normalizeContacts(previewData);
        if (!normalized.length) { toast('No valid contacts to save.', 'error'); return; }
        
        const btn = $('contact-save-btn');
        const oldText = btn.innerHTML;
        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Saving...';

        try {
            const savedCount = await apiFetch('/contacts/batch', {
                method: 'POST',
                body: JSON.stringify(normalized)
            });
            
            await refreshData();
            $('contact-preview-wrap').style.display = 'none';
            $('contact-file').value = '';
            previewData = [];
            toast(`Saved ${savedCount} contacts to backend database.`, 'success');
        } catch (err) {
            console.error(err);
            toast('Failed to save contacts to backend.', 'error');
        } finally {
            btn.disabled = false;
            btn.innerHTML = oldText;
        }
    });

    $('contact-search').addEventListener('input', (e) => renderContactsList(e.target.value.toLowerCase().trim()));

    // Download template
    $('download-template').addEventListener('click', (e) => {
        e.preventDefault();
        const csv = 'name,phone,email\nJohn Doe,9876543210,john@example.com\nJane Smith,9123456789,jane@example.com';
        const blob = new Blob([csv], { type: 'text/csv' });
        const a = document.createElement('a');
        a.href = URL.createObjectURL(blob);
        a.download = 'contacts_template.csv';
        a.click();
        toast('Template downloaded!', 'info');
    });
}

// ─── CONTACT GROUPS ──────────────────────────────────────────────
function escHtml(str) {
    return String(str)
        .replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;');
}

function renderGroups() {
    const list = $('groups-list');
    const empty = $('groups-empty');
    $('groups-count-badge').textContent = state.groups.length;
    empty.style.display = state.groups.length ? 'none' : 'flex';
    list.innerHTML = '';
    state.groups.forEach(g => {
        const div = document.createElement('div');
        div.className = 'group-item';
        div.innerHTML = `
            <div class="group-item-info">
                <strong>${escHtml(g.name)}</strong>
                <span>${fmt(g.contacts.length)} contacts${g.filter ? ' · prefix: ' + escHtml(g.filter) : ''}</span>
            </div>
            <div class="group-item-actions">
                <button class="btn-use-group" data-gid="${g.id}"><i class="fa-solid fa-paper-plane"></i> Use in Campaign</button>
                <button class="btn-del-group" data-gid="${g.id}" title="Delete group"><i class="fa-solid fa-trash"></i></button>
            </div>
        `;
        list.appendChild(div);
    });
    // Delete
    list.querySelectorAll('.btn-del-group').forEach(btn => {
        btn.addEventListener('click', async () => {
            const id = btn.dataset.gid;
            try {
                await apiFetch(`/contacts/groups/${id}`, { method: 'DELETE' });
                await refreshData();
                toast('Group deleted from backend.', 'info');
            } catch (err) {
                console.error(err);
                toast('Failed to delete group.', 'error');
            }
        });
    });
    // Use in Campaign
    list.querySelectorAll('.btn-use-group').forEach(btn => {
        btn.addEventListener('click', () => {
            const g = state.groups.find(g => String(g.id) === String(btn.dataset.gid));
            if (!g) return;
            switchView('view-campaigns');
            $('camp-group').value = g.id;
            toast(`Group "${g.name}" selected in campaign form.`, 'success');
        });
    });
}

function initGroups() {
    // Create group by prefix filter
    $('create-group-btn').addEventListener('click', async () => {
        const name = $('group-name-input').value.trim();
        const prefix = $('group-filter-input').value.trim();
        if (!name) { toast('Please enter a group name.', 'error'); $('group-name-input').focus(); return; }
        if (!state.contacts.length) { toast('No contacts imported yet.', 'error'); return; }
        
        const filtered = prefix
            ? state.contacts.filter(c => c.phone.startsWith(prefix))
            : [...state.contacts];
            
        if (!filtered.length) { toast(`No contacts match prefix "${prefix}".`, 'error'); return; }
        
        try {
            await apiFetch('/contacts/groups', {
                method: 'POST',
                body: JSON.stringify({
                    name,
                    filterPrefix: prefix,
                    contactIds: filtered.map(c => c.id)
                })
            });
            $('group-name-input').value = '';
            $('group-filter-input').value = '';
            await refreshData();
            toast(`Group "${name}" created with ${fmt(filtered.length)} contacts.`, 'success');
        } catch (err) {
            console.error(err);
            toast('Failed to create group in backend.', 'error');
        }
    });

    // Create group from checkbox selection
    $('create-from-sel-btn').addEventListener('click', async () => {
        const name = $('sel-group-name').value.trim();
        if (!name) { toast('Enter a group name in the selection bar.', 'error'); $('sel-group-name').focus(); return; }
        const checkedIdxs = [...qsa('#contacts-list-body .row-cb:checked')].map(cb => parseInt(cb.dataset.idx));
        if (!checkedIdxs.length) { toast('No contacts selected.', 'error'); return; }
        
        const selected = checkedIdxs.map(i => state.contacts[i]).filter(Boolean);
        
        try {
            await apiFetch('/contacts/groups', {
                method: 'POST',
                body: JSON.stringify({
                    name,
                    filterPrefix: '',
                    contactIds: selected.map(c => c.id)
                })
            });
            $('sel-group-name').value = '';
            // Uncheck all
            qsa('#contacts-list-body .row-cb').forEach(cb => cb.checked = false);
            $('select-all-cb').checked = false;
            $('selection-bar').style.display = 'none';
            await refreshData();
            toast(`Group "${name}" created from ${fmt(selected.length)} selected contacts.`, 'success');
        } catch (err) {
            console.error(err);
            toast('Failed to create group from selection.', 'error');
        }
    });

    // Clear selection
    $('clear-sel-btn').addEventListener('click', () => {
        qsa('#contacts-list-body .row-cb').forEach(cb => cb.checked = false);
        $('select-all-cb').checked = false;
        $('selection-bar').style.display = 'none';
    });
}

// ─── DELIVERY LOG ─────────────────────────────────────────────────
async function dispatchCampaign(campaign) {
    try {
        await apiFetch(`/campaigns/${campaign.id}/dispatch`, { method: 'POST' });
        await refreshData();
        toast(`📨 Scheduled campaign "${campaign.name}" just fired! Messages sent via Messaging Service.`, 'success');
    } catch (err) {
        console.error(err);
        toast('Failed to dispatch scheduled campaign.', 'error');
    }
}

async function renderDeliveryLog(filter = '') {
    const sentCampaigns = state.campaigns.filter(c => c.status === 'sent');
    $('delivery-badge').textContent = sentCampaigns.length;
    $('delivery-empty').style.display = sentCampaigns.length ? 'none' : 'flex';
    const container = $('delivery-log-list');
    container.innerHTML = '';

    for (const campaign of [...sentCampaigns].reverse()) {
        if (filter && !campaign.name.toLowerCase().includes(filter)) continue;

        let entries = [];
        try {
            entries = await apiFetch(`/delivery/campaign/${campaign.id}`);
        } catch (err) {
            console.error(`Failed to fetch logs for campaign ${campaign.id}`, err);
        }

        const delivered = entries.filter(e => e.status === 'delivered').length;
        const failed    = entries.filter(e => e.status === 'failed').length;
        const pending   = campaign.recipientCount - entries.length;
        const rate      = entries.length ? Math.round((delivered / entries.length) * 100) : 0;

        const block = document.createElement('div');
        block.className = 'delivery-campaign-block';
        block.innerHTML = `
            <div class="delivery-campaign-header" onclick="this.nextElementSibling.style.display = this.nextElementSibling.style.display === 'none' ? 'block' : 'none'">
                <div>
                    <h4><i class="fa-solid fa-paper-plane" style="color:var(--primary);margin-right:8px"></i>${escHtml(campaign.name)}</h4>
                    <div class="delivery-meta" style="margin-top:4px">
                        <span>Group: ${escHtml(campaign.group)}</span>
                        <span>Sent: ${fmtDT(campaign.sentAt)}</span>
                        <span>Delivery Rate: <strong style="color:${rate>=95?'var(--success)':rate>=80?'var(--warning)':'var(--danger)'}">${rate}%</strong></span>
                    </div>
                </div>
                <div class="delivery-stats">
                    <span class="d-stat delivered"><i class="fa-solid fa-circle-check"></i> ${fmt(delivered)}</span>
                    <span class="d-stat failed"><i class="fa-solid fa-circle-xmark"></i> ${fmt(failed)}</span>
                    <span class="d-stat pending"><i class="fa-solid fa-clock"></i> ${fmt(pending > 0 ? pending : 0)}</span>
                    <i class="fa-solid fa-chevron-down" style="color:var(--muted);margin-left:8px"></i>
                </div>
            </div>
            <div class="delivery-rows" style="display:none">
                ${entries.slice(0, 50).map(e => `
                    <div class="delivery-row">
                        <span class="dr-name">${escHtml(e.phone)}</span>
                        <span class="dr-phone">Recipient</span>
                        <span class="dr-status ${e.status}"><i class="fa-solid fa-${e.status==='delivered'?'check':e.status==='failed'?'xmark':'clock'}"></i> ${e.status}</span>
                        <span class="dr-time">${fmtDT(e.timestamp)}</span>
                    </div>`).join('')}
                ${entries.length > 50 ? `<div class="delivery-row" style="color:var(--muted);justify-content:center">+ ${entries.length - 50} more recipients</div>` : ''}
                ${entries.length === 0 ? `<div class="delivery-row" style="color:var(--muted);justify-content:center">No logs available yet. Processing...</div>` : ''}
            </div>
        `;
        container.appendChild(block);
    }
}

// ─── SCHEDULED CAMPAIGN DISPATCHER ────────────────────────────────
function startScheduler() {
    function checkScheduled() {
        const now = new Date();
        state.campaigns.forEach(c => {
            if (c.status === 'scheduled' && c.schedule) {
                const scheduledAt = new Date(c.schedule);
                if (scheduledAt <= now) {
                    dispatchCampaign(c);
                }
            }
        });
    }
    // Check immediately on load, then every 30 seconds
    checkScheduled();
    setInterval(checkScheduled, 30000);
}


// ─── CAMPAIGNS ───────────────────────────────────────────────────
function populateGroupSelect() {
    const sel = $('camp-group');
    const prev = sel.value;
    sel.innerHTML = '<option value="">-- Select contact group --</option>';
    if (state.contacts.length) {
        const opt = document.createElement('option');
        opt.value = 'all';
        opt.textContent = `All Contacts (${fmt(state.contacts.length)})`;
        sel.appendChild(opt);
    }
    state.groups.forEach(g => {
        const opt = document.createElement('option');
        opt.value = g.id;
        opt.textContent = `${g.name} (${fmt(g.contacts.length)})`;
        sel.appendChild(opt);
    });
    if (sel.querySelector(`option[value="${prev}"]`)) sel.value = prev;
}

function renderCampaignsList() {
    populateGroupSelect();
    const filtered = state.currentFilter === 'all'
        ? state.campaigns
        : state.campaigns.filter(c => c.status === state.currentFilter);

    $('campaigns-badge').textContent = state.campaigns.length;
    const wrap = $('campaigns-list');
    const empty = $('campaigns-empty-mini');

    wrap.innerHTML = '';
    empty.style.display = state.campaigns.length ? 'none' : 'flex';

    if (!filtered.length && state.campaigns.length) {
        wrap.innerHTML = '<div class="empty-mini"><i class="fa-solid fa-filter"></i><p>No campaigns match this filter.</p></div>';
        return;
    }

    [...filtered].reverse().forEach(c => {
        const div = document.createElement('div');
        div.className = 'campaign-card';
        div.innerHTML = `
            <div class="campaign-card-info">
                <h4>${c.name}</h4>
                <p>${c.message}</p>
                <div style="margin-top:6px;font-size:11px;color:var(--muted)">Group: ${c.group} &nbsp;·&nbsp; ${fmt(c.recipientCount || 0)} recipients</div>
            </div>
            <div class="campaign-card-meta">
                <span class="status-badge ${c.status}">${c.status}</span>
                <time>${fmtDT(c.sentAt || c.schedule)}</time>
            </div>
        `;
        wrap.appendChild(div);
    });
}

function initCampaigns() {
    // Filter tabs
    qsa('.filter-tabs .tab').forEach(tab => {
        tab.addEventListener('click', () => {
            qsa('.filter-tabs .tab').forEach(t => t.classList.remove('active'));
            tab.classList.add('active');
            state.currentFilter = tab.dataset.filter;
            renderCampaignsList();
        });
    });

    // Send Mode toggle – show/hide schedule field
    qsa('input[name="send-mode"]').forEach(radio => {
        radio.addEventListener('change', () => {
            const isLater = $('send-later').checked;
            $('schedule-field').style.display = isLater ? 'block' : 'none';
            const btn = $('launch-btn');
            if (isLater) {
                btn.innerHTML = '<i class="fa-solid fa-clock"></i> Schedule Campaign';
            } else {
                btn.innerHTML = '<i class="fa-solid fa-rocket"></i> Launch Campaign';
            }
            if (!isLater) $('camp-schedule').value = '';
        });
    });

    // Char counter
    $('camp-msg').addEventListener('input', () => {
        const len = $('camp-msg').value.length;
        $('char-count').textContent = len;
        const parts = Math.ceil(len / 160) || 1;
        $('sms-parts').textContent = parts > 1 ? `(${parts} SMS parts)` : '';
        $('char-count').style.color = len > 160 ? 'var(--warning)' : 'var(--muted)';
    });

    // Save Draft
    $('save-draft-btn').addEventListener('click', async () => {
        const name = $('camp-name').value.trim();
        if (!name) { $('fg-camp-name').classList.add('error'); return; }
        $('fg-camp-name').classList.remove('error');
        const group = $('camp-group').value;
        const message = $('camp-msg').value.trim();
        const schedule = $('camp-schedule').value;
        
        try {
            await apiFetch('/campaigns', {
                method: 'POST',
                body: JSON.stringify({
                    name,
                    groupId: group || 'all',
                    message,
                    schedule,
                    status: 'draft'
                })
            });
            await refreshData();
            $('campaign-form').reset();
            $('char-count').textContent = '0';
            $('sms-parts').textContent = '';
            toast(`Draft "${name}" saved to backend.`, 'info');
        } catch (err) {
            console.error(err);
            toast('Failed to save draft to backend.', 'error');
        }
    });

    // Submit – Launch
    $('campaign-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        let valid = true;
        const name    = $('camp-name').value.trim();
        const group   = $('camp-group').value;
        const message = $('camp-msg').value.trim();

        if (!name)    { $('fg-camp-name').classList.add('error'); valid = false; } else $('fg-camp-name').classList.remove('error');
        if (!group)   { $('fg-camp-group').classList.add('error'); valid = false; } else $('fg-camp-group').classList.remove('error');
        if (!message) { $('fg-camp-msg').classList.add('error'); valid = false; } else $('fg-camp-msg').classList.remove('error');
        if (!valid) return;

        if (!state.contacts.length) {
            toast('No contacts saved. Please import contacts first.', 'error');
            switchView('view-contacts');
            return;
        }

        const btn = $('launch-btn');
        const isScheduled = $('send-later').checked;
        const schedule = isScheduled ? $('camp-schedule').value.trim() : '';

        if (isScheduled && !schedule) {
            $('schedule-error').style.display = 'block';
            $('camp-schedule').focus();
            return;
        }
        if (isScheduled) $('schedule-error').style.display = 'none';

        btn.disabled = true;
        btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Launching...';

        try {
            await apiFetch('/campaigns', {
                method: 'POST',
                body: JSON.stringify({
                    name,
                    groupId: group,
                    message,
                    schedule,
                    status: isScheduled ? 'scheduled' : 'sent',
                    createdBy: localStorage.getItem('username') || 'admin'
                })
            });
            
            await refreshData();
            $('campaign-form').reset();
            $('send-now').checked = true;
            $('schedule-field').style.display = 'none';
            $('char-count').textContent = '0';
            $('sms-parts').textContent = '';
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-rocket"></i> Launch Campaign';
            toast(
                !isScheduled
                    ? `Campaign "${name}" launched! Messages queued via RabbitMQ.`
                    : `Campaign "${name}" scheduled for ${fmtDT(schedule)}.`,
                'success'
            );
        } catch (err) {
            console.error(err);
            toast('Failed to launch campaign.', 'error');
            btn.disabled = false;
            btn.innerHTML = '<i class="fa-solid fa-rocket"></i> Launch Campaign';
        }
    });
}

// ─── MODAL ───────────────────────────────────────────────────────
$('modal-close').addEventListener('click', closeModal);
$('modal-overlay').addEventListener('click', (e) => { if (e.target === $('modal-overlay')) closeModal(); });

// ─── INIT ────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', async () => {
    await hydrate();
    initLogin();
    initApp();
});

function initApp() {
    initSidebar();
    initContacts();
    initGroups();
    initCampaigns();
    initBilling();
    startScheduler();
    // Delivery log search
    $('delivery-search').addEventListener('input', (e) => renderDeliveryLog(e.target.value.toLowerCase().trim()));
}

function initBilling() {
    // Refresh balance every 10 seconds
    setInterval(updateBalance, 10000);
    updateBalance();
}

async function updateBalance() {
    const username = localStorage.getItem('username') || 'testuser';
    try {
        const balance = await apiFetch(`/billing/balance/${username}`);
        const el = document.getElementById('user-credits');
        if (el) el.textContent = balance.toLocaleString();
        const elHero = document.getElementById('billing-balance-hero');
        if (elHero) elHero.textContent = balance.toLocaleString();
    } catch (err) {
        console.error('Failed to fetch balance:', err);
    }
}

async function renderBilling() {
    const username = localStorage.getItem('username') || 'testuser';
    const list = $('billing-history');
    if (!list) return;

    // Add Restore Button at the top of transaction history if it doesn't exist
    let restoreBtn = $('restore-credits-btn');
    if (!restoreBtn) {
        const header = list.closest('.panel').querySelector('.panel-header');
        restoreBtn = document.createElement('button');
        restoreBtn.id = 'restore-credits-btn';
        restoreBtn.className = 'btn-secondary';
        restoreBtn.style.fontSize = '12px';
        restoreBtn.innerHTML = '<i class="fa-solid fa-plus-circle"></i> Add 500 Credits';
        restoreBtn.onclick = async () => {
            try {
                await apiFetch(`/billing/recharge`, {
                    method: 'POST',
                    body: JSON.stringify({ username, amount: 500 })
                });
                toast('Successfully added 500 credits!', 'success');
                updateBalance();
                renderBilling();
            } catch (err) {
                toast('Failed to restore credits.', 'error');
            }
        };
        header.appendChild(restoreBtn);
    }

    try {
        const history = await apiFetch(`/billing/history/${username}`);
        list.innerHTML = history.slice().reverse().map(h => `
            <tr>
                <td>${new Date(h.timestamp).toLocaleString()}</td>
                <td><span class="type-badge ${h.type.toLowerCase()}">${h.type}</span></td>
                <td>${h.type === 'DEDUCTION' ? '-' : '+'}${h.amount}</td>
                <td>${escHtml(h.description)}</td>
            </tr>
        `).join('') || '<tr><td colspan="4" class="text-center">No transaction history.</td></tr>';
    } catch (err) {
        console.error(err);
        toast('Failed to load billing history', 'error');
    }
}
