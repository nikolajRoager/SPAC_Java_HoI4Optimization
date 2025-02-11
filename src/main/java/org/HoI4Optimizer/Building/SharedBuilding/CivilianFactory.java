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
        product=colorize("Awaiting orders",Attribute.BRIGHT_RED_TEXT()) ;
    }

    private String product;

    /// Tell the factory to start producing this stuff
    public void assign(String product)
    {
        this.product=product;
    }

    /// What am I, the different types behave very differently:
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// They are essentially interchangeable
    @Override
    public Building.type getMyType() {
        return Building.type.Civilian;
    }

    /// Print a report of this building and all its properties
    @Override
    public void printReport(PrintStream out, String prefix) {
        out.println(colorize(prefix+"==== Civilian Factory "+String.format("civ%-3d",id)+" ===================================================================", Attribute.BRIGHT_YELLOW_TEXT()) );
        super.printReport(out,prefix+"\t");
        if (operating())
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
        CivilianFactory clone = (CivilianFactory) super.clone();
        clone.CIC_invested=CIC_invested;

        //Keep the same location, the state is responsible for moving me to a clone of it, if it is cloned
        clone.id=id;
        clone.location=location;
        clone.name=name;
        clone.underConstruction=underConstruction;
        return clone;
    }

    @Override
    public String getBuildingName(){return "Civilian factory";}
}