package org.HoI4Optimizer.Building.stateBuilding;

/// A building in a state, with a level between 0 and some max, each state building has its own slot
public abstract class stateBuilding {
    public static final int maxLevel=5;
    protected int level=0;
}