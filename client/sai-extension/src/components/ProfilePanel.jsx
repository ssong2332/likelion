import { useState, useEffect, useRef } from 'react'
import { getUserStyle, updateUserStyle } from '../api/userstyle'
import { getStorage, setStorage } from '../utils/storage'

export default function ProfilePanel() {
  const [userName, setUserName] = useState('사용자')
  const [userTeam, setUserTeam] = useState('글로벌 협업팀')
  const [isEditingProfile, setIsEditingProfile] = useState(false)

  const [tone, setTone] = useState('polite')
  const [conciseness, setConciseness] = useState(50)
  const [politeness, setPoliteness] = useState(70)
  const [length, setLength] = useState(50)
  const [phrases, setPhrases] = useState(['Could you please', 'I would appreciate it', 'As soon as possible'])
  const [isSaving, setIsSaving] = useState(false)

  // Use refs to avoid stale closures in event handlers
  const stateRef = useRef({ tone, conciseness, politeness, length })
  useEffect(() => {
    stateRef.current = { tone, conciseness, politeness, length }
  }, [tone, conciseness, politeness, length])

  useEffect(() => {
    loadSavedData()
  }, [])

  async function loadSavedData() {
    try {
      const savedProfile = await getStorage('sai_user_profile')
      if (savedProfile) {
        if (savedProfile.name) setUserName(savedProfile.name)
        if (savedProfile.team) setUserTeam(savedProfile.team)
      }

      const savedPhrases = await getStorage('sai_user_phrases')
      if (savedPhrases && Array.isArray(savedPhrases)) {
        setPhrases(savedPhrases)
      }

      const savedStyle = await getStorage('sai_user_style')
      if (savedStyle) {
        if (savedStyle.tone) setTone(savedStyle.tone)
        if (typeof savedStyle.conciseness === 'number') setConciseness(savedStyle.conciseness)
        if (typeof savedStyle.politeness === 'number') setPoliteness(savedStyle.politeness)
        if (typeof savedStyle.length === 'number') setLength(savedStyle.length)
      } else {
        const res = await getUserStyle()
        const data = res?.data
        if (data) {
          if (data.tone) setTone(data.tone)
          if (data.directness === 'direct') setPoliteness(30)
          else if (data.directness === 'indirect') setPoliteness(90)
          else setPoliteness(60)

          if (data.detailLevel === 'concise') setConciseness(20)
          else if (data.detailLevel === 'detailed') setConciseness(80)
          else setConciseness(50)
        }
      }
    } catch (err) {
      console.warn('Failed to load profile data:', err)
    }
  }

  async function persistStyle(overrideState = {}) {
    const currentState = { ...stateRef.current, ...overrideState }
    const { tone: t, conciseness: c, politeness: p, length: l } = currentState

    const directness = p < 40 ? 'direct' : p > 75 ? 'indirect' : 'balanced'
    const detailLevel = c < 40 ? 'concise' : c > 70 ? 'detailed' : 'moderate'
    const lengthLevel = l < 40 ? 'short' : l > 70 ? 'long' : 'medium'

    const styleData = {
      tone: t,
      conciseness: c,
      politeness: p,
      length: l,
      directness,
      detailLevel,
      lengthLevel,
    }

    await setStorage('sai_user_style', styleData)

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

  async function handleToneChange(newTone) {
    setTone(newTone)
    await persistStyle({ tone: newTone })
  }

  function handleSaveProfile() {
    setIsEditingProfile(false)
    setStorage('sai_user_profile', { name: userName, team: userTeam })
  }

  async function removePhrase(p) {
    const next = phrases.filter((x) => x !== p)
    setPhrases(next)
    await setStorage('sai_user_phrases', next)
  }

  async function addPhrase() {
    const p = prompt('추가할 선호 표현을 입력하세요 (예: Thank you in advance)')
    if (p && p.trim()) {
      const next = [...phrases, p.trim()]
      setPhrases(next)
      await setStorage('sai_user_phrases', next)
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
          onChange={(val) => setConciseness(val)}
          onRelease={(val) => persistStyle({ conciseness: val })}
        />
        <SliderRow
          label="정중할 정도"
          leftLabel="낮게"
          rightLabel="높게"
          value={politeness}
          onChange={(val) => setPoliteness(val)}
          onRelease={(val) => persistStyle({ politeness: val })}
        />
        <SliderRow
          label="문장 길이"
          leftLabel="짧게"
          rightLabel="길게"
          value={length}
          onChange={(val) => setLength(val)}
          onRelease={(val) => persistStyle({ length: val })}
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

function SliderRow({ label, leftLabel, rightLabel, value, onChange, onRelease }) {
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
        onPointerUp={(e) => onRelease?.(Number(e.target.value))}
        onMouseUp={(e) => onRelease?.(Number(e.target.value))}
        onTouchEnd={(e) => onRelease?.(Number(e.target.value))}
        className="sai-slider"
      />
    </div>
  )
}