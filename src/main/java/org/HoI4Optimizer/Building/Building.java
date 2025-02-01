package org.HoI4Optimizer.Building;

import org.HoI4Optimizer.Nation.State;

/// Any type of building which can be constructed , parent class for factories
public abstract class Building {
    public enum type
    {
        Military,
        Refinery,
        Civilian,
        Infrastructure,
    }

    protected State location;
    /// Name of this construction project
    protected String name;

    ///Under construction, the factory does not contribute anything
    protected boolean underConstruction;

    ///How much Civilian Industrial Capacity has been invested in this building? (used when under construction)
    protected double CIC_invested;

    /// CIC to build this building
    public static double getCost(type T)
    {
        switch (T)
        {
            case Civilian -> {return 10800;}
            case Military -> {return 7200;}
            case Refinery -> {return 14500;}
            case Infrastructure -> {return 6000;}
            default -> {return 10000;}//Should never be returned!
        }
    }


    /// What am I, the different types behave very differently:
    /// Refineries add rubber resources to the state
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// and each military factory contain a unique production line
    public abstract Building.type getMyType();

    /// Is this thing being build
    public boolean getUnderConstruction() {
        return underConstruction;
    }

    /// Mark this thing as under construction or not, used by json deserializer
    public void setUnderConstruction(boolean underConstruction) {
        this.underConstruction = underConstruction;
    }

    /// Called automatically when the building is finished
    protected void onFinishConstruction()
    {/*Overwrite this function to do stuff*/}

    /// Add this CIC to the factory, return true if the construction finished,
    /// CIC is counted up at the end of every day in game, and buildings do not finish faster regardless if the last step costs 100 or 1 CIC
    /// I believe this is how it works in game, (or so my testing suggests)
    public boolean construct(double CIC)
    {
        //This is already working
        if (!underConstruction) return true;

        CIC_invested +=CIC;

        if (CIC_invested >getCost(getMyType()))
        {
            //We are done here
            underConstruction = false;
            CIC_invested =getCost(getMyType());
            onFinishConstruction();
            return true;
        }
        else return false;
    }

    /// Send the building beyond the Urals (or wherever), Brick by Brick
    public void setLocation(State location) {
        this.location = location;
    }

    /// Where is this building, controls infrastructure bonus applied
    public State getLocation() {
        return location;
    }

    /// What is this thing called
    public String getName() {
        return name;
    }

    /// Overwrite the name of this thing
    public void setName(String name) {
        this.name = name;
    }

    /// How much civilian industrial capacity CIC has been invested in this building?
    public double getCIC_invested() {
        return CIC_invested;
    }

    /// Is this factory operating?, other implementations may overwrite this with more data
    public boolean operating() {return !underConstruction;}

}
