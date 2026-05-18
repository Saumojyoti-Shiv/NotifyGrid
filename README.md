<div align="center">

# 🌌 NotifyGrid

**Enterprise-grade bulk messaging architecture, simplified.**

[![Java](https://img.shields.io/badge/Java-23-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/projects/jdk/23/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-4.0.6-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-black?style=for-the-badge)](LICENSE)

---

NotifyGrid is a high-performance, microservices-based Bulk SMS Service System designed for reliability, scalability, and ease of integration.

[Features](#-key-pillars) • [Architecture](#-architecture) • [Quick Start](#-getting-started) • [Demo](#-quick-demo)

</div>

## ✨ Key Pillars

<div align="center">

| 🛡️ Secure | 🚀 Scalable | 📊 Insightful |
| :--- | :--- | :--- |
| JWT-based RBAC and encrypted communication channels. | Independent microservices ready for horizontal growth. | Real-time delivery reports and billing tracking. |

</div>

---

## 🏗️ Architecture

NotifyGrid follows a decoupled microservices architecture coordinated via a centralized Discovery Server and API Gateway.

```mermaid
graph TD
    Client[Web/API Client] --> Gateway[API Gateway]
    
    subgraph "Core Services"
        Gateway --> Auth[Auth Service]
        Gateway --> Campaign[Campaign Service]
        Gateway --> Contact[Contact Service]
    end
    
    subgraph "Scheduling & Processing"
        Campaign --> Scheduler[Scheduler Service]
        Scheduler --> MQ[RabbitMQ]
        MQ --> Msg[Messaging Service]
        Msg --> Provider[External SMS Gateway]
    end
    
    subgraph "Monitoring & Notifications"
        Provider --> Report[Delivery Report Service]
        Report --> Dashboard[Real-time Dashboard]
        Msg --> Billing[Billing Service]
        Report --> Notify[Notification Service]
        Notify --> Client
    end
    
    subgraph "Infrastructure"
        Services[All Services] --> Eureka[Discovery Server]
        Services --> DB[(PostgreSQL)]
    end
```

## 🛠️ Tech Stack

- **Language:** Java 23 (JDK 23)
- **Framework:** Spring Boot 4.0.6, Spring Cloud 2025.1.1
- **Messaging:** RabbitMQ (Asynchronous processing)
- **Database:** PostgreSQL (Relational persistence)
- **DevOps:** Docker & Docker Compose
- **Scripting:** Python 3.x (System Orchestration)

### 📡 Service Map

| Service | Port | Responsibility |
| :--- | :--- | :--- |
| **Discovery Server** | `8761` | Service registration and discovery (Eureka) |
| **API Gateway** | `8080` | Centralized request routing |
| **Auth Service** | `8081` | JWT-based authentication and security |
| **Campaign Service** | `8084` | Campaign lifecycle and scheduling |
| **Scheduler Service** | `8089` | Triggering scheduled campaigns |
| **Messaging Service** | `8085` | Queue processing and provider integration |
| **Notification Service** | `8088` | Alerts and completion notifications |
| **Frontend** | `8000` | Management Dashboard |

---

