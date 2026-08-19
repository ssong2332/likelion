package com.likelion.manyfast.ai.service;

import com.likelion.manyfast.ai.dto.RefineRequestDto;
import com.likelion.manyfast.domain.glossary.GlossaryNotFoundException;
import com.likelion.manyfast.domain.glossary.GlossaryService;
import com.likelion.manyfast.domain.glossary.dto.GlossaryResponse;
import com.likelion.manyfast.domain.rules.RuleNotFoundException;
import com.likelion.manyfast.domain.rules.RuleService;
import com.likelion.manyfast.domain.rules.dto.RuleResponse;
import com.likelion.manyfast.domain.userstyle.CollaborationStyleService;
import com.likelion.manyfast.domain.userstyle.dto.CollaborationStyleResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiServicePromptIntegrationTest {

    @Mock
    private GlossaryService glossaryService;

    @Mock
    private RuleService ruleService;

    @Mock
    private CollaborationStyleService collaborationStyleService;

    private AiService aiService;

    @BeforeEach
    void setUp() {
        aiService = new AiService(glossaryService, ruleService, collaborationStyleService);
        lenient().when(glossaryService.findByIds(anyList())).thenReturn(List.of());
        lenient().when(ruleService.findByIds(anyList())).thenReturn(List.of());
        lenient().when(collaborationStyleService.get()).thenReturn(
                new CollaborationStyleResponse("polite", "balanced", "concise")
        );
    }

    @Test
    void selectedGlossaryIsLoadedAndIncludedInPrompt() {
        GlossaryResponse selected = new GlossaryResponse(
                1L,
                "EOD",
                "Keep EOD and clarify the local cutoff time",
                "Team deadline convention"
        );
        when(glossaryService.findByIds(List.of(1L))).thenReturn(List.of(selected));

        AiService.RefineContext context = aiService.prepareRefineContext(request(
                List.of(1L),
                List.of(),
                null
        ));

        assertThat(context.systemPrompt())
                .contains("Term: EOD")
                .contains("Rule: Keep EOD and clarify the local cutoff time")
                .contains("Note: Team deadline convention");
        verify(glossaryService).findByIds(List.of(1L));
    }

    @Test
    void unselectedGlossaryIsNotIncludedInPrompt() {
        GlossaryResponse selected = new GlossaryResponse(1L, "EOD", "Selected rule", null);
        when(glossaryService.findByIds(List.of(1L))).thenReturn(List.of(selected));

        AiService.RefineContext context = aiService.prepareRefineContext(request(
                List.of(1L),
                List.of(),
                null
        ));

        assertThat(context.systemPrompt())
                .contains("Term: EOD")
                .doesNotContain("Term: ASAP")
                .doesNotContain("Term: Manyfast")
                .doesNotContain("Term: PR");
    }

    @Test
    void selectedRuleIsLoadedAndIncludedInPrompt() {
        RuleResponse selected = new RuleResponse(
                7L,
                "보고서 마감",
                "매주 목요일 17:00 KST까지 초안을 공유한다"
        );
        when(ruleService.findByIds(List.of(7L))).thenReturn(List.of(selected));

        AiService.RefineContext context = aiService.prepareRefineContext(request(
                List.of(),
                List.of(7L),
                null
        ));

        assertThat(context.systemPrompt())
                .contains("Name: 보고서 마감")
                .contains("Description: 매주 목요일 17:00 KST까지 초안을 공유한다");
        verify(ruleService).findByIds(List.of(7L));
    }

    @Test
    void savedUserStyleIncludingDetailLevelIsUsedWhenNoOverrideExists() {
        when(collaborationStyleService.get()).thenReturn(
                new CollaborationStyleResponse("friendly", "direct", "detailed")
        );

        AiService.RefineContext context = aiService.prepareRefineContext(request(
                List.of(),
                List.of(),
                null
        ));

        assertThat(context.collaborationStyle()).isEqualTo(
                new CollaborationStyleResponse("friendly", "direct", "detailed")
        );
        assertThat(context.systemPrompt())
                .contains("Tone: friendly")
                .contains("Directness: direct")
                .contains("Detail Level: detailed")
                .contains("Length Level: medium");
        verify(collaborationStyleService).get();
    }

    @Test
    void requestStyleOverridesOnlyProvidedFieldsAndKeepsSavedDefaults() {
        when(collaborationStyleService.get()).thenReturn(
                new CollaborationStyleResponse("polite", "balanced", "detailed")
        );

        AiService.RefineContext context = aiService.prepareRefineContext(request(
                List.of(),
                List.of(),
                Map.of("tone", "friendly")
        ));

        assertThat(context.collaborationStyle()).isEqualTo(
                new CollaborationStyleResponse("friendly", "balanced", "detailed")
        );
        assertThat(context.systemPrompt())
                .contains("Tone: friendly")
                .contains("Directness: balanced")
                .contains("Detail Level: detailed");
    }

    @Test
    void requestLengthLevelIsAppliedAsOptionalPromptOnlyOverride() {
        when(collaborationStyleService.get()).thenReturn(
                new CollaborationStyleResponse("polite", "balanced", "detailed")
        );

        AiService.RefineContext context = aiService.prepareRefineContext(request(
                List.of(),
                List.of(),
                Map.of("tone", "friendly", "lengthLevel", "short")
        ));

        assertThat(context.collaborationStyle()).isEqualTo(
                new CollaborationStyleResponse("friendly", "balanced", "detailed")
        );
        assertThat(context.lengthLevel()).isEqualTo("short");
        assertThat(context.systemPrompt())
                .contains("Tone: friendly")
                .contains("Length Level: short")
                .contains("SHORT: Use one compact sentence or two very short lines.");
    }

    @Test
    void missingGlossaryIdIsPropagatedAsDomainNotFoundException() {
        when(glossaryService.findByIds(List.of(999L)))
                .thenThrow(new GlossaryNotFoundException(999L));

        assertThatThrownBy(() -> aiService.prepareRefineContext(request(
                List.of(999L),
                List.of(),
                null
        )))
                .isInstanceOf(GlossaryNotFoundException.class)
                .hasMessage("Glossary not found: 999");
    }

    @Test
    void missingRuleIdIsPropagatedAsDomainNotFoundException() {
        when(ruleService.findByIds(List.of(999L)))
                .thenThrow(new RuleNotFoundException(999L));

        assertThatThrownBy(() -> aiService.prepareRefineContext(request(
                List.of(),
                List.of(999L),
                null
        )))
                .isInstanceOf(RuleNotFoundException.class)
                .hasMessage("Rule not found: 999");
    }

    private RefineRequestDto request(
            List<Long> glossaryIds,
            List<Long> ruleIds,
            Map<String, Object> collaborationStyle
    ) {
        return RefineRequestDto.builder()
                .originalText("EOD까지 보고서를 공유해 주세요")
                .targetLang("en")
                .collaborationStyle(collaborationStyle)
                .appliedGlossaryIds(glossaryIds)
                .appliedRuleIds(ruleIds)
                .build();
    }
}
