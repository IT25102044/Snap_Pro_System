// SnapPro - Package Management JavaScript

const API_BASE_URL = '/api/packages';

// Global cache for packages
let packagesList = [];

// Image file state for Create and Edit modals (Single Image)
let createSelectedFile = null;
let editExistingImage = null;
let editNewFile = null;
let currentEditingPackageId = null;

// Initialize on DOM load
document.addEventListener('DOMContentLoaded', () => {
    loadPackages();

    // Attach form submit listeners
    const createForm = document.getElementById('createPackageForm');
    if (createForm) {
        createForm.addEventListener('submit', handleCreatePackage);
    }

    const editForm = document.getElementById('editPackageForm');
    if (editForm) {
        editForm.addEventListener('submit', handleUpdatePackage);
    }

    // Image preview listener for Create modal (single file)
    const createImagesInput = document.getElementById('createImage') || document.getElementById('createImages');
    if (createImagesInput) {
        createImagesInput.addEventListener('change', (e) => {
            const files = e.target.files;
            createSelectedFile = (files && files.length > 0) ? files[0] : null;
            renderCreateImagePreview();
        });
    }

    // Image preview listener for Edit modal (single file)
    const editImagesInput = document.getElementById('editImage') || document.getElementById('editImages');
    if (editImagesInput) {
        editImagesInput.addEventListener('change', (e) => {
            const files = e.target.files;
            editNewFile = (files && files.length > 0) ? files[0] : null;
            renderEditImagePreview();
        });
    }
});

/**
 * Render Create Image Preview (Single Image)
 */
function renderCreateImagePreview() {
    const container = document.getElementById('createImagePreviewContainer');
    const gallery = document.getElementById('createImagePreviewGallery');
    if (!container || !gallery) return;

    if (!createSelectedFile) {
        container.style.display = 'none';
        gallery.innerHTML = '';
        return;
    }

    container.style.display = 'block';
    const reader = new FileReader();
    reader.onload = (ev) => {
        const sizeKb = (createSelectedFile.size / 1024).toFixed(1);
        gallery.innerHTML = `
            <div class="d-flex align-items-center justify-content-between w-100 p-1">
                <div class="d-flex align-items-center gap-2">
                    <img src="${ev.target.result}" class="rounded border shadow-sm" style="width: 60px; height: 60px; object-fit: cover;" alt="Cover Preview">
                    <div>
                        <span class="fw-semibold small d-block text-truncate" style="max-width: 250px;">${escapeHtml(createSelectedFile.name)}</span>
                        <span class="text-muted small">${sizeKb} KB</span>
                    </div>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="clearCreateImage()" title="Remove selected image">
                    <i class="bi bi-trash3 me-1"></i>Remove
                </button>
            </div>
        `;
    };
    reader.readAsDataURL(createSelectedFile);
}

function clearCreateImage() {
    createSelectedFile = null;
    const input = document.getElementById('createImage') || document.getElementById('createImages');
    if (input) input.value = '';
    renderCreateImagePreview();
}

function clearCreateImages() {
    clearCreateImage();
}

function removeCreateImage() {
    clearCreateImage();
}

/**
 * Render Edit Image Preview (Single Image)
 */
function renderEditImagePreview() {
    const container = document.getElementById('editImagePreviewContainer');
    const gallery = document.getElementById('editImagePreviewGallery');
    if (!container || !gallery) return;

    if (!editNewFile && !editExistingImage) {
        container.style.display = 'none';
        gallery.innerHTML = '';
        return;
    }

    container.style.display = 'block';

    if (editNewFile) {
        const reader = new FileReader();
        reader.onload = (ev) => {
            const sizeKb = (editNewFile.size / 1024).toFixed(1);
            gallery.innerHTML = `
                <div class="d-flex align-items-center justify-content-between w-100 p-1">
                    <div class="d-flex align-items-center gap-2">
                        <img src="${ev.target.result}" class="rounded border border-primary shadow-sm" style="width: 60px; height: 60px; object-fit: cover;" alt="New Upload Preview">
                        <div>
                            <span class="badge bg-primary mb-1">New Cover Selected</span>
                            <span class="fw-semibold small d-block text-truncate" style="max-width: 250px;">${escapeHtml(editNewFile.name)}</span>
                            <span class="text-muted small">${sizeKb} KB (Replaces current cover)</span>
                        </div>
                    </div>
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="cancelEditNewFile()" title="Cancel new image">
                        <i class="bi bi-x-circle me-1"></i>Cancel
                    </button>
                </div>
            `;
        };
        reader.readAsDataURL(editNewFile);
    } else if (editExistingImage) {
        gallery.innerHTML = `
            <div class="d-flex align-items-center justify-content-between w-100 p-1">
                <div class="d-flex align-items-center gap-2">
                    <img src="${editExistingImage}" class="rounded border shadow-sm" style="width: 60px; height: 60px; object-fit: cover;" alt="Current Cover">
                    <div>
                        <span class="fw-semibold small d-block">Current Package Cover</span>
                        <span class="text-muted small">Saved in system</span>
                    </div>
                </div>
                <button type="button" class="btn btn-sm btn-outline-danger" onclick="removeEditExistingImage()" title="Delete cover photo">
                    <i class="bi bi-trash3 me-1"></i>Delete Image
                </button>
            </div>
        `;
    }
}

