const state = {
  user: null,
  token: null,
  trips: [],
  incidents: [],
  notifications: [],
  stompClient: null,
  connected: false,
  theme: 'dark'
};

const els = {};

document.addEventListener('DOMContentLoaded', () => {
  els.authView = document.getElementById('authView');
  els.dashboardView = document.getElementById('dashboardView');
  els.loginForm = document.getElementById('loginForm');
  els.registerForm = document.getElementById('registerForm');
  els.tripForm = document.getElementById('tripForm');
  els.incidentForm = document.getElementById('incidentForm');
  els.logoutButtons = document.querySelectorAll('.logout-btn');
  els.refreshBtn = document.getElementById('refreshBtn');
  els.refreshDashBtn = document.getElementById('refreshDashBtn');
  els.runRadarBtn = document.getElementById('travelerPortalBtn');
  els.scanActionBtn = document.getElementById('scanAction');
  els.userLabel = document.getElementById('userLabel');
  els.roleBadge = document.getElementById('roleBadge');
  els.dashboardTitle = document.getElementById('dashboardTitle');
  els.dashboardSubtitle = document.getElementById('dashboardSubtitle');
  els.tripList = document.getElementById('tripList');
  els.incidentList = document.getElementById('incidentList');
  els.notificationList = document.getElementById('notificationList');
  els.alertFeed = document.getElementById('alertFeed');
  els.adminPanel = document.getElementById('adminPanel');
  els.adminList = document.getElementById('adminList');
  els.riskOutput = document.getElementById('riskOutput');
  els.socketStatus = document.getElementById('socketStatus');
  els.themeToggle = document.getElementById('themeToggle');
  els.routeMap = document.getElementById('routeMap');
  els.mapStatus = document.getElementById('mapStatus');
  els.loginPanel = document.getElementById('loginPanel');
  els.signupPanel = document.getElementById('signupPanel');
  els.switchButtons = document.querySelectorAll('.switch-btn');
  els.portalCards = document.querySelectorAll('.portal-card');
  els.travelerPortalBtn = document.getElementById('travelerPortalBtn');
  els.adminPortalBtn = document.getElementById('adminPortalBtn');
  els.stats = {
    auth: document.getElementById('statTrips'),
    hazards: document.getElementById('statHazards'),
    alerts: document.getElementById('statAlerts')
  };
  els.statsDash = {
    trips: document.getElementById('statTripsDash'),
    hazards: document.getElementById('statHazardsDash'),
    alerts: document.getElementById('statAlertsDash'),
    connection: document.getElementById('statConnection')
  };

  els.coinBalance = document.getElementById('coinBalance');
  els.placesList = document.getElementById('placesList');
  // Map placeholders
  window.__googleMapsLoaded = false;
  window.__map = null;
  window.__directionsRenderer = null;
  window.__placesService = null;

  els.loginForm.addEventListener('submit', handleLogin);
  els.registerForm.addEventListener('submit', handleRegister);
  els.tripForm.addEventListener('submit', handleCreateTrip);
  els.incidentForm.addEventListener('submit', handleCreateIncident);
  els.logoutButtons.forEach((button) => button.addEventListener('click', logout));
  els.refreshBtn?.addEventListener('click', loadDashboard);
  els.refreshDashBtn?.addEventListener('click', loadDashboard);
  els.runRadarBtn?.addEventListener('click', () => exploreAs('traveler'));
  els.scanActionBtn?.addEventListener('click', runRadarScan);
  els.travelerPortalBtn?.addEventListener('click', () => exploreAs('traveler'));
  els.adminPortalBtn?.addEventListener('click', () => exploreAs('admin'));
  els.themeToggle?.addEventListener('click', toggleTheme);

  els.switchButtons.forEach((button) => {
    button.addEventListener('click', () => toggleAuthView(button.dataset.authView));
  });

  els.portalCards.forEach((button) => {
    button.addEventListener('click', () => exploreAs(button.dataset.demo));
  });

  document.querySelectorAll('[data-scroll]').forEach((button) => {
    button.addEventListener('click', () => {
      const target = document.getElementById(button.getAttribute('data-scroll'));
      target?.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
  });

  els.tripList.addEventListener('click', (event) => {
    const button = event.target.closest('button');
    if (!button) return;
    const action = button.dataset.action;
    const tripId = Number(button.dataset.tripId);
    if (action === 'risk') viewTripRisk(tripId);
    if (action === 'map') showTripMap(tripId);
    if (action === 'status') updateTripStatus(tripId, button.dataset.status);
  });

  els.incidentList.addEventListener('click', (event) => {
    const button = event.target.closest('button');
    if (!button) return;
    const action = button.dataset.action;
    const incidentId = Number(button.dataset.incidentId);
    if (action === 'verify') updateIncidentStatus(incidentId, 'VERIFIED');
    if (action === 'resolve') updateIncidentStatus(incidentId, 'RESOLVED');
    if (action === 'gov') submitToGov(incidentId);
  });

  els.notificationList.addEventListener('click', (event) => {
    const button = event.target.closest('button');
    if (!button) return;
    dismissNotification(Number(button.dataset.id));
  });

  const cachedTheme = localStorage.getItem('touralert-theme') || 'dark';
  applyTheme(cachedTheme);

  const cached = localStorage.getItem('touralert-session');
  if (cached) {
    const parsed = JSON.parse(cached);
    state.user = parsed.user;
    state.token = parsed.token;
    renderAuthState();
    connectSocket();
    loadDashboard();
  } else {
    renderAuthState();
  }
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

function ensureMap() {
  if (window.__googleMapsLoaded) return Promise.resolve();
  return fetch('/api/config').then(r => r.json()).then(cfg => {
    const key = cfg.mapsApiKey || '';
    return new Promise((resolve) => {
      const script = document.createElement('script');
      script.src = `https://maps.googleapis.com/maps/api/js?key=${key}&libraries=places`;
      script.onload = () => {
        window.__googleMapsLoaded = true;
        resolve();
      };
      document.head.appendChild(script);
    });
  });
}

async function initMapForTrip(start, dest) {
  await ensureMap();
  if (!window.__map) {
    window.__map = new google.maps.Map(document.getElementById('map'), { center: { lat: 20.5937, lng: 78.9629 }, zoom: 6 });
    window.__directionsRenderer = new google.maps.DirectionsRenderer({ suppressMarkers: false });
    window.__directionsRenderer.setMap(window.__map);
    window.__placesService = new google.maps.places.PlacesService(window.__map);
  }
  const directionsService = new google.maps.DirectionsService();
  directionsService.route({ origin: start, destination: dest, travelMode: 'DRIVING' }, (result, status) => {
    if (status === 'OK' && result) {
      window.__directionsRenderer.setDirections(result);
      // search nearby places around destination location
      const leg = result.routes[0].legs[0];
      const destLoc = leg.end_location;
      findNearbyPlaces(destLoc);
    }
  });
}

function findNearbyPlaces(location) {
  if (!window.__placesService) return;
  const request = { location: location, radius: 5000, type: ['lodging', 'restaurant'] };
  window.__placesService.nearbySearch(request, (results, status) => {
    if (status === google.maps.places.PlacesServiceStatus.OK && results && results.length) {
      els.placesList.innerHTML = results.slice(0,6).map(p => `
        <div class="list-item">
          <div class="inline-row">
            <h4>${p.name}</h4>
            <span class="muted small">${p.types?.includes('lodging') ? 'Hotel' : 'Restaurant'}</span>
          </div>
          <div class="muted small">${p.vicinity || ''}</div>
          <div style="margin-top:8px;">
            <button class="chip primary" onclick="spendCoins(10)">Pay 10 coins (₹2)</button>
          </div>
        </div>
      `).join('');
    } else {
      els.placesList.innerHTML = '<div class="list-item muted">No nearby places found.</div>';
    }
  });
}

function toggleAuthView(view) {
  els.switchButtons.forEach((button) => button.classList.toggle('active', button.dataset.authView === view));
  els.loginPanel.classList.toggle('hidden', view !== 'login');
  els.signupPanel.classList.toggle('hidden', view !== 'signup');
}

function renderAuthState() {
  if (state.user) {
    els.authView.classList.add('hidden');
    els.dashboardView.classList.remove('hidden');
    els.userLabel.textContent = `${state.user.username}`;
    els.roleBadge.textContent = state.user.role || 'USER';
    els.dashboardTitle.textContent = state.user.role === 'ADMIN' ? 'Admin safety command center' : 'Traveler safety command center';
    els.dashboardSubtitle.textContent = state.user.role === 'ADMIN'
      ? 'Verify incidents, monitor hazards, and coordinate responses in real time.'
      : 'Monitor route safety, receive updates, and respond quickly to new hazards.';
    els.logoutButtons.forEach((button) => button.classList.remove('hidden'));
  } else {
    els.authView.classList.remove('hidden');
    els.dashboardView.classList.add('hidden');
    els.logoutButtons.forEach((button) => button.classList.add('hidden'));
  }
}

function setStatus(message, isOk = true) {
  const target = document.getElementById('statusMessage');
  if (!target) return;
  target.textContent = message;
  target.style.color = isOk ? '#34d399' : '#fb7185';
}

async function authenticateUser(payload) {
  const response = await fetch('/api/users/login', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload)
  });
  const data = await response.json();
  if (!response.ok) throw new Error(data.message || 'Authentication failed');
  state.user = {
    id: Number(data.id),
    username: data.username,
    role: data.role,
    email: data.email
  };
  // include TripCoins if provided
  state.user.tripCoins = data.coins ? Number(data.coins) : 0;
  state.token = data.token;
  localStorage.setItem('touralert-session', JSON.stringify({ user: state.user, token: state.token }));
  renderAuthState();
  connectSocket();
  await loadDashboard();
  setStatus(`Welcome back, ${state.user.username}!`);
}

async function exploreAs(role) {
  const config = role === 'admin'
    ? { username: 'system_admin', password: 'adminsecure456' }
    : { username: 'avinash_travels', password: 'travelpass123' };
  try {
    await authenticateUser(config);
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function handleLogin(event) {
  event.preventDefault();
  const formData = new FormData(els.loginForm);
  try {
    await authenticateUser({
      username: formData.get('username'),
      password: formData.get('password')
    });
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function handleRegister(event) {
  event.preventDefault();
  const formData = new FormData(els.registerForm);
  try {
    const response = await fetch('/api/users/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: formData.get('username'),
        email: formData.get('email'),
        password: formData.get('password'),
        role: 'USER'
      })
    });
    const text = await response.text();
    if (!response.ok) throw new Error(text || 'Registration failed');
    setStatus('Account created. Logging you in...');
    await authenticateUser({
      username: formData.get('username'),
      password: formData.get('password')
    });
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function loadDashboard() {
  if (!state.user) return;
  try {
    const [tripsRes, hazardsRes, notificationsRes] = await Promise.all([
      fetch(`/api/trips/user/${state.user.id}`),
      fetch('/api/incidents/active'),
      fetch(`/api/notifications/user/${state.user.id}`)
    ]);

    state.trips = tripsRes.ok ? await tripsRes.json() : [];
    state.incidents = hazardsRes.ok ? await hazardsRes.json() : [];
    state.notifications = notificationsRes.ok ? await notificationsRes.json() : [];

    renderStats();
    renderTrips();
    renderIncidents();
    if (state.trips.length) {
      showTripMap(state.trips[0].id);
    }
    renderNotifications();
    renderAdminConsole();
    if (!state.connected) connectSocket();
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function handleCreateTrip(event) {
  event.preventDefault();
  if (!state.user) return;
  const formData = new FormData(els.tripForm);
  try {
    const response = await fetch(`/api/trips?userId=${state.user.id}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        destination: formData.get('destination'),
        startLocation: formData.get('startLocation'),
        startDate: formData.get('startDate'),
        endDate: formData.get('endDate')
      })
    });
    const text = await response.text();
    if (!response.ok) throw new Error(text || 'Unable to create trip');
    els.tripForm.reset();
    setStatus('Trip planned successfully');
    await loadDashboard();
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function handleCreateIncident(event) {
  event.preventDefault();
  if (!state.user) return;
  const formData = new FormData(els.incidentForm);
  const payload = new FormData();
  payload.append('type', formData.get('type'));
  payload.append('description', formData.get('description'));
  payload.append('routeOrLocation', formData.get('routeOrLocation'));
  payload.append('latitude', formData.get('latitude'));
  payload.append('longitude', formData.get('longitude'));
  payload.append('userId', String(state.user.id));
  const file = formData.get('file');
  if (file && file.size) payload.append('file', file);

  try {
    const response = await fetch('/api/incidents/upload', {
      method: 'POST',
      body: payload
    });
    const text = await response.text();
    if (!response.ok) throw new Error(text || 'Unable to report incident');
    els.incidentForm.reset();
    setStatus('Incident reported successfully');
    await loadDashboard();
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function dismissNotification(id) {
  try {
    await fetch(`/api/notifications/dismiss/${id}`, { method: 'PATCH' });
    await loadDashboard();
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function updateTripStatus(id, status) {
  try {
    await fetch(`/api/trips/${id}/status?status=${status}`, { method: 'PUT' });
    setStatus(`Trip marked as ${status.toLowerCase()}`);
    await loadDashboard();
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function updateIncidentStatus(id, status) {
  if (!state.user || state.user.role !== 'ADMIN') return;
  try {
    await fetch(`/api/incidents/${id}/status?status=${status}&adminUserId=${state.user.id}`, { method: 'PUT' });
    setStatus(`Incident marked as ${status}`);
    await loadDashboard();
  } catch (error) {
    setStatus(error.message, false);
  }
}

async function viewTripRisk(id) {
  try {
    const response = await fetch(`/api/trips/${id}/risk-assessment`);
    const data = await response.json();
    const summary = data.activeHazards?.length
      ? `${data.activeHazards.length} active hazard(s) linked to this route.`
      : 'No active hazards detected for this route.';
    const suggestion = data.recommendation || 'No additional guidance available.';
    els.riskOutput.innerHTML = `<strong>${data.trip?.destination || 'Route overview'}</strong><br/>${summary}<br/><span class="muted">${suggestion}</span>`;
    if (data.trip) {
      showTripMap(data.trip.id);
    }
    setStatus('Risk view refreshed');
  } catch (error) {
    setStatus(error.message, false);
  }
}

function showTripMap(tripId) {
  const trip = state.trips.find((item) => item.id === tripId);
  if (!trip) return;
  const origin = trip.startLocation || 'Current location';
  const destination = trip.destination || 'Destination';
  const query = encodeURIComponent(`${origin} to ${destination}`);
  if (els.routeMap) {
    els.routeMap.src = `https://www.google.com/maps?q=${query}&output=embed`;
  }
  if (els.mapStatus) {
    els.mapStatus.textContent = `${origin} → ${destination}`;
  }
}

async function runRadarScan() {
  try {
    const response = await fetch('/api/trips/radar?currentLat=18.2850&currentLng=82.9110');
    const data = await response.json();
    els.riskOutput.innerHTML = data.join('<br/>');
    setStatus('Route scan completed');
  } catch (error) {
    setStatus(error.message, false);
  }
}

function renderStats() {
  els.stats.auth.textContent = state.trips.length;
  els.stats.hazards.textContent = state.incidents.length;
  els.stats.alerts.textContent = state.notifications.length;
  els.statsDash.trips.textContent = state.trips.length;
  els.statsDash.hazards.textContent = state.incidents.length;
  els.statsDash.alerts.textContent = state.notifications.length;
  els.statsDash.connection.textContent = state.connected ? 'Live' : 'Offline';
  if (els.coinBalance) {
    const coins = state.user?.coins ?? state.user?.tripCoins ?? 0;
    els.coinBalance.textContent = coins;
  }
}

function renderTrips() {
  if (!state.trips.length) {
    els.tripList.innerHTML = '<div class="list-item muted">No trips yet. Plan one to get started.</div>';
    return;
  }

  els.tripList.innerHTML = state.trips.map(trip => `
    <div class="list-item">
      <div class="inline-row">
        <h4>${trip.destination}</h4>
        <span class="badge ${trip.status === 'PLANNED' ? 'warning' : trip.status === 'ONGOING' ? 'info' : 'success'}">${trip.status}</span>
      </div>
      <div class="muted small">From ${trip.startLocation || 'Unknown'} · ${trip.startDate || '—'} to ${trip.endDate || '—'}</div>
      <div class="chip-row">
        <button class="chip primary" data-action="risk" data-trip-id="${trip.id}">View risk</button>
        <button class="chip" data-action="map" data-trip-id="${trip.id}">Route map</button>
        <button class="chip" data-action="status" data-trip-id="${trip.id}" data-status="ONGOING">Start</button>
        <button class="chip" data-action="status" data-trip-id="${trip.id}" data-status="COMPLETED">Complete</button>
        <button class="chip danger" data-action="status" data-trip-id="${trip.id}" data-status="CANCELLED">Cancel</button>
      </div>
    </div>
  `).join('');
}

function showTripMap(tripId) {
  const trip = state.trips.find(t => Number(t.id) === Number(tripId));
  if (!trip) return;
  const start = trip.startLocation || '';
  const dest = trip.destination || '';
  initMapForTrip(start, dest);
  // scroll to map
  document.getElementById('map')?.scrollIntoView({ behavior: 'smooth' });
}

async function spendCoins(amount) {
  if (!state.user) return setStatus('Login to spend TripCoins', false);
  try {
    const res = await fetch(`/api/users/${state.user.id}/coins/debit?amount=${amount}`, { method: 'PUT' });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Payment failed');
    // update local state and UI
    state.user.tripCoins = data.coins;
    renderStats();
    setStatus(`Paid ${amount} coins (₹${(amount/10)*2})`);
  } catch (err) {
    setStatus(err.message, false);
  }
}

async function submitToGov(incidentId) {
  if (!state.user) return setStatus('Login as admin to submit to government portal', false);
  try {
    const res = await fetch(`/api/admin/incidents/${incidentId}/submit-gov`, { method: 'POST', headers: { 'Authorization': `Bearer ${state.token}` } });
    const data = await res.json();
    if (!res.ok) throw new Error(data.message || 'Submission failed');
    setStatus(`Submitted to government: ${data.govUrl}`);
    window.open(data.govUrl, '_blank');
  } catch (err) {
    setStatus(err.message, false);
  }
}

function renderIncidents() {
  if (!state.incidents.length) {
    els.incidentList.innerHTML = '<div class="list-item muted">No active hazards right now.</div>';
    return;
  }

  els.incidentList.innerHTML = state.incidents.map(incident => `
    <div class="list-item">
      <div class="inline-row">
        <h4>${incident.type}</h4>
        <span class="badge ${incident.status === 'VERIFIED' ? 'success' : 'warning'}">${incident.status}</span>
      </div>
      <div class="muted small">${incident.description}</div>
      <div class="muted small">${incident.routeOrLocation}</div>
      ${state.user?.role === 'ADMIN' ? `<div class="chip-row">
        <button class="chip primary" data-action="verify" data-incident-id="${incident.id}">Verify</button>
        <button class="chip" data-action="gov" data-incident-id="${incident.id}">Submit to Gov</button>
        <button class="chip danger" data-action="resolve" data-incident-id="${incident.id}">Resolve</button>
      </div>` : ''}
    </div>
  `).join('');
}

function renderNotifications() {
  if (!state.notifications.length) {
    els.notificationList.innerHTML = '<div class="list-item muted">No active alerts.</div>';
    return;
  }

  els.notificationList.innerHTML = state.notifications.map(note => `
    <div class="list-item">
      <div class="inline-row">
        <h4>${note.message}</h4>
        <button class="chip" data-id="${note.id}">Dismiss</button>
      </div>
      <div class="muted small">Trip: ${note.relatedTrip?.destination || '—'}</div>
    </div>
  `).join('');
}

function renderAdminConsole() {
  if (state.user?.role !== 'ADMIN') {
    els.adminPanel.classList.add('hidden');
    return;
  }
  els.adminPanel.classList.remove('hidden');
  if (!state.incidents.length) {
    els.adminList.innerHTML = '<div class="list-item muted">No incidents to review.</div>';
    return;
  }
  els.adminList.innerHTML = state.incidents.map(incident => `
    <div class="list-item">
      <div class="inline-row">
        <h4>${incident.type}</h4>
        <span class="badge ${incident.status === 'VERIFIED' ? 'success' : 'warning'}">${incident.status}</span>
      </div>
      <div class="muted small">${incident.description}</div>
      <div class="chip-row">
        <button class="chip primary" onclick="updateIncidentStatus(${incident.id}, 'VERIFIED')">Verify</button>
        <button class="chip danger" onclick="updateIncidentStatus(${incident.id}, 'RESOLVED')">Resolve</button>
      </div>
    </div>
  `).join('');
}

function connectSocket() {
  if (state.stompClient || !state.user) return;
  const socket = new SockJS('/ws-alerts');
  const client = Stomp.over(socket);
  client.debug = null;
  client.connect({}, () => {
    state.connected = true;
    state.stompClient = client;
    client.subscribe('/topic/active-hazards', (message) => {
      const payload = JSON.parse(message.body);
      appendAlert(payload);
    });
    els.statsDash.connection.textContent = 'Live';
    if (els.socketStatus) els.socketStatus.innerHTML = '<span class="status-online">● Live</span>';
  }, () => {
    state.connected = false;
    els.statsDash.connection.textContent = 'Offline';
    if (els.socketStatus) els.socketStatus.innerHTML = '<span class="status-busy">● Reconnecting</span>';
  });
}

function appendAlert(payload) {
  const item = document.createElement('div');
  item.className = 'alert-card';
  item.innerHTML = `
    <div class="inline-row">
      <strong>${payload.type || 'Alert'}</strong>
      <span class="badge warning">Live</span>
    </div>
    <div class="small muted">${payload.description || 'New hazard broadcast received.'}</div>
    <div class="small muted">${payload.routeOrLocation || 'Location pending'}</div>
  `;
  els.alertFeed.prepend(item);
}

function logout() {
  localStorage.removeItem('touralert-session');
  state.user = null;
  state.token = null;
  state.trips = [];
  state.incidents = [];
  state.notifications = [];
  renderAuthState();
  els.tripList.innerHTML = '';
  els.incidentList.innerHTML = '';
  els.notificationList.innerHTML = '';
  els.alertFeed.innerHTML = '<div class="muted">Live alerts will appear here.</div>';
  els.adminList.innerHTML = '';
  els.riskOutput.innerHTML = 'Run a scan to inspect nearby hazards and proximity alerts.';
  if (els.routeMap) els.routeMap.src = 'https://www.google.com/maps?q=TourAlert%20Route&output=embed';
  if (els.mapStatus) els.mapStatus.textContent = 'Select a trip to preview its route.';
  if (state.stompClient) {
    state.stompClient.disconnect();
    state.stompClient = null;
  }
  els.statsDash.connection.textContent = 'Offline';
  if (els.socketStatus) els.socketStatus.innerHTML = '<span class="status-busy">● Offline</span>';
}
