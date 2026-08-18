// src/components/MissingInfoAlert.jsx

function MissingInfoAlert({ missingInfo }) {
  if (!missingInfo || missingInfo.length === 0) return null

  return (
    <p style={{ color: 'orange' }}>
      ⚠️ {missingInfo[0]}
    </p>
  )
}

export default MissingInfoAlert