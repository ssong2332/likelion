// src/api/config.js
export const API_BASE_URL = 'http://localhost:8080';

export async function request(path, options = {}) {
  // 크롬 확장 프로그램 환경(Content Script 또는 SidePanel)에서는 Background Service Worker를 경유하여
  // 대상 웹사이트의 CSP / Mixed Content 제약을 우회합니다.
  if (typeof chrome !== 'undefined' && chrome.runtime && chrome.runtime.sendMessage) {
    return new Promise((resolve, reject) => {
      chrome.runtime.sendMessage(
        {
          type: 'API_REQUEST',
          path,
          options: {
            ...options,
            headers: {
              'Content-Type': 'application/json',
              ...options.headers,
            },
          },
        },
        (response) => {
          if (chrome.runtime.lastError) {
            return reject(new Error(chrome.runtime.lastError.message));
          }
          if (!response) {
            return reject(new Error('백그라운드 서비스 워커로부터 응답이 없습니다.'));
          }
          if (!response.success) {
            return reject(new Error(response.error || '요청 처리에 실패했습니다.'));
          }
          resolve(response.data);
        }
      );
    });
  }

  // 일반 브라우저 환경 직접 호출 Fallback
  const url = `${API_BASE_URL}${path}`;
  const response = await fetch(url, {
    ...options,
    headers: {
      'Content-Type': 'application/json',
      ...options.headers,
    },
  });

  if (!response.ok) {
    let errorMessage = `HTTP ${response.status}: ${response.statusText}`;
    try {
      const errorJson = await response.json();
      if (errorJson.message) errorMessage = errorJson.message;
      else if (errorJson.error) errorMessage = errorJson.error;
    } catch (_) {}
    throw new Error(errorMessage);
  }

  return response.json();
}
