import { useState, useEffect } from 'react'
import { getUserStyle, updateUserStyle } from '../api/userstyle'

export default function ProfilePanel() {
  const [userName, setUserName] = useState('사용자')
  const [userTeam, setUserTeam] = useState('글로벌 협업팀')
  const [isEditingProfile, setIsEditingProfile] = useState(false)

  const [tone, setTone] = useState('polite')
  const [conciseness, setConciseness] = useState(30)
  const [politeness, setPoliteness] = useState(85)
  const [length, setLength] = useState(50)
  const [phrases, setPhrases] = useState(['Could you please', 'I would appreciate it', 'As soon as possible'])
  const [isSaving, setIsSaving] = useState(false)

  useEffect(() => {
    // 1. 프로필 정보 및 선호 표현 로컬 스토리지에서 로드
    try {
      const savedProfile = localStorage.getItem('sai_user_profile')
      if (savedProfile) {
        const parsed = JSON.parse(savedProfile)
        if (parsed.name) setUserName(parsed.name)
        if (parsed.team) setUserTeam(parsed.team)
      }
      const savedPhrases = localStorage.getItem('sai_user_phrases')
      if (savedPhrases) {
        setPhrases(JSON.parse(savedPhrases))
      }
      const savedStyle = localStorage.getItem('sai_user_style')
      if (savedStyle) {
        const parsed = JSON.parse(savedStyle)
        if (parsed.tone) setTone(parsed.tone)
        if (parsed.conciseness !== undefined) setConciseness(parsed.conciseness)
        if (parsed.politeness !== undefined) setPoliteness(parsed.politeness)
        if (parsed.length !== undefined) setLength(parsed.length)
      }
    } catch (_) {}

    // 2. 백엔드 DB에서 협업 스타일 로드
    getUserStyle()
      .then((res) => {
        const data = res.data
        if (data) {
          if (data.tone) setTone(data.tone)
          if (data.directness === 'direct') setPoliteness(30)
          else if (data.directness === 'indirect') setPoliteness(90)
          else setPoliteness(60)

          if (data.detailLevel === 'concise') setConciseness(20)
          else if (data.detailLevel === 'detailed') setConciseness(80)
          else setConciseness(50)
        }
      })
      .catch((err) => {
        console.warn('Could not load user style from server:', err)
      })
  }, [])

  function handleSaveProfile() {
    setIsEditingProfile(false)
    try {
      localStorage.setItem('sai_user_profile', JSON.stringify({ name: userName, team: userTeam }))
    } catch (_) {}
  }

  async function handleToneChange(newTone) {
    setTone(newTone)
    updateAndSaveStyle(newTone, conciseness, politeness, length)
  }

  async function handleConciseChange(newVal) {
    setConciseness(newVal)
    updateAndSaveStyle(tone, newVal, politeness, length)
  }

  async function handlePoliteChange(newVal) {
    setPoliteness(newVal)
    updateAndSaveStyle(tone, conciseness, newVal, length)
  }

  async function handleLengthChange(newVal) {
    setLength(newVal)
    updateAndSaveStyle(tone, conciseness, politeness, newVal)
  }

  async function updateAndSaveStyle(t, c, p, l) {
    const directness = p < 40 ? 'direct' : p > 75 ? 'indirect' : 'balanced'
    const detailLevel = c < 40 ? 'concise' : c > 70 ? 'detailed' : 'moderate'

    // 로컬 스토리지에 즉시 동기화
    try {
      localStorage.setItem(
        'sai_user_style',
        JSON.stringify({
          tone: t,
          conciseness: c,
          politeness: p,
          length: l,
          directness,
          detailLevel,
        })
      )
    } catch (_) {}

    // 서버 DB에 동기화
    try {
      setIsSaving(true)
      await updateUserStyle({
        tone: t,
        directness,
        detailLevel,
      })
    } catch (err) {
      console.warn('Failed to save user style to server:', err)
    } finally {
      setIsSaving(false)
    }
  }

  function removePhrase(p) {
    const next = phrases.filter((x) => x !== p)
    setPhrases(next)
    try {
      localStorage.setItem('sai_user_phrases', JSON.stringify(next))
    } catch (_) {}
  }

  function addPhrase() {
    const p = prompt('추가할 선호 표현을 입력하세요 (예: Thank you in advance)')
    if (p && p.trim()) {
      const next = [...phrases, p.trim()]
      setPhrases(next)
      try {
        localStorage.setItem('sai_user_phrases', JSON.stringify(next))
      } catch (_) {}
    }
  }

  return (
    <div>
      <h3 className="sai-h3">내 프로필</h3>
      <div className="sai-profile-card" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div className="sai-avatar">{userName.charAt(0) || 'U'}</div>
          {isEditingProfile ? (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '4px' }}>
              <input
                type="text"
                value={userName}
                onChange={(e) => setUserName(e.target.value)}
                placeholder="이름"
                style={{ padding: '2px 6px', fontSize: '13px', borderRadius: '4px', border: '1px solid #ccc' }}
              />
              <input
                type="text"
                value={userTeam}
                onChange={(e) => setUserTeam(e.target.value)}
                placeholder="소속 팀"
                style={{ padding: '2px 6px', fontSize: '12px', borderRadius: '4px', border: '1px solid #ccc' }}
              />
            </div>
          ) : (
            <div>
              <div className="sai-row-title">{userName}</div>
              <div className="sai-row-desc">{userTeam}</div>
            </div>
          )}
        </div>
        <div>
          {isEditingProfile ? (
            <button className="sai-link-btn" onClick={handleSaveProfile} style={{ fontSize: '12px' }}>저장</button>
          ) : (
            <button className="sai-link-btn" onClick={() => setIsEditingProfile(true)} style={{ fontSize: '12px' }}>수정</button>
          )}
        </div>
      </div>

      <div className="sai-settings-card">
        <div className="sai-setting-label">선호 말투 {isSaving && <span style={{ fontSize: '11px', color: 'var(--sai-primary, #4361ee)' }}>(저장 중...)</span>}</div>
        <div className="sai-tone-row">
          {[
            { id: 'polite', label: '정중하게' },
            { id: 'friendly', label: '친근하게' },
            { id: 'professional', label: '전문적으로' },
          ].map((t) => (
            <button
              key={t.id}
              className={tone === t.id ? 'sai-tone-btn active' : 'sai-tone-btn'}
              onClick={() => handleToneChange(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <SliderRow
          label="간결한 정도"
          leftLabel="간결하게"
          rightLabel="자세하게"
          value={conciseness}
          onChange={handleConciseChange}
        />
        <SliderRow
          label="정중할 정도"
          leftLabel="낮게"
          rightLabel="높게"
          value={politeness}
          onChange={handlePoliteChange}
        />
        <SliderRow
          label="문장 길이"
          leftLabel="짧게"
          rightLabel="길게"
          value={length}
          onChange={handleLengthChange}
        />

        <div className="sai-setting-label" style={{ marginTop: '12px' }}>AI 자주 표현 선호</div>
        <div className="sai-tag-row">
          {phrases.map((p) => (
            <span key={p} className="sai-tag">
              {p} <button onClick={() => removePhrase(p)}>×</button>
            </span>
          ))}
          <button className="sai-tag sai-tag-add" onClick={addPhrase}>+ 표현 추가 &gt;</button>
        </div>
      </div>

      <div className="sai-tip-banner">내 성향에 맞는 표현으로 AI가 교정 및 제안을 제공합니다.</div>
    </div>
  )
}

function SliderRow({ label, leftLabel, rightLabel, value, onChange }) {
  return (
    <div className="sai-slider-row">
      <div className="sai-setting-label">{label}</div>
      <div className="sai-slider-labels">
        <span>{leftLabel}</span>
        <span>{rightLabel}</span>
      </div>
      <input
        type="range"
        min="0"
        max="100"
        value={value}
        onChange={(e) => onChange(Number(e.target.value))}
        className="sai-slider"
      />
    </div>
  )
}