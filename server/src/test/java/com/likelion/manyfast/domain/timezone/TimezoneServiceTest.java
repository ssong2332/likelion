package com.likelion.manyfast.domain.timezone;

import com.likelion.manyfast.domain.timezone.dto.OffHoursCheckResponse;
import com.likelion.manyfast.domain.timezone.dto.TimezoneConvertResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class TimezoneServiceTest {

    private static final String SEOUL = "Asia/Seoul";
    private static final String NEW_YORK = "America/New_York";
    private static final Clock FIXED_CLOCK = Clock.fixed(
            Instant.parse("2026-08-17T11:00:00Z"),
            ZoneOffset.UTC
    );

    private final TimezoneService timezoneService = new TimezoneService(FIXED_CLOCK);

    @Test
    void convertsSameInstantFromSeoulToNewYorkDuringDst() {
        Instant dateTime = Instant.parse("2026-07-15T12:00:00Z");

        TimezoneConvertResponse response = timezoneService.convert(dateTime, SEOUL, NEW_YORK);

        assertThat(response.senderLocalTime()).isEqualTo(OffsetDateTime.parse("2026-07-15T21:00:00+09:00"));
        assertThat(response.receiverLocalTime()).isEqualTo(OffsetDateTime.parse("2026-07-15T08:00:00-04:00"));
        assertThat(response.senderLocalTime().toInstant()).isEqualTo(dateTime);
        assertThat(response.receiverLocalTime().toInstant()).isEqualTo(dateTime);
        assertThat(response.receiverLocalTime().getOffset()).isEqualTo(ZoneOffset.ofHours(-4));
    }

    @Test
    void appliesNewYorkStandardTimeOutsideDst() {
        Instant dateTime = Instant.parse("2026-01-15T12:00:00Z");

        TimezoneConvertResponse response = timezoneService.convert(dateTime, SEOUL, NEW_YORK);

        assertThat(response.receiverLocalTime()).isEqualTo(OffsetDateTime.parse("2026-01-15T07:00:00-05:00"));
        assertThat(response.receiverLocalTime().getOffset()).isEqualTo(ZoneOffset.ofHours(-5));
    }

    @ParameterizedTest
    @MethodSource("offHoursBoundaryCases")
    void determinesOffHoursAtBusinessBoundaries(String receiverLocalDateTime, boolean expectedOffHours) {
        Instant dateTime = instantAt(receiverLocalDateTime, NEW_YORK);

        OffHoursCheckResponse response = timezoneService.checkOffHours(dateTime, NEW_YORK);

        assertThat(response.isReceiverOffHours()).isEqualTo(expectedOffHours);
        if (expectedOffHours) {
            assertThat(response.nextAvailableCheckingTime()).isNotNull();
        } else {
            assertThat(response.nextAvailableCheckingTime()).isNull();
        }
    }

    private static Stream<Arguments> offHoursBoundaryCases() {
        return Stream.of(
                arguments("2026-08-17T08:59:00", true),
                arguments("2026-08-17T09:00:00", false),
                arguments("2026-08-17T17:59:00", false),
                arguments("2026-08-17T18:00:00", true),
                arguments("2026-08-22T12:00:00", true),
                arguments("2026-08-23T12:00:00", true)
        );
    }

    @ParameterizedTest
    @MethodSource("nextAvailableTimeCases")
    void calculatesNextAvailableBusinessTime(String receiverLocalDateTime, String expectedLocalDateTime) {
        Instant dateTime = instantAt(receiverLocalDateTime, NEW_YORK);

        OffHoursCheckResponse response = timezoneService.checkOffHours(dateTime, NEW_YORK);

        assertThat(response.isReceiverOffHours()).isTrue();
        assertThat(response.nextAvailableCheckingTime())
                .isEqualTo(offsetDateTimeAt(expectedLocalDateTime, NEW_YORK));
    }

    private static Stream<Arguments> nextAvailableTimeCases() {
        return Stream.of(
                arguments("2026-08-17T07:00:00", "2026-08-17T09:00:00"),
                arguments("2026-08-17T20:00:00", "2026-08-18T09:00:00"),
                arguments("2026-08-21T20:00:00", "2026-08-24T09:00:00"),
                arguments("2026-08-22T12:00:00", "2026-08-24T09:00:00"),
                arguments("2026-08-23T12:00:00", "2026-08-24T09:00:00")
        );
    }

    @Test
    void rejectsInvalidIanaTimezone() {
        assertThatThrownBy(() -> timezoneService.convert(
                Instant.parse("2026-07-15T12:00:00Z"),
                SEOUL,
                "Invalid/Zone"
        ))
                .isInstanceOf(InvalidTimezoneException.class)
                .hasMessage("Invalid IANA timezone: Invalid/Zone");
    }

    @ParameterizedTest
    @MethodSource("backendAAdapterTimezoneCases")
    void compatibilityAdapterUsesInjectedClockAndDefaultsOnlyNullTimezones(
            String senderTimezone,
            String receiverTimezone
    ) {
        Map<String, Object> timezoneInfo = timezoneService.calculateTimezone(
                senderTimezone,
                receiverTimezone
        );

        assertThat(timezoneInfo).containsOnlyKeys(
                "senderLocalTime",
                "receiverLocalTime",
                "isReceiverOffHours",
                "nextAvailableCheckingTime"
        );
        assertThat(timezoneInfo.get("senderLocalTime"))
                .isEqualTo(OffsetDateTime.parse("2026-08-17T20:00:00+09:00"));
        assertThat(timezoneInfo.get("receiverLocalTime"))
                .isEqualTo(OffsetDateTime.parse("2026-08-17T07:00:00-04:00"));
        assertThat(timezoneInfo.get("isReceiverOffHours")).isEqualTo(true);
        assertThat(timezoneInfo.get("nextAvailableCheckingTime"))
                .isEqualTo(OffsetDateTime.parse("2026-08-17T09:00:00-04:00"));
    }

    private static Stream<Arguments> backendAAdapterTimezoneCases() {
        return Stream.of(
                arguments(SEOUL, NEW_YORK),
                arguments(null, NEW_YORK),
                arguments(SEOUL, null),
                arguments(null, null)
        );
    }

    private static Instant instantAt(String localDateTime, String timezone) {
        return LocalDateTime.parse(localDateTime).atZone(ZoneId.of(timezone)).toInstant();
    }

    private static OffsetDateTime offsetDateTimeAt(String localDateTime, String timezone) {
        return LocalDateTime.parse(localDateTime).atZone(ZoneId.of(timezone)).toOffsetDateTime();
    }
}
