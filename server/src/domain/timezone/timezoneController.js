import { calculateTimezoneInfo } from './timezoneService.js';

export function handleConvertTimezone(req, res) {
  const { senderTz, receiverTz } = req.body;
  const result = calculateTimezoneInfo(senderTz, receiverTz);
  res.json(result);
}
