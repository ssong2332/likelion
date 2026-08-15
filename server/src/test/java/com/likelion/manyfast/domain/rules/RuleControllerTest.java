package com.likelion.manyfast.domain.rules;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.manyfast.domain.rules.dto.RuleRequest;
import com.likelion.manyfast.domain.rules.dto.RuleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RuleController.class)
class RuleControllerTest {

    private static final RuleResponse REPORT_DEADLINE = new RuleResponse(
            1L,
            "보고서 마감",
            "매주 목요일 17:00 KST까지 초안 공유"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RuleService ruleService;

    @Test
    void getsRules() throws Exception {
        given(ruleService.findAll()).willReturn(List.of(REPORT_DEADLINE));

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].name").value("보고서 마감"))
                .andExpect(jsonPath("$.data[0].description").value("매주 목요일 17:00 KST까지 초안 공유"));
    }

    @Test
    void getsEmptyRuleList() throws Exception {
        given(ruleService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/rules"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void createsRule() throws Exception {
        given(ruleService.create(any(RuleRequest.class))).willReturn(REPORT_DEADLINE);

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("보고서 마감"))
                .andExpect(jsonPath("$.data.description").value("매주 목요일 17:00 KST까지 초안 공유"));
    }

    @Test
    void updatesRule() throws Exception {
        RuleResponse updated = new RuleResponse(1L, "보고서 마감", "매주 금요일 12:00 KST까지 최종본 공유");
        given(ruleService.update(any(Long.class), any(RuleRequest.class))).willReturn(updated);

        mockMvc.perform(put("/api/rules/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "보고서 마감",
                                  "description": "매주 금요일 12:00 KST까지 최종본 공유"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.name").value("보고서 마감"))
                .andExpect(jsonPath("$.data.description").value("매주 금요일 12:00 KST까지 최종본 공유"));
    }

    @Test
    void deletesRule() throws Exception {
        mockMvc.perform(delete("/api/rules/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Rule deleted successfully"));

        verify(ruleService).delete(1L);
    }

    @Test
    void rejectsMissingName() throws Exception {
        assertValidationError(Map.of("description", "설명"), "name is required");
    }

    @Test
    void rejectsBlankName() throws Exception {
        assertValidationError(Map.of("name", "  ", "description", "설명"), "name is required");
    }

    @Test
    void rejectsNameLongerThan100Characters() throws Exception {
        assertValidationError(
                Map.of("name", "a".repeat(101), "description", "설명"),
                "name must be at most 100 characters"
        );
    }

    @Test
    void rejectsMissingDescription() throws Exception {
        assertValidationError(Map.of("name", "보고서 마감"), "description is required");
    }

    @Test
    void rejectsBlankDescription() throws Exception {
        assertValidationError(Map.of("name", "보고서 마감", "description", "  "), "description is required");
    }

    @Test
    void rejectsDescriptionLongerThan500Characters() throws Exception {
        assertValidationError(
                Map.of("name", "보고서 마감", "description", "a".repeat(501)),
                "description must be at most 500 characters"
        );
    }

    @Test
    void rejectsUnreadableRequestBody() throws Exception {
        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid rule request body"));
    }

    @Test
    void rejectsNonNumericPathId() throws Exception {
        mockMvc.perform(delete("/api/rules/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be a number"));
    }

    @Test
    void rejectsNonPositivePathId() throws Exception {
        mockMvc.perform(delete("/api/rules/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be positive"));

        mockMvc.perform(delete("/api/rules/-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be positive"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingRule() throws Exception {
        given(ruleService.update(any(Long.class), any(RuleRequest.class)))
                .willThrow(new RuleNotFoundException(999L));

        mockMvc.perform(put("/api/rules/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Rule not found: 999"));
    }

    @Test
    void returnsNotFoundWhenDeletingMissingRule() throws Exception {
        willThrow(new RuleNotFoundException(999L)).given(ruleService).delete(999L);

        mockMvc.perform(delete("/api/rules/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Rule not found: 999"));
    }

    @Test
    void returnsConflictForDuplicateName() throws Exception {
        given(ruleService.create(any(RuleRequest.class)))
                .willThrow(new DuplicateRuleNameException("보고서 마감"));

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Rule name already exists: 보고서 마감"));
    }

    @Test
    void exposesOnlyRuleResponseFields() throws Exception {
        given(ruleService.create(any(RuleRequest.class))).willReturn(REPORT_DEADLINE);

        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.name").exists())
                .andExpect(jsonPath("$.data.description").exists())
                .andExpect(jsonPath("$.data.createdAt").doesNotExist())
                .andExpect(jsonPath("$.data.updatedAt").doesNotExist())
                .andExpect(jsonPath("$.data.hibernateLazyInitializer").doesNotExist());
    }

    private void assertValidationError(Map<String, String> request, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    private String validRequest() {
        return """
                {
                  "name": "보고서 마감",
                  "description": "매주 목요일 17:00 KST까지 초안 공유"
                }
                """;
    }
}
