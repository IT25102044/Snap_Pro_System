const API_INVENTORY = '/api/equipment/inventory';
const API_ALLOCATIONS = '/api/equipment/allocations';
const API_DASHBOARD = '/api/equipment/dashboard';
const API_MAINTENANCE = '/api/equipment/maintenance';
let HANDLED_BY_ID = 1; // Logged in coordinator ID (dynamically synced from active session)

let currentCoordinator = null;
let gearList = [];
let gearFormModal = null;
let recordReturnModal = null;
let reportDamageModal = null;
let checkoutModal = null;

// ====================================================
// AUTHENTICATION & SESSION MANAGEMENT
// ====================================================
async function checkEquipmentSession() {
    try {
        const response = await fetch('/api/auth/session', {
            method: 'GET',
            credentials: 'include',
            cache: 'no-store'
        });

        const data = await response.json();

        if (response.ok && data.status && data.data) {
            const user = data.data;
            const normalizedRole = (user.role || '').toString().trim().toUpperCase().replace(/[\s-]+/g, '_');

            // Authorized roles for Equipment Dashboard
            const authorizedRoles = [
                'EQUIPMENT_MANAGER',
                'EQUIPMENT',
                'INVENTORY_MANAGER',
                'ADMIN',
                'SYSTEM_ADMIN',
                'ADMINISTRATOR'
            ];

            if (!authorizedRoles.includes(normalizedRole)) {
                // If user has another authenticated role, redirect to appropriate portal using auth.js
                if (typeof getDashboardForRole === 'function') {
                    window.location.href = getDashboardForRole(normalizedRole);
                } else {
                    window.location.href = 'login.html';
                }
                return false;
            }

            currentCoordinator = user;
            if (user.id) {
                HANDLED_BY_ID = user.id;
            }

            populateCoordinatorProfile(user);
            return true;
        } else {
            sessionStorage.setItem('postLoginRedirect', 'equipment-dashboard.html');
            window.location.href = 'login.html';
            return false;
        }
    } catch (error) {
        console.error('Session verification error:', error);
        sessionStorage.setItem('postLoginRedirect', 'equipment-dashboard.html');
        window.location.href = 'login.html';
        return false;
    }
}

function populateCoordinatorProfile(user) {
    if (!user) return;
    const firstName = user.firstName || '';
    const lastName = user.lastName || '';
    const fullName = `${firstName} ${lastName}`.trim() || 'Equipment Coordinator';
    const firstLetter = firstName ? firstName.charAt(0).toUpperCase() : 'E';
    const lastLetter = lastName ? lastName.charAt(0).toUpperCase() : 'C';
    const initials = `${firstLetter}${lastLetter}`;
    const email = user.email || 'coordinator@snappro.com';

    // Desktop sidebar
    const nameEl = document.getElementById('coordinatorName');
    if (nameEl) nameEl.textContent = fullName;

    const emailEl = document.getElementById('coordinatorEmail');
    if (emailEl) emailEl.textContent = email;

    const initialsEl = document.getElementById('coordinatorInitials');
    if (initialsEl) initialsEl.textContent = initials;

    // Mobile offcanvas menu
    const mobNameEl = document.getElementById('mobileCoordinatorName');
    if (mobNameEl) mobNameEl.textContent = fullName;

    const mobEmailEl = document.getElementById('mobileCoordinatorEmail');
    if (mobEmailEl) mobEmailEl.textContent = email;

    const mobInitialsEl = document.getElementById('mobileCoordinatorInitials');
    if (mobInitialsEl) mobInitialsEl.textContent = initials;
}

// ====================================================
// LOGOUT / SIGN OUT
// ====================================================
async function logout() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
    } catch (err) {
        console.error('Logout error:', err);
    } finally {
        sessionStorage.removeItem('postLoginRedirect');
        window.location.href = 'login.html';
    }
}

// Support both logout() and signOut() for cross-module compatibility
const signOut = logout;
window.logout = logout;
window.signOut = logout;