function cancelEditNewFile() {
    editNewFile = null;
    const input = document.getElementById('editImage') || document.getElementById('editImages');
    if (input) input.value = '';
    renderEditImagePreview();
}

function removeEditNewFile() {
    cancelEditNewFile();
}

/**
 * Remove an existing saved image from the package immediately
 */
function removeEditExistingImage(imageUrl) {
    if (!currentEditingPackageId) return;

    if (!confirm('Are you sure you want to delete this cover image from the package?')) {
        return;
    }

    const targetUrl = imageUrl || editExistingImage;
    const urlParam = targetUrl ? `?imageUrl=${encodeURIComponent(targetUrl)}` : '';

    fetch(`${API_BASE_URL}/${currentEditingPackageId}/images${urlParam}`, {
        method: 'DELETE'
    })
        .then(async response => {
            const data = await response.json().catch(() => null);
            if (!response.ok || (data && data.status === false)) {
                throw new Error((data && data.message) || 'Failed to delete image.');
            }
            return data;
        })
        .then(() => {
            editExistingImage = null;
            renderEditImagePreview();
            loadPackages();
        })
        .catch(error => {
            console.error('Error deleting image:', error);
            alert('Error deleting image: ' + error.message);
        });
}

/**
 * Clear edit images
 */
function clearAllEditImages() {
    if (editNewFile) {
        cancelEditNewFile();
    }
    if (editExistingImage) {
        removeEditExistingImage();
    }
}

function clearEditImage() {
    clearAllEditImages();
}

/**
 * Check if a package is considered active
 */
function isPackageActive(pkg) {
    if (!pkg) return false;
    if (pkg.status) {
        const s = pkg.status.toUpperCase();
        return s === 'ACTIVE' || s === 'AVAILABLE';
    }
    if (pkg.packageStatusName) {
        const s = pkg.packageStatusName.toUpperCase();
        return s === 'ACTIVE' || s === 'AVAILABLE';
    }
    if (pkg.packageStatusId !== undefined && pkg.packageStatusId !== null) {
        return pkg.packageStatusId === 1;
    }
    return true;
}

/**
 * Fetch and display all packages
 */
function loadPackages() {
    fetch(`${API_BASE_URL}/all`)
        .then(response => {
            if (!response.ok) {
                return fetch(API_BASE_URL).then(r => r.json());
            }
            return response.json();
        })
        .then(data => {
            if (data && Array.isArray(data.data)) {
                packagesList = data.data;
            } else if (Array.isArray(data)) {
                packagesList = data;
            } else {
                packagesList = [];
            }

            renderOverviewTable(packagesList);
            renderCatalogGrid(packagesList);
            renderArchivedTable(packagesList);
            updateDashboardMetrics(packagesList);
        })
        .catch(error => {
            console.error('Error loading packages:', error);
            const tbody = document.getElementById('packagesTableBody');
            if (tbody) {
                tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-danger">Failed to load packages: ${error.message}</td></tr>`;
            }
        });
}

/**
 * Helper to get package title by ID
 */
function getPackageTitle(id) {
    const pkg = packagesList.find(p => p.id === id);
    return pkg ? (pkg.title || 'Package #' + id) : 'Package #' + id;
}

/**
 * Render Managed Photography Packages Table (Overview Tab)
 */
