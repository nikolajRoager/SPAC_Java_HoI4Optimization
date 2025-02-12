package org.HoI4Optimizer.Building.SharedBuilding;

import com.diogonunes.jcolor.Attribute;
import net.bytebuddy.utility.nullability.NeverNull;
import org.HoI4Optimizer.MyCalendar;
import org.HoI4Optimizer.MyPlotter.DataLogger;
import org.HoI4Optimizer.Nation.State;
import org.HoI4Optimizer.NationalConstants.Equipment;

import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Map;
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
    private int steelSupplied;
    private int aluminiumSupplied;
    private int tungstenSupplied;
    private int chromiumSupplied;
    private int rubberSupplied;

    //The first day this was operational
    private int day0=0;

    /// Keep track of our local data over time
    @NeverNull
    private DataLogger statisticsDepartment;

    /// How much aluminium do we get every day
    public int getAluminiumSupplied() {
        return aluminiumSupplied;
    }
    /// How much chromium do we get every day
    public int getChromiumSupplied() {
        return chromiumSupplied;
    }
    /// How much rubber do we get every day
    public int getRubberSupplied() {
        return rubberSupplied;
    }
    /// How much steel do we get every day
    public int getSteelSupplied() {
        return steelSupplied;
    }
    /// How much tungsten do we get every day
    public int getTungstenSupplied() {
        return tungstenSupplied;
    }
    /// Add this amount of steel, take what we need and return it
    /// We may go negative, that represents us having to find alternative suppliers/use lower quality or ersatz materials
    /// @param steelSupplied how much steel is available in the nation? the more negative it is, the harder it is to find replacements
    /// @return how much is left after this factory
    public int addSteelSupplied(int steelSupplied)
    {
        if (!operating() || getSteelNeeded()==0)
        {
            this.steelSupplied=0;
            return steelSupplied;
        }
        this.steelSupplied=Math.min(steelSupplied,getSteelNeeded());
        return steelSupplied-this.getSteelNeeded();
    }
    /// Add this amount of aluminium, take what we need and return it
    /// We may go negative, that represents us having to find alternative suppliers/use lower quality or ersatz materials
    /// @param aluminiumSupplied how much aluminium is available in the nation? the more negative it is, the harder it is to find replacements
    /// @return how much is left after this factory
    public int addAluminiumSupplied(int aluminiumSupplied)
    {
        if (!operating() || getAluminiumNeeded()==0)
        {
            this.aluminiumSupplied=0;
            return aluminiumSupplied;
        }
        this.aluminiumSupplied=Math.min(aluminiumSupplied,getAluminiumNeeded());
        return aluminiumSupplied-this.getAluminiumNeeded();
    }
    /// Add this amount of rubber, take what we need and return it
    /// We may go negative, that represents us having to find alternative suppliers/use lower quality or ersatz materials
    /// @param rubberSupplied how much rubber is available in the nation? the more negative it is, the harder it is to find replacements
    /// @return how much is left after this factory
    public int addRubberSupplied(int rubberSupplied)
    {
        if (!operating() || getRubberNeeded()==0)
        {
            this.rubberSupplied=0;
            return rubberSupplied;
        }
        this.rubberSupplied=Math.min(rubberSupplied,getRubberNeeded());
        return rubberSupplied-this.getRubberNeeded();
    }
    /// Add this amount of tungsten, take what we need and return it
    /// We may go negative, that represents us having to find alternative suppliers/use lower quality or ersatz materials
    /// @param tungstenSupplied how much tungsten is available in the nation? the more negative it is, the harder it is to find replacements
    /// @return how much is left after this factory
    public int addTungstenSupplied(int tungstenSupplied)
    {
        if (!operating() || getTungstenNeeded()==0)
        {
            this.tungstenSupplied=0;
            return tungstenSupplied;
        }
        this.tungstenSupplied=Math.min(tungstenSupplied,getTungstenNeeded());
        return tungstenSupplied-this.getTungstenNeeded();
    }
    /// Add this amount of chromium, take what we need and return it
    /// We may go negative, that represents us having to find alternative suppliers/use lower quality or ersatz materials
    /// @param chromiumSupplied how much chromium is available in the nation? the more negative it is, the harder it is to find replacements
    /// @return how much is left after this factory
    public int addChromiumSupplied(int chromiumSupplied)
    {
        if (!operating() || getChromiumNeeded()==0)
        {
            this.chromiumSupplied=0;
            return chromiumSupplied;
        }
        this.chromiumSupplied=Math.min(chromiumSupplied,getChromiumNeeded());
        return chromiumSupplied-this.getChromiumNeeded();
    }


    /// How much aluminium do we need every day to run optimally
    public int getAluminiumNeeded() {
        return (product==null||!operating())?0:product.getAluminium();
    }
    /// How much chromium do we need every day to run optimally
    public int getChromiumNeeded() {
        return (product==null||!operating())?0:product.getChromium();
    }
    /// How much rubber do we need every day to run optimally
    public int getRubberNeeded() {
        return (product==null||!operating())?0:product.getRubber();
    }
    /// How much steel do we need every day to run optimally
    public int getSteelNeeded() {
        return (product==null||!operating())?0:product.getSteel();
    }
    /// How much tungsten do we need every day to run optimally
    public int getTungstenNeeded() {
        return (product==null||!operating())?0:product.getTungsten();
    }

    /// Penalty due to out-of-resources
    public double getResourceMultiplier() {
        //Check what is the bottleneck
        //We only apply a penalty for it (presumably, while we are busy figuring out how to turn 100 tonne alu-foil back into aluminium, we will have more than enough time to find 50 tonnes of scap steel we can melt down)
        int deficit =
                Math.max(0,
                Math.max(-chromiumSupplied+getChromiumNeeded(),Math.max(
                Math.max(-aluminiumSupplied+getAluminiumNeeded(),-tungstenSupplied+getTungstenNeeded()),
                Math.max(-rubberSupplied+getRubberNeeded(),-steelSupplied+getSteelNeeded()))));

        //The greater our deficit, the harder solving it becomes
        //This includes if we are the 5th factory in line, and everybody else just bought up all the scrap iron
        return Math.clamp(1.0-deficit*0.05,0.0,1.0);
    }

    /// What this factory is making
    private Equipment product;

    /// Unique name of the thing I produce, used for safe loading of products
    private String productName;

    /// Save these national stats
    private double factoryOutput;
    private double efficiencyCap;

    public String getProductName() {
        return productName;
    }
    public Equipment getProduct() {
        return product;
    }

    /// For use by JSon deserializer, change the name of the product, will be loaded later
    public void setProductName(String productName) {
        if (Objects.equals(this.productName, "null"))
            this.productName = productName;
        else
            throw new RuntimeException("Attempting to reassign factory already assigned to "+this.productName);
    }

    /// Replace null with product, we can NOT re-assign existing factories (instead close the factory and build a new one)
    public void setProduct(Equipment product,int day)
    {
        if (Objects.equals(this.productName, "null"))
            this.product=product;
        else if (!Objects.equals(this.productName, product.getName()))
            throw new RuntimeException("Attempting to assign product "+product.getName()+" to factory expecting "+this.productName);
        else
            this.product=product;
        day0=day;
        generateName();
    }

    @Override
    protected void onFinishConstruction(int day) {
        super.onFinishConstruction(day);
        day0=day;//This is the day we became operational
    }

    /// Military industrial capacity produced in the lifetime of the factory
    private double MIC_produced;
    private double MIC_gain;

    /// Create civilian factory with this name, which may or may not have been unlocked already
    public MilitaryFactory(State location, boolean underConstruction)
    {
        super(location,underConstruction);
        efficiency = 0.1;
        this.closed = false;
        this.MIC_produced = 0;
        product=null;
        productName="null";
        efficiencyCap=0.5;
        factoryOutput=0.0;

        //These things will be set by the nation
        steelSupplied=0;
        aluminiumSupplied=0;
        tungstenSupplied=0;
        chromiumSupplied=0;
        rubberSupplied=0;
        statisticsDepartment = new DataLogger();
        openDepartment();
        statisticsDepartment.addPlot("Production stats",Map.of("Factory Output", Color.GREEN,"Efficiency Cap",new Color(64,255,255,127),"Efficiency",Color.CYAN,"Resource Multiplier",Color.RED),"%",false);
        statisticsDepartment.addPlot("Production",Map.of("Daily MIC", new Color(0,255,128),"Total MIC",Color.GREEN,"Units produced",Color.RED),"MIC/units",false);
    }

    /// Open this factory's department of statistics
    private void openDepartment() {
        statisticsDepartment.setLog("Efficiency",this::getEfficiency, DataLogger.logDataType.PositivePercent);
        statisticsDepartment.setLog("Efficiency Cap",()->efficiencyCap, DataLogger.logDataType.PositivePercent);
        statisticsDepartment.setLog("Factory Output",()->factoryOutput, DataLogger.logDataType.PositivePercent);
        statisticsDepartment.setLog("Resource Multiplier",this::getResourceMultiplier, DataLogger.logDataType.PositivePercent);
        statisticsDepartment.setLog("Daily MIC",()->MIC_gain, DataLogger.logDataType.PositiveReal);
        statisticsDepartment.setLog("Total MIC",()->MIC_produced, DataLogger.logDataType.PositiveReal);
        statisticsDepartment.setLog("Units produced",this::getQuantity, DataLogger.logDataType.PositiveInteger);
    }

    /// What am I, the different types behave very differently:
    /// Refineries add rubber resources to the state
    /// Civilian factories add civilian construction industrial capacity CIC to the nation's production line
    /// and each military factory contain a unique production line
    @Override
    public type getMyType() {
        return type.Military;
    }

    /// Print a report of this building and all its properties
    @Override
    public void printReport(PrintStream out, String prefix) {
        out.println(colorize(prefix+"==== Military Factory "+String.format("mil%-3d",id)+" ===================================================================", Attribute.BRIGHT_GREEN_TEXT()) );
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
                out.println(colorize(prefix+"\tEstablished...........: "+ MyCalendar.getDate(day0)));
                out.println(colorize(prefix+"\tProduces..............: "+product.getName()+" ("+product.getShortname()+")"));
                out.println(colorize(prefix+"\tResource multiplier...: "+String.format("%.2f",getResourceMultiplier()*100)+"%"));
                out.println(colorize(prefix+"\tTotal units produced..: "+getQuantity()+"\t("+MIC_produced+" MIC)"));
                out.println(colorize(prefix+"\tEfficiency............: "+efficiency));
                out.println(colorize(prefix+"\tSteel.................: "+steelSupplied+"/"+getSteelNeeded()));
                out.println(colorize(prefix+"\tAluminium.............: "+aluminiumSupplied+"/"+getAluminiumNeeded()));
                out.println(colorize(prefix+"\tChromium..............: "+chromiumSupplied+"/"+getChromiumNeeded()));
                out.println(colorize(prefix+"\tTungsten..............: "+tungstenSupplied+"/"+getTungstenNeeded()));
                out.println(colorize(prefix+"\tRubber................: "+rubberSupplied+"/"+getRubberNeeded()));
            }
        }
    }

    /// Generate the name of this factory, given a town name
    @Override
    public void generateName() {
        Random rand = new Random();

        String townName = location==null ? "offmap" : location.getTownName(rand);

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

    /// Create military factory , which may or may not have been unlocked already
    public MilitaryFactory(State location, boolean underConstruction,double efficiencyBase)
    {
        super(location,underConstruction);
        efficiency = efficiencyBase;
        this.closed = false;
        this.MIC_produced = 0;
        product=null;
        productName="null";
        efficiencyCap=0.5;
        factoryOutput=0.0;

        //These things will be set by the nation
        steelSupplied=0;
        aluminiumSupplied=0;
        tungstenSupplied=0;
        chromiumSupplied=0;
        rubberSupplied=0;
        statisticsDepartment = new DataLogger();
        openDepartment();
        statisticsDepartment.addPlot("Factory "+id+" Production stats ("+name+") in "+location.getName(),Map.of("Factory Output", Color.GREEN,"Efficiency Cap",new Color(64,255,255,127),"Efficiency",Color.CYAN,"Resource Multiplier",Color.RED),"%",false);
        statisticsDepartment.addPlot("Factory "+id+" Production ("+name+") in "+location.getName(),Map.of("Daily MIC", new Color(0,255,128),"Total MIC",Color.GREEN,"Units produced",Color.RED),"MIC/units",false);
    }

    /// For use by JSon deserializer
    public MilitaryFactory()
    {
        super(null,false);
        efficiency = 0.1;
        this.closed = false;
        this.MIC_produced = 0;
        product=null;
        productName="null";
        efficiencyCap=0.5;
        factoryOutput=0.0;

        //These things will be set by the nation
        steelSupplied=0;
        aluminiumSupplied=0;
        tungstenSupplied=0;
        chromiumSupplied=0;
        rubberSupplied=0;
        statisticsDepartment = new DataLogger();
        openDepartment();
        statisticsDepartment.addPlot("Production stats",Map.of("Factory Output", Color.GREEN,"Efficiency Cap",new Color(64,255,255,127),"Efficiency",Color.CYAN,"Resource Multiplier",Color.RED),"%",false);
        statisticsDepartment.addPlot("Production",Map.of("Daily MIC", new Color(0,255,128),"Total MIC",Color.GREEN,"Units produced",Color.RED),"MIC/units",false);
    }

    /// Current efficiency of the production line
    public double getEfficiency() {return efficiency;}

    /// How many MIC points have we ever made?
    public double getMIC_produced() {return MIC_produced;}

    /// how much stuff have we ever made?
    ///Round down, something which has not been finished has not been produced
    public long getQuantity() {return product==null?0:(long)(MIC_produced/product.getUnit_cost());}

    /// how much stuff did we make today
    /// The difference between today and yesterday
    public long getDailyQuantity() {return product==null?0:(long)(MIC_produced/product.getUnit_cost())-(long)((MIC_produced-MIC_gain)/product.getUnit_cost());}

    /// A name for identifying this factory
    public String getName() {return name;}

    /// Is this factory operating?
    @Override
    public boolean operating() {return !underConstruction && !closed;}

    /// Step forward 1 day, and calculate production following the instructions on the wiki: [...](https://hoi4.paradoxwikis.com/Production#Production_lines)
    /// @param efficiencyCap highest possible efficiency in this nation, efficiency is how much of the 4.5 MIC per day base output we can access
    /// @param factoryOutput national % bonus output, will be further modified by resource penalties
    public void update(double efficiencyCap, double factoryOutput, int day) {
        if (day> statisticsDepartment.getDay()+1)
        {
            MIC_gain=0;
            //Have department collect no-production data until we are caught up
            for (int i = statisticsDepartment.getDay(); i+1 < day; i++)
                statisticsDepartment.log();
        }

        if (product!=null && operating()) {
            this.factoryOutput=factoryOutput;
            this.efficiencyCap=efficiencyCap;
            //Daily production can never become negative, (as long as striking workers don't start running the assembly lines backwards)
            MIC_gain = Math.max(4.5 * (1 + factoryOutput) * efficiency * getResourceMultiplier(), 0);
            MIC_produced += MIC_gain;
            //Apply changes in efficiency afterward, as described on the Wiki
            //If we are close to our cap, innovation becomes hard, and if we have to waste our creative energies looking for alternate suppliers we don't improve as fast
            double efficiencyGain = 0.001 * getResourceMultiplier()*efficiencyCap * efficiencyCap / efficiency;
            efficiency = Math.clamp(efficiency+efficiencyGain, 0, efficiencyCap);
        }
        else
        {
            MIC_gain=0.0;
            MIC_produced =0.0;
        }
        statisticsDepartment.log();
    }

    @Override
    public MilitaryFactory clone() {
        MilitaryFactory clone = (MilitaryFactory) super.clone();
        clone.CIC_invested=CIC_invested;

        //Keep the same location, the state is responsible for moving me to a clone of it, if it is cloned
        clone.location=location;

        clone.name=name;
        clone.underConstruction=underConstruction;
        clone.closed=closed;
        clone.efficiency=efficiency;
        clone.MIC_produced=MIC_produced;
        //The product can not be modified by the factory, so I just copy the reference
        clone.product=product;
        clone.productName=productName;

        clone.steelSupplied     =steelSupplied;
        clone.aluminiumSupplied =aluminiumSupplied;
        clone.tungstenSupplied  =tungstenSupplied;
        clone.chromiumSupplied  =chromiumSupplied;
        clone.rubberSupplied    =rubberSupplied;
        clone.statisticsDepartment=statisticsDepartment.clone();
        clone.id=id;
        clone.openDepartment();
        return clone;
    }
    @Override
    public String getBuildingName(){return "Military factory";}

    public void show(boolean save) {
        statisticsDepartment.show(save,name+", "+location.getName()+" ");
    }
}
