package org.HoI4Optimizer.Building.SharedBuilding;

import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Nation.State;

import java.io.PrintStream;
import java.util.Random;

import static com.diogonunes.jcolor.Ansi.colorize;

/// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
/// They are essentially interchangeable
public class CivilianFactory extends Factory implements Cloneable{

    /// A list of types of factory names, used to generate the name
    private static final String[] factoryNames =
    {
        "Iron-Steel works",
            "Heavy industry",
            "light metal works",
            "farming equipment factory",
            "Electronics",
            "radio factory",
            "aluminium works",
            "kitchen appliance factory",
            "woodworks",
            "brickworks",
            "cement plant",
            "engine factory",
            "cloth factory",
            "automotive industry",
            "tractor factory",
            "powerplant",
            "forge",
            "paper factory",
    };

    /// Create civilian factory with this name, which may or may not have been unlocked already
    public CivilianFactory(State location, boolean underConstruction)
    {
        super(location,underConstruction);
    }

    /// For use by JSon deserializer, creates already constructed civilian factory
    public CivilianFactory()
    {
        super(null,false);
    }


    /// What am I, the different types behave very differently:
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// They are essentially interchangeable
    @Override
    public Building.type getMyType() {
        return Building.type.Civilian;
    }

    @Override
    public void printReport(PrintStream out, String prefix) {
        out.println(colorize(prefix+"==== Civilian Factory ========================================================", Attribute.BRIGHT_YELLOW_TEXT()) );
        super.printReport(out,prefix+"\t");
    }


    public void printReport(PrintStream out,String product, String prefix) {
        out.println(colorize(prefix+"==== Civilian Factory ========================================================", Attribute.BRIGHT_YELLOW_TEXT()) );
        super.printReport(out,prefix+"\t");
        out.println(colorize(prefix+"\tProducing.............: ") +product);
    }


    /// Generate the name of this factory, given a town name
    @Override
    public void generateName() {
        Random rand = new Random();
        String townName = location==null ? "offmap" : location.getTownName(rand);
        name=townName+' '+ factoryNames[rand.nextInt(0,factoryNames.length)];
    }

    @Override
    public CivilianFactory clone() {
        try {
            CivilianFactory clone = (CivilianFactory) super.clone();
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

    @Override
    public String getBuildingName(){return "Civilian factory";}
}