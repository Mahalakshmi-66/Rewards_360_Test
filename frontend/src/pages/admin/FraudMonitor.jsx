import React, { useEffect, useState } from 'react'
import api from '../../api/client'
import FraudAlerts from './FraudAlerts'
import FraudAnomalies from './FraudAnomalies'
import TransactionMonitoring from './TransactionMonitoring'
import AuditReports from './AuditReports'

/**
 * FRAUD MONITORING DASHBOARD - Main component with tabs
 */
export default function FraudMonitor() {
  const [activeTab, setActiveTab] = useState('Alerts')
  const [alerts, setAlerts] = useState([])
  const [anomalies, setAnomalies] = useState([])
  const [auditLog, setAuditLog] = useState([])
  const [transactions, setTransactions] = useState([])

  console.log('FraudMonitor rendered, activeTab:', activeTab)

  // Load all fraud data on mount
  useEffect(() => {
    loadFraudData()
  }, [])

  // Fetch data from API
  const loadFraudData = async () => {
    try {
      console.log('Loading fraud data...')
      const [alertsRes, anomaliesRes, auditRes, transactionsRes] = await Promise.all([
        api.get('/admin/fraud/alerts').catch(() => ({ data: [] })),
        api.get('/admin/fraud/anomalies').catch(() => ({ data: [] })),
        api.get('/admin/fraud/audit').catch(() => ({ data: [] })),
        api.get('/admin/fraud/transactions').catch(() => ({ data: [] }))
      ])

      console.log('Loaded data:', { 
        alerts: alertsRes.data?.length, 
        anomalies: anomaliesRes.data?.length,
        audit: auditRes.data?.length,
        transactions: transactionsRes.data?.length
      })

      setAlerts(alertsRes.data || [])
      setAnomalies(anomaliesRes.data || [])
      setAuditLog(auditRes.data || [])
      setTransactions(transactionsRes.data || [])
    } catch (error) {
      console.error('Error loading fraud data:', error)
    }
  }

  // Update transaction status
  const updateTransaction = (id, newStatus) => {
    setTransactions(prev =>
      prev.map(t => (t.id === id ? { ...t, status: newStatus } : t))
    )
  }

  return (
    <div className="card">
      <h2>Fraud Monitoring Dashboard</h2>
      
      {/* Tab Navigation */}
      <div className="tabs">
        <select value={activeTab} onChange={e => setActiveTab(e.target.value)}>
          <option>Alerts</option>
          <option>Anomalies</option>
          <option>Transaction Monitoring</option>
          <option>Audit Reports</option>
        </select>
      </div>

      {/* Tab Content */}
      <div style={{ marginTop: 12 }}>
        {activeTab === 'Alerts' && <FraudAlerts alerts={alerts} />}
        {activeTab === 'Anomalies' && <FraudAnomalies anomalies={anomalies} />}
        {activeTab === 'Transaction Monitoring' && (
          <TransactionMonitoring
            transactions={transactions}
            onUpdate={updateTransaction}
          />
        )}
        {activeTab === 'Audit Reports' && <AuditReports auditLog={auditLog} />}
      </div>
    </div>
  )
}
