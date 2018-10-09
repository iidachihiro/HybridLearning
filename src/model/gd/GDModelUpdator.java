package model.gd;

import java.util.ArrayList;
import java.util.List;

import core.ActionSet;
import core.Condition;
import core.Rule;
import model.generator.DomainModelGenerator;
import util.GDUtils;

public class GDModelUpdator {
    private List<Rule> rules;
    private List<ActionSet> traces;
    private double THRESHOLD;
    private int LEARNING_SIZE = 1500;
    
    public GDModelUpdator(List<Rule> _rules) {
        this.rules = _rules;
        this.traces = new ArrayList<>();
        new GDUtils();
        GDUtils.reflesh();
        THRESHOLD = GDUtils.getThreshold();
    }
    
    public void learn(List<ActionSet> sets) {
        this.traces = sets;
        int count = LEARNING_SIZE;
        for (int i = LEARNING_SIZE; i < traces.size(); i++) {
            List<ActionSet> targetTraces = getLearningTargetTraces(i);
            this.rules = GradientDescent.getUpdatedRules(rules, targetTraces);
            if (isNecessaryOfUpdatingEnvironmentModel()) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules, THRESHOLD, count, "GD");
            }
            count++;
        }
        GDUtils.outputResult(rules, THRESHOLD);
    }
    
    private boolean isNecessaryOfUpdatingEnvironmentModel() {
        for (Rule rule : rules) {
            for (Condition post : rule.getPostConditions()) {
                double value = post.getValue();
                double preValue = post.getPreValue();
                if ((value-THRESHOLD)*(preValue-THRESHOLD) < 0) {
                    return true;
                }
            }
        }
        return false;
    }
    
    public List<ActionSet> getLearningTargetTraces(int i) {
        List<ActionSet> res = new ArrayList<>();
        for (int j = i-LEARNING_SIZE; j < i; j++) {
            res.add(traces.get(j));
        }
        return res;
    }
}
