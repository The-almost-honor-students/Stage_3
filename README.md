# Stage 3 — The Almost Honor Students

This project corresponds to **Stage 3** of the *Project Gutenberg Book Search Engine*, developed using a modular and decoupled architecture.  
In this stage, four independent services —**Ingestion**, **Indexing**, **Search**, and **Control**— were implemented to handle the complete data flow from book retrieval to indexed search and performance benchmarking.

---

## Table of Contents
1. [Architecture Overview](#1-architecture-overview)
2. [Implemented Functionality](#2-implemented-functionality)  
3. [Environment Variables Configuration](#3-environment-variables-configuration)  
 2.1 [Ingestion Service](#ingestion-service)  
 2.2 [Indexing Service](#indexing-service)  
 2.3 [Search Service](#search-service)  
 2.4 [Control Service](#control-service)  
4. [Building the Project](#4-building-the-project)  
5. [Running with Docker](#5-running-with-docker)  
6. [Example Usage and Test Queries](#6-example-usage-and-test-queries)  
7. [Benchmarking](#7-benchmarking)  

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

- Modular implementation of **Ingestion**, **Indexing**, **Search**, and **Control** services.  
- REST APIs built with **Javalin**.  
- Flexible configuration via environment variables (supports both `.env` files and Docker environment variables).  
- Persistent data storage in **MongoDB** (`metadata` and `inverted_index` collections).  
- Text preprocessing and tokenization for indexing.  
- Workflow orchestration through the **Control Service**.  
- Integration of **JMH** benchmarking in all modules.  
- Full Docker containerization with `docker-compose`.  
- Clear and incremental **Git history** showing contributions and progress.ç
  
---

## 3. Environment Variables Configuration

The services support two configuration methods:
- **`.env` files** (optional): For local development, place `.env` files inside each service's `resources/` directory.  
- **Docker environment variables** (recommended): When using Docker, all variables are defined in `docker-compose.yaml` and passed automatically.

**Note**: If a `.env` file is not found, the services automatically fallback to system environment variables, making `.env` files **optional when using Docker**.

### Configuration Variables by Service

### Ingestion Service

**Purpose:** Downloads books from Project Gutenberg, stores them temporarily, and forwards them to the Indexing service.  
**File:** `resources/.env`

```env
URL_GUTENBERG=https://www.gutenberg.org
PORT=7070
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

### Control Service

**Purpose:** Orchestrates the workflow by coordinating ingestion, indexing, and search services.  
It also triggers benchmarking routines using **JMH** to evaluate performance.  
**File:** `resources/.env`

```env
INGESTION_URL=http://localhost:7070
INDEXING_URL=http://localhost:8080
SEARCH_URL=http://localhost:9090
```


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
   - Ingestion → `http://localhost:7070`
   - Indexing → `http://localhost:8080`
   - Search → `http://localhost:9090`
   - Control → `http://localhost:6060`

---

## 6. Example Usage and Test Queries

Once the system is running, the services can be tested with tools like **curl** or **Postman** (recommended for easier visualization and testing).  
Below is a list of all available endpoints and example calls for each one. In case you use Postman, ignore curl calls, just take the examples, like http://localhost:7070/ingest/6036.  

### Available Endpoints

| Service      | Method | Endpoint                        | Description |
|---------------|---------|----------------------------------|-------------|
| **Ingestion** | POST    | `/ingest/{bookId}`              | Downloads a specific book from Project Gutenberg by ID and prepares it for indexing. |
| **Ingestion** | GET     | `/ingest/list`                  | Returns the list of ingested books. |
| **Ingestion** | GET     | `/ingest/status/{bookId}`       | Returns the ingestion status for a specific book. |
| **Indexing**  | POST    | `/index/update/{bookId}`        | Processes and indexes the specified book. |
| **Search**    | GET     | `/search?query=<keyword>`       | Searches for a specific keyword in the inverted index. |

### Example Queries

#### Ingest a Book
Example for book ID 6036:

```bash
curl -X POST http://localhost:7070/ingest/6036
```

#### Get the Ingested Book List

```bash
curl http://localhost:7070/ingest/list
```

#### Check Ingestion Status of a Book
Example for book ID 6036:

```bash
curl http://localhost:7070/ingest/status/6036
```

#### Index a Book
Example for book ID 6036:

```bash
curl -X POST "http://localhost:8080/index/update/6036"
```

#### Search for a Keyword
Example for the word “love”:

```bash
curl "http://localhost:9090/search?query=love"
```

#### Run the Complete Workflow via Control Service

```bash
curl -X POST http://localhost:6060/control/start
```

---

## 7. Benchmarking

Performance benchmarking has been implemented in **all services** — Ingestion, Indexing, Search, and Control — using **JMH (Java Microbenchmark Harness)**.  
Each benchmark evaluates throughput, latency, and scalability under different workloads.

### Location

Benchmarks are distributed across the modules:
- **Control (End-to-End)**: `control/src/main/java/com/tahs/benchmark/`
- **Ingestion**: `crawler/src/main/java/com/tahs/benchmark/`
- **Indexing**: `indexer/src/main/java/com/tahs/benchmark/`
- **Search**: `search/src/main/java/com/tahs/benchmark/`

### How to Execute

#### 1. End-to-End Benchmark (via Control)

1. Ensure all services are **running via Docker Compose**:

   ```bash
   docker-compose up -d
   ```

2. Once the containers are up and connected, execute the benchmark from the **Control module**:

   ```bash
   cd control/src/main/java/com/tahs
   java Main
   ```

This will automatically perform end-to-end benchmarking of ingestion, indexing, and search through the orchestrator.

Benchmark results are printed to the console and may include:
- Operations per second (ops/s)
- Execution time per thread
- CPU and memory usage across services





---

## 8. Cluster Deployment

The system is configured to run as a distributed cluster across three nodes:
- **Node 1**: 10.26.14.223 (Mongo + ActiveMQ + Hazelcast + Ingestion + Indexing + Search)
- **Node 2**: 10.26.14.222 (ActiveMQ + Hazelcast + Ingestion + Indexing + Search)
- **Node 3**: 10.26.14.221 (ActiveMQ + Hazelcast + Ingestion + Indexing + Search)

### Prerequisites
- Docker and Docker Compose installed on all nodes.
- Network connectivity between nodes on ports:
  - **5701** (Hazelcast)
  - **61616** (ActiveMQ)
  - **9090** (Search Service)
  - **8000** (NGINX Load Balancer)

### Deployment Steps

1. **Set Environment Variables**
   On each node, set the `NODE_IP` variable before running docker-compose:
   ```bash
   export NODE_IP=<current_node_ip>  # e.g., 10.26.14.223
   ```

2. **Deploy to Nodes**
   Use the provided deployment script to deploy to each node:
   ```bash
   ./deployment/deploy-node.sh <node_ip>
   ```
   Or manually copy the project and run:
   ```bash
   docker-compose -f docker-compose-cluster.yaml up -d
   ```

3. **Start Load Balancer**
   On the machine hosting NGINX (can be any node or a separate one):
   ```bash
   cd nginx
   docker-compose up -d
   ```
   The search service will be available at `http://<nginx-ip>:8000/search`.

### Architecture
- **Hazelcast**: Forms a TCP/IP cluster across all 3 nodes for distributed caching of the inverted index.
- **ActiveMQ**: Configured as a Network of Brokers, allowing seamless message propagation between nodes.
- **NGINX**: Distributes search traffic across the available Search Service instances using a `least_conn` strategy.
