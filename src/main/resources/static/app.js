// A more professional structure using a class
class ExpenseTrackerApp {
    constructor() {
    // --- STATE MANAGEMENT ---
    this.apiBaseUrls = this._buildApiBaseList();
    this.token = localStorage.getItem('token');
        this.expenses = [];
        this.undoTimeout = null;

        // --- DOM ELEMENT SELECTION ---
        this._selectDOMElements();
        
        // --- BIND EVENT LISTENERS ---
        this._bindEventListeners();
        
        // --- INITIALIZE APP ---
        this.init();
    }

    _selectDOMElements() {
        this.authContainer = document.getElementById('auth-container');
        this.dashboardContainer = document.getElementById('dashboard-container');
        this.loginForm = document.getElementById('login-form');
        this.registerForm = document.getElementById('register-form');
        this.expenseForm = document.getElementById('expense-form');
        this.expenseList = document.getElementById('expense-list');
        this.errorMessage = document.getElementById('error-message');
        this.toast = document.getElementById('toast-notification');
        this.showRegisterLink = document.getElementById('show-register');
        this.showLoginLink = document.getElementById('show-login');
        this.logoutBtn = document.getElementById('logout-btn');
        this.cancelEditBtn = document.getElementById('cancel-edit-btn');
    }

    _buildApiBaseList() {
        const bases = [];
        const origin = window.location.origin;

        if (origin && origin.startsWith('http')) {
            bases.push(origin);
            try {
                const url = new URL(origin);
                const hostname = url.hostname;
                const protocol = url.protocol;
                bases.push(`${protocol}//${hostname}:8081`);
                bases.push(`${protocol}//${hostname}:8080`);
            } catch (error) {
                console.warn('[ExpenseTracker] Unable to parse current origin:', error);
            }
        }

        bases.push('http://localhost:8081');
        bases.push('http://127.0.0.1:8081');
        bases.push('http://localhost:8080');
        bases.push('http://127.0.0.1:8080');

        return Array.from(new Set(bases));
    }

    _bindEventListeners() {
        this.showRegisterLink.addEventListener('click', this._toggleForms.bind(this));
        this.showLoginLink.addEventListener('click', this._toggleForms.bind(this));
        
        this.registerForm.addEventListener('submit', this._handleRegister.bind(this));
        this.loginForm.addEventListener('submit', this._handleLogin.bind(this));
        this.logoutBtn.addEventListener('click', this._handleLogout.bind(this));

        this.expenseForm.addEventListener('submit', this._handleExpenseSubmit.bind(this));
        this.expenseList.addEventListener('click', this._handleExpenseListClick.bind(this));
        this.cancelEditBtn.addEventListener('click', this._resetExpenseForm.bind(this));
    }

    init() {
        this._updateUI();
    }

    // --- UI LOGIC ---
    _toggleForms(e) {
        e.preventDefault();
        this.loginForm.classList.toggle('hidden');
        this.registerForm.classList.toggle('hidden');
    }

    _updateUI() {
        if (this.token) {
            this.authContainer.classList.add('hidden');
            this.dashboardContainer.classList.remove('hidden');
            this.fetchExpenses();
        } else {
            this.authContainer.classList.remove('hidden');
            this.dashboardContainer.classList.add('hidden');
        }
    }

    _displayMessage(element, message) {
        element.textContent = message;
        element.style.display = 'block';
        setTimeout(() => { element.style.display = 'none'; }, 3000);
    }

    _showToast(message, onUndo) {
        this.toast.innerHTML = `<span>${message}</span>`;
        if (onUndo) {
            const undoButton = document.createElement('button');
            undoButton.textContent = 'Undo';
            undoButton.onclick = async () => {
                undoButton.disabled = true;
                try {
                    await onUndo();
                } finally {
                    undoButton.disabled = false;
                }
            };
            this.toast.appendChild(undoButton);
        }
        this.toast.classList.add('show');
    }

    _hideToast() {
        this.toast.classList.remove('show');
    }

