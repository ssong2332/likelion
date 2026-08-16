package com.likelion.manyfast.domain.history;

import com.likelion.manyfast.domain.history.dto.MessageHistoryResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HistoryController.class)
class HistoryControllerTest {

    private static final String HISTORY_ENDPOINT = "/api/messages/history";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MessageHistoryService messageHistoryService;

    @Test
    void getsHistory() throws Exception {
        given(messageHistoryService.findHistory()).willReturn(List.of(
                new MessageHistoryResponse(
                        2L,
                        "Can you send this by EOD?",
                        "Could you please send this by the end of the day?",
                        Instant.parse("2026-08-16T10:30:00Z")
                )
        ));

        mockMvc.perform(get(HISTORY_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.history[0].id").isNumber())
                .andExpect(jsonPath("$.history[0].id").value(2))
                .andExpect(jsonPath("$.history[0].originalText").value("Can you send this by EOD?"))
                .andExpect(jsonPath("$.history[0].resultText").value("Could you please send this by the end of the day?"))
                .andExpect(jsonPath("$.history[0].createdAt").value("2026-08-16T10:30:00Z"))
                .andExpect(jsonPath("$.history[0].userId").doesNotExist())
                .andExpect(jsonPath("$.history[0].ownerId").doesNotExist());
    }

    @Test
    void getsEmptyHistory() throws Exception {
        given(messageHistoryService.findHistory()).willReturn(List.of());

        mockMvc.perform(get(HISTORY_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(0))
                .andExpect(jsonPath("$.history").isArray())
                .andExpect(jsonPath("$.history").isEmpty());
    }

    @Test
    void deletesExistingHistory() throws Exception {
        mockMvc.perform(delete("/api/messages/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("History item permanently deleted"));

        verify(messageHistoryService).delete(1L);
    }

    @Test
    void returnsNotFoundWhenDeletingMissingHistory() throws Exception {
        willThrow(new MessageHistoryNotFoundException(999999L))
                .given(messageHistoryService).delete(999999L);

        mockMvc.perform(delete("/api/messages/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Message history not found: 999999"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    @Test
    void rejectsNonNumericId() throws Exception {
        mockMvc.perform(delete("/api/messages/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be a number"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    @Test
    void rejectsZeroId() throws Exception {
        assertInvalidId(0L);
    }

    @Test
    void rejectsNegativeId() throws Exception {
        assertInvalidId(-1L);
    }

    @Test
    void deletesAllHistory() throws Exception {
        mockMvc.perform(delete("/api/messages/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("All message history permanently deleted"));

        verify(messageHistoryService).deleteAll();
    }

    @Test
    void routesAllToDeleteAllInsteadOfIdEndpoint() throws Exception {
        mockMvc.perform(delete("/api/messages/all"))
                .andExpect(status().isOk());

        verify(messageHistoryService).deleteAll();
        verify(messageHistoryService, never()).delete(anyLong());
    }

    private void assertInvalidId(Long id) throws Exception {
        mockMvc.perform(delete("/api/messages/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be positive"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
        verify(messageHistoryService, never()).delete(id);
    }

}
