package org.HoI4Optimizer.Building.stateBuilding;

import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Nation.State;

import org.HoI4Optimizer.NationalConstants.stateType;
/// A temporary class, used for infrastructure construction projects
public class Infrastructure extends stateBuilding implements Cloneable {
    public static final int maxLevel=5;

    public Infrastructure(State location,int level,boolean underConstruction) {
        this.underConstruction = underConstruction;

        this.level = level;
        if (underConstruction) {
            level = Math.clamp(level, 1, maxLevel);

            this.location = location;

            name = getUpgradeName(level);
            CIC_invested = 0;

        }
        else
        {
            this.name=location.getName()+" Infrastructure level "+level;
            CIC_invested = getCost(this.getMyType());
        }
    }

    /// Should really be static, but that doesn't work with Java Overrides
    @Override
    public int getMaxLevel() {
        return maxLevel;
    }

    @Override
    public String getUpgradeName(int level) {
        //Some reasonable names for mid to late 1930s infrastructure expansion programs, depending on if it is mainly rural or urban
        if (location.getType().getBuildingSlots() < stateType.large_town.getBuildingSlots()) {
            return switch (level) {
                case 1 -> location.getName() + " Rural rail service (infrastructure 0->1)";
                case 2 -> location.getName() + " Paved roads (infrastructure 1->2)";
                case 3 -> location.getName() + " Agrarian motorization programme (infrastructure 2->3)";
                case 4 -> location.getName() + " Gas-stations and auto-shops (infrastructure 3->4)";
                case 5 -> location.getName() + " Highway (infrastructure 4->5)";
                default -> location.getName() + " Infrastructure Illegal level " + level;
            };
        }
        else
            return switch (level) {
                case 1 -> location.getName() + " Street lighting (infrastructure 0->1)";
                case 2 -> location.getName() + " Tram network (infrastructure 1->2)";
                case 3 -> location.getName() + " water and gas pipes (infrastructure 2->3)";
                case 4 -> location.getName() + " Urban electrification (infrastructure 3->4)";
                case 5 -> location.getName() + " Metro network (infrastructure 4->5)";
                default -> location.getName() + " Infrastructure Illegal level " + level;
            };

    }

    ///Upgrade infrastructure level, called automatically when construction finishes
    @Override
    protected void onFinishConstruction() {
        //Upgrade
        level=Math.clamp(level+1, 0, maxLevel);
        //Use finished name
        name=location.getName()+" Infrastructure level "+level;
    }

    @Override
    public type getMyType() {
        return type.Infrastructure;
    }

    @Override
    public Infrastructure clone() {
        try {
            Infrastructure clone = (Infrastructure) super.clone();
            //Keep the same location, the state is responsible for moving me to a clone of it, if it is cloned
            clone.location = location;
            clone.level = level;
            clone.name = name;

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