    _renderExpenses() {
        this.expenseList.innerHTML = '';
        this.expenses.forEach(expense => {
            const li = document.createElement('li');
            li.dataset.id = expense.id;
            li.innerHTML = `
                <div class="expense-info">
                    <div class="expense-details">
                        <span class="expense-description">${expense.description}</span>
                        <span class="expense-meta">
                            ${new Date(expense.date).toLocaleDateString()}
                            <span class="expense-category">${expense.category}</span>
                        </span>
                    </div>
                    <span class="expense-amount">₹${parseFloat(expense.amount).toFixed(2)}</span>
                </div>
                <div class="expense-actions">
                    <button class="edit-btn" title="Edit"><i class="fa-solid fa-pencil"></i></button>
                    <button class="delete-btn" title="Delete"><i class="fa-solid fa-trash-can"></i></button>
                </div>`;
            this.expenseList.appendChild(li);
        });
    }
    
    _resetExpenseForm() {
        this.expenseForm.reset();
        document.getElementById('expense-id').value = '';
        document.getElementById('form-title').textContent = 'Add New Expense';
        this.cancelEditBtn.classList.add('hidden');
    }
    
    // --- API SERVICE ---
    async _apiFetch(endpoint, method = 'GET', body = null) {
        const options = {
            method,
            headers: { 'Content-Type': 'application/json' }
        };
        if (this.token) {
            options.headers['Authorization'] = `Bearer ${this.token}`;
        }
        if (body) {
            options.body = JSON.stringify(body);
        }

        let lastNetworkError = null;
        console.info('[ExpenseTracker] Trying API bases in order:', this.apiBaseUrls);

        for (let i = 0; i < this.apiBaseUrls.length; i++) {
            const baseUrl = this.apiBaseUrls[i];
            try {
                const response = await fetch(`${baseUrl}${endpoint}`, options);

                if (response.status === 204) {
                    this._promoteApiBase(i);
                    return null;
                }

                let data = null;
                try {
                    data = await response.json();
                } catch (parseError) {
                    if (response.ok) {
                        console.warn('[ExpenseTracker] Expected JSON response but received none.', parseError);
                    }
                }

                if (!response.ok) {
                    const message = (data && data.message) || `Request failed with status ${response.status}`;
                    const retryableStatus = [404, 502, 503, 504].includes(response.status);

                    if ([401, 403].includes(response.status)) {
                        console.warn('[ExpenseTracker] Authentication failed. Clearing stored token.');
                        this._handleLogout();
                        this._displayMessage(this.errorMessage, 'Session expired. Please log in again.');
                        throw new Error(message);
                    }

                    if (retryableStatus && i < this.apiBaseUrls.length - 1) {
                        console.warn(`[ExpenseTracker] ${response.status} from ${baseUrl}${endpoint}. Trying next API base...`);
                        continue;
                    }

                    this._displayMessage(this.errorMessage, message);
                    throw new Error(message);
                }

                this._promoteApiBase(i);
                return data;
            } catch (error) {
                if (error instanceof TypeError || error.name === 'TypeError') {
                    console.warn(`[ExpenseTracker] Network error talking to ${baseUrl}: ${error.message}`);
                    lastNetworkError = error;
                    continue;
                }
                this._displayMessage(this.errorMessage, error.message);
                throw error;
            }
        }

        const fallbackError = lastNetworkError || new Error('Unable to reach the backend API. Please confirm it is running on port 8081.');
        console.error('[ExpenseTracker] All API base URLs failed. Is the Spring Boot server running?', fallbackError);
        this._displayMessage(this.errorMessage, fallbackError.message);
        throw fallbackError;
    }

    _promoteApiBase(successIndex) {
        if (successIndex <= 0 || successIndex >= this.apiBaseUrls.length) return;
        const [base] = this.apiBaseUrls.splice(successIndex, 1);
        this.apiBaseUrls.unshift(base);
    }

