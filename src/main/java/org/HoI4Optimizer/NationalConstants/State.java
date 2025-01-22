package org.HoI4Optimizer.NationalConstants;

import java.util.List;

public class State {
    private stateType type;//The type decides my base building slots
    private int extraBuildingSlots;

    private String name;

    /// Factories in this state
    private List<MilitaryFactory> factories;
    /// Civilian factories are fungible, we can just store a number
    private int CivilianFactories;
    /// Refineries are fungible, we can just store a number (between 0 and 3)
    private int Refineries;
    /// Get our military factories as a list
    public List<MilitaryFactory> getFactories() {
        return factories;
    }
    /// Get number of civilian factories
    public int getCivilianFactories() {
        return CivilianFactories;
    }
    /// Get number of refineries
    public int getRefineries() {
        return Refineries;
    }




    /// What is the type of this state? controls the number of building slots
    public stateType getType() {return type;}
    /// What is this place called?
    public String getName() {return name;}

    /// Get the number of building slots in this state, given the level of industry technology
    public int getBuildingSlots(int industry_technology)
    {
        //Each level of industry technology gives +20% to base number from type, this does not effect event slots
        return extraBuildingSlots + type.getBuildingSlots()*(industry_technology*2)/10;
    }
}