function renderOverviewTable(packages) {
    const tbody = document.getElementById('packagesTableBody');
    if (!tbody) return;

    if (!packages || packages.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-muted">No packages found. Create one to get started!</td></tr>`;
        return;
    }

    tbody.innerHTML = packages.map(pkg => {
        const active = isPackageActive(pkg);
        const statusBadge = active
            ? `<span class="badge badge-soft-success">Active / Published</span>`
            : `<span class="badge badge-soft-secondary">Archived / Discontinued</span>`;

        const hours = (pkg.coverageHours !== undefined && pkg.coverageHours !== null) ? Number(pkg.coverageHours).toFixed(1) : '0.0';
        const price = (pkg.price !== undefined && pkg.price !== null) ? Number(pkg.price).toFixed(2) : '0.00';
        const discount = (pkg.discountRate !== undefined && pkg.discountRate !== null) ? Number(pkg.discountRate).toFixed(0) : '0';
        const discountDisplay = Number(discount) > 0 ? `${discount}% Promo` : '0%';
        const updatedDate = pkg.updatedAt ? formatDate(pkg.updatedAt) : (pkg.createdAt ? formatDate(pkg.createdAt) : 'Recently');

        const coverUrl = pkg.imageUrl || (pkg.imageUrls && pkg.imageUrls[0]);

        const imageThumb = coverUrl
            ? `<div class="position-relative me-2 flex-shrink-0">
                 <img src="${coverUrl}" class="rounded border shadow-sm" style="width: 44px; height: 44px; object-fit: cover;" alt="Cover" />
               </div>`
            : `<div class="rounded me-2 bg-light border flex-shrink-0 d-flex align-items-center justify-content-center text-muted" style="width: 44px; height: 44px;"><i class="bi bi-camera"></i></div>`;

        return `
            <tr>
                <td>
                    <div class="d-flex align-items-center">
                        ${imageThumb}
                        <div>
                            <span class="fw-semibold d-block text-truncate" style="max-width: 220px;">${escapeHtml(pkg.title || 'Untitled Package')}</span>
                            <small class="text-muted" style="font-size: 11px;">ID #${pkg.id}</small>
                        </div>
                    </div>
                </td>
                <td>${hours} Hours</td>
                <td>LKR ${price}</td>
                <td>${discountDisplay}</td>
                <td>${statusBadge}</td>
                <td>${updatedDate}</td>
                <td class="text-end">
                    <button type="button" class="btn btn-sm btn-outline-secondary me-1" onclick="openPreviewModal(${pkg.id})">
                        <i class="bi bi-eye"></i> Preview
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-primary me-1" onclick="openEditModal(${pkg.id})">
                        <i class="bi bi-pencil"></i> Edit
                    </button>
                    ${active ? `
                        <button type="button" class="btn btn-sm btn-outline-warning me-1" onclick="archivePackage(${pkg.id})">
                            <i class="bi bi-archive"></i> Archive
                        </button>
                    ` : `
                        <button type="button" class="btn btn-sm btn-outline-success me-1" onclick="restorePackage(${pkg.id})">
                            <i class="bi bi-arrow-counterclockwise"></i> Restore
                        </button>
                    `}
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="deletePackage(${pkg.id})">
                        <i class="bi bi-trash"></i> Delete
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Render Live Catalog Grid View (Service Packages Tab)
 */
function renderCatalogGrid(packages) {
    const grid = document.getElementById('catalogCardsContainer');
    if (!grid) return;

    const activePackages = (packages || []).filter(pkg => isPackageActive(pkg));

    if (activePackages.length === 0) {
        grid.innerHTML = `<div class="col-12 text-center py-5 text-muted"><p>No active service packages available.</p></div>`;
        return;
    }

    grid.innerHTML = activePackages.map((pkg, index) => {
        const hours = (pkg.coverageHours !== undefined && pkg.coverageHours !== null) ? Number(pkg.coverageHours).toFixed(1) : '0.0';
        const price = (pkg.price !== undefined && pkg.price !== null) ? Number(pkg.price).toFixed(2) : '0.00';
        const isFeatured = index === 1;

        const coverUrl = pkg.imageUrl || (pkg.imageUrls && pkg.imageUrls[0]);

        const imageBanner = coverUrl 
            ? `<div class="position-relative d-flex align-items-center justify-content-center bg-dark overflow-hidden" style="height: 190px; border-radius: 12px 12px 0 0;">
                 <img src="${coverUrl}" class="position-absolute w-100 h-100" style="object-fit: cover; filter: blur(16px) brightness(0.5); transform: scale(1.15); pointer-events: none;" alt="">
                 <img src="${coverUrl}" class="position-relative" style="max-width: 100%; max-height: 100%; width: auto; height: 100%; object-fit: contain; z-index: 1;" alt="${escapeHtml(pkg.title)}">
               </div>`
            : `<div class="bg-light d-flex align-items-center justify-content-center text-muted" style="height: 110px; border-radius: 12px 12px 0 0;"><i class="bi bi-camera-fill fs-1 opacity-50"></i></div>`;

        let inclusionsHtml = '';
        if (pkg.inclusions) {
            const items = pkg.inclusions.includes(';')
                ? pkg.inclusions.split(';').map(s => s.trim()).filter(Boolean)
                : pkg.inclusions.split(',').map(s => s.trim()).filter(Boolean);
            inclusionsHtml = items.map(item => `
                <li class="mb-1"><i class="bi bi-check-circle-fill text-primary me-2"></i>${escapeHtml(item)}</li>
            `).join('');
        } else {
            inclusionsHtml = `<li class="mb-1"><i class="bi bi-check-circle-fill text-primary me-2"></i>Standard package inclusions</li>`;
        }

        return `
            <div class="col-12 col-md-4 mb-3">
                <div class="catalog-card overflow-hidden ${isFeatured ? 'border-primary' : ''}">
                    ${imageBanner}
                    <div class="p-4 d-flex flex-column flex-grow-1">
                        <div class="d-flex justify-content-between align-items-center">
                            <span class="${isFeatured ? 'text-primary' : 'text-muted'} small fw-bold text-uppercase">Tier #${index + 1}</span>
                            ${isFeatured ? '<span class="badge bg-primary">Best Value</span>' : ''}
                        </div>
                        <h4 class="fw-bold my-1">${escapeHtml(pkg.title || 'Untitled Package')}</h4>
                        <div class="my-2">
                            <span class="fs-3 fw-bold text-primary">LKR ${price}</span>
                            <span class="text-muted small">/ ${hours} Hours</span>
                        </div>
                        <p class="text-muted small">${escapeHtml(pkg.description || 'Standard photography package coverage.')}</p>
                        <ul class="list-unstyled small text-secondary mb-3 flex-grow-1">
                            ${inclusionsHtml}
                        </ul>
                        <div class="border-top pt-3 mt-2 d-flex justify-content-between align-items-center">
                            <span class="badge badge-soft-success">Active</span>
                            <div>
                                <button type="button" class="btn btn-sm btn-outline-secondary me-1" onclick="openPreviewModal(${pkg.id})">Preview</button>
                                <button type="button" class="btn btn-sm btn-outline-secondary me-1" onclick="openEditModal(${pkg.id})">Edit</button>
                                <button type="button" class="btn btn-sm btn-outline-danger" onclick="archivePackage(${pkg.id})">Archive</button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }).join('');
}

