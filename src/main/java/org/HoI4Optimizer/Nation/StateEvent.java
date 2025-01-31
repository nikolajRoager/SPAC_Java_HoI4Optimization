package org.HoI4Optimizer.Nation;

import java.io.PrintStream;
import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;

/// An event which effects a single stay in a single way, always additive
public class StateEvent extends BaseEvent {

    public enum Target{
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

    /// What thing in the state does this target
    private Target target;

    /// Id of the state, an id not a State because I want this to work for different copies of the nation
    private int stateID;

    /// How many of the things do we add, the value is always an integer
    private int number;

    public void setNumber(int value)
    {
        this.number = value;
    }

    public void setTarget(Target target)
    {
        this.target = target;
    }

    public void setState(int stateID)
    {
        this.stateID = stateID;
    }

    /// Apply this event to the target state in this list
    /// @param stateList List of states in this nation, included so that the same event can be applied to different copies of the nation
    void apply(List<State> stateList, PrintStream out)
    {
        switch (target) {
            case null->{
                if (out!=null)
                {
                    out.println(colorize("    No effect",MiddlingOutcome));
                }
            }
            case Slot->{
                if (out!=null)
                {
                    State state = stateList.get(stateID);
                    state.setExtraBuildingSlots(state.getExtraBuildingSlots()+number);
                    out.println(colorize("    add "+number+" building slots in "+ state.getExtraBuildingSlots()+", now has "+ state.getExtraBuildingSlots(), GoodOutcome));
                }
            }
            case Infrastructure->{

            }
            case Civilian->{

            }
            case Military->{

            }
            case Refinery->{

            }
            case Oil -> {

            }
            case Aluminium->{

            }
            case Tungsten->{

            }
            case Chromium->{

            }
            case Rubber->{

            }
            case Steel->{

            }
            case noBuilding -> {

            }
        }
    }
}
