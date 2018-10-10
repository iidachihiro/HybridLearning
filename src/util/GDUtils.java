package util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import core.Condition;
import core.Rule;
import model.fsp.FSPSentence;
import model.fsp.Process;

public class GDUtils extends Utils {
    private static String resultPath;
    private static String domainPath;
    private static String probabilityTablePath;
    private static String valuesOfRulesFilePath;

    private static double learningRate = 0.1;
    private static double threshold = 0.1;
    
    public GDUtils() {
        setConfig();
        resultPath = outputPath+"Result_"+makeIdentificationPart()+".txt";
        domainPath = outputPath+"Domain_"+makeIdentificationPart()+".txt";
        probabilityTablePath = outputPath+"ProbabilityTable_"+makeIdentificationPart()+".csv";
        valuesOfRulesFilePath = outputPath+"ValuesOfRules_"+makeIdentificationPart()+".csv";
    }    
    
    private static void setConfig() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(configPath)));
            String line;
            while ((line = br.readLine()) != null) {
                line = removeSpace(line);
                if (line.startsWith("#GD")) {
                    boolean flagLR = false, flagTH = false;
                    while ((line = br.readLine()) != null) {
                        if (flagLR && flagTH) { 
                            break;
                        }
                        line = removeSpace(line);
                        if (line.startsWith("LearningRate")) {
                            learningRate = Double.valueOf(line.substring("LearningRate=".length()));
                            flagLR = true;
                        } else if (line.startsWith("Threshold")) {
                            threshold = Double.valueOf(line.substring("Threshold=".length()));
                            flagTH = true;
                        }
                    }
                }
            }
            br.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static String getOutputPath() {
        return outputPath;
    }
    
    public static String getProbabilityTablePath() {
        return probabilityTablePath;
    }
    
    public static String getConfigPath() {
        return configPath;
    }
    
    public static String getDomainPath() {
        return domainPath;
    }
    
    public static String getResultPath() {
        return resultPath;
    }
    
    public static double getLearningRate() {
        return learningRate;
    }
    
    public static double getThreshold() {
        return threshold;
    }
    
    public static String getValuesOfRulesFilePath() {
        return valuesOfRulesFilePath;
    }
    
    public static void reflesh() {
        String[] paths = {resultPath, domainPath, probabilityTablePath};
        for (String path : paths) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }
    
    public static String removeSpace(String line) {
        line = line.replaceAll(" ", "");
        line = line.replaceAll("\t", "");
        return line;
    }
    
    public static void outputResult(List<Rule> rules, double threshold) {
        try {
            File file = new File(resultPath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            int index = 1;
            
            for (Rule rule : rules) {
                if (rule.isNeverUpdated()) {
                    continue;
                }
                if (rule.existsOnlyNanValue()) {
                    continue;
                }
                pw.println("Rule "+index+":");
                pw.println(tab+"PreCondition: "+rule.getPreConditionName());
                pw.println(tab+"Action: "+rule.getActionName());
                pw.println(tab+"PostConditions: ");
                for (Condition cond : rule.getPostConditions()) {
                    if (cond.getValue() < threshold) {
                        continue;
                    }
                    pw.println(tab+tab+cond.getName()+tab+cond.getValue());
                }
                index++;
            }
            pw.close();
            System.out.println("Generated "+file.getName()+".");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void outputDomainModel(List<FSPSentence> fsps) {
        try {
            File file = new File(domainPath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            for (int i = 0; i < fsps.size(); i++) {
                FSPSentence fsp = fsps.get(i);
                pw.println(fsp.getMap()+"=");
                pw.print("(");
                for (int j = 0; j < fsp.getProcesses().size(); j++) {
                    Process process = fsp.getProcess(j);
                    pw.print(process.getAction()+" -> (");
                    for (int k = 0; k < process.getPosts().size(); k++) {
                        pw.print(process.getPosts().get(k));
                        if (k < process.getPosts().size()-1) {
                            pw.print("|");
                        }
                    }
                    pw.println(")");
                    if (j < fsp.getProcesses().size()-1) {
                        pw.print("|");   
                    }
                }
                if (i < fsps.size()-1) {
                    pw.println("),");
                } else {
                    pw.println(").");
                }
            }
            pw.close();
            System.out.println("Generated "+file.getName()+".");
        } catch (IOException e) {
            System.out.println(e.toString());
        }
    }
    
    public static void prepareProbabilityTable(List<Rule> rules) {
        try {
            File file = new File(probabilityTablePath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.print("ActionSet,");
            for (Rule rule : rules) {
                for (Condition postCond : rule.getPostConditions()) {
                    pw.print(rule.getPreConditionName()+" "+rule.getActionName()+" "+postCond.getName()+",");
                }
            }
            pw.println();
            pw.print("0,");
            for (Rule rule : rules) {
                for (Condition postCond : rule.getPostConditions()) {
                    pw.print(postCond.getValue()+",");
                }
            }
            pw.println();
            pw.close();
            System.out.println("Generated "+file.getName()+".");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void updateProbabilityTable(List<Rule> rules, int index) {
        try {
            File file = new File(probabilityTablePath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            pw.print(index+",");
            for (Rule rule : rules) {
                for (Condition postCond : rule.getPostConditions()) {
                    pw.print(postCond.getValue()+",");
                }
            }
            pw.println();
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void prepareValuesOfRules(List<Rule> rules) {
        try {
            File file = new File(valuesOfRulesFilePath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            pw.print("ActionSet,");
            for (Rule rule : rules) {
                for (Condition postCond : rule.getPostConditions()) {
                    pw.print(rule.getPreConditionName()+" "+rule.getActionName()+" "+postCond.getName()+",");
                }
            }
            pw.println();
            pw.print("0,");
            for (Rule rule : rules) {
                for (Condition postCond : rule.getPostConditions()) {
                    pw.print(postCond.getValue()+",");
                }
            }
            pw.println();
            pw.close();
            System.out.println("Generated "+file.getName()+" file.");
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static void updateValuesOfRules(List<Rule> rules, int index) {
        try {
            File file = new File(valuesOfRulesFilePath);
            PrintWriter pw = new PrintWriter(new BufferedWriter(new FileWriter(file, true)));
            pw.print(index+",");
            for (Rule rule : rules) {
                for (Condition postCond : rule.getPostConditions()) {
                    pw.print(postCond.getValue()+",");
                }
            }
            pw.println();
            pw.close();
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }
    
    public static String makeIdentificationPart() {
        String result = "";
        result += traceFileName.substring(0,  traceFileName.length()-4); // ".txt" is removed.
        result += "_";
        result += learningRate;
        result += "_";
        result += threshold;
        result += "_GD";
        return result;
    }
}
