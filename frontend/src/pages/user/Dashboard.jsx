
import React, { useEffect, useState } from 'react'
import api from '../../api/client'
import { Link } from 'react-router-dom'

export default function Dashboard(){
  const [me, setMe] = useState(null)
  const [txns, setTxns] = useState([])
  useEffect(()=>{ (async()=>{
    const meRes = await api.get('/user/me'); setMe(meRes.data)
    const txRes = await api.get('/user/transactions'); setTxns(txRes.data)
  })() },[])
  if(!me) return null
  return (
    <div className="grid cols-2"> 
      <div className="card">
        <h3>Points Summary</h3>
        <div className="flex"><span className="badge">Member: {me.name}</span><span className="badge">Tier: {me.profile?.loyaltyTier||'Bronze'}</span></div>
        <p><strong>Current Balance</strong>: {me.profile?.pointsBalance||0}</p>
      </div>
      <div className="card">
        <h3>Daily Activities</h3>
        <div className="grid cols-2"> 
          {[{title:'Daily Login Bonus',points:50,code:'LOGIN'},{title:'Write a Product Review',points:100,code:'REVIEW'},{title:'Share on Social',points:75,code:'SOCIAL'},{title:'Refer a Friend',points:200,code:'REFER'}].map(a=> (
            <ClaimCard key={a.code} a={a} />
          ))}
        </div>
      </div>
      <div className="card" style={{gridColumn:'1/-1'}}>
        <h3>Recent Transactions (Top 10)</h3>
        <table width="100%"> 
          <thead><tr><th>Date</th><th>Type</th><th>Points Earned</th><th>Points Redeemed</th><th>Store/Activity</th></tr></thead>
          <tbody>
            {txns.map(t=> (
              <tr key={t.id}><td>{t.date}</td><td>{t.type}</td><td>{t.pointsEarned}</td><td>{t.pointsRedeemed}</td><td>{t.store||t.note}</td></tr>
            ))}
          </tbody>
        </table>
        <div style={{marginTop:8}}>
          <Link className="link" to="/user/transactions">View all</Link>
        </div>
      </div>
    </div>
  )
}

function ClaimCard({a}){
  const [busy, setBusy] = useState(false)
  const [ok, setOk] = useState(false)
  const claim = async()=>{
    setBusy(true)
    try{ await api.post('/user/claim',{ activityCode:a.code, points:a.points, note:a.title }); setOk(true) } finally{ setBusy(false) }
  }
  return (
    <div className="card"> 
      <h4>{a.title}</h4>
      <p>Earn {a.points} points</p>
      {!ok ? <button disabled={busy} className="button" onClick={claim}>Claim</button> : <span className="badge">Claimed</span>}
    </div>
  )
}
