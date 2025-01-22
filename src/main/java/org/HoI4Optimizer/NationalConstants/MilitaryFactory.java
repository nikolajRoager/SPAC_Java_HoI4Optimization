package org.HoI4Optimizer.NationalConstants;

import org.HoI4Optimizer.NationState;

/// A military factory in a state, it is the only factory which gets its own class, civilian factories and refineries don't build up efficiency so they can just be a number
/// It represents a single production line, changing equipment effectively closes the factory and replaces it with a new one
public class MilitaryFactory{
    /// Has this factory been closed, and is kept around only for history
    boolean closed;

    /// Current efficiency, a number between 0.0 and 1.0, which we multiply the output with
    private double efficiency;

    /// What this factory is making
    private Equipment product;
    /// Military industrial capacity produced in the lifetime of the factory
    private double MIC_produced;



    /// The name of this factory, typically generated from the state name and equipment production
    private final String name;


    /// Default constructor, with default efficiency base (10%)
    MilitaryFactory(String name)
    {
        efficiency = 0.1;
        this.closed = false;
        this.MIC_produced = 0;
        this.name = name;
    }
    MilitaryFactory(String name,double efficiencyBase)
    {
        efficiency = efficiencyBase;
        this.closed = false;
        this.MIC_produced = 0;
        this.name = name;
    }

    /// Current efficiency of the production line
    public double getEfficiency() {return efficiency;}

    /// How many CIC points does this building cost?
    public static double getCost() {return 7200;}

    /// How many MIC points have we ever made?
    public double getMIC_produced() {return MIC_produced;}

    /// how much stuff have we ever made?
    //Round down, something which has not been finished has not been produced
    public long getQuantity() {return (long)(MIC_produced/product.getUnit_cost());}
    /// A name for identifying this factory
    public String getName() {return name;}

    /// Step forward 1 day
    public void update(NationState NationState) {

    }
}
