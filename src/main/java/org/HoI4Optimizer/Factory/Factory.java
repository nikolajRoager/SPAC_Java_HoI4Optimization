package org.HoI4Optimizer.Factory;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.HoI4Optimizer.Nation.State;

import java.io.PrintStream;

import static com.diogonunes.jcolor.Ansi.colorize;

/// Base class for military, civilian or chemical industry, take up the same slots and are constructed in the same way
/// AI generated code to load different types of factories from Json
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME, // Use the name of the type for identification
        include = JsonTypeInfo.As.PROPERTY, // Add type as a property in JSON
        property = "type" // This property will be used to distinguish between subclasses
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = CivilianFactory.class, name = "civilian"),
        @JsonSubTypes.Type(value = MilitaryFactory.class, name = "military"),
        @JsonSubTypes.Type(value = Refinery.class, name = "refinery")
})
public abstract class Factory {

    public enum type
    {
        Military,
        Refinery,
        Civilian,
    };

    protected State location;

    public State getLocation() {
        return location;
    }

    public void setLocation(State location) {
        this.location = location;
    }

    /// Name of this factory
    protected String name;

    ///Under construction, the factory does not contribute anything
    protected boolean underConstruction;

    ///How much Civilian Industrial Capacity has been invested in this building? (used when under construction)
    protected double CIC_invested;

    /// How much civilian industrial capacity CIC has been invested in this building?
    public double getCIC_invested() {
        return CIC_invested;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /// Is this factory operating?, other implementations may overwrite this with more data
    public boolean operating() {return !underConstruction;}

    /// Factories created by event activate immediately
    public Factory(String name,State location,boolean underConstruction)
    {
        this.location=location;
        this.name = name;
        this.underConstruction = underConstruction;
        if (underConstruction) CIC_invested = getCost(getMyType());
        else CIC_invested = 0;
    }

    /// What am I, the different types behave very differently:
    /// Refineries add rubber resources to the state
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// and each military factory contain a unique production line
    public abstract type getMyType();
    /// CIC to build this building
    public static double getCost(type T)
    {
        switch (T)
        {
            case Civilian -> {return 10800;}
            case Military -> {return 7200;}
            case Refinery -> {return 14500;}
            default -> {return 10000;}//Should never be returned!
        }
    }

    public boolean getUnderConstruction() {
        return underConstruction;
    }


    public void setUnderConstruction(boolean underConstruction) {
        this.underConstruction = underConstruction;
    }

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
            return true;
        }
        else return false;
    }

    public void printReport(PrintStream out, String prefix)
    {
        out.println(prefix+"Name..................: "+name);
        out.println(prefix+"Location..............: "+(location==null?"offmap": location.getName()));
        if (underConstruction)
        {
            out.println(prefix+"Building..........: "+CIC_invested+"/"+getCost(getMyType())+" CIC");
        }
    }

    /// Generate the name of this factory, given a town name
    public abstract void generateName(String townName);
}
