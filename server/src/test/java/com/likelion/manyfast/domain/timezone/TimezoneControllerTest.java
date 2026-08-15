package com.likelion.manyfast.domain.timezone;

import com.likelion.manyfast.domain.timezone.dto.OffHoursCheckResponse;
import com.likelion.manyfast.domain.timezone.dto.TimezoneConvertResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.OffsetDateTime;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimezoneController.class)
class TimezoneControllerTest {

    private static final Instant DATE_TIME = Instant.parse("2026-07-15T12:00:00Z");
    private static final String SEOUL = "Asia/Seoul";
    private static final String NEW_YORK = "America/New_York";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimezoneService timezoneService;

    @Test
    void convertsTimezoneWithTypedResponse() throws Exception {
        given(timezoneService.convert(DATE_TIME, SEOUL, NEW_YORK))
                .willReturn(new TimezoneConvertResponse(
                        DATE_TIME,
                        SEOUL,
                        OffsetDateTime.parse("2026-07-15T21:00:00+09:00"),
                        NEW_YORK,
                        OffsetDateTime.parse("2026-07-15T08:00:00-04:00")
                ));

        mockMvc.perform(post("/api/timezone/convert")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dateTime": "2026-07-15T12:00:00Z",
                                  "senderTimezone": "Asia/Seoul",
                                  "receiverTimezone": "America/New_York"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dateTime").value("2026-07-15T12:00:00Z"))
                .andExpect(jsonPath("$.senderTimezone").value(SEOUL))
                .andExpect(jsonPath("$.senderLocalTime").value("2026-07-15T21:00:00+09:00"))
                .andExpect(jsonPath("$.receiverTimezone").value(NEW_YORK))
                .andExpect(jsonPath("$.receiverLocalTime").value("2026-07-15T08:00:00-04:00"));
    }

    @Test
    void checksOffHoursWithTypedResponse() throws Exception {
        Instant fridayNight = Instant.parse("2026-08-22T00:00:00Z");
        given(timezoneService.checkOffHours(fridayNight, NEW_YORK))
                .willReturn(new OffHoursCheckResponse(
                        NEW_YORK,
                        OffsetDateTime.parse("2026-08-21T20:00:00-04:00"),
                        true,
                        OffsetDateTime.parse("2026-08-24T09:00:00-04:00")
                ));

        mockMvc.perform(post("/api/timezone/check-offhours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "dateTime": "2026-08-22T00:00:00Z",
                                  "receiverTimezone": "America/New_York"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.receiverTimezone").value(NEW_YORK))
                .andExpect(jsonPath("$.receiverLocalTime").value("2026-08-21T20:00:00-04:00"))
                .andExpect(jsonPath("$.isReceiverOffHours").value(true))
                .andExpect(jsonPath("$.nextAvailableCheckingTime").value("2026-08-24T09:00:00-04:00"));
    }

    @Test
    void rejectsMissingDateTime() throws Exception {
        assertBadRequest(
                "/api/timezone/convert",
                """
                        {
                          "senderTimezone": "Asia/Seoul",
                          "receiverTimezone": "America/New_York"
                        }
                        """,
                "dateTime is required"
        );
    }

    @Test
    void rejectsMissingSenderTimezone() throws Exception {
        assertBadRequest(
                "/api/timezone/convert",
                """
                        {
                          "dateTime": "2026-07-15T12:00:00Z",
                          "receiverTimezone": "America/New_York"
                        }
                        """,
                "senderTimezone is required"
        );
    }

    @Test
    void rejectsMissingReceiverTimezone() throws Exception {
        assertBadRequest(
                "/api/timezone/convert",
                """
                        {
                          "dateTime": "2026-07-15T12:00:00Z",
                          "senderTimezone": "Asia/Seoul"
                        }
                        """,
                "receiverTimezone is required"
        );
    }

    @Test
    void rejectsBlankTimezone() throws Exception {
        assertBadRequest(
                "/api/timezone/check-offhours",
                """
                        {
                          "dateTime": "2026-07-15T12:00:00Z",
                          "receiverTimezone": "  "
                        }
                        """,
                "receiverTimezone is required"
        );
    }

    @Test
    void rejectsInvalidDateTime() throws Exception {
        assertBadRequest(
                "/api/timezone/convert",
                """
                        {
                          "dateTime": "not-a-date-time",
                          "senderTimezone": "Asia/Seoul",
                          "receiverTimezone": "America/New_York"
                        }
                        """,
                "Invalid dateTime format. Use ISO-8601 UTC format such as 2026-07-15T12:00:00Z."
        );
    }

    @Test
    void rejectsInvalidIanaTimezoneWithCommonErrorShape() throws Exception {
        given(timezoneService.convert(DATE_TIME, SEOUL, "Invalid/Zone"))
                .willThrow(new InvalidTimezoneException(
                        "Invalid/Zone",
                        new DateTimeException("Unknown time-zone ID")
                ));

        assertBadRequest(
                "/api/timezone/convert",
                """
                        {
                          "dateTime": "2026-07-15T12:00:00Z",
                          "senderTimezone": "Asia/Seoul",
                          "receiverTimezone": "Invalid/Zone"
                        }
                        """,
                "Invalid IANA timezone: Invalid/Zone"
        );
    }

    private void assertBadRequest(String path, String requestBody, String expectedMessage) throws Exception {
        mockMvc.perform(post(path)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }
}
