<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=Fira+Code&size=32&pause=1000&color=22C55E&center=true&vCenter=true&width=700&lines=🌱+Carbon+Credit+Platform;AI-Powered+Emission+Verification;Peer-to-Peer+Carbon+Trading" alt="Typing SVG" />

<br/>

<p>
  <strong>A full-stack enterprise platform for carbon emission reporting, AI-powered verification, and secure peer-to-peer carbon credit trading.</strong>
</p>

<br/>

<p>
  <img src="https://img.shields.io/badge/Version-2.0.0-22c55e?style=for-the-badge&logo=github" alt="Version"/>
  <img src="https://img.shields.io/badge/Spring_Boot-4.0.3-6DB33F?style=for-the-badge&logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/React-19-61DAFB?style=for-the-badge&logo=react&logoColor=black" alt="React"/>
  <img src="https://img.shields.io/badge/FastAPI-Python_3.13-009688?style=for-the-badge&logo=fastapi&logoColor=white" alt="FastAPI"/>
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" alt="MySQL"/>
  <img src="https://img.shields.io/badge/Java-24-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java"/>
</p>

<p>
  <img src="https://img.shields.io/badge/License-MIT-blue?style=flat-square" alt="License"/>
  <img src="https://img.shields.io/badge/PRs-Welcome-brightgreen?style=flat-square" alt="PRs Welcome"/>
  <img src="https://img.shields.io/badge/Build-Passing-success?style=flat-square" alt="Build"/>
  <img src="https://img.shields.io/badge/AI_Engine-Active-9c27b0?style=flat-square" alt="AI Engine"/>
</p>

</div>

---

## 📖 Overview

The **Carbon Credit Trading Platform** is a production-grade, enterprise-level application that empowers companies to participate in a transparent and intelligent carbon credit ecosystem. Companies can register, submit CO₂ emission reports, receive AI-verified carbon scores, and trade credits securely in a peer-to-peer marketplace.

> **Version 2.0** introduces a full **AI Intelligence Suite** — predicting future emissions, recommending dynamic credit pricing, and detecting suspicious transactions using machine learning.

---

## ✨ Features at a Glance

<table>
  <tr>
    <td>
      <h3>🏢 Core Platform</h3>
      <ul>
        <li>🔐 <strong>Role-Based Access Control</strong> — Admin, Seller, Buyer roles</li>
        <li>📊 <strong>Emission Reporting</strong> — Submit CO₂ data for AI verification</li>
        <li>🤝 <strong>P2P Carbon Marketplace</strong> — Secure credit transfers between companies</li>
        <li>🛡️ <strong>JWT Authentication</strong> — Stateless, token-based security</li>
        <li>📋 <strong>Admin Dashboard</strong> — Company registry and credit assignment</li>
        <li>📜 <strong>Transaction History</strong> — Role-filtered trade audit trail</li>
      </ul>
    </td>
    <td>
      <h3>🤖 AI Intelligence Suite (v2.0)</h3>
      <ul>
        <li>📈 <strong>Emission Prediction</strong> — Linear Regression forecasts future CO₂ output</li>
        <li>💰 <strong>Dynamic Pricing</strong> — Real-time price recommendations based on supply and demand</li>
        <li>🕵️ <strong>Fraud Detection</strong> — Isolation Forest anomaly detection for suspicious trades</li>
        <li>✅ <strong>Emission Verification</strong> — Automated carbon score assignment</li>
        <li>🔌 <strong>Offline Fallback</strong> — Platform operates even if AI engine is down</li>
      </ul>
    </td>
  </tr>
</table>

---

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                                                         │
│   🌐  React Frontend   (Port 3000)                      │
│        Axios · Recharts · JWT decode                    │
│                      │                                  │
│                       ▼  HTTP / REST API                │
│   ⚙️  Spring Boot Backend   (Port 8080)                 │
│        JPA · Hibernate · Tomcat · JWT Security          │
│         │                          │                    │
│         ▼                          ▼                    │
│  🗄️  MySQL Database          🤖  FastAPI AI Engine       │
│     (Port 3306)                 (Port 8000)             │
│    carbon_db                   /verify · /predict       │
│                                /price  · /detect        │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

