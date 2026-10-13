/**
 * SnapPro - Client Relations Officer (CRO) Module
 * Handles CRO Feedback Management, Search, Modals, Session Verification, and Logout.
 * 
 * Location: js/CRO/customer-feedback.js
 * Independent from shared auth.js
 */

// =====================================================
// 1. LOGOUT & SESSION TERMINATION
// =====================================================

async function logout(confirmFirst = false) {
    if (confirmFirst) {
        const confirmed = confirm("Are you sure you want to log out of SnapPro?");
        if (!confirmed) {
            return;
        }
    }

    try {
        const response = await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include',
            headers: {
                'Content-Type': 'application/json'
            }
        });

        if (response.ok) {
            console.log("Session successfully invalidated on server.");
        } else {
            console.warn(`Logout request responded with status: ${response.status}`);
        }
    } catch (error) {
        console.error("Network error during logout, proceeding with client cleanup:", error);
    } finally {
        sessionStorage.clear();
        localStorage.removeItem('currentUser');
        localStorage.removeItem('postLoginRedirect');
        window.location.href = 'login.html';
    }
}

// Backward-compatibility alias
window.signOut = logout;

// Auto-bind click listener to elements with data-action="logout" or class "btn-logout"
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('[data-action="logout"], .btn-logout').forEach(button => {
        button.addEventListener('click', (e) => {
            e.preventDefault();
            logout();
        });
    });
});


// =====================================================
// 2. CRO FEEDBACK MANAGEMENT CONTROLLER
// =====================================================

const API_BASE = "/api/cro/feedback";

// Global in-memory storage
let allFeedbacks = [];
let selectedFeedbackId = null;
let feedbackToDeleteId = null;

// Fallback sample data in case MySQL/Backend is offline during presentation
const fallbackFeedbacks = [
    {
        id: 1,
        customerId: 11,
        bookingId: 1,
        rating: 5,
        reviewText: "Excellent photography service. Very professional team.",
        feedbackStatusId: 3,
        croResponse: "Thank you for your feedback",
        escalatedToManager: true,
        escalationRemarks: "Schedule reviewed with operations team.",
        createdAt: new Date().toISOString()
    },
    {
        id: 2,
        customerId: 12,
        bookingId: 2,
        rating: 4,
        reviewText: "Very good service and friendly photographer.",
        feedbackStatusId: 3,
        croResponse: "Thank you for your valuable feedback.",
        escalatedToManager: false,
        escalationRemarks: "",
        createdAt: new Date(Date.now() - 7200000).toISOString()
    },
    {
        id: 3,
        customerId: 13,
        bookingId: 3,
        rating: 5,
        reviewText: "Professional service for our corporate event.",
        feedbackStatusId: 5,
        croResponse: "Thank you for choosing SnapPro.",
        escalatedToManager: false,
        escalationRemarks: "",
        createdAt: new Date(Date.now() - 86400000).toISOString()
    },
    {
        id: 4,
        customerId: 14,
        bookingId: 4,
        rating: 4,
        reviewText: "Good photographs but delivery took longer than expected.",
        feedbackStatusId: 4,
        croResponse: "We apologize for the delay.",
        escalatedToManager: true,
        escalationRemarks: "Delivery delay reported to manager.",
        createdAt: new Date(Date.now() - 172800000).toISOString()
    },
    {
        id: 5,
        customerId: 15,
        bookingId: 5,
        rating: 4,
        reviewText: "Good quality photographs and service.",
        feedbackStatusId: 2,
        croResponse: "Thank you. Your feedback has been reviewed.",
        escalatedToManager: false,
        escalationRemarks: "",
        createdAt: new Date(Date.now() - 259200000).toISOString()
    }
];

// Helper mappings for real database IDs
function getCustomerLabel(customerId) {
    const customerMap = {
        11: "Kavindu Perera",
        12: "Sachini Silva",
        13: "Malith Fernando",
        14: "Dinithi Kumari",
        15: "Akila Bandara",
        101: "Kasun Jayawardena",
        102: "Dharshani Perera",
        103: "Sarah Jenkins"
    };
    return customerMap[customerId] || `Customer #${customerId}`;
}

