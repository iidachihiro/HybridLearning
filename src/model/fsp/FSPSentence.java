package model.fsp;

import java.util.ArrayList;
import java.util.List;

public class FSPSentence {
    private String map;
    private List<Process> processes;
    
    public FSPSentence(String map, Process process) {
        this.map = map;
        this.processes = new ArrayList<>();
        processes.add(process);
    }
    
    public String getMap() {
        return this.map;
    }
    
    public List<Process> getProcesses() {
        return this.processes;
    }
    
    public Process getProcess(int i) {
        return this.processes.get(i);
    }
    
    public int getIndexOf(String action) {
        for (int i = 0; i < this.processes.size(); i++) {
            Process process = this.processes.get(i);
            if (process.getAction().equals(action)) {
                return i;
            }
        }
        return -1;
    }
    
    public void addProcess(Process process) {
        this.processes.add(process);
    }
}
