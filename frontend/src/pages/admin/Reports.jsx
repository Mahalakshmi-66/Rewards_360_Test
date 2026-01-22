
import React, { useMemo, useState } from 'react'

export default function Reports(){
  const metrics = useMemo(()=>({ engagement:75, redemption:45, retention:65, previewPoints:[58,51.5,45,49,55,61,63] }),[])
  const [form, setForm] = useState({ id:'58103', from:'2025-11-01', to:'2025-12-10', engagement:true, redemption:true, retention:true })
  const onChange = e=>{ const {name, value, type, checked} = e.target; setForm(p=>({...p, [name]: type==='checkbox'? checked : value})) }
  const downloadBlob = (content, mime, filename)=>{ const blob = new Blob([content], {type:mime}); const url = URL.createObjectURL(blob); const a = document.createElement('a'); a.href = url; a.download = filename; a.click(); URL.revokeObjectURL(url) }
  const exportCSV = ()=>{ const rows = [['Metric','Value'],['Engagement',metrics.engagement],['Redemption',metrics.redemption],['Retention',metrics.retention]]; const csv = rows.map(r=>r.join(',')).join(''); downloadBlob(csv, 'text/csv', 'rewards360_report.csv') }
  const exportJSON = ()=>{ const obj = { reportId:form.id, from:form.from, to:form.to, selected:{ engagement:form.engagement, redemption:form.redemption, retention:form.retention }, data:{ engagement:metrics.engagement, redemption:metrics.redemption, retention:metrics.retention } }; downloadBlob(JSON.stringify(obj, null, 2), 'application/json', 'rewards360_report.json') }
  return (
    <div className="grid cols-2"> 
      <div className="card"> 
        <h3>Loyalty Analytics & Reporting — Dashboard</h3>
        <div className="grid cols-3"> 
          <MetricCard label="Engagement rate" value={`${metrics.engagement}%`} />
          <MetricCard label="Redemption rate" value={`${metrics.redemption}%`} />
          <MetricCard label="Retention rate" value={`${metrics.retention}%`} />
        </div>
        <div style={{marginTop:12}}>
          <div className="badge">Preview</div>
          <div style={{fontSize:12, color:'#555', marginTop:6}}>(Simple preview: {metrics.previewPoints.join(' → ')})</div>
        </div>
      </div>
      <div className="card"> 
        <h3>Generate Report</h3>
        <div style={{marginBottom:8}}>
          <label>Report ID</label>
          <input className="input" name="id" value={form.id} onChange={onChange} placeholder="e.g., 58103" />
        </div>
        <div className="grid cols-2"> 
          <div><label>Date from</label><input className="input" type="date" name="from" value={form.from} onChange={onChange} /></div>
          <div><label>Date to</label><input className="input" type="date" name="to" value={form.to} onChange={onChange} /></div>
        </div>
        <div style={{marginTop:8}}>
          <label>Metrics</label>
          <div className="flex" style={{flexWrap:'wrap', gap:12}}>
            <label><input type="checkbox" name="engagement" checked={form.engagement} onChange={onChange} /> Engagement rate</label>
            <label><input type="checkbox" name="redemption" checked={form.redemption} onChange={onChange} /> Redemption rate</label>
            <label><input type="checkbox" name="retention" checked={form.retention} onChange={onChange} /> Retention rate</label>
          </div>
        </div>
        <div style={{marginTop:10}}>
          <button className="button" style={{marginRight:8}} onClick={()=>alert('Report generated (stub).')}>Generate report</button>
          <button className="button" style={{marginRight:8}} onClick={exportCSV}>Export CSV</button>
          <button className="button" onClick={exportJSON}>Export JSON</button>
        </div>
      </div>
    </div>
  )
}

function MetricCard({label, value}){
  return (
    <div className="card" style={{marginBottom:0}}>
      <div style={{fontSize:12, color:'#555'}}>{label}</div>
      <div style={{fontSize:24, fontWeight:700}}>{value}</div>
    </div>
  )
}
