# Carbon Credit Trading Platform – Project Documentation

> **Version:** 2.0.0  
> **Last Updated:** April 2026  
> **Stack:** Spring Boot (Java 17) · React · MySQL · FastAPI (Python 3.11)

---

## Table of Contents

1. [Project Overview](#1-project-overview)
2. [System Architecture](#2-system-architecture)
3. [Existing Features](#3-existing-features)
4. [Newly Added AI Features](#4-newly-added-ai-features)
5. [AI Model Details](#5-ai-model-details)
6. [Dataset Information](#6-dataset-information)
7. [Training Process](#7-training-process)
8. [API Endpoints](#8-api-endpoints)
9. [How to Run All Services](#9-how-to-run-all-services)
10. [Integration Guide](#10-integration-guide)
11. [Future Improvements](#11-future-improvements)

---

## 1. Project Overview

The **Carbon Credit Trading Platform** is a full-stack enterprise application enabling companies to register with role-based access (Admin / Seller / Buyer), submit CO₂ emission reports verified by an AI engine, receive carbon scores, and trade carbon credits peer-to-peer.

**Version 2.0.0 adds an AI Intelligence Suite** with three machine-learning-powered features:
1. **Carbon Emission Prediction** – forecast future CO₂ output (Linear Regression)
2. **Credit Price Recommendation** – dynamic pricing based on supply/demand
3. **Suspicious Transaction Detection** – anomaly detection (Isolation Forest)

---

## 2. System Architecture

```
Browser (React :3000)
    │ HTTP axios
    ▼
Spring Boot Backend (:8080)
    /auth  /company  /admin  /emissions  /trading
    /ai/predict  /ai/price  /ai/detect  /ai/detect/all
    │ JPA                              │ RestTemplate
    ▼                                  ▼
MySQL :3306 (carbon_db)         FastAPI AI Engine (:8000)
                                    /verify  /predict  /price  /detect
```

**Key design decisions:**
- **Offline fallback**: Every AI call in `AIService.java` has a local heuristic — app works even when Python engine is down
- **Additive only**: No existing controller, service, or entity was modified
- **JPA auto-update**: `ddl-auto=update` means no manual DB migration needed

---

## 3. Existing Features

| Feature | Endpoint | Roles |
|---|---|---|
| User Registration | `POST /auth/register` | All (ADMIN blocked) |
| User Login (JWT) | `POST /auth/login` | All |
| Company Profile | `GET /company/{id}` | All |
| Emission Submission + AI Verify | `POST /emissions/submit` | All |
| Credit Assignment | `POST /admin/assignCredits` | Admin |
| Admin Credit Sale | `POST /admin/sellCredits` | Admin |
| Company Registry | `GET /admin/companies` | Admin |
| Company Lookup | `GET /admin/company/{id}` | Admin |
| Credit Transfer | `POST /trading/transfer` | Seller, Admin |
| Transaction History | `GET /trading/history` | All (role-filtered) |

---

## 4. Newly Added AI Features

### 4.1 Carbon Emission Prediction

Given a list of historical CO₂ readings, predicts the next N emissions using Linear Regression, classifies trend (increasing/stable/decreasing), and returns reduction strategies.

**Files modified:**
- `ai-engine/main.py` — `POST /predict` endpoint
- `AIService.java` — `predictEmissions()` with local fallback
- `AIController.java` — `POST /ai/predict` Spring endpoint
- `EmissionReportRepository.java` — `findByCompanyId()`
- `App.js` — Prediction panel with LineChart
- `App.css` — AI suite styles

### 4.2 Credit Price Recommendation

Computes a recommended market price based on:
- **Supply** = total credit balance across all companies (auto-fetched from DB)
- **Demand** = transaction count proxy
- **Carbon score** = green market premium/penalty

Returns price, trend (rising/stable/falling), confidence, and explanation.

**Files modified:**
- `ai-engine/main.py` — `POST /price` endpoint
- `AIService.java` — `recommendPrice()` with local fallback
- `AIController.java` — `GET /ai/price` Spring endpoint
- `App.js` — Price recommendation panel

### 4.3 Suspicious Transaction Detection

Isolation Forest analyses transaction vectors for anomalies. Layered with rule-based flags:
- Volume > 2,000 credits
- Frequency > 20 TX/24h
- Volume spike > 10x average
- Self-transaction (buyer == seller)

Returns `riskScore` (0-1), `isSuspicious`, and `flags[]`.

**Files modified:**
- `ai-engine/main.py` — `POST /detect` + Isolation Forest pre-training at startup
- `AIService.java` — `detectSuspicious()` with local fallback
- `AIController.java` — `POST /ai/detect` (single) + `GET /ai/detect/all` (admin bulk)
- `App.js` — Single TX check panel + Admin bulk scan table
- `App.css` — Detection verdict, flag chips, risk badges

---

## 5. AI Model Details

### Emission Prediction

| Attribute | Detail |
|---|---|
| **Model** | Linear Regression (OLS) |
| **Why** | Near-linear annual trends; fully interpretable for regulators |
| **Library** | scikit-learn LinearRegression |
| **Inputs** | Time-step index (X), CO2 value (y) |
| **Training** | Online fit per request |
| **Output** | predictedEmissions[], trend, slope, reductionStrategies[] |

### Credit Price Recommendation

| Attribute | Detail |
|---|---|
| **Model** | Rule-based supply/demand ratio with log-dampening + score bonus |
| **Why** | Carbon credit pricing is regulated; explainability is legally required |
| **Formula** | price = 25 x (1 + log(demand/supply)x0.4 + (score-50)/100x0.3) |
| **Inputs** | availableCredits, buyRequests, avgCarbonScore |
| **Output** | recommendedPrice, priceTrend, deltaPercent, confidence, explanation |

### Suspicious Transaction Detection

| Attribute | Detail |
|---|---|
| **Model** | Isolation Forest |
| **Why** | Unsupervised — no labelled fraud data needed; industry-standard for fintech |
| **Library** | scikit-learn IsolationForest |
| **Parameters** | n_estimators=200, contamination=0.1, random_state=42 |
| **Inputs** | credits, txCountLast24h, avgCreditsPerTx |
| **Pre-processing** | StandardScaler (zero mean, unit variance) |
| **Training** | Pre-trained at server startup on 550 synthetic samples |
| **Output** | riskScore, isSuspicious, flags[], rawIsoForestScore |

---

## 6. Dataset Information

### Emission Prediction
- **Type:** Synthetic (mirrors real-world annual company emission patterns)
- **Real-world references:**
  - EPA GHG Reporting: https://www.epa.gov/ghgreporting/data-sets
  - Kaggle CO2 Emissions: https://www.kaggle.com/datasets/ulrikthygepedersen/co2-emissions-by-country
- **Preprocessing:** Index as time-steps → OLS fit → predict future indices

### Credit Price Recommendation
- **Type:** Rule-based (no training data required)
- **Calibration references:**
  - Ember Climate Carbon Price Viewer: https://ember-climate.org/data-catalogue/carbon-price-viewer/
  - Kaggle Carbon Credit Price: https://www.kaggle.com/datasets/jacksondivakar/carbon-credit-price
- **Baseline:** $25 USD (EU ETS 2023 average)

### Suspicious Transaction Detection
- **Type:** Synthetic — 550 records generated at startup
  - 500 normal: credits 1-200, freq 1-10/day, avg 10-100
  - 50 anomalies: credits 5000-50000, freq 50-200/day, avg 1000-10000
- **Real-world reference:**
  - Credit Card Fraud (adapted features): https://www.kaggle.com/datasets/mlg-ulb/creditcardfraud
- **Preprocessing:** StandardScaler normalisation → Isolation Forest fit

---

## 7. Training Process

### Emission Prediction
Fit per-request (online training):
```python
X = np.array(range(len(history))).reshape(-1, 1)
y = np.array(history)
model = LinearRegression().fit(X, y)
predictions = model.predict(future_indices)
```

### Credit Price Recommendation
No training — deterministic formula applied at request time.

### Suspicious Transaction Detection
Pre-trained once at server startup:
```python
_scaler     = StandardScaler()
_X_scaled   = _scaler.fit_transform(_X_train)
_iso_forest = IsolationForest(n_estimators=200, contamination=0.1, random_state=42)
_iso_forest.fit(_X_scaled)
```
To retrain on real data: replace `_X_train` with real transaction records and restart.

---

## 8. API Endpoints

### New Endpoints (v2.0.0)

#### Spring Boot (:8080)

| Method | URL | Body / Params | Description |
|---|---|---|---|
| POST | /ai/predict | {companyId, historicalEmissions[], steps} | Emission prediction |
| GET | /ai/price | ?availableCredits&buyRequests&avgCarbonScore&previousPrice | Price recommendation |
| POST | /ai/detect | {credits, buyerId, sellerId, txCountLast24h, avgCreditsPerTx} | Single TX anomaly |
| GET | /ai/detect/all | — | Bulk scan all TXs (admin) |

#### FastAPI (:8000)

| Method | URL | Body | Description |
|---|---|---|---|
| POST | /predict | {companyId, historicalEmissions[], steps} | Linear Regression forecast |
| POST | /price | {availableCredits, buyRequests, avgCarbonScore, previousPrice?} | Price model |
| POST | /detect | {credits, buyerId, sellerId, txCountLast24h, avgCreditsPerTx} | Isolation Forest |

Interactive API docs: http://localhost:8000/docs

### Existing Endpoints (unchanged)

`POST /auth/register` · `POST /auth/login` · `GET /company/{id}` · `POST /emissions/submit`  
`GET /admin/companies` · `GET /admin/company/{id}` · `POST /admin/assignCredits`  
`POST /admin/sellCredits` · `POST /trading/transfer` · `GET /trading/history`  
`POST /verify (FastAPI)`

---

## 9. How to Run All Services

### Prerequisites
- Java 17+, Maven 3.8+
- Python 3.9+, pip
- Node.js 18+, npm
- MySQL 8.0

### Step 1 – Database
```sql
CREATE DATABASE IF NOT EXISTS carbon_db;
```
Update credentials in `carbon-credit-platform/src/main/resources/application.properties`.

### Step 2 – AI Engine (FastAPI)
```powershell
cd ai-engine
python -m venv venv
venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --reload --port 8000
```
Verify at: http://localhost:8000/docs

### Step 3 – Spring Boot Backend
```powershell
cd carbon-credit-platform
.\mvnw spring-boot:run
```
API at: http://localhost:8080

### Step 4 – React Frontend
```powershell
cd frontend-ui
npm install
npm start
```
App at: http://localhost:3000

### Port Summary

| Service | Port |
|---|---|
| React Frontend | 3000 |
| Spring Boot | 8080 |
| FastAPI AI Engine | 8000 |
| MySQL | 3306 |

---

## 10. Integration Guide

### A. Emission Prediction
1. Log in with any role → scroll to **AI Intelligence Suite**
2. Enter historical CO2 values (comma-separated), e.g. `30, 35, 28, 40, 43`
3. Set steps (1-12) → click **Predict**
4. View LineChart (historical green / predicted amber dashed) + reduction strategies

### B. Credit Price Recommendation
1. Log in → scroll to AI suite → click **Get Price** in the blue panel
2. Backend auto-reads supply/demand from DB and computes price
3. Price hero card shows recommended price, trend badge, confidence, and explanation

### C. Suspicious Transaction Detection

**Single check:**
1. Scroll to amber **Transaction Anomaly Check** panel
2. Enter credits, buyer ID, seller ID, freq, avg → click **Scan Transaction**
3. Verdict, risk score, and flags displayed

**Bulk admin scan:**
1. Log in as **ADMIN** → Admin Console
2. Click **Scan All Transactions** in the red panel
3. Table shows all suspicious TXs with risk scores and flags

### Running Without the AI Engine
All features degrade gracefully to local fallbacks — same response format, no UI changes.

---

## 11. Future Improvements

### Short-term
- Replace synthetic Isolation Forest training data with real transaction history from DB
- Implement real 24-hour window query for `txCountLast24h`
- Scheduled credit price updates with history chart

### Medium-term
- Upgrade emission prediction from LR to ARIMA/SARIMA for seasonal patterns
- Labelled fraud dataset → supervised Random Forest or XGBoost
- Model persistence with `joblib`
- Email alerts for flagged suspicious transactions

### Long-term
- Reinforcement learning for continuous price optimisation
- Federated learning for per-company emission models
- EU ETS / ICAP-compliant regulatory report generation
- React Native mobile companion app

---

*Carbon Credit Trading Platform v2.0.0 – Documentation*
