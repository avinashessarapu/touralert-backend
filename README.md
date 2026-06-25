

# 📡 TourAlert Backend Engine
> A real-time, event-driven public safety and transit hazard notification system built using Java, Spring Boot, Spring Security, and WebSocket architectures.

---

## 🛠️ System Architecture Diagram
The system operates as a stateless backend processing engine that securely updates database states and broadcasts streaming event feeds live to clients instantly without polling overhead.



1. **Ingestion (REST API):** Travelers upload safety hazards via a secure multi-part request. The incident is securely persistent in the database as `PENDING`.
2. **Authorization & State Control:** Administrators validate their session identity using stateless JWT signatures via an active Spring Security filter chain to verify the report.
3. **Reactive Broadcast Stream:** Upon verification, an automated event-driven service pipeline captures the update and pipes the payload out across a dedicated STOMP message broker channel over active SockJS WebSockets.

---

## 🚀 Core Technical Feature Highlights
* **Stateless JWT Security Filter Chain:** Integrated custom token parsers using Spring Security to validate incoming user payloads and control access based on granular Role Authorities (USER vs. ADMIN).
* **Real-time Event Broker (STOMP/SockJS):** Configured full-duplex persistent connections to transmit immediate warning blocks dynamically to multiple listening client nodes without requiring expensive page refreshes.
* **Relational Database Integrity:** Engineered decoupled data layer relations mapping incident parameters, geographic telemetry metadata (Latitude/Longitude), and user assignment states securely.

---

## 📡 Primary Managed API Blueprint

### 1. File & Incident Ingestion Pipeline
* **Endpoint:** `POST /api/incidents/upload`
* **Access:** Authenticated `USER` / `ADMIN` (Bearer JWT Token required)
* **Parameters:** `type`, `description`, `routeOrLocation`, `latitude`, `longitude`
* **Response:** `200 OK` (Incident parsed and saved successfully as `PENDING`)

### 2. State Machine Modification & Broadcast Trigger
* **Endpoint:** `PUT /api/incidents/{id}/status`
* **Access:** Restrictive `ADMIN` Role Authority Only
* **URL Query Parameters:** `?status=VERIFIED&adminUserId=2`
* **Response:** `200 OK` — `Incident status successfully updated to: VERIFIED`
* **System Event Side-Effect:** Fires automated routing loop to publish live alert data onto public sub-channel `/topic/active-hazards`.

---

## ⚙️ How to Run & Spin Up the Core Engine
1. Clone the repository down into your runtime workspace.
2. Ensure your active environment has Java 17+ and Maven installed.
3. Boot up the local web service server node instance by executing:
   ```bash
   ./mvnw spring-boot:run