function getBookingLabel(bookingId) {
    const eventMap = {
        1: "Kavindu Wedding (Colombo)",
        2: "Birthday Celebration (Kandy)",
        3: "ABC Company Annual Event (Colombo)",
        4: "Graduation Celebration (Malabe)",
        5: "Family Celebration (Galle)",
        8821: "Corporate Gala 2026 (#EVT-8821)",
        8920: "Milestone Birthday Shoot (#EVT-8920)",
        8850: "Executive Headshot Session (#EVT-8850)"
    };
    return eventMap[bookingId] || `Booking #EVT-${bookingId}`;
}

function getRatingStarsHtml(rating) {
    const r = Math.max(1, Math.min(5, Number(rating) || 5));
    let stars = "";
    for (let i = 1; i <= 5; i++) {
        if (i <= r) {
            stars += '<i class="bi bi-star-fill text-warning"></i> ';
        } else {
            stars += '<i class="bi bi-star text-muted"></i> ';
        }
    }
    return `${stars} <span class="text-muted small">(${r}/5)</span>`;
}

function getStatusBadge(statusId, isEscalated) {
    if (isEscalated || Number(statusId) === 4) {
        return '<span class="badge badge-soft-danger"><i class="bi bi-exclamation-triangle me-1"></i>Escalated to OM</span>';
    }
    switch (Number(statusId)) {
        case 1:
            return '<span class="badge badge-soft-warning"><i class="bi bi-clock me-1"></i>New Review</span>';
        case 2:
            return '<span class="badge badge-soft-info"><i class="bi bi-eye me-1"></i>Reviewed</span>';
        case 3:
            return '<span class="badge badge-soft-success"><i class="bi bi-check2 me-1"></i>Resolved</span>';
        case 4:
            return '<span class="badge badge-soft-danger"><i class="bi bi-shield-exclamation me-1"></i>Escalated</span>';
        case 5:
            return '<span class="badge bg-secondary"><i class="bi bi-check2-all me-1"></i>Closed</span>';
        default:
            return '<span class="badge bg-secondary">Status #' + statusId + '</span>';
    }
}

function formatTimeAgo(dateString) {
    if (!dateString) return "Recently";
    try {
        const diffMs = Date.now() - new Date(dateString).getTime();
        const diffMins = Math.floor(diffMs / 60000);
        if (diffMins < 1) return "Just now";
        if (diffMins < 60) return `${diffMins}m ago`;
        const diffHours = Math.floor(diffMins / 60);
        if (diffHours < 24) return `${diffHours}h ago`;
        const diffDays = Math.floor(diffHours / 24);
        return `${diffDays}d ago`;
    } catch {
        return "Recently";
    }
}

// Toast Notification
function showToast(title, message, isSuccess = true) {
    const toast = document.createElement("div");
    toast.className = "position-fixed bottom-0 end-0 p-3";
    toast.style.zIndex = "1090";
    toast.innerHTML = `
      <div class="toast show border-0 shadow" role="alert">
        <div class="toast-header ${isSuccess ? 'bg-success text-white' : 'bg-danger text-white'}">
          <i class="bi ${isSuccess ? 'bi-check-circle-fill' : 'bi-exclamation-circle-fill'} me-2"></i>
          <strong class="me-auto">${title}</strong>
          <button type="button" class="btn-close btn-close-white" onclick="this.closest('.position-fixed').remove()"></button>
        </div>
        <div class="toast-body">
          ${message}
        </div>
      </div>
    `;
    document.body.appendChild(toast);
    setTimeout(() => toast.remove(), 4000);
}

/* =====================================================
   3. LOAD & RENDER FEEDBACK DATA
   ===================================================== */
