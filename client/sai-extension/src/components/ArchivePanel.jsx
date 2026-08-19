import { useState, useEffect } from 'react'
import { getMessageHistory, deleteMessageHistoryItem, deleteAllMessageHistory } from '../api/history'

export default function ArchivePanel({ active = true }) {
  const [subTab, setSubTab] = useState('all')
  const [entries, setEntries] = useState([])
  const [retentionDays, setRetentionDays] = useState('30')
  const [archiveAlert, setArchiveAlert] = useState(true)
  const [autoDelete, setAutoDelete] = useState(true)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (active) {
      loadHistory()
    }
  }, [active])

  async function loadHistory() {
    try {
      setIsLoading(true)
      const res = await getMessageHistory()
      if (res && res.history) {
        setEntries(res.history)
      } else {
        setEntries([])
      }
    } catch (err) {
      console.warn('Failed to load message history:', err)
    } finally {
      setIsLoading(false)
    }
  }

  async function handleDeleteItem(id) {
    if (confirm('해당 메시지 기록을 삭제하시겠습니까?')) {
      try {
        await deleteMessageHistoryItem(id)
        setEntries((prev) => prev.filter((item) => item.id !== id))
      } catch (err) {
        alert('삭제 실패: ' + err.message)
      }
    }
  }

  async function handleDeleteAll() {
    if (confirm('모든 메시지 데이터를 영구 삭제하시겠습니까?')) {
      try {
        await deleteAllMessageHistory()
        setEntries([])
        alert('모든 데이터가 삭제되었습니다.')
      } catch (err) {
        alert('삭제 실패: ' + err.message)
      }
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
        {isLoading && <div style={{ padding: '12px', color: '#888', textAlign: 'center' }}>로딩 중...</div>}
        {!isLoading && entries.length === 0 && (
          <div style={{ padding: '20px', color: '#888', textAlign: 'center', fontSize: '13px' }}>
            저장된 메시지 이력이 없습니다.
          </div>
        )}
        {entries.map((entry) => (
          <div key={entry.id} className="sai-row-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
            <div style={{ flex: 1, marginRight: '8px' }}>
              <div className="sai-row-title" style={{ fontWeight: 600 }}>{entry.originalText}</div>
              <div className="sai-row-desc" style={{ color: 'var(--sai-primary, #4361ee)', marginTop: '4px' }}>
                {entry.refinedText}
              </div>
            </div>
            <div style={{ display: 'flex', flexDirection: 'column', alignItems: 'flex-end', gap: '4px' }}>
              <span className="sai-row-time" style={{ fontSize: '11px', color: '#aaa' }}>
                {entry.createdAt ? new Date(entry.createdAt).toLocaleDateString() : ''}
              </span>
              <button className="sai-icon-btn" onClick={() => handleDeleteItem(entry.id)}>🗑</button>
            </div>
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

      <button className="sai-danger-btn" onClick={handleDeleteAll} disabled={entries.length === 0}>
        🗑 모든 데이터 삭제
      </button>

      <div className="sai-tip-banner">개인정보 보호를 위해 데이터가 간편하게 관리돼요.</div>
    </div>
  )
}