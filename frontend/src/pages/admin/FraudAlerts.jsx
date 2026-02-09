import React from 'react'

/**
 * Displays fraud alerts with severity badges
 */
export default function FraudAlerts({ alerts }) {
  console.log('FraudAlerts received:', alerts)
  
  if (!alerts || alerts.length === 0) {
    return <p>No alerts found.</p>
  }

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

  // Helper function for status badge colors
  const getStatusColor = (status) => {
    switch(status) {
      case 'OPEN': return '#dc3545'
      case 'ACKNOWLEDGED': return '#ffc107'
      case 'CLOSED': return '#28a745'
      default: return '#6c757d'
    }
  }

  return (
    <div>
      <h3>Fraud Alerts ({alerts.length})</h3>
      <table width="100%">
        <thead>
          <tr>
            <th>Severity</th>
            <th>Status</th>
            <th>Title</th>
            <th>Created</th>
          </tr>
        </thead>
        <tbody>
          {alerts.map(alert => (
            <tr key={alert.id}>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: getSeverityColor(alert.severity),
                  color: 'white'
                }}>
                  {alert.severity}
                </span>
              </td>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: getStatusColor(alert.status),
                  color: 'white'
                }}>
                  {alert.status}
                </span>
              </td>
              <td>
                <div><strong>{alert.title}</strong></div>
                {alert.description && (
                  <div style={{ fontSize: '12px', color: '#6c757d', marginTop: '2px' }}>
                    {alert.description.length > 80 
                      ? alert.description.substring(0, 80) + '...' 
                      : alert.description}
                  </div>
                )}
              </td>
              <td>{new Date(alert.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