document.addEventListener("DOMContentLoaded", async () => {
    // 1. Enforce login / session verification before loading dashboard data
    const isAuthenticated = await checkEquipmentSession();
    if (!isAuthenticated) {
        return;
    }

    // 2. Initialize Bootstrap modals
    gearFormModal = new bootstrap.Modal(document.getElementById('gearFormModal'));
    recordReturnModal = new bootstrap.Modal(document.getElementById('recordReturnModal'));
    reportDamageModal = new bootstrap.Modal(document.getElementById('reportDamageModal'));
    checkoutModal = new bootstrap.Modal(document.getElementById('checkoutModal'));

    // 3. Load dashboard datasets
    fetchDashboardSummary();
    fetchActiveAllocations();
    fetchGear();
    fetchReturnsLog();
    fetchMaintenanceLog();
});

// ====================================================
// 1. DASHBOARD OVERVIEW METRICS (UPDATED FIX)
// ====================================================
function fetchDashboardSummary() {
    fetch(API_DASHBOARD)
        .then(res => {
            if (!res.ok) throw new Error('Dashboard fetch failed');
            return res.json();
        })
        .then(data => {
            console.log("Dashboard Summary API Response:", data);

            // flexible property name checking
            let available = data.availableItems ?? data.availableCount ?? data.available;
            let checkedOut = data.checkedOutItems ?? data.checkedOutCount ?? data.checkedOut;
            let expectedReturns = data.expectedReturnsToday ?? data.expectedReturnsCount ?? data.expectedReturns;
            let maintenance = data.maintenanceItems ?? data.maintenanceCount ?? data.maintenance;

            if (available !== undefined && document.getElementById('metricAvailable')) {
                document.getElementById('metricAvailable').innerText = (available ?? 0) + " Items";
            }
            if (checkedOut !== undefined && document.getElementById('metricCheckedOut')) {
                document.getElementById('metricCheckedOut').innerText = (checkedOut ?? 0) + " Items";
            }
            if (expectedReturns !== undefined && document.getElementById('metricExpectedReturns')) {
                document.getElementById('metricExpectedReturns').innerText = (expectedReturns ?? 0) + " Items";
            }
            if (maintenance !== undefined && document.getElementById('metricMaintenance')) {
                document.getElementById('metricMaintenance').innerText = (maintenance ?? 0) + " Items";
            }
        })
        .catch(err => {
            console.error('Dashboard summary error:', err);
            updateMetricsFromInventory();
        });
}

