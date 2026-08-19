import { useEffect, useState } from 'react'
import { getGlossaries, addGlossary, deleteGlossary } from '../api/glossary'
import { getRules, addRule, deleteRule } from '../api/rules'
import { getMessageHistory } from '../api/history'

export default function SidebarHome({ active = true }) {
  const [subTab, setSubTab] = useState('rules') // rules | glossary
  const [rules, setRules] = useState([])
  const [glossaries, setGlossaries] = useState([])
  const [historyCount, setHistoryCount] = useState(0)
  const [isLoading, setIsLoading] = useState(false)

  useEffect(() => {
    if (active) {
      loadData()
    }
  }, [active])

  async function loadData() {
    setIsLoading(true)
    try {
      const [rulesRes, glossRes, historyRes] = await Promise.allSettled([
        getRules(),
        getGlossaries(),
        getMessageHistory(),
      ])

      if (rulesRes.status === 'fulfilled' && rulesRes.value?.data) {
        setRules(rulesRes.value.data)
      }
      if (glossRes.status === 'fulfilled' && glossRes.value?.data) {
        setGlossaries(glossRes.value.data)
      }
      if (historyRes.status === 'fulfilled' && historyRes.value?.history) {
        setHistoryCount(historyRes.value.history.length)
      }
    } catch (err) {
      console.warn('Failed to load initial data:', err)
    } finally {
      setIsLoading(false)
    }
  }

  async function handleAddRule() {
    const name = prompt('규칙 이름 (예: 보고서 마감)')
    if (!name) return
    const description = prompt('규칙 설명 (예: 매주 목요일 17:00까지 공유)') || ''
    try {
      const res = await addRule({ name, description })
      if (res?.data) {
        setRules((prev) => [...prev, res.data])
      }
    } catch (err) {
      alert('규칙 추가 실패: ' + err.message)
    }
  }

  async function handleDeleteRule(id) {
    if (!confirm('이 규칙을 삭제하시겠습니까?')) return
    try {
      await deleteRule(id)
      setRules((prev) => prev.filter((r) => r.id !== id))
    } catch (err) {
      alert('규칙 삭제 실패: ' + err.message)
    }
  }

  async function handleAddGlossary() {
    const term = prompt('용어 (예: EOD)')
    if (!term) return
    const rule = prompt('설명/치환어 (예: End of Day)') || ''
    const note = prompt('참고/비고 (선택사항)') || null
    try {
      const res = await addGlossary({ term, rule, note })
      if (res?.data) {
        setGlossaries((prev) => [...prev, res.data])
      }
    } catch (err) {
      alert('용어 추가 실패: ' + err.message)
    }
  }

  async function handleDeleteGlossary(id) {
    if (!confirm('이 용어를 삭제하시겠습니까?')) return
    try {
      await deleteGlossary(id)
      setGlossaries((prev) => prev.filter((g) => g.id !== id))
    } catch (err) {
      alert('용어 삭제 실패: ' + err.message)
    }
  }

  return (
    <div>
      <h3 className="sai-h3">오늘의 요약</h3>
      <div className="sai-stat-row">
        <div className="sai-stat"><div className="sai-stat-num">{historyCount}</div>누적 교정</div>
        <div className="sai-stat"><div className="sai-stat-num">{rules.length}</div>활성 규칙</div>
        <div className="sai-stat"><div className="sai-stat-num">{glossaries.length}</div>등록 용어</div>
      </div>

      <div className="sai-sub-tabs">
        <button
          className={subTab === 'rules' ? 'sai-sub-tab active' : 'sai-sub-tab'}
          onClick={() => setSubTab('rules')}
        >
          규칙 ({rules.length})
        </button>
        <button
          className={subTab === 'glossary' ? 'sai-sub-tab active' : 'sai-sub-tab'}
          onClick={() => setSubTab('glossary')}
        >
          용어집 ({glossaries.length})
        </button>
      </div>

      {isLoading && <div style={{ padding: '12px', textAlign: 'center', color: '#888' }}>로딩 중...</div>}

      {!isLoading && subTab === 'rules' && (
        <div className="sai-list-section">
          <div className="sai-list-header">
            <span>커뮤니케이션 규칙</span>
            <button className="sai-link-btn" onClick={handleAddRule}>+ 규칙 추가</button>
          </div>
          {rules.length === 0 && (
            <div style={{ padding: '16px', color: '#888', textAlign: 'center', fontSize: '12px' }}>
              등록된 규칙이 없습니다.
            </div>
          )}
          {rules.map((rule) => (
            <div key={rule.id} className="sai-row-card">
              <div>
                <div className="sai-row-title">{rule.name}</div>
                <div className="sai-row-desc">{rule.description}</div>
              </div>
              <button className="sai-icon-btn" onClick={() => handleDeleteRule(rule.id)}>🗑</button>
            </div>
          ))}
        </div>
      )}

      {!isLoading && subTab === 'glossary' && (
        <div className="sai-list-section">
          <div className="sai-list-header">
            <span>자주 쓰는 업무 용어</span>
            <button className="sai-link-btn" onClick={handleAddGlossary}>+ 용어 추가</button>
          </div>
          {glossaries.length === 0 && (
            <div style={{ padding: '16px', color: '#888', textAlign: 'center', fontSize: '12px' }}>
              등록된 용어가 없습니다.
            </div>
          )}
          {glossaries.map((g) => (
            <div key={g.id} className="sai-row-card">
              <div>
                <strong>{g.term}</strong>: {g.rule}
                {g.note && <div style={{ fontSize: '11px', color: '#999', marginTop: '2px' }}>{g.note}</div>}
              </div>
              {!g.isSystem && (
                <button className="sai-icon-btn" onClick={() => handleDeleteGlossary(g.id)}>🗑</button>
              )}
            </div>
          ))}
        </div>
      )}

      <div className="sai-tip-banner">
        내 회사/팀의 규칙과 용어를 설정하면 AI가 더 정확하게 도와드려요.
      </div>
    </div>
  )
}