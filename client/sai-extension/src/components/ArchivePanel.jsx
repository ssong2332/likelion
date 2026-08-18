import { useState } from 'react'

const mockEntries = [
  { title: 'Project Update', preview: 'Could you please submit the PR as soon...', time: '오늘 18:32' },
  { title: '회의 일정 변경 요청', preview: 'Would it be possible to reschedule...', time: '어제 14:20' },
  { title: '협업 제안 메일', preview: 'I would like to propose a collaboration...', time: '어제 11:05' },
]

export default function ArchivePanel() {
  const [subTab, setSubTab] = useState('all')
  const [retentionDays, setRetentionDays] = useState('30')
  const [archiveAlert, setArchiveAlert] = useState(true)
  const [autoDelete, setAutoDelete] = useState(true)

  function handleDeleteAll() {
    if (confirm('모든 데이터를 삭제하시겠습니까?')) {
      alert('삭제되었습니다 (더미)')
    }
  }

  return (
    <div>
      <h3 className="sai-h3">저장한 문장</h3>
      <div className="sai-sub-tabs">
        {['all', 'sentence', 'reply', 'summary'].map((t) => (
          <button
            key={t}
            className={subTab === t ? 'sai-sub-tab active' : 'sai-sub-tab'}
            onClick={() => setSubTab(t)}
          >
            {{ all: '전체', sentence: '문장', reply: '회신 초안', summary: '요약' }[t]}
          </button>
        ))}
      </div>

      <div className="sai-list-section">
        {mockEntries.map((entry, i) => (
          <div key={i} className="sai-row-card">
            <div>
              <div className="sai-row-title">{entry.title}</div>
              <div className="sai-row-desc">{entry.preview}</div>
            </div>
            <span className="sai-row-time">{entry.time}</span>
          </div>
        ))}
      </div>

      <h4 className="sai-h4">데이터 관리</h4>
      <p className="sai-desc-text">내 메시지 데이터의 보관과 삭제를 관리하세요.</p>

      <div className="sai-settings-card">
        <div className="sai-setting-row">
          <span>메시지 자동 보관 기간</span>
          <select value={retentionDays} onChange={(e) => setRetentionDays(e.target.value)}>
            <option value="7">7일</option>
            <option value="30">30일</option>
            <option value="90">90일</option>
          </select>
        </div>
        <div className="sai-setting-row">
          <div>
            <div>보관 알림</div>
            <div className="sai-row-desc">보관 기간 만료 3일 전에 알림을 보내요.</div>
          </div>
          <label className="sai-toggle">
            <input type="checkbox" checked={archiveAlert} onChange={(e) => setArchiveAlert(e.target.checked)} />
            <span />
          </label>
        </div>
      </div>

      <div className="sai-settings-card">
        <div className="sai-setting-row">
          <div>
            <div>자동 삭제</div>
            <div className="sai-row-desc">보관 기간이 지난 데이터를 자동으로 삭제해요.</div>
          </div>
          <label className="sai-toggle">
            <input type="checkbox" checked={autoDelete} onChange={(e) => setAutoDelete(e.target.checked)} />
            <span />
          </label>
        </div>
      </div>

      <button className="sai-danger-btn" onClick={handleDeleteAll}>🗑 모든 데이터 삭제</button>

      <div className="sai-tip-banner">개인정보 보호를 위해 데이터가 간편하게 관리돼요.</div>
    </div>
  )
}