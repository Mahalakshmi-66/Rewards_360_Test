import React, { useState } from 'react'
import api from '../../api/client'

/**
 * Simple Fraud Detection Control - trigger automated fraud analysis
 */
export default function FraudDetectionPanel({ onAnalysisComplete }) {
  const [loading, setLoading] = useState(false)

  const analyzeAllTransactions = async () => {
    if (!confirm('Run automated fraud detection on all transactions?')) {
      return
    }

    setLoading(true)

    try {
      const response = await api.post('/admin/fraud/analyze-all')
      alert(`✅ Analysis Complete!\n\nAnalyzed: ${response.data.totalAnalyzed}\nCleared: ${response.data.cleared}\nReview: ${response.data.flaggedForReview}\nBlocked: ${response.data.blocked}`)
      
      if (onAnalysisComplete) {
        onAnalysisComplete()
      }
    } catch (error) {
      console.error('Analysis failed:', error)
      alert('❌ Analysis failed: ' + (error.response?.data?.message || error.message))
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      padding: '12px 15px',
      marginBottom: '15px',
      backgroundColor: '#f8f9fa',
      border: '1px solid #dee2e6',
      borderRadius: '4px',
      display: 'flex',
      justifyContent: 'space-between',
      alignItems: 'center',
      flexWrap: 'wrap',
      gap: '10px'
    }}>
      <div style={{ flex: 1, minWidth: '250px' }}>
        <h4 style={{ margin: 0, marginBottom: '5px', fontSize: '16px' }}>Fraud Detection Rules</h4>
        <div style={{ fontSize: '13px', color: '#6c757d' }}>
          <strong>Amount:</strong> $1K+ (Medium), $10K+ (High), $50K+ (Critical) | 
          <strong> Velocity:</strong> 5+ txns/30min | 
          <strong> Geo:</strong> Different countries/2hr | 
          <strong> High-Risk:</strong> Crypto, Luxury, Gambling
        </div>
      </div>
      
      <button 
        className="button"
        onClick={analyzeAllTransactions}
        disabled={loading}
        style={{
          padding: '8px 16px',
          fontSize: '14px',
          fontWeight: 'bold',
          backgroundColor: loading ? '#6c757d' : '#0d6efd',
          cursor: loading ? 'not-allowed' : 'pointer',
          whiteSpace: 'nowrap'
        }}
      >
        {loading ? '⏳ Analyzing...' : '▶ Run Fraud Analysis'}
      </button>
    </div>
  )
}