/**
 * Render Archived Packages Table
 */
function renderArchivedTable(packages) {
    const tbody = document.getElementById('archivedPackagesTableBody');
    if (!tbody) return;

    const archivedPackages = (packages || []).filter(pkg => !isPackageActive(pkg));

    if (archivedPackages.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">No archived packages currently.</td></tr>`;
        return;
    }

    tbody.innerHTML = archivedPackages.map(pkg => {
        const hours = (pkg.coverageHours !== undefined && pkg.coverageHours !== null) ? Number(pkg.coverageHours).toFixed(1) : '0.0';
        const price = (pkg.price !== undefined && pkg.price !== null) ? Number(pkg.price).toFixed(2) : '0.00';
        const archivedDate = pkg.updatedAt ? formatDate(pkg.updatedAt) : (pkg.createdAt ? formatDate(pkg.createdAt) : 'Previously');

        const coverUrl = pkg.imageUrl || (pkg.imageUrls && pkg.imageUrls[0]);
        const imageThumb = coverUrl 
            ? `<img src="${coverUrl}" class="rounded me-2 border flex-shrink-0 shadow-sm" style="width: 38px; height: 38px; object-fit: cover;" alt="Cover" />`
            : `<div class="rounded me-2 bg-light border flex-shrink-0 d-flex align-items-center justify-content-center text-muted" style="width: 38px; height: 38px;"><i class="bi bi-camera"></i></div>`;

        return `
            <tr>
                <td>
                    <div class="d-flex align-items-center">
                        ${imageThumb}
                        <div>
                            <span class="fw-semibold d-block text-truncate" style="max-width: 220px;">${escapeHtml(pkg.title || 'Untitled Package')}</span>
                            <small class="text-muted" style="font-size: 11px;">ID #${pkg.id}</small>
                        </div>
                    </div>
                </td>
                <td>LKR ${price}</td>
                <td>${hours} hrs</td>
                <td>${archivedDate}</td>
                <td>Decommissioned / Discontinued</td>
                <td class="text-end">
                    <button type="button" class="btn btn-sm btn-outline-primary me-1" onclick="restorePackage(${pkg.id})">
                        <i class="bi bi-arrow-counterclockwise me-1"></i> Restore
                    </button>
                    <button type="button" class="btn btn-sm btn-outline-danger" onclick="deletePackage(${pkg.id})">
                        <i class="bi bi-trash me-1"></i> Delete
                    </button>
                </td>
            </tr>
        `;
    }).join('');
}

