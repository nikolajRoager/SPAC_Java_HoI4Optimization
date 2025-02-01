package org.HoI4Optimizer.Nation.decision;

import net.bytebuddy.implementation.bind.annotation.Default;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Building.SharedBuilding.Factory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.stateBuilding.stateBuilding;
import org.HoI4Optimizer.Nation.BuildingDecision;
import org.HoI4Optimizer.Nation.State;
import org.HoI4Optimizer.NationalConstants.Equipment;

/// Please DO NOT USE THE DEFAULT CONSTRUCTOR UNLESS YOU ARE A JSON DESERIALIZER, use the specialised constructors decision(State )!
/// This records stores something which the player can do to their nation, either building something or changing production of an existing military factory
/// @param build is this a new building to build? if not it is interpreted as an order to change the equipment produced by the building
/// @param location
/// @param building Either a new shared building (with location null), which will be built in this state, an existing stateBuilding which will be upgraded, or an existing military factory
/// @param equipment
public record decision(
        boolean build,
        State location,
        Building building,
        Equipment equipment
)
{
    /// Some basic sanity checks
    public decision
    {
        if (!build)
        {
            if (!(building instanceof MilitaryFactory))
                throw new IllegalArgumentException("Non-building decisions only allowed for military factories");
            if (((MilitaryFactory) building).getProduct()!=null)
                throw new IllegalArgumentException("Attempting to re-assign military factories no-allowed");
            if (location!=building.getLocation())
                throw new IllegalArgumentException("Location is not the same as the building location");
        }
        else
        {
            if (building.getLocation()==null)
                building.setLocation(location);
            else if (building.getLocation()!=location)
                throw new IllegalArgumentException("Location is not the same as the building location");
        }

    }

    public decision (stateBuilding building)
    {
        this(true,building.getLocation(),building,null);
    }

    /// Specifically create a decision building a new shared building in this state, which creates a new building, to be build in this location
    public decision(State location, Factory building)
    {
        this(true,location,building,null);
    }
    /// Specifically create a decision building a NEW military factory in this location producing this equipment
    public decision(State location, MilitaryFactory factory, Equipment equipment)
    {
        this(true,location,factory,equipment);
    }
    /// Specifically change the equipment of this factory
    public decision(MilitaryFactory factory, Equipment equipment)
    {
        this(false,factory.getLocation(),factory,equipment);
    }



}