    // --- EVENT HANDLERS ---
    async _handleRegister(e) {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target).entries());
        const body = {
            firstName: data['register-firstname'],
            lastName: data['register-lastname'],
            email: data['register-email'],
            password: data['register-password']
        };
        try {
            const result = await this._apiFetch('/api/auth/register', 'POST', body);
            this.token = result.token;
            localStorage.setItem('token', this.token);
            this._updateUI();
            this.registerForm.reset();
        } catch (error) {
            console.error('Registration failed:', error);
        }
    }

    async _handleLogin(e) {
        e.preventDefault();
        const data = Object.fromEntries(new FormData(e.target).entries());
        const body = {
            email: data['login-email'],
            password: data['login-password']
        };
        try {
            const result = await this._apiFetch('/api/auth/login', 'POST', body);
            this.token = result.token;
            localStorage.setItem('token', this.token);
            this._updateUI();
            this.loginForm.reset();
        } catch (error) {
            console.error('Login failed:', error);
        }
    }

    _handleLogout() {
        this.token = null;
        localStorage.removeItem('token');
        this.expenses = [];
        this._updateUI();
    }
    
    async fetchExpenses() {
        try {
            this.expenses = (await this._apiFetch('/api/expenses')) || [];
            this._renderExpenses();
        } catch (error) {
            console.error('Failed to fetch expenses:', error);
        }
    }

    async _handleExpenseSubmit(e) {
        e.preventDefault();
        const id = document.getElementById('expense-id').value;
        const expenseData = {
            description: document.getElementById('expense-description').value,
            amount: parseFloat(document.getElementById('expense-amount').value),
            date: document.getElementById('expense-date').value,
            category: document.getElementById('expense-category').value
        };
        
        try {
            if (id) {
                await this._apiFetch(`/api/expenses/${id}`, 'PUT', expenseData);
            } else {
                await this._apiFetch('/api/expenses', 'POST', expenseData);
            }

            await this.fetchExpenses();
            this._resetExpenseForm();
            this._showToast('Expense saved.');
        } catch(error) {
            console.error('Failed to save expense:', error);
        }
    }

    async _handleExpenseListClick(e) {
        const target = e.target.closest('button');
        if (!target) return;

        const li = target.closest('li');
        if (!li) return;
        const id = li.dataset.id;
        
        if (target.classList.contains('edit-btn')) {
            const expense = this.expenses.find(exp => exp.id == id);
            document.getElementById('expense-id').value = expense.id;
            document.getElementById('expense-description').value = expense.description;
            document.getElementById('expense-amount').value = expense.amount;
            document.getElementById('expense-date').value = expense.date;
            document.getElementById('expense-category').value = expense.category;

            document.getElementById('form-title').textContent = 'Edit Expense';
            this.cancelEditBtn.classList.remove('hidden');
            window.scrollTo(0, 0);
        }
        
        if (target.classList.contains('delete-btn')) {
            clearTimeout(this.undoTimeout);
            const index = this.expenses.findIndex(exp => exp.id == id);
            if (index === -1) return;
            const deletedExpense = this.expenses[index];

            this.expenses.splice(index, 1);
            this._renderExpenses();

            try {
                await this._apiFetch(`/api/expenses/${id}`, 'DELETE');
            } catch (error) {
                console.error('Failed to delete expense:', error);
                this._displayMessage(this.errorMessage, 'Failed to delete expense. Please try again.');
                this.expenses.splice(index, 0, deletedExpense);
                this._renderExpenses();
                return;
            }

            const restoreExpense = async () => {
                clearTimeout(this.undoTimeout);
                try {
                    await this._apiFetch('/api/expenses', 'POST', {
                        description: deletedExpense.description,
                        amount: parseFloat(deletedExpense.amount),
                        date: deletedExpense.date,
                        category: deletedExpense.category
                    });
                    await this.fetchExpenses();
                } catch (err) {
                    console.error('Failed to restore expense:', err);
                    this._displayMessage(this.errorMessage, 'Failed to restore expense.');
                } finally {
                    this._hideToast();
                }
            };

            this._showToast('Expense deleted.', restoreExpense);
            this.undoTimeout = setTimeout(() => {
                this._hideToast();
            }, 5000);

            await this.fetchExpenses();
        }
    }
}

// Start the application
new ExpenseTrackerApp();
