/* ==========================================================
   Operations Manager Portal - Photography Scheduling & Assignment (FR03)
   Talks to: /api/ops/assignments/*   Session: /api/auth/*
   ========================================================== */
const API = '/api/ops/assignments';
const WEEK_LIMIT_HOURS = 40;

const state = { user: null, events: [], roster: [], photographers: [] };
let ctx = {};   // data for whichever modal is open

/* ---------- helpers ---------- */
const $ = (id) => document.getElementById(id);
const esc = (s) => String(s ?? '').replace(/[&<>"']/g, (c) =>
  ({ '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#039;' }[c]));

// Dates/times may arrive as "2026-10-24" / "17:00:00" or as arrays; handle both.
function toDate(v) {
  if (!v) return null;
  if (Array.isArray(v)) return new Date(v[0], v[1] - 1, v[2]);
  const [y, m, d] = String(v).split('-').map(Number);
  return new Date(y, m - 1, d);
}
function toTime(v) {
  if (!v) return '';
  if (Array.isArray(v)) return String(v[0]).padStart(2, '0') + ':' + String(v[1] ?? 0).padStart(2, '0');
  return String(v).slice(0, 5);
}
function fmtDate(v) {
  const d = toDate(v);
  return d ? d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' }) : '';
}
const when = (e) => `${fmtDate(e.eventDate)} (${toTime(e.startTime)} - ${toTime(e.endTime)})`;
function hoursOf(e) {
  const [sh, sm] = toTime(e.startTime).split(':').map(Number);
  const [eh, em] = toTime(e.endTime).split(':').map(Number);
  return Math.max(0, ((eh * 60 + em) - (sh * 60 + sm)) / 60);
}
const evtId = (id) => '#EVT-' + String(id).padStart(4, '0');
function initialsOf(name) {
  const p = String(name || '').trim().split(/\s+/);
  return ((p[0]?.[0] || '') + (p[1]?.[0] || '')).toUpperCase() || '?';
}
function today() { const t = new Date(); t.setHours(0, 0, 0, 0); return t; }
const sameDay = (a, b) => a && b && a.getTime() === b.getTime();

// Options for one assignment. On the day of the event the crew is locked: no Reassign/Remove.
function crewButtons(assignmentId, eventDate) {
  if (sameDay(eventDate, today())) {
    return '<span class="badge badge-soft-primary" title="Crew cannot be changed on the day of the event">'
      + '<i class="bi bi-lock-fill me-1"></i>Today\'s Event</span>';
  }
  return `<button class="btn btn-sm btn-outline-secondary" onclick="openReassign(${assignmentId})">Reassign</button>` +
    `<button class="btn btn-sm btn-outline-danger ms-1" onclick="openRemove(${assignmentId})">Remove</button>`;
}

function weekRange() {
  const t = today();
  const mon = new Date(t); mon.setDate(t.getDate() - ((t.getDay() + 6) % 7));
  const sun = new Date(mon); sun.setDate(mon.getDate() + 6);
  return [mon, sun];
}
function weeklyHours(pid) {
  const [a, b] = weekRange();
  return state.roster
    .filter((r) => r.photographerId === pid)
    .filter((r) => { const d = toDate(r.eventDate); return d >= a && d <= b; })
    .reduce((s, r) => s + hoursOf(r), 0);
}

function toast(message, type = 'success') {
  const el = document.createElement('div');
  el.className = `toast align-items-center text-bg-${type} border-0`;
  el.setAttribute('role', 'alert');
  el.innerHTML = `<div class="d-flex"><div class="toast-body">${esc(message)}</div>
    <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close"></button></div>`;
  $('toastBox').appendChild(el);
  const t = new bootstrap.Toast(el, { delay: 4500 });
  el.addEventListener('hidden.bs.toast', () => el.remove());
  t.show();
}

function showInline(id, message) { const el = $(id); el.textContent = message; el.classList.remove('d-none'); }
function hideInline(id) { $(id).classList.add('d-none'); }

// Open a modal; if another one is open, close it first.
function showModal(id) {
  const open = document.querySelector('.modal.show');
  const target = bootstrap.Modal.getOrCreateInstance($(id));
  if (open && open.id !== id) {
    open.addEventListener('hidden.bs.modal', () => target.show(), { once: true });
    bootstrap.Modal.getInstance(open).hide();
  } else {
    target.show();
  }
}
const hideModal = (id) => bootstrap.Modal.getOrCreateInstance($(id)).hide();

function setBusy(btn, busy, label) {
  if (busy) { btn.dataset.label = btn.innerHTML; btn.disabled = true; btn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>' + (label || 'Working...'); }
  else { btn.disabled = false; btn.innerHTML = btn.dataset.label || btn.innerHTML; }
}

/* ---------- API ---------- */
async function api(path, options = {}) {
  let res;
  try {
    res = await fetch(API + path, {
      credentials: 'include',
      headers: { 'Content-Type': 'application/json' },
      ...options
    });
  } catch (e) {
    return { ok: false, message: 'Cannot reach the server. Is the application running?' };
  }
  if (res.status === 401) { window.location.href = 'login.html'; return { ok: false, message: 'Please log in.' }; }
  if (res.status === 403) return { ok: false, message: 'You do not have permission for this action.' };
  let body = null;
  try { body = await res.json(); } catch (e) { /* not JSON */ }
  const ok = res.ok && body && body.status === true;
  return { ok, message: (body && (body.message || body.error)) || ('Request failed (' + res.status + ').'), data: body ? body.data : null };
}

/* ---------- session ---------- */
async function checkSession() {
  try {
    const res = await fetch('/api/auth/session', { credentials: 'include' });
    const body = await res.json();
    if (!(res.ok && body.status && body.data)) throw new Error('no session');
    const role = (body.data.role || '').toString().trim().toUpperCase().replace(/[\s_-]+/g, '');
    if (role !== 'OPERATIONSMANAGER' && role !== 'OPERATIONMANAGER') {
      window.location.href = 'login.html';
      return false;
    }
    state.user = body.data;
    const name = `${body.data.firstName || ''} ${body.data.lastName || ''}`.trim();
    $('userName').textContent = name || 'Operations Manager';
    $('userInitials').textContent = initialsOf(name);
    return true;
  } catch (e) {
    sessionStorage.setItem('postLoginRedirect', window.location.pathname);
    window.location.href = 'login.html';
    return false;
  }
}

async function signOut() {
  try { await fetch('/api/auth/logout', { method: 'POST', credentials: 'include' }); } catch (e) { /* ignore */ }
  window.location.href = 'login.html';
}

/* ---------- load + render ---------- */
async function loadAll() {
  const [ev, ro] = await Promise.all([api('/events'), api('/roster')]);
  if (!ev.ok) toast(ev.message, 'danger');
  if (!ro.ok) toast(ro.message, 'danger');
  state.events = ev.ok ? (ev.data || []) : [];
  state.roster = ro.ok ? (ro.data || []) : [];
  await loadPhotographers();
  renderAll();
}

// All photographer profiles (active and inactive) from GET /api/ops/assignments/photographers
async function loadPhotographers() {
  const r = await api('/photographers');
  if (!r.ok) { toast(r.message, 'danger'); state.photographers = []; return; }
  state.photographers = (r.data || []).map((p) => ({
    id: p.photographerId, name: p.name, email: p.email, mobile: p.mobile,
    exp: p.experienceYears, bio: p.bio, active: p.active, upcoming: p.upcomingShifts
  }));
}

async function syncRoster() {
  const btn = $('syncBtn');
  setBusy(btn, true, 'Syncing...');
  await loadAll();
  setBusy(btn, false);
  toast('Roster refreshed.');
}

function renderAll() {
  renderStats();
  renderUrgent();
  renderEvents();
  renderRoster();
  renderRegistry();
}

function renderStats() {
  const unassigned = state.events.filter((e) => e.assignedCount === 0).length;
  $('statEvents').textContent = state.events.length;
  $('statUnassigned').textContent = unassigned;
  $('statPhotographers').textContent = state.photographers.filter((p) => p.active).length;
  $('statShifts').textContent = state.roster.length;

  const badge = $('coverageBadge');
  if (unassigned > 0) { badge.className = 'badge badge-soft-warning'; badge.textContent = `${unassigned} Needs Coverage`; }
  else { badge.className = 'badge badge-soft-success'; badge.textContent = 'All Events Covered'; }
}

function renderUrgent() {
  const limit = new Date(today()); limit.setDate(limit.getDate() + 7);
  const urgent = state.events.filter((e) => e.assignedCount === 0 && toDate(e.eventDate) <= limit);
  const box = $('urgentAlert');
  if (!urgent.length) { box.classList.add('d-none'); box.classList.remove('d-flex'); return; }
  const first = urgent[0];
  $('urgentAlertText').textContent = urgent.length === 1
    ? `${first.eventTitle} on ${fmtDate(first.eventDate)} has no photographer assigned yet.`
    : `${urgent.length} events in the next 7 days have no photographer assigned. Next: ${first.eventTitle} on ${fmtDate(first.eventDate)}.`;
  $('urgentAlertBtn').onclick = () => openAssign(first.bookingId);
  box.classList.remove('d-none'); box.classList.add('d-flex');
}

function renderEvents() {
  const body = $('eventsBody');
  if (!state.events.length) {
    body.innerHTML = `<tr><td colspan="7" class="empty-state"><i class="bi bi-calendar-x"></i>No upcoming confirmed events.</td></tr>`;
    return;
  }
  body.innerHTML = state.events.map((e) => {
    const crew = e.assignedCount
      ? e.assignedPhotographers.map((n) => `<span class="badge badge-soft-success me-1">${esc(n)}</span>`).join('')
      : '<span class="badge badge-soft-warning">Not Assigned</span>';
    const action = e.assignedCount
      ? `<button class="btn btn-sm btn-light border" onclick="openDetails(${e.bookingId})">Details</button>`
      : `<button class="btn btn-sm btn-primary" onclick="openAssign(${e.bookingId})">Assign Crew</button>`;
    return `<tr>
      <td class="fw-semibold">${evtId(e.bookingId)}</td>
      <td>${esc(e.eventTitle)}</td>
      <td>${esc(when(e))}</td>
      <td>${esc(e.eventLocation)}</td>
      <td>${esc(e.customerName)}</td>
      <td>${crew}</td>
      <td class="text-end">${action}</td>
    </tr>`;
  }).join('');
}

function renderRoster() {
  // keep the filter options in sync
  const sel = $('rosterFilter');
  const current = sel.value;
  const names = [...new Set(state.roster.map((r) => r.photographerName))].sort();
  sel.innerHTML = '<option value="">All Photographers</option>' +
    names.map((n) => `<option value="${esc(n)}">${esc(n)}</option>`).join('');
  sel.value = names.includes(current) ? current : '';

  const rows = state.roster.filter((r) => !sel.value || r.photographerName === sel.value);
  const body = $('rosterBody');
  if (!rows.length) {
    body.innerHTML = `<tr><td colspan="7" class="empty-state"><i class="bi bi-calendar2-x"></i>No active assignments.</td></tr>`;
    return;
  }
  const t = today();
  body.innerHTML = rows.map((r) => {
    const d = toDate(r.eventDate);
    let badge = '<span class="badge badge-soft-success">Confirmed</span>';
    if (d < t) badge = '<span class="badge badge-soft-muted">Completed</span>';
    else if (sameDay(d, t)) badge = '<span class="badge badge-soft-primary">On Duty Today</span>';
    const options = d < t ? '' : crewButtons(r.assignmentId, d);
    return `<tr>
      <td>
        <div class="fw-semibold">${esc(r.photographerName)}</div>
        <small class="text-muted">Assigned by ${esc(r.assignedByName)}</small>
      </td>
      <td>${esc(r.eventTitle)}</td>
      <td>${esc(when(r))}</td>
      <td>${esc(r.eventLocation)}</td>
      <td>${hoursOf(r).toFixed(1)} hrs</td>
      <td>${badge}</td>
      <td class="text-end">${options}</td>
    </tr>`;
  }).join('');
}

const AVATAR_COLORS = ['bg-primary', 'bg-success', 'bg-info', 'bg-secondary', 'bg-warning'];
function renderRegistry() {
  const grid = $('registryGrid');
  if (!state.photographers.length) {
    grid.innerHTML = '<div class="col-12"><div class="empty-state"><i class="bi bi-people"></i>No photographers found.</div></div>';
    return;
  }
  const t = today();
  grid.innerHTML = state.photographers.map((p, i) => {
    const hrs = weeklyHours(p.id);
    const pct = Math.min(100, Math.round((hrs / WEEK_LIMIT_HOURS) * 100));
    const barColor = pct < 60 ? 'bg-success' : pct < 85 ? 'bg-primary' : 'bg-danger';
    const onDuty = state.roster.some((r) => r.photographerId === p.id && sameDay(toDate(r.eventDate), t));
    let badge;
    if (!p.active) badge = '<span class="badge badge-soft-muted">Inactive Account</span>';
    else if (onDuty) badge = '<span class="badge badge-soft-primary">On Duty Today</span>';
    else if (pct >= 100) badge = '<span class="badge badge-soft-danger">Weekly Limit Reached</span>';
    else badge = '<span class="badge badge-soft-success">Available for Assignment</span>';
    const sub = [p.exp != null ? `${p.exp} yrs experience` : 'Photographer',
      `${p.upcoming || 0} upcoming shift${p.upcoming === 1 ? '' : 's'}`].join(' | ');
    return `<div class="col-12 col-md-6 col-xl-4">
      <div class="card border-0 shadow-sm p-3 h-100 d-flex flex-column">
        <div class="d-flex align-items-center mb-3">
          <div class="rounded-circle ${AVATAR_COLORS[i % AVATAR_COLORS.length]} text-white d-flex align-items-center justify-content-center me-3" style="width: 44px; height: 44px; font-weight: 600;">${esc(initialsOf(p.name))}</div>
          <div>
            <h6 class="fw-bold mb-0">${esc(p.name)}</h6>
            <small class="text-muted">${esc(sub)}</small>
          </div>
        </div>
        ${p.bio ? `<p class="small text-muted bio-clamp mb-3">${esc(p.bio)}</p>` : ''}
        <div class="small mb-2">
          <div class="d-flex justify-content-between mb-1">
            <span class="text-muted">This Week's Workload:</span>
            <span class="fw-semibold">${hrs.toFixed(1)} / ${WEEK_LIMIT_HOURS} hrs</span>
          </div>
          <div class="progress" style="height: 6px;">
            <div class="progress-bar ${barColor}" style="width: ${pct}%;"></div>
          </div>
        </div>
        <div class="small text-muted mb-2">
          <div class="text-truncate"><i class="bi bi-envelope me-1"></i>${esc(p.email || '')}</div>
          ${p.mobile ? `<div><i class="bi bi-telephone me-1"></i>${esc(p.mobile)}</div>` : ''}
        </div>
        <div class="d-flex justify-content-between align-items-center border-top pt-2 mt-auto">
          ${badge}
          <button class="btn btn-sm btn-outline-primary" onclick="openSchedule(${p.id})">
            <i class="bi bi-calendar-week me-1"></i>View Schedule
          </button>
        </div>
      </div>
    </div>`;
  }).join('');
}

/* ---------- PHOTOGRAPHER SCHEDULE ---------- */
async function openSchedule(photographerId) {
  const p = state.photographers.find((x) => x.id === photographerId);
  if (!p) return;
  ctx = { photographerId };
  $('scheduleName').textContent = p.name;
  $('scheduleMeta').textContent = `This week: ${weeklyHours(p.id).toFixed(1)} / ${WEEK_LIMIT_HOURS} hrs`
    + (p.active ? '' : ' | Inactive account');
  $('scheduleBody').innerHTML = '<tr><td colspan="6" class="text-muted">Loading schedule...</td></tr>';
  showModal('scheduleModal');

  const r = await api(`/photographers/${photographerId}/schedule`);
  if (!r.ok) {
    $('scheduleBody').innerHTML = `<tr><td colspan="6" class="text-danger">${esc(r.message)}</td></tr>`;
    return;
  }
  const rows = r.data || [];
  if (!rows.length) {
    $('scheduleBody').innerHTML = '<tr><td colspan="6" class="empty-state"><i class="bi bi-calendar2-x"></i>No shifts assigned.</td></tr>';
    return;
  }
  const t = today();
  $('scheduleBody').innerHTML = rows.map((a) => {
    const d = toDate(a.eventDate);
    let badge = '<span class="badge badge-soft-success">Upcoming</span>';
    if (d < t) badge = '<span class="badge badge-soft-muted">Completed</span>';
    else if (sameDay(d, t)) badge = '<span class="badge badge-soft-primary">Today</span>';
    const options = d < t ? '' : crewButtons(a.assignmentId, d);
    return `<tr>
      <td><div class="fw-semibold">${esc(a.eventTitle)}</div><small class="text-muted">${evtId(a.bookingId)}</small></td>
      <td>${esc(when(a))}</td>
      <td>${esc(a.eventLocation)}</td>
      <td>${hoursOf(a).toFixed(1)}</td>
      <td>${badge}</td>
      <td class="text-end text-nowrap">${options}</td>
    </tr>`;
  }).join('');
}

/* ---------- ASSIGN ---------- */
async function openAssign(bookingId) {
  const ev = state.events.find((e) => e.bookingId === bookingId);
  if (!ev) return;
  ctx = { bookingId };
  $('assignEventTitle').textContent = `${ev.eventTitle} (${evtId(ev.bookingId)})`;
  $('assignEventMeta').innerHTML =
    `<i class="bi bi-calendar3 me-1"></i> ${esc(fmtDate(ev.eventDate))} &nbsp;|&nbsp; ` +
    `<i class="bi bi-clock me-1"></i> ${esc(toTime(ev.startTime))} - ${esc(toTime(ev.endTime))} (${hoursOf(ev).toFixed(1)} Hours) &nbsp;|&nbsp; ` +
    `<i class="bi bi-geo-alt me-1"></i> ${esc(ev.eventLocation)}`;
  $('assignList').innerHTML = '<div class="list-group-item text-muted">Checking availability...</div>';
  hideInline('assignAlert');
  $('assignConfirmBtn').disabled = true;
  showModal('assignPhotographerModal');

  const r = await api(`/events/${bookingId}/photographers`);
  if (!r.ok) { $('assignList').innerHTML = ''; showInline('assignAlert', r.message); return; }

  const already = new Set(ev.assignedPhotographers);
  const items = r.data.map((p) => {
    const isAssigned = already.has(p.name);
    const free = p.available && !isAssigned;
    let badge, note;
    if (isAssigned) { badge = '<span class="badge badge-soft-primary">Already Assigned</span>'; note = 'Already on this event'; }
    else if (p.available) { badge = '<span class="badge badge-soft-success">Free on Schedule</span>'; note = ''; }
    else { badge = '<span class="badge badge-soft-danger">Unavailable</span>'; note = 'Schedule conflict: ' + p.conflicts.join('; '); }
    const meta = [p.experienceYears != null ? `${p.experienceYears} yrs experience` : null,
      `Workload this week: ${weeklyHours(p.photographerId).toFixed(1)} hrs`, note].filter(Boolean).join(' | ');
    return `<label class="list-group-item d-flex justify-content-between align-items-center gap-2 ${free ? '' : 'bg-light text-muted'}">
      <div class="d-flex align-items-center">
        <input class="form-check-input me-3" type="radio" name="selectedPhotographer" value="${p.photographerId}" ${free ? '' : 'disabled'}>
        <div>
          <strong class="d-block ${free ? '' : 'text-secondary'}">${esc(p.name)}</strong>
          <small class="text-muted">${esc(meta)}</small>
        </div>
      </div>
      ${badge}
    </label>`;
  });
  $('assignList').innerHTML = items.join('') || '<div class="list-group-item text-muted">No photographers found.</div>';

  const firstFree = document.querySelector('#assignList input[type=radio]:not(:disabled)');
  if (firstFree) { firstFree.checked = true; $('assignConfirmBtn').disabled = false; }
  else showInline('assignAlert', 'No photographer is free for this schedule.');
}

async function submitAssign() {
  const sel = document.querySelector('input[name=selectedPhotographer]:checked');
  if (!sel) return;
  const btn = $('assignConfirmBtn');
  hideInline('assignAlert');
  setBusy(btn, true, 'Assigning...');
  const r = await api('', { method: 'POST', body: JSON.stringify({ bookingId: ctx.bookingId, photographerId: Number(sel.value) }) });
  setBusy(btn, false);
  if (r.ok) { hideModal('assignPhotographerModal'); toast(r.message); await loadAll(); }
  else showInline('assignAlert', r.message);
}

/* ---------- DETAILS ---------- */
async function openDetails(bookingId) {
  const ev = state.events.find((e) => e.bookingId === bookingId);
  if (!ev) return;
  ctx = { bookingId };
  $('viewEventTitle').textContent = `${ev.eventTitle} (${evtId(ev.bookingId)})`;
  $('viewEventWhen').textContent = when(ev);
  $('viewAddBtn').onclick = () => openAssign(bookingId);
  $('viewCrewList').innerHTML = '<div class="list-group-item text-muted">Loading...</div>';
  showModal('viewAssignmentModal');

  const r = await api(`/events/${bookingId}`);
  if (!r.ok) { $('viewCrewList').innerHTML = `<div class="list-group-item text-danger">${esc(r.message)}</div>`; return; }
  $('viewCrewList').innerHTML = (r.data || []).map((a) => `
    <div class="list-group-item d-flex justify-content-between align-items-center">
      <div>
        <strong class="d-block">${esc(a.photographerName)}</strong>
        <small class="text-muted">Assigned by ${esc(a.assignedByName)}${a.reassignmentReason ? ' | ' + esc(a.reassignmentReason) : ''}</small>
      </div>
      <div class="text-nowrap">${crewButtons(a.assignmentId, toDate(ev.eventDate))}</div>
    </div>`).join('') || '<div class="list-group-item text-muted">No photographers assigned.</div>';
}

/* ---------- REASSIGN ---------- */
async function openReassign(assignmentId) {
  const a = state.roster.find((x) => x.assignmentId === assignmentId);
  if (!a) return;
  if (sameDay(toDate(a.eventDate), today())) {
    toast('This event is today. Crew can no longer be reassigned.', 'warning');
    return;
  }
  const ev = state.events.find((e) => e.bookingId === a.bookingId);
  ctx = { assignmentId, bookingId: a.bookingId, photographerId: a.photographerId };
  $('reassignInfo').innerHTML = `Currently assigned: <strong>${esc(a.photographerName)}</strong> for <strong>${esc(a.eventTitle)}</strong> (${esc(when(a))}).`;
  $('reassignSelect').innerHTML = '<option>Checking availability...</option>';
  $('reassignReason').value = '';
  hideInline('reassignAlert');
  $('reassignConfirmBtn').disabled = true;
  showModal('reassignPhotographerModal');

  const r = await api(`/events/${a.bookingId}/photographers`);
  if (!r.ok) { $('reassignSelect').innerHTML = ''; showInline('reassignAlert', r.message); return; }
  const already = new Set(ev ? ev.assignedPhotographers : [a.photographerName]);
  const options = r.data
    .filter((p) => p.photographerId !== a.photographerId)
    .map((p) => {
      const taken = already.has(p.name);
      const ok = p.available && !taken && !p.previouslyAssigned;
      let reason;
      if (taken) reason = 'Already on this event';
      else if (p.previouslyAssigned) reason = 'Previously replaced on this event';
      else reason = 'Conflict: ' + p.conflicts.join('; ');
      const label = ok
        ? `${p.name} (Available - ${weeklyHours(p.photographerId).toFixed(1)} hrs this week)`
        : `${p.name} (${reason})`;
      return `<option value="${p.photographerId}" ${ok ? '' : 'disabled'}>${esc(label)}</option>`;
    });
  $('reassignSelect').innerHTML = options.join('');
  const firstFree = [...$('reassignSelect').options].find((o) => !o.disabled);
  if (firstFree) { $('reassignSelect').value = firstFree.value; $('reassignConfirmBtn').disabled = false; }
  else showInline('reassignAlert', 'No replacement photographer is free for this schedule.');
}

async function submitReassign() {
  const reason = $('reassignReason').value.trim();
  if (!reason) { showInline('reassignAlert', 'Please enter a reason for the reallocation.'); return; }
  const btn = $('reassignConfirmBtn');
  hideInline('reassignAlert');
  setBusy(btn, true, 'Reassigning...');
  const r = await api(`/${ctx.assignmentId}/reassign`, {
    method: 'PUT',
    body: JSON.stringify({ newPhotographerId: Number($('reassignSelect').value), reason })
  });
  setBusy(btn, false);
  if (r.ok) { hideModal('reassignPhotographerModal'); toast(r.message); await loadAll(); }
  else showInline('reassignAlert', r.message);
}

/* ---------- REMOVE ---------- */
function openRemove(assignmentId) {
  const a = state.roster.find((x) => x.assignmentId === assignmentId);
  if (!a) return;
  if (sameDay(toDate(a.eventDate), today())) {
    toast('This event is today. Crew can no longer be removed.', 'warning');
    return;
  }
  ctx = { assignmentId };
  $('removeInfo').innerHTML = `Remove <strong>${esc(a.photographerName)}</strong> from <strong>${esc(a.eventTitle)}</strong> (${esc(when(a))})?`;
  $('removeReason').value = '';
  hideInline('removeAlert');
  showModal('removeAssignmentModal');
}

async function submitRemove() {
  const reason = $('removeReason').value.trim();
  if (!reason) { showInline('removeAlert', 'Please enter a reason.'); return; }
  const btn = $('removeConfirmBtn');
  hideInline('removeAlert');
  setBusy(btn, true, 'Removing...');
  const r = await api(`/${ctx.assignmentId}?reason=${encodeURIComponent(reason)}`, { method: 'DELETE' });
  setBusy(btn, false);
  if (r.ok) { hideModal('removeAssignmentModal'); toast(r.message); await loadAll(); }
  else showInline('removeAlert', r.message);
}

/* ---------- wiring ---------- */
document.addEventListener('DOMContentLoaded', async () => {
  // Keep sidebar + mobile menu highlight in sync (same pane has two links)
  document.querySelectorAll('[data-bs-toggle="tab"]').forEach((a) =>
    a.addEventListener('shown.bs.tab', (e) => {
      const h = e.target.getAttribute('href');
      document.querySelectorAll('[data-bs-toggle="tab"]').forEach((x) =>
        x.classList.toggle('active', x.getAttribute('href') === h));
    }));

  $('rosterFilter').addEventListener('change', renderRoster);
  $('assignList').addEventListener('change', () => { $('assignConfirmBtn').disabled = false; });

  if (await checkSession()) loadAll();
});