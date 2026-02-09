import React from 'react'

/**
 * Displays audit log entries
 */
export default function AuditReports({ auditLog }) {
  console.log('AuditReports received:', auditLog)
  
  if (!auditLog || auditLog.length === 0) {
    return <p>No audit entries found.</p>
  }

  // Helper function for action badge colors
  const getActionColor = (action) => {
    if (action?.includes('LOGIN') || action?.includes('LOGOUT')) return '#0d6efd'
    if (action?.includes('BLOCK')) return '#dc3545'
    if (action?.includes('REVIEW')) return '#ffc107'
    if (action?.includes('EXPORT')) return '#6f42c1'
    if (action?.includes('CREATE') || action?.includes('UPDATE')) return '#198754'
    return '#6c757d'
  }

  return (
    <div>
      <h3>Audit Log ({auditLog.length})</h3>
      <table width="100%">
        <thead>
          <tr>
            <th>User</th>
            <th>Action</th>
            <th>Entity Type</th>
            <th>Entity ID</th>
            <th>Details</th>
            <th>Timestamp</th>
          </tr>
        </thead>
        <tbody>
          {auditLog.map(entry => (
            <tr key={entry.id}>
              <td><strong>{entry.username}</strong></td>
              <td>
                <span style={{
                  padding: '4px 8px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  backgroundColor: getActionColor(entry.action),
                  color: 'white'
                }}>
                  {entry.action}
                </span>
              </td>
              <td>{entry.entityType || '-'}</td>
              <td>{entry.entityId || '-'}</td>
              <td>{entry.details || '-'}</td>
              <td>{new Date(entry.createdAt).toLocaleString()}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  )
}
