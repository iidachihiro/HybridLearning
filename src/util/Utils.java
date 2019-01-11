package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import core.ActionSet;
import core.Condition;
import core.Rule;
import model.gd.GDModelUpdator;
import model.hybrid.HybridModelUpdator;
import model.sgd.SGDModelUpdator;

public class Utils {
    private static String originalPath;
    private static String resourcesPath;
    private static String baseRulesPath;
    private static String tracesPath;
    private static String trueProbabilityPath;
    private static String baseActionsPath;
    protected static String outputPath;
    protected static String configPath;
    private static String experiment1FilePath;
    private static String errorEX3FilePath;
    private static String errorEX4FilePath;
    private static String errorEX3EX4FilePath;
    
    protected static String traceFileName = "Traces.txt";
    protected static String trueProbabilityFileName;
    private static String baseRulesFileName = "BaseRules.txt";
    private static String baseActionsFileName = "BaseActions.txt";
    
    private static String experiment1FileName = "experiment1.csv";
    
    private static String errorEX3FileName = "ErrorValues_ex3.csv";
    private static String errorEX4FileName = "ErrorValues_ex4.csv";
    private static String errorEX3EX4FileName = "ErrorValues.csv";
    
    protected final static String tab = "  ";
    
    public Utils() {
        originalPath = System.getProperty("user.dir");
        originalPath += "/";
        resourcesPath = originalPath+"resources/";
        outputPath = originalPath+"output/";
        configPath = resourcesPath+"learning.config";
        
        setConfig();
        tracesPath = resourcesPath+traceFileName;
        trueProbabilityPath = resourcesPath+trueProbabilityFileName;
        baseRulesPath = resourcesPath+baseRulesFileName;
        baseActionsPath = resourcesPath+baseActionsFileName;
        
        experiment1FileName = "experiment1_SGD("+SGDUtils.getLearningRate()+")_GD("+GDUtils.getLearningRate()+").csv";
        experiment1FilePath = outputPath+experiment1FileName;
        
        errorEX3FilePath = outputPath+errorEX3FileName;
        errorEX4FilePath = outputPath+errorEX4FileName;
        errorEX3EX4FilePath = outputPath+errorEX3EX4FileName;
    }    
    
