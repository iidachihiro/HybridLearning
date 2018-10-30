package model.sgd;

import java.util.List;

import core.ActionSet;
import core.Condition;
import core.Rule;
import model.generator.DomainModelGenerator;
import util.SGDUtils;

public class SGDModelUpdator {
    private List<Rule> rules;
    private double THRESHOLD;
    
    private int domainModelUpdatedCount = 0;
    
    public SGDModelUpdator(List<Rule> _rules) {
        rules = _rules;
        new SGDUtils();
        SGDUtils.reflesh();
        THRESHOLD = SGDUtils.getThreshold();
    }
    
    public void learn(List<ActionSet> traces) {
        int count = 1;
        for (ActionSet as : traces) {
            int index = getIndexOfTargetRule(as);
            Rule targetRule = rules.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            if (isNecessaryOfUpdatingEnvironmentModel(targetRule)) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules, THRESHOLD, count, "SGD");
            }
            rules.set(index, targetRule);
            count++;
        }
        SGDUtils.outputResult(rules, THRESHOLD);
    }
    
    public void learn(List<ActionSet> traces, String exMode) {
        if (exMode.equals("experiment1")) {
            int count = 1;
            SGDUtils.prepareValuesOfRules(rules);
            for (ActionSet as : traces) {
                int index = getIndexOfTargetRule(as);
                Rule targetRule = rules.get(index);
                targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
                if (isNecessaryOfUpdatingEnvironmentModel(targetRule)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules, THRESHOLD, count, "SGD");
                    this.domainModelUpdatedCount++;
                }
                rules.set(index, targetRule);
                SGDUtils.updateValuesOfRules(rules, count++);
            }
            SGDUtils.outputResult(rules, THRESHOLD);
        }
    }
    
    private boolean isNecessaryOfUpdatingEnvironmentModel(Rule rule) {
        for (Condition post : rule.getPostConditions()) {
            double value = post.getValue();
            double preValue = post.getPreValue();
            if ((value-THRESHOLD)*(preValue-THRESHOLD) < 0) {
                return true;
            }
        }
        return false;
    }
    
    private int getIndexOfTargetRule(ActionSet as) {
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = rules.get(i);
            if (rule.getPreConditionName().equals(as.getPreMonitorableAction()) 
                    && rule.getActionName().equals(as.getControllableAction())) {
                return i;
            }
        }
        return -1;
    }
    
    public void printDomainModelUpdatedCount() {
        System.out.println("The number of updating domain model is "+this.domainModelUpdatedCount+".");
    }
}
