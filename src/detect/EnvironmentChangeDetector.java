package detect;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

public class EnvironmentChangeDetector {
    private List<ActionSet> traces;
    private List<Rule> rules_SGD;
    private List<Rule> rules_GD;
    
    private double SGD_THRESHOLD = 0.1;
    private double GD_THRESHOLD = 0.1;
    private double THRESHOLD = 0.1;
    private double DIFF_THRESHOLD = 1000;
    
    private int LEARNING_SIZE = 1500;
    
    private int DIFF_SIZE = 10;
    
    private String filePath;
    
    public EnvironmentChangeDetector(List<Rule> rules) {
        this.rules_SGD = new ArrayList<>();
        rules_SGD = copyRules(rules);
        this.rules_GD = new ArrayList<>();
        rules_GD = copyRules(rules);
        new SGDUtils();
        SGDUtils.reflesh();
        SGD_THRESHOLD = SGDUtils.getThreshold();
        new GDUtils();
        GDUtils.reflesh();
        GD_THRESHOLD = GDUtils.getThreshold();
        THRESHOLD = (SGD_THRESHOLD+GD_THRESHOLD)/2;
    }
    
    public void detect(List<ActionSet> sets, String mode) {
        this.traces = sets;
        int count = 1;
        for (ActionSet as : traces) {
            // SGD
            int index = getIndexOfTargetRule(rules_SGD, as);
            Rule targetRule = rules_SGD.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            if (isNecessaryOfUpdatingEnvironmentModel(targetRule, SGD_THRESHOLD)) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules_SGD, SGD_THRESHOLD, count, "SGD");
            }
            rules_SGD.set(index, targetRule);
            count++;
            if (count >= LEARNING_SIZE && count < traces.size()) {
                // GD
                List<ActionSet> targetTraces = getLearningTargetTraces(count);
                this.rules_GD = GradientDescent.getUpdatedRules(rules_GD, targetTraces);
                if (isNecessaryOfUpdatingEnvironmentModel(rules_GD, GD_THRESHOLD)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules_GD, GD_THRESHOLD, count, "GD");
                }
                // detect
                int index_ = 0;