```mermaid
graph TD
    A["🌐 React UI<br/>localhost:3000"] -->|HTTP Axios| B["⚙️ Spring Boot<br/>localhost:8080"]
    B -->|JPA / Hibernate| C[("🗄️ MySQL DB<br/>carbon_db")]
    B <-->|RestTemplate| D["🤖 FastAPI AI Engine<br/>localhost:8000"]
    D -->|scikit-learn| E["📊 ML Models<br/>LinearRegression · IsolationForest"]

    classDef frontend fill:#61DAFB,stroke:#0891b2,stroke-width:2px,color:#000
    classDef backend fill:#6DB33F,stroke:#166534,stroke-width:2px,color:#fff
    classDef db fill:#4479A1,stroke:#1e3a5f,stroke-width:2px,color:#fff
    classDef ai fill:#7c3aed,stroke:#4c1d95,stroke-width:2px,color:#fff
    classDef ml fill:#d97706,stroke:#78350f,stroke-width:2px,color:#fff

    class A frontend
    class B backend
    class C db
    class D ai
    class E ml
```

---

## 🚀 Getting Started

### Prerequisites

| Tool | Required Version |
|------|-----------------|
| ☕ Java (JDK) | 17 or higher |
| 📦 Maven | 3.8+ |
| 🐍 Python | 3.9+ |
| 🟢 Node.js | 18+ |
| 🐬 MySQL | 8.0 |

---

### 1️⃣ Database Setup

Create the database in MySQL:

```sql
CREATE DATABASE IF NOT EXISTS carbon_db;
```

Update the credentials in `carbon-credit-platform/src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/carbon_db
spring.datasource.username=YOUR_MYSQL_USERNAME
spring.datasource.password=YOUR_MYSQL_PASSWORD
spring.jpa.hibernate.ddl-auto=update
server.port=8080
```

> ⚠️ **Never commit your real credentials.** Use environment variables or a secrets manager in production.

---

### 2️⃣ Start the AI Engine (FastAPI)

```bash
cd ai-engine

# Create a virtual environment (recommended)
python -m venv venv

# Activate — Windows
venv\Scripts\activate
# Activate — Mac/Linux
source venv/bin/activate

# Install dependencies
pip install -r requirements.txt

# Start the server
uvicorn main:app --reload --port 8000
```

✅ AI Engine running at **http://localhost:8000**
📚 Interactive API docs at **http://localhost:8000/docs**

---

### 3️⃣ Start the Backend (Spring Boot)

```bash
cd carbon-credit-platform
./mvnw spring-boot:run
```

✅ Backend running at **http://localhost:8080**

---

### 4️⃣ Start the Frontend (React)

```bash
cd frontend-ui
npm install
npm start
```

✅ App running at **http://localhost:3000**

---

## 🔌 API Reference

