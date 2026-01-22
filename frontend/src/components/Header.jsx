
import React from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function Header(){
  const navigate = useNavigate()
  const role = localStorage.getItem('role')
  const logout = ()=>{ localStorage.clear(); navigate('/login') }
  return (
    <header>
      <div className="navbar flex"> 
        <strong>Rewards360</strong>
        <nav style={{marginLeft:'auto'}}>
          {role==='ADMIN' && (<>
            <Link to="/admin">Promotions</Link>
            <Link to="/admin/offers">Offers</Link>
            <Link to="/admin/fraud">Fraud Monitor</Link>
            <Link to="/admin/reports">Report</Link>
          </>)}
          {role==='USER' && (<>
            <Link to="/user">Dashboard</Link>
            <Link to="/user/profile">Profile</Link>
            <Link to="/user/offers">Offers</Link>
            <Link to="/user/redemptions">Redemptions</Link>
            <Link to="/user/transactions">Transactions</Link>
          </>)}
          {role && <button className="button" onClick={logout}>Logout</button>}
        </nav>
      </div>
    </header>
  )
}
