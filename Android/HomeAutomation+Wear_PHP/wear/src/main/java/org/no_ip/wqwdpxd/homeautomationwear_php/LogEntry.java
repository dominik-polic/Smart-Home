package org.no_ip.wqwdpxd.homeautomationwear_php;

import android.text.format.DateFormat;

public class LogEntry {
    private Object action;
    private String node;
    private String user;
    private String timestamp;
    private String origin;

    protected LogEntry(Object action, String node, String user){
        this.action = action;
        this.node = node;
        this.user = user;
        this.timestamp= DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString();
        this.origin = "ANDROID_APP";
    }
    protected LogEntry(Object action, String node, String user, String origin){
        this.action = action;
        this.node = node;
        this.user = user;
        this.timestamp= DateFormat.format("yyyy-MM-dd HH:mm:ss", new java.util.Date()).toString();
        this.origin = origin;
    }

    public Object getAction() {
        return action;
    }

    public void setAction(Object action) {
        this.action = action;
    }

    public String getNode() {
        return node;
    }

    public void setNode(String node) {
        this.node = node;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }
}
