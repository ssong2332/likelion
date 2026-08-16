import React from 'react';
import { Clock, AlertTriangle } from 'lucide-react';

export default function TimezoneWidget({ senderTz = 'Asia/Seoul', receiverTz = 'America/New_York', isOffHours, nextAvailableTime = '' }) {
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

  const getReceiverHour = (tz) => {
    try {
      const hourStr = new Intl.DateTimeFormat('en-US', {
        timeZone: tz,
        hour: 'numeric',
        hour12: false
      }).format(new Date());
      return parseInt(hourStr, 10);
    } catch {
      return 12;
    }
  };

  const currentHour = getReceiverHour(receiverTz);
  // 오전 9시 이전(< 9) 또는 오후 6시 이후(>= 18)는 야간/비업무시간
  const isCurrentlyOffHours = (currentHour < 9 || currentHour >= 18);

  const getNextAvailableTimeText = () => {
    if (nextAvailableTime) return nextAvailableTime;
    const hoursLeft = currentHour < 9 ? (9 - currentHour) : (24 - currentHour + 9);
    return `현지 시각 오전 09:00 (약 ${hoursLeft}시간 뒤)`;
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
          <span style={{ fontSize: '13px', color: isCurrentlyOffHours ? '#d97706' : 'var(--text-muted)', fontWeight: isCurrentlyOffHours ? 700 : 500 }}>
            {getFormattedTime(receiverTz)}
          </span>
        </div>
      </div>

      {isCurrentlyOffHours && (
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
          <span>수신자 비업무(야간) 시간대입니다. 확인 가능 시점: <strong>{getNextAvailableTimeText()}</strong></span>
        </div>
      )}
    </div>
  );
}