// Gear Inventory එකෙන් Live Cards Update කරන Utility Function එක
function updateMetricsFromInventory() {
    if (!gearList || gearList.length === 0) return;

    const available = gearList.filter(g => (g.statusId === 1 || (g.status || '').toLowerCase() === 'available') && !g.decommissioned).length;
    const checkedOut = gearList.filter(g => (g.statusId === 2 || (g.status || '').toLowerCase() === 'checked out') && !g.decommissioned).length;
    const maintenance = gearList.filter(g => (g.statusId === 3 || (g.status || '').toLowerCase().includes('mainten') || (g.status || '').toLowerCase().includes('repair') || (g.status || '').toLowerCase().includes('damag')) && !g.decommissioned).length;

    const metricAvail = document.getElementById('metricAvailable');
    const metricChecked = document.getElementById('metricCheckedOut');
    const metricMaint = document.getElementById('metricMaintenance');

    if (metricAvail) metricAvail.innerText = available + " Items";
    if (metricChecked) metricChecked.innerText = checkedOut + " Items";
    if (metricMaint) metricMaint.innerText = maintenance + " Items";
}
// ====================================================
// 2. ACTIVE ALLOCATIONS
// ====================================================
function fetchActiveAllocations() {
    fetch(`${API_ALLOCATIONS}/active`)
        .then(res => {
            if (!res.ok) throw new Error('Active allocations fetch failed');
            return res.json();
        })
        .then(data => {
            const tbody = document.getElementById('activeAllocationsTableBody');
            tbody.innerHTML = '';
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-3">No active allocations.</td></tr>';
                return;
            }
            data.forEach(item => {
                const allocId = item.allocationId ?? item.id;
                const equipId = item.equipmentId ?? item.id;
                const serial = item.serialNumber || '';
                const itemName = (item.itemName || '').replace(/'/g, "\\'");
                const serialEscaped = serial.replace(/'/g, "\\'");
                const statusBadge = '<span class="badge badge-soft-primary">Checked Out</span>';

                const eventRef = `${item.eventName ? item.eventName : 'Event'} (ID: #${item.bookingId || item.eventId || '-'})`;
                const photographerRef = `${item.photographerName ? item.photographerName : 'Photographer'} (ID: #${item.photographerId || '-'})`;

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="fw-semibold">${item.serialNumber || '-'}</td>
                    <td>${item.itemName || '-'}</td>
                    <td>${photographerRef}</td>
                    <td>${eventRef}</td>
                    <td>${item.checkoutTime ? new Date(item.checkoutTime).toLocaleString() : '-'}</td>
                    <td>${statusBadge}</td>
                    <td class="text-end">
                      <button class="btn btn-sm btn-outline-primary me-1" onclick="openReturnModal('${serialEscaped}')">
                        <i class="bi bi-box-arrow-in-down"></i> Return
                      </button>
                      <button class="btn btn-sm btn-outline-danger" onclick="openDamageModal(${equipId}, ${allocId}, '${itemName}', '${serialEscaped}')">
                        <i class="bi bi-exclamation-octagon"></i> Report Damage
                      </button>
                    </td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Active allocations error:', err));
}

// ====================================================
// 3. GEAR INVENTORY CRUD
// ====================================================
function fetchGear() {
    fetch(API_INVENTORY)
        .then(res => {
            if (!res.ok) throw new Error('Inventory fetch failed');
            return res.json();
        })
        .then(data => {
            gearList = data;
            renderTable(gearList);
            updateMetricsFromInventory(); // <-- Inventory Load වූ සැනින් Cards Live Update වේ
        })
        .catch(err => {
            console.error('Gear fetch error:', err);
            const tbody = document.getElementById('gearTableBody');
            if (tbody) tbody.innerHTML = '<tr><td colspan="6" class="text-center text-danger py-3">Failed to load inventory.</td></tr>';
        });
}

function renderTable(data) {
    const tbody = document.getElementById('gearTableBody');
    if (!tbody) return;
    tbody.innerHTML = '';

    if (data.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No equipment found.</td></tr>';
        return;
    }

    data.forEach(gear => {
        let statusBadge = '';
        const statusName = (gear.status || '').toLowerCase();
        if (statusName === 'available' || gear.statusId === 1) {
            statusBadge = '<span class="badge badge-soft-success">Available</span>';
        } else if (statusName === 'checked out' || gear.statusId === 2) {
            statusBadge = '<span class="badge badge-soft-primary">Checked Out</span>';
        } else if (statusName.includes('maintenance') || gear.statusId === 3) {
            statusBadge = '<span class="badge badge-soft-warning">In Maintenance</span>';
        } else {
            statusBadge = `<span class="badge badge-soft-danger">${gear.status || 'Unknown'}</span>`;
        }

        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td class="fw-semibold">${gear.serialNumber || '-'}</td>
            <td>${gear.itemName || '-'}</td>
            <td>${gear.model || '-'}</td>
            <td>${gear.category || '-'}</td>
            <td>${statusBadge}</td>
            <td class="text-end">
                <button class="btn btn-sm btn-outline-secondary me-1" onclick="editGear(${gear.id})"><i class="bi bi-pencil"></i></button>
                <button class="btn btn-sm btn-outline-danger" onclick="deleteGear(${gear.id})"><i class="bi bi-trash"></i></button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function searchGear() {
    const query = (document.getElementById('searchInput').value || '').toLowerCase();
    const filtered = gearList.filter(gear =>
        (gear.serialNumber && gear.serialNumber.toLowerCase().includes(query)) ||
        (gear.itemName && gear.itemName.toLowerCase().includes(query)) ||
        (gear.category && gear.category.toLowerCase().includes(query))
    );
    renderTable(filtered);
}

function openAddGearModal() {
    document.getElementById('gearId').value = '';
    document.getElementById('gearSerialNumber').value = '';
    document.getElementById('gearItemName').value = '';
    document.getElementById('gearModel').value = '';
    document.getElementById('gearCategory').value = 'General';
    document.getElementById('gearStatusId').value = '1';
    document.getElementById('gearNotes').value = '';
    document.getElementById('gearFormTitle').innerText = 'Add New Gear';
    gearFormModal.show();
}

function editGear(id) {
    const gear = gearList.find(g => g.id === id);
    if (!gear) { alert('Gear item not found.'); return; }

    document.getElementById('gearId').value = gear.id;
    document.getElementById('gearSerialNumber').value = gear.serialNumber || '';
    document.getElementById('gearItemName').value = gear.itemName || '';
    document.getElementById('gearModel').value = gear.model || '';

    // Automatically default empty or missing category values to 'General' to avoid UI validation alerts
    const rawCategory = (gear.category || '').trim();
    const resolvedCategory = (!rawCategory || rawCategory === '-' || rawCategory.toLowerCase() === 'undefined' || rawCategory.toLowerCase() === 'null')
        ? 'General'
        : rawCategory;
    document.getElementById('gearCategory').value = resolvedCategory;

    document.getElementById('gearStatusId').value = gear.statusId || 1;
    document.getElementById('gearNotes').value = gear.notes || '';
    document.getElementById('gearFormTitle').innerText = 'Edit Gear';
    gearFormModal.show();
}

function saveGear() {
    const id = document.getElementById('gearId').value.trim();
    const statusIdVal = parseInt(document.getElementById('gearStatusId').value);

    // Automatically default empty or missing category values to 'General' to avoid UI validation alerts
    let categoryInput = document.getElementById('gearCategory');
    let categoryVal = categoryInput ? categoryInput.value.trim() : '';
    if (!categoryVal || categoryVal === '-' || categoryVal.toLowerCase() === 'undefined' || categoryVal.toLowerCase() === 'null') {
        categoryVal = 'General';
    }
    if (categoryInput) {
        categoryInput.value = categoryVal;
    }

    const data = {
        serialNumber: document.getElementById('gearSerialNumber').value.trim(),
        itemName: document.getElementById('gearItemName').value.trim(),
        model: document.getElementById('gearModel').value.trim(),
        category: categoryVal,
        statusId: isNaN(statusIdVal) ? 1 : statusIdVal,
        notes: document.getElementById('gearNotes').value.trim()
    };

    if (!data.serialNumber || !data.itemName) {
        alert("Serial Number and Item Name are required.");
        return;
    }

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${API_INVENTORY}/${id}` : API_INVENTORY;

    fetch(url, {
        method: method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Save failed'); });
        }
        return response.json();
    })
    .then(() => {
        gearFormModal.hide();
        fetchGear();
        fetchDashboardSummary();
        alert(id ? "Gear updated successfully!" : "Gear added successfully!");
    })
    .catch(err => {
        console.error('Save gear error:', err);
        alert("Could not save equipment: " + err.message);
    });
}

function deleteGear(id) {
    if (!confirm("Are you sure you want to delete this gear?")) return;

    fetch(`${API_INVENTORY}/${id}`, { method: 'DELETE' })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Delete failed'); });
        }
        fetchGear();
        fetchDashboardSummary();
        alert("Gear deleted successfully.");
    })
    .catch(err => {
        console.error('Delete gear error:', err);
        alert("Could not delete equipment: " + err.message);
    });
}

// ====================================================
// 4. CHECKOUT GEAR
// ====================================================
function openCheckoutModal() {
    const select = document.getElementById('checkoutEquipmentId');
    select.innerHTML = '';
    const availableGear = gearList.filter(g => g.statusId === 1 || (g.status || '').toLowerCase() === 'available');
    if (availableGear.length === 0) {
        select.innerHTML = '<option disabled>No gear currently available</option>';
    } else {
        availableGear.forEach(g => {
            const opt = document.createElement('option');
            opt.value = g.id;
            opt.innerText = `${g.serialNumber} - ${g.itemName}`;
            select.appendChild(opt);
        });
    }

    const bookingSelect = document.getElementById('checkoutBookingId');
    if (bookingSelect && bookingSelect.tagName === 'SELECT' && bookingSelect.options.length === 0) {
        bookingSelect.innerHTML = `
            <option value="1" selected>Corporate Annual Gala - ID #1</option>
            <option value="2">Colombo Fashion Week - ID #2</option>
            <option value="3">Tech Leaders Summit - ID #3</option>
        `;
    }

    const photographerSelect = document.getElementById('checkoutPhotographerId');
    if (photographerSelect && photographerSelect.tagName === 'SELECT' && photographerSelect.options.length === 0) {
        photographerSelect.innerHTML = `
            <option value="1" selected>Ruwan Silva - ID #1</option>
            <option value="2">Kasun Perera - ID #2</option>
            <option value="3">Nimal Fernando - ID #3</option>
        `;
    }

    checkoutModal.show();
}

function submitCheckout() {
    const equipmentIdVal = parseInt(document.getElementById('checkoutEquipmentId').value);
    let bookingIdVal = parseInt(document.getElementById('checkoutBookingId').value);
    const photographerIdVal = parseInt(document.getElementById('checkoutPhotographerId').value);

    if (isNaN(bookingIdVal)) {
        const rawText = document.getElementById('checkoutBookingId').value || '';
        const match = rawText.match(/#?(\d+)/);
        if (match) {
            bookingIdVal = parseInt(match[1]);
        }
    }

    if (isNaN(equipmentIdVal) || isNaN(bookingIdVal) || isNaN(photographerIdVal)) {
        alert("Please fill all required fields with valid numbers.");
        return;
    }

    const data = {
        equipmentId: equipmentIdVal,
        bookingId: bookingIdVal,
        photographerId: photographerIdVal
    };

    fetch(`${API_ALLOCATIONS}/checkout?handledBy=${HANDLED_BY_ID}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Checkout failed'); });
        }
        return response.json();
    })
    .then(() => {
        checkoutModal.hide();
        refreshAllData();
        alert("Gear checked out successfully!");
    })
    .catch(err => {
        console.error('Checkout error:', err);
        alert("Checkout failed: " + err.message);
    });
}

// ====================================================
// 5. RETURN GEAR (UPDATED FIX)
// ====================================================
function openReturnModal(serial) {
    serial = serial || '';
    document.getElementById('returnSerialNumber').value = serial;
    document.getElementById('returnAllocationId').value = '';
    document.getElementById('returnNotes').value = '';
    document.getElementById('returnItemDetails').style.display = 'none';

    const normalRadio = document.getElementById('conditionNormal');
    if (normalRadio) normalRadio.checked = true;

    recordReturnModal.show();

    // Serial number එක පවති නම් Auto-Lookup කිරීම
    if (serial) {
        lookupReturnGear();
    }
}

function lookupReturnGear() {
    const serial = (document.getElementById('returnSerialNumber').value || '').trim();
    if (!serial) {
        alert("Enter a serial number to lookup.");
        return;
    }
    fetch(`${API_ALLOCATIONS}/active/serial/${encodeURIComponent(serial)}`)
        .then(res => {
            if (!res.ok) throw new Error("No active allocation found");
            return res.json();
        })
        .then(data => {
            // Support both allocationId and id safely
            const allocId = data.allocationId ?? data.id;
            if (!allocId) {
                throw new Error("Allocation ID missing from server response.");
            }
            document.getElementById('returnAllocationId').value = allocId;
            document.getElementById('returnItemName').innerText = `Item: ${data.itemName || data.serialNumber || 'Equipment'}`;
            document.getElementById('returnItemContext').innerText = `Checkout time: ${data.checkoutTime ? new Date(data.checkoutTime).toLocaleString() : 'N/A'}`;
            document.getElementById('returnItemDetails').style.display = 'block';
        })
        .catch(err => {
            console.error('Lookup error:', err);
            alert("No active allocation found for serial: " + serial);
            document.getElementById('returnItemDetails').style.display = 'none';
            document.getElementById('returnAllocationId').value = '';
        });
}

function submitReturn() {
    const allocationIdStr = document.getElementById('returnAllocationId').value.trim();
    const allocationId = parseInt(allocationIdStr);

    if (!allocationIdStr || isNaN(allocationId) || allocationIdStr === 'undefined' || allocationIdStr === 'null') {
        alert("Please use the Lookup button to find a valid active allocation first.");
        return;
    }

    const damageRadio = document.getElementById('conditionDamage');
    const isDamaged = damageRadio ? damageRadio.checked : false;
    const notes = document.getElementById('returnNotes').value.trim();

    if (isDamaged && !notes) {
        alert("Please add inspection remarks when marking item as damaged.");
        return;
    }

    const data = {
        allocationId: allocationId,
        id: allocationId,
        returnNotes: notes || null,
        reportedDamage: isDamaged,
        damageRemarks: isDamaged ? notes : null,
        damageDescription: isDamaged ? notes : null
    };

    fetch(`${API_ALLOCATIONS}/return?handledBy=${HANDLED_BY_ID}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Return failed'); });
        }
        return response.json();
    })
    .then(() => {
        recordReturnModal.hide();
        refreshAllData();
        alert("Gear returned successfully!");
    })
    .catch(err => {
        console.error('Return error:', err);
        alert("Return failed: " + err.message);
    });
}

// ====================================================
// 6. REPORT DAMAGE
// ====================================================
function openDamageModal(equipmentId, allocationId, itemName, serialNumber) {
    const validAllocId = (allocationId && allocationId !== 'undefined' && !isNaN(parseInt(allocationId))) ? parseInt(allocationId) : '';
    document.getElementById('damageEquipmentId').value = equipmentId || '';
    document.getElementById('damageAllocationId').value = validAllocId;
    document.getElementById('damageEquipmentName').value = `${itemName || ''} (${serialNumber || ''})`;
    document.getElementById('damageDescription').value = '';
    document.getElementById('damageActionRoute').value = 'REPAIR';
    reportDamageModal.show();
}

function submitDamage() {
    const equipmentIdVal = parseInt(document.getElementById('damageEquipmentId').value);
    const allocationIdVal = parseInt(document.getElementById('damageAllocationId').value);
    const description = document.getElementById('damageDescription').value.trim();
    const actionRoute = document.getElementById('damageActionRoute').value;

    if (!description) {
        alert("Please describe the damage.");
        return;
    }

    const data = {
        equipmentId: equipmentIdVal,
        allocationId: isNaN(allocationIdVal) ? null : allocationIdVal,
        id: isNaN(allocationIdVal) ? null : allocationIdVal,
        damageDescription: description,
        damageRemarks: description,
        actionRoute: actionRoute
    };

    fetch(`${API_ALLOCATIONS}/damage?handledBy=${HANDLED_BY_ID}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(data)
    })
    .then(response => {
        if (!response.ok) {
            return response.text().then(text => { throw new Error(text || 'Damage report failed'); });
        }
        return response.json();
    })
    .then(() => {
        reportDamageModal.hide();
        refreshAllData();
        alert("Damage reported successfully!");
    })
    .catch(err => {
        console.error('Damage report error:', err);
        alert("Damage report failed: " + err.message);
    });
}

// ====================================================
// 7. RETURNS LOG (UPDATED FIX)
// ====================================================
function fetchReturnsLog() {
    fetch(`${API_ALLOCATIONS}/returned`)
        .then(res => {
            if (!res.ok) throw new Error('Returns log fetch failed');
            return res.json();
        })
        .then(data => {
            const tbody = document.getElementById('returnsLogTableBody');
            tbody.innerHTML = '';
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No return history found.</td></tr>';
                return;
            }
            data.forEach(item => {
                const rawLogId = item.allocationId ?? item.id ?? item.logId;
                const logId = (rawLogId !== undefined && rawLogId !== null && rawLogId !== 'undefined') ? rawLogId : '-';
                const statusBadge = item.reportedDamage
                    ? '<span class="badge badge-soft-danger">Damaged on Return</span>'
                    : '<span class="badge badge-soft-success">Returned in Good Condition</span>';

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="fw-semibold">#LOG-${logId}</td>
                    <td>${item.itemName || '-'} (${item.serialNumber || '-'})</td>
                    <td>Photographer #${item.photographerId || '-'}</td>
                    <td>Coordinator #${item.handledBy || '-'}</td>
                    <td>${item.returnTime ? new Date(item.returnTime).toLocaleString() : '-'}</td>
                    <td>${statusBadge}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Returns log error:', err));
}

// ====================================================
// 8. MAINTENANCE & REPAIRS
// ====================================================
function fetchMaintenanceLog() {
    fetch(API_MAINTENANCE)
        .then(res => {
            if (!res.ok) throw new Error('Maintenance fetch failed');
            return res.json();
        })
        .then(data => {
            const tbody = document.getElementById('maintenanceTableBody');
            tbody.innerHTML = '';
            if (!data || data.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center text-muted py-3">No maintenance records found.</td></tr>';
                return;
            }
            data.forEach(item => {
                const recordId = item.maintenanceId ?? item.id;
                const statusBadge = (item.status || '').toLowerCase() === 'repaired'
                    ? '<span class="badge badge-soft-success">Repaired</span>'
                    : `<span class="badge badge-soft-danger">${item.status || 'In Maintenance'}</span>`;

                const actionBtn = (item.status || '').toLowerCase() !== 'repaired'
                    ? `<button class="btn btn-sm btn-outline-success" onclick="markRepaired(${recordId})">Mark Repaired</button>`
                    : '-';

                const tr = document.createElement('tr');
                tr.innerHTML = `
                    <td class="fw-semibold">#RMA-${recordId || '-'}</td>
                    <td>${item.serialNumber || '-'}</td>
                    <td>${item.itemName || 'Unknown'}</td>
                    <td>${item.issueDescription || '-'}</td>
                    <td>${statusBadge}</td>
                    <td class="text-end">${actionBtn}</td>
                `;
                tbody.appendChild(tr);
            });
        })
        .catch(err => console.error('Maintenance log error:', err));
}

function markRepaired(maintenanceId) {
    if (!maintenanceId) { alert("Invalid maintenance record."); return; }
    if (!confirm("Mark this item as repaired and set back to Available?")) return;

    fetch(`${API_MAINTENANCE}/${maintenanceId}/repair`, { method: 'PUT' })
        .then(response => {
            if (!response.ok) {
                return response.text().then(text => { throw new Error(text || 'Update failed'); });
            }
            return response.json();
        })
        .then(() => {
            refreshAllData();
            alert("Equipment marked as repaired!");
        })
        .catch(err => {
            console.error('Mark repaired error:', err);
            alert("Failed to update: " + err.message);
        });
}

// ====================================================
// UTILS
// ====================================================
function refreshAllData() {
    fetchDashboardSummary();
    fetchActiveAllocations();
    fetchGear();
    fetchReturnsLog();
    fetchMaintenanceLog();
}