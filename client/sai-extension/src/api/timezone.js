// src/api/timezone.js
import { request } from './config.js';

export async function checkOffHours({ dateTime, receiverTimezone }) {
  return request('/api/timezone/check-offhours', {
    method: 'POST',
    body: JSON.stringify({
      dateTime: dateTime || new Date().toISOString().slice(0, 19),
      receiverTimezone,
    }),
  });
}

export async function convertTimezone({ dateTime, senderTimezone, receiverTimezone }) {
  return request('/api/timezone/convert', {
    method: 'POST',
    body: JSON.stringify({
      dateTime: dateTime || new Date().toISOString().slice(0, 19),
      senderTimezone,
      receiverTimezone,
    }),
  });
}