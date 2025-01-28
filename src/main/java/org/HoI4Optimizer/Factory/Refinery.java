package org.HoI4Optimizer.Factory;

import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Nation.State;

import java.io.PrintStream;
import java.util.Random;

import static com.diogonunes.jcolor.Ansi.colorize;

/// Refineries are a special type of factory, which doesn't produce industrial capacity, but increases rubber in the state, and add fuel to the national stockpile
/// They are essentially interchangeable
public class Refinery extends Factory implements Cloneable{
    /// A list of types of factory names, used to generate the name
    private static final String[] factoryNames =
            {
                    "refinery",
                    "hydrocarbon plant",
                    "Oil",
                    "Chemicals",
                    "synthetic rubber plant",
                    "coal liquefaction plant",
                    "fertilizer factory",
            };


    /// Create a refinery with this name, which may or may not have been unlocked already
    public Refinery(String name, State location, boolean underConstruction)
    {
        super(name,location,underConstruction);
    }
    /// What am I, the different types behave very differently:
    /// Refineries are a special type of factory, which doesn't produce industrial capacity, but increases rubber in the state, and add fuel to the national stockpile
    /// They are essentially interchangeable
    @Override
    public type getMyType() {
        return type.Refinery;
    }

    @Override
    public void printReport(PrintStream out, String prefix) {
        out.println(colorize(prefix+"==== Chemical plant ========================================================", Attribute.BRIGHT_YELLOW_TEXT()) );
        super.printReport(out,prefix+"\t");
    }


    /// Generate the name of this factory, given a town name
    @Override
    public void generateName(String townName) {
        Random rand = new Random();
        name=townName+ ' '+factoryNames[rand.nextInt(0, factoryNames.length)];
    }

    @Override
    public Refinery clone() {
        try {
            Refinery clone = (Refinery) super.clone();
            clone.CIC_invested=CIC_invested;
            clone.location=null;//MUST BE SET LATER BY THE STATE OWNING US
            clone.name=name;
            clone.underConstruction=underConstruction;
            // TODO: copy mutable state here, so the clone can't change the internals of the original
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
