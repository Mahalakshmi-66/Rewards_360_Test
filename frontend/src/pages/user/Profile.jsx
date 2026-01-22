
import React, { useEffect, useState } from 'react'
import api from '../../api/client'

export default function Profile(){
  const [me, setMe] = useState(null)
  useEffect(()=>{ (async()=>{ const {data} = await api.get('/user/me'); setMe(data) })() },[])
  if(!me) return null
  const prefs = (me.profile?.preferences||'').split(',').filter(Boolean)
  return (
    <div className="card"> 
      <h3>Profile</h3>
      <p><strong>Name:</strong> {me.name}</p>
      <p><strong>Email:</strong> {me.email}</p>
      <p><strong>Phone:</strong> {me.phone}</p>
      <p><strong>Preferences:</strong> {prefs.join(', ')||'—'}</p>
      <p><strong>Communication:</strong> {me.profile?.communication||'Email'}</p>
    </div>
  )
}