/**
 * Update dynamic metric cards
 */
function updateDashboardMetrics(packages) {
    const list = packages || [];
    const activePackages = list.filter(pkg => isPackageActive(pkg));
    const archivedPackages = list.filter(pkg => !isPackageActive(pkg));

    const statActiveEl = document.getElementById('statActiveCount');
    if (statActiveEl) statActiveEl.innerText = `${activePackages.length} Tiers`;

    const statTotalEl = document.getElementById('statTotalCount');
    if (statTotalEl) statTotalEl.innerText = `${list.length} Total`;

    const statArchivedEl = document.getElementById('statArchivedCount');
    if (statArchivedEl) statArchivedEl.innerText = `${archivedPackages.length} Taken Down`;

    const statTopSellerEl = document.getElementById('statTopSeller');
    if (statTopSellerEl) {
        if (activePackages.length > 0) {
            statTopSellerEl.innerText = activePackages[0].title;
        } else {
            statTopSellerEl.innerText = 'None';
        }
    }
}

/**
 * CREATE Package
 */
function handleCreatePackage(event) {
    event.preventDefault();

    const title = document.getElementById('createTitle').value.trim();
    const coverageHours = parseFloat(document.getElementById('createHours').value);
    const price = parseFloat(document.getElementById('createPrice').value);
    const discountRate = parseFloat(document.getElementById('createDiscount').value || '0');
    const description = document.getElementById('createDescription').value.trim();
    const inclusionsRaw = document.getElementById('createInclusions') ? document.getElementById('createInclusions').value.trim() : '';
    const statusVal = document.getElementById('createStatus') ? document.getElementById('createStatus').value : '1';
    const status = (statusVal === '2' || statusVal === 'DISCONTINUED') ? 'DISCONTINUED' : 'ACTIVE';

    if (!title) {
        alert('Please enter a package title.');
        return;
    }
    if (isNaN(coverageHours) || coverageHours <= 0) {
        alert('Please enter valid coverage hours.');
        return;
    }
    if (isNaN(price) || price < 0) {
        alert('Please enter a valid price.');
        return;
    }

    // Convert comma-separated inclusions to semicolon-separated for database storage
    let inclusions = '';
    if (inclusionsRaw) {
        const items = inclusionsRaw.split(/[,;\n]+/).map(s => s.trim()).filter(Boolean);
        inclusions = items.join('; ');
    } else {
        inclusions = 'Standard package inclusions';
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('coverageHours', coverageHours);
    formData.append('price', price);
    formData.append('discountRate', isNaN(discountRate) ? 0 : discountRate);
    formData.append('inclusions', inclusions);
    formData.append('description', description);
    formData.append('status', status);

    // Attach single image if selected
    if (createSelectedFile) {
        formData.append('image', createSelectedFile);
        formData.append('images', createSelectedFile);
    }

    fetch(API_BASE_URL, {
        method: 'POST',
        body: formData
    })
        .then(async response => {
            const data = await response.json();
            if (!response.ok || (data && data.status === false)) {
                throw new Error((data && data.message) || 'Failed to create package.');
            }
            return data;
        })
        .then(data => {
            const createdTitle = (data && data.data && data.data.title) || title;
            alert(`Package "${createdTitle}" created successfully!`);

            // Reset form and file selection
            const form = document.getElementById('createPackageForm');
            if (form) form.reset();
            if (document.getElementById('createInclusions')) {
                document.getElementById('createInclusions').value = '';
            }
            clearCreateImage();

            // Close modal
            const modalEl = document.getElementById('createPackageModal');
            if (modalEl) {
                const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                modalInstance.hide();
            }

            // Refresh list
            loadPackages();
        })
        .catch(error => {
            console.error('Error creating package:', error);
            alert('Error creating package: ' + error.message);
        });
}

/**
 * Open Edit Modal with package data and current images
 */
function openEditModal(id) {
    currentEditingPackageId = id;
    editNewFiles = [];

    fetch(`${API_BASE_URL}/${id}`)
        .then(response => {
            if (!response.ok) {
                throw new Error('Could not fetch package details');
            }
            return response.json();
        })
        .then(data => {
            const pkg = (data && data.data) ? data.data : data;
            document.getElementById('editId').value = pkg.id;
            document.getElementById('editTitle').value = pkg.title || '';
            document.getElementById('editHours').value = pkg.coverageHours !== undefined ? pkg.coverageHours : '';
            document.getElementById('editPrice').value = pkg.price !== undefined ? pkg.price : '';
            document.getElementById('editDiscount').value = pkg.discountRate !== undefined ? pkg.discountRate : '0';
            document.getElementById('editDescription').value = pkg.description || '';
            if (document.getElementById('editInclusions')) {
                if (pkg.inclusions) {
                    // Show inclusions separated by commas for manager editing convenience
                    const items = pkg.inclusions.includes(';')
                        ? pkg.inclusions.split(';').map(s => s.trim()).filter(Boolean)
                        : pkg.inclusions.split(',').map(s => s.trim()).filter(Boolean);
                    document.getElementById('editInclusions').value = items.join(', ');
                } else {
                    document.getElementById('editInclusions').value = '';
                }
            }
            if (document.getElementById('editStatus')) {
                document.getElementById('editStatus').value = isPackageActive(pkg) ? '1' : '2';
            }

            // Reset edit file input
            const editImagesInput = document.getElementById('editImage') || document.getElementById('editImages');
            if (editImagesInput) editImagesInput.value = '';

            // Populate existing image
            if (pkg.imageUrl) {
                editExistingImage = pkg.imageUrl;
            } else if (pkg.imageUrls && pkg.imageUrls.length > 0) {
                editExistingImage = pkg.imageUrls[0];
            } else {
                editExistingImage = null;
            }

            renderEditImagePreview();

            const editModalEl = document.getElementById('editPackageModal');
            const editModal = bootstrap.Modal.getOrCreateInstance(editModalEl);
            editModal.show();
        })
        .catch(error => {
            console.error('Error opening edit modal:', error);
            alert('Error fetching package details: ' + error.message);
        });
}

/**
 * UPDATE Package
 */
function handleUpdatePackage(event) {
    event.preventDefault();

    const id = document.getElementById('editId').value;
    const title = document.getElementById('editTitle').value.trim();
    const coverageHours = parseFloat(document.getElementById('editHours').value);
    const price = parseFloat(document.getElementById('editPrice').value);
    const discountRate = parseFloat(document.getElementById('editDiscount').value || '0');
    const description = document.getElementById('editDescription').value.trim();
    const inclusionsRaw = document.getElementById('editInclusions') ? document.getElementById('editInclusions').value.trim() : '';
    const statusVal = document.getElementById('editStatus') ? document.getElementById('editStatus').value : '1';
    const status = (statusVal === '2' || statusVal === 'DISCONTINUED') ? 'DISCONTINUED' : 'ACTIVE';

    if (!title) {
        alert('Please enter a package title.');
        return;
    }
    if (isNaN(coverageHours) || coverageHours <= 0) {
        alert('Please enter valid coverage hours.');
        return;
    }
    if (isNaN(price) || price < 0) {
        alert('Please enter a valid price.');
        return;
    }

    // Convert comma-separated inclusions to semicolon-separated for database storage
    let inclusions = '';
    if (inclusionsRaw) {
        const items = inclusionsRaw.split(/[,;\n]+/).map(s => s.trim()).filter(Boolean);
        inclusions = items.join('; ');
    } else {
        inclusions = 'Standard package inclusions';
    }

    const formData = new FormData();
    formData.append('title', title);
    formData.append('coverageHours', coverageHours);
    formData.append('price', price);
    formData.append('discountRate', isNaN(discountRate) ? 0 : discountRate);
    formData.append('inclusions', inclusions);
    formData.append('description', description);
    formData.append('status', status);

    // Append newly selected image if any
    if (editNewFile) {
        formData.append('image', editNewFile);
        formData.append('images', editNewFile);
    }

    fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        body: formData
    })
        .then(async response => {
            const data = await response.json();
            if (!response.ok || (data && data.status === false)) {
                throw new Error((data && data.message) || 'Failed to update package.');
            }
            return data;
        })
        .then(data => {
            const updatedTitle = (data && data.data && data.data.title) || title;
            alert(`Package "${updatedTitle}" updated successfully!`);

            editNewFile = null;
            const editImagesInput = document.getElementById('editImage') || document.getElementById('editImages');
            if (editImagesInput) editImagesInput.value = '';

            // Close modal
            const modalEl = document.getElementById('editPackageModal');
            if (modalEl) {
                const modalInstance = bootstrap.Modal.getInstance(modalEl) || new bootstrap.Modal(modalEl);
                modalInstance.hide();
            }

            // Refresh list
            loadPackages();
        })
        .catch(error => {
            console.error('Error updating package:', error);
            alert('Error updating package: ' + error.message);
        });
}

