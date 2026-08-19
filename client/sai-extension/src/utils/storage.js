// src/utils/storage.js

export async function getStorage(key, defaultValue = null) {
  if (typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local) {
    const fromChrome = await new Promise((resolve) => {
      try {
        chrome.storage.local.get([key], (result) => {
          if (result && result[key] !== undefined) {
            resolve(result[key])
          } else {
            resolve(null)
          }
        })
      } catch (_) {
        resolve(null)
      }
    })
    if (fromChrome !== null && fromChrome !== undefined) return fromChrome
  }

  try {
    const val = localStorage.getItem(key)
    return val ? JSON.parse(val) : defaultValue
  } catch (_) {
    return defaultValue
  }
}

export async function setStorage(key, value) {
  // 1. localStorage 즉시 저장 (동기식 백업)
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (_) {}

  // 2. Chrome/Edge 전역 스토리지 저장
  if (typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local) {
    return new Promise((resolve) => {
      try {
        chrome.storage.local.set({ [key]: value }, () => resolve())
      } catch (_) {
        resolve()
      }
    })
  }
}
