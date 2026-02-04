// src/pages/Dashboard.jsx
import React, { useEffect, useState } from 'react'
import api from '../../api/client'
import { Link } from 'react-router-dom'
import '../../../styles/Dashboard.css'

function ClaimCard({ a, onClaimed }) {
  const [busy, setBusy] = useState(false)
  const [ok, setOk] = useState(false)

  const claim = async () => {
    setBusy(true)
    try {
      await api.post('/user/claim', {
        activityCode: a.code,
        points: a.points,
        note: a.title
      })
      setOk(true)
      onClaimed?.() // tell parent to refresh me + transactions
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="d-card d-claim-card">
      <h4 className="d-card-title">{a.title}</h4>
      <p>Earn {a.points} points</p>
      {!ok ? (
        <button disabled={busy} className="d-btn" onClick={claim}>
          {busy ? 'Claiming…' : 'Claim'}
        </button>
      ) : (
        <span className="d-badge">Claimed</span>
      )}
    </div>
  )
}

export default function Dashboard() {
  const [me, setMe] = useState(null)
  const [txns, setTxns] = useState([])

  const loadMe = async () => {
    const meRes = await api.get('/user/me')
    setMe(meRes.data)
  }

  const loadTxns = async () => {
    const txRes = await api.get('/user/transactions')
    setTxns(txRes.data)
  }

  const refreshAfterClaim = async () => {
    // Refetch both: updates balance and the table
    await Promise.all([loadMe(), loadTxns()])
  }

  useEffect(() => {
    (async () => {
      await Promise.all([loadMe(), loadTxns()])
    })()
  }, [])

  if (!me) return null

  const activities = [
    { title: 'Daily Login Bonus', points: 50, code: 'LOGIN' },
    { title: 'Write a Product Review', points: 100, code: 'REVIEW' },
    { title: 'Share on Social', points: 75, code: 'SOCIAL' },
    { title: 'Refer a Friend', points: 200, code: 'REFER' },
  ]

  // Replace with your real field if you have one
  const nextExpiry = me.profile?.nextExpiryDate ?? '23/2/2026'

  return (
    <div className="d-page">
      {/* ===== Points Summary (standalone) ===== */}
      <div className="d-card d-ps">
        <div className="d-ps-row">
          {/* Left: title, member/tier, expiry pill */}
          <div className="d-ps-left">
            <h3 className="d-ps-title">Points Summary</h3>
            <p className="d-ps-sub">
              Member: <span className="d-ps-strong">{me.name}</span>
              <span className="d-ps-dot">·</span>
              Tier: <span className="d-ps-strong">{me.profile?.loyaltyTier || 'Bronze'}</span>
            </p>
            <div className="d-ps-pill">
              <span className="d-ps-pill-label">Next Expiry</span>
              <span className="d-ps-pill-value">{nextExpiry}</span>
            </div>
          </div>

          {/* Right: current balance */}
          <div className="d-ps-right">
            <div className="d-ps-right-label">Current Balance</div>
            <div className="d-ps-right-value">{me.profile?.pointsBalance ?? 0}</div>
          </div>
        </div>
      </div>

      {/* ===== Daily Activities + Recent Transactions ===== */}
      <div className="d-card">
        <h3 className="d-section-title">Daily Activities</h3>
        <div className="d-activities-row">
          {activities.map(a => (
            <ClaimCard key={a.code} a={a} onClaimed={refreshAfterClaim} />
          ))}
        </div>
      </div>

     
      <div className="d-card">
        <h3 className="d-section-title">Recent Transactions</h3>
        <div className="d-table-wrap">
          <table className="d-table">
            <thead>
              <tr>
                <th>Date</th>
                <th>Type</th>
                <th>Points Earned</th>
                <th>Points Redeemed</th>
                <th>Store/Activity</th>
              </tr>
            </thead>
            <tbody>
              {txns.map(t => (
                <tr key={t.id}>
                  <td>{t.date}</td>
                  <td>{t.type}</td>
                  <td>{t.pointsEarned}</td>
                  <td>{t.pointsRedeemed}</td>
                  <td>{t.store || t.note}</td>
                </tr>
              ))}
              {txns.length === 0 && (
                <tr>
                  <td colSpan="5" style={{ textAlign: 'center', color: '#64748b', padding: 12 }}>
                    No transactions yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>

        {/* <div style={{ marginTop: 8 }}>
          <Link className="d-link" to="/user/transactions">
            View all
          </Link>
        </div> */}
      </div>
    </div>
  )
}