    private static void setConfig() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(configPath)));
            String line;
            while ((line = br.readLine()) != null) {
                line = removeSpace(line);
                if (line.startsWith("TraceFileName")) {
                    traceFileName = line.substring("TraceFileName=".length());
                } else if (line.startsWith("BaseRulesFileName")) {
                    baseRulesFileName = line.substring("BaseRulesFileName=".length());
                } else if (line.startsWith("BaseActionsFileName")) {
                    baseActionsFileName = line.substring("BaseACtionsFileName=".length());
                } else if (line.startsWith("TrueProbabilityFileName=")) {
                    trueProbabilityFileName = line.substring("TrueProbabilityFileName=".length());
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static String getBasePath() {
        return originalPath;
    }
    
    public static String getResourcesPath() {
        return resourcesPath;
    }
    
    public static String getBaseRulesPath() {
        return baseRulesPath;
    }
    
    public static String getOutputPath() {
        return outputPath;
    }
    
    public static String getConfigPath() {
        return configPath;
    }
    
    public static String getTraceFileName() {
        return traceFileName;
    }
    
    public static String removeSpace(String line) {
        line = line.replaceAll(" ", "");
        line = line.replaceAll("\t", "");
        return line;
    }
    
    public static List<Rule> readBaseRules() {
        List<Rule> rules = new ArrayList<>();
        try {
            File file = new File(baseRulesPath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.substring(0, 2).equals("//")) {
                    continue;
                }
                ActionSet as = new ActionSet(line.split(",", 0));
                boolean isNewRule = true;
                for (Rule rule : rules) {
                    if (rule.isSameKind(as)) {
                        rule.addNewPostCondition(new Condition(as.getPostMonitorableAction()));
                        isNewRule = false;
                        break;
                    }
                }
                if (isNewRule) {
                    rules.add(new Rule(as));
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        System.out.println("Read base rules file.");
        return rules;
    }
    
    public static void printRules(List<Rule> rules) {
        int i = 0;
        for (Rule rule : rules) {
            System.out.println("Rule "+i);
            System.out.println(tab+"preCondition: "+rule.getPreConditionName());
            System.out.println(tab+"action: "+rule.getActionName());
            System.out.println(tab+"postConditions: ");
            for (Condition cond : rule.getPostConditions()) {
                System.out.println(tab+tab+cond.getName()+tab+cond.getValue());
            }
            i++;
        }
    }
    
    public static List<ActionSet> readTraces() {
        List<ActionSet> sets = new ArrayList<>();
        try {
            File file = new File(tracesPath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            String pre = br.readLine();
            String act = null;
            boolean actFlag = true;
            while ((line = br.readLine()) != null) {
                if (actFlag) {
                    act = line;
                    actFlag = false;
                } else {
                    sets.add(new ActionSet(pre, act, line));
                    pre = line;
                    actFlag = true;
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        System.out.println("Read traces file.");
        return sets;
    }
    
    public static void printActionSets() {
        List<ActionSet> sets = readTraces();
        for (ActionSet set : sets) {
            System.out.println(set.getPreMonitorableAction()+","
                    +set.getControllableAction()+","+set.getPostMonitorableAction());
        }
    }
    
    public static List<Rule> readBaseActions() {
        List<Rule> rules = new ArrayList<>();
        List<String> monitorable = new ArrayList<>();
        List<String> controllable = new ArrayList<>();
        try {
            File file = new File(baseActionsPath);
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.equals("#Monitorable")) {
                    while ((line = br.readLine()).length() > 0) {
                        String[] strs = line.split(",", 0);
                        monitorable.addAll(Arrays.asList(strs));
                    }
                } else if (line.equals("#Controllable")) {
                    while ((line = br.readLine()) != null && line.length() > 0) {
                        String[] strs = line.split(",", 0);
                        controllable.addAll(Arrays.asList(strs));
                    }
                }
            }
            br.close();
            for (String pre : monitorable) {
                for (String act : controllable) {
                    Rule rule = new Rule(pre, act);
                    for (String post : monitorable) {
                        rule.addNewPostCondition(new Condition(post));
                    }
                    rules.add(rule);
                }
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }
        System.out.println("Read base actions file.");
        return rules;
    }
    
    //file1 is SGD's, file2 is GD's
    public static void mergeFiles_ValuesOfRules(File file1, File file2, List<Rule> rules, int GD_LEARNING_SIZE, int TRACE_SIZE) {
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(file1));
            BufferedReader br2 = new BufferedReader(new FileReader(file2));
            File outputFile = new File(experiment1FilePath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(outputFile)));
            int rulesNum = 0;
            pw.print("ActionSet,");
            for (Rule rule : rules) {
                for (Condition post : rule.getPostConditions()) {
                    pw.print(rule.getPreConditionName()+" "+rule.getActionName()+" "+post.getName()+",,,");
                    rulesNum++;
                }
            }
            pw.println();
            for (int i = 0; i < rulesNum; i++) {
                pw.print(",SGD,GD,difference");
            }
            pw.println();
            // title is ignored.
            br1.readLine(); 
            br2.readLine();
            
            String[] strs1 = br1.readLine().split(",");
            String[] strs2 = br2.readLine().split(",");
            pw.print("0");
            for (int i = 1; i < strs1.length; i++) {
                pw.print(","+strs1[i]+","+strs2[i]+","+String.valueOf((Double.valueOf(strs1[i])-Double.valueOf(strs2[i]))));
            }
            pw.println();
            for (int i = 1; i < GD_LEARNING_SIZE; i++) {
                strs1 = br1.readLine().split(",");
                pw.print(i);
                for (int j = 1; j < strs1.length; j++) {
                    pw.print(","+strs1[j]+",,");
                }
                pw.println();
            }
            for (int i = GD_LEARNING_SIZE; i <= TRACE_SIZE; i++) {
                strs1 = br1.readLine().split(",");
                strs2 = br2.readLine().split(",");
                pw.print(i);
                for (int j = 1; j < strs1.length; j++) {
                    pw.print(","+strs1[j]+","+strs2[j]+","+String.valueOf((Double.valueOf(strs1[j])-Double.valueOf(strs2[j]))));
                }
                pw.println();
            }
            br1.close();
            br2.close();
            pw.close();
            System.out.println("Merged "+file1.getName()+" and "+file2.getName()+" into "+outputFile.getName()+".");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    //file1 is SGD's, file2 is GD's, file3 is true probability file.
    public static void outputErrorValues_EX3(File file1, File file2, int GD_LEARNING_SIZE, int TRACE_SIZE) {
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(file1));
            BufferedReader br2 = new BufferedReader(new FileReader(file2));
            BufferedReader br3 = new BufferedReader(new FileReader(new File(trueProbabilityPath)));
            // read file3
            String line = br3.readLine();
            String[] strs = line.split(",");
            int num = Integer.valueOf(strs[0]);
            int[] points = new int[strs.length-1];
            for (int i = 1; i < strs.length; i++) {
                points[i-1] = Integer.valueOf(strs[i]);
            }
            double[][] probabilities = new double[num][points.length];
            for (int i = 0; i < num; i++) {
                line = br3.readLine();
                strs = line.split(",");
                for (int j = 0; j < points.length; j++) {
                    probabilities[i][j] = Double.valueOf(strs[j+1]);
                }
            }
            br3.close();
            // read file1
            double[] aveErrors1 = new double[TRACE_SIZE];
            line = br1.readLine();
            int point_index = 1;
            for (int i = 0; i < TRACE_SIZE; i++) {
                if (point_index < points.length && i == points[point_index]) {
                    point_index++;
                }
                line = br1.readLine();
                strs = line.split(",");
                double aveError = 0;
                int count = 0;
                for (int j = 0; j < num; j++) {
                    double value = Double.valueOf(strs[j+1]);
                    if (value != 0.5) {
                        aveError += Math.abs(value-probabilities[j][point_index-1]);
                        count++;
                    }
                }
                if (count == 0) {
                    aveErrors1[i] = 0;
                } else {
                    aveErrors1[i] = aveError/count;
                }
            }
            br1.close();
            // read file2
            double[] aveErrors2 = new double[TRACE_SIZE];
            line = br2.readLine();
            point_index = 1;
            for (int i = GD_LEARNING_SIZE; i < TRACE_SIZE; i++) {
                if (point_index < points.length && i == points[point_index]) {
                    point_index++;
                }
                line = br2.readLine();
                strs = line.split(",");
                double aveError = 0;
                int count = 0;
                for (int j = 0; j < num; j++) {
                    double value = Double.valueOf(strs[j+1]);
                    if (value != 0.5) {
                        aveError += Math.abs(value-probabilities[j][point_index-1]);
                        count++;
                    }
                }
                if (count == 0) {
                    aveErrors2[i] = 0;
                } else {
                    aveErrors2[i] = aveError/count;
                }
            }
            br2.close();
            // write errorsFile
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(errorEX3FilePath))));
            pw.println("ActionSet,SGD,GD");
            for (int i = 0; i < GD_LEARNING_SIZE; i++) {
                pw.println(i+","+aveErrors1[i]+",");
            }
            for (int i = GD_LEARNING_SIZE; i < TRACE_SIZE; i++) {
                pw.println(i+","+aveErrors1[i]+","+aveErrors2[i]);
            }
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void outputErrorValues_EX4(HybridModelUpdator HUpdator) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(trueProbabilityPath)));
            // read true probability file
            String line = br.readLine();
            String[] strs = line.split(",");
            int num = Integer.valueOf(strs[0]);
            int[] points = new int[strs.length-1];
            for (int i = 1; i < strs.length; i++) {
                points[i-1] = Integer.valueOf(strs[i]);
            }
            double[][] true_probabilities = new double[num][points.length];
            for (int i = 0; i < num; i++) {
                line = br.readLine();
                strs = line.split(",");
                for (int j = 0; j < points.length; j++) {
                    true_probabilities[i][j] = Double.valueOf(strs[j+1]);
                }
            }
            br.close();
            // write
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(errorEX4FilePath))));
            pw.println("ActionSet,Hybrid");
            List<List<Double>> probabilities = HUpdator.getProbabilities();
            int point_index = 1;
            for (int i = 0; i < probabilities.size(); i++) {
                if (point_index < points.length && points[point_index] == i) {
                    point_index++;
                }
                double aveError = 0;
                int count = 0;
                for (int j = 0; j < num; j++) {
                    double value = probabilities.get(i).get(j);
                    if (value != 0.5) {
                        aveError += Math.abs(true_probabilities[j][point_index-1]-value);
                        count++;
                    }
                    /*
                    aveError += Math.abs(true_probabilities[j][point_index]-value);
                    count++;
                    */
                }
                if (count != 0) {
                    aveError /= count;
                }
                pw.println(i+","+aveError);
            }
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void outputErrorValues_EX4_2(double[] rates, HybridModelUpdator[] HUpdators) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(trueProbabilityPath)));
            // read true probability file
            String line = br.readLine();
            String[] strs = line.split(",");
            int num = Integer.valueOf(strs[0]);
            int[] points = new int[strs.length-1];
            for (int i = 1; i < strs.length; i++) {
                points[i-1] = Integer.valueOf(strs[i]);
            }
            double[][] true_probabilities = new double[num][points.length];
            for (int i = 0; i < num; i++) {
                line = br.readLine();
                strs = line.split(",");
                for (int j = 0; j < points.length; j++) {
                    true_probabilities[i][j] = Double.valueOf(strs[j+1]);
                }
            }
            br.close();
            // write
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(errorEX4FilePath))));
            pw.print("ActionSet");
            for (double rate : rates) {
                pw.print(",Hybrid("+rate+")");
            }
            pw.println();
            List<List<List<Double>>> probabilities_all = new ArrayList<>();
            for (HybridModelUpdator HUpdator : HUpdators) {
                List<List<Double>> probabilities = HUpdator.getProbabilities();
                probabilities_all.add(probabilities);
            }
            int point_index = 1;
            for (int i = 0; i < probabilities_all.get(0).size(); i++) {
                if (point_index < points.length && points[point_index] == i) {
                    point_index++;
                }
                pw.print(i);
                for (int index = 0; index < HUpdators.length; index++) {
                    double aveError = 0;
                    int count = 0;
                    for (int j = 0; j < num; j++) {
                        double value = probabilities_all.get(index).get(i).get(j);
                        if (value != 0.5) {
                            aveError += Math.abs(true_probabilities[j][point_index-1]-value);
                            count++;
                        }
                    }
                    if (count != 0) {
                        aveError /= count;
                    }
                    pw.print(","+aveError);
                }
                pw.println();
            }
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void mergeFile_EX4() {
        try {
            BufferedReader br1 = new BufferedReader(new FileReader(new File(errorEX3FilePath)));
            BufferedReader br2 = new BufferedReader(new FileReader(new File(errorEX4FilePath)));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(errorEX3EX4FilePath)));
            String line1, line2;
            while(((line1 = br1.readLine()) != null) && ((line2 = br2.readLine()) != null)) {
                String[] strs1 = line1.split(",");
                String[] strs2 = line2.split(",");
                if (strs1.length == 2) {
                    pw.print(strs1[0]+","+strs1[1]+",");
                } else if (strs1.length == 3) {
                    pw.print(strs1[0]+","+strs1[1]+","+strs1[2]);
                }
                for (int i = 1; i < strs2.length; i++) {
                    pw.print(","+strs2[i]);
                }
                pw.println();
            }
            br1.close();
            br2.close();
            pw.close();
            System.out.println((new File(errorEX3EX4FilePath)).getName()+" is merged.");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void setConfigFileToDefault() {
        String tmpFileName = "tmp.config";
        double DEFAULT_LEARNING_RATE = 0.1;
        double DEFAULT_THRESHOLD = 0.3;
        File beforeConfig = new File(configPath);
        File afterConfig = new File(resourcesPath+tmpFileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(beforeConfig));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(afterConfig)));
            String line;
            while((line = br.readLine()) != null) {
                if (line.startsWith("Learning Rate = ")) {
                    pw.println("Learning Rate = "+DEFAULT_LEARNING_RATE);
                } else if (line.startsWith("Threshold = ")) {
                    pw.println("Threshold = "+DEFAULT_THRESHOLD);
                } else {
                    pw.println(line);
                }
            }
            br.close();
            pw.close();
            if (!beforeConfig.delete()) {
                throw new IOException(beforeConfig.getAbsolutePath()+" can't be deleted.");
            } else {
                System.out.println(beforeConfig.getAbsolutePath()+" was deleted.");
            }
            if (!afterConfig.renameTo(beforeConfig)) {
                throw new IOException(afterConfig.getAbsolutePath()+" can't be renamed to "+beforeConfig.getAbsolutePath());
            } else {
                System.out.println(afterConfig.getAbsolutePath()+" was renamed to "+beforeConfig.getAbsolutePath());
            }
            System.out.println("setConfigFileToDefault() was successfully completed.");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void setLearningRateToConfig(double rate) {
        String tmpFileName = "tmp.config";
        File beforeConfig = new File(configPath);
        File afterConfig = new File(resourcesPath+tmpFileName);
        try {
            BufferedReader br = new BufferedReader(new FileReader(beforeConfig));
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(afterConfig)));
            String line;
            while((line = br.readLine()) != null) {
                if (line.startsWith("Learning Rate = ")) {
                    pw.println("Learning Rate = "+rate);
                } else {
                    pw.println(line);
                }
            }
            br.close();
            pw.close();
            if (!beforeConfig.delete()) {
                throw new IOException(beforeConfig.getAbsolutePath()+" can't be deleted.");
            } else {
                System.out.println(beforeConfig.getAbsolutePath()+" was deleted.");
            }
            if (!afterConfig.renameTo(beforeConfig)) {
                throw new IOException(afterConfig.getAbsolutePath()+" can't be renamed to "+beforeConfig.getAbsolutePath());
            } else {
                System.out.println(afterConfig.getAbsolutePath()+" was renamed to "+beforeConfig.getAbsolutePath());
            }
            System.out.println("setLearningRateToConfig() was successfully completed.");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static List<Rule> copyRules(List<Rule> rules) {
        List<Rule> result = new ArrayList<>();
        for (Rule rule : rules) {
            Rule newRule = rule.clone();
            result.add(newRule);
        }
        return result;
    }
    
    public static void outputErrorValues_EX4_3(HybridModelUpdator HUpdator, SGDModelUpdator SGDUpdator, GDModelUpdator GDUpdator) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(trueProbabilityPath)));
            // read true probability file
            String line = br.readLine();
            String[] strs = line.split(",");
            int num = Integer.valueOf(strs[0]);
            int[] points = new int[strs.length-1];
            for (int i = 1; i < strs.length; i++) {
                points[i-1] = Integer.valueOf(strs[i]);
            }
            double[][] true_probabilities = new double[num][points.length];
            for (int i = 0; i < num; i++) {
                line = br.readLine();
                strs = line.split(",");
                for (int j = 0; j < points.length; j++) {
                    true_probabilities[i][j] = Double.valueOf(strs[j+1]);
                }
            }
            br.close();
            // write
            File ex4_3Dir = new File(outputPath+"ex4-3/");
            if (!ex4_3Dir.exists()) {
                ex4_3Dir.mkdirs();
            }
            
            int GD_LEARNING_SIZE = GDUtils.getLearningSize();
            List<List<Double>> HProbabilities = HUpdator.getProbabilities();
            List<List<Double>> SGDProbabilities = SGDUpdator.getProbabilities();
            List<List<Double>> GDProbabilities = GDUpdator.getProbabilities();
            if (HProbabilities.size() != SGDProbabilities.size() || HProbabilities.size() != GDProbabilities.size()+GD_LEARNING_SIZE) {
                System.out.println("size is different!!!!!!");
            }
            int index = 0;
            int size = HProbabilities.size();
            for (Rule rule : HUpdator.getRules()) {
                String preCondName = rule.getPreConditionName();
                String actName = rule.getActionName();
                for (Condition post : rule.getPostConditions()) {
                    String postCondName = post.getName();
                    String path = ex4_3Dir.getAbsolutePath()+"/"+preCondName+"_"+actName+"_"+postCondName+".csv";
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(path))));
                    pw.println("ActionSet,SGD,GD,Hybrid");
                    int point_index = 1;
                    for (int i = 1; i < size; i++) {
                        if (point_index < points.length && points[point_index] == i) {
                            point_index++;
                        }
                        double HError = Math.abs(HProbabilities.get(i).get(index)-true_probabilities[index][point_index-1]);
                        double SGDError = Math.abs(SGDProbabilities.get(i).get(index)-true_probabilities[index][point_index-1]);
                        if (i < GD_LEARNING_SIZE) {
                            pw.println(i+","+SGDError+",,"+HError);
                        } else {
                            double GDError = Math.abs(GDProbabilities.get(i-GD_LEARNING_SIZE).get(index)-true_probabilities[index][point_index-1]);
                            pw.println(i+","+SGDError+","+GDError+","+HError);
                        }
                    }
                    pw.close();
                    index++;
                }
            }
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void outputErrorValues_EX5(HybridModelUpdator HUpdator, SGDModelUpdator SGDUpdator, GDModelUpdator GDUpdator) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(trueProbabilityPath)));
            // read true probability file
            String line = br.readLine();
            String[] strs = line.split(",");
            int num = Integer.valueOf(strs[0]);
            int[] points = new int[strs.length-1];
            for (int i = 1; i < strs.length; i++) {
                points[i-1] = Integer.valueOf(strs[i]);
            }
            double[][] true_probabilities = new double[num][points.length];
            for (int i = 0; i < num; i++) {
                line = br.readLine();
                strs = line.split(",");
                for (int j = 0; j < points.length; j++) {
                    true_probabilities[i][j] = Double.valueOf(strs[j+1]);
                }
            }
            br.close();
            // write
            File ex5Dir = new File(outputPath+"ex5/");
            if (!ex5Dir.exists()) {
                ex5Dir.mkdirs();
            }
            File errorDir = new File(ex5Dir.getAbsolutePath()+"/error/");
            if (!errorDir.exists()) {
                errorDir.mkdirs();
            }
            File valueDir = new File(ex5Dir.getAbsolutePath()+"/value/");
            if (!valueDir.exists()) {
                valueDir.mkdirs();
            }
            
            int GD_LEARNING_SIZE = GDUtils.getLearningSize();
            List<List<Double>> HProbabilities = HUpdator.getProbabilities();
            List<List<Double>> SGDProbabilities = SGDUpdator.getProbabilities();
            List<List<Double>> GDProbabilities = GDUpdator.getProbabilities();
            if (HProbabilities.size() != SGDProbabilities.size() || HProbabilities.size() != GDProbabilities.size()+GD_LEARNING_SIZE) {
                System.out.println("size is different!!!!!!");
            }
            int index = 0;
            int size = HProbabilities.size();
            for (Rule rule : HUpdator.getRules()) {
                String preCondName = rule.getPreConditionName();
                String actName = rule.getActionName();
                for (Condition post : rule.getPostConditions()) {
                    String postCondName = post.getName();
                    String path = errorDir.getAbsolutePath()+"/"+preCondName+"_"+actName+"_"+postCondName+".csv";
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(path))));
                    pw.println("ActionSet,SGD,GD,Hybrid");
                    int point_index = 1;
                    for (int i = 1; i < size; i++) {
                        if (point_index < points.length && points[point_index] == i) {
                            point_index++;
                        }
                        double HError = Math.abs(HProbabilities.get(i).get(index)-true_probabilities[index][point_index-1]);
                        double SGDError = Math.abs(SGDProbabilities.get(i).get(index)-true_probabilities[index][point_index-1]);
                        if (i < GD_LEARNING_SIZE) {
                            pw.println(i+","+SGDError+",,"+HError);
                        } else {
                            double GDError = Math.abs(GDProbabilities.get(i-GD_LEARNING_SIZE).get(index)-true_probabilities[index][point_index-1]);
                            pw.println(i+","+SGDError+","+GDError+","+HError);
                        }
                    }
                    pw.close();
                    index++;
                }
            }
            index = 0;
            for (Rule rule : HUpdator.getRules()) {
                String preCondName = rule.getPreConditionName();
                String actName = rule.getActionName();
                for (Condition post : rule.getPostConditions()) {
                    String postCondName = post.getName();
                    String path = valueDir.getAbsolutePath()+"/"+preCondName+"_"+actName+"_"+postCondName+".csv";
                    PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(path))));
                    pw.println("ActionSet,SGD,GD,Hybrid");
                    int point_index = 1;
                    for (int i = 1; i < size; i++) {
                        if (point_index < points.length && points[point_index] == i) {
                            point_index++;
                        }
                        double HValue = HProbabilities.get(i).get(index);
                        double SGDValue = SGDProbabilities.get(i).get(index);
                        if (i < GD_LEARNING_SIZE) {
                            pw.println(i+","+SGDValue+",,"+HValue);
                        } else {
                            double GDValue = GDProbabilities.get(i-GD_LEARNING_SIZE).get(index);
                            pw.println(i+","+SGDValue+","+GDValue+","+HValue);
                        }
                    }
                    pw.close();
                    index++;
                }
            }
            
            String valuesForEX5Path = outputPath+ex5Dir.getName()+"/valuesEX5.csv";
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(new File(valuesForEX5Path))));
            pw.print("ActionSet");
            for (Rule rule : HUpdator.getRules()) {
                String preCondName = rule.getPreConditionName();
                String actName = rule.getActionName();
                for (Condition post : rule.getPostConditions()) {
                    String postCondName = post.getName();
                    String name = preCondName+"_"+actName+"_"+postCondName;
                    pw.print(","+name);
                }
            }
            pw.println();
            int i = 0;
            for (List<Double> tmp : HUpdator.getTmpProbabilities()) {
                pw.print(i++);
                for (double t : tmp) {
                    pw.print(","+t);
                }
                pw.println();
            }
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static double calculateVariance(double... values) {
        double average = 0;
        double squareAverage = 0;
        for (double value : values) {
            average += value;
            squareAverage += value*value;
        }
        average /= values.length;
        squareAverage /= values.length;
        return squareAverage - average*average;
    }
    
    public static List<Double> getValuesOfPostConditions(List<Rule> rules) {
        List<Double> values = new ArrayList<>();
        for (Rule rule : rules) {
            for (Condition post : rule.getPostConditions()) {
                values.add(post.getValue());
            }
        }
        return values;
    }
    
    public static void outputResultE5(int SGD_count, int GD_count, int H_count, double SGD_time, double GD_time, double H_time) {
        try {
            String filePath = outputPath+"ex5/EX5Result.txt";
            File file = new File(filePath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.println("EX5 Result");
            pw.println("**************************************************");
            pw.println("configuration");
            pw.println("SGD Learning Rate = "+SGDUtils.getLearningRate());
            pw.println("SGD Threshold = "+SGDUtils.getThreshold());
            pw.println("GD Learning Rate = "+GDUtils.getLearningRate());
            pw.println("GD Threshold = "+GDUtils.getThreshold());
            pw.println("**************************************************");
            pw.println("SGD");
            pw.println("updated count = "+SGD_count);
            pw.println("time = "+nanoToSec(SGD_time)+" sec");
            pw.println("**************************************************");
            pw.println("GD");
            pw.println("updated count = "+GD_count);
            pw.println("time = "+nanoToSec(GD_time)+" sec");
            pw.println("**************************************************");
            pw.println("Hybrid");
            pw.println("updated count = "+H_count);
            pw.println("time = "+nanoToSec(H_time)+" sec");
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static double nanoToSec(double time) {
        return time/Math.pow(10, 9);
    }
}
