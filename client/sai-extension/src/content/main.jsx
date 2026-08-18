// src/content/main.js
import { createRoot } from 'react-dom/client'
import CorrectionModal from './Modal.jsx'
import { analyzeRefine } from '../api/refine.js'
import saiLogoPath from '../assets/sai-logo.png?url'
const saiLogo = chrome.runtime.getURL(saiLogoPath.replace(/^\//, ''))

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
  toolbarEl.style.zIndex = '999999'
  toolbarEl.style.background = 'white'
  toolbarEl.style.border = '1px solid #ccc'
  toolbarEl.style.borderRadius = '8px'
  toolbarEl.style.padding = '6px 10px'
  toolbarEl.style.boxShadow = '0 2px 8px rgba(0,0,0,0.15)'
  toolbarEl.style.cursor = 'pointer'
  toolbarEl.style.fontSize = '13px'
  toolbarEl.innerHTML = `<img src="${saiLogo}" alt="SAI" style="height:16px;vertical-align:middle;" /> SAI`

  toolbarEl.addEventListener('click', async () => {
    removeToolbar()
    const result = await analyzeRefine({ originalText: selectedText })
    showModal(result, selectedText)
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

  const selectedText = window.getSelection().toString().trim()
  if (selectedText.length > 0) {
    createToolbar(e.pageX + 10, e.pageY + 10, selectedText)
  } else {
    removeToolbar()
  }
})

console.log('SAI content script loaded')