
import React, { useEffect, useState } from 'react'
import api from '../../api/client'

export default function Transactions(){
  const [items, setItems] = useState([])
  const [me, setMe] = useState(null)
  useEffect(()=>{ (async()=>{ const {data} = await api.get('/user/transactions'); setItems(data); const meR = await api.get('/user/me'); setMe(meR.data) })() },[])
  return (
    <div className="card"> 
      <h3>Transactions</h3>
      {me && (<div className="flex"><div className="badge">Remaining Points {me.profile?.pointsBalance}</div></div>)}
      <table width="100%"> 
        <thead><tr><th>ID</th><th>Type</th><th>Note</th><th>Store</th><th>Date</th><th>Expiry</th><th>Points +/-</th></tr></thead>
        <tbody>
          {items.map(it=> (
            <tr key={it.id}><td>{it.externalId}</td><td>{it.type}</td><td>{it.note}</td><td>{it.store}</td><td>{it.date}</td><td>{it.expiry||''}</td><td>{it.pointsEarned>0?`+${it.pointsEarned}`:(it.pointsRedeemed>0?`-${it.pointsRedeemed}`:0)}</td></tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
