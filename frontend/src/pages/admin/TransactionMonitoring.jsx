import React, { useState, useEffect } from 'react'

/**
 * Transaction monitoring with filters and actions
 */
export default function TransactionMonitoring({ transactions, onUpdate }) {
  const [filtered, setFiltered] = useState(transactions)
  const [search, setSearch] = useState('')
  const [riskFilter, setRiskFilter] = useState('All')
  const [statusFilter, setStatusFilter] = useState('All')

  console.log('TransactionMonitoring rendered with transactions:', transactions)

  // Apply filters when any filter changes
  useEffect(() => {
    let result = transactions

    // Search filter
    if (search) {
      const term = search.toLowerCase()
      result = result.filter(t =>
        (t.transactionId || '').toLowerCase().includes(term) ||
        (t.accountId || '').toLowerCase().includes(term) ||
        (t.merchantName || '').toLowerCase().includes(term) ||
        (t.location || '').toLowerCase().includes(term) ||
        String(t.amount || '').toLowerCase().includes(term)
      )
    }

    // Risk level filter
    if (riskFilter !== 'All') {
      result = result.filter(t => t.riskLevel === riskFilter)
    }

    // Status filter
    if (statusFilter !== 'All') {
      result = result.filter(t => t.status === statusFilter)
    }

    setFiltered(result)
    console.log('Filtered transactions:', result)
  }, [search, riskFilter, statusFilter, transactions])

  // Mark transaction for review
  const handleReview = (id) => {
    const txn = transactions.find(t => t.id === id)
    if (txn) {
      onUpdate(id, 'REVIEW')
      alert(`Transaction #${txn.transactionId} marked for REVIEW`)
    }
  }

  // Block transaction
  const handleBlock = (id) => {
    const txn = transactions.find(t => t.id === id)
    if (txn) {
      onUpdate(id, 'BLOCKED')
      alert(`Transaction #${txn.transactionId} has been BLOCKED`)
    }
  }

  // Export as CSV
  const exportCSV = () => {
    if (filtered.length === 0) {
      alert('No transactions to export')
      return
    }

    const headers = ['ID', 'Date', 'Account', 'Merchant', 'Location', 'Amount', 'Risk', 'Status']
    const rows = filtered.map(t => [
      t.transactionId,
      new Date(t.createdAt).toLocaleString(),
      t.accountId || 'N/A',
      t.merchantName,
      t.location,
      `${t.amount} ${t.currency}`,
      t.riskLevel,
      t.status
    ])

    let csv = headers.join(',') + '\n'
    rows.forEach(row => {
      csv += row.map(cell => `"${cell}"`).join(',') + '\n'
    })

    downloadFile(csv, 'text/csv', 'csv')
    alert(`Exported ${filtered.length} transactions to CSV`)
  }

  // Export as JSON
  const exportJSON = () => {
    if (filtered.length === 0) {
      alert('No transactions to export')
      return
    }

    const json = JSON.stringify(filtered, null, 2)
    downloadFile(json, 'application/json', 'json')
    alert(`Exported ${filtered.length} transactions to JSON`)
  }

  // Helper to download file
  const downloadFile = (content, type, extension) => {
    const blob = new Blob([content], { type })
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `transactions_${new Date().toISOString().split('T')[0]}.${extension}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
  }

  return (
    <div>
      {/* Filters */}
      <div className="flex" style={{ marginBottom: 10 }}>
        <input
          className="input"
          placeholder="Search by ID, cardholder, merchant..."
          value={search}
          onChange={e => setSearch(e.target.value)}
        />

        <select
          className="input"
          style={{ maxWidth: 160 }}
          value={riskFilter}
          onChange={e => setRiskFilter(e.target.value)}
        >
          <option>All</option>
          <option>LOW</option>
          <option>MEDIUM</option>
          <option>HIGH</option>
        </select>

        <select
          className="input"
          style={{ maxWidth: 160 }}
          value={statusFilter}
          onChange={e => setStatusFilter(e.target.value)}
        >
          <option>All</option>
          <option>CLEARED</option>
          <option>REVIEW</option>
          <option>BLOCKED</option>
        </select>
      </div>

      {/* Transaction Table */}
      <table width="100%">
        <thead>
          <tr>
            <th>ID</th>
            <th>Date</th>
            <th>Account</th>
            <th>Merchant</th>
            <th>Location</th>
            <th>Amount</th>
            <th>Risk</th>
            <th>Status</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {filtered.length === 0 ? (
            <tr>
              <td colSpan="9" style={{ textAlign: 'center', padding: '20px' }}>
                No transactions found. Total transactions available: {transactions.length}
              </td>
            </tr>
          ) : (
            filtered.map(t => (
            <tr key={t.id}>
              <td>#{t.transactionId}</td>
              <td>{new Date(t.createdAt).toLocaleString()}</td>
              <td>{t.accountId || 'N/A'}</td>
              <td>{t.merchantName}</td>
              <td>{t.location}</td>
              <td>{t.amount} {t.currency}</td>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: 
                    t.riskLevel === 'CRITICAL' ? '#dc3545' :
                    t.riskLevel === 'HIGH' ? '#fd7e14' :
                    t.riskLevel === 'MEDIUM' ? '#ffc107' :
                    '#28a745',
                  color: 'white'
                }}>
                  {t.riskLevel}
                </span>
              </td>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: 
                    t.status === 'BLOCKED' ? '#dc3545' :
                    t.status === 'REVIEW' ? '#ffc107' :
                    '#28a745',
                  color: 'white'
                }}>
                  {t.status}
                </span>
              </td>
              <td>
                <button 
                  className="button" 
                  onClick={() => handleReview(t.id)}
                  disabled={t.status === 'BLOCKED'}
                  style={{ marginRight: '5px' }}
                >
                  Review
                </button>
                <button
                  className="button"
                  style={{ background: t.status === 'BLOCKED' ? '#999' : '#d00' }}
                  onClick={() => handleBlock(t.id)}
                  disabled={t.status === 'BLOCKED'}
                >
                  Block
                </button>
              </td>
            </tr>
            ))
          )}
        </tbody>
      </table>

      {/* Export Buttons */}
      <div className="flex" style={{ marginTop: 10 }}>
        <button className="button" onClick={exportCSV}>
          Export CSV
        </button>
        <button className="button" onClick={exportJSON}>
          Export JSON
        </button>
      </div>
    </div>
  )
}
