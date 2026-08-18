// src/api/timezone.js

export async function checkOffHours({ dateTime, receiverTimezone }) {
  await new Promise((r) => setTimeout(r, 300))
  return {
    receiverTimezone,
    receiverLocalTime: new Date().toISOString(),
    isReceiverOffHours: true,
    nextAvailableCheckingTime: null,
  }
}

export async function convertTimezone({ dateTime, senderTimezone, receiverTimezone }) {
  await new Promise((r) => setTimeout(r, 300))
  return {
    dateTime,
    senderTimezone,
    senderLocalTime: new Date().toISOString(),
    receiverTimezone,
    receiverLocalTime: new Date().toISOString(),
  }
}