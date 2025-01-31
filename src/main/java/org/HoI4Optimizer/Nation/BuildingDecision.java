package org.HoI4Optimizer.Nation;

/// One decision a nation can make is to build something, somewhere
public class BuildingDecision {
    /// What kind of building decision is this
    public enum buildingDecisionType
    {
        BuildMilitaryFactory,
        BuildCivilianFactory,
        BuildRefinery,
        BuildInfrastructure,
    }


    /// What kind of building decision is this
    private final buildingDecisionType buildingDecisionType;

    /// Where will this building take place
    private final State target;

    /// What kind of building decision is this
    public buildingDecisionType getDecisionType() {return buildingDecisionType;}
    /// Where will this building take place
    public State getTarget() {return target;}

    public BuildingDecision(State target, buildingDecisionType buildingDecisionType){
        this.target = target;
        this.buildingDecisionType = buildingDecisionType;
    }
}
