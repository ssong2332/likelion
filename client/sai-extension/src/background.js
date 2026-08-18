// Alt+S 단축키를 눌렀을 때 사이드패널을 열어주는 역할
chrome.action.onClicked.addListener((tab) => {
  chrome.sidePanel.open({ tabId: tab.id })
})