### 🧠 AI Endpoints (FastAPI — port 8000)

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/verify` | `POST` | Verify emission and assign carbon score |
| `/predict` | `POST` | Predict future CO₂ emissions (Linear Regression) |
| `/price` | `POST` | Get dynamic credit price recommendation |
| `/detect` | `POST` | Detect suspicious transaction (Isolation Forest) |

### ⚙️ Core Spring Boot Endpoints (port 8080)

| Category | Endpoint | Method | Access |
|----------|----------|--------|--------|
| **Auth** | `/auth/register` | `POST` | Public |
| **Auth** | `/auth/login` | `POST` | Public |
| **Company** | `/company/{id}` | `GET` | Authenticated |
| **Emissions** | `/emissions/submit` | `POST` | Seller/Buyer |
| **Admin** | `/admin/companies` | `GET` | Admin only |
| **Admin** | `/admin/assignCredits` | `POST` | Admin only |
| **Trading** | `/trading/transfer` | `POST` | Seller/Buyer |
| **Trading** | `/trading/history` | `GET` | Authenticated |
| **AI Proxy** | `/ai/predict` | `POST` | Authenticated |
| **AI Proxy** | `/ai/price` | `GET` | Authenticated |
| **AI Proxy** | `/ai/detect` | `POST` | Authenticated |
| **AI Proxy** | `/ai/detect/all` | `GET` | Admin only |

---

## 👥 User Roles

| Role | Capabilities |
|------|-------------|
| 🛡️ **ADMIN** | Manage all companies, assign/revoke credits, view all transactions, run bulk fraud detection |
| 🏭 **SELLER** | Submit emission reports, sell carbon credits, view own transaction history |
| 🛒 **BUYER** | Purchase carbon credits, view marketplace, view own transaction history |

> **Note:** Admin accounts can only be created directly in the database. The `/auth/register` endpoint is restricted to `SELLER` and `BUYER` roles only.

---

## 🗂️ Project Structure

```
Carbon-credit-platform/
│
├── 📁 ai-engine/                   # FastAPI Python AI microservice
│   ├── main.py                     # All AI endpoints (verify, predict, price, detect)
│   └── requirements.txt            # Python dependencies
│
├── 📁 carbon-credit-platform/      # Spring Boot Java backend
│   ├── src/main/java/com/carbon/
│   │   ├── controller/             # REST controllers (Auth, Admin, Trading, AI, ...)
│   │   ├── entity/                 # JPA entities (Company, EmissionReport, Transaction)
│   │   ├── repository/             # Spring Data JPA repositories
│   │   ├── service/                # Business logic services
│   │   ├── security/               # JWT utility and filters
│   │   └── config/                 # CORS and security configuration
│   └── src/main/resources/
│       └── application.properties  # DB and server config (DO NOT commit credentials)
│
├── 📁 frontend-ui/                  # React.js frontend
│   ├── src/
│   │   ├── App.js                  # Main app with routing and all views
│   │   ├── App.css                 # Global styles
│   │   └── components/             # Reusable UI components
│   └── package.json
│
├── 📁 docker/                       # Docker support files
├── docker-compose.yml              # Multi-service Docker orchestration
├── .gitignore                      # Excludes secrets, node_modules, build artifacts
└── README.md
```

---

## 🐳 Docker Deployment

To run the entire stack with Docker Compose:

```bash
# Build and start all services
docker-compose up --build

# Stop all services
docker-compose down
```

Services started automatically:
- 🌐 React Frontend → `http://localhost:3000`
- ⚙️ Spring Boot → `http://localhost:8080`
- 🤖 FastAPI AI → `http://localhost:8000`
- 🗄️ MySQL → `localhost:3306`

---

## 🔮 Roadmap

- [ ] Upgrade emission prediction to **ARIMA/SARIMA** for seasonal tracking
- [ ] Implement **federated learning** for personalized per-company emission models
- [ ] Integrate **real-time charting** (WebSocket) for live transaction monitoring
- [ ] Generate **EU ETS / ICAP-compliant** regulatory PDF reports
- [ ] Add **password hashing** (BCrypt) for production-ready security
- [ ] **Email notifications** for trade confirmations and fraud alerts

---

## 🤝 Contributing

Contributions are welcome! Here is how:

1. **Fork** the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit your changes: `git commit -m 'feat: add amazing feature'`
4. Push to the branch: `git push origin feature/amazing-feature`
5. Open a **Pull Request**

---

## 📄 License

This project is licensed under the **MIT License** — see the [LICENSE](LICENSE) file for details.

---

<div align="center">

**Built with ❤️ for a Greener Future 🌍**

<br/>

<img src="https://img.shields.io/badge/Made_with-Java-ED8B00?style=for-the-badge&logo=openjdk" />
<img src="https://img.shields.io/badge/Made_with-Python-3776AB?style=for-the-badge&logo=python&logoColor=white" />
<img src="https://img.shields.io/badge/Made_with-React-61DAFB?style=for-the-badge&logo=react&logoColor=black" />

<br/><br/>

⭐ **Star this repo if you found it useful!** ⭐

</div>
