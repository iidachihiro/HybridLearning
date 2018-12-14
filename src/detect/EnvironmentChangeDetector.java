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
import util.Utils;

public class EnvironmentChangeDetector {
    private List<ActionSet> traces;
    private List<Rule> rules_SGD;
    private List<Rule> rules_GD;
    
    private double SGD_THRESHOLD = 0.1;
    private double GD_THRESHOLD = 0.1;
    private double THRESHOLD = 0.1;
    private double DIFF_THRESHOLD = 1000;
    
    private int LEARNING_SIZE;
    
    private int DIFF_SIZE = 10;
    
    public EnvironmentChangeDetector(List<Rule> rules) {
        this.rules_SGD = new ArrayList<>();
        rules_SGD = Utils.copyRules(rules);
        this.rules_GD = new ArrayList<>();
        rules_GD = Utils.copyRules(rules);
        new SGDUtils();
        SGDUtils.reflesh();
        SGD_THRESHOLD = SGDUtils.getThreshold();
        new GDUtils();
        GDUtils.reflesh();
        GD_THRESHOLD = GDUtils.getThreshold();
        THRESHOLD = (SGD_THRESHOLD+GD_THRESHOLD)/2;
        LEARNING_SIZE = GDUtils.getLearningSize();
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
    
    public void detect4(List<ActionSet> sets) {
        this.traces = sets;
        int count = 1;
        //double[] pre_diffs = new double[DIFF_SIZE]; 
        //double[] recent_diffs = new double[DIFF_SIZE]; 
        List<double[]> recent_diffs_list = new ArrayList<>();
        int len = 0;
        for (Rule rule : rules_SGD) {
            len += rule.getPostConditions().size();
        }
        for (int i = 0; i < len; i++) {
            recent_diffs_list.add(new double[DIFF_SIZE*2]);
        }
        List<String> lines = new ArrayList<>();
        
        for (ActionSet as : traces) {
            // SGD
            int index = getIndexOfTargetRule(rules_SGD, as);
            Rule targetRule = rules_SGD.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            /*
            if (isNecessaryOfUpdatingEnvironmentModel(targetRule, SGD_THRESHOLD)) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules_SGD, SGD_THRESHOLD, count, "SGD");
            }
            */
            rules_SGD.set(index, targetRule);
            count++;
            if (count >= LEARNING_SIZE && count < traces.size()) {
                // GD
                List<ActionSet> targetTraces = getLearningTargetTraces(count);
                this.rules_GD = GradientDescent.getUpdatedRules(rules_GD, targetTraces);
                /*
                if (isNecessaryOfUpdatingEnvironmentModel(rules_GD, GD_THRESHOLD)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules_GD, GD_THRESHOLD, count, "GD");
                }
                */
                // detect
                int index_ = 0;

                for (Rule rule : rules_SGD) {
                    for (Condition post : rule.getPostConditions()) {
                        double value_SGD = post.getValue();
                        double value_GD = getValueOfNthPostCondition(rules_GD, index_);
                        double diff = value_SGD-value_GD;
                        recent_diffs_list.set(index_, replaceArray(recent_diffs_list.get(index_), diff));
                        double variance1 = calculateVariance(recent_diffs_list.get(index_), 0, 10);
                        double variance2 = calculateVariance(recent_diffs_list.get(index_), 10, 20);
                        if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                                && post.getName().equals("arrive.e")) {
                            //System.out.println(count+", "+variance1+", "+variance2);
                            lines.add(count+", "+variance1+", "+variance2);
                        }
                        if (calculateRatio(variance1, variance2) >= Math.pow(10,  4)) {
                            printEnvironmentalChanges(count-10, rule, post, value_SGD, value_GD);
                        }
                        index_++;
                    }
                }
            }
        }
        makeTMPFile(lines, "/Users/iidachihiro/workspace/HybridLearning/output/tmp_mode4.txt");
    }
    
