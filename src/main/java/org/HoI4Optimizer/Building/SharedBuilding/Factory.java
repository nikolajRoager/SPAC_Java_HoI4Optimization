package org.HoI4Optimizer.Building.SharedBuilding;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Nation.State;

import java.io.PrintStream;

/// Base class for shared buildingsÆ military, civilian or Refineries (so all factories, hence the name), they take up the same slots and are constructed in the same way
/// This class doesn't add anything to the building abstract class, I only define it so I can create something which is either a CivilianFactory, MilitaryFactory, or a Refinery but not infrastructure
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
public abstract class Factory extends Building implements Cloneable {
    /// My id, likely my location in the List of factories of my type
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    /// Used when we are inserting this in a list, and also need to update the ID to fit that list
    public Factory setIdAndGet(int id)
    {
        this.id=id;
        return this;
    }

    /// Factories created by event activate immediately
    public Factory(State location,boolean underConstruction)
    {
        this.location=location;
        this.generateName();
        this.underConstruction = underConstruction;
        if (underConstruction) CIC_invested = 0;
        else CIC_invested = getCost(getMyType());
    }


    /// Print a report of this building and all its properties
    public void printReport(PrintStream out, String prefix)
    {
        out.println(prefix+"Name..................: "+name);
        out.println(prefix+"Location..............: "+(location==null?"offmap": location.getName()));
        if (underConstruction)
        {
            out.println(prefix+"Building..........: "+String.format("%.2f",CIC_invested)+"/"+String.format("%.2f",getCost(getMyType()))+" CIC");
        }
    }

    /// Send the building beyond the Urals (or wherever), Brick by Brick
    @Override
    public void setLocation(State location) {
        this.location = location;
        generateName();
    }
    /// Generate the name of this factory, given a town name
    public abstract void generateName();

    @Override
    public Factory clone() {
        try {
            Factory clone = (Factory) super.clone();
            clone.id = id;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
