const API_BASE = '/api';

// ---- Users ----

async function loadUsers() {
    const res = await fetch(`${API_BASE}/users`);
    const users = await res.json();
    renderUserList(users);
    return users;
}

function renderUserList(users) {
    const list = document.getElementById('user-list');
    list.innerHTML = '';
    for (const user of users) {
        const li = document.createElement('li');
        li.textContent = `${user.name} (${user.email})`;
        list.appendChild(li);
    }
}

document.getElementById('add-user-btn').addEventListener('click', async () => {
    const name = document.getElementById('user-name').value.trim();
    const email = document.getElementById('user-email').value.trim();

    if (!name || !email) {
        alert('Please enter both name and email.');
        return;
    }

    const res = await fetch(`${API_BASE}/users`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, email })
    });

    if (!res.ok) {
        alert('Failed to add user.');
        return;
    }

    document.getElementById('user-name').value = '';
    document.getElementById('user-email').value = '';
    await loadGroups(); // refresh both the user list and the checkboxes
});

// ---- Groups ----

async function loadGroups() {
    const users = await loadUsers();
    renderMemberCheckboxes(users);

    const res = await fetch(`${API_BASE}/groups`);
    const groups = await res.json();
    renderGroupList(groups);
    populateGroupDropdowns(groups);
    return groups;
}

function renderGroupList(groups) {
    const list = document.getElementById('group-list');
    list.innerHTML = '';
    for (const group of groups) {
        const li = document.createElement('li');
        li.textContent = `${group.name} (id: ${group.id}) — members: ${group.members.map(m => m.name).join(', ')}`;
        list.appendChild(li);
    }
}

function renderMemberCheckboxes(users) {
    const container = document.getElementById('group-member-checkboxes');
    container.innerHTML = '';
    for (const user of users) {
        const label = document.createElement('label');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = user.id;
        checkbox.className = 'member-checkbox';
        label.appendChild(checkbox);
        label.appendChild(document.createTextNode(user.name));
        container.appendChild(label);
        container.appendChild(document.createElement('br'));
    }
}

document.getElementById('create-group-btn').addEventListener('click', async () => {
    const name = document.getElementById('group-name').value.trim();
    const checkboxes = document.querySelectorAll('.member-checkbox:checked');
    const memberIds = Array.from(checkboxes).map(cb => Number(cb.value));

    if (!name) {
        alert('Please enter a group name.');
        return;
    }
    if (memberIds.length === 0) {
        alert('Please select at least one member.');
        return;
    }

    const res = await fetch(`${API_BASE}/groups`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ name, memberIds })
    });

    if (!res.ok) {
        alert('Failed to create group.');
        return;
    }

    document.getElementById('group-name').value = '';
    document.querySelectorAll('.member-checkbox:checked').forEach(cb => cb.checked = false);
    await loadGroups(); // refetch from the server instead of manually appending
});

// ---- Group dropdowns (expense + balance sections) ----

function populateGroupDropdowns(groups) {
    const expenseSelect = document.getElementById('expense-group-select');
    const balanceSelect = document.getElementById('balance-group-select');
    const previousExpenseValue = expenseSelect.value;
    const previousBalanceValue = balanceSelect.value;

    for (const select of [expenseSelect, balanceSelect]) {
        select.innerHTML = '<option value="">-- Select a group --</option>';
        for (const group of groups) {
            const option = document.createElement('option');
            option.value = group.id;
            option.textContent = group.name;
            select.appendChild(option);
        }
    }

    // Preserve selection across re-renders where possible
    expenseSelect.value = previousExpenseValue;
    balanceSelect.value = previousBalanceValue;
}

document.getElementById('expense-group-select').addEventListener('change', async (e) => {
    const groupId = Number(e.target.value);
    if (!groupId) {
        populateExpensePayerAndSplits(null);
        return;
    }
    const res = await fetch(`${API_BASE}/groups/${groupId}`);
    const group = await res.json();
    populateExpensePayerAndSplits(group);
});

