// src/content/main.jsx
import { createRoot } from 'react-dom/client'
import CorrectionModal from './Modal.jsx'
import { analyzeRefine } from '../api/refine.js'
import { getStorage } from '../utils/storage.js'
import saiLogoPath from '../assets/sai-logo.png?url'

const saiLogo = chrome.runtime?.getURL
  ? chrome.runtime.getURL(saiLogoPath.replace(/^\//, ''))
  : saiLogoPath

let toolbarEl = null
let modalRoot = null
let modalContainer = null

function createToolbar(x, y, selectedText) {
  removeToolbar()

  toolbarEl = document.createElement('div')
  toolbarEl.id = 'sai-toolbar'
  toolbarEl.style.position = 'absolute'
  toolbarEl.style.left = `${x}px`
  toolbarEl.style.top = `${y}px`
  toolbarEl.style.zIndex = '2147483647'
  toolbarEl.style.background = '#ffffff'
  toolbarEl.style.color = '#1e1e1e'
  toolbarEl.style.border = '1px solid #dcdcdc'
  toolbarEl.style.borderRadius = '8px'
  toolbarEl.style.padding = '6px 12px'
  toolbarEl.style.boxShadow = '0 4px 14px rgba(0,0,0,0.18)'
  toolbarEl.style.cursor = 'pointer'
  toolbarEl.style.fontSize = '13px'
  toolbarEl.style.fontFamily = '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, sans-serif'
  toolbarEl.style.display = 'flex'
  toolbarEl.style.alignItems = 'center'
  toolbarEl.style.gap = '8px'
  toolbarEl.style.lineHeight = '1'
  toolbarEl.style.userSelect = 'none'
  toolbarEl.innerHTML = `<img src="${saiLogo}" alt="SAI" style="height:16px;width:auto;vertical-align:middle;display:inline-block;" /> <span style="color:#1e1e1e !important;font-size:13px;font-weight:600;display:inline-block;letter-spacing:-0.2px;">SAI 교정</span>`

  toolbarEl.addEventListener('click', async (e) => {
    e.stopPropagation()
    try {
      toolbarEl.innerHTML = `<img src="${saiLogo}" alt="SAI" style="height:16px;width:auto;vertical-align:middle;display:inline-block;" /> <span style="color:#1e1e1e !important;font-size:13px;font-weight:600;display:inline-block;letter-spacing:-0.2px;">교정 중...</span>`
      toolbarEl.style.pointerEvents = 'none'

      const savedStyle = await getStorage('sai_user_style')
      const collaborationStyle = {
        tone: savedStyle?.tone || 'polite',
        directness: savedStyle?.directness || 'balanced',
        detailLevel: savedStyle?.detailLevel || 'concise',
        lengthLevel: savedStyle?.lengthLevel || 'medium',
      }

      const result = await analyzeRefine({
        originalText: selectedText,
        collaborationStyle,
      })
      removeToolbar()
      showModal(result, selectedText)
    } catch (err) {
      removeToolbar()
      alert('SAI 교정 요청 실패: ' + (err.message || '백엔드 서버에 연결할 수 없습니다.'))
    }
  })

  document.body.appendChild(toolbarEl)
}

function removeToolbar() {
  if (toolbarEl) {
    toolbarEl.remove()
    toolbarEl = null
  }
}

function showModal(result, originalText) {
  removeModal()
  modalContainer = document.createElement('div')
  document.body.appendChild(modalContainer)
  modalRoot = createRoot(modalContainer)
  modalRoot.render(
    <CorrectionModal result={result} originalText={originalText} onClose={removeModal} />
  )
}

function removeModal() {
  if (modalRoot) {
    modalRoot.unmount()
    modalContainer?.remove()
    modalRoot = null
    modalContainer = null
  }
}

document.addEventListener('mouseup', (e) => {
  // 툴바 버튼 자체를 클릭한 경우는 무시 (버튼이 재생성되어 클릭이 씹히는 걸 방지)
  if (toolbarEl && toolbarEl.contains(e.target)) {
    return
  }

  // 모달 내부 클릭 시 툴바 방지
  if (modalContainer && modalContainer.contains(e.target)) {
    return
  }

  const selectedText = window.getSelection().toString().trim()
  if (selectedText.length > 0) {
    createToolbar(e.pageX + 10, e.pageY + 10, selectedText)
  } else {
    removeToolbar()
  }
})

console.log('SAI content script loaded')