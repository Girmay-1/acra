package com.acra.service;


import com.acra.exception.RuleSetNotFoundException;
import com.acra.model.RuleSet;
import com.acra.repository.RuleSetRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RuleSetService {
    private final RuleSetRepository ruleSetRepository;

    @Autowired
    public RuleSetService(RuleSetRepository ruleSetRepository) {
        this.ruleSetRepository = ruleSetRepository;
    }

    public RuleSet createRuleSet(RuleSet ruleSet) {
        if (ruleSet.getId() != null) {
            throw new IllegalArgumentException("New rule set should not have an ID");
        }
        return ruleSetRepository.save(ruleSet);
    }

    public RuleSet updateRuleSet(RuleSet ruleSet) {
        if (ruleSet.getId() == null) {
            throw new IllegalArgumentException("Cannot update a rule set without an ID");
        }
        return ruleSetRepository.save(ruleSet);
    }

    public RuleSet getRuleSet(Long id) {
        return ruleSetRepository.findById(id)
                .orElseThrow(() -> new RuleSetNotFoundException("Rule set not found with id: " + id));
    }

    public List<RuleSet> getAllRuleSets() {
        return ruleSetRepository.findAll();
    }

    public void deleteRuleSet(Long id) {
        if (!ruleSetRepository.existsById(id)) {
            throw new RuleSetNotFoundException("Rule set not found with id: " + id);
        }
        ruleSetRepository.deleteById(id);
    }
}