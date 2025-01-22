package org.HoI4Optimizer.NationalConstants;

/// A generic factory
public abstract class Factory {
    public enum type
    {
        Military,
        Refinery,
        Civilian,
    };

    protected final String name;

    public String getName() {
        return name;
    }

    ///Under construction, the factory does not contribute anything
    protected boolean underConstruction;

    ///How much Civilian INdustrial Capacity has been invested in this building? (used when under construction)
    public double CICinvested;

    /// Is this factory operating?, other implementations may overwrite this with more data
    public boolean operating() {return !underConstruction;}

    /// Factories created by event activate immediately
    public Factory(String name,boolean underConstruction)
    {
        this.name = name;
        this.underConstruction = underConstruction;
        if (underConstruction) CICinvested = getCost();
        else CICinvested = 0;
    }

    /// What am I, the different types behave very differently:
    /// Refineries add rubber resources to the state
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// and each military factory contain a unique production line
    public abstract type getMyType();
    /// CIC to build this building
    public abstract double getCost();

    /// Add this CIC to the factory, return true if the construction finished
    public boolean construct(double CIC)
    {
        //This is already working
        if (!underConstruction) return true;

        CICinvested+=CIC;

        if (CICinvested>getCost())
        {
            //We are done here
            underConstruction = false;
            CICinvested=getCost();
            return true;
        }
        else return false;
    }
}
