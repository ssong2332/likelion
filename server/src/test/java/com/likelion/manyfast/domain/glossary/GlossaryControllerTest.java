package com.likelion.manyfast.domain.glossary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.manyfast.domain.glossary.dto.GlossaryRequest;
import com.likelion.manyfast.domain.glossary.dto.GlossaryResponse;
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

@WebMvcTest(GlossaryController.class)
class GlossaryControllerTest {

    private static final GlossaryResponse EOD = new GlossaryResponse(
            1L,
            "EOD",
            "End of Day",
            "업무 종료 전까지"
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GlossaryService glossaryService;

    @Test
    void getsGlossaries() throws Exception {
        given(glossaryService.findAll()).willReturn(List.of(EOD));

        mockMvc.perform(get("/api/glossaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(1))
                .andExpect(jsonPath("$.data[0].term").value("EOD"))
                .andExpect(jsonPath("$.data[0].rule").value("End of Day"))
                .andExpect(jsonPath("$.data[0].note").value("업무 종료 전까지"))
                .andExpect(jsonPath("$.data[0].rule_text").doesNotExist())
                .andExpect(jsonPath("$.data[0].createdAt").doesNotExist());
    }

    @Test
    void getsEmptyGlossaryList() throws Exception {
        given(glossaryService.findAll()).willReturn(List.of());

        mockMvc.perform(get("/api/glossaries"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void createsGlossary() throws Exception {
        given(glossaryService.create(any(GlossaryRequest.class))).willReturn(EOD);

        mockMvc.perform(post("/api/glossaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "term": "EOD",
                                  "rule": "End of Day",
                                  "note": "업무 종료 전까지"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.term").value("EOD"))
                .andExpect(jsonPath("$.data.rule").value("End of Day"))
                .andExpect(jsonPath("$.data.note").value("업무 종료 전까지"))
                .andExpect(jsonPath("$.data.rule_text").doesNotExist());
    }

    @Test
    void updatesGlossary() throws Exception {
        GlossaryResponse updated = new GlossaryResponse(1L, "EOD", "End of business day", null);
        given(glossaryService.update(any(Long.class), any(GlossaryRequest.class))).willReturn(updated);

        mockMvc.perform(put("/api/glossaries/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "term": "EOD",
                                  "rule": "End of business day",
                                  "note": null
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.term").value("EOD"))
                .andExpect(jsonPath("$.data.rule").value("End of business day"))
                .andExpect(jsonPath("$.data.note").value((Object) null));
    }

    @Test
    void deletesGlossary() throws Exception {
        mockMvc.perform(delete("/api/glossaries/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Glossary entry deleted successfully"));

        verify(glossaryService).delete(1L);
    }

    @Test
    void rejectsMissingTerm() throws Exception {
        assertValidationError(Map.of("rule", "End of Day"), "term is required");
    }

    @Test
    void rejectsBlankTerm() throws Exception {
        assertValidationError(Map.of("term", "  ", "rule", "End of Day"), "term is required");
    }

    @Test
    void rejectsTermLongerThan100Characters() throws Exception {
        assertValidationError(
                Map.of("term", "a".repeat(101), "rule", "End of Day"),
                "term must be at most 100 characters"
        );
    }

    @Test
    void rejectsMissingRule() throws Exception {
        assertValidationError(Map.of("term", "EOD"), "rule is required");
    }

    @Test
    void rejectsBlankRule() throws Exception {
        assertValidationError(Map.of("term", "EOD", "rule", "  "), "rule is required");
    }

    @Test
    void rejectsRuleLongerThan255Characters() throws Exception {
        assertValidationError(
                Map.of("term", "EOD", "rule", "a".repeat(256)),
                "rule must be at most 255 characters"
        );
    }

    @Test
    void rejectsNoteLongerThan255Characters() throws Exception {
        assertValidationError(
                Map.of("term", "EOD", "rule", "End of Day", "note", "a".repeat(256)),
                "note must be at most 255 characters"
        );
    }

    @Test
    void rejectsUnreadableRequestBody() throws Exception {
        mockMvc.perform(post("/api/glossaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid glossary request body"));
    }

    @Test
    void rejectsNonNumericPathId() throws Exception {
        mockMvc.perform(delete("/api/glossaries/abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be a number"));
    }

    @Test
    void rejectsNonPositivePathId() throws Exception {
        mockMvc.perform(delete("/api/glossaries/0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("id must be positive"));
    }

    @Test
    void returnsNotFoundWhenUpdatingMissingGlossary() throws Exception {
        given(glossaryService.update(any(Long.class), any(GlossaryRequest.class)))
                .willThrow(new GlossaryNotFoundException(999L));

        mockMvc.perform(put("/api/glossaries/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "term": "EOD",
                                  "rule": "End of Day",
                                  "note": null
                                }
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Glossary not found: 999"));
    }

    @Test
    void returnsNotFoundWhenDeletingMissingGlossary() throws Exception {
        willThrow(new GlossaryNotFoundException(999L)).given(glossaryService).delete(999L);

        mockMvc.perform(delete("/api/glossaries/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("NOT_FOUND"))
                .andExpect(jsonPath("$.message").value("Glossary not found: 999"));
    }

    @Test
    void returnsConflictForDuplicateTerm() throws Exception {
        given(glossaryService.create(any(GlossaryRequest.class)))
                .willThrow(new DuplicateGlossaryTermException("EOD"));

        mockMvc.perform(post("/api/glossaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "term": "EOD",
                                  "rule": "End of Day",
                                  "note": null
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("CONFLICT"))
                .andExpect(jsonPath("$.message").value("Glossary term already exists: EOD"));
    }

    private void assertValidationError(Map<String, String> request, String expectedMessage) throws Exception {
        mockMvc.perform(post("/api/glossaries")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }
}
