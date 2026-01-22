
import React, { useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import api from '../../api/client'

export default function Register(){
  const [f, setF] = useState({ name:'', email:'', phone:'', password:'', role:'USER', preferences:[], communication:'Email' })
  const [err, setErr] = useState({})
  const navigate = useNavigate()

  const validate = ()=>{
    const e={}
    if(!f.name) e.name='Name is required'
    if(!/^[^@\s]+@[^@\s]+\.[^@\s]+$/.test(f.email)) e.email='Valid email is required'
    if(!/^\d{10}$/.test(f.phone)) e.phone='Phone must be 10 digits'
    if(f.password.length<8) e.password='Password must be at least 8 characters'
    return e
  }

  const onChange = (e)=>{
    const {name, value, type, checked} = e.target
    if(name==='preferences'){
      const arr = new Set(f.preferences)
      if(checked) arr.add(value); else arr.delete(value)
      setF(p=>({...p, preferences:[...arr]}));
    } else setF(p=>({...p, [name]: value}))
  }

  const submit = async (e)=>{
    e.preventDefault()
    const v = validate(); setErr(v)
    if(Object.keys(v).length) return
    try{
      await api.post('/auth/register', {
        name:f.name, email:f.email, phone:f.phone, password:f.password,
        role:f.role, preferences:f.preferences.join(','), communication:f.communication
      })
      navigate('/login')
    }catch(ex){ setErr({ api:'Registration failed' }) }
  }

  return (
    <div className="card" style={{margin:'20px auto', maxWidth:720}}>
      <h2>Create account (User/Admin)</h2>
      <form onSubmit={submit} className="grid cols-2"> 
        <div>
          <label>Register as</label>
          <select className="input" name="role" value={f.role} onChange={onChange}>
            <option>USER</option>
            <option>ADMIN</option>
          </select>
        </div>
        <div>
          <label>Name</label>
          <input className="input" name="name" value={f.name} onChange={onChange} />
          {err.name && <div className="error">{err.name}</div>}
        </div>
        <div>
          <label>Email</label>
          <input className="input" name="email" value={f.email} onChange={onChange} />
          {err.email && <div className="error">{err.email}</div>}
        </div>
        <div>
          <label>Phone</label>
          <input className="input" name="phone" value={f.phone} onChange={onChange} />
          {err.phone && <div className="error">{err.phone}</div>}
        </div>
        <div>
          <label>Password</label>
          <input className="input" type="password" name="password" value={f.password} onChange={onChange} />
          {err.password && <div className="error">{err.password}</div>}
        </div>
        <div>
          <label>Preferences (user role)</label>
          <div className="flex" style={{flexWrap:'wrap', gap:12}}>
            {['Fashion','Electronics','Groceries','Beauty','Home'].map(opt=> (
              <label key={opt}><input type="checkbox" name="preferences" value={opt} onChange={onChange} /> {opt}</label>
            ))}
          </div>
        </div>
        <div>
          <label>Communication</label>
          <div className="flex"> 
            {['Email','SMS','WhatsApp'].map(c => (
              <label key={c}><input type="radio" name="communication" value={c} checked={f.communication===c} onChange={onChange}/> {c}</label>
            ))}
          </div>
        </div>
        <div style={{gridColumn:'1/-1'}}>
          {err.api && <div className="error">{err.api}</div>}
          <button className="button" type="submit">Register</button>
          <span style={{marginLeft:12}}>Already have an account? <a className="link" href="/login">Login</a></span>
        </div>
      </form>
    </div>
  )
}
