
import React from 'react'
import { Link } from 'react-router-dom'

export default function Promotions(){
  return (
    <div className="grid cols-3"> 
      <div className="card"> 
        <h3>Create Campaign</h3>
        <p>Quickly design and launch a new promotional campaign. Attach audiences, set discounts and preview before publishing.</p>
        <Link className="button" to="/admin/campaigns/new">Create Campaign</Link>
      </div>
      <div className="card"> 
        <h3>Analytics</h3>
        <p>High-level metrics of ongoing campaigns.</p>
      </div>
      <div className="card"> 
        <h3>View All Offers</h3>
        <Link className="button" to="/admin/offers">Go</Link>
      </div>
    </div>
  )
}
