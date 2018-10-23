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

public class Utils {
    private static String originalPath;
    private static String resourcesPath;
    private static String baseRulesPath;
    private static String tracesPath;
    private static String baseActionsPath;
    protected static String outputPath;
    protected static String configPath;
    private static String experiment1FilePath;
    
    protected static String traceFileName = "Traces.txt";
    private static String baseRulesFileName = "BaseRules.txt";
    private static String baseActionsFileName = "BaseActions.txt";
    
    private static String experiment1FileName = "experiment1.csv";
    
    protected final static String tab = "  ";
    
    public Utils() {
        originalPath = System.getProperty("user.dir");
        originalPath += "/";
        resourcesPath = originalPath+"resources/";
        outputPath = originalPath+"output/";
        configPath = resourcesPath+"learning.config";
        
        setConfig();
        tracesPath = resourcesPath+traceFileName;
        baseRulesPath = resourcesPath+baseRulesFileName;
        baseActionsPath = resourcesPath+baseActionsFileName;
        
        experiment1FileName = "experiment1_SGD("+SGDUtils.getLearningRate()+")_GD("+GDUtils.getLearningRate()+").csv";
        experiment1FilePath = outputPath+experiment1FileName;
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
}
