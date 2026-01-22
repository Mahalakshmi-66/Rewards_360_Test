
import React, { useEffect, useState } from 'react'
import api from '../../api/client'

export default function Redemptions(){
  const [items, setItems] = useState([])
  useEffect(()=>{ (async()=>{ const {data} = await api.get('/user/redemptions'); setItems(data) })() },[])
  return (
    <div className="card"> 
      <h3>Redemptions</h3>
      <table width="100%"> 
        <thead><tr><th>Date</th><th>Offer</th><th>Points</th><th>Store</th><th>Confirmation</th></tr></thead>
        <tbody>
          {items.map(it=> (
            <tr key={it.id}><td>{it.date}</td><td>{it.offerTitle}</td><td>{it.costPoints}</td><td>{it.store}</td><td>{it.confirmationCode}</td></tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
