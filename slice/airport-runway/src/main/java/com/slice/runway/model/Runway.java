package com.slice.runway.model;

public class Runway {

    private final String runwayId;
    private final String name;

    public Runway(String runwayId, String name) {
        this.runwayId = runwayId;
        this.name     = name;
    }

    public String getRunwayId() { return runwayId; }
    public String getName()     { return name; }

    @Override
    public String toString() {
        return String.format("Runway{id='%s', name='%s'}", runwayId, name);
    }
}