/**
 * DELETE Package
 */
function deletePackage(id) {
    const title = getPackageTitle(id);
    if (!confirm(`Are you sure you want to permanently delete package "${title}"?`)) {
        return;
    }

    fetch(`${API_BASE_URL}/${id}`, {
        method: 'DELETE'
    })
        .then(async response => {
            const data = await response.json().catch(() => null);
            if (!response.ok || (data && data.status === false)) {
                throw new Error((data && data.message) || 'Failed to delete package.');
            }
            alert(`Package "${title}" processed: ${data.message || 'Deleted successfully'}`);
            loadPackages();
        })
        .catch(error => {
            console.error('Error deleting package:', error);
            alert('Error deleting package: ' + error.message);
            loadPackages();
        });
}

/**
 * ARCHIVE Package (Change status to DISCONTINUED)
 */
function archivePackage(id) {
    const title = getPackageTitle(id);
    if (!confirm(`Are you sure you want to archive / take down package "${title}" from the customer portal?`)) {
        return;
    }

    fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            status: 'DISCONTINUED'
        })
    })
        .then(async response => {
            const data = await response.json();
            if (!response.ok || (data && data.status === false)) {
                throw new Error((data && data.message) || 'Failed to archive package.');
            }
            alert(`Package "${title}" archived successfully!`);
            loadPackages();
        })
        .catch(error => {
            console.error('Error archiving package:', error);
            alert('Error archiving package: ' + error.message);
        });
}

