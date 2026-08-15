package com.likelion.manyfast.domain.timezone;

import com.likelion.manyfast.domain.timezone.dto.OffHoursCheckResponse;
import com.likelion.manyfast.domain.timezone.dto.TimezoneConvertResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimezoneService {

    static final LocalTime BUSINESS_START = LocalTime.of(9, 0);
    static final LocalTime BUSINESS_END = LocalTime.of(18, 0);

    private final Clock clock;

    public TimezoneConvertResponse convert(
            Instant dateTime,
            String senderTimezone,
            String receiverTimezone
    ) {
        ZoneId senderZone = parseZoneId(senderTimezone);
        ZoneId receiverZone = parseZoneId(receiverTimezone);

        OffsetDateTime senderLocalTime = dateTime.atZone(senderZone).toOffsetDateTime();
        OffsetDateTime receiverLocalTime = dateTime.atZone(receiverZone).toOffsetDateTime();

        return new TimezoneConvertResponse(
                dateTime,
                senderTimezone,
                senderLocalTime,
                receiverTimezone,
                receiverLocalTime
        );
    }

    public OffHoursCheckResponse checkOffHours(Instant dateTime, String receiverTimezone) {
        ZoneId receiverZone = parseZoneId(receiverTimezone);
        ZonedDateTime receiverLocalDateTime = dateTime.atZone(receiverZone);
        boolean receiverOffHours = isOffHours(receiverLocalDateTime);

        OffsetDateTime nextAvailableCheckingTime = receiverOffHours
                ? calculateNextAvailableTime(receiverLocalDateTime).toOffsetDateTime()
                : null;

        return new OffHoursCheckResponse(
                receiverTimezone,
                receiverLocalDateTime.toOffsetDateTime(),
                receiverOffHours,
                nextAvailableCheckingTime
        );
    }

    /**
     * Compatibility adapter used by Backend A. New timezone APIs should pass an explicit Instant.
     */
    public Map<String, Object> calculateTimezone(String senderTimezone, String receiverTimezone) {
        String effectiveSenderTimezone = senderTimezone != null ? senderTimezone : "Asia/Seoul";
        String effectiveReceiverTimezone = receiverTimezone != null ? receiverTimezone : "America/New_York";
        Instant currentInstant = clock.instant();
        TimezoneConvertResponse conversion = convert(
                currentInstant,
                effectiveSenderTimezone,
                effectiveReceiverTimezone
        );
        OffHoursCheckResponse offHours = checkOffHours(currentInstant, effectiveReceiverTimezone);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("senderLocalTime", conversion.senderLocalTime());
        result.put("receiverLocalTime", conversion.receiverLocalTime());
        result.put("isReceiverOffHours", offHours.isReceiverOffHours());
        result.put("nextAvailableCheckingTime", offHours.nextAvailableCheckingTime());
        return result;
    }

    private boolean isOffHours(ZonedDateTime localDateTime) {
        if (!isBusinessDay(localDateTime.getDayOfWeek())) {
            return true;
        }

        LocalTime localTime = localDateTime.toLocalTime();
        return localTime.isBefore(BUSINESS_START) || !localTime.isBefore(BUSINESS_END);
    }

    private ZonedDateTime calculateNextAvailableTime(ZonedDateTime localDateTime) {
        LocalDate nextBusinessDate = localDateTime.toLocalDate();

        if (!(isBusinessDay(localDateTime.getDayOfWeek())
                && localDateTime.toLocalTime().isBefore(BUSINESS_START))) {
            nextBusinessDate = nextBusinessDate.plusDays(1);
        }

        while (!isBusinessDay(nextBusinessDate.getDayOfWeek())) {
            nextBusinessDate = nextBusinessDate.plusDays(1);
        }

        return ZonedDateTime.of(nextBusinessDate, BUSINESS_START, localDateTime.getZone());
    }

    private boolean isBusinessDay(DayOfWeek dayOfWeek) {
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }

    private ZoneId parseZoneId(String timezone) {
        try {
            return ZoneId.of(timezone);
        } catch (DateTimeException | NullPointerException exception) {
            throw new InvalidTimezoneException(timezone, exception);
        }
    }
}
