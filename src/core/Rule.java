package core;

import java.util.ArrayList;
import java.util.List;

public class Rule {
    String preConditionName;
    String actionName;
    List<Condition> postConditions;
    
    public Rule(String _preConditionName, String _actionName, Condition _postCondition) {
        this.preConditionName = _preConditionName;
        this.actionName = _actionName;
        this.postConditions = new ArrayList<>();
        this.postConditions.add(_postCondition);
    }
    
    public Rule(ActionSet as) {
        this.preConditionName = as.getPreMonitorableAction();
        this.actionName = as.getControllableAction();
        this.postConditions = new ArrayList<Condition>();
        this.postConditions.add(new Condition(as.getPostMonitorableAction()));
    }
    
    public Rule(String _preConditionName, String _actionName) {
        this.preConditionName = _preConditionName;
        this.actionName = _actionName;
        this.postConditions = new ArrayList<>();
    }
    
    public String getPreConditionName() {
        return preConditionName;
    }
    
    public String getActionName() {
        return actionName;
    }
    
    public List<Condition> getPostConditions() {
        return this.postConditions;
    }
    
    public Condition getPostCondition(int i) {
        return this.postConditions.get(i);
    }
    
    public void addNewPostCondition(Condition cond) {
        this.postConditions.add(cond);
    }
    
    public double getTotalOfPostConditionValues() {
        double res = 0;
        for (Condition post : postConditions) {
            res += post.getValue();
        }
        return res;
    }
    
    public double getProbabilityOfTargetPostCondition(String postConditionName) {
        double total = getTotalOfPostConditionValues();
        double res = 0;
        for (Condition post : postConditions) {
            if (post.getName().equals(postConditionName)) {
                res = post.getValue()/total;
                break;
            }
        }
        return res;
    }
    
    public Rule normalize() {
        double total = getTotalOfPostConditionValues();
        for (Condition post : postConditions) {
            post.setValue(post.getValue()/total);
        }
        return this;
    }
    
    public boolean isSameKind(ActionSet as) {
        return this.preConditionName.equals(as.getPreMonitorableAction()) 
                && this.actionName.equals(as.getControllableAction());
    }
    
    public boolean isNeverUpdated() {
        for (Condition post : postConditions) {
            if (post.getValue() != post.getINITIALVALUE()) {
                return false;
            }
        }
        return true;
    }
    
    public boolean existsOnlyNanValue() {
        for (Condition post : postConditions) {
            if (!Double.isNaN(post.getValue())) {
                return false;
            }
        }
        return true;
    }
}
