
import React, { useEffect, useState } from 'react'
import api from '../../api/client'

export default function Offers(){
  const [offers, setOffers] = useState([])
  const [me, setMe] = useState(null)
  const [confirm, setConfirm] = useState(null)
  useEffect(()=>{ (async()=>{ const o = await api.get('/user/offers'); setOffers(o.data); const m = await api.get('/user/me'); setMe(m.data) })() },[])
  const open = (o)=> setConfirm(o)
  const close = ()=> setConfirm(null)
  const redeem = async()=>{ await api.post('/user/redeem',{ offerId:confirm.id, store:'Online' }); setConfirm(null); alert('Redemption confirmed! Check Redemptions page.'); }
  return (
    <div>
      <div className="card"><h3>Member Offers</h3></div>
      <div className="grid cols-3"> 
        {offers.map(o=> (
          <div className="card" key={o.id}>
            <h4>{o.title}</h4>
            <p>{o.description}</p>
            <p>Cost: {o.costPoints} pts</p>
            <button className="button" onClick={()=>open(o)}>Redeem</button>
          </div>
        ))}
      </div>
      {confirm && (
        <div className="card" style={{position:'fixed', right:20, bottom:20, maxWidth:360}}>
          <h4>Confirm Redemption</h4>
          <p>{confirm.title}</p>
          <p>Cost: {confirm.costPoints} pts</p>
          <div className="flex"><button className="button" onClick={redeem}>Confirm & Redeem</button><button className="button" style={{background:'#666'}} onClick={close}>Cancel</button></div>
        </div>
      )}
      {me && (<div className="card"><strong>Your balance:</strong> {me.profile?.pointsBalance}</div>)}
    </div>
  )
}
