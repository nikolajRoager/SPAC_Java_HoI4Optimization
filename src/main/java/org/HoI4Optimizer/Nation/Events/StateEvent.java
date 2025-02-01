package org.HoI4Optimizer.Nation.Events;

/// An event which effects a single stay in a single way, always additive
///@param modify What thing in the state does this modify
///@param stateID ID of the state, using an integer id ensures that the same record work even after we clone the nation.
///@param number  How many of the things do we add, in cases where we change a boolean value, 0 means set it false, and 1 means set it true
public record StateEvent (
    Operation modify,
    int stateID,
    int number)
{

    public enum Operation {
        /// Single infrastructure level, if below 5
        Infrastructure,
        /// Add refinery to shared building slots (if slots free)
        Civilian,
        /// Add refinery to shared building slots (if slots free)
        Military,
        /// Add refinery to shared building slots (if slots free)
        Refinery,
        /// Building slot for shared buildings
        Slot,
        /// disallow all future construction, existing buildings are KEPT does not affect buildings added by event or already ongoing constructions
        noBuilding,
        /// Base steel in state, before taking into account infrastructure
        Steel,
        /// Base aluminium  in state, before taking into account infrastructure
        Aluminium,
        /// Base tungsten in state, before taking into account infrastructure
        Tungsten,
        /// Base chromium in state, before taking into account infrastructure
        Chromium,
        /// Base rubber in state, before taking into account infrastructure, does NOT include effects from refineries
        Rubber,
        /// Base oil in state, before taking into account infrastructure
        Oil,
    }
}
