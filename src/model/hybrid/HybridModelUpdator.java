package model.hybrid;

import java.util.ArrayList;
import java.util.List;

import core.ActionSet;
import core.Condition;
import core.Rule;
import model.gd.GradientDescent;
import model.sgd.StochasticGradientDescent;
import util.GDUtils;

public class HybridModelUpdator {
    private List<Rule> rules;
    private List<ActionSet> traces;
    
    private List<List<Double>> probabilities;
    
    private int LEARNING_SIZE;
    
    public HybridModelUpdator(List<Rule> _rules) {
        this.rules = _rules;
        new GDUtils();
        LEARNING_SIZE = GDUtils.getLearningSize();
    }
    
    public List<List<Double>> getProbabilities() {
        return this.probabilities;
    }
    
    // for experiment4
    public void learn1(List<ActionSet> sets, int[] detectedPoints) {
        this.traces = sets;
        int point_index = 0;
        int shift_point = -1;
        probabilities = new ArrayList<>();
        probabilities.add(getValuesOfPostConditions());
        for (int i = 0; i < traces.size(); i++) {
            ActionSet as = traces.get(i);
            int index = getIndexOfTargetRule(as);
            Rule targetRule = rules.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            probabilities.add(getValuesOfPostConditions());
            if (point_index < detectedPoints.length && detectedPoints[point_index] == i) {
                // environmental change is detected
                shift_point = detectedPoints[point_index]+LEARNING_SIZE;
                point_index++;
            }
            if (shift_point == i) {
                // GD learning
                List<ActionSet> targetTraces = getLearningTargetTraces(i);
                rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                System.out.println("GD is executed!!!     "+i);
                probabilities.add(getValuesOfPostConditions());
                i++;
                
            }
        }
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
    
    public List<ActionSet> getLearningTargetTraces(int i) {
        List<ActionSet> res = new ArrayList<>();
        for (int j = i-LEARNING_SIZE+1; j <= i; j++) {
            res.add(traces.get(j));
        }
        return res;
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