function populateExpensePayerAndSplits(group) {
    const payerSelect = document.getElementById('expense-payer-select');
    const splitContainer = document.getElementById('expense-split-checkboxes');

    payerSelect.innerHTML = '';
    splitContainer.innerHTML = '';

    if (!group) return;

    for (const member of group.members) {
        const option = document.createElement('option');
        option.value = member.id;
        option.textContent = member.name;
        payerSelect.appendChild(option);

        const label = document.createElement('label');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = member.id;
        checkbox.className = 'split-checkbox';
        checkbox.checked = true; // default: split among everyone
        label.appendChild(checkbox);
        label.appendChild(document.createTextNode(member.name));
        splitContainer.appendChild(label);
        splitContainer.appendChild(document.createElement('br'));
    }
}

// ---- Expenses ----

document.getElementById('add-expense-btn').addEventListener('click', async () => {
    const groupId = Number(document.getElementById('expense-group-select').value);
    const description = document.getElementById('expense-description').value.trim();
    const amountRupees = parseFloat(document.getElementById('expense-amount').value);
    const paidByUserId = Number(document.getElementById('expense-payer-select').value);
    const splitCheckboxes = document.querySelectorAll('.split-checkbox:checked');
    const splitAmongUserIds = Array.from(splitCheckboxes).map(cb => Number(cb.value));

    if (!groupId || !description || isNaN(amountRupees) || !paidByUserId || splitAmongUserIds.length === 0) {
        alert('Please fill in all expense fields and select at least one person to split with.');
        return;
    }

    const amountInCents = Math.round(amountRupees * 100);

    const res = await fetch(`${API_BASE}/groups/${groupId}/expenses`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ description, amountInCents, paidByUserId, splitAmongUserIds })
    });

    if (!res.ok) {
        alert('Failed to add expense.');
        return;
    }

    document.getElementById('expense-description').value = '';
    document.getElementById('expense-amount').value = '';
    alert('Expense added!');
});

// ---- Balances & Settlement ----

document.getElementById('view-balances-btn').addEventListener('click', async () => {
    const groupId = Number(document.getElementById('balance-group-select').value);
    if (!groupId) {
        alert('Please select a group.');
        return;
    }

    const [balancesRes, settlementRes] = await Promise.all([
        fetch(`${API_BASE}/groups/${groupId}/balances`),
        fetch(`${API_BASE}/groups/${groupId}/balances/settlement-plan`)
    ]);

    const balances = await balancesRes.json();
    const settlement = await settlementRes.json();

    renderBalances(balances);
    renderSettlement(settlement);
});

function renderBalances(balances) {
    const container = document.getElementById('balance-results');
    container.innerHTML = '<h3>Net Balances</h3>';
    const ul = document.createElement('ul');
    for (const b of balances) {
        const li = document.createElement('li');
        const rupees = (b.netBalanceInCents / 100).toFixed(2);
        const sign = b.netBalanceInCents > 0 ? 'is owed' : b.netBalanceInCents < 0 ? 'owes' : 'is settled,';
        li.textContent = `${b.userName} ${sign} ₹${Math.abs(rupees)}`;
        ul.appendChild(li);
    }
    container.appendChild(ul);
}

function renderSettlement(settlement) {
    const container = document.getElementById('settlement-results');
    container.innerHTML = '<h3>Settlement Plan</h3>';
    if (settlement.length === 0) {
        container.innerHTML += '<p>Everyone is settled up!</p>';
        return;
    }
    const ul = document.createElement('ul');
    for (const t of settlement) {
        const li = document.createElement('li');
        const rupees = (t.amountInCents / 100).toFixed(2);
        li.textContent = `${t.fromUserName} pays ${t.toUserName} ₹${rupees}`;
        ul.appendChild(li);
    }
    container.appendChild(ul);
}

// ---- Page load ----

loadGroups();