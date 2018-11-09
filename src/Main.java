import java.io.File;
import java.util.List;

import core.ActionSet;
import core.Rule;
import detect.EnvironmentChangeDetector;
import model.gd.GDModelUpdator;
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
            SGDModelUpdator updator = new SGDModelUpdator(rules);
            updator.learn(sets);
            System.out.println("sgd learning finished.");
        } else if (args[0].equals("gd")) {
            GDModelUpdator updator = new GDModelUpdator(rules);
            updator.learn(sets);
            System.out.println("gd learning finished.");
        } else if (args[0].equals("experiment1")) {
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
        } else if (args[0].equals("experiment2")) {
            String mode = args[1];
            EnvironmentChangeDetector detector = new EnvironmentChangeDetector(rules);
            detector.detect(sets, mode);
        }
    }
}