                for (Rule rule : rules_SGD) {
                    double pre_diff = 0; // for mode2
                    double[] pre_diffs = new double[DIFF_SIZE]; // for mode3, mode4
                    double[] recent_diffs = new double[DIFF_SIZE]; // for mode4
                    List<String> lines = new ArrayList<>();
                    for (Condition post : rule.getPostConditions()) {
                        double value_SGD = post.getValue();
                        double value_GD = getValueOfNthPostCondition(rules_GD, index_++);
                        double diff = value_SGD - value_GD;
                        if (mode.equals("mode1")) {
                            if (Math.abs(diff) >= THRESHOLD) {
                                printEnvironmentalChanges(count, rule, post, value_SGD, value_GD);
                            }
                        } else if (mode.equals("mode2")) {
                            if (pre_diff != 0) {
                                double ratio = diff/pre_diff;
                                if (ratio >= DIFF_THRESHOLD) {
                                    printEnvironmentalChanges(count, rule, post, value_SGD, value_GD);
                                }
                            } else {
                                if (diff >= SGDUtils.getLearningRate()) {
                                    printEnvironmentalChanges(count, rule, post, value_SGD, value_GD);
                                }
                            }
                            pre_diff = diff;
                        } else if (mode.equals("mode3")) {
                            double ratio_average = calculateRatioAvarage(pre_diffs);
                            if (ratio_average != 0) {
                                double new_ratio = Math.abs(diff - pre_diffs[pre_diffs.length-1]);
                                if (new_ratio/ratio_average >= 20) {
                                    printEnvironmentalChanges(count, rule, post, value_SGD, value_GD);
                                }
                            }
                            pre_diffs = replaceArray(pre_diffs, diff);
                        } else if (mode.equals("mode4")) {
                            // variance of difference
                            double pre_diffs_variance = calculateVariance(pre_diffs);
                            double recent_diffs_variance = calculateVariance(recent_diffs);
                            /*
                            try {
                                filePath = "/Users/iidachihiro/workspace/HybridLearning/output/result_mode4.csv";
                                File file = new File(filePath);
                                PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
                                pw.println(count+", "+pre_diffs_variance+", "+recent_diffs_variance);
                                pw.close();
                            } catch (IOException e) {
                                System.err.println(e.toString());
                            }
                            */
                            if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                                    && post.getName().equals("arrive.m")) {
                                System.out.println(count+", "+pre_diffs_variance+", "+recent_diffs_variance);
                                lines.add(count+", "+pre_diffs_variance+", "+recent_diffs_variance);
                            }
                            pre_diffs = replaceArray(pre_diffs, recent_diffs[0]);
                            recent_diffs = replaceArray(recent_diffs, diff);
                        }
                    }
                }
            }
        }
    }
    
    private int getIndexOfTargetRule(List<Rule> rules, ActionSet as) {
        for (int i = 0; i < rules.size(); i++) {
            Rule rule = rules.get(i);
            if (rule.getPreConditionName().equals(as.getPreMonitorableAction()) 
                    && rule.getActionName().equals(as.getControllableAction())) {
                return i;
            }
        }
        return -1;
    }
    
    private boolean isNecessaryOfUpdatingEnvironmentModel(Rule rule, double THRESHOLD) {
        for (Condition post : rule.getPostConditions()) {
            double value = post.getValue();
            double preValue = post.getPreValue();
            if ((value-THRESHOLD)*(preValue-THRESHOLD) < 0) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isNecessaryOfUpdatingEnvironmentModel(List<Rule> rules, double THRESHOLD) {
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
    
    private List<ActionSet> getLearningTargetTraces(int i) {
        List<ActionSet> res = new ArrayList<>();
        for (int j = i-LEARNING_SIZE+1; j <= i; j++) {
            res.add(traces.get(j));
        }
        return res;
    }
    
    private double getValueOfNthPostCondition(List<Rule> rules, int N) {
        for (Rule rule : rules) {
            if (N >= rule.getPostConditions().size()) {
                N -= rule.getPostConditions().size();
                continue;
            } else {
                return rule.getPostCondition(N).getValue();
            }
        }
        return -1;
    }
    
    private List<Rule> copyRules(List<Rule> rules) {
        List<Rule> result = new ArrayList<>();
        for (Rule rule : rules) {
            Rule newRule = rule.clone();
            result.add(newRule);
        }
        return result;
    }
    
    private double calculateAverage(double[] array) {
        double ave = 0;
        for (double a : array) {
            ave += a;
        }
        return ave/array.length;
    }
    
    private double[] replaceArray(double[] array, double value) {
        for (int i = 0; i < array.length - 1; i++) {
            array[i] = array[i+1];
        }
        array[array.length-1] = value;
        return array;
    }
    
    private void printEnvironmentalChanges(int count, Rule rule, Condition post, double value_SGD, double value_GD) {
        System.out.println("Environment changes!! "+count);
        System.out.println(rule.getPreConditionName()+" "+rule.getActionName()+" "+post.getName()
        +"  SGD:"+value_SGD+"  GD:"+value_GD);
    }
    
    private double calculateRatioAvarage(double[] array) {
        double ratioSum = 0;
        int len = array.length;
        for (int i = 0; i < len-1; i++) {
            double ratio = array[i+1]-array[i];
            ratioSum += Math.abs(ratio);
        }
        if (len == 1) {
            return ratioSum;
        } else {
            return ratioSum/(len-1);
        }
    }
    
    private double calculateVariance(double[] array) {
        double average = 0;
        double square_average = 0;
        int len = array.length;
        for (int i = 0; i < len; i++) {
            average += array[i];
            square_average += Math.pow(array[i],  2);
        }
        average /= len;
        square_average /= len;
        return square_average - Math.pow(average,  2);
    }
}