    public void detect5(List<ActionSet> sets) {
        this.traces = sets;
        int count = 1;
        //double[] pre_diffs = new double[DIFF_SIZE]; 
        //double[] recent_diffs = new double[DIFF_SIZE]; 
        List<double[]> recent_sgds_list = new ArrayList<>();
        int len = 0;
        for (Rule rule : rules_SGD) {
            len += rule.getPostConditions().size();
        }
        for (int i = 0; i < len; i++) {
            recent_sgds_list.add(new double[DIFF_SIZE*2]);
        }
        List<String> lines = new ArrayList<>();
        
        for (ActionSet as : traces) {
            // SGD
            int index = getIndexOfTargetRule(rules_SGD, as);
            Rule targetRule = rules_SGD.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            /*
            if (isNecessaryOfUpdatingEnvironmentModel(targetRule, SGD_THRESHOLD)) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules_SGD, SGD_THRESHOLD, count, "SGD");
            }
            */
            rules_SGD.set(index, targetRule);
            count++;
            if (count >= LEARNING_SIZE && count < traces.size()) {
                // GD
                List<ActionSet> targetTraces = getLearningTargetTraces(count);
                this.rules_GD = GradientDescent.getUpdatedRules(rules_GD, targetTraces);
                /*
                if (isNecessaryOfUpdatingEnvironmentModel(rules_GD, GD_THRESHOLD)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules_GD, GD_THRESHOLD, count, "GD");
                }
                */
                // detect
                int index_ = 0;

                for (Rule rule : rules_SGD) {
                    for (Condition post : rule.getPostConditions()) {
                        double value_SGD = post.getValue();
                        double value_GD = getValueOfNthPostCondition(rules_GD, index_);
                        recent_sgds_list.set(index_, replaceArray(recent_sgds_list.get(index_), value_SGD));
                        double variance1 = calculateVariance(recent_sgds_list.get(index_), 0, 10);
                        double variance2 = calculateVariance(recent_sgds_list.get(index_), 10, 20);
                        if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                                && post.getName().equals("arrive.e")) {
                            //System.out.println(count+", "+variance1+", "+variance2);
                            lines.add(count+", "+variance1+", "+variance2);
                        }
                        if (calculateRatio(variance1, variance2) >= Math.pow(10,  4)) {
                            printEnvironmentalChanges(count-10, rule, post, value_SGD, value_GD);
                        }
                        index_++;
                    }
                }
            }
        }
        makeTMPFile(lines, "/Users/iidachihiro/workspace/HybridLearning/output/tmp_mode5.txt");
    }
    
    public void detect6(List<ActionSet> sets) {
        this.traces = sets;
        int count = 1;
        //double[] pre_diffs = new double[DIFF_SIZE]; 
        //double[] recent_diffs = new double[DIFF_SIZE]; 
        List<double[]> recent_diffs_list = new ArrayList<>();
        int len = 0;
        for (Rule rule : rules_SGD) {
            len += rule.getPostConditions().size();
        }
        for (int i = 0; i < len; i++) {
            recent_diffs_list.add(new double[DIFF_SIZE*2]);
        }
        List<String> lines = new ArrayList<>();
        
        for (ActionSet as : traces) {
            // SGD
            int index = getIndexOfTargetRule(rules_SGD, as);
            Rule targetRule = rules_SGD.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            /*
            if (isNecessaryOfUpdatingEnvironmentModel(targetRule, SGD_THRESHOLD)) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules_SGD, SGD_THRESHOLD, count, "SGD");
            }
            */
            rules_SGD.set(index, targetRule);
            count++;
            if (count >= LEARNING_SIZE && count < traces.size()) {
                // GD
                List<ActionSet> targetTraces = getLearningTargetTraces(count);
                this.rules_GD = GradientDescent.getUpdatedRules(rules_GD, targetTraces);
                /*
                if (isNecessaryOfUpdatingEnvironmentModel(rules_GD, GD_THRESHOLD)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules_GD, GD_THRESHOLD, count, "GD");
                }
                */
                // detect
                int index_ = 0;

                for (Rule rule : rules_SGD) {
                    for (Condition post : rule.getPostConditions()) {
                        double value_SGD = post.getValue();
                        double value_GD = getValueOfNthPostCondition(rules_GD, index_);
                        double diff = value_SGD-value_GD;
                        recent_diffs_list.set(index_, replaceArray(recent_diffs_list.get(index_), diff));
                        double variation1 = calculateVariationSum(recent_diffs_list.get(index_), 0, 10);
                        double variation2 = calculateVariationSum(recent_diffs_list.get(index_), 10, 20);
                        if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                                && post.getName().equals("arrive.e")) {
                            //System.out.println(count+", "+variance1+", "+variance2);
                            lines.add(count+", "+variation1+", "+variation2);
                        }
                        if (calculateRatio(variation1, variation2) >= 100) {
                            printEnvironmentalChanges(count-10, rule, post, value_SGD, value_GD);
                        }
                        index_++;
                    }
                }
            }
        }
        makeTMPFile(lines, "/Users/iidachihiro/workspace/HybridLearning/output/tmp_mode6.txt");
    }
    
    public void detect7(List<ActionSet> sets) {
        this.traces = sets;
        int count = 1;
        //double[] pre_diffs = new double[DIFF_SIZE]; 
        //double[] recent_diffs = new double[DIFF_SIZE]; 
        List<double[]> recent_sgds_list = new ArrayList<>();
        int len = 0;
        for (Rule rule : rules_SGD) {
            len += rule.getPostConditions().size();
        }
        for (int i = 0; i < len; i++) {
            recent_sgds_list.add(new double[DIFF_SIZE*2]);
        }
        List<String> lines = new ArrayList<>();
        
        for (ActionSet as : traces) {
            // SGD
            int index = getIndexOfTargetRule(rules_SGD, as);
            Rule targetRule = rules_SGD.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            /*
            if (isNecessaryOfUpdatingEnvironmentModel(targetRule, SGD_THRESHOLD)) {
                DomainModelGenerator generator = new DomainModelGenerator();
                generator.generate(rules_SGD, SGD_THRESHOLD, count, "SGD");
            }
            */
            rules_SGD.set(index, targetRule);
            count++;
            if (count >= LEARNING_SIZE && count < traces.size()) {
                // GD
                List<ActionSet> targetTraces = getLearningTargetTraces(count);
                this.rules_GD = GradientDescent.getUpdatedRules(rules_GD, targetTraces);
                /*
                if (isNecessaryOfUpdatingEnvironmentModel(rules_GD, GD_THRESHOLD)) {
                    DomainModelGenerator generator = new DomainModelGenerator();
                    generator.generate(rules_GD, GD_THRESHOLD, count, "GD");
                }
                */
                // detect
                int index_ = 0;

                for (Rule rule : rules_SGD) {
                    for (Condition post : rule.getPostConditions()) {
                        double value_SGD = post.getValue();
                        double value_GD = getValueOfNthPostCondition(rules_GD, index_);
                        recent_sgds_list.set(index_, replaceArray(recent_sgds_list.get(index_), value_SGD));
                        double variation1 = calculateVariationSum(recent_sgds_list.get(index_), 0, 10);
                        double variation2 = calculateVariationSum(recent_sgds_list.get(index_), 10, 20);
                        if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                                && post.getName().equals("arrive.e")) {
                            //System.out.println(count+", "+variance1+", "+variance2);
                            lines.add(count+", "+variation1+", "+variation2);
                        }
                        if (calculateRatio(variation1, variation2) >= 100) {
                            printEnvironmentalChanges(count-10, rule, post, value_SGD, value_GD);
                        }
                        index_++;
                    }
                }
            }
        }
        makeTMPFile(lines, "/Users/iidachihiro/workspace/HybridLearning/output/tmp_mode7.txt");
    }
    
    public void detect8(List<ActionSet> sets) {
        int WINDOW = 1000;
        this.traces = sets;
        int count = 1;
        List<double[]> recent_sgds_list = new ArrayList<>();
        int len = 0;
        for (Rule rule : rules_SGD) {
            len += rule.getPostConditions().size();
        }
        for (int i = 0; i < len; i++) {
            recent_sgds_list.add(new double[WINDOW*2]);
        }
        List<String> lines = new ArrayList<>();
        List<Double> differences = new ArrayList<>();
        
        for (ActionSet as : traces) {
            int index = getIndexOfTargetRule(rules_SGD, as);
            Rule targetRule = rules_SGD.get(index);
            targetRule = StochasticGradientDescent.getUpdatedRule(targetRule, as.getPostMonitorableAction());
            rules_SGD.set(index, targetRule);
            count++;
            int index_ = 0;
            for (Rule rule : rules_SGD) {
                for (Condition post : rule.getPostConditions()) {
                    double value_SGD = post.getValue();
                    //double value_GD = getValueOfNthPostCondition(rules_GD, index_);
                    recent_sgds_list.set(index_, replaceArray(recent_sgds_list.get(index_), value_SGD));
                    double averageFirstHarf = calculateAverage(recent_sgds_list.get(index_), 0, WINDOW);
                    double averageSecondHarf = calculateAverage(recent_sgds_list.get(index_), WINDOW, WINDOW*2);
                    if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                            && post.getName().equals("arrive.e")) {
                        lines.add(count+", "+averageFirstHarf+", "+averageSecondHarf);
                        if (count <= WINDOW*2) {
                            differences.add(0.0);
                        } else {
                            differences.add(averageFirstHarf-averageSecondHarf);
                        }
                    }
                    index_++;
                 }
            }
            /*
            if (count >= LEARNING_SIZE && count < traces.size()) {
                List<ActionSet> targetTraces = getLearningTargetTraces(count);
                this.rules_GD = GradientDescent.getUpdatedRules(rules_GD, targetTraces);
                int index_ = 0;
                for (Rule rule : rules_SGD) {
                    for (Condition post : rule.getPostConditions()) {
                        double value_SGD = post.getValue();
                        //double value_GD = getValueOfNthPostCondition(rules_GD, index_);
                        recent_sgds_list.set(index_, replaceArray(recent_sgds_list.get(index_), value_SGD));
                        double averageFirstHarf = calculateAverage(recent_sgds_list.get(index_), 0, 100);
                        double averageSecondHarf = calculateAverage(recent_sgds_list.get(index_), 100, 200);
                        if (rule.getPreConditionName().equals("arrive.m") && rule.getActionName().equals("move.e")
                                && post.getName().equals("arrive.e")) {
                            lines.add(count+", "+averageFirstHarf+", "+averageSecondHarf);
                            differences.add(averageFirstHarf-averageSecondHarf);
                        }
                        index_++;
                     }
                }
            }
            */
        }
        makeTMPFile(lines, "/Users/iidachihiro/workspace/HybridLearning/output/tmp_mode8.txt");
        makeTMPDifferenceFile_mode8(differences, "/Users/iidachihiro/workspace/HybridLearning/output/diff_mode8.csv");
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
    
    private double calculateVariance(double[] array_, int start, int end) {
        double[] array = new double[end-start];
        for (int i = start; i < end; i++) {
            array[i-start] = array_[i]; 
        }
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
    
    private double calculateVariationSum(double[] array_, int start, int end) {    
        double[] array = new double[end-start];
        for (int i = start; i < end; i++) {
            array[i-start] = array_[i]; 
        }
        double result = 0;
        for (int i = 0; i < array.length-1; i++) {
            result += Math.abs(array[i+1]-array[i]);
        }
        return result;
    }
    
    private void makeTMPFile(List<String> lines, String path) {
        File file = new File(path);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            for (String line : lines) {
                pw.println(line);
            }
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        System.out.println("Created "+file.getName()+"!!");
    }
    
    private double calculateRatio(double a, double b) {
        if (a == 0 || b == 0) { 
        return 0;
        }
        if (a >= b) return a/b;
        else return b/a;
    } 
    
    private double calculateAverage(double[] array, int start, int end) {
        double result = 0;
        for (int i = start; i < end && i < array.length; i++) {
            result += array[i];
        }
        return result/(end-start);
    }
    
    private void makeTMPDifferenceFile_mode8(List<Double> list, String path) {
        File file = new File(path);
        try {
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println("ActionSet,Difference");
            for (int i = 0; i < list.size(); i++) {
                pw.println(i+","+list.get(i));
            }
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        System.out.println("Created "+file.getName()+"!!");
    }
}
