package com.uairouter;

public class Cluster {
    public String tenantId;
    public String name;
    public int size;
    public int[] color;

    public Cluster(String tenantId, String name, int size, int[] color) {
        this.tenantId = tenantId;
        this.name = name;
        this.size = size;
        this.color = color;
    }
}
