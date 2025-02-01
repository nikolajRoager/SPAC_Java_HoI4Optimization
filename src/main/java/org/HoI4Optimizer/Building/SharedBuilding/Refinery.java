package org.HoI4Optimizer.Building.SharedBuilding;

import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Building.Building;
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
    public Refinery(State location, boolean underConstruction)
    {
        super(location,underConstruction);
    }
    /// What am I, the different types behave very differently:
    /// Refineries are a special type of factory, which doesn't produce industrial capacity, but increases rubber in the state, and add fuel to the national stockpile
    /// They are essentially interchangeable
    @Override
    public type getMyType() {
        return Building.type.Refinery;
    }

    @Override
    public void printReport(PrintStream out, String prefix) {
        out.println(colorize(prefix+"==== Chemical plant ========================================================", Attribute.BRIGHT_YELLOW_TEXT()) );
        super.printReport(out,prefix+"\t");
    }


    /// Generate the name of this factory, given a town name
    @Override
    public void generateName() {
        Random rand = new Random();
        String townName = location==null ? "offmap" : location.getTownName(rand);
        name=townName+ ' '+factoryNames[rand.nextInt(0, factoryNames.length)];
    }

    @Override
    public Refinery clone() {
        try {
            Refinery clone = (Refinery) super.clone();
            clone.CIC_invested=CIC_invested;

            //Keep the same location, the state is responsible for moving me to a clone of it, if it is cloned
            clone.location=location;

            clone.name=name;
            clone.underConstruction=underConstruction;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