/**
 * RESTORE Package (Change status to ACTIVE)
 */
function restorePackage(id) {
    const title = getPackageTitle(id);
    if (!confirm(`Restore package "${title}" back to the active catalog?`)) {
        return;
    }

    fetch(`${API_BASE_URL}/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            status: 'ACTIVE'
        })
    })
        .then(async response => {
            const data = await response.json();
            if (!response.ok || (data && data.status === false)) {
                throw new Error((data && data.message) || 'Failed to restore package.');
            }
            alert(`Package "${title}" restored to active catalog!`);
            loadPackages();
        })
        .catch(error => {
            console.error('Error restoring package:', error);
            alert('Error restoring package: ' + error.message);
        });
}

/**
 * Open Preview Modal (Supports Multi-Image Gallery view)
 */
function openPreviewModal(id) {
    fetch(`${API_BASE_URL}/${id}`)
        .then(response => {
            if (!response.ok) throw new Error('Package not found');
            return response.json();
        })
        .then(data => {
            const pkg = (data && data.data) ? data.data : data;
            const previewTitleEl = document.getElementById('previewTitle');
            const previewPriceEl = document.getElementById('previewPrice');
            const previewHoursEl = document.getElementById('previewHours');
            const previewDescEl = document.getElementById('previewDescription');
            const previewImgContainer = document.getElementById('previewImageContainer');
            const previewImg = document.getElementById('previewImage');
            const previewImgBg = document.getElementById('previewImageBg');
            const previewThumbs = document.getElementById('previewGalleryThumbs');

            if (previewTitleEl) previewTitleEl.innerText = pkg.title || 'Package Title';
            if (previewPriceEl) previewPriceEl.innerText = `LKR ${Number(pkg.price || 0).toFixed(2)}`;
            if (previewHoursEl) previewHoursEl.innerText = `/ ${Number(pkg.coverageHours || 0).toFixed(1)} Hours`;
            if (previewDescEl) previewDescEl.innerText = pkg.description || 'Standard photography package coverage.';

            // Render inclusions with ticks in preview card
            const previewInclusionsList = document.getElementById('previewInclusionsList');
            if (previewInclusionsList) {
                previewInclusionsList.innerHTML = '';
                const rawInclusions = pkg.inclusions || 'Standard package inclusions';
                const items = rawInclusions.includes(';')
                    ? rawInclusions.split(';').map(s => s.trim()).filter(Boolean)
                    : rawInclusions.split(',').map(s => s.trim()).filter(Boolean);
                items.forEach(item => {
                    previewInclusionsList.innerHTML += `<li class="mb-1"><i class="bi bi-check-circle-fill text-primary me-2"></i>${escapeHtml(item)}</li>`;
                });
            }

            const images = (pkg.imageUrls && pkg.imageUrls.length > 0) ? pkg.imageUrls : (pkg.imageUrl ? [pkg.imageUrl] : []);

            if (images.length > 0) {
                if (previewImg) previewImg.src = images[0];
                if (previewImgBg) previewImgBg.src = images[0];
                if (previewImgContainer) previewImgContainer.style.display = 'block';

                if (previewThumbs) {
                    previewThumbs.innerHTML = '';
                    if (images.length > 1) {
                        previewThumbs.style.setProperty('display', 'flex', 'important');
                        images.forEach((url, idx) => {
                            const thumb = document.createElement('img');
                            thumb.src = url;
                            thumb.alt = `Thumb ${idx + 1}`;
                            thumb.className = `rounded border ${idx === 0 ? 'border-primary border-2' : ''}`;
                            thumb.style.width = '60px';
                            thumb.style.height = '60px';
                            thumb.style.objectFit = 'cover';
                            thumb.style.cursor = 'pointer';
                            thumb.onclick = () => {
                                if (previewImg) previewImg.src = url;
                                if (previewImgBg) previewImgBg.src = url;
                                Array.from(previewThumbs.children).forEach(c => c.classList.remove('border-primary', 'border-2'));
                                thumb.classList.add('border-primary', 'border-2');
                            };
                            previewThumbs.appendChild(thumb);
                        });
                    } else {
                        previewThumbs.style.setProperty('display', 'none', 'important');
                    }
                }
            } else {
                if (previewImgContainer) previewImgContainer.style.display = 'none';
            }

            const previewModalEl = document.getElementById('previewPackageModal');
            const previewModal = bootstrap.Modal.getOrCreateInstance(previewModalEl);
            previewModal.show();
        })
        .catch(error => {
            console.error('Error opening preview:', error);
            alert('Error loading package preview: ' + error.message);
        });
}

/**
 * Helper: Format date string
 */
function formatDate(dateStr) {
    if (!dateStr) return '';
    try {
        const d = new Date(dateStr);
        if (isNaN(d.getTime())) return dateStr;
        return d.toLocaleDateString('en-GB', { day: '2-digit', month: 'short', year: 'numeric' });
    } catch (e) {
        return dateStr;
    }
}

/**
 * Helper: Escape HTML strings to prevent XSS
 */
function escapeHtml(text) {
    if (!text) return '';
    return String(text)
        .replace(/&/g, '&amp;')
        .replace(/</g, '&lt;')
        .replace(/>/g, '&gt;')
        .replace(/"/g, '&quot;')
        .replace(/'/g, '&#039;');
}

// Attach functions to window object for inline onclick handlers
window.loadPackages = loadPackages;
window.openEditModal = openEditModal;
window.deletePackage = deletePackage;
window.archivePackage = archivePackage;
window.restorePackage = restorePackage;
window.openPreviewModal = openPreviewModal;
window.clearCreateImage = clearCreateImage;
window.clearCreateImages = clearCreateImage;
window.removeCreateImage = clearCreateImage;
window.cancelEditNewFile = cancelEditNewFile;
window.removeEditExistingImage = removeEditExistingImage;
window.removeEditNewFile = cancelEditNewFile;
window.clearAllEditImages = clearAllEditImages;
window.clearEditImage = clearEditImage;

/**
 * Logout Marketing Manager and redirect to customer dashboard
 */
async function logoutUser() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
    } catch (e) {
        console.warn('Logout error:', e);
    } finally {
        sessionStorage.clear();
        localStorage.clear();
        window.location.href = 'customer-dashboard.html';
    }
}

function logout() {
    logoutUser();
}

function logoutAndGoHome() {
    logoutUser();
}

window.logoutUser = logoutUser;
window.logout = logoutUser;
window.logoutAndGoHome = logoutAndGoHome;
