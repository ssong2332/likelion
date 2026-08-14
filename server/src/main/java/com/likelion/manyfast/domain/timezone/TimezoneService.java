package com.likelion.manyfast.domain.timezone;

import org.springframework.stereotype.Service;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class TimezoneService {

    public Map<String, Object> calculateTimezone(String senderTz, String receiverTz) {
        ZoneId senderZone = ZoneId.of(senderTz != null ? senderTz : "Asia/Seoul");
        ZoneId receiverZone = ZoneId.of(receiverTz != null ? receiverTz : "America/New_York");

        ZonedDateTime senderTime = ZonedDateTime.now(senderZone);
        ZonedDateTime receiverTime = ZonedDateTime.now(receiverZone);

        int receiverHour = receiverTime.getHour();
        boolean isReceiverOffHours = receiverHour < 9 || receiverHour >= 18;

        Map<String, Object> result = new HashMap<>();
        result.put("senderLocalTime", senderTime.toString());
        result.put("receiverLocalTime", receiverTime.toString());
        result.put("isReceiverOffHours", isReceiverOffHours);
        result.put("nextAvailableCheckingTime", isReceiverOffHours ? "현지 시각 오전 09:00 EST (약 6시간 뒤)" : "즉시 확인 가능");

        return result;
    }
}
