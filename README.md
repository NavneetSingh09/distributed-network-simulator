# Distributed Network Simulator

A production-grade distributed systems simulator built with Spring Boot and Docker. Simulates a real network with a router, multiple servers, packet queuing, fault tolerance, and live metrics — all running as separate Docker containers communicating over a virtual network.

---

## Live Demo

>  **[Live Demo Link]** — *(coming soon — deploying on AWS EC2)*

---

## Architecture

```
┌─────────────┐        ┌─────────────┐        ┌─────────────┐
│   CLIENT    │──────▶ │   ROUTER    │──────▶ │  SERVER 1   │
│  (Traffic   │        │  (Port 5000)│        │  (Port 6001)│
│ Simulator)  │        │             │──────▶ │             │
└─────────────┘        └─────────────┘        ├─────────────┤
                                               │  SERVER 2   │
                                               │  (Port 6002)│
                                               └─────────────┘
```

Each node runs as a **separate Docker container** on a shared virtual network (`sim-network`), making this a genuinely distributed system — not just threads in a single JVM.

---

## Features

### Core Distributed Systems
- **Separate Docker containers** — router, server1, server2 each run their own JVM
- **Concurrent packet handling** — router uses `ExecutorService` thread pool
- **Bounded BlockingQueue** — packets buffered (capacity 100) instead of dropped under load
- **Retry logic** — up to 3 attempts with 200ms delay before dropping a packet
- **Dynamic routing algorithms** — Round Robin and Least Load, switchable at runtime
- **Server fault tolerance** — toggle servers down, router automatically reroutes

### Observability
- **WebSocket live metrics** — STOMP push every second (no polling)
- **Real-time throughput chart** — packets/sec visualization
- **Color-coded system logs** — INFO / WARN / ERROR
- **Network flow animation** — live packet movement between nodes
- **Queue size bar** — visual fill indicator (green → yellow → red)

### Engineering Quality
- **Spring constructor injection** — no static mutable state anywhere
- **Type-safe config** — `RoutingConfig.Algorithm` enum, validated setters
- **37 unit + integration tests** — zero failures
- **Dark / Light mode** — preference persisted in localStorage

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3.2 |
| WebSocket | Spring WebSocket + STOMP + SockJS |
| Containerization | Docker, Docker Compose |
| Frontend | Vanilla JS, Chart.js, SockJS, STOMP.js |
| Testing | JUnit 5, Spring MockMvc |
| Build | Maven |

---

## Getting Started

### Prerequisites
- Docker Desktop
- Java 17+
- Maven 3.8+

### Run with Docker (Recommended)

```bash
git clone https://github.com/NavneetSingh09/distributed-network-simulator.git
cd distributed-network-sim
docker-compose up --build
```

Open **http://localhost:8080** in your browser.

### Run Locally (without Docker)

```bash
mvn clean spring-boot:run
```

Then open **http://localhost:8080**.

---

## How It Works

### 1. Packet Flow
```
TrafficSimulator → Router (port 5000) → PacketQueue → Dispatcher → Server1 or Server2
```

1. `TrafficSimulator` sends packets to the router via TCP socket
2. Router receives packet, checks drop rate, enqueues it in `PacketQueue`
3. Dispatcher thread drains the queue and forwards to a server using the selected algorithm
4. If forwarding fails, retries up to 3 times before dropping
5. Server handles the packet and reports back to the router for centralized metrics

### 2. Routing Algorithms

| Algorithm | Description |
|---|---|
| **Round Robin** | Alternates between Server1 and Server2 evenly |
| **Least Load** | Sends to the server with fewer handled packets |

### 3. Fault Tolerance
- Toggle any server **DOWN** via the dashboard
- Router automatically detects unavailable servers and reroutes
- Packets queue up during outages and are delivered on retry
- Server status reflected live in the dashboard

---

## Project Structure

```
src/main/java/sim/
├── SimulatorApplication.java    # Entry point, auto-starts node based on NODE_ROLE
├── client/
│   ├── ClientNode.java          # Interactive CLI client
│   └── TrafficSimulator.java    # Automated traffic generator
├── config/
│   ├── Ports.java               # Port constants + host resolution
│   ├── RoutingConfig.java       # Algorithm enum (ROUND_ROBIN / LEAST_LOAD)
│   ├── ServerStatusConfig.java  # Server up/down state
│   ├── SimulationConfig.java    # Drop rate, latency config with validation
│   └── WebSocketConfig.java     # STOMP WebSocket configuration
├── controller/
│   ├── MetricsController.java   # GET /api/metrics, /api/logs, /api/flows
│   └── SimulatorController.java # POST endpoints for control
├── metrics/
│   ├── LogStore.java            # In-memory log buffer
│   ├── MetricsStore.java        # Thread-safe metrics bean
│   └── PacketFlowStore.java     # Packet flow event store
├── model/
│   └── Packet.java              # Packet model with serialize/deserialize
├── osi/
│   └── OsiStack.java            # OSI layer encapsulation simulation
├── router/
│   ├── PacketQueue.java         # Bounded BlockingQueue (capacity 100)
│   └── Router.java              # Core router with thread pool + retry logic
├── server/
│   └── ServerNode.java          # Server node with HTTP callback to router
├── service/
│   ├── MetricsBroadcaster.java  # WebSocket push every 1 second
│   └── SimulatorService.java    # Orchestrates startup/shutdown
└── util/
    └── Log.java                 # Timestamped logger
```

---

## API Reference

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/router/start` | Start the router |
| POST | `/api/server/start/1` | Start Server 1 |
| POST | `/api/server/start/2` | Start Server 2 |
| POST | `/api/traffic/start` | Start auto traffic |
| POST | `/api/traffic/stop` | Stop traffic |
| POST | `/api/server/toggle?id=1&up=true` | Toggle server up/down |
| POST | `/api/routing?algo=ROUND_ROBIN` | Set routing algorithm |
| POST | `/api/config?dropRate=0.1&minLatency=50&maxLatency=300` | Update simulation config |
| GET | `/api/metrics` | Get current metrics |
| GET | `/api/server/status` | Get server up/down status |
| GET | `/api/logs` | Get system logs |
| GET | `/api/flows` | Get packet flow events |
| WS | `/ws` | WebSocket endpoint (STOMP) |

---

## Docker Setup

```yaml
services:
  router:   NODE_ROLE=router   # Runs router + REST API + frontend
  server1:  NODE_ROLE=server1  # Runs Server 1 on port 6001
  server2:  NODE_ROLE=server2  # Runs Server 2 on port 6002
```

All containers communicate over `sim-network` (Docker bridge network).

---

## Running Tests

```bash
mvn test
```

**37 tests, 0 failures:**
- `MetricsStoreTest` — 9 tests (counters, latency, thread safety)
- `SimulationConfigTest` — 7 tests (validation, defaults, edge cases)
- `RoutingConfigTest` — 5 tests (enum switching, case insensitive, invalid input)
- `PacketQueueTest` — 6 tests (enqueue, dequeue, capacity, timeout)
- `SimulatorControllerTest` — 9 tests (REST endpoints)
- `AppTest` — 1 test (Spring context loads)

---

## Improvements Roadmap

- [ ] Heartbeat / auto fault detection
- [ ] Circuit Breaker pattern (open/half-open/closed)
- [ ] Persistent metrics with H2 database
- [ ] JWT authentication for API endpoints
- [ ] Multi-region router simulation

---

## Author

Navneet Singh
MS Computer Science  
DePaul University
