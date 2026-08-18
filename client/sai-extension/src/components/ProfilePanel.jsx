import { useState } from 'react'

export default function ProfilePanel() {
  const [tone, setTone] = useState('polite')
  const [conciseness, setConciseness] = useState(30)
  const [politeness, setPoliteness] = useState(85)
  const [length, setLength] = useState(50)
  const [phrases, setPhrases] = useState(['Could you please', 'I would appreciate it', 'As soon as possible'])

  function removePhrase(p) {
    setPhrases((prev) => prev.filter((x) => x !== p))
  }

  function addPhrase() {
    const p = prompt('추가할 표현')
    if (p) setPhrases((prev) => [...prev, p])
  }

  return (
    <div>
      <h3 className="sai-h3">내 프로필</h3>
      <div className="sai-profile-card">
        <div className="sai-avatar">E</div>
        <div>
          <div className="sai-row-title">Emily Kim</div>
          <div className="sai-row-desc">Marketing Team</div>
        </div>
      </div>

      <div className="sai-settings-card">
        <div className="sai-setting-label">선호 말투</div>
        <div className="sai-tone-row">
          {[
            { id: 'polite', label: '정중하게' },
            { id: 'friendly', label: '친근하게' },
            { id: 'professional', label: '전문적으로' },
          ].map((t) => (
            <button
              key={t.id}
              className={tone === t.id ? 'sai-tone-btn active' : 'sai-tone-btn'}
              onClick={() => setTone(t.id)}
            >
              {t.label}
            </button>
          ))}
        </div>

        <SliderRow label="간결한 정도" leftLabel="간결하게" rightLabel="자세하게" value={conciseness} onChange={setConciseness} />
        <SliderRow label="정중할 정도" leftLabel="낮게" rightLabel="높게" value={politeness} onChange={setPoliteness} />
        <SliderRow label="문장 길이" leftLabel="짧게" rightLabel="길게" value={length} onChange={setLength} />

        <div className="sai-setting-label">AI 자주 표현 선호</div>
        <div className="sai-tag-row">
          {phrases.map((p) => (
            <span key={p} className="sai-tag">
              {p} <button onClick={() => removePhrase(p)}>×</button>
            </span>
          ))}
          <button className="sai-tag sai-tag-add" onClick={addPhrase}>+ 표현 추가 &gt;</button>
        </div>
      </div>

      <div className="sai-tip-banner">내 성향에 맞는 표현으로 AI가 제안해요.</div>
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