package model.hybrid;

import java.util.ArrayList;
import java.util.List;

import core.ActionSet;
import core.Condition;
import core.Rule;
import model.gd.GradientDescent;
import model.generator.DomainModelGenerator;
import model.sgd.StochasticGradientDescent;
import util.GDUtils;
import util.SGDUtils;
import util.Utils;

public class HybridModelUpdator {
    private List<Rule> rules;
    private List<ActionSet> traces;
    
    private List<List<Double>> probabilities;
    private List<List<Double>> tmp_probabilities;
    
    private List<Double> prob_large;
    
    private int LEARNING_SIZE;
    
    private double SGD_THRESHOLD;
    private double GD_THRESHOLD;
    private int domainModelUpdatedCount = 0;
    
    private int windowSize; //for detecting Environmental Change
    
    public int getDomainModelUpdatedCount() {
        return this.domainModelUpdatedCount;
    }
    
    public HybridModelUpdator(List<Rule> _rules) {
        this.rules = _rules;
        new GDUtils();
        new SGDUtils();
        LEARNING_SIZE = GDUtils.getLearningSize();
        SGD_THRESHOLD = SGDUtils.getThreshold();
        GD_THRESHOLD = GDUtils.getThreshold();
        windowSize = LEARNING_SIZE / 10;
    }
    
    public List<List<Double>> getProbabilities() {
        return this.probabilities;
    }
    
    public List<List<Double>> getTmpProbabilities() {
        return this.tmp_probabilities;
    }
    
    public List<Double> getProbLarge() {
        return prob_large;
    }
    
    public List<Rule> getRules() {
        return this.rules;
    }
    
    // for experiment4
    public void learn1(List<ActionSet> sets, int[] detectedPoints) {
        this.traces = sets;
        int point_index = 0;
        int shift_point = -1;
        probabilities = new ArrayList<>();
        probabilities.add(Utils.getValuesOfPostConditions(rules));
        for (int i = 0; i < traces.size(); i++) {
            ActionSet as = traces.get(i);
            int index = getIndexOfTargetRule(as);
            Rule targetRule = rules.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            probabilities.add(Utils.getValuesOfPostConditions(rules));
            if (point_index < detectedPoints.length && detectedPoints[point_index] == i) {
                // environmental change is detected
                shift_point = detectedPoints[point_index]+LEARNING_SIZE;
                point_index++;
            }
            /*
            if (shift_point == i) {
                i++;
                // GD learning
                List<ActionSet> targetTraces = getLearningTargetTraces(i);
                rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                System.out.println("GD is executed!!!     "+i);
                probabilities.add(getValuesOfPostConditions());
            }
            */
            if (shift_point == i) {
                // GD learning
                int NUMBER_OF_GD_LEARNING = 100;
                for (int j = 0; j < NUMBER_OF_GD_LEARNING; j++) {
                    i++;
                    List<ActionSet> targetTraces = getLearningTargetTraces(i);
                    rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                    System.out.println("GD is executed!!!     "+i);
                    probabilities.add(Utils.getValuesOfPostConditions(rules));
                }
            }
        }
    }
    
    public void learn2(List<ActionSet> sets) {
        List<Rule> tmp_rules = Utils.copyRules(rules); // for detecting environment change
        this.traces = sets;
        probabilities = new ArrayList<>();
        probabilities.add(Utils.getValuesOfPostConditions(rules));
        tmp_probabilities = new ArrayList<>();
        tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
        for (int i = 0; i < sets.size(); i++) {
            ActionSet as = traces.get(i);
            int index  = getIndexOfTargetRule(as);
            Rule targetRule = tmp_rules.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
            if (detectEnvironmentalChange1()) {
                System.out.println(i);
                int changePoint = i;
                while (true) {
                    i++;
                    if (i >= traces.size()) {
                        break;
                    }
                    List<ActionSet> targetTraces = getTargetActionSet(changePoint, i);
                    rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                    probabilities.add(Utils.getValuesOfPostConditions(rules));
                    as = traces.get(i);
                    index = getIndexOfTargetRule(as);
                    targetRule = tmp_rules.get(index);
                    targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
                    tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
                    /*
                    if (convergence(rules)) {
                        break;
                    }
                    */
                    // tentative
                    if (i >= changePoint+1500 && i % 2500 != 0) {
                        break;
                    }
                }
            } else {
                rules = Utils.copyRules(tmp_rules);
            }
            probabilities.add(Utils.getValuesOfPostConditions(rules));
            //tmp_rules = Utils.copyRules(rules);
        }
    }
    
    public void learn3(List<ActionSet> sets) {
        List<Rule> tmp_rules = Utils.copyRules(rules); // for detecting environment change
        this.traces = sets;
        probabilities = new ArrayList<>();
        probabilities.add(Utils.getValuesOfPostConditions(rules));
        tmp_probabilities = new ArrayList<>();
        tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
        for (int i = 0; i < sets.size(); i++) {
            ActionSet as = traces.get(i);
            int index  = getIndexOfTargetRule(as);
            Rule targetRule = tmp_rules.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
            if (detectEnvironmentalChangeFromDetToNonDet()) {
                int changePoint = i;
                while (!detectEnvironmentalChangeFromNonDetToDet()) {
                    i++;
                    if (i >= traces.size()) {
                        break;
                    }
                    List<ActionSet> targetTraces = getTargetActionSet(changePoint, i);
                    rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                    if (isNecessaryOfUpdatingEnvironmentModel_GD()) {
                        DomainModelGenerator generator = new DomainModelGenerator();
                        generator.generate(rules, GD_THRESHOLD, i, "GD");
                        this.domainModelUpdatedCount++;
                    }
                    probabilities.add(Utils.getValuesOfPostConditions(rules));
                    as = traces.get(i);
                    index = getIndexOfTargetRule(as);
                    targetRule = tmp_rules.get(index);
                    targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
                    tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
                    /*
                    if (convergence(rules)) {
                        break;
                    }
                    */
                    // tentative
                    boolean flag = false;
                    if (i >= changePoint+LEARNING_SIZE) {
                        while(!flag) {
                            i++;
                            if (i >= traces.size()) {
                                break;
                            }
                            probabilities.add(Utils.getValuesOfPostConditions(rules));
                            as = traces.get(i);
                            index = getIndexOfTargetRule(as);
                            targetRule = tmp_rules.get(index);
                            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
                            tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
                            if (detectEnvironmentalChangeFromNonDetToDet()) {
                                flag = true;
                            }
                        }
                    }
                    if (flag) {
                        break;
                    }
                }
            } else {
                rules = Utils.copyRules(tmp_rules);
                Rule real_targetRule = getSameRule(targetRule);
                if (isNecessaryOfUpdatingEnvironmentModel_SGD(real_targetRule)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules, SGD_THRESHOLD, i, "SGD");
                    this.domainModelUpdatedCount++;
                }
            }
            probabilities.add(Utils.getValuesOfPostConditions(rules));
            //tmp_rules = Utils.copyRules(rules);
        }
    }
    
    public void learn3large(List<ActionSet> sets) {
        GDUtils.setLearningSize(6000);
        this.LEARNING_SIZE = 6000;
        windowSize = LEARNING_SIZE / 20;
        List<Rule> tmp_rules = Utils.copyRules(rules); // for detecting environment change
        this.traces = sets;
        probabilities = new ArrayList<>();
        probabilities.add(Utils.getValuesOfPostConditions(rules));
        tmp_probabilities = new ArrayList<>();
        tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
        for (int i = 0; i < sets.size(); i++) {
            ActionSet as = traces.get(i);
            int index  = getIndexOfTargetRule(as);
            Rule targetRule = tmp_rules.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
            if (detectEnvironmentalChangeFromDetToNonDet()) {
                int changePoint = i;
                while (!detectEnvironmentalChangeFromNonDetToDet()) {
                    i++;
                    if (i >= traces.size()) {
                        break;
                    }
                    List<ActionSet> targetTraces = getTargetActionSet(changePoint, i);
                    rules = GradientDescent.getUpdatedRules(rules, targetTraces);
                    if (isNecessaryOfUpdatingEnvironmentModel_GD()) {
                        /*
                        DomainModelGenerator generator = new DomainModelGenerator();
                        generator.generate(rules, GD_THRESHOLD, i, "GD");
                        */
                        this.domainModelUpdatedCount++;
                    }
                    probabilities.add(Utils.getValuesOfPostConditions(rules));
                    as = traces.get(i);
                    index = getIndexOfTargetRule(as);
                    targetRule = tmp_rules.get(index);
                    targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
                    tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
                    /*
                    if (convergence(rules)) {
                        break;
                    }
                    */
                    // tentative
                    boolean flag = false;
                    if (i >= changePoint+LEARNING_SIZE) {
                        while(!flag) {
                            i++;
                            if (i >= traces.size()) {
                                break;
                            }
                            probabilities.add(Utils.getValuesOfPostConditions(rules));
                            as = traces.get(i);
                            index = getIndexOfTargetRule(as);
                            targetRule = tmp_rules.get(index);
                            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
                            tmp_probabilities.add(Utils.getValuesOfPostConditions(tmp_rules));
                            if (detectEnvironmentalChangeFromNonDetToDet()) {
                                flag = true;
                            }
                        }
                    }
                    if (flag) {
                        break;
                    }
                }
            } else {
                rules = Utils.copyRules(tmp_rules);
                Rule real_targetRule = getSameRule(targetRule);
                if (isNecessaryOfUpdatingEnvironmentModel_SGD(real_targetRule)) {
                    /*
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules, SGD_THRESHOLD, i, "SGD");
                    */
                    this.domainModelUpdatedCount++;
                }
            }
            probabilities.add(Utils.getValuesOfPostConditions(rules));
            //tmp_rules = Utils.copyRules(rules);
        }
    }
    
    private List<ActionSet> getTargetActionSet(int changePoint, int index) {
        List<ActionSet> target = new ArrayList<>();
        for (int i = changePoint; i < index; i++) {
            target.add(traces.get(i));
        }
        return target;
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
    
    private boolean detectEnvironmentalChange1() {
        int i = tmp_probabilities.size();
        if (i <= 3) {
            return false;
        } else {
            for (int j = 0; j < tmp_probabilities.get(0).size(); j++) {
                double variance = Utils.calculateVariance(tmp_probabilities.get(i-1).get(j), tmp_probabilities.get(i-2).get(j), 
                        tmp_probabilities.get(i-3).get(j), tmp_probabilities.get(i-4).get(j));
                if (variance >= 0.0001) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean detectEnvironmentalChangeFromDetToNonDet() {
        int i = tmp_probabilities.size();
        if (i < windowSize*2) {
            return false;
        } else {
            for (int j = 0; j < tmp_probabilities.get(0).size(); j++) {
                double[] firstHarf = getFirstHarfTMPProbabilities(j);
                double[] secondHarf = getSecondHarfTMPProbabilities(j);
                if (isDeterministicEnvironment(firstHarf) && !isDeterministicEnvironment(secondHarf)) {
                    System.out.println("detect from deterministic to non-deterministic  "+i);
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean detectEnvironmentalChangeFromNonDetToDet() {
        int i = tmp_probabilities.size();
        if (i < windowSize*2) {
            return false;
        } else {
            for (int j = 0; j < tmp_probabilities.get(0).size(); j++) {
                double[] firstHarf = getFirstHarfTMPProbabilities(j);
                double[] secondHarf = getSecondHarfTMPProbabilities(j);
                if (!isDeterministicEnvironment(firstHarf) && isDeterministicEnvironment(secondHarf)) {
                    System.out.println("detect from non-deterministic to deterministic  "+i);
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean isDeterministicEnvironment(double[] values) {
        for (int i = 2; i < values.length; i++) {
            double value1 = values[i-2];
            double value2 = values[i-1];
            double value3 = values[i];
            if ((value1-value2)*(value2-value3) < 0) {
                return false;
            }
        }
        return true;
    }
    
    private double[] getFirstHarfTMPProbabilities(int j) {
        int start = tmp_probabilities.size()-windowSize*2;
        double[] array = new double[windowSize];
        for (int i = start; i < start+windowSize; i++) {
            array[i-start] = tmp_probabilities.get(i).get(j);
        }
        return array;
    }
    
    private double[] getSecondHarfTMPProbabilities(int j) {
        int start = tmp_probabilities.size()-windowSize;
        double[] array = new double[windowSize];
        for (int i = start; i < start+windowSize; i++) {
            array[i-start] = tmp_probabilities.get(i).get(j);
        }
        return array;
    }
    
    private boolean isNecessaryOfUpdatingEnvironmentModel_GD() {
        for (Rule rule : rules) {
            for (Condition post : rule.getPostConditions()) {
                double value = post.getValue();
                double preValue = post.getPreValue();
                if ((value-GD_THRESHOLD)*(preValue-GD_THRESHOLD) < 0) {
                    return true;
                }
            }
        }
        return false;
    } 
    
    private boolean isNecessaryOfUpdatingEnvironmentModel_SGD(Rule rule) {
        for (Condition post : rule.getPostConditions()) {
            double value = post.getValue();
            double preValue = post.getPreValue();
            if (preValue == 0.5 || value == 0.5) {
                continue;
            }
            if ((value-SGD_THRESHOLD)*(preValue-SGD_THRESHOLD) < 0) {
                System.out.println(rule.getPreConditionName()+" "+rule.getActionName()+" "+post.getName());
                System.out.println(post.getPreValue()+" "+post.getValue());
                return true;
            }
        }
        return false;
    }
    
    private Rule getSameRule(Rule rule) {
        for (Rule real_rule : rules) {
            if (real_rule.getPreConditionName().equals(rule.getPreConditionName()) && 
                    real_rule.getActionName().equals(rule.getActionName())) {
                return real_rule;
            }
        }
        return null;
    }
}
