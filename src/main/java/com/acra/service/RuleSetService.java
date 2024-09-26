package com.acra.service;


import com.acra.model.RuleSet;
import com.acra.repository.RuleSetRepository;
import org.springframework.stereotype.Service;

@Service
public class RuleSetService {

    private final RuleSetRepository ruleSetRepository;

    public RuleSetService(RuleSetRepository ruleSetRepository) {
        this.ruleSetRepository = ruleSetRepository;
    }

    public RuleSet updateRuleSet(RuleSet ruleSet) {
        // This is a placeholder implementation
        return ruleSetRepository.save(ruleSet);
    }
}