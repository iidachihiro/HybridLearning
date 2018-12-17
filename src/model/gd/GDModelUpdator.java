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
    private int LEARNING_SIZE;
    
    private int domainModelUpdatedCount = 0;
    
    private List<List<Double>> probabilities;
    
    public GDModelUpdator(List<Rule> _rules) {
        this.rules = _rules;
        this.traces = new ArrayList<>();
        new GDUtils();
        GDUtils.reflesh();
        THRESHOLD = GDUtils.getThreshold();
        LEARNING_SIZE = GDUtils.getLearningSize();
    }
    
    public int getLearningSize() {
        return LEARNING_SIZE;
    }
    
    public List<List<Double>> getProbabilities() {
        return probabilities;
    }
    
    public void learn(List<ActionSet> sets) {
        this.traces = sets;
        for (int i = LEARNING_SIZE; i < traces.size(); i++) {
            List<ActionSet> targetTraces = getLearningTargetTraces(i);
            this.rules = GradientDescent.getUpdatedRules(rules, targetTraces);
            if (isNecessaryOfUpdatingEnvironmentModel()) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules, THRESHOLD, i, "GD");
            }
        }
        GDUtils.outputResult(rules, THRESHOLD);
    }
    
    public void learn(List<ActionSet> sets, String exMode) {
        probabilities = new ArrayList<>();
        probabilities.add(getValuesOfPostConditions());
        if (exMode.equals("experiment1")) {
            this.traces.add(null);
            this.traces.addAll(sets);
            GDUtils.prepareValuesOfRules(rules);
            for (int i = LEARNING_SIZE; i < traces.size(); i++) {
                List<ActionSet> targetTraces = getLearningTargetTraces(i);
                this.rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                probabilities.add(getValuesOfPostConditions());
                if (isNecessaryOfUpdatingEnvironmentModel()) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules, THRESHOLD, i, "GD");
                    this.domainModelUpdatedCount++;
                }
                GDUtils.updateValuesOfRules(rules, i);
            }
            GDUtils.outputResult(rules, THRESHOLD);
        }
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
        for (int j = i-LEARNING_SIZE+1; j <= i; j++) {
            res.add(traces.get(j));
        }
        return res;
    }
    
    public void printDomainModelUpdatedCount() {
        System.out.println("The number of updating domain model is "+this.domainModelUpdatedCount+".");
    }
    
    public List<Double> getValuesOfPostConditions() {
        List<Double> values = new ArrayList<>();
        for (Rule rule : rules) {
            for (Condition post : rule.getPostConditions()) {
                values.add(post.getValue());
            }
        }
        return values;
    }
}
