import { useEffect, useState } from 'react'
import { getGlossaries, addGlossary, deleteGlossary } from '../api/glossary'
import { getRules, addRule, deleteRule } from '../api/rules'

export default function SidebarHome() {
  const [subTab, setSubTab] = useState('rules') // rules | glossary
  const [rules, setRules] = useState([])
  const [glossaries, setGlossaries] = useState([])

  useEffect(() => {
    getRules().then((res) => setRules(res.data))
    getGlossaries().then((res) => setGlossaries(res.data))
  }, [])

  async function handleAddRule() {
    const name = prompt('규칙 이름')
    if (!name) return
    const res = await addRule({ name, description: '' })
    setRules((prev) => [...prev, res.data])
  }

  async function handleDeleteRule(id) {
    await deleteRule(id)
    setRules((prev) => prev.filter((r) => r.id !== id))
  }

  async function handleAddGlossary() {
    const term = prompt('용어')
    if (!term) return
    const rule = prompt('설명') || ''
    const res = await addGlossary({ term, rule, note: null })
    setGlossaries((prev) => [...prev, res.data])
  }

  async function handleDeleteGlossary(id) {
    await deleteGlossary(id)
    setGlossaries((prev) => prev.filter((g) => g.id !== id))
  }

  return (
    <div>
      <h3 className="sai-h3">오늘의 요약</h3>
      <div className="sai-stat-row">
        <div className="sai-stat"><div className="sai-stat-num">4</div>교정</div>
        <div className="sai-stat"><div className="sai-stat-num">2</div>제안</div>
        <div className="sai-stat"><div className="sai-stat-num">1</div>예약 발송</div>
      </div>

      <div className="sai-sub-tabs">
        <button
          className={subTab === 'rules' ? 'sai-sub-tab active' : 'sai-sub-tab'}
          onClick={() => setSubTab('rules')}
        >
          규칙
        </button>
        <button
          className={subTab === 'glossary' ? 'sai-sub-tab active' : 'sai-sub-tab'}
          onClick={() => setSubTab('glossary')}
        >
          용어집
        </button>
      </div>

      {subTab === 'rules' && (
        <div className="sai-list-section">
          <div className="sai-list-header">
            <span>커뮤니케이션 규칙</span>
            <button className="sai-link-btn" onClick={handleAddRule}>+ 규칙 추가</button>
          </div>
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

      {subTab === 'glossary' && (
        <div className="sai-list-section">
          <div className="sai-list-header">
            <span>자주 쓰는 업무 용어</span>
            <button className="sai-link-btn" onClick={handleAddGlossary}>+ 용어 추가</button>
          </div>
          {glossaries.map((g) => (
            <div key={g.id} className="sai-row-card">
              <div>
                <strong>{g.term}</strong> {g.rule}
              </div>
              <button className="sai-icon-btn" onClick={() => handleDeleteGlossary(g.id)}>🗑</button>
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