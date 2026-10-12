// Role-based Authentication & Dashboard Redirection

function getDashboardForRole(role) {
    if (!role) {
        return 'customer-dashboard.html';
    }

    // Normalize role string (e.g. "ClientRelationsOfficer" -> "CLIENT_RELATIONS_OFFICER")
    const rawRole = role.toString().trim();
    const decamelized = rawRole.replace(/([a-z])([A-Z])/g, '$1_$2');
    const normalizedRole = decamelized.toUpperCase().replace(/[\s-]+/g, '_');

    switch (normalizedRole) {
        case 'ADMIN':
        case 'SYSTEM_ADMIN':
        case 'ADMINISTRATOR':
            return 'admin-dashboard.html';

        case 'CUSTOMER':
        case 'CLIENT':
            return 'customer-dashboard.html';

        case 'CRO':
        case 'CLIENT_RELATIONS_OFFICER':
        case 'CLIENT_RELATION_OFFICER':
        case 'CLIENTRELATIONSOFFICER':
        case 'CLIENTRELATIONOFFICER':
            return 'customer-feedback.html';

        case 'MARKETING_MANAGER':
        case 'MARKETING':
        case 'MARKETINGMANAGER':
            return 'marketingManager-dashboard.html';

        case 'OPERATION_MANAGER':
        case 'OPERATIONS_MANAGER':
        case 'OPERATIONS':
        case 'OPERATIONMANAGER':
            return 'operationManager-dashboard.html';

        case 'PHOTOGRAPHER':
        case 'LEAD_PHOTOGRAPHER':
        case 'SENIOR_PHOTOGRAPHER':
        case 'SENIOR_EVENT_PHOTOGRAPHER':
        case 'SENIOREVENTPHOTOGRAPHER':
            return 'photographer.html';

        case 'EQUIPMENT_MANAGER':
        case 'EQUIPMENT_COORDINATOR':
        case 'EQUIPMENT':
        case 'INVENTORY_MANAGER':
            return 'equipment-dashboard.html';

        default:
            console.warn(`Unknown user role: "${role}", defaulting to customer dashboard.`);
            return 'customer-dashboard.html';
    }
}

async function signIn() {
    const emailInput = document.getElementById('signInEmail');
    const passwordInput = document.getElementById('signInPassword');

    if (!emailInput || !passwordInput) {
        console.error("Sign in form inputs not found.");
        return;
    }

    const email = emailInput.value.trim();
    const password = passwordInput.value.trim();

    if (!email || !password) {
        alert("Please enter both email and password.");
        return;
    }

    const signInDto = {
        email: email,
        password: password
    };

    try {
        const response = await fetch('/api/auth/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            credentials: 'include',
            body: JSON.stringify(signInDto)
        });

        const data = await response.json();

        if (response.ok && data.status && data.data) {
            const user = data.data;
            const userRole = user.role || '';
            const targetDashboard = getDashboardForRole(userRole);

            // Save user in sessionStorage for fast access
            sessionStorage.setItem('currentUser', JSON.stringify(user));
            sessionStorage.removeItem('postLoginRedirect');

            console.log(`Authenticated as ${userRole}. Redirecting to: ${targetDashboard}`);
            window.location.href = targetDashboard;

        } else {
            alert(data.message || "Sign In Failed! Invalid email or password.");
        }
    } catch (error) {
        console.error("Error during sign in:", error);
        alert("An error occurred while connecting to the server.");
    }
}

async function signOut() {
    try {
        await fetch('/api/auth/logout', {
            method: 'POST',
            credentials: 'include'
        });
    } catch (error) {
        console.error("Error during sign out:", error);
    } finally {
        sessionStorage.clear();
        window.location.href = 'login.html';
    }
}

async function verifyUserSession(allowedRoles = []) {
    // If opened directly from file system (file:///), allow testing without redirect
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
            const user = data.data;
            const rawRole = (user.role || '').toString().trim();
            const decamelRole = rawRole.replace(/([a-z])([A-Z])/g, '$1_$2').toUpperCase().replace(/[\s-]+/g, '_');
            const simpleRole = rawRole.toUpperCase().replace(/[\s-]+/g, '_');

            if (allowedRoles.length > 0) {
                const isAllowed = allowedRoles.some(r => {
                    const norm = r.toString().toUpperCase().replace(/[\s-]+/g, '_');
                    return norm === simpleRole || norm === decamelRole;
                });

                if (!isAllowed) {
                    console.warn(`Access denied for role "${user.role}". Redirecting to proper dashboard...`);
                    const target = getDashboardForRole(user.role);
                    window.location.href = target;
                    return null;
                }
            }

            sessionStorage.setItem('currentUser', JSON.stringify(user));
            return user;
        } else {
            console.warn("No active session found. Redirecting to login...");
            window.location.href = 'login.html';
            return null;
        }
    } catch (error) {
        console.warn("Session check failed or server offline:", error);
        return null;
    }
}
