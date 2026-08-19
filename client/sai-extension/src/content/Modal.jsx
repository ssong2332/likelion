// src/content/Modal.jsx
import './modal.css'
import saiLogoPath from '../assets/sai-logo.png?url'

const saiLogo = chrome.runtime.getURL(
  saiLogoPath.replace(/^\//, '')
)

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

          <h3>
            1. 문장 교정 및 제안
          </h3>

          <p className="sai-label">
            문제 문장
          </p>

          <div className="sai-box sai-box-danger">
            <span className="sai-box-icon">
              ⚠
            </span>

            <span>
              {originalText}
            </span>
          </div>


          <p className="sai-label">
            추천 개선 문장
          </p>

          <div className="sai-box sai-box-success">
            <span className="sai-box-icon">
              ✓
            </span>

            <span>
              {result.refinedText}
            </span>
          </div>

        </section>


        {/* =========================
            2. 상황 및 시간대 분석
        ========================== */}
        <section className="sai-section">

          <h3>
            2. 상황 및 시간대 분석
          </h3>

          <div className="sai-info-table">

            <div className="sai-info-row">
              <div>상대방 시간</div>
              <div>
                {formatTime(result.timezoneInfo?.receiverLocalTime)} (현재 시간)
              </div>
            </div>

            <div className="sai-info-row">
              <div>업무 시간</div>
              <div>
                {result.timezoneInfo?.isReceiverOffHours
                  ? '업무 시간 외'
                  : '업무 시간'}
              </div>
            </div>

            <div className="sai-info-row">
              <div>추천 행동 시간</div>
              <div>
                {result.timezoneInfo?.nextAvailableCheckingTime
                  ? formatTime(result.timezoneInfo.nextAvailableCheckingTime)
                  : '지금 가능'}
              </div>
            </div>

            {result.timezoneInfo?.receiverTimezone && (
              <div className="sai-info-row">
                <div>상대방 근무지</div>
                <div>
                  {result.timezoneInfo.receiverTimezone}
                </div>
              </div>
            )}

          </div>

        </section>


        {/* =========================
            3. 주요 정보
        ========================== */}
        <section className="sai-section">

          <h3>
            3. 주요 정보
          </h3>

          <div className="sai-info-table sai-table-muted">

            <div className="sai-info-row">
              <div>요청 내용</div>
              <div>
                {result.extractedInfo?.purpose}
              </div>
            </div>

            <div className="sai-info-row">
              <div>긴급도</div>

              <div className="sai-urgent">
                {result.extractedInfo?.urgency}
              </div>
            </div>

            <div className="sai-info-row">
              <div>마감 관련</div>
              <div>
                {result.extractedInfo?.deadline}
              </div>
            </div>

            <div className="sai-info-row">
              <div>요청자</div>
              <div>
                {result.extractedInfo?.assignee}
              </div>
            </div>

                        <div className="sai-info-row">
              <div>요청자</div>
              <div>
                {result.extractedInfo?.assignee}
              </div>
            </div>

          </div>

        </section>


        {/* =========================
            4. 원문 & 수정안 비교
        ========================== */}
<section className="sai-section sai-compare-section">

  <h3>
    4. 원문 & 수정안 비교
  </h3>

  <p className="sai-label">
    원문
  </p>

  <div className="sai-compare-box sai-original">
    {originalText}
  </div>

  <div className="sai-arrow">
    ↓
  </div>

  <p className="sai-label">
    개선안
  </p>

  <div className="sai-compare-box sai-improved">
    {result.refinedText}
  </div>

  <div className="sai-changes">

    <div className="sai-changes-title">
      변경된 부분
    </div>

    {result.changes?.map((change, index) => (
      <div className="sai-change-item" key={index}>
        <span className="sai-change-check">
          ✓
        </span>

        <span>
          {change}
        </span>
      </div>
    ))}

  </div>

</section>

      </div>
    </div>
  )
}