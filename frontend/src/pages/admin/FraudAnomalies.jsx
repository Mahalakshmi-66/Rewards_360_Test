import React from 'react'

/**
 * Displays fraud anomalies detected in the system
 */
export default function FraudAnomalies({ anomalies }) {
  console.log('FraudAnomalies received:', anomalies)
  
  if (!anomalies || anomalies.length === 0) {
    return <p>No anomalies found.</p>
  }

  // Show only the most recent 10 anomalies
  const recentAnomalies = anomalies.slice(0, 10)

  // Helper function for severity badge colors
  const getSeverityColor = (severity) => {
    switch(severity) {
      case 'CRITICAL': return '#dc3545'
      case 'HIGH': return '#fd7e14'
      case 'MEDIUM': return '#ffc107'
      case 'LOW': return '#28a745'
      default: return '#6c757d'
    }
  }

  return (
    <div>
      <h3>Transaction Anomalies ({anomalies.length > 10 ? `Showing 10 of ${anomalies.length}` : anomalies.length})</h3>
      <table width="100%">
        <thead>
          <tr>
            <th>Transaction ID</th>
            <th>Anomaly Type</th>
            <th>Severity</th>
            <th>Reason</th>
            <th>Detected At</th>
          </tr>
        </thead>
        <tbody>
          {recentAnomalies.map(anomaly => (
            <tr key={anomaly.id}>
              <td><strong>#{anomaly.transactionId}</strong></td>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  backgroundColor: '#e9ecef',
                  color: '#495057'
                }}>
                  {anomaly.anomalyType}
                </span>
              </td>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: getSeverityColor(anomaly.severity),
                  color: 'white'
                }}>
                  {anomaly.severity}
                </span>
              </td>
              <td>{anomaly.flaggedReason}</td>
              <td>{new Date(anomaly.detectedAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
