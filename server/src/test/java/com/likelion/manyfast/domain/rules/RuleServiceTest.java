package com.likelion.manyfast.domain.rules;

import com.likelion.manyfast.domain.rules.dto.RuleRequest;
import com.likelion.manyfast.domain.rules.dto.RuleResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RuleServiceTest {

    @Mock
    private RuleRepository ruleRepository;

    @InjectMocks
    private RuleService ruleService;

    @Test
    void createsRule() {
        RuleRequest request = new RuleRequest("보고서 마감", "매주 목요일 17:00 KST까지 초안 공유");
        when(ruleRepository.existsByName("보고서 마감")).thenReturn(false);
        when(ruleRepository.saveAndFlush(any(Rule.class))).thenAnswer(invocation -> {
            Rule rule = invocation.getArgument(0);
            ReflectionTestUtils.setField(rule, "id", 1L);
            return rule;
        });

        RuleResponse response = ruleService.create(request);

        assertThat(response).isEqualTo(new RuleResponse(1L, "보고서 마감", "매주 목요일 17:00 KST까지 초안 공유"));
        ArgumentCaptor<Rule> captor = ArgumentCaptor.forClass(Rule.class);
        verify(ruleRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getName()).isEqualTo("보고서 마감");
        assertThat(captor.getValue().getDescription()).isEqualTo("매주 목요일 17:00 KST까지 초안 공유");
    }

    @Test
    void findsAllRulesOrderedByIdAscending() {
        Rule first = rule(1L, "보고서 마감", "첫 번째 규칙");
        Rule second = rule(2L, "회의록 공유", "두 번째 규칙");
        Sort idAscending = Sort.by(Sort.Direction.ASC, "id");
        when(ruleRepository.findAll(idAscending)).thenReturn(List.of(first, second));

        List<RuleResponse> responses = ruleService.findAll();

        assertThat(responses).extracting(RuleResponse::id).containsExactly(1L, 2L);
        verify(ruleRepository).findAll(idAscending);
    }

    @Test
    void findsOnlySelectedRulesByIdInRequestOrder() {
        Rule first = rule(1L, "보고서 마감", "첫 번째 규칙");
        Rule third = rule(3L, "회의록 공유", "세 번째 규칙");
        when(ruleRepository.findAllById(any())).thenReturn(List.of(first, third));

        List<RuleResponse> responses = ruleService.findByIds(List.of(3L, 1L, 3L));

        assertThat(responses).extracting(RuleResponse::id).containsExactly(3L, 1L);
        verify(ruleRepository).findAllById(new LinkedHashSet<>(List.of(3L, 1L)));
    }

    @Test
    void rejectsMissingSelectedRuleId() {
        Rule existing = rule(1L, "보고서 마감", "첫 번째 규칙");
        when(ruleRepository.findAllById(any())).thenReturn(List.of(existing));

        assertThatThrownBy(() -> ruleService.findByIds(List.of(1L, 999L)))
                .isInstanceOf(RuleNotFoundException.class)
                .hasMessage("Rule not found: 999");
    }

    @Test
    void updatesRule() {
        Rule existing = rule(1L, "보고서 마감", "기존 설명");
        RuleRequest request = new RuleRequest("최종본 마감", "매주 금요일 12:00 KST까지 최종본 공유");
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ruleRepository.existsByNameAndIdNot("최종본 마감", 1L)).thenReturn(false);
        when(ruleRepository.saveAndFlush(existing)).thenReturn(existing);

        RuleResponse response = ruleService.update(1L, request);

        assertThat(response).isEqualTo(new RuleResponse(1L, "최종본 마감", "매주 금요일 12:00 KST까지 최종본 공유"));
    }

    @Test
    void deletesRule() {
        Rule existing = rule(1L, "보고서 마감", "기존 설명");
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(existing));

        ruleService.delete(1L);

        verify(ruleRepository).delete(existing);
    }

    @Test
    void rejectsUpdateWhenRuleDoesNotExist() {
        RuleRequest request = new RuleRequest("보고서 마감", "설명");
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.update(999L, request))
                .isInstanceOf(RuleNotFoundException.class)
                .hasMessage("Rule not found: 999");
    }

    @Test
    void rejectsDeleteWhenRuleDoesNotExist() {
        when(ruleRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> ruleService.delete(999L))
                .isInstanceOf(RuleNotFoundException.class)
                .hasMessage("Rule not found: 999");
    }

    @Test
    void rejectsDuplicateNameOnCreate() {
        RuleRequest request = new RuleRequest("보고서 마감", "설명");
        when(ruleRepository.existsByName("보고서 마감")).thenReturn(true);

        assertThatThrownBy(() -> ruleService.create(request))
                .isInstanceOf(DuplicateRuleNameException.class)
                .hasMessage("Rule name already exists: 보고서 마감");
        verify(ruleRepository, never()).saveAndFlush(any(Rule.class));
    }

    @Test
    void rejectsDuplicateNameOnUpdate() {
        Rule existing = rule(1L, "보고서 마감", "설명");
        RuleRequest request = new RuleRequest("회의록 공유", "새 설명");
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ruleRepository.existsByNameAndIdNot("회의록 공유", 1L)).thenReturn(true);

        assertThatThrownBy(() -> ruleService.update(1L, request))
                .isInstanceOf(DuplicateRuleNameException.class)
                .hasMessage("Rule name already exists: 회의록 공유");
        verify(ruleRepository, never()).saveAndFlush(any(Rule.class));
    }

    @Test
    void allowsUpdateKeepingCurrentName() {
        Rule existing = rule(1L, "보고서 마감", "기존 설명");
        RuleRequest request = new RuleRequest("보고서 마감", "새 설명");
        when(ruleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(ruleRepository.existsByNameAndIdNot("보고서 마감", 1L)).thenReturn(false);
        when(ruleRepository.saveAndFlush(existing)).thenReturn(existing);

        RuleResponse response = ruleService.update(1L, request);

        assertThat(response).isEqualTo(new RuleResponse(1L, "보고서 마감", "새 설명"));
    }

    @Test
    void convertsDatabaseUniqueViolationToDuplicateNameException() {
        RuleRequest request = new RuleRequest("보고서 마감", "설명");
        when(ruleRepository.existsByName("보고서 마감")).thenReturn(false);
        when(ruleRepository.saveAndFlush(any(Rule.class)))
                .thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> ruleService.create(request))
                .isInstanceOf(DuplicateRuleNameException.class)
                .hasMessage("Rule name already exists: 보고서 마감")
                .hasCauseInstanceOf(DataIntegrityViolationException.class);
    }

    private Rule rule(Long id, String name, String description) {
        Rule rule = new Rule(name, description);
        ReflectionTestUtils.setField(rule, "id", id);
        return rule;
    }
}
