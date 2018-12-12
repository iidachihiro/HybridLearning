package model.gd;

import java.util.List;

import core.ActionSet;
import core.Condition;
import core.Rule;
import util.GDUtils;

public class GradientDescent {
    static double total;
    static double LEARNING_RATE;
    
    public static List<Rule> getUpdatedRules(List<Rule> rules, List<ActionSet> observedData) {
        LEARNING_RATE = GDUtils.getLearningRate();
        for (Rule rule : rules) {
            total = 1;
            String preCondName = rule.getPreConditionName();
            String actionName = rule.getActionName();
            double[] sums = new double[rule.getPostConditions().size()];
            int count = 0;
            for (ActionSet as : observedData) {
                if (preCondName.equals(as.getPreMonitorableAction()) && actionName.equals(as.getControllableAction())) {
                    count++;
                    for (int i = 0; i < rule.getPostConditions().size(); i++) {
                        Condition post = rule.getPostCondition(i);
                        post.setPreValue(post.getValue()); // for identifying model updating.
                        if (post.getName().equals(as.getPostMonitorableAction())) {
                            sums[i] += calculateInternalGradient(post);
                        } else {
                            sums[i] += calculateExternalGradient(post);
                        }
                    }
                } else {
                    continue;
                }
            }
            if (count > 0) {
                for (int i = 0; i < rule.getPostConditions().size(); i++) {
                    double value = rule.getPostCondition(i).getValue();
                    rule.getPostCondition(i).setValue(value-LEARNING_RATE*sums[i]/count);
                }
                rule = rule.normalize();
            } else {
                for (int i = 0; i < rule.getPostConditions().size(); i++) {
                    rule.getPostCondition(i).setValue(rule.getPostCondition(i).getPreValue());
                }
            }
        }
        return rules;
    }
    

    private static double calculateInternalGradient(Condition post) {
        double pb = post.getValue()/total;
        return -2*(1-pb)*(total-post.getValue())/(Math.pow(total, 2));
    }
    
    private static double calculateExternalGradient(Condition post) {
        double pb = post.getValue()/total;
        return 2*(1-pb)*(post.getValue())/(Math.pow(total, 2));
    }
}
