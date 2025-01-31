package org.HoI4Optimizer.Building.SharedBuilding;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Nation.State;

import java.io.PrintStream;

import static com.diogonunes.jcolor.Ansi.colorize;

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
public abstract class Factory extends Building {
    /// Factories created by event activate immediately
    public Factory(String townName,State location,boolean underConstruction)
    {
        this.location=location;
        this.generateName(townName);
        this.underConstruction = underConstruction;
        if (underConstruction) CIC_invested = 0;
        else CIC_invested = getCost(getMyType());;
    }


    public void printReport(PrintStream out, String prefix)
    {
        out.println(prefix+"Name..................: "+name);
        out.println(prefix+"Location..............: "+(location==null?"offmap": location.getName()));
        if (underConstruction)
        {
            out.println(prefix+"Building..........: "+String.format("%.2f",CIC_invested)+"/"+String.format("%.2f",getCost(getMyType()))+" CIC");
        }
    }

    /// Generate the name of this factory, given a town name
    public abstract void generateName(String townName);
}
