package org.HoI4Optimizer.Factory;

import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Nation.NationState;
import org.HoI4Optimizer.Nation.State;
import org.HoI4Optimizer.NationalConstants.Equipment;

import java.io.PrintStream;
import java.util.Objects;
import java.util.Random;

import static com.diogonunes.jcolor.Ansi.colorize;

/// A military factory in a state, it is the only factory which gets its own class, civilian factories and refineries don't build up efficiency so they can just be a number
/// It represents a single production line, changing equipment effectively closes the factory and replaces it with a new one
public class MilitaryFactory extends Factory implements Cloneable{
    /// Has this factory been closed, and is kept around only for history
    private boolean closed;

    /// Current efficiency, a number between 0.0 and 1.0, which we multiply the output with
    private double efficiency;

    /// What this factory is making
    private Equipment product;

    /// Unique name of the thing I produce, used for safe loading of products
    private String productName;

    public String getProductName() {
        return productName;
    }

    /// For use by JSon deserializer, change the name of the product, will be loaded later
    public void setProductName(String productName) {
        if (Objects.equals(this.productName, "null"))
            this.productName = productName;
        else
            throw new RuntimeException("Attempting to reassign factory already assigned to "+this.productName);
    }

    public void setProduct(Equipment product)
    {
        if (Objects.equals(this.productName, "null"))
        {
            this.product=product;
        }
        if (!Objects.equals(this.productName, product.getName()))
            throw new RuntimeException("Attempting to assign product "+product.getName()+" to factory expecting "+this.productName);
        else
            this.product=product;
    }

    /// Military industrial capacity produced in the lifetime of the factory
    private double MIC_produced;


    /// Create civilian factory with this name, which may or may not have been unlocked already
    public MilitaryFactory(String name, State location, boolean underConstruction)
    {
        super(name,location,underConstruction);
        efficiency = 0.1;
        this.closed = false;
        this.MIC_produced = 0;
        this.name = name;
        product=null;
        productName="null";
    }

    /// What am I, the different types behave very differently:
    /// Refineries add rubber resources to the state
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// and each military factory contain a unique production line
    @Override
    public type getMyType() {
        return type.Military;
    }

    @Override
    public void printReport(PrintStream out, String prefix) {
        out.println(colorize(prefix+"==== Military Factory ========================================================", Attribute.BRIGHT_GREEN_TEXT()) );
        super.printReport(out,prefix+"\t");
        if (closed)
        {
            out.println(colorize(prefix+"\tCLOSED",Attribute.RED_TEXT()));
        }
        else
        {
            if (product==null)
            {
                out.println(colorize(prefix+"\tProduction line not set up!",Attribute.GREEN_TEXT()));
            }
            else
            {
                out.println(colorize(prefix+"\tProduces..............: "+product.getName()+" ("+product.getShortname()+")"));
                out.println(colorize(prefix+"\tTotal units produced..: "+getQuantity()+"\t("+MIC_produced+" MIC)"));
                out.println(colorize(prefix+"\tEfficiency............: "+efficiency));
            }
        }
    }

    /// Generate the name of this factory, given a town name
    @Override
    public void generateName(String townName) {
        Random rand = new Random();
        if (product==null)
        {
            name=townName+" factory (assignable Military Factory)";
        }
        else
        {
            int producerId = rand.nextInt(0,product.brandNames.size());
            String producer="";
            if (product.brandNames!=null) {
                if (product.brandNames.size() > producerId) {
                    producer = product.brandNames.get(producerId%product.brandNames.size()) + " ";
                }
            }


            name=townName+' '+producer+product.getFactoryNames().get(rand.nextInt(0,product.getFactoryNames().size()));
        }
    }

    /// Create civilian factory with this name, which may or may not have been unlocked already
    public MilitaryFactory(String name,State location, boolean underConstruction,double efficiencyBase)
    {
        super(name,location,underConstruction);
        efficiency = efficiencyBase;
        this.closed = false;
        this.MIC_produced = 0;
        this.name = name;
        product=null;
        productName="null";
    }

    /// For use by JSon deserializer
    public MilitaryFactory()
    {
        super("null",null,false);
        efficiency = 0.1;
        this.closed = false;
        this.MIC_produced = 0;
        this.name = name;
        product=null;
        productName="null";
    }

    /// Current efficiency of the production line
    public double getEfficiency() {return efficiency;}

    /// How many MIC points have we ever made?
    public double getMIC_produced() {return MIC_produced;}

    /// how much stuff have we ever made?
    //Round down, something which has not been finished has not been produced
    public long getQuantity() {return (long)(MIC_produced/product.getUnit_cost());}
    /// A name for identifying this factory
    public String getName() {return name;}

    /// Is this factory operating?
    @Override
    public boolean operating() {return !underConstruction && !closed;}

    /// Step forward 1 day
    public void update(NationState NationState) {

    }

    @Override
    public MilitaryFactory clone() {
        try {
            MilitaryFactory clone = (MilitaryFactory) super.clone();
            clone.CIC_invested=CIC_invested;
            clone.location=null;//MUST BE SET LATER BY THE STATE OWNING US
            clone.name=name;
            clone.underConstruction=underConstruction;
            clone.closed=closed;
            clone.efficiency=efficiency;
            clone.MIC_produced=MIC_produced;
            //The product can not be modified by the factory, so I just copy the reference
            clone.product=product;
            clone.productName=productName;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }
}
