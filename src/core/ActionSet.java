package core;

public class ActionSet {
    private String preMonitorableAction;
    private String controllableAction;
    private String postMonitorableAction;
    
    public ActionSet(String x, String y, String z) {
        this.preMonitorableAction = x;
        this.controllableAction = y;
        this.postMonitorableAction = z;
    }
    
    public ActionSet(String...strings) {
        this.preMonitorableAction = strings[0];
        this.controllableAction = strings[1];
        this.postMonitorableAction = strings[2];
    }
    
    public String getPreMonitorableAction() {
        return preMonitorableAction;
    }
    
    public String getControllableAction() {
        return this.controllableAction;
    }
    
    public String getPostMonitorableAction() {
        return this.postMonitorableAction;
    }
}

