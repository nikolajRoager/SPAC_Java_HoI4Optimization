package org.HoI4Optimizer.NationalConstants;

/// A military factory in a state, it is the only factory which gets its own class, civilian factories and refineries don't build up efficiency so they can just be a number
/// It represents a single production line, changing equipment effectively closes the factory and replaces it with a new one
public class MilitaryFactory {
    /// Has this factory been closed, and is kept around only for history
    boolean closed;

    /// Current efficiency, a number between 0.0 and 1.0, which we multiply the output with
    private double efficiency;

    /// Military industrial capacity produced in the lifetime of the factory
    private double MIC_produced;
    /// Quantity of weapons ever produced
    private long quantity;



    /// The name of this factory, typically generated from the state name and equipment production
    String name;


    /// Default constructor, with default efficiency base (10%)
    MilitaryFactory() {efficiency = 0.1;}
    MilitaryFactory(double efficiencyBase) {efficiency = efficiencyBase;}

    public double getEfficiency() {return efficiency;}
    public static double getCost() {return 7200;}
}
