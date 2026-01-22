
import React, { useEffect, useState } from 'react'
import api from '../../api/client'

export default function OffersAdmin(){
  const [items, setItems] = useState([])
  const [o, setO] = useState({ title:'', category:'Electronics', description:'', costPoints:0, imageUrl:'' })
  const [err, setErr] = useState('')
  const [msg, setMsg] = useState('')
  const load = async()=>{ try{ const {data} = await api.get('/admin/offers'); setItems(data) }catch(e){ setErr('Failed to load offers') } }
  useEffect(()=>{ load() },[])
  const validate = ()=>{
    if(!o.title.trim()) return 'Title is required'
    if(!o.category.trim()) return 'Category is required'
    if(!o.description.trim()) return 'Description is required'
    if(Number.isNaN(Number(o.costPoints)) || Number(o.costPoints)<0) return 'Cost points must be a non-negative number'
    return ''
  }
  const submit = async e=>{ e.preventDefault(); setErr(''); setMsg(''); const v = validate(); if(v){ setErr(v); return } try{ await api.post('/admin/offers', o); setMsg('Offer created successfully'); setO({ title:'', category:'Electronics', description:'', costPoints:0, imageUrl:'' }); load() }catch(ex){ setErr('Failed to create offer') } }
  return (
    <div className="grid cols-2"> 
      <div className="card"> 
        <h3>Create Offer</h3>
        <form onSubmit={submit}>
          <label>Title</label>
          <input className="input" placeholder="e.g., Festive 15% Off" value={o.title} onChange={e=>setO(p=>({...p,title:e.target.value}))} required />
          <label>Category</label>
          <input className="input" placeholder="e.g., Electronics" value={o.category} onChange={e=>setO(p=>({...p,category:e.target.value}))} />
          <label>Description</label>
          <textarea className="input" placeholder="Short description visible to users" value={o.description} onChange={e=>setO(p=>({...p,description:e.target.value}))} />
          <label>Cost Points</label>
          <input className="input" type="number" placeholder="e.g., 350" value={o.costPoints} onChange={e=>setO(p=>({...p,costPoints:parseInt(e.target.value||0,10)}))} />
          <label>Image URL (optional)</label>
          <input className="input" placeholder="https://..." value={o.imageUrl} onChange={e=>setO(p=>({...p,imageUrl:e.target.value}))} />
          {err && <div className="error" style={{marginTop:6}}>{err}</div>}
          {msg && <div className="badge" style={{marginTop:6}}>{msg}</div>}
          <div style={{marginTop:10}}>
            <button className="button">Create Offer</button>
          </div>
        </form>
      </div>
      <div className="card"> 
        <h3>Offers</h3>
        {items?.length===0 ? (<p>No offers yet. Create one on the left.</p>) : (
          <ul style={{listStyle:'none', paddingLeft:0}}>
            {items.map(i=> (
              <li key={i.id} style={{padding:'10px 8px', borderBottom:'1px solid #eee', display:'flex', justifyContent:'space-between', alignItems:'center'}}>
                <div style={{maxWidth:'75%'}}>
                  <strong>{i.title}</strong> — {i.category}
                  <div style={{color:'#555', fontSize:13}}>{i.description}</div>
                  <div className="badge" style={{marginTop:4}}>Cost: {i.costPoints} pts</div>
                </div>
                {i.imageUrl && (
                  <img src={i.imageUrl} alt={i.title} style={{width:80, height:50, objectFit:'cover', borderRadius:6}} onError={(e)=>{ e.currentTarget.style.display='none' }} />
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  )
}
