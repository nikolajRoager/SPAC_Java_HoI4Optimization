package org.HoI4Optimizer.Building.stateBuilding;

import org.HoI4Optimizer.Nation.State;

import org.HoI4Optimizer.NationalConstants.stateType;
/// A temporary class, used for infrastructure construction projects
public class Infrastructure extends StateBuilding implements Cloneable {
    public static final int maxLevel=5;

    public Infrastructure(State location,int level,boolean underConstruction) {
        this.underConstruction = underConstruction;


        this.location = location;

        this.level = level;
        if (underConstruction) {
            level = Math.clamp(level, 1, maxLevel);

            name = getUpgradeName(level);
            CIC_invested = 0;

        }
        else
        {
            this.name="Infrastructure level "+level;
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
                case 1 ->  "Rural rail service (infrastructure 0->1)";
                case 2 ->  "Paved roads (infrastructure 1->2)";
                case 3 ->  "Agrarian motorization programme (infrastructure 2->3)";
                case 4 ->  "Gas-stations and auto-shops (infrastructure 3->4)";
                case 5 ->  "Highway (infrastructure 4->5)";
                default -> "Infrastructure Illegal level " + level;
            };
        }
        else
            return switch (level) {
                case 1 -> "Street lighting (infrastructure 0->1)";
                case 2 -> "Tram network (infrastructure 1->2)";
                case 3 -> "water and gas pipes (infrastructure 2->3)";
                case 4 -> "Urban electrification (infrastructure 3->4)";
                case 5 -> "Metro network (infrastructure 4->5)";
                default ->"Infrastructure Illegal level " + level;
            };

    }

    ///Upgrade infrastructure level, called automatically when construction finishes
    @Override
    protected void onFinishConstruction() {
        //Upgrade
        level=Math.clamp(level, 0, maxLevel);
        underConstruction=false;
        //Use finished name
        name="Infrastructure level "+level;
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

    @Override
    public String getBuildingName(){return "Infrastructure";}

}
