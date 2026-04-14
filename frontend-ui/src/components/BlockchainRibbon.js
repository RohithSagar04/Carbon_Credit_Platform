import React from "react";

function shortenHash(seed) {
  const s = String(seed || "ledger");
  let h = 0;
  for (let i = 0; i < s.length; i += 1) {
    h = (h << 5) - h + s.charCodeAt(i);
    h |= 0;
  }
  const hex = Math.abs(h).toString(16).padStart(8, "0");
  return `0x${hex}${hex}`.slice(0, 18);
}

export default function BlockchainRibbon({ companyId, pulse }) {
  const hash = shortenHash(companyId);

  return (
    <div className="chain-ribbon card" aria-label="Blockchain-style verification strip">
      <div className="chain-ribbon-inner">
        <div className="chain-nodes" aria-hidden>
          {[0, 1, 2, 3, 4].map((i) => (
            <span key={i} className={`chain-node ${pulse && i === 2 ? "chain-node-pulse" : ""}`} />
          ))}
        </div>
        <div className="chain-copy">
          <p className="chain-title">On-chain style audit trail</p>
          <p className="chain-sub">
            Emission checks are prepared for anchoring to your enterprise ledger. Session anchor{" "}
            <code className="chain-hash">{hash}</code>
          </p>
        </div>
        <div className="chain-badge" title="Simulated network status">
          <span className="chain-dot" />
          Network ready
        </div>
      </div>
    </div>
  );
}
