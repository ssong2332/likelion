// src/background.js

// Alt+S 단축키나 액션 아이콘을 눌렀을 때 사이드패널을 열어주는 역할
chrome.action.onClicked.addListener((tab) => {
  if (chrome.sidePanel && tab?.id) {
    chrome.sidePanel.open({ tabId: tab.id })
  }
})

// Content Script의 CSP / Mixed Content 제약을 우회하기 위한 백그라운드 API 프록시 핸들러
chrome.runtime.onMessage.addListener((request, sender, sendResponse) => {
  if (request.type === 'API_REQUEST') {
    const { path, options = {} } = request
    const url = `http://1.201.117.20:8080${path}`

    fetch(url, options)
      .then(async (res) => {
        const text = await res.text()
        let data
        try {
          data = JSON.parse(text)
        } catch (_) {
          data = text
        }

        if (!res.ok) {
          const errMsg = (data && (data.message || data.error)) || res.statusText || '서버 오류'
          sendResponse({ success: false, status: res.status, error: errMsg })
        } else {
          sendResponse({ success: true, data })
        }
      })
      .catch((err) => {
        sendResponse({
          success: false,
          error: `가비아 백엔드 서버(http://1.201.117.20:8080)에 연결할 수 없습니다. 서버가 실행 중인지 확인해 주세요. (${err.message})`,
        })
      })

    return true // 비동기 응답(sendResponse)을 위해 채널 유지
  }
})