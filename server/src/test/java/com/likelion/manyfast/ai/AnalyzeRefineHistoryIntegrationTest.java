package com.likelion.manyfast.ai;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.manyfast.domain.history.MessageHistoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "openai.api-key=",
        "spring.datasource.url=jdbc:h2:mem:history-integration;DB_CLOSE_DELAY=-1;MODE=MySQL",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
@AutoConfigureMockMvc
class AnalyzeRefineHistoryIntegrationTest {

    private static final String ANALYZE_ENDPOINT = "/api/ai/analyze-refine";
    private static final String HISTORY_ENDPOINT = "/api/messages/history";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MessageHistoryRepository messageHistoryRepository;

    @BeforeEach
    void clearHistory() {
        messageHistoryRepository.deleteAllInBatch();
    }

    @Test
    void successfulAnalyzeRefineStoresExactlyTheReturnedResultOnce() throws Exception {
        String originalText = "오늘 안에 검토 부탁드립니다.";

        MvcResult analyzeResult = analyze(originalText)
                .andExpect(status().isOk())
                .andReturn();
        JsonNode response = objectMapper.readTree(analyzeResult.getResponse().getContentAsString());
        String refinedText = response.path("refinedText").asText();

        mockMvc.perform(get(HISTORY_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(1))
                .andExpect(jsonPath("$.history.length()").value(1))
                .andExpect(jsonPath("$.history[0].originalText").value(originalText))
                .andExpect(jsonPath("$.history[0].resultText").value(refinedText))
                .andExpect(jsonPath("$.history[0].createdAt").isNotEmpty());
    }

    @Test
    void twoAnalyzeRefineRequestsAccumulateTwoHistoryItems() throws Exception {
        analyze("첫 번째 검토 요청입니다.").andExpect(status().isOk());
        analyze("두 번째 검토 요청입니다.").andExpect(status().isOk());

        mockMvc.perform(get(HISTORY_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(2))
                .andExpect(jsonPath("$.history.length()").value(2));
    }

    @Test
    void invalidRequestsDoNotCreateHistory() throws Exception {
        analyze("   ").andExpect(status().isBadRequest());
        assertHistoryCount(0);

        mockMvc.perform(post(ANALYZE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalText": "Glossary 참조 실패",
                                  "appliedGlossaryIds": [999999]
                                }
                                """))
                .andExpect(status().isNotFound());
        assertHistoryCount(0);

        mockMvc.perform(post(ANALYZE_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "originalText": "Rule 참조 실패",
                                  "appliedRuleIds": [999999]
                                }
                                """))
                .andExpect(status().isNotFound());
        assertHistoryCount(0);
    }

    private org.springframework.test.web.servlet.ResultActions analyze(String originalText)
            throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("originalText", originalText)
        );
        return mockMvc.perform(post(ANALYZE_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    private void assertHistoryCount(int count) throws Exception {
        mockMvc.perform(get(HISTORY_ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(count));
    }
}
