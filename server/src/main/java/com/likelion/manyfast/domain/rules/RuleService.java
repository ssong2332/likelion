package com.likelion.manyfast.domain.rules;

import com.likelion.manyfast.domain.rules.dto.RuleRequest;
import com.likelion.manyfast.domain.rules.dto.RuleResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RuleService {

    private static final Sort ID_ASC = Sort.by(Sort.Direction.ASC, "id");

    private final RuleRepository ruleRepository;

    @Transactional
    public RuleResponse create(RuleRequest request) {
        if (ruleRepository.existsByName(request.name())) {
            throw new DuplicateRuleNameException(request.name());
        }

        Rule rule = new Rule(request.name(), request.description());
        return save(rule);
    }

    public List<RuleResponse> findAll() {
        return ruleRepository.findAll(ID_ASC).stream()
                .map(RuleResponse::from)
                .toList();
    }

    @Transactional
    public RuleResponse update(Long id, RuleRequest request) {
        Rule rule = findById(id);
        if (ruleRepository.existsByNameAndIdNot(request.name(), id)) {
            throw new DuplicateRuleNameException(request.name());
        }

        rule.update(request.name(), request.description());
        return save(rule);
    }

    @Transactional
    public void delete(Long id) {
        Rule rule = findById(id);
        ruleRepository.delete(rule);
    }

    private Rule findById(Long id) {
        return ruleRepository.findById(id)
                .orElseThrow(() -> new RuleNotFoundException(id));
    }

    private RuleResponse save(Rule rule) {
        try {
            return RuleResponse.from(ruleRepository.saveAndFlush(rule));
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateRuleNameException(rule.getName(), exception);
        }
    }
}