async function loadFeedbackData() {
    // If opened directly from file system (file:///), use fallback/local data immediately
    if (window.location.protocol === "file:") {
        const saved = localStorage.getItem("snappro_local_feedback");
        allFeedbacks = saved ? JSON.parse(saved) : fallbackFeedbacks;
        allFeedbacks = allFeedbacks.filter(f => !f.isDeleted);
        updateMetrics();
        renderQueueTable(allFeedbacks);
        renderCardsDirectory(allFeedbacks);
        renderEscalatedTable(allFeedbacks);
        return;
    }

    try {
        const response = await fetch(API_BASE);
        if (!response.ok) {
            throw new Error(`Server returned HTTP ${response.status}`);
        }
        const data = await response.json();
        allFeedbacks = Array.isArray(data) ? data : [];
        if (allFeedbacks.length === 0) {
            allFeedbacks = fallbackFeedbacks;
        }
    } catch (err) {
        console.warn("API offline or failed, falling back to local dataset:", err.message);
        const saved = localStorage.getItem("snappro_local_feedback");
        allFeedbacks = saved ? JSON.parse(saved) : fallbackFeedbacks;
    }

    // Filter out soft-deleted items
    allFeedbacks = allFeedbacks.filter(f => !f.isDeleted);

    updateMetrics();
    renderQueueTable(allFeedbacks);
    renderCardsDirectory(allFeedbacks);
    renderEscalatedTable(allFeedbacks);
}

function updateMetrics() {
    const pending = allFeedbacks.filter(f => Number(f.feedbackStatusId) === 1).length;
    const resolved = allFeedbacks.filter(f => [2, 3, 5].includes(Number(f.feedbackStatusId))).length;
    const escalated = allFeedbacks.filter(f => f.escalatedToManager || Number(f.feedbackStatusId) === 4).length;

    let avgRating = 0;
    if (allFeedbacks.length > 0) {
        const sum = allFeedbacks.reduce((acc, f) => acc + (Number(f.rating) || 0), 0);
        avgRating = (sum / allFeedbacks.length).toFixed(1);
    }

    const pendingEl = document.getElementById("statPendingCount");
    const resolvedEl = document.getElementById("statResolvedCount");
    const escalatedEl = document.getElementById("statEscalatedCount");
    const avgEl = document.getElementById("statAvgRating");
    const badgeEl = document.getElementById("totalQueueBadge");

    if (pendingEl) pendingEl.textContent = pending;
    if (resolvedEl) resolvedEl.textContent = resolved;
    if (escalatedEl) escalatedEl.textContent = escalated;
    if (avgEl) avgEl.textContent = `${avgRating} / 5.0`;
    if (badgeEl) badgeEl.textContent = `${allFeedbacks.length} Total`;

    // Attention Banner for Escalations
    const banner = document.getElementById("escalationAlertBanner");
    const bannerText = document.getElementById("escalationAlertText");
    const latestEscalated = allFeedbacks.find(f => f.escalatedToManager || Number(f.feedbackStatusId) === 4);
    if (banner && bannerText) {
        if (latestEscalated) {
            banner.style.display = "flex";
            bannerText.textContent = `Booking #EVT-${latestEscalated.bookingId} has been escalated for corrective crew action.`;
        } else {
            banner.style.display = "none";
        }
    }
}

