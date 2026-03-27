# Distributed Network Simulator

A production-grade distributed systems simulator built with Java, Spring Boot, and Docker. Simulates a real network with a router, multiple servers, packet queuing, fault tolerance, and live metrics — all running as **separate Docker containers** communicating over a virtual network. Deployed on AWS EC2 with CI/CD via GitHub Actions.

---

##  Live Demo

**[http://3.142.129.218:8080](http://3.142.129.218:8080)** — Deployed on AWS EC2 (t2.micro) with Docker Compose

---

## Architecture

```
┌─────────────┐        ┌──────────────────────────┐        ┌─────────────┐
│   CLIENT    │        │         ROUTER           │        │  SERVER 1   │
│  (Traffic   │──────▶ │  - BlockingQueue (100)   │──────▶ │  (Port 6001)│
│ Simulator)  │        │  - Retry logic (3x)      │        └─────────────┘
└─────────────┘        │  - Round Robin / Least   │
                       │    Load routing          │──────▶ ┌─────────────┐
                       │  - Drop simulation       │        │  SERVER 2   │
                       │  - Latency simulation    │        │  (Port 6002)│
                       └──────────────────────────┘        └─────────────┘
                                    │
                       ┌────────────▼─────────────┐
                       │     Spring Boot API      │
                       │  - REST endpoints        │
                       │  - WebSocket (STOMP)     │
                       │  - Live dashboard        │
                       └──────────────────────────┘
```

Each node runs as a **separate Docker container** on a shared virtual network (`sim-network`), making this a genuinely distributed system — not just threads in a single JVM.

---

## Features

### Distributed Systems
- **3 separate Docker containers** — router, server1, server2 each run their own JVM
- **Concurrent packet handling** — router uses `ExecutorService` thread pool
- **Bounded BlockingQueue** — packets buffered (capacity 100) instead of dropped under load
- **Retry logic** — up to 3 attempts with 200ms delay before dropping a packet
- **Dynamic routing algorithms** — Round Robin and Least Load, switchable at runtime
- **Server fault tolerance** — toggle servers down, router automatically reroutes
- **Configurable packet drop rate** — simulates real network packet loss
- **Configurable latency** — simulates real network delays

### Observability & Dashboard
- **WebSocket live metrics** — STOMP push every second (no polling)
- **Real-time throughput chart** — packets/sec visualization
- **Server health indicators** — ONLINE/OFFLINE with glow effect
- **Color-coded system logs** — INFO / WARN / ERROR
- **Network flow animation** — live packet movement between nodes
- **Queue size progress bar** — visual fill indicator (green → yellow → red)
- **Metrics reset** — reset all counters with confirmation dialog
- **Dark / Light mode** — preference persisted in localStorage

### Engineering Quality
- **Spring constructor injection** — no static mutable state anywhere
- **Type-safe config** — `RoutingConfig.Algorithm` enum, validated setters
- **37 unit + integration tests** — zero failures
- **Health check endpoint** — `/api/health` for monitoring
- **Structured logging** — configurable log levels and file rotation
- **CI/CD pipeline** — GitHub Actions runs tests on every push, auto-deploys on merge to main

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot 3.3.2 |
| WebSocket | Spring WebSocket + STOMP + SockJS |
| Containerization | Docker, Docker Compose |
| Cloud | AWS EC2 (t2.micro), Elastic IP |
| CI/CD | GitHub Actions |
| Frontend | Vanilla JS, Chart.js, SockJS, STOMP.js |
| Fonts | JetBrains Mono, Space Grotesk |
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
cd distributed-network-simulator
docker-compose up --build
```

Open **http://localhost:8080** in your browser.

All 3 nodes start automatically — router, server1, and server2 each launch in their own container.

### Run Locally (without Docker)

```bash
mvn clean spring-boot:run
```

Open **http://localhost:8080**, then manually click **Start Router → Start S1 → Start S2 → Start Traffic**.

---

## How It Works

### Packet Flow
```
TrafficSimulator
      │
      ▼ TCP Socket (port 5000)
   Router
      │
      ├── Drop simulation (configurable %)
      ├── Enqueue → PacketQueue (BlockingQueue, capacity 100)
      │
      ▼ Dispatcher Thread
   getNextServer() ──→ Round Robin or Least Load
      │
      ├── Retry up to 3x (200ms between attempts)
      │
      ▼
 Server1 or Server2
      │
      ▼ HTTP callback
   Router MetricsStore (centralized metrics)
```

### Routing Algorithms

| Algorithm | Description |
|---|---|
| **Round Robin** | Alternates between Server1 and Server2 evenly |
| **Least Load** | Sends to the server with fewer handled packets |

### Fault Tolerance
- Toggle any server **DOWN** via the dashboard
- Router automatically detects unavailable servers and reroutes
- Packets queue up during outages and are delivered on retry
- Server status reflected live in the dashboard with color indicators

---

## Project Structure

```
src/main/java/sim/
├── SimulatorApplication.java    # Entry point — auto-starts node based on NODE_ROLE
├── client/
│   ├── ClientNode.java          # Interactive CLI client
│   └── TrafficSimulator.java    # Automated traffic generator
├── config/
│   ├── Ports.java               # Port constants + host resolution
│   ├── RoutingConfig.java       # Algorithm enum (ROUND_ROBIN / LEAST_LOAD)
│   ├── ServerStatusConfig.java  # Server up/down state
│   ├── SimulationConfig.java    # Drop rate, latency with validation
│   └── WebSocketConfig.java     # STOMP WebSocket configuration
├── controller/
│   ├── MetricsController.java   # GET /api/metrics, /api/logs, /api/health, POST /api/metrics/reset
│   └── SimulatorController.java # POST endpoints for control
├── metrics/
│   ├── LogStore.java            # In-memory log buffer
│   ├── MetricsStore.java        # Thread-safe metrics bean with reset()
│   └── PacketFlowStore.java     # Packet flow event store
├── model/
│   └── Packet.java              # Packet model with serialize/deserialize
├── osi/
│   └── OsiStack.java            # OSI layer encapsulation simulation
├── router/
│   ├── PacketQueue.java         # Bounded BlockingQueue (capacity 100)
│   └── Router.java              # Core router — thread pool + retry logic
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
| POST | `/api/metrics/reset` | Reset all metrics to zero |
| GET | `/api/metrics` | Get current metrics |
| GET | `/api/health` | Health check endpoint |
| GET | `/api/server/status` | Get server up/down status |
| GET | `/api/logs` | Get system logs |
| GET | `/api/flows` | Get packet flow events |
| WS | `/ws` | WebSocket endpoint (STOMP over SockJS) |

---

## Docker Setup

```yaml
services:
  router:   NODE_ROLE=router   # Runs router + REST API + frontend (port 8080)
  server1:  NODE_ROLE=server1  # Runs Server 1 on port 6001
  server2:  NODE_ROLE=server2  # Runs Server 2 on port 6002
```

All containers communicate over `sim-network` (Docker bridge network). The router resolves server hostnames via `SERVER1_HOST` / `SERVER2_HOST` environment variables.

---

## CI/CD Pipeline

```
Push to any branch  →  GitHub Actions CI  →  Run 37 tests
Push to main        →  GitHub Actions CD  →  SSH into EC2 → git pull → docker-compose up --build -d
```

Every merge to `main` automatically deploys to AWS EC2 within ~5 minutes.

---

## Running Tests

```bash
mvn test
```

**37 tests, 0 failures:**

| Test Class | Tests | What it covers |
|---|---|---|
| `MetricsStoreTest` | 9 | Counters, latency, thread safety, reset |
| `SimulationConfigTest` | 7 | Validation, defaults, edge cases |
| `RoutingConfigTest` | 5 | Enum switching, case insensitive, invalid input |
| `PacketQueueTest` | 6 | Enqueue, dequeue, capacity, timeout |
| `SimulatorControllerTest` | 9 | REST endpoints, routing, config, status |
| `AppTest` | 1 | Full Spring context loads |

---

## Roadmap

- [ ] Heartbeat / auto fault detection
- [ ] Circuit Breaker pattern (open/half-open/closed states)
- [ ] Persistent metrics with H2 database
- [ ] JWT authentication for API endpoints
- [ ] Multi-region router simulation

---

## Author

**Navneet Singh**
MS Computer Science
[LinkedIn](https://www.linkedin.com/in/navneet-kumar-singh-842429154) · [GitHub](https://github.com/NavneetSingh09) · [Portfolio](https://navneet-portfolio-rho.vercel.app/)
