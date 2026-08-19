package com.likelion.manyfast.domain.userstyle;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleRequest;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserStyleController.class)
class UserStyleControllerTest {

    private static final String ENDPOINT = "/api/user/collaboration-style";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CollaborationStyleService collaborationStyleService;

    @Test
    void getsStoredCollaborationStyle() throws Exception {
        given(collaborationStyleService.get())
                .willReturn(new CollaborationStyleResponse("friendly", "direct", "detailed"));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tone").value("friendly"))
                .andExpect(jsonPath("$.data.directness").value("direct"))
                .andExpect(jsonPath("$.data.detailLevel").value("detailed"))
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

    @Test
    void getsDefaultCollaborationStyle() throws Exception {
        given(collaborationStyleService.get())
                .willReturn(new CollaborationStyleResponse("polite", "balanced", "concise"));

        mockMvc.perform(get(ENDPOINT))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tone").value("polite"))
                .andExpect(jsonPath("$.data.directness").value("balanced"))
                .andExpect(jsonPath("$.data.detailLevel").value("concise"))
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

    @Test
    void updatesCollaborationStyle() throws Exception {
        given(collaborationStyleService.update(any(CollaborationStyleRequest.class)))
                .willReturn(new CollaborationStyleResponse("friendly", "direct", "detailed"));

        mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequest()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.tone").value("friendly"))
                .andExpect(jsonPath("$.data.directness").value("direct"))
                .andExpect(jsonPath("$.data.detailLevel").value("detailed"))
                .andExpect(jsonPath("$.data.id").doesNotExist());
    }

    @Test
    void rejectsMissingTone() throws Exception {
        assertValidationError(
                Map.of("directness", "direct", "detailLevel", "detailed"),
                "tone is required"
        );
    }

    @Test
    void rejectsBlankTone() throws Exception {
        assertBadRequest(Map.of("tone", " ", "directness", "direct", "detailLevel", "detailed"));
    }

    @Test
    void rejectsUnsupportedTone() throws Exception {
        assertBadRequest(Map.of("tone", "formal", "directness", "direct", "detailLevel", "detailed"));
    }

    @Test
    void rejectsToneLongerThan50Characters() throws Exception {
        assertBadRequest(Map.of("tone", "a".repeat(51), "directness", "direct", "detailLevel", "detailed"));
    }

    @Test
    void rejectsMissingDirectness() throws Exception {
        assertValidationError(
                Map.of("tone", "friendly", "detailLevel", "detailed"),
                "directness is required"
        );
    }

    @Test
    void rejectsBlankDirectness() throws Exception {
        assertBadRequest(Map.of("tone", "friendly", "directness", " ", "detailLevel", "detailed"));
    }

    @Test
    void rejectsUnsupportedDirectness() throws Exception {
        assertBadRequest(Map.of("tone", "friendly", "directness", "sideways", "detailLevel", "detailed"));
    }

    @Test
    void acceptsIndirectDirectness() throws Exception {
        given(collaborationStyleService.update(any(CollaborationStyleRequest.class)))
                .willReturn(new CollaborationStyleResponse("friendly", "indirect", "detailed"));

        mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "tone", "friendly",
                                "directness", "indirect",
                                "detailLevel", "detailed"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.directness").value("indirect"));
    }

    @Test
    void rejectsDirectnessLongerThan50Characters() throws Exception {
        assertBadRequest(Map.of("tone", "friendly", "directness", "a".repeat(51), "detailLevel", "detailed"));
    }

    @Test
    void rejectsMissingDetailLevel() throws Exception {
        assertValidationError(
                Map.of("tone", "friendly", "directness", "direct"),
                "detailLevel is required"
        );
    }

    @Test
    void rejectsBlankDetailLevel() throws Exception {
        assertBadRequest(Map.of("tone", "friendly", "directness", "direct", "detailLevel", " "));
    }

    @Test
    void rejectsDetailLevelLongerThan50Characters() throws Exception {
        assertBadRequest(Map.of("tone", "friendly", "directness", "direct", "detailLevel", "a".repeat(51)));
    }

    @Test
    void rejectsMalformedJsonWithScopedErrorFormat() throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("Invalid collaboration style request body"))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    private void assertValidationError(Map<String, String> request, String message) throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value(message))
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    private void assertBadRequest(Map<String, String> request) throws Exception {
        mockMvc.perform(put(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.timestamp").doesNotExist());
    }

    private String validRequest() {
        return """
                {
                  "tone": "friendly",
                  "directness": "direct",
                  "detailLevel": "detailed"
                }
                """;
    }
}
