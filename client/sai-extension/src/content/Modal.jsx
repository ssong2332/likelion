// src/content/Modal.jsx
import './modal.css'
import saiLogoPath from '../assets/sai-logo.png?url'

const saiLogo = chrome.runtime?.getURL
  ? chrome.runtime.getURL(saiLogoPath.replace(/^\//, ''))
  : saiLogoPath

function formatTime(isoString) {
  if (!isoString) return '-'
  const date = new Date(isoString)
  if (isNaN(date.getTime())) return isoString // 파싱 실패 시 원본 그대로

  const hours = date.getHours()
  const minutes = date.getMinutes()
  const ampm = hours < 12 ? 'AM' : 'PM'
  const displayHour = hours % 12 === 0 ? 12 : hours % 12
  const displayMinute = String(minutes).padStart(2, '0')

  return `${String(displayHour).padStart(2, '0')}:${displayMinute} ${ampm}`
}

export default function CorrectionModal({
  result,
  originalText,
  onClose,
}) {
  if (!result) return null

  function handleCopy() {
    if (result.refinedText) {
      navigator.clipboard.writeText(result.refinedText)
      alert('개선된 문장이 클립보드에 복사되었습니다!')
    }
  }

  // Derive change items from backend risky expressions, glossaries, or changes array
  const changeItems = []
  if (result.changes && Array.isArray(result.changes)) {
    changeItems.push(...result.changes)
  }
  if (result.riskyExpressions && Array.isArray(result.riskyExpressions)) {
    result.riskyExpressions.forEach((r) => {
      changeItems.push(`위험 표현 개선: "${r.originalPhrase}" → "${r.replacedWith}" (${r.reason})`)
    })
  }
  if (result.appliedGlossary && Array.isArray(result.appliedGlossary)) {
    result.appliedGlossary.forEach((g) => {
      changeItems.push(`용어 사전 적용: ${g.term} (${g.meaning || g.rule || ''})`)
    })
  }

  return (
    <div className="sai-modal-overlay">
      <div className="sai-modal">

        {/* =========================
            Header
        ========================== */}
        <div className="sai-modal-header">
          <div className="sai-brand">
            <img
              src={saiLogo}
              alt="SAI"
              className="sai-logo-img"
            />
            <span className="sai-brand-text">
              SAI
            </span>
          </div>

          <button
            className="sai-close-btn"
            onClick={onClose}
            aria-label="닫기"
          >
            ×
          </button>
        </div>

        {/* =========================
            1. 문장 교정 및 제안
        ========================== */}
        <section className="sai-section">
          <h3>1. 문장 교정 및 제안</h3>

          <p className="sai-label">문제 문장</p>
          <div className="sai-box sai-box-danger">
            <span className="sai-box-icon">⚠</span>
            <span>{originalText}</span>
          </div>

          <p className="sai-label" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
            <span>추천 개선 문장</span>
            <button
              onClick={handleCopy}
              style={{
                background: 'transparent',
                border: '1px solid #4361ee',
                color: '#4361ee',
                borderRadius: '4px',
                padding: '2px 8px',
                fontSize: '11px',
                cursor: 'pointer',
              }}
            >
              복사
            </button>
          </p>
          <div className="sai-box sai-box-success">
            <span className="sai-box-icon">✓</span>
            <span>{result.refinedText}</span>
          </div>

          {result.backTranslation && (
            <>
              <p className="sai-label" style={{ marginTop: '8px', color: '#666' }}>역번역 (한국어 검토)</p>
              <div style={{ fontSize: '13px', color: '#555', padding: '6px 10px', background: '#f8f9fa', borderRadius: '6px' }}>
                {result.backTranslation}
              </div>
            </>
          )}
        </section>

        {/* =========================
            2. 상황 및 시간대 분석
        ========================== */}
        {result.timezoneInfo && (
          <section className="sai-section">
            <h3>2. 상황 및 시간대 분석</h3>
            <div className="sai-info-table">
              <div className="sai-info-row">
                <div>상대방 시간</div>
                <div>{formatTime(result.timezoneInfo.receiverLocalTime)} (현재 시간)</div>
              </div>
              <div className="sai-info-row">
                <div>업무 시간</div>
                <div>
                  {result.timezoneInfo.isReceiverOffHours
                    ? '⚠️ 업무 시간 외 (비업무시간)'
                    : '✅ 업무 시간 중'}
                </div>
              </div>
              <div className="sai-info-row">
                <div>추천 행동 시간</div>
                <div>
                  {result.timezoneInfo.nextAvailableCheckingTime
                    ? formatTime(result.timezoneInfo.nextAvailableCheckingTime)
                    : '지금 발송 권장'}
                </div>
              </div>
              {result.timezoneInfo.receiverTimezone && (
                <div className="sai-info-row">
                  <div>상대방 근무지</div>
                  <div>{result.timezoneInfo.receiverTimezone}</div>
                </div>
              )}
            </div>
          </section>
        )}

        {/* =========================
            3. 주요 정보 및 누락 경고
        ========================== */}
        <section className="sai-section">
          <h3>3. 주요 정보 분석</h3>
          <div className="sai-info-table sai-table-muted">
            <div className="sai-info-row">
              <div>요청 목적</div>
              <div>{result.extractedInfo?.purpose || '-'}</div>
            </div>
            <div className="sai-info-row">
              <div>긴급도</div>
              <div className="sai-urgent">{result.extractedInfo?.urgency || '-'}</div>
            </div>
            <div className="sai-info-row">
              <div>마감 기한</div>
              <div>{result.extractedInfo?.deadline || '-'}</div>
            </div>
            <div className="sai-info-row">
              <div>담당자</div>
              <div>{result.extractedInfo?.assignee || '-'}</div>
            </div>
          </div>

          {result.missingInfoWarnings && result.missingInfoWarnings.length > 0 && (
            <div style={{ marginTop: '10px' }}>
              <p className="sai-label" style={{ color: '#e67e22' }}>누락 정보 알림</p>
              {result.missingInfoWarnings.map((w, idx) => (
                <div key={idx} style={{ fontSize: '12px', background: '#fef5e7', padding: '6px 10px', borderRadius: '6px', marginBottom: '4px', borderLeft: '3px solid #f39c12' }}>
                  <strong>{w.warning}</strong>
                  {w.suggestedCompletion && <div style={{ color: '#555', marginTop: '2px' }}>💡 제안: {w.suggestedCompletion}</div>}
                </div>
              ))}
            </div>
          )}
        </section>

        {/* =========================
            4. 원문 & 수정안 비교
        ========================== */}
        <section className="sai-section sai-compare-section">
          <h3>4. 원문 & 수정안 비교</h3>
          <p className="sai-label">원문</p>
          <div className="sai-compare-box sai-original">
            {originalText}
          </div>
          <div className="sai-arrow">↓</div>
          <p className="sai-label">개선안</p>
          <div className="sai-compare-box sai-improved">
            {result.refinedText}
          </div>

          {changeItems.length > 0 && (
            <div className="sai-changes" style={{ marginTop: '12px' }}>
              <div className="sai-changes-title">반영된 교정 사항</div>
              {changeItems.map((change, index) => (
                <div className="sai-change-item" key={index}>
                  <span className="sai-change-check">✓</span>
                  <span>{change}</span>
                </div>
              ))}
            </div>
          )}
        </section>

      </div>
    </div>
  )
}