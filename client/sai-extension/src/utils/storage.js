// src/utils/storage.js

export async function getStorage(key, defaultValue = null) {
  if (typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local) {
    return new Promise((resolve) => {
      chrome.storage.local.get([key], (result) => {
        if (result && result[key] !== undefined) {
          resolve(result[key])
        } else {
          resolve(defaultValue)
        }
      })
    })
  }
  try {
    const val = localStorage.getItem(key)
    return val ? JSON.parse(val) : defaultValue
  } catch (_) {
    return defaultValue
  }
}

export async function setStorage(key, value) {
  if (typeof chrome !== 'undefined' && chrome.storage && chrome.storage.local) {
    return new Promise((resolve) => {
      chrome.storage.local.set({ [key]: value }, () => resolve())
    })
  }
  try {
    localStorage.setItem(key, JSON.stringify(value))
  } catch (_) {}
}
