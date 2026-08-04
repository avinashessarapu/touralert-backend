const state = {
  incidents: [],
  connected: false,
  stompClient: null,
  theme: 'dark'
};

const els = {};

document.addEventListener('DOMContentLoaded', () => {
  els.form = document.getElementById('incidentForm');
  els.formStatus = document.getElementById('formStatus');
  els.feed = document.getElementById('feed');
  els.incidentList = document.getElementById('incidentList');
  els.connectedState = document.getElementById('connectedState');
  els.incidentCount = document.getElementById('incidentCount');
  els.feedCount = document.getElementById('feedCount');
  els.themeToggle = document.getElementById('themeToggle');
  els.refreshBtn = document.getElementById('refreshBtn');

  els.form?.addEventListener('submit', submitIncident);
  els.themeToggle?.addEventListener('click', toggleTheme);
  els.refreshBtn?.addEventListener('click', loadIncidents);

  const cachedTheme = localStorage.getItem('touralert-theme') || 'dark';
  applyTheme(cachedTheme);
  loadIncidents();
  connectSocket();
});

function applyTheme(theme) {
  state.theme = theme;
  document.body.setAttribute('data-theme', theme);
  if (els.themeToggle) {
    els.themeToggle.textContent = theme === 'dark' ? '☀️ Light' : '🌙 Dark';
  }
}

function toggleTheme() {
  const nextTheme = state.theme === 'dark' ? 'light' : 'dark';
  localStorage.setItem('touralert-theme', nextTheme);
  applyTheme(nextTheme);
}

async function loadIncidents() {
  try {
    const response = await fetch('/api/incidents');
    const data = await response.json();
    state.incidents = Array.isArray(data) ? data : [];
    renderIncidents();
    updateStats();
  } catch (error) {
    setStatus('Unable to load incidents right now.', false);
  }
}

async function submitIncident(event) {
  event.preventDefault();
  const formData = new FormData(els.form);
  const payload = {
    type: formData.get('type'),
    description: formData.get('description'),
    routeOrLocation: formData.get('routeOrLocation'),
    latitude: Number(formData.get('latitude')),
    longitude: Number(formData.get('longitude')),
    status: 'OPEN'
  };

  try {
    const response = await fetch('/api/incidents', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    const data = await response.json();
    if (!response.ok) throw new Error(data.message || 'Submit failed');
    els.form.reset();
    setStatus(`Incident reported successfully at ${data.routeOrLocation || 'the requested route'}.`, true);
    await loadIncidents();
  } catch (error) {
    setStatus(error.message || 'Unable to submit incident.', false);
  }
}

function renderIncidents() {
  if (!state.incidents.length) {
    els.incidentList.innerHTML = '<div class="item muted">No incidents have been recorded yet.</div>';
    return;
  }

  els.incidentList.innerHTML = state.incidents.map((incident) => {
    const statusClass = incident.status === 'OPEN' ? 'open' : incident.status === 'RESOLVED' ? 'resolved' : 'critical';
    return `
      <div class="item">
        <strong>${incident.type || 'Incident'}</strong>
        <div class="muted">${incident.description || '—'}</div>
        <div class="muted" style="margin-top: 4px;">${incident.routeOrLocation || 'Unknown location'}</div>
        <span class="pill ${statusClass}">${incident.status || 'OPEN'}</span>
      </div>`;
  }).join('');
}

function updateStats() {
  if (els.incidentCount) els.incidentCount.textContent = state.incidents.length;
  if (els.feedCount) els.feedCount.textContent = els.feed?.children.length || 0;
}

function setStatus(message, success) {
  if (!els.formStatus) return;
  els.formStatus.textContent = message;
  els.formStatus.style.color = success ? 'var(--success)' : 'var(--danger)';
}

function connectSocket() {
  const socket = new SockJS('/ws');
  const client = Stomp.over(socket);
  client.debug = null;
  client.connect({}, () => {
    state.connected = true;
    state.stompClient = client;
    client.subscribe('/topic/hazards', (message) => {
      const payload = JSON.parse(message.body);
      appendFeed(payload);
      loadIncidents();
    });
    setConnectionState(true);
  }, () => {
    state.connected = false;
    setConnectionState(false);
  });
}

function setConnectionState(online) {
  if (els.connectedState) {
    els.connectedState.textContent = online ? 'Online' : 'Offline';
    els.connectedState.className = online ? 'status-online' : 'status-offline';
  }
}

function appendFeed(payload) {
  if (!els.feed) return;
  const item = document.createElement('div');
  item.className = 'item';
  item.innerHTML = `
    <strong>${payload.type || 'Hazard'}</strong>
    <div class="muted">${payload.description || 'New hazard broadcast'}</div>
    <div class="muted" style="margin-top: 4px;">${payload.routeOrLocation || 'Location pending'}</div>
    <span class="pill open">Live</span>`;
  els.feed.prepend(item);
  while (els.feed.children.length > 6) {
    els.feed.removeChild(els.feed.lastChild);
  }
  updateStats();
}