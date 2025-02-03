package org.HoI4Optimizer.Nation.decision;
import com.diogonunes.jcolor.Attribute;

import net.bytebuddy.utility.nullability.NeverNull;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Building.SharedBuilding.Factory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.stateBuilding.StateBuilding;
import org.HoI4Optimizer.Nation.State;
import org.HoI4Optimizer.NationalConstants.Equipment;

import java.io.PrintStream;

import static com.diogonunes.jcolor.Ansi.colorize;

/// Please DO NOT USE THE DEFAULT CONSTRUCTOR UNLESS YOU ARE A JSON DESERIALIZER, use the specialised constructors for creating different types of decisions instead
/// This records stores something which the player can build
/// @param type is this a new building to build? a state building to upgrade? or is it re-assignment of an existing factory?
/// @param location what state is this new thing build or this thing upgraded
/// @param building Either a new shared building (with location null), which will be built in this state, or an existing stateBuilding which will be upgraded
/// @param description A human friendly description of what this does
public record BuildDecision(
        Type type,
        State location,
        Building building,
        String description
)
{
    public enum Type
    {
        build,
        upgrade
    }
    /// Some basic sanity checks
    public BuildDecision
    {
        {
            if (building.getLocation()==null)
                building.setLocation(location);
            else if (building.getLocation()!=location)
                throw new IllegalArgumentException("Location is not the same as the building location");
            else if (type==Type.upgrade && ! (building instanceof StateBuilding))
            {
                throw new IllegalArgumentException("Upgrade command can only be applied to state buildings");
            }
        }
    }

    /// specifically create a decision which upgrades a state building
    public BuildDecision(StateBuilding building, String description)
    {
        this(Type.upgrade,building.getLocation(),building,description);
    }

    /// Specifically create a decision building a new shared building in this state, which creates a new building, to be build in this location
    public BuildDecision(State location, Factory building, String description)
    {
        this(Type.build,location,building,description);
    }
    /// Specifically create a decision building a NEW military factory in this location producing this equipment
    public BuildDecision(State location, MilitaryFactory factory, Equipment equipment, String description)
    {
        this(Type.build,location,factory,description);
        factory.setProduct(equipment);
    }

    /// Print this thing to out
    public void display(int id, @NeverNull PrintStream out)
    {
        out.println(colorize("Decision "+id,Attribute.BOLD(),Attribute.GREEN_TEXT()));
        switch (type)
        {
            case build ->{
                out.println(colorize("  Build "+building.getBuildingName()+" in "+location.getName()+":",Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  "+building.getName(),Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  "+description,Attribute.BRIGHT_YELLOW_TEXT()));
            }
            case upgrade ->{
                out.println(colorize("  Upgrade "+building.getBuildingName()+" in "+location.getName()+":",Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  "+((StateBuilding) building).getNextUpgradeName(),Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  "+description,Attribute.BRIGHT_YELLOW_TEXT()));
            }
        }
    }
}
