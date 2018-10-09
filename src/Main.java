import java.util.List;

import core.ActionSet;
import core.Rule;
import model.gd.GDModelUpdator;
import model.sgd.SGDModelUpdator;
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
        }
    }
}
