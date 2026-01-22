import React, { useEffect, useState } from 'react'
import api from '../../api/client'

export default function FraudMonitor(){
  const [tab, setTab] = useState('Alerts')
  const [alerts, setAlerts] = useState([])
  const [anoms, setAnoms] = useState([])
  const [audit, setAudit] = useState([])
  const [txns] = useState([
    {id:'20001', datetime:'2025-12-20 14:51', cardholder:'John Doe', merchant:'Amazon', country:'US', amount:'$129.99', risk:'LOW', status:'OK'},
    {id:'20002', datetime:'2025-12-20 15:33', cardholder:'Jane Smith', merchant:'Starbucks', country:'US', amount:'$7.25', risk:'LOW', status:'OK'},
    {id:'20003', datetime:'2025-12-20 15:40', cardholder:'Alice Brown', merchant:'eBay', country:'DE', amount:'€545.00', risk:'MEDIUM', status:'OK'},
    {id:'20004', datetime:'2025-12-20 17:22', cardholder:'Robert King', merchant:'CryptoEx', country:'SG', amount:'$3,200.00', risk:'HIGH', status:'OK'}
  ])
  useEffect(()=>{ (async()=>{ try{ const a = await api.get('/admin/fraud/alerts'); setAlerts(a.data||[]) }catch(_){ setAlerts([]) } try{ const an = await api.get('/admin/fraud/anomalies'); setAnoms(an.data||[]) }catch(_){ setAnoms([]) } try{ const au = await api.get('/admin/fraud/audit'); setAudit(au.data||[]) }catch(_){ setAudit([]) } })() },[])
  return (
    <div className="card"> 
      <div className="tabs"><select value={tab} onChange={e=>setTab(e.target.value)}>
        <option>Alerts</option>
        <option>Anomalies</option>
        <option>Transaction Monitoring</option>
        <option>Audit Reports</option>
      </select></div>
      {tab==='Alerts' && (<div style={{marginTop:12}}>{alerts.length===0? (<p>No alerts found.</p>): (<ul style={{listStyle:'none', paddingLeft:0}}>{alerts.map(x=> (<li key={x.id} style={{padding:'10px 8px', borderBottom:'1px solid #eee'}}><span className="badge" style={{marginRight:8}}>{x.severity}</span>{x.message}</li>))}</ul>)}</div>)}
      {tab==='Anomalies' && (<div style={{marginTop:12}}>{anoms.length===0? (<p>No anomalies found.</p>): (<ul style={{listStyle:'none', paddingLeft:0}}>{anoms.map(x=> (<li key={x.id} style={{padding:'10px 8px', borderBottom:'1px solid #eee'}}><strong>{x.title}</strong>: {x.detail}</li>))}</ul>)}</div>)}
      {tab==='Transaction Monitoring' && (<div style={{marginTop:12}}>
        <div className="flex" style={{marginBottom:10}}>
          <input className="input" placeholder="Search by ID, cardholder, merchant, country, or amount ..." />
          <select className="input" style={{maxWidth:160}}><option>Risk: All</option><option>LOW</option><option>MEDIUM</option><option>HIGH</option></select>
          <select className="input" style={{maxWidth:160}}><option>Status: All</option><option>OK</option><option>REVIEW</option><option>BLOCKED</option></select>
        </div>
        <table width="100%"> 
          <thead><tr><th>ID</th><th>Date/Time</th><th>Cardholder</th><th>Merchant</th><th>Country</th><th>Amount</th><th>Risk</th><th>Status</th><th>Actions</th></tr></thead>
          <tbody>{txns.map(t=> (<tr key={t.id}><td>#{t.id}</td><td>{t.datetime}</td><td>{t.cardholder}</td><td>{t.merchant}</td><td>{t.country}</td><td>{t.amount}</td><td>{t.risk}</td><td>{t.status}</td><td><button className="button">Review</button> <button className="button" style={{background:'#d00'}}>Block</button></td></tr>))}</tbody>
        </table>
        <div className="flex" style={{marginTop:10}}><button className="button">Export CSV</button><button className="button">Export JSON</button></div>
      </div>)}
      {tab==='Audit Reports' && (<div style={{marginTop:12}}><h4>Audit Log</h4>{audit.length===0? (<p>No audit entries found.</p>): (<ul style={{listStyle:'none', paddingLeft:0}}>{audit.map(x=> (<li key={x.id} style={{padding:'10px 8px', borderBottom:'1px solid #eee'}}>{x.userName} — {x.action} ({x.date})</li>))}</ul>)}</div>)}
    </div>
  )
}
