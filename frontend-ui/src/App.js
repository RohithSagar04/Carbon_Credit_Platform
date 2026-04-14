import React, { useEffect, useState, useCallback, useMemo } from "react";
import axios from "axios";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip
} from "recharts";
import BlockchainRibbon from "./components/BlockchainRibbon";
import "./App.css";

const API_BASE = process.env.REACT_APP_API_BASE || "http://localhost:8080";

function App() {
  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [isRegister, setIsRegister] = useState(false);
  const [companyName, setCompanyName] = useState("");
  const [registerRole, setRegisterRole] = useState("");

  const [loggedIn, setLoggedIn] = useState(false);
  const [role, setRole] = useState("");
  const [companyId, setCompanyId] = useState("");
  const [allCompanies, setAllCompanies] = useState([]);
  const [searchId, setSearchId] = useState("");
  const [searchedCompany, setSearchedCompany] = useState(null);
  const [assignCompanyId, setAssignCompanyId] = useState("");
  const [assignCredits, setAssignCredits] = useState("");

  const [company, setCompany] = useState(null);
  const [co2, setCo2] = useState("");
  const [result, setResult] = useState(null);
  const [emissionSubmitting, setEmissionSubmitting] = useState(false);
  const [sellerId, setSellerId] = useState("");
  const [buyerId, setBuyerId] = useState("");
  const [credits, setCredits] = useState("");
  const [transactions, setTransactions] = useState([]);
  const [message, setMessage] = useState({ type: "", text: "" });

  useEffect(() => {
    const token = localStorage.getItem("token");
    const storedRole = localStorage.getItem("role");
    const storedCompanyId = localStorage.getItem("companyId");

    if (
      token &&
      storedRole &&
      storedCompanyId &&
      storedRole !== "null" &&
      storedRole !== "undefined"
    ) {
      axios.defaults.headers.common.Authorization = `Bearer ${token}`;
      setLoggedIn(true);
      setRole(storedRole);
      setCompanyId(storedCompanyId);
    } else {
      localStorage.clear();
      setLoggedIn(false);
    }
  }, []);

  const setFeedback = useCallback((type, text) => setMessage({ type, text }), []);

  const register = async () => {
    try {
      await axios.post(`${API_BASE}/auth/register`, {
        companyName,
        email,
        password,
        role: registerRole
      });
      setIsRegister(false);
      setFeedback("success", "Registration successful.");
    } catch {
      setFeedback("error", "Registration failed. Please check details and retry.");
    }
  };

  const login = async () => {
    try {
      const res = await axios.post(`${API_BASE}/auth/login`, {
        email,
        password
      });

      if (!res.data.role) {
        setFeedback("error", "Role is missing from login response.");
        return;
      }

      localStorage.setItem("token", res.data.token);
      localStorage.setItem("role", res.data.role);
      localStorage.setItem("companyId", String(res.data.companyId));
      axios.defaults.headers.common.Authorization = `Bearer ${res.data.token}`;

      setRole(res.data.role);
      setCompanyId(String(res.data.companyId));
      setLoggedIn(true);
      setFeedback("success", "Signed in successfully.");
    } catch {
      setFeedback("error", "Login failed. Verify your credentials.");
    }
  };

  const logout = () => {
    localStorage.clear();
    window.location.reload();
  };

  // ================= ADMIN DASHBOARD FUNCTIONS =================

// Load all registered companies
const loadAllCompanies = async () => {
  try {
    const res = await axios.get(`${API_BASE}/admin/companies`);
    setAllCompanies(res.data);
    setFeedback("success", "All companies loaded successfully.");
  } catch (err) {
    const detail =
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      "Request failed";
    setFeedback("error", `Failed to load companies: ${detail}`);
  }
};

// Search single company by ID
const searchCompany = async () => {
  try {
    if (!searchId || searchId.trim() === "") {
      setFeedback("error", "Please enter a valid company ID.");
      return;
    }

    const res = await axios.get(`${API_BASE}/admin/company/${searchId}`);
    setSearchedCompany(res.data);
    setFeedback("success", "Company details loaded successfully.");
  } catch (err) {
    const detail =
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      "Request failed";
    setFeedback("error", `Failed to fetch company: ${detail}`);
  }
};

// Assign credits manually to company
const assignCreditsToCompany = async () => {
  try {
    if (!assignCompanyId || !assignCredits) {
      setFeedback("error", "Company ID and credits are required.");
      return;
    }

    await axios.post(
      `${API_BASE}/admin/assignCredits?companyId=${assignCompanyId}&credits=${assignCredits}`
    );

    setFeedback("success", "Credits assigned successfully.");

    // Refresh company list after assignment
    loadAllCompanies();

  } catch (err) {
    const detail =
      err.response?.data?.message ||
      err.response?.data?.error ||
      err.message ||
      "Request failed";
    setFeedback("error", `Credit assignment failed: ${detail}`);
  }
};

  const loadCompany = async () => {
    try {
      const res = await axios.get(`${API_BASE}/company/${companyId}`);
      setCompany(res.data);
      setFeedback("success", "Dashboard refreshed.");
    } catch {
      setFeedback("error", "Could not load company dashboard.");
    }
  };

  const parseCompanyId = () => {
    const n = Number(companyId);
    return Number.isFinite(n) && n > 0 ? n : NaN;
  };

  const submitEmission = async () => {
    const value = parseFloat(String(co2).replace(/,/g, "").trim());
    if (!Number.isFinite(value) || value <= 0) {
      setFeedback("error", "Enter a positive CO2 value (for example 12.5).");
      return;
    }

    const cid = parseCompanyId();
    if (!Number.isFinite(cid)) {
      setFeedback("error", "Company session is invalid. Please sign in again.");
      return;
    }

    setEmissionSubmitting(true);
    try {
      const res = await axios.post(`${API_BASE}/emissions/submit`, {
        companyId: cid,
        co2Emission: value
      });
      setResult(res.data);
      setFeedback("success", "Emission analyzed and stored.");
    } catch (err) {
      const detail =
        err.response?.data?.message ||
        err.response?.data?.error ||
        err.message ||
        "Request failed";
      setFeedback("error", `Emission submission failed: ${detail}`);
    } finally {
      setEmissionSubmitting(false);
    }
  };

  const transferCredits = async () => {
    try {
      await axios.post(
        `${API_BASE}/trading/transfer?sellerId=${sellerId}&buyerId=${buyerId}&credits=${credits}`
      );
      setFeedback("success", "Credits transferred successfully.");
    } catch {
      setFeedback("error", "Transfer failed. Check IDs and credit amount.");
    }
  };

  const loadTransactions = async () => {
    try {
      const res = await axios.get(
        `http://localhost:8080/trading/history?companyId=${companyId}&role=${role}`
      );
  
      setTransactions(res.data);
  
    } catch {
      alert("Failed to load history ❌");
    }
  };

  const chartData = [
    { name: "Credits", value: company?.creditBalance || 0 },
    { name: "Score", value: company?.carbonScore || 0 }
  ];

  const adminStats = useMemo(() => {
    const list = Array.isArray(allCompanies) ? allCompanies : [];
    const totalCredits = list.reduce((s, c) => s + (Number(c.creditBalance) || 0), 0);
    const avgScore =
      list.length > 0
        ? list.reduce((s, c) => s + (Number(c.carbonScore) || 0), 0) / list.length
        : 0;
    return {
      count: list.length,
      totalCredits,
      avgScore: Math.round(avgScore * 10) / 10
    };
  }, [allCompanies]);

  const fraudPct =
    result && result.fraudProbability != null
      ? `${(Number(result.fraudProbability) * 100).toFixed(1)}%`
      : null;

  const adminTransactionSummary = useMemo(() => {
    const sellingHistory = Array.isArray(transactions?.sellingHistory)
      ? transactions.sellingHistory
      : [];
    const buyingHistory = Array.isArray(transactions?.buyingHistory)
      ? transactions.buyingHistory
      : [];
    const totalMoves = sellingHistory.length + buyingHistory.length;
    const volume = [...sellingHistory, ...buyingHistory].reduce(
      (sum, tx) => sum + (Number(tx.credits) || 0),
      0
    );
    const counterparties = new Set(
      [...sellingHistory, ...buyingHistory].flatMap((tx) => [tx.sellerId, tx.buyerId])
    );

    return {
      sellingCount: sellingHistory.length,
      buyingCount: buyingHistory.length,
      totalMoves,
      volume,
      counterparties: counterparties.size
    };
  }, [transactions]);

  if (!loggedIn) {
    return (
      <div className="page auth-page">
        <div className="auth-ambient" aria-hidden />
        <div className="auth-shell">
          <div className="auth-visual" aria-hidden>
            <div className="auth-visual-grid" />
            <div className="auth-visual-glow" />
            <div className="auth-visual-glow auth-visual-glow--secondary" />
            <div className="auth-visual-noise" />
            <div className="auth-visual-content">
              <span className="auth-visual-badge">Carbon marketplace</span>
              <h2 className="auth-visual-title">
                Verified credits.
                <br />
                <span className="auth-visual-title-accent">Transparent by design.</span>
              </h2>
              <ul className="auth-feature-list">
                <li>
                  <span className="auth-feature-icon" aria-hidden />
                  Immutable-style transaction history
                </li>
                <li>
                  <span className="auth-feature-icon" aria-hidden />
                  AI-assisted emission verification
                </li>
                <li>
                  <span className="auth-feature-icon" aria-hidden />
                  Role-based access for admins, sellers, and buyers
                </li>
              </ul>
              <div className="auth-visual-metrics">
                <div className="auth-metric">
                  <strong>Desk-ready</strong>
                  <span>Enterprise UI</span>
                </div>
                <div className="auth-metric">
                  <strong>Secure</strong>
                  <span>JWT sessions</span>
                </div>
                <div className="auth-metric">
                  <strong>Live</strong>
                  <span>Credit mobility</span>
                </div>
              </div>
            </div>
          </div>

          <div className="auth-card card auth-card--elevated">
            <div className="auth-card-head">
              <div className="auth-logo" aria-hidden>
                <span className="auth-logo-mark" />
              </div>
              <p className="eyebrow">Carbon Credit Platform</p>
              <h1 className="brand auth-brand">Sign in to continue</h1>
              <p className="subtitle auth-subtitle">
                Access your dashboard, credits, and emission tools in one place.
              </p>
            </div>

            <div className="auth-tabs" role="tablist" aria-label="Authentication mode">
              <button
                type="button"
                role="tab"
                aria-selected={!isRegister}
                className={`auth-tab ${!isRegister ? "auth-tab--active" : ""}`}
                onClick={() => setIsRegister(false)}
              >
                Sign in
              </button>
              <button
                type="button"
                role="tab"
                aria-selected={isRegister}
                className={`auth-tab ${isRegister ? "auth-tab--active" : ""}`}
                onClick={() => setIsRegister(true)}
              >
                Create account
              </button>
            </div>

            <form
              className="auth-form"
              onSubmit={(e) => {
                e.preventDefault();
                if (isRegister) register();
                else login();
              }}
            >
              {isRegister && (
                <label className="auth-label">
                  Company name
                  <input
                    className="field"
                    placeholder="Acme Industries"
                    value={companyName}
                    onChange={(e) => setCompanyName(e.target.value)}
                    autoComplete="organization"
                  />
                </label>
              )}
              <label className="auth-label">
                Email
                <input
                  className="field"
                  type="email"
                  placeholder="you@company.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  autoComplete="email"
                />
              </label>
              <label className="auth-label">
                Password
                <input
                  className="field"
                  type="password"
                  placeholder="Enter your password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  autoComplete={isRegister ? "new-password" : "current-password"}
                />
              </label>
              {isRegister && (
                <label className="auth-label">
                  Role
                  <select
                    className="field"
                    value={registerRole}
                    onChange={(e) => setRegisterRole(e.target.value)}
                  >
                    <option value="">Select role</option>
                    <option value="ADMIN">Admin</option>
                    <option value="SELLER">Seller</option>
                    <option value="BUYER">Buyer</option>
                  </select>
                </label>
              )}

              <button className="btn btn-primary btn-block auth-submit" type="submit">
                {isRegister ? "Create account" : "Sign in"}
              </button>
            </form>

            <p className="auth-footnote">
              {isRegister
                ? "Already registered? Switch to Sign in."
                : "New to the platform? Create an account."}
            </p>

            {message.text && (
              <div className={`auth-alert ${message.type === "error" ? "auth-alert--error" : "auth-alert--success"}`}>
                {message.text}
              </div>
            )}
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page app-page">
      <header className="topbar card">
        <div>
          <p className="eyebrow">Enterprise carbon desk</p>
          <h1 className="brand">Carbon Credit Platform</h1>
          <p className="subtitle">Professional carbon intelligence for modern enterprises</p>
        </div>
        <div className="topbar-actions">
          <span className="pill">Role: {role}</span>
          <button className="btn btn-secondary" type="button" onClick={logout}>
            Logout
          </button>
        </div>
      </header>

      <BlockchainRibbon companyId={companyId} pulse={emissionSubmitting} />

      {message.text && (
        <div className={`toast card message-toast ${message.type}`} role="status">
          {message.text}
        </div>
      )}

      {role === "ADMIN" && (
        <section className="card panel-elevated admin-console" aria-labelledby="admin-console-title">
          <div className="admin-console-hero">
            <div className="admin-console-hero-text">
              <span className="admin-pill">Administrator</span>
              <h2 id="admin-console-title" className="admin-console-title">
                Registry &amp; credit control
              </h2>
              <p className="admin-console-lede">
                Search organizations, review balances, and assign credits from a single command view.
              </p>
            </div>
            <button className="btn btn-primary admin-console-cta" type="button" onClick={loadAllCompanies}>
              Load registry
            </button>
          </div>

          <div className="admin-stats">
            <div className="admin-stat admin-stat--emerald">
              <span className="admin-stat-label">Organizations</span>
              <strong className="admin-stat-value">{adminStats.count}</strong>
              <span className="admin-stat-hint">In directory</span>
            </div>
            <div className="admin-stat admin-stat--sky">
              <span className="admin-stat-label">Credits on record</span>
              <strong className="admin-stat-value">{adminStats.totalCredits.toLocaleString()}</strong>
              <span className="admin-stat-hint">Sum of balances</span>
            </div>
            <div className="admin-stat admin-stat--violet">
              <span className="admin-stat-label">Avg. carbon score</span>
              <strong className="admin-stat-value">{adminStats.avgScore || "—"}</strong>
              <span className="admin-stat-hint">Across registry</span>
            </div>
          </div>

          <div className="admin-grid">
            <div className="admin-panel admin-panel--wide">
              <div className="admin-panel-head">
                <div>
                  <h3 className="panel-title">Company registry</h3>
                  <p className="field-hint tight">Live snapshot from the backend.</p>
                </div>
              </div>
              {allCompanies.length === 0 ? (
                <p className="placeholder-text admin-placeholder">
                  Load the registry to populate this table.
                </p>
              ) : (
                <div className="table-wrap admin-table-wrap">
                  <table className="admin-table">
                    <thead>
                      <tr>
                        <th>ID</th>
                        <th>Company</th>
                        <th>Role</th>
                        <th>Score</th>
                        <th className="admin-th-numeric">Credits</th>
                      </tr>
                    </thead>
                    <tbody>
                      {allCompanies.map((c) => (
                        <tr key={c.id}>
                          <td className="admin-mono">{c.id}</td>
                          <td>
                            <span className="admin-company-name">{c.companyName}</span>
                          </td>
                          <td>
                            <span className={`admin-role-badge admin-role-badge--${String(c.role || "").toLowerCase()}`}>
                              {c.role}
                            </span>
                          </td>
                          <td>{c.carbonScore}</td>
                          <td className="admin-td-numeric">{Number(c.creditBalance).toLocaleString()}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>

            <div className="admin-panel-stack">
              <div className="admin-panel">
                <h3 className="panel-title">Lookup company</h3>
                <p className="field-hint">Fetch a profile by numeric company ID.</p>
                <div className="admin-inline">
                  <label className="auth-label auth-label--compact">
                    Company ID
                    <input
                      className="field"
                      placeholder="e.g. 12"
                      value={searchId}
                      onChange={(e) => setSearchId(e.target.value)}
                      inputMode="numeric"
                    />
                  </label>
                  <button className="btn btn-secondary admin-inline-btn" type="button" onClick={searchCompany}>
                    Search
                  </button>
                </div>
                {searchedCompany && (
                  <div className="admin-detail-card">
                    <div className="admin-detail-row">
                      <span>Name</span>
                      <strong>{searchedCompany.companyName}</strong>
                    </div>
                    <div className="admin-detail-row">
                      <span>Email</span>
                      <strong>{searchedCompany.email}</strong>
                    </div>
                    <div className="admin-detail-row">
                      <span>Role</span>
                      <strong>{searchedCompany.role}</strong>
                    </div>
                    <div className="admin-detail-row">
                      <span>Carbon score</span>
                      <strong>{searchedCompany.carbonScore}</strong>
                    </div>
                    <div className="admin-detail-row">
                      <span>Credits</span>
                      <strong>{Number(searchedCompany.creditBalance).toLocaleString()}</strong>
                    </div>
                  </div>
                )}
              </div>

              <div className="admin-panel admin-panel--accent">
                <h3 className="panel-title">Assign credits</h3>
                <p className="field-hint">Increase a company&apos;s balance for testing or issuance.</p>
                <label className="auth-label auth-label--compact">
                  Company ID
                  <input
                    className="field"
                    placeholder="Target company ID"
                    value={assignCompanyId}
                    onChange={(e) => setAssignCompanyId(e.target.value)}
                    inputMode="numeric"
                  />
                </label>
                <label className="auth-label auth-label--compact">
                  Credits to add
                  <input
                    className="field"
                    placeholder="Amount"
                    value={assignCredits}
                    onChange={(e) => setAssignCredits(e.target.value)}
                    inputMode="numeric"
                  />
                </label>
                <button className="btn btn-primary btn-block" type="button" onClick={assignCreditsToCompany}>
                  Assign credits
                </button>
              </div>
            </div>
          </div>
        </section>
      )}

      <section className="grid two-col">
        <div className="card panel-elevated">
          <div className="panel-head">
            <h3 className="panel-title">Company Dashboard</h3>
            <span className="micro-label">Live profile</span>
          </div>
          <button className="btn btn-primary" type="button" onClick={loadCompany}>
            Load Dashboard
          </button>
          {company && (
            <div className="stats">
              <div className="stat stat-glow">
                <span>Company</span>
                <strong>{company.companyName}</strong>
              </div>
              <div className="stat stat-glow">
                <span>Carbon Score</span>
                <strong>{company.carbonScore}</strong>
              </div>
              <div className="stat stat-glow">
                <span>Credit Balance</span>
                <strong>{company.creditBalance}</strong>
              </div>
            </div>
          )}
        </div>

        <div className="card panel-elevated chart-card">
          <div className="panel-head">
            <h3 className="panel-title">Performance Snapshot</h3>
            <span className="micro-label">Credits vs score</span>
          </div>
          {company ? (
            <ResponsiveContainer width="100%" height={250}>
              <BarChart data={chartData}>
                <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
                <XAxis dataKey="name" tick={{ fill: "#64748b", fontSize: 12 }} />
                <YAxis tick={{ fill: "#64748b", fontSize: 12 }} />
                <Tooltip
                  contentStyle={{
                    borderRadius: 10,
                    border: "1px solid #e2e8f0",
                    boxShadow: "0 8px 24px rgba(15,23,42,0.08)"
                  }}
                />
                <Bar dataKey="value" fill="url(#barGrad)" radius={[8, 8, 0, 0]} />
                <defs>
                  <linearGradient id="barGrad" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="0%" stopColor="#22c55e" />
                    <stop offset="100%" stopColor="#0f766e" />
                  </linearGradient>
                </defs>
              </BarChart>
            </ResponsiveContainer>
          ) : (
            <p className="placeholder-text">Load dashboard data to view chart insights.</p>
          )}
        </div>
      </section>

      <section className="grid two-col">
        <div className="card panel-elevated emission-card">
          <div className="panel-head">
            <h3 className="panel-title">Emission Submission</h3>
            <span className="micro-label">AI verification pipeline</span>
          </div>
          <p className="field-hint">
            Enter a positive CO2 figure. The backend calls the verification service (with a safe
            fallback if the AI engine is offline).
          </p>
          <div className="inline-fields emission-row">
            <input
              className="field"
              type="text"
              inputMode="decimal"
              placeholder="e.g. 24.6 (CO₂)"
              value={co2}
              onChange={(e) => setCo2(e.target.value)}
              disabled={emissionSubmitting}
            />
            <button
              className="btn btn-primary"
              type="button"
              onClick={submitEmission}
              disabled={emissionSubmitting}
            >
              {emissionSubmitting ? "Analyzing…" : "Submit & verify"}
            </button>
          </div>
          {result && (
            <div className="stats compact emission-results">
              {result.co2Emission != null && (
                <div className="stat stat-accent">
                  <span>Recorded CO₂</span>
                  <strong>{Number(result.co2Emission).toLocaleString()}</strong>
                </div>
              )}
              <div className="stat stat-accent">
                <span>Carbon score</span>
                <strong>{result.carbonScore}</strong>
              </div>
              <div className="stat stat-accent">
                <span>Fraud probability</span>
                <strong>{fraudPct ?? result.fraudProbability}</strong>
              </div>
            </div>
          )}
        </div>

        <div className="card panel-elevated">
          <div className="panel-head">
            <h3 className="panel-title">Credit Trading</h3>
            <span className="micro-label">Peer transfers</span>
          </div>
          {(role === "SELLER" || role === "ADMIN") && (
            <>
              <div className="inline-fields triple">
                <input
                  className="field"
                  placeholder="Seller ID"
                  onChange={(e) => setSellerId(e.target.value)}
                />
                <input
                  className="field"
                  placeholder="Buyer ID"
                  onChange={(e) => setBuyerId(e.target.value)}
                />
                <input
                  className="field"
                  placeholder="Credits"
                  onChange={(e) => setCredits(e.target.value)}
                />
              </div>
              <button className="btn btn-primary" type="button" onClick={transferCredits}>
                Transfer Credits
              </button>
            </>
          )}
          {role === "BUYER" && (
            <p className="placeholder-text">Buyer accounts cannot initiate credit sales.</p>
          )}
        </div>
      </section>

      <section className="card panel-elevated ledger-card">
  <div className="section-header">
    <div>
      <h3 className="panel-title">Transaction History</h3>
      <p className="field-hint tight">Immutable-style log of credit movements.</p>
    </div>
    <button className="btn btn-secondary" type="button" onClick={loadTransactions}>
      Refresh History
    </button>
  </div>

  {/* ADMIN VIEW */}
  {role === "ADMIN" ? (
    <>
      <div className="ledger-admin-overview">
        <div className="ledger-kpi ledger-kpi--emerald">
          <span className="ledger-kpi-label">Total records</span>
          <strong className="ledger-kpi-value">{adminTransactionSummary.totalMoves}</strong>
          <span className="ledger-kpi-sub">Buying + selling entries</span>
        </div>
        <div className="ledger-kpi ledger-kpi--sky">
          <span className="ledger-kpi-label">Credits moved</span>
          <strong className="ledger-kpi-value">{adminTransactionSummary.volume.toLocaleString()}</strong>
          <span className="ledger-kpi-sub">Aggregate transaction volume</span>
        </div>
        <div className="ledger-kpi ledger-kpi--violet">
          <span className="ledger-kpi-label">Unique entities</span>
          <strong className="ledger-kpi-value">{adminTransactionSummary.counterparties}</strong>
          <span className="ledger-kpi-sub">Sellers and buyers involved</span>
        </div>
      </div>

      {/* SELLING HISTORY */}
      <h3 className="ledger-section-title">Selling history</h3>
      {transactions.sellingHistory?.length > 0 ? (
        <div className="table-wrap ledger-table-wrap">
          <table className="ledger-table">
            <thead>
              <tr>
                <th>Seller</th>
                <th>Buyer</th>
                <th className="ledger-numeric">Credits</th>
              </tr>
            </thead>
            <tbody>
              {transactions.sellingHistory.map((tx, index) => (
                <tr key={`sell-${index}`}>
                  <td className="ledger-mono">{tx.sellerId}</td>
                  <td className="ledger-mono">{tx.buyerId}</td>
                  <td className="ledger-numeric ledger-credit">{Number(tx.credits).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="placeholder-text ledger-empty">No selling history found.</p>
      )}

      {/* BUYING HISTORY */}
      <h3 className="ledger-section-title">Buying history</h3>
      {transactions.buyingHistory?.length > 0 ? (
        <div className="table-wrap ledger-table-wrap">
          <table className="ledger-table">
            <thead>
              <tr>
                <th>Seller</th>
                <th>Buyer</th>
                <th className="ledger-numeric">Credits</th>
              </tr>
            </thead>
            <tbody>
              {transactions.buyingHistory.map((tx, index) => (
                <tr key={`buy-${index}`}>
                  <td className="ledger-mono">{tx.sellerId}</td>
                  <td className="ledger-mono">{tx.buyerId}</td>
                  <td className="ledger-numeric ledger-credit">{Number(tx.credits).toLocaleString()}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      ) : (
        <p className="placeholder-text ledger-empty">No buying history found.</p>
      )}
    </>
  ) : (
    <>
      {/* SELLER / BUYER VIEW */}
      {transactions.length === 0 ? (
        <p className="placeholder-text">No transactions loaded yet.</p>
      ) : (
        <div className="table-wrap">
          <table>
            <thead>
              <tr>
                <th>Seller</th>
                <th>Buyer</th>
                <th>Credits</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((tx, index) => (
                <tr key={`${tx.sellerId}-${tx.buyerId}-${index}`}>
                  <td>{tx.sellerId}</td>
                  <td>{tx.buyerId}</td>
                  <td>{tx.credits}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </>
  )}
</section>
    </div>
  );
}

export default App;
