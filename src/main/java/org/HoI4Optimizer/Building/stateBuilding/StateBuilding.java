package org.HoI4Optimizer.Building.stateBuilding;

import org.HoI4Optimizer.Building.Building;

/// A building in a state, which there is exactly one or 0 of, with a level between 0 and some max, each state building has its own slot
public abstract class StateBuilding extends Building {

    protected int level = 0;
    ///actually we can not define a static max level, since we can not override static functions
    ///We can't define it like this: public static final int maxLevel=5; since we can't override static functions
    ///instead each stateBuilding must override this function:
    public abstract int getMaxLevel();

    /// Can this building be upgraded? may be overwritten if we have more requirements (like radar, which require technology)
    public boolean canUpgrade()
    {
        return !underConstruction && level < getMaxLevel();
    }

    /// Get the level of this building, under construction still functions at previous level (as in game), level 0 should never be registered as under construction
    public int getLevel(){return underConstruction && level>0?  level-1: level;}

    /// Force a level, likely due to an event, like in-game ongoing construction is NOT canceled (except if max-level is reached)
    public void setLevel(int level, int day) {
        this.level = Math.clamp(level, 0, getMaxLevel());

        //Only cancel construction if we hit max level, similar to how it works in game
        if (level == getMaxLevel()) {
            //If we were under construction, stop that
            underConstruction = false;
            CIC_invested = getCost(this.getMyType());
        }
        //And also call this function as we DID just gain a new level
        onFinishConstruction(day);
    }


    /// Force an instant upgrade, likely due to an event, like in-game ongoing construction is NOT canceled (except if max-level is reached)
    public void addLevel(int level, int day)
    {
        this.level = Math.clamp(this.level+level,0,getMaxLevel());
        //Only cancel construction if we hit max level, similar to how it works in game
        if (level == getMaxLevel()) {
            //If we were under construction, stop that
            underConstruction = false;
            CIC_invested = getCost(this.getMyType());
            //And also call this function as we DID just gain a new level
            onFinishConstruction(day);
        }
    }

    /// Get the name of the upgrade to this level
    public abstract String getUpgradeName(int level);

    /// Get the name of the upgrade to this level
    public String getNextUpgradeName()
    {
        return getUpgradeName(level+1);
    }

    /// Begin the process of upgrading this to next level
    public void upgrade()
    {
        if (canUpgrade())
        {
            ++level;
            CIC_invested = 0;
            underConstruction = true;
            name=getUpgradeName(level);
        }
    }
}