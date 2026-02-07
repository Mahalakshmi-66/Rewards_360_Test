import React from 'react'
import { useUser } from '../../context/UserContext'
import '../../../styles/Profile.css'

export default function Profile() {
  const { user, loading } = useUser()
  
  if (loading || !user) return <div className="profile-page">Loading...</div>
  
  const prefs = (user.profile?.preferences || '').split(',').filter(Boolean)
  
  return (
    <div className="profile-page">
      <div className="profile-card">
        <div className="profile-header">
          <div className="profile-avatar">
            {user.name?.charAt(0).toUpperCase()}
          </div>
          <div className="profile-header-info">
            <h2 className="profile-name">{user.name}</h2>
            <span className="profile-tier">{user.profile?.loyaltyTier || 'Bronze'} Member</span>
          </div>
        </div>
        
        <div className="profile-details">
          <div className="profile-item">
            <span className="profile-label">Email</span>
            <span className="profile-value">{user.email}</span>
          </div>
          <div className="profile-item">
            <span className="profile-label">Phone</span>
            <span className="profile-value">{user.phone}</span>
          </div>
          <div className="profile-item">
            <span className="profile-label">Points Balance</span>
            <span className="profile-value profile-points">{user.profile?.pointsBalance ?? 0}</span>
          </div>
          <div className="profile-item">
            <span className="profile-label">Preferences</span>
            <span className="profile-value">{prefs.join(', ') || '—'}</span>
          </div>
          <div className="profile-item">
            <span className="profile-label">Communication</span>
            <span className="profile-value">{user.profile?.communication || 'Email'}</span>
          </div>
        </div>
      </div>
    </div>
  )
}