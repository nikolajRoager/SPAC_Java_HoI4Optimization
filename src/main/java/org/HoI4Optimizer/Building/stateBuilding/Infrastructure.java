package org.HoI4Optimizer.Building.stateBuilding;

import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Nation.State;

import org.HoI4Optimizer.NationalConstants.stateType;
/// A temporary class, used for infrastructure construction projects
public class Infrastructure extends Building implements Cloneable {
    public static final int maxLevel=5;
    private int level=0;

    public Infrastructure(State location,int level,boolean underConstruction) {
        this.underConstruction = underConstruction;

        if (underConstruction) {
            level = Math.clamp(level, 1, maxLevel);

            //While under construction, we are missing one level
            this.level = level-1;
            this.location = location;

            //Some reasonable names for late 1930s infrastructure expansion programs, depending on if it is mainly rural or urban
            if (location.getType().getBuildingSlots() < stateType.large_town.getBuildingSlots())
                switch (level) {
                    case 1 -> {
                        this.name = location.getName() + " Rural rail service (infrastructure 0->1)";
                    }
                    case 2 -> {
                        this.name = location.getName() + " Paved roads (infrastructure 1->2)";
                    }
                    case 3 -> {
                        this.name = location.getName() + " Agrarian motorization programme (infrastructure 2->3)";
                    }
                    case 4 -> {
                        this.name = location.getName() + " Gas-stations and auto-shops (infrastructure 3->4)";
                    }
                    case 5 -> {
                        this.name = location.getName() + " Highway (infrastructure 4->5)";
                    }
                }
            else
                switch (level) {
                    case 1 -> {
                        this.name = location.getName() + " Installing Street lighting (infrastructure 0->1)";
                    }
                    case 2 -> {
                        this.name = location.getName() + " Tram network (infrastructure 1->2)";
                    }
                    case 3 -> {
                        this.name = location.getName() + " Water and gas infrastructure (infrastructure 2->3)";
                    }
                    case 4 -> {
                        this.name = location.getName() + " Installing electricity (infrastructure 3->4)";
                    }
                    case 5 -> {
                        this.name = location.getName() + " Metro network (infrastructure 4->5)";
                    }
                }

            CIC_invested = 0;

        }
        else
        {
            this.name=location.getName()+" Infrastructure level "+level;
            this.level = level;
            CIC_invested = getCost(this.getMyType());
        }
    }

    public int getLevel(){
        return level;
    }

    ///Upgrade infrastructure level, called automatically when construction finishes
    @Override
    protected void onFinishConstruction() {
        //Upgrade
        level=Math.clamp(level+1, 0, maxLevel);
        //Use finished name
        name=location.getName()+" Infrastructure level "+level;
    }

    /// Set level to some level
    public void setLevel(int level) {
        this.level = Math.clamp(level,0,maxLevel);
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
