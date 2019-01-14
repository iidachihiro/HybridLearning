import java.io.File;
import java.util.List;

import core.ActionSet;
import core.Rule;
import detect.EnvironmentChangeDetector;
import model.gd.GDModelUpdator;
import model.hybrid.HybridModelUpdator;
import model.sgd.SGDModelUpdator;
import util.GDUtils;
import util.SGDUtils;
import util.Utils;

public class Main {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please input arguments.");
        }
        new Utils();
        List<Rule> rules = Utils.readBaseRules();
        List<ActionSet> sets = Utils.readTraces();
        if (args[0].equals("sgd")) {
            doSGD(rules, sets);
        } else if (args[0].equals("gd")) {
            doGD(rules, sets);
        } else if (args[0].equals("experiment1")) {
            doExperiment1(rules, sets);
        } else if (args[0].equals("experiment2")) {
            doExperiment2(rules, sets, args[1]);
        } else if (args[0].equals("experiment3")) {
            doExperiment3(rules, sets);
        } else if (args[0].equals("experiment4")) {
            doExperiment4(rules, sets, args);
        } else if (args[0].equals("experiment4-2")) {
            doExperiment4_2(rules, sets, args);
        } else if (args[0].equals("experiment4-3")) {
            doExperiment4_3(rules, sets, args);
        } else if (args[0].equals("experiment5-1")) {
            doExperiment5_1(rules, sets);
        } else if (args[0].equals("experiment5-2")) {
            doExperiment5_2(rules, sets);
        } else if (args[0].equals("large1")) {
            doExperimentLarge1(rules, sets);
        } else if (args[0].equals("large2")) {
            doExperimentLarge2(rules, sets);
        } else if (args[0].equals("large3")) {
            doExperimentLarge3(rules, sets);
        } else if (args[0].equals("large")) {
            if (args[1].equals("3_3")) {
                doExperimentLarge_3_3(rules, sets);
            }
        }
    }
    
    private static void doSGD(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator updator = new SGDModelUpdator(rules);
        updator.learn(sets);
        System.out.println("sgd learning finished.");
    }
    
    private static void doGD(List<Rule> rules, List<ActionSet> sets) {
        GDModelUpdator updator = new GDModelUpdator(rules);
        updator.learn(sets);
        System.out.println("gd learning finished.");
    }
    
    private static void doExperiment1(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        SGDUpdator.learn(sets, "experiment1");
        SGDUpdator.printDomainModelUpdatedCount();
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        GDUpdator.learn(sets, "experiment1");
        GDUpdator.printDomainModelUpdatedCount();
        Utils.mergeFiles_ValuesOfRules(new File(SGDUtils.getValuesOfRulesFilePath()), new File(GDUtils.getValuesOfRulesFilePath()), 
                rules, GDUpdator.getLearningSize(), sets.size());
        System.out.println("experiment1 finished.");
    }
    
    private static void doExperiment2(List<Rule> rules, List<ActionSet> sets, String mode) {
        EnvironmentChangeDetector detector = new EnvironmentChangeDetector(rules);
        if (mode.equals("mode4")) {
            detector.detect4(sets);
        } else if (mode.equals("mode5")) {
            detector.detect5(sets);
        } else if (mode.equals("mode6")) {
            detector.detect6(sets);
        } else if (mode.equals("mode7")) {
            detector.detect7(sets);
        } else if (mode.equals("mode8")) {
            detector.detect8(sets);
        } else {
            detector.detect(sets, mode);
        }
        System.out.println("experiment2 finished.");
    }
    
    private static void doExperiment3(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        SGDUpdator.learn(sets, "experiment1");
        SGDUpdator.printDomainModelUpdatedCount();
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        GDUpdator.learn(sets, "experiment1");
        GDUpdator.printDomainModelUpdatedCount();
        Utils.outputErrorValues_EX3(new File(SGDUtils.getValuesOfRulesFilePath()), new File(GDUtils.getValuesOfRulesFilePath()), 
                GDUpdator.getLearningSize(), sets.size());
        System.out.println("experiment3 finished.");
    }
    
    private static void doExperiment4(List<Rule> rules, List<ActionSet> sets, String[] args) {
        doExperiment3(rules, sets);
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        int[] detectedPoints = new int[args.length-1];
        for (int i = 0; i < detectedPoints.length; i++) {
            detectedPoints[i] = Integer.valueOf(args[i+1]);
        }
        HUpdator.learn1(sets, detectedPoints);
        Utils.outputErrorValues_EX4(HUpdator);
        Utils.mergeFile_EX4();
        System.out.println("experiment4 finished.");
    }
    
    private static void doExperiment4_2(List<Rule> rules, List<ActionSet> sets, String[] args) {
        doExperiment3(rules, sets);
        rules = Utils.readBaseRules();
        double[] rates = {0.001, 0.005, 0.01, 0.05, 0.1, 0.5};
        HybridModelUpdator[] HUpdators = new HybridModelUpdator[rates.length];
        int[] detectedPoints = new int[args.length-1];
        for (int i = 0; i < detectedPoints.length; i++) {
            detectedPoints[i] = Integer.valueOf(args[i+1]);
        }
        for (int i = 0; i < rates.length; i++) {
            double rate = rates[i];
            Utils.setLearningRateToConfig(rate);
            List<Rule> tmp_rules = Utils.copyRules(rules);
            HybridModelUpdator HUpdator = new HybridModelUpdator(tmp_rules);
            HUpdator.learn1(sets, detectedPoints);
            HUpdators[i] = HUpdator;
        }
        Utils.outputErrorValues_EX4_2(rates, HUpdators);
        Utils.mergeFile_EX4();
        Utils.setConfigFileToDefault();
        System.out.println("experiment4-2 finished.");
    }
    
    private static void doExperiment4_3(List<Rule> rules, List<ActionSet> sets, String[] args) {
        int[] detectedPoints = new int[args.length-1];
        for (int i = 0; i < detectedPoints.length; i++) {
            detectedPoints[i] = Integer.valueOf(args[i+1]);
        }
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        HUpdator.learn1(sets, detectedPoints);
        rules = Utils.readBaseRules();
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        SGDUpdator.learn(sets,  "experiment1");
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        GDUpdator.learn(sets,  "experiment1");
        Utils.outputErrorValues_EX4_3(HUpdator, SGDUpdator, GDUpdator);
        System.out.println("experiment4-3 finished.");
    }
    
    private static void doExperiment5_1(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        SGDUpdator.learn(sets,  "experiment1");
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        GDUpdator.learn(sets,  "experiment1");
        rules = Utils.readBaseRules();
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        HUpdator.learn2(sets);
        Utils.outputErrorValues_EX5(HUpdator, SGDUpdator, GDUpdator);
        System.out.println("experiment5 finished.");
    }
    
    private static void doExperiment5_2(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        long startTime_SGD = System.nanoTime();
        SGDUpdator.learn(sets,  "experiment1");
        long endTime_SGD = System.nanoTime();
        System.out.println("SGD: "+SGDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        
        
        
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        long startTime_GD = System.nanoTime();
        GDUpdator.learn(sets,  "experiment1");
        long endTime_GD = System.nanoTime();
        System.out.println("GD: "+GDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        
        
        
        rules = Utils.readBaseRules();
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        long startTime_H = System.nanoTime();
        HUpdator.learn3(sets);
        long endTime_H = System.nanoTime();
        System.out.println("Hybrid: "+HUpdator.getDomainModelUpdatedCount()+" updated.");
        Utils.outputErrorValues_EX5(HUpdator, SGDUpdator, GDUpdator);
 
        Utils.outputResultEX5(SGDUpdator.getDomainModelUpdatedCount(), GDUpdator.getDomainModelUpdatedCount(), HUpdator.getDomainModelUpdatedCount()
                , endTime_SGD-startTime_SGD, endTime_GD-startTime_GD, endTime_H-startTime_H);
        
        System.out.println("experiment5 finished.");
    }
    
    private static void doExperimentLarge1(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        long startTime_SGD = System.nanoTime();
        SGDUpdator.learn(sets,  "large1");
        long endTime_SGD = System.nanoTime();
        System.out.println("SGD: "+SGDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        long startTime_GD = System.nanoTime();
        GDUpdator.learn(sets,  "large1");
        long endTime_GD = System.nanoTime();
        System.out.println("GD: "+GDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        rules = Utils.readBaseRules();
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        long startTime_H = System.nanoTime();
        HUpdator.learn3large(sets);
        long endTime_H = System.nanoTime();
        System.out.println("Hybrid: "+HUpdator.getDomainModelUpdatedCount()+" updated.");
        
        System.out.println("SGD time: "+(endTime_SGD-startTime_SGD)/Math.pow(10,  9));
        System.out.println("GD time: "+(endTime_GD-startTime_GD)/Math.pow(10,  9));
        System.out.println("Hybrid time: "+(endTime_H-startTime_H)/Math.pow(10,  9));
        
        Utils.outputResultLarge(SGDUpdator.getDomainModelUpdatedCount(), GDUpdator.getDomainModelUpdatedCount(), HUpdator.getDomainModelUpdatedCount()
                , endTime_SGD-startTime_SGD, endTime_GD-startTime_GD, endTime_H-startTime_H);
    }
    
    private static void doExperimentLarge2(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        long startTime_SGD = System.nanoTime();
        SGDUpdator.learn(sets,  "large2");
        long endTime_SGD = System.nanoTime();
        System.out.println("SGD: "+SGDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        long startTime_GD = System.nanoTime();
        GDUpdator.learn(sets,  "large2");
        long endTime_GD = System.nanoTime();
        System.out.println("GD: "+GDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        System.out.println("SGD time: "+(endTime_SGD-startTime_SGD)/Math.pow(10,  9));
        System.out.println("GD time: "+(endTime_GD-startTime_GD)/Math.pow(10,  9));
    }
    
    private static void doExperimentLarge3(List<Rule> rules, List<ActionSet> sets) {
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        long startTime_H = System.nanoTime();
        HUpdator.learn3large(sets);
        long endTime_H = System.nanoTime();
        System.out.println("Hybrid: "+HUpdator.getDomainModelUpdatedCount()+" updated.");
        
        System.out.println("Hybrid time: "+(endTime_H-startTime_H)/Math.pow(10,  9));
    }
    
    private static void doExperimentLarge_3_3(List<Rule> rules, List<ActionSet> sets) {
        SGDModelUpdator SGDUpdator = new SGDModelUpdator(rules);
        long startTime_SGD = System.nanoTime();
        SGDUpdator.learn(sets,  "large1");
        long endTime_SGD = System.nanoTime();
        System.out.println("SGD: "+SGDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        rules = Utils.readBaseRules();
        GDModelUpdator GDUpdator = new GDModelUpdator(rules);
        long startTime_GD = System.nanoTime();
        GDUpdator.learn(sets,  "large1");
        long endTime_GD = System.nanoTime();
        System.out.println("GD: "+GDUpdator.getDomainModelUpdatedCount()+" updated.");
        printPartition(80);
        
        rules = Utils.readBaseRules();
        HybridModelUpdator HUpdator = new HybridModelUpdator(rules);
        long startTime_H = System.nanoTime();
        HUpdator.learn3large(sets);
        long endTime_H = System.nanoTime();
        System.out.println("Hybrid: "+HUpdator.getDomainModelUpdatedCount()+" updated.");
        
        System.out.println("SGD time: "+(endTime_SGD-startTime_SGD)/Math.pow(10,  9));
        System.out.println("GD time: "+(endTime_GD-startTime_GD)/Math.pow(10,  9));
        System.out.println("Hybrid time: "+(endTime_H-startTime_H)/Math.pow(10,  9));
        
        Utils.outputResultLarge(SGDUpdator.getDomainModelUpdatedCount(), GDUpdator.getDomainModelUpdatedCount(), HUpdator.getDomainModelUpdatedCount()
                , endTime_SGD-startTime_SGD, endTime_GD-startTime_GD, endTime_H-startTime_H);
        
        Utils.outputErrorValuesLarge("large_3_3", HUpdator, SGDUpdator, GDUpdator);
    }
    
    private static void printPartition(int n) {
        while (n > 0) {
            System.out.print("*");
            n--;
        }
        System.out.println("");
    }
}
