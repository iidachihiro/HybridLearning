package model.sgd;

import core.Condition;
import core.Rule;
import util.SGDUtils;

public class StochasticGradientDescent {
    static double total;
    static double pb;
    static double LEARNING_RATE;
    
    public static Rule getUpdatedRule(Rule rule, String observedPostCondition) {
        LEARNING_RATE = SGDUtils.getLearningRate();
        total = rule.getTotalOfPostConditionValues();
        pb = rule.getProbabilityOfTargetPostCondition(observedPostCondition);
        for (Condition post : rule.getPostConditions()) {
            double value = post.getValue();
            post.setPreValue(value); // for identifying model updating.
            if (post.getName().equals(observedPostCondition)) {
                post.setValue(value-LEARNING_RATE*calculateInternalGradient(post));
            } else {
                post.setValue(value-LEARNING_RATE*calculateExternalGradient(post));
            }
        }
        return rule.normalize();
    }
    
    private static double calculateInternalGradient(Condition post) {
        return -2*(1-pb)*(total-post.getValue())/(Math.pow(total, 2));
    }
    
    private static double calculateExternalGradient(Condition post) {
        return 2*(1-pb)*(post.getValue())/(Math.pow(total, 2));
    }
}
