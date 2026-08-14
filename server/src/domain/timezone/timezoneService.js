// Timezone & Off-hours Calculator (Backend B - F-1)

export function calculateTimezoneInfo(senderTz = 'Asia/Seoul', receiverTz = 'America/New_York') {
  const now = new Date();

  let senderHour = now.getHours();
  let receiverHour = (senderHour - 13 + 24) % 24; // approximate KST to EST

  try {
    const receiverTimeStr = new Intl.DateTimeFormat('en-US', {
      timeZone: receiverTz,
      hour: 'numeric',
      hour12: false
    }).format(now);
    receiverHour = parseInt(receiverTimeStr, 10);
  } catch (e) {
    // fallback
  }

  const isReceiverOffHours = receiverHour < 9 || receiverHour >= 18;
  const nextAvailableCheckingTime = isReceiverOffHours ? "현지 시각 오전 09:00 EST (약 6시간 뒤)" : "즉시 확인 가능";

  return {
    senderLocalTime: now.toISOString(),
    receiverLocalTime: new Date(now.getTime() - 13 * 3600 * 1000).toISOString(),
    isReceiverOffHours,
    nextAvailableCheckingTime
  };
}
