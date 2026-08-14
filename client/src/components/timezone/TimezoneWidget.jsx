import React from 'react';
import { Clock, AlertTriangle } from 'lucide-react';

export default function TimezoneWidget({ senderTz = 'Asia/Seoul', receiverTz = 'America/New_York', isOffHours = false, nextAvailableTime = '' }) {
  const getFormattedTime = (tz) => {
    try {
      return new Intl.DateTimeFormat('ko-KR', {
        timeZone: tz,
        hour: '2-digit',
        minute: '2-digit',
        hour12: true
      }).format(new Date());
    } catch {
      return '16:00';
    }
  };

  return (
    <div style={{
      background: 'white',
      border: '1px solid var(--border-color)',
      borderRadius: 'var(--radius-md)',
      padding: '12px 16px',
      marginBottom: '16px',
      boxShadow: 'var(--shadow-sm)'
    }}>
      <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <Clock size={16} color="var(--primary-orange)" />
          <span style={{ fontSize: '13px', fontWeight: 600 }}>발신 (내 시간)</span>
          <span style={{ fontSize: '13px', color: 'var(--text-muted)' }}>{getFormattedTime(senderTz)}</span>
        </div>
        <span style={{ color: 'var(--text-subtle)' }}>⇄</span>
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
          <span style={{ fontSize: '13px', fontWeight: 600 }}>수신 (상대방)</span>
          <span style={{ fontSize: '13px', color: isOffHours ? 'var(--warning-yellow)' : 'var(--text-muted)', fontWeight: isOffHours ? 700 : 500 }}>
            {getFormattedTime(receiverTz)}
          </span>
        </div>
      </div>

      {isOffHours && (
        <div style={{
          marginTop: '10px',
          padding: '8px 12px',
          background: 'var(--warning-yellow-light)',
          borderRadius: 'var(--radius-sm)',
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          fontSize: '12px',
          color: '#92400e'
        }}>
          <AlertTriangle size={14} color="#d97706" />
          <span>수신자 비업무(야간) 시간대입니다. 확인 가능 시점: <strong>{nextAvailableTime || '오전 9:00'}</strong></span>
        </div>
      )}
    </div>
  );
}
