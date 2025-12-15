# Stage 3 — The Almost Honor Students

This project corresponds to **Stage 3** of the *Project Gutenberg Book Search Engine*, developed using a modular and decoupled architecture.  

---

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Implemented Functionality](#2-implemented-functionality)  
3. [Environment Variables Configuration](#3-environment-variables-configuration)  
   - 3.1 [Crawler Service](#crawler-service)  
   - 3.2 [Indexing Service](#indexing-service)  
   - 3.3 [Search Service](#search-service)  
4. [Building the Project](#4-building-the-project)  
5. [Running with Docker](#5-running-with-docker)  
6. [Example Usage and Test Queries](#6-example-usage-and-test-queries)  
7. [Laboratory Deployment Procedure: Distributed Cluster Setup](#7-laboratory-deployment-procedure-distributed-cluster-setup)
   - 7.1 [Cluster Node Configuration](#71-cluster-node-configuration)
   - 7.2 [Prerequisites](#72-prerequisites)
   - 7.3 [Environment Setup](#73-environment-setup)
   - 7.4 [Service Deployment](#74-service-deployment)
   - 7.5 [Datalake Configuration and Synchronization](#75-datalake-configuration-and-synchronization)
   - 7.6 [System Verification and Monitoring](#76-system-verification-and-monitoring)
   - 7.7 [Observability Verification: Metrics, Traces and Logs](#77-observability-verification-metrics-traces-and-logs)
   - 7.8 [Load Testing with Locust](#78-load-testing-with-locust)
   - 7.9 [Functional Search Service Test](#79-functional-search-service-test)
   - 7.10 [Fault Tolerance Test (Failover)](#710-fault-tolerance-test-failover)
   - 7.11 [Architecture Components](#711-architecture-components)  

---

## 1. Architecture Overview

Each service runs as an independent module with its own HTTP server (based on **Javalin**). Configuration can be managed through optional `.env` files or environment variables passed via Docker.  
The services communicate through **REST APIs**, and **MongoDB** acts as the main storage system for both metadata and inverted indexes.

The project follows **Clean Architecture / Hexagonal Architecture** principles, with clear separation of responsibilities:

- `application` → business logic and use cases.  
- `infrastructure` → external adapters (MongoDB, HTTP, S3/local FS).  
- `domain` → core entities of the data model.

---

## 2. Implemented Functionality

- Modular implementation of **Crawler**, **Indexing**, and **Search** services  
- Event-driven architecture using **ActiveMQ Artemis** clustering  
- Distributed caching with **Hazelcast** for high-performance search  
- REST APIs built with **Javalin**  
- Flexible configuration via environment variables (supports both `.env` files and Docker environment variables)  
- Persistent data storage in **MongoDB** (`metadata` and `inverted_index` collections)  
- Text preprocessing and tokenization for indexing  
- Full Docker containerization with multi-node deployment support  
- High availability and fault tolerance through clustering  
- Comprehensive observability with **OpenTelemetry**, **Prometheus**, **Jaeger**, **Loki**, and **Grafana**
  
---

## 3. Environment Variables Configuration

The services support two configuration methods:
- **`.env` files** (optional): For local development, place `.env` files inside each service's `resources/` directory.  
- **Docker environment variables** (recommended): When using Docker, all variables are defined in `docker-compose.yaml` and passed automatically.

**Note**: If a `.env` file is not found, the services automatically fallback to system environment variables, making `.env` files **optional when using Docker**.

### Configuration Variables by Service

### Crawler Service

**Purpose:** Downloads books from Project Gutenberg and publishes events to ActiveMQ for indexing.  
**File:** `resources/.env`

```env
URL_GUTENBERG=https://www.gutenberg.org
PORT=7070
ACTIVEMQ_URL=failover:(tcp://10.26.14.221:61616,tcp://10.26.14.222:61616,tcp://10.26.14.223:61616)
```

---

### Indexing Service

**Purpose:** Processes the downloaded books, extracts metadata, generates an inverted index, and stores the results in MongoDB.  
**File:** `resources/.env`

```env
MONGO_URL=mongodb://localhost:27017
DATABASE_NAME=books
COLLECTION_METADATA=metadata
COLLECTION_INDEX=inverted_index
PORT=8080
```

---

### Search Service

**Purpose:** Performs full-text searches over the inverted index and returns results through REST API responses.  
**File:** `resources/.env`

```env
MONGO_URL=mongodb://localhost:27017
DATABASE_NAME=books
COLLECTION_METADATA=metadata
COLLECTION_INDEX=inverted_index
PORT=9090
```

---

> **💡 When are `.env` files needed?**  
> - **Running with Docker** → `.env` files are **NOT needed**. Docker Compose passes all variables automatically.  
> - **Running locally without Docker** → Create `.env` files in `resources/` directory with the variables shown above.

---

## 4. Building the Project

Each microservice is built using **Maven**.

To build all services from the project root:

```bash
mvn clean package -DskipTests
```

To build a specific service (for example, `indexing`):

```bash
cd indexing
mvn clean package -DskipTests
```

This will generate a JAR file inside each module’s `target` directory.

---

## 5. Running with Docker

All services are **dockerized** and can be executed independently or together using `docker-compose`.

### Data Persistence
The MongoDB container is configured to persist data locally in the `mongo_data` directory at the project root. This ensures that your database content survives container restarts.

### Build Docker Images for ARM and AMD64

```bash
docker buildx build \
--platform linux/amd64,linux/arm64 \
-t giselabcr8888/mi-app:latest \
--push \
.
```
### Steps to Run

1. **Set the NODE_IP environment variable** (required for multi-node deployment):

   ```bash
   export NODE_IP=10.26.14.223  # Use the IP of your current node
   ```

   > **Note**: `.env` files are **NOT required** when using Docker. All environment variables are defined in `docker-compose.yaml` and passed automatically to the containers.

1. From the project root directory, run:

   ```bash
   docker-compose up --build -d
   ```


2. Once the containers are running, the services will be available at:
   - Crawler → `http://localhost:7070`
   - Indexing → `http://localhost:8080`
   - Search → `http://localhost:9090`

---

## 6. Example Usage and Test Queries

Once the system is running, the services can be tested with tools like **curl** or **Postman** (recommended for easier visualization and testing).  
Below is a list of all available endpoints and example calls for each one. In case you use Postman, ignore curl calls, just take the examples, like http://localhost:7070/ingest/6036.  

### Available Endpoints

| Service      | Method | Endpoint                        | Description |
|---------------|---------|----------------------------------|-------------|
| **Search**    | GET     | `/search?q=<keyword>`           | Searches for a specific keyword in the inverted index. |

### Example Queries

#### Search for a Keyword
Example for the word "love":

```bash
curl "http://localhost:9090/search?q=love"
```

Or via the Nginx load balancer:

```bash
curl "http://localhost:8000/search?q=love"
```

---

## 7. Laboratory Deployment Procedure: Distributed Cluster Setup

### 7.1 Cluster Node Configuration

The distributed system is deployed across **three laboratory nodes**. The final digit of the IP determines the node number:

| **Node** | **IP Address** | **Role** |
|----------|----------------|----------|
| **Node 1** | `10.26.14.221` | Microservices (Crawler, Indexer, Search) + ActiveMQ |
| **Node 2** | `10.26.14.222` | Microservices + Transversal Services (MongoDB, Nginx, Prometheus, Jaeger, Loki, Grafana, OTel Collector) + ActiveMQ |
| **Node 3** | `10.26.14.223` | Microservices (Crawler, Indexer, Search) + ActiveMQ |

### 7.2 Prerequisites

- **Docker** and **Docker Compose** installed on all nodes
- Network connectivity between nodes on the following ports:
  - **5701** (Hazelcast distributed cache)
  - **61616** (ActiveMQ Artemis broker)
  - **9090** (Search Service)
  - **8080** (Indexer Service)
  - **7070** (Crawler Service)
  - **8000** (NGINX Load Balancer)
  - **27017** (MongoDB)
  - **3000** (Grafana)
  - **4317/4318** (OpenTelemetry Collector)
  - **16686** (Jaeger UI)
  - **1010** (Prometheus)
  - **3100** (Loki)

### 7.3 Environment Setup

#### 7.3.1 Python Environment Preparation for Load Testing

Before executing Locust in the laboratory machines, install it at user-level to avoid permission issues:

```bash
# Install Locust using the --user flag
pip install locust --user

# Verify Locust installation (optional)
python -m locust --version
```

### 7.4 Service Deployment

#### 7.4.1 Node-Specific Services Deployment

On each of the three cluster nodes, execute the `docker compose` command to deploy the microservices specific to that node (Crawler, Indexer, Search, ActiveMQ):

**On Node 1 (10.26.14.221):**
```bash
docker compose -f docker-compose-node1.yaml up -d
```

**On Node 2 (10.26.14.222):**
```bash
docker compose -f docker-compose-node2.yaml up -d
```

**On Node 3 (10.26.14.223):**
```bash
docker compose -f docker-compose-node3.yaml up -d
```

#### 7.4.2 Transversal Services Deployment (Node 2 Only)

The shared infrastructure services (**MongoDB**, **Nginx Load Balancer**, **Prometheus**, **Jaeger**, **Loki**, **Grafana**, and **OpenTelemetry Collector**) are deployed centrally on **Node 2**.

```bash
# Navigate to the transversal services directory
cd transversal_services

# Execute the deployment of transversal services
docker compose up -d
```

At this point, all microservices and transversal services should be deployed and running across the cluster.

#### 7.4.3 Docker Images Used

The following multi-architecture Docker images (AMD64 and ARM64) are published on Docker Hub:

- **Crawler Service**: `giselabcr8888/crawler:1.2.0`
- **Indexer Service**: `giselabcr8888/indexer:1.1.0`
- **Search Service**: `giselabcr8888/search:1.2.1`

All images are available at: https://hub.docker.com/repositories/giselabcr8888

### 7.5 Datalake Configuration and Synchronization

#### 7.5.1 Execution Policy Configuration (Windows/PowerShell)

Before executing the configuration script on Windows, modify the PowerShell execution policy:

```powershell
Set-ExecutionPolicy RemoteSigned -Scope Process
```

#### 7.5.2 Syncthing Configuration Script Execution

To configure Syncthing in the laboratory machines, execute the script with an ExecutionPolicy bypass:

```powershell
powershell -ExecutionPolicy ByPass -file .\configure_syncthing.ps1
```

Or simply:

```bash
./configure_syncthing.ps1
```

#### 7.5.3 Accepting Datalake Connections

Ensure that the network or firewall configurations on all three machines allow the necessary connections for sharing the datalake folder, so that:
- All containers can access the books
- Cluster members (Hazelcast and ActiveMQ) can communicate effectively

### 7.6 System Verification and Monitoring

After deploying and configuring the services, verify their correct operation by reviewing the container logs:

```bash
# Verify Crawler Logs
docker logs <crawler_container_id> -f

# Verify Indexer Logs
docker logs <indexer_container_id> -f

# Verify Search Service Logs
docker logs <search_container_id> -f
```

**Verify MongoDB Express:**  
Access the MongoDB Express interface to verify the persistence of metadata and the index:
```
http://10.26.14.222:8081
```

### 7.7 Observability Verification: Metrics, Traces and Logs

Once the transversal services are deployed on Node 2, verify that the monitoring stack is functioning correctly. The observability pipeline consists of:

- **Prometheus** – collects metrics from microservices
- **Jaeger** – stores and displays distributed traces
- **Loki** – aggregates logs from all containers
- **Grafana** – unified dashboard for visualizing metrics, traces and logs

#### 7.7.1 Accessing Grafana

Grafana runs at:
```
http://10.26.14.222:3000
```

**Default login:**
- User: `admin`
- Password: `admin`

Three data sources appear preconfigured:
- **Prometheus** (metrics)
- **Loki** (logs)
- **Jaeger** (traces)

#### 7.7.2 Importing the Search Dashboard in Grafana

In the laboratory setup, the Search Service dashboard can be imported manually:

1. Open Grafana: `http://10.26.14.222:3000`
2. Navigate to: **Dashboards** → **New** → **Import**
3. Upload the file: `search_service.json`
4. Select the Prometheus/Loki data sources if Grafana requests mapping
5. Click **Import** to create the dashboard

#### 7.7.3 What Must Be Validated

- **Metrics**: Prometheus panels show activity from Search, Crawler, Indexer
- **Traces**: Jaeger shows multi-service traces from the `/search` endpoint
- **Logs**: Loki correctly displays logs from all microservices

### 7.8 Load Testing with Locust

To evaluate the cluster's performance under stress, Locust is used to simulate high concurrency directed at the Nginx load balancer.

```bash
# Navigate to the load-testing folder
cd load-testing

# Run Locust (laboratory execution)
python -m locust -f locustfile.py
```

Access the Locust web interface on port **8089** of the host machine:
```
http://localhost:8089
```

**Configure and run the load test:**
- **Number of Users**: 500 concurrent users
- **Spawn Rate**: 1 user/s (or the value used in the lab)
- **Host**: `http://localhost:8000/search`

**Monitor key metrics** such as:
- Requests/s
- 95th Percentile (P95) latency

The objective of this test is to stress the entrypoint (**Nginx** on port `8000`), which is responsible for distributing the load across the Search Service replicas in the cluster.

### 7.9 Functional Search Service Test

Perform a manual search test using a browser:

```
http://localhost:9090/search?q=poems
```

Verify that the response is correct and originated from one of the Search Services in the cluster.

### 7.10 Fault Tolerance Test (Failover)

To verify the High Availability and fault tolerance of the distributed system:

1. **Select a node** (e.g., Node 1) and stop its microservice containers:
   ```bash
   # On Node 1 machine
   docker compose -f docker-compose-node1.yaml stop
   ```

2. **Verification:**
   - Observe the logs of the remaining nodes (Node 2 and 3) to confirm the ActiveMQ and Hazelcast reconnections
   - Repeat the functional search test (`/search?q=Love`) to confirm that the system remains operational without interruptions, with traffic successfully being redirected to the remaining nodes

### 7.11 Architecture Components

- **Hazelcast**: Forms a TCP/IP cluster across all 3 nodes for distributed caching of the inverted index
- **ActiveMQ Artemis**: Configured as a 3-node cluster with message redistribution (`redistribution-delay=0`), allowing seamless message propagation between nodes
- **NGINX**: Distributes search traffic across the available Search Service instances using a `least_conn` strategy
- **MongoDB**: Centralized persistent storage for book metadata and inverted index
- **OpenTelemetry Stack**: Comprehensive observability with metrics (Prometheus), traces (Jaeger), and logs (Loki), all visualized in Grafana
