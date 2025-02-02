package org.HoI4Optimizer.Nation.decision;
import com.diogonunes.jcolor.Attribute;

import net.bytebuddy.utility.nullability.NeverNull;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Building.SharedBuilding.Factory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.stateBuilding.stateBuilding;
import org.HoI4Optimizer.Nation.State;
import org.HoI4Optimizer.NationalConstants.Equipment;

import java.io.PrintStream;

import static com.diogonunes.jcolor.Ansi.colorize;

/// Please DO NOT USE THE DEFAULT CONSTRUCTOR UNLESS YOU ARE A JSON DESERIALIZER, use the specialised constructors for creating different types of decisions instead
/// This records stores something which the player can do to their nation, either building something or changing production of an existing military factory
/// @param type is this a new building to build? a state building to upgrade? or is it re-assignment of an existing factory?
/// @param location what state is this new thing build or this thing upgraded
/// @param building Either a new shared building (with location null), which will be built in this state, an existing stateBuilding which will be upgraded, or an existing military factory
/// @param equipment If we re-assign equipment, what do we make it do
public record decision(
        Type type,
        State location,
        Building building,
        Equipment equipment
)
{
    public enum Type
    {
        build,
        upgrade,
        reassign
    }
    /// Some basic sanity checks
    public decision
    {
        if (type==Type.reassign)
        {
            if (!(building instanceof MilitaryFactory))
                throw new IllegalArgumentException("Non-building decisions only allowed for military factories");
            if (((MilitaryFactory) building).getProduct()!=null && ((MilitaryFactory) building).getProduct()!=equipment)
                throw new IllegalArgumentException("Attempting to re-assign military factories no-allowed");
            if (location!=building.getLocation())
                throw new IllegalArgumentException("Location is not the same as the building location");
        }
        else if (type==Type.build ||type==Type.upgrade)
        {
            if (building.getLocation()==null)
                building.setLocation(location);
            else if (building.getLocation()!=location)
                throw new IllegalArgumentException("Location is not the same as the building location");
            else if (type==Type.upgrade && ! (building instanceof stateBuilding))
            {
                throw new IllegalArgumentException("Upgrade command can only be applied to state buildings");
            }
        }
    }

    /// specifically create a decision which upgrades a state building
    public decision (stateBuilding building)
    {
        this(Type.upgrade,building.getLocation(),building,null);
    }

    /// Specifically create a decision building a new shared building in this state, which creates a new building, to be build in this location
    public decision(State location, Factory building)
    {
        this(Type.build,location,building,null);
    }
    /// Specifically create a decision building a NEW military factory in this location producing this equipment
    public decision(State location, MilitaryFactory factory, Equipment equipment)
    {
        this(Type.build,location,factory,equipment);
    }
    /// Specifically change the equipment of this factory
    public decision(MilitaryFactory factory, Equipment equipment)
    {
        this(Type.reassign,factory.getLocation(),factory,equipment);
    }

    /// Print this thing to out
    void display(@NeverNull PrintStream out)
    {
        switch (type)
        {
            case build ->{
                out.println(colorize("==Possible decision==",Attribute.BOLD(),Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  Build building "+(building).getName(),Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  In"+location.getName(),Attribute.BRIGHT_YELLOW_TEXT()));
            }
            case reassign ->{
                out.println(colorize("==Possible decision==",Attribute.BOLD(),Attribute.BRIGHT_GREEN_TEXT()));
                out.println(colorize("  Re-assign military factory "+building.getName()+" to produce "+equipment.getName(),Attribute.BOLD(),Attribute.BRIGHT_GREEN_TEXT()));
            }
            case upgrade ->{
                out.println(colorize("==Possible decision==",Attribute.BOLD(),Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  Upgrade building "+((stateBuilding)building).getNextUpgradeName(),Attribute.BRIGHT_YELLOW_TEXT()));
                out.println(colorize("  In"+location.getName(),Attribute.BRIGHT_YELLOW_TEXT()));
            }
        }
    }
}