function renderQueueTable(dataList) {
    const tbody = document.getElementById("feedbackTableBody");
    if (!tbody) return;

    if (!dataList || dataList.length === 0) {
        tbody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-4 text-muted">
            <i class="bi bi-inbox fs-3 d-block mb-2 text-secondary"></i>
            No feedback found in the queue.
          </td>
        </tr>`;
        return;
    }

    tbody.innerHTML = dataList.map(f => {
        const custName = getCustomerLabel(f.customerId);
        const evtTitle = getBookingLabel(f.bookingId);
        const ratingHtml = getRatingStarsHtml(f.rating);
        const statusBadge = getStatusBadge(f.feedbackStatusId, f.escalatedToManager);
        const timeAgo = formatTimeAgo(f.createdAt);

        return `
        <tr id="row-fb-${f.id}">
          <td class="fw-semibold">#FB-${f.id}</td>
          <td>${custName}</td>
          <td>${evtTitle}</td>
          <td>${ratingHtml}</td>
          <td>${statusBadge}</td>
          <td class="text-muted small">${timeAgo}</td>
          <td class="text-end text-nowrap">
            <button class="btn btn-sm btn-primary" onclick="openManageModal(${f.id})">
              Manage
            </button>
            <button class="btn btn-sm btn-outline-secondary ms-1" onclick="openViewModal(${f.id})" title="View Details">
              <i class="bi bi-eye"></i>
            </button>
            <button class="btn btn-sm btn-soft-delete ms-1" onclick="openDeleteModal(${f.id})" title="Soft delete feedback">
              <i class="bi bi-trash3"></i>
            </button>
          </td>
        </tr>
      `;
    }).join("");
}

function renderCardsDirectory(dataList) {
    const container = document.getElementById("feedbackCardsContainer");
    if (!container) return;

    if (!dataList || dataList.length === 0) {
        container.innerHTML = `
        <div class="col-12 text-center py-5 text-muted">
          No feedback entries found.
        </div>`;
        return;
    }

    container.innerHTML = dataList.map(f => {
        const custName = getCustomerLabel(f.customerId);
        const evtTitle = getBookingLabel(f.bookingId);
        const statusBadge = getStatusBadge(f.feedbackStatusId, f.escalatedToManager);
        const starsHtml = getRatingStarsHtml(f.rating);

        return `
        <div class="col-12 col-md-6" id="card-fb-${f.id}">
          <div class="card border-0 shadow-sm p-4 h-100">
            <div class="d-flex justify-content-between align-items-start mb-2">
              <div>
                <h6 class="fw-bold mb-0">${custName}</h6>
                <small class="text-muted">Event: ${evtTitle}</small>
              </div>
              ${statusBadge}
            </div>
            <div class="mb-2">
              ${starsHtml}
            </div>
            <p class="text-muted small flex-grow-1">${f.reviewText || "No comment provided."}</p>
            ${f.croResponse ? `<div class="p-2 mb-3 bg-light border rounded small text-secondary"><strong>CRO Reply:</strong> ${f.croResponse}</div>` : ''}
            <div class="border-top pt-3 d-flex justify-content-end gap-2">
              ${!f.escalatedToManager ? `
                <button class="btn btn-sm btn-outline-danger" onclick="openEscalateModal(${f.id})">Escalate</button>
              ` : `
                <span class="small text-danger fw-semibold d-flex align-items-center me-auto">
                  <i class="bi bi-person-fill-exclamation me-1"></i> Routed to Operations Mgr
                </span>
              `}
              <button class="btn btn-sm btn-primary" onclick="openManageModal(${f.id})">Respond & Publish</button>
              <button class="btn btn-sm btn-soft-delete" onclick="openDeleteModal(${f.id})">
                <i class="bi bi-trash3 me-1"></i>Delete
              </button>
            </div>
          </div>
        </div>
      `;
    }).join("");
}

function renderEscalatedTable(dataList) {
    const tbody = document.getElementById("escalatedTableBody");
    if (!tbody) return;

    const escalatedList = dataList.filter(f => f.escalatedToManager || Number(f.feedbackStatusId) === 4);

    if (escalatedList.length === 0) {
        tbody.innerHTML = `
        <tr>
          <td colspan="7" class="text-center py-4 text-muted">
            <i class="bi bi-check2-circle fs-3 d-block mb-1 text-success"></i>
            No active escalations. All complaints resolved.
          </td>
        </tr>`;
        return;
    }

    tbody.innerHTML = escalatedList.map(f => {
        const custName = getCustomerLabel(f.customerId);
        const ratingHtml = getRatingStarsHtml(f.rating);

        return `
        <tr>
          <td class="fw-semibold text-danger">#ESC-${f.id}</td>
          <td>${custName}</td>
          <td>#EVT-${f.bookingId}</td>
          <td>${ratingHtml}</td>
          <td class="text-muted small">${f.escalationRemarks || "Critical customer complaint routed to OM."}</td>
          <td><span class="badge badge-soft-danger">Under Review (OM)</span></td>
          <td class="text-end">
            <button class="btn btn-sm btn-outline-primary" onclick="openViewModal(${f.id})">Track Status</button>
          </td>
        </tr>
      `;
    }).join("");
}

/* =====================================================
   4. SEARCH & FILTERING
   ===================================================== */
function handleSearch(query) {
    const q = query.trim().toLowerCase();
    if (!q) {
        renderQueueTable(allFeedbacks);
        return;
    }
    const filtered = allFeedbacks.filter(f => {
        const idMatch = String(f.id).includes(q);
        const custMatch = getCustomerLabel(f.customerId).toLowerCase().includes(q);
        const bookingMatch = String(f.bookingId).includes(q) || getBookingLabel(f.bookingId).toLowerCase().includes(q);
        const textMatch = (f.reviewText || "").toLowerCase().includes(q);
        return idMatch || custMatch || bookingMatch || textMatch;
    });
    renderQueueTable(filtered);
}

function filterDirectory(statusValue) {
    if (statusValue === "ALL") {
        renderCardsDirectory(allFeedbacks);
        return;
    }
    const filtered = allFeedbacks.filter(f => String(f.feedbackStatusId) === String(statusValue));
    renderCardsDirectory(filtered);
}

function switchToEscalationsTab() {
    const tabEl = document.getElementById("tab-escalations");
    if (tabEl && window.bootstrap) {
        bootstrap.Tab.getOrCreateInstance(tabEl).show();
    }
}

/* =====================================================
   5. MANAGE / RESPOND TO FEEDBACK MODAL
   ===================================================== */
function openManageModal(id) {
    const feedback = allFeedbacks.find(f => Number(f.id) === Number(id));
    if (!feedback) return;

    selectedFeedbackId = id;
    document.getElementById("manageModalTitle").textContent = `Review & Respond: #FB-${id}`;
    document.getElementById("manageCustomerName").textContent = getCustomerLabel(feedback.customerId);
    document.getElementById("manageBookingSub").textContent = `Booking Event ID: #${feedback.bookingId}`;
    document.getElementById("manageRatingStars").innerHTML = getRatingStarsHtml(feedback.rating);
    document.getElementById("manageReviewText").textContent = `"${feedback.reviewText || ''}"`;
    document.getElementById("manageCroResponseInput").value = feedback.croResponse || "";
    document.getElementById("manageStatusSelect").value = feedback.feedbackStatusId || "1";

    const modalEl = document.getElementById("manageFeedbackModal");
    if (modalEl && window.bootstrap) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

async function saveFeedbackResponse() {
    if (!selectedFeedbackId) return;

    const responseText = document.getElementById("manageCroResponseInput").value.trim();
    const statusId = document.getElementById("manageStatusSelect").value;

    const saveBtn = document.getElementById("saveResponseBtn");
    if (saveBtn) {
        saveBtn.disabled = true;
        saveBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Saving...';
    }

    try {
        const params = new URLSearchParams();
        if (responseText) params.append("croResponse", responseText);
        if (statusId) params.append("feedbackStatusId", statusId);

        const res = await fetch(`${API_BASE}/${selectedFeedbackId}?${params.toString()}`, {
            method: "PUT"
        });

        if (!res.ok) throw new Error("Server update failed");

        // Update in local memory
        const fb = allFeedbacks.find(f => Number(f.id) === Number(selectedFeedbackId));
        if (fb) {
            fb.croResponse = responseText;
            fb.feedbackStatusId = Number(statusId);
        }
        showToast("Response Saved", `Feedback #${selectedFeedbackId} updated successfully.`, true);
    } catch (e) {
        console.warn("Backend API call failed, persisting update locally:", e.message);
        const fb = allFeedbacks.find(f => Number(f.id) === Number(selectedFeedbackId));
        if (fb) {
            fb.croResponse = responseText;
            fb.feedbackStatusId = Number(statusId);
            localStorage.setItem("snappro_local_feedback", JSON.stringify(allFeedbacks));
        }
        showToast("Response Updated", `Feedback #${selectedFeedbackId} updated (offline mode).`, true);
    } finally {
        if (saveBtn) {
            saveBtn.disabled = false;
            saveBtn.innerHTML = "Save Response & Resolve";
        }

        const modalEl = document.getElementById("manageFeedbackModal");
        if (modalEl && window.bootstrap) {
            bootstrap.Modal.getOrCreateInstance(modalEl).hide();
        }

        updateMetrics();
        renderQueueTable(allFeedbacks);
        renderCardsDirectory(allFeedbacks);
    }
}

/* =====================================================
   6. ESCALATION TO OM
   ===================================================== */
function promptEscalateCurrent() {
    if (!selectedFeedbackId) return;
    const manageModal = document.getElementById("manageFeedbackModal");
    if (manageModal && window.bootstrap) {
        bootstrap.Modal.getOrCreateInstance(manageModal).hide();
    }
    openEscalateModal(selectedFeedbackId);
}

function openEscalateModal(id) {
    selectedFeedbackId = id;
    document.getElementById("escalateFeedbackIdLabel").textContent = `#FB-${id}`;
    document.getElementById("escalateRemarksInput").value = "";

    const modalEl = document.getElementById("escalateFeedbackModal");
    if (modalEl && window.bootstrap) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

async function confirmEscalationSubmit() {
    if (!selectedFeedbackId) return;
    const remarks = document.getElementById("escalateRemarksInput").value.trim();
    if (!remarks) {
        alert("Please enter escalation remarks describing the reason.");
        return;
    }

    try {
        const params = new URLSearchParams();
        params.append("escalationRemarks", remarks);

        const res = await fetch(`${API_BASE}/${selectedFeedbackId}/escalate?${params.toString()}`, {
            method: "PUT"
        });
        if (!res.ok) throw new Error("Server error during escalation");

        const fb = allFeedbacks.find(f => Number(f.id) === Number(selectedFeedbackId));
        if (fb) {
            fb.escalatedToManager = true;
            fb.escalationRemarks = remarks;
        }
        showToast("Escalated to OM", `Feedback #${selectedFeedbackId} routed to Operations Manager.`, true);
    } catch (e) {
        console.warn("API offline, persisting escalation locally:", e.message);
        const fb = allFeedbacks.find(f => Number(f.id) === Number(selectedFeedbackId));
        if (fb) {
            fb.escalatedToManager = true;
            fb.escalationRemarks = remarks;
            localStorage.setItem("snappro_local_feedback", JSON.stringify(allFeedbacks));
        }
        showToast("Escalated to OM", `Feedback #${selectedFeedbackId} routed to Operations Manager.`, true);
    } finally {
        const modalEl = document.getElementById("escalateFeedbackModal");
        if (modalEl && window.bootstrap) {
            bootstrap.Modal.getOrCreateInstance(modalEl).hide();
        }

        updateMetrics();
        renderQueueTable(allFeedbacks);
        renderCardsDirectory(allFeedbacks);
        renderEscalatedTable(allFeedbacks);
    }
}

/* =====================================================
   7. SOFT DELETE
   ===================================================== */
function openDeleteModal(id) {
    feedbackToDeleteId = id;
    document.getElementById("deleteFeedbackId").textContent = `#FB-${id}`;
    const modalEl = document.getElementById("deleteFeedbackModal");
    if (modalEl && window.bootstrap) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

async function executeSoftDelete() {
    if (!feedbackToDeleteId) return;

    const id = feedbackToDeleteId;
    const deleteBtn = document.getElementById("confirmSoftDeleteBtn");
    if (deleteBtn) {
        deleteBtn.disabled = true;
        deleteBtn.innerHTML = '<span class="spinner-border spinner-border-sm me-1"></span>Deleting...';
    }

    try {
        const res = await fetch(`${API_BASE}/${id}`, {
            method: "DELETE"
        });
        if (!res.ok) throw new Error("Delete failed on server");

        allFeedbacks = allFeedbacks.filter(f => Number(f.id) !== Number(id));
        showToast("Archived", `Feedback #${id} was soft-deleted successfully.`, true);
    } catch (e) {
        console.warn("API offline, archiving feedback locally:", e.message);
        allFeedbacks = allFeedbacks.filter(f => Number(f.id) !== Number(id));
        localStorage.setItem("snappro_local_feedback", JSON.stringify(allFeedbacks));
        showToast("Archived", `Feedback #${id} was soft-deleted successfully.`, true);
    } finally {
        if (deleteBtn) {
            deleteBtn.disabled = false;
            deleteBtn.innerHTML = '<i class="bi bi-trash3 me-1"></i> Delete Feedback';
        }

        const modalEl = document.getElementById("deleteFeedbackModal");
        if (modalEl && window.bootstrap) {
            bootstrap.Modal.getOrCreateInstance(modalEl).hide();
        }

        feedbackToDeleteId = null;
        updateMetrics();
        renderQueueTable(allFeedbacks);
        renderCardsDirectory(allFeedbacks);
        renderEscalatedTable(allFeedbacks);
    }
}

/* =====================================================
   8. VIEW AUDIT & ESCALATION MODAL
   ===================================================== */
function openViewModal(id) {
    const fb = allFeedbacks.find(f => Number(f.id) === Number(id));
    if (!fb) return;

    document.getElementById("viewCaseRef").textContent = `#FB-${fb.id} / Booking #EVT-${fb.bookingId}`;
    document.getElementById("viewCustomerRemarks").textContent = fb.reviewText || "No customer remarks.";
    document.getElementById("viewCroResponse").textContent = fb.croResponse || "No CRO response logged.";

    const escSection = document.getElementById("viewEscalationSection");
    if (fb.escalatedToManager || Number(fb.feedbackStatusId) === 4) {
        escSection.style.display = "block";
        document.getElementById("viewEscalationRemarks").textContent = fb.escalationRemarks || "Incident forwarded to OM.";
    } else {
        escSection.style.display = "none";
    }

    const modalEl = document.getElementById("viewFeedbackModal");
    if (modalEl && window.bootstrap) {
        bootstrap.Modal.getOrCreateInstance(modalEl).show();
    }
}

/* =====================================================
   9. SESSION VERIFICATION & PAGE INITIALIZATION
   ===================================================== */
async function verifyCroSession() {
    if (window.location.protocol === 'file:') {
        return null;
    }
    try {
        const response = await fetch('/api/auth/session', {
            method: 'GET',
            credentials: 'include'
        });
        const data = await response.json();
        if (response.ok && data.status && data.data) {
            return data.data;
        } else {
            console.warn("No active session found, redirecting to login.html");
            window.location.href = 'login.html';
            return null;
        }
    } catch (e) {
        console.warn("Session check error:", e);
        return null;
    }
}

document.addEventListener("DOMContentLoaded", async function () {
    // Bind soft-delete confirm button
    const confirmDeleteBtn = document.getElementById("confirmSoftDeleteBtn");
    if (confirmDeleteBtn) {
        confirmDeleteBtn.addEventListener("click", executeSoftDelete);
    }

    // Load CRO officer details
    const user = await verifyCroSession();
    if (user) {
        const fullName = `${user.firstName || ''} ${user.lastName || ''}`.trim() || 'Prabhashvara A.P.K.I';
        const initials = ((user.firstName?.[0] || 'P') + (user.lastName?.[0] || 'A')).toUpperCase();

        const nameEl = document.getElementById("officerNameDisplay");
        const avatarEl = document.getElementById("officerAvatar");
        const mobNameEl = document.getElementById("mobileOfficerName");
        const mobAvatarEl = document.getElementById("mobileOfficerAvatar");

        if (nameEl) nameEl.textContent = fullName;
        if (avatarEl) avatarEl.textContent = initials;
        if (mobNameEl) mobNameEl.textContent = fullName;
        if (mobAvatarEl) mobAvatarEl.textContent = initials;
    }

    loadFeedbackData();
});
