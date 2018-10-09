package model.generator;

import java.util.ArrayList;
import java.util.List;

import core.Condition;
import core.Rule;
import model.fsp.FSPSentence;
import model.fsp.Process;
import util.GDUtils;
import util.SGDUtils;

public class DomainModelGenerator {
    private List<FSPSentence> fsps;
    
    public DomainModelGenerator() {
        this.fsps = new ArrayList<>();
    }
    
    public void generate(List<Rule> rules, double threshold, int count, String mode) {
        for (Rule rule : rules) {
            if (rule.isNeverUpdated()) {
                continue;
            }
            if (rule.existsOnlyNanValue()) {
                continue;
            }
            String map = translateMAP(translateCondition(rule.getPreConditionName()));
            String action = translateAction(rule.getActionName());
            List<String> posts = new ArrayList<>();
            for (Condition cond : rule.getPostConditions()) {
                if (cond.getValue() < threshold) {
                    continue;
                }
                String post = translateCondition(cond.getName());
                if (!posts.contains(post)) {
                    posts.add(post+" -> "+translateMAP(post));
                }
            }
            int index = getIndexOf(map);
            if (index < 0) { // fsps don't have same map
                this.fsps.add(new FSPSentence(map, new Process(action, posts)));
            } else {
                int index_ = fsps.get(index).getIndexOf(action);
                if (index_ < 0) { // fsps have same map, but don't have same action
                    this.fsps.get(index).addProcess(new Process(action, posts));
                } else { // fsps have same map and action
                    this.fsps.get(index).getProcess(index_).addPosts(posts);
                }
            }
        }
        if (mode.equals("GD")) {
            GDUtils.outputDomainModel(fsps);
        } else if (mode.equals("SGD")) {
            SGDUtils.outputDomainModel(fsps);
        }
        System.out.println("Domain model is updated. (The "+count+" action set.)");
    }
    
    private int getIndexOf(String map) {
        for (int i = 0; i < this.fsps.size(); i++) {
            FSPSentence fsp = this.fsps.get(i);
            if (fsp.getMap().equals(map)) {
                return i;
            }
        }
        return -1;
    }
    
    // for sample traces
    
    private String translateCondition(String condName) {
        String name = condName;
        String id = "";
        if (name.indexOf('_') >= 1) {
            id = name.substring(0, name.indexOf('_')+1);
            name = name.substring(name.indexOf('_')+1);
        }
        switch (name) {
            case "arrive.e":
                return id+"arrive['e]";
            case "pickupfail":
                return id+"pickupfail";
            case "pickupsuccess":
                return id+"pickupsuccess";
            case "arrive.m":
                return id+"arrive['m]";
            case "arrive.w":
                return id+"arrive['w]";
            case "putsuccess":
                return id+"putsuccess";
            case "putfail":
                return id+"putfail";
            default:
                return null;
        }
    }
    
    private String translateMAP(String pre) {
        final String[] maps = {"MAP['e]", "MAP['m]", "MAP['w]"};
        String id = "";
        if (pre.indexOf('_') >= 1) {
            id = pre.substring(0, pre.indexOf('_')+1);
            pre = pre.substring(pre.indexOf('_')+1);
        }
        switch (pre) {
            case "arrive['e]":
                return id+maps[0];
            case "pickupfail":
                return id+maps[0];
            case "pickupsuccess":
                return id+maps[0];
            case "arrive['m]":
                return id+maps[1];
            case "arrive['w]":
                return id+maps[2];
            case "putsuccess":
                return id+maps[2];
            case "putfail":
                return id+maps[2];
            default:
                return null;
        }
    }
    
    private String translateAction(String action) {
        String id = "";
        if (action.indexOf('_') >= 1) {
            id = action.substring(0, action.indexOf('_')+1);
            action = action.substring(action.indexOf('_')+1);
        }
        switch (action) {
            case "move.e":
                return id+"move['e]";
            case "move.m":
                return id+"move['m]";
            case "move.w":
                return id+"move['w]";
            case "pickup":
                return id+"pickup";
            case "putdown":
                return id+"putdown";
            default:
                return null;
        }
    }
}
