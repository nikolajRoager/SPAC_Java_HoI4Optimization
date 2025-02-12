package org.HoI4Optimizer.Nation;
import com.diogonunes.jcolor.Attribute;
import net.bytebuddy.utility.nullability.NeverNull;
import org.HoI4Optimizer.Building.SharedBuilding.CivilianFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Factory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Refinery;
import org.HoI4Optimizer.Building.stateBuilding.StateBuilding;
import org.HoI4Optimizer.MyCalendar;
import org.HoI4Optimizer.Building.*;
import org.HoI4Optimizer.MyPlotter.DataLogger;
import org.HoI4Optimizer.Nation.Event.Events;
import org.HoI4Optimizer.Nation.decision.BuildDecision;
import org.HoI4Optimizer.NationalConstants.Equipment;
import org.HoI4Optimizer.NationalConstants.NationalSetup;
import java.awt.*;
import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static com.diogonunes.jcolor.Ansi.colorize;

/// The state of a nation, at some point in time, it is essentially a "save" we can return to
public class NationState  implements Cloneable
{

    ///A little struct containing a possible trade policy
    /// @param rubberDeals How many civilian factories are we trading away for rubber?
    /// @param steelDeals How many civilian factories are we trading away for steel?
    /// @param oilDeals How many civilian factories are we trading away for oil?
    /// @param chromiumDeals How many civilian factories are we trading away for chromium?
    /// @param aluminiumDeals How many civilian factories are we trading away for aluminium?
    /// @param tungstenDeals How many civilian factories are we trading away for tungsten?
    private record TradePolicy(
            int rubberDeals,
            int steelDeals,
            int tungstenDeals,
            int chromiumDeals,
            int aluminiumDeals,
            int oilDeals)
    {
        /// Empty trade policy
        public TradePolicy()
        {
            this(0,0,0,0,0,0);
        }
        public int steelImport(){return steelDeals*8;}
        public int rubberImport(){return rubberDeals*8;}
        public int oilImport(){return oilDeals*8;}
        public int aluminiumImport(){return aluminiumDeals*8;}
        public int tungstenImport(){return tungstenDeals*8;}
        public int chromiumImport(){return chromiumDeals*8;}

        public int cost (){return rubberDeals+steelDeals+tungstenDeals+chromiumDeals+aluminiumDeals+oilDeals;}

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            TradePolicy that = (TradePolicy) o;
            return oilDeals == that.oilDeals && steelDeals == that.steelDeals && rubberDeals == that.rubberDeals && tungstenDeals == that.tungstenDeals && chromiumDeals == that.chromiumDeals && aluminiumDeals == that.aluminiumDeals;
        }

        @Override
        public int hashCode() {
            return Objects.hash(rubberDeals, steelDeals, tungstenDeals, chromiumDeals, aluminiumDeals, oilDeals);
        }
    }

    @NeverNull
    TradePolicy tradePolicy;


    /// The organ tasked with writing down every little detail of this nation
    @NeverNull
    private DataLogger instituteOfStatistics;

    /// What time are we looking at now?
    private int day=0;

    /// A very long list of modifiers
    private NationalProperties properties;

    /// A construction line for some building, with between 0 and 15 civilian factories assigned
    private record constructionLine
            (
                    Building building
            )
    {
        public int daysRemaining(int civs, NationalProperties properties)
        {
            double CIC_per_day = 5*Math.min(civs,15)*(1+0.2*building.getLocation().getInfrastructureLevel())*(1+properties.getConstruction_speed()
                    +(building instanceof MilitaryFactory ? properties.getMil_construction_speed_bonus() :0.0)
                    +(building instanceof CivilianFactory ? properties.getCiv_construction_speed_bonus() :0.0));
            double remainingCost = Building.getCost(building.getMyType())-building.getCIC_invested();
            return (int)(remainingCost/CIC_per_day+1.0/*Round up*/);
        }
    }
    
    /// All equipment ever produced, once upon a time, the developers used Unsigned longs for this, players very quickly found numerous ways to overflow that
    @NeverNull
    private Map<Equipment,Long> nationalStockpile;

    /// Reference to the Setup for this nation, has effects to modify properties above on certain days
    private NationalSetup setup;

    private String name;
    private List<State> states;

    /// All states in this nation, including all factories
    public List<State> getStates() {return states;}
    //Refineries don't need to be references from the nation directly
    /// References to all military factories in all core states
    public List<MilitaryFactory> militaryFactories;

    /// References to all civilian factories in all core states
    public List<CivilianFactory> civilianFactories;

    /// References to all civilian factories in all core states
    public List<Refinery> refineries;

    public int getDay() {return day;}

    public void setStates(List<State> states) {this.states = states;}

    private List<constructionLine> constructionLines;

    private int nationalAluminium=0;
    private int nationalOil=0;
    private int nationalSteel=0;
    private int nationalRubber=0;
    private int nationalChromium=0;
    private int nationalTungsten=0;

    /// The things we can build this day
    @NeverNull
    private List<BuildDecision> buildingDecisions;
    /// The list of all the different ways we can re-assign un-assigned factories
    @NeverNull
    private List<Map<Equipment,Integer>> factoryReassignmentDecisions;

    /// The list of possible trade policies we can adopt
    @NeverNull
    private List<TradePolicy> tradeDecisions;



    /// Load setup from a folder, assumed to be start of the simulation
    public NationState(String countryName,NationalSetup setup) throws IOException {
        //Start not importing anything
        tradePolicy=new TradePolicy();
        try {states=State.loadState(Paths.get(countryName,"States.json").toString(),setup);}
        catch (IOException e) {throw new IOException("Error loading states of "+countryName+":"+e.getMessage());}

        try {properties=NationalProperties.loadProperties(Paths.get(countryName,"Properties.json").toString());}
        catch (IOException e) {throw new IOException("Error loading properties of "+countryName+":"+e.getMessage());}

        // Set up references to all military and civilian factories loaded
        civilianFactories=new ArrayList<>();
        militaryFactories=new ArrayList<>();
        refineries=new ArrayList<>();

        constructionLines=new ArrayList<>();

        //For incrementing the unique id of each factory
        AtomicInteger milID=new AtomicInteger(0);
        AtomicInteger civID=new AtomicInteger(0);
        AtomicInteger refID=new AtomicInteger(0);

        for (var s : states)
        {
            s.getCivilianFactories().forEach(f->f.setId(civID.getAndIncrement()));
            s.getMilitaryFactories().forEach(f->f.setId(milID.getAndIncrement()));
            s.getRefineries().forEach(f->f.setId(refID.getAndIncrement()));

            civilianFactories.addAll(s.getCivilianFactories());
            militaryFactories.addAll(s.getMilitaryFactories());
            refineries.addAll(s.getRefineries());
        }


        //Shuffle the factories, so the factories producing consumer goods are all over the nation
        Collections.shuffle(civilianFactories);
        Collections.shuffle(militaryFactories);
        Collections.shuffle(refineries);


        name=countryName;
        this.setup=setup;

        //Check if we have decisions the first day (we most certainly have)
        buildingDecisions = new ArrayList<>();
        factoryReassignmentDecisions = new ArrayList<>();
        tradeDecisions = new ArrayList<>();
        updateDecisionsResourcesAndFactories();

        instituteOfStatistics = new DataLogger();
        openInstituteOfStatistics();

        //This is where I think it makes the most sense to tell the logger what to plot together, since this is the class which knows what the things mean
        instituteOfStatistics.addPlot("Construction stats",Map.of("Political Stability",Color.GRAY,"Military factory construction speed",Color.GREEN,"Civilian factory construction speed",Color.YELLOW,"Construction speed",Color.BLUE,"Consumer goods ratio",Color.ORANGE),"%",false);
        instituteOfStatistics.addPlot("Production stats",Map.of("Factory Output",Color.GREEN,"Efficiency Cap",Color.CYAN),"%",false);
        instituteOfStatistics.addPlot("Popularity of ideologies",Map.of("Non-aligned",Color.GRAY,"Democracy",Color.BLUE,"Communism",Color.RED,"Fascism (Falangism)",new Color(115, 57, 0)),"%",true);

        instituteOfStatistics.addPlot("Political stability",Map.of("Political Stability",Color.GRAY,"Base Stability",Color.BLACK,"Permanent stability modifiers",Color.GREEN,"Government support",Color.BLUE),"%",false);

        instituteOfStatistics.addPlot("Consumer goods",Map.of("Base consumer goods ratio",Color.YELLOW,"Consumer goods multiplier",Color.GREEN,"Consumer goods ratio",Color.ORANGE,"Political Stability",Color.GRAY),"%",false);

        instituteOfStatistics.addPlot("Industry",Map.of("Civilian factories",Color.YELLOW,"Military factories",Color.GREEN,"Chemical refineries",Color.BLUE),"Factories",false);

        instituteOfStatistics.addPlot("Military Factories",setup.getEquipment().values().stream().map(e->e.getShortname()+" factories").toList(),"Factories",true);

        instituteOfStatistics.addPlot("Resource balance",Map.of("Steel",Color.CYAN,"Aluminium",Color.LIGHT_GRAY,"Tungsten",Color.DARK_GRAY,"Chromium",Color.magenta,"Rubber",Color.BLUE,"Oil",Color.BLACK),"units/day",false);

        instituteOfStatistics.addPlot("National Stockpile",setup.getEquipment().values().stream().map(Equipment::getShortname).toList(),"units",false);

        nationalStockpile =setup.getEquipment().values().stream().collect(Collectors.toMap(e->e, Equipment::getInitial));
    }

    /// Open the institute of statistics, so we can keep track of literally everything
    private void openInstituteOfStatistics()
    {
        instituteOfStatistics.setLog("Political Stability",properties::getStability, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Base Stability",properties::getStability, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Permanent stability modifiers",properties::getStability, DataLogger.logDataType.PositivePercent);

        instituteOfStatistics.setLog("Factory Output",properties::getFactoryOutput, DataLogger.logDataType.Percent);
        instituteOfStatistics.setLog("Efficiency Cap",properties::getEfficiency_cap, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Resource gain bonus",properties::getResource_gain_bonus, DataLogger.logDataType.PositiveUnboundedPercent);
        instituteOfStatistics.setLog("Construction speed",properties::getConstruction_speed, DataLogger.logDataType.Percent);
        instituteOfStatistics.setLog("Military factory construction speed",
                ()->{return properties.getMil_construction_speed_bonus()+properties.getConstruction_speed();}
                , DataLogger.logDataType.Percent);
        instituteOfStatistics.setLog("Civilian factory construction speed",
                ()->{return properties.getCiv_construction_speed_bonus()+properties.getConstruction_speed();}
                , DataLogger.logDataType.Percent);

        instituteOfStatistics.setLog("Consumer goods ratio",properties::getConsumer_goods_ratio, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Base consumer goods ratio",properties::getBase_consumer_goods_ratio, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Consumer goods multiplier",properties::getBase_consumer_goods_multiplier, DataLogger.logDataType.PositivePercent);

        instituteOfStatistics.setLog("percent of resources exported",properties::getResources_to_market, DataLogger.logDataType.PositivePercent);

        instituteOfStatistics.setLog("Non-aligned"       ,properties::getAutocracy_support, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Democracy"         ,properties::getDemocracy_support, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Communism"         ,properties::getCommunism_support, DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Fascism (Falangism)"           ,properties::getFascism_support  , DataLogger.logDataType.PositivePercent);
        instituteOfStatistics.setLog("Government support",properties::getRulingParty_support, DataLogger.logDataType.PositivePercent);

        instituteOfStatistics.setLog("Steel",()->nationalSteel, DataLogger.logDataType.Integer);
        instituteOfStatistics.setLog("Rubber",()->nationalRubber, DataLogger.logDataType.Integer);
        instituteOfStatistics.setLog("Oil",()->nationalOil, DataLogger.logDataType.Integer);
        instituteOfStatistics.setLog("Aluminium",()->nationalAluminium, DataLogger.logDataType.Integer);
        instituteOfStatistics.setLog("Tungsten",()->nationalTungsten, DataLogger.logDataType.Integer);
        instituteOfStatistics.setLog("Chromium",()->nationalChromium, DataLogger.logDataType.Integer);

        instituteOfStatistics.setLog("Civilian factories",
                ()->
                {
                    int civs =0;
                    for (var f : civilianFactories)
                        if (!f.getUnderConstruction())
                            civs++;
                    return civs;
                }
                , DataLogger.logDataType.PositiveInteger);
        instituteOfStatistics.setLog("Military factories",
                ()->
                {
                    int mils=0;
                    for (var f : militaryFactories)
                        if (!f.getUnderConstruction())
                            mils++;
                    return mils;
                }
                , DataLogger.logDataType.PositiveInteger);
        instituteOfStatistics.setLog("Chemical refineries",
                ()->
                {
                    int refs=0;
                    for (var f : refineries)
                        if (!f.getUnderConstruction())
                            refs++;
                    return refs;
                }
                , DataLogger.logDataType.PositiveInteger);
        //Add all equipment factories numbers in bulk, append the word factories after each name
        instituteOfStatistics.setLog(setup.getEquipment().values().stream().map(e->e.getShortname()+" factories").collect(Collectors.toSet()),
                ()->
                {
                    Map<String,Double> counts=new HashMap<>();
                    for (var f : militaryFactories)
                        if (!f.getUnderConstruction() && f.getProduct()!=null) {
                            //Increment the count of this factory
                                counts.put(f.getProduct().getShortname()+" factories",counts.getOrDefault(f.getProduct().getShortname()+" factories",0.0)+1.0);
                        }
                    return counts;
                }
                , DataLogger.logDataType.PositiveInteger);

        //Add equipment stockpiles
        instituteOfStatistics.setLog(setup.getEquipment().values().stream().map(Equipment::getShortname).collect(Collectors.toSet()),
                ()->
                {
                    Map<String,Double> counts=new HashMap<>();
                    for (var entry : nationalStockpile.entrySet())
                    {
                        counts.put(entry.getKey().getShortname(),(double)entry.getValue());
                    }
                    return counts;
                }
                , DataLogger.logDataType.PositiveInteger);
    }

    public void displayPlots(boolean save)
    {
        instituteOfStatistics.show(save);
    }
    public void displayPlots(int factoryId,boolean save)
    {
        if (factoryId<militaryFactories.size() && factoryId>=0)
            militaryFactories.get(factoryId).show(save);
        else
            militaryFactories.forEach(f->f.show(save));
    }

    /// Print a bar-plot with a percentage
    private static String printBarPlot(int percent, Attribute barAtr)
    {
        StringBuilder Out= new StringBuilder(colorize((percent < 10 ? " " : "") + (percent < 100 ? " " : "")+ percent + "%:", Attribute.BLACK_TEXT(),barAtr));

        for (; percent>=1;percent-=1)
        {
            Out.append(colorize(" ",barAtr));
        }
        return Out.toString();
    }

    /// Print a positive modifier as a percentage bonus (green if positive, red if negative)
    private static String printGoodModifier(double modifier)
    {
        if (modifier>-0.01 && modifier<0.01)
            return colorize("   0%",Attribute.WHITE_TEXT());
        if (modifier>0)
            return colorize((modifier<1?" ":"")+(modifier<.1?" ":"")+"+"+(int)(100*modifier)+"%",Attribute.GREEN_TEXT());
        else
            return colorize((modifier>-1?" ":"")+(modifier>-.1?" ":"")+(int)(100*modifier)+"%",Attribute.RED_TEXT());
    }

    /// Print a negative modifier as a percentage bonus (red if positive, green if negative)
    private static String printBadModifier(double modifier)
    {
        if (modifier>-0.01 && modifier<0.01)
            return colorize("   0%",Attribute.WHITE_TEXT());
        if (modifier>0)
            return colorize((modifier<1?" ":"")+(modifier<.1?" ":"")+"+"+(int)(100*modifier)+"%",Attribute.RED_TEXT());
        else
            return colorize((modifier>-1?" ":"")+(modifier>-.1?" ":"")+(int)(100*modifier)+"%",Attribute.GREEN_TEXT());
    }

    public void applyDecision(int decId, PrintStream out) throws RuntimeException
    {
        if (buildingDecisions.size()+factoryReassignmentDecisions.size()+tradeDecisions.size()<decId)
            throw new RuntimeException("Target decision does not exist");
        if (buildingDecisions.size()>decId) {
            var decision = buildingDecisions.get(decId);
            //Build something new
            if (decision.type() == BuildDecision.Type.build) {
                decision.location().build(decision.building());
                //These adds are a little long, what I do here is I set the id to the index they are at in the list when added
                //That way they can correctly display the id you need to call to load them
                if (decision.building() instanceof Refinery)
                    refineries.add((Refinery) ((Factory) decision.building()).setIdAndGet(refineries.size()));
                else if (decision.building() instanceof CivilianFactory)
                    civilianFactories.add((CivilianFactory)((Factory) decision.building()).setIdAndGet(civilianFactories.size()));
                else if (decision.building() instanceof MilitaryFactory)
                    militaryFactories.add((MilitaryFactory) ((Factory) decision.building()).setIdAndGet(militaryFactories.size()));
                constructionLines.add(new constructionLine(decision.building()));
                out.println(colorize("==Started building "+constructionLines.getLast().building.getBuildingName()+"==",Attribute.YELLOW_TEXT()));
                int finish = constructionLines.getLast().daysRemaining(getUnassignedCivs()-15*(constructionLines.size()-1),properties);
                out.println(colorize(constructionLines.getLast().building.getName()+" in "+constructionLines.getLast().building.getLocation().getName()+" approximate days remaining: "+finish,Attribute.BRIGHT_YELLOW_TEXT())+colorize(" ("+ MyCalendar.getDate(finish+day)+")",Attribute.WHITE_TEXT()));
            }
            if (decision.type() == BuildDecision.Type.upgrade) {
                ((StateBuilding) decision.building()).upgrade();
                constructionLines.add(new constructionLine(decision.building()));
                out.println(colorize("==Started upgrading==",Attribute.YELLOW_TEXT()));
                int finish = constructionLines.getLast().daysRemaining(getUnassignedCivs()-15*(constructionLines.size()-1),properties);
                out.println(colorize(constructionLines.getLast().building.getName()+" in "+constructionLines.getLast().building.getLocation().getName()+" approximate days remaining: "+finish,Attribute.BRIGHT_YELLOW_TEXT())+colorize(" ("+ MyCalendar.getDate(finish+day)+")",Attribute.WHITE_TEXT()));
            }
        }
        else if (buildingDecisions.size()+factoryReassignmentDecisions.size()>decId)
        {
            var assignmentDecision = factoryReassignmentDecisions.get(decId-buildingDecisions.size());
            out.println(colorize("==Set up production lines==",Attribute.GREEN_TEXT()));
            int factoryI=0;
            for (var EqNumber : assignmentDecision.entrySet())
            {
                while (EqNumber.getValue()>0)
                {
                    //Loop through the list and add to the unassigned factories
                    var f = militaryFactories.get(factoryI);
                    if (f.getProduct()==null) {
                        f.setProduct(EqNumber.getKey(),day);
                        EqNumber.setValue(EqNumber.getValue()-1);
                        out.println(colorize(EqNumber.getKey().getName()+" at "+f.getName()+" in "+f.getLocation().getName(),Attribute.BRIGHT_WHITE_TEXT()));
                    }else {
                        factoryI++;
                    }
                }
            }
        }
        else
            tradePolicy=tradeDecisions.get(decId-buildingDecisions.size()-factoryReassignmentDecisions.size());
        updateDecisionsResourcesAndFactories();
    }


    /// Print an integer which is green if positive, red if negative, otherwise gray
    private String goodInt(int i)
    {
        return colorize(String.format("%5d",i),
                i>0? Attribute.GREEN_TEXT() : (i==0? Attribute.WHITE_TEXT() : Attribute.RED_TEXT())
                );
    }

    /// Give a written report of the nation
    /// @param Output where we print to (for example System.out)
    /// @param showProductionLines Show military factories with all their production lines
    /// @param showConstructionLines Show ongoing construction
    /// @param showFactories Also print all civilian and military factories and refineries
    /// @param showResources Show detailed breakdown of all resources
    /// @param showStates Show detailed breakdown of all states
    public void printReport(PrintStream Output,boolean showResources, boolean showProductionLines,boolean showConstructionLines , boolean showFactories,boolean showStates,boolean showDecisions)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out=new PrintStream(outputStream);

        out.println(colorize("==Status report for "+name+" at "+ MyCalendar.getDate(day)+"==",Attribute.BOLD()));
        out.println(colorize("--Political report---"));
        double stab = properties.getStability();
        out.println(colorize("\tSocial stability..............:"+(int)(stab*100)+"%",(stab<0.3334?Attribute.BRIGHT_RED_TEXT():(stab>0.6667?Attribute.BRIGHT_GREEN_TEXT():Attribute.BRIGHT_YELLOW_TEXT()))));

        char Agrowth = properties.getAutocracy_growth()>0.001?'^':(properties.getAutocracy_growth()<-0.001?'v':'=');
        char Cgrowth = properties.getCommunism_growth()>0.001?'^':(properties.getAutocracy_growth()<-0.001?'v':'=');
        char Dgrowth = properties.getDemocracy_growth()>0.001?'^':(properties.getAutocracy_growth()<-0.001?'v':'=');
        char Fgrowth = properties.getFascism_growth()>0.001?'^':(properties.getAutocracy_growth()<-0.001?'v':'=');

        switch(properties.getRulingParty())
        {
            case Nonaligned ->
            {
                out.println(colorize("\tNon-aligned Government support......:"+Agrowth+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.WHITE_BACK()),Attribute.BOLD()       ,Attribute.WHITE_TEXT()     ));
                out.println(colorize("\tDemocratic opposition support.......:"+Dgrowth+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK())                         ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tCommunist opposition support........:"+Cgrowth+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK())                   ,Attribute.RED_TEXT()));
                out.println(colorize("\tFascist opposition support..........:"+Fgrowth+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK())                       ,Attribute.YELLOW_TEXT()    ));
            }
            case Democratic->
            {
                out.println(colorize("\tDemocratic Government support.......:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK()),Attribute.BOLD()        ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tNon-aligned opposition support......:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.WHITE_BACK())                        ,Attribute.WHITE_TEXT()     ));
                out.println(colorize("\tCommunist opposition support........:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK())                   ,Attribute.RED_TEXT()));
                out.println(colorize("\tFascist opposition support..........:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK())                       ,Attribute.YELLOW_TEXT()    ));
            }
            case Communist ->
            {
                out.println(colorize("\tCommunist Government support........:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK()),Attribute.BOLD() ,Attribute.RED_TEXT()));
                out.println(colorize("\tDemocratic opposition support.......:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK())                        ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tNon-aligned opposition support......:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.WHITE_BACK())                       ,Attribute.WHITE_TEXT()     ));
                out.println(colorize("\tFascist opposition support..........:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK())                      ,Attribute.YELLOW_TEXT()    ));
            }
            case Fascist->
            {
                out.println(colorize("\tFascist Government support..........:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK()),Attribute.BOLD(),Attribute.YELLOW_TEXT() ));
                out.println(colorize("\tDemocratic opposition support.......:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK())                ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tNon-aligned opposition support......:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.RED_BACK())          ,Attribute.RED_TEXT()));
                out.println(colorize("\tCommunist opposition support........:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK())          ,Attribute.RED_TEXT()));
            }
        }

        int civs=0;
        int mils=0;
        int refs=0;
        for (var f : militaryFactories)
            if (!f.getUnderConstruction())
                mils++;
        for (var f : civilianFactories)
            if (!f.getUnderConstruction())
                civs++;
        for (var f : refineries)
            if (!f.getUnderConstruction())
                refs++;

        int factories =civs+mils;


        double cg = properties.getConsumer_goods_ratio();
        int cg_factories = (int)(factories*cg);

        out.println(colorize("--Industrial report--"));
        out.println(colorize("\t...Military Sector...",Attribute.ITALIC(),Attribute.BRIGHT_GREEN_TEXT()));
        out.println(colorize("\t\t Factory output bonus....................:",Attribute.BRIGHT_GREEN_TEXT())+ printGoodModifier(properties.getFactoryOutput()));
        out.println(colorize("\t\t Efficiency cap..........................:",Attribute.BRIGHT_GREEN_TEXT())+ printGoodModifier(properties.getEfficiency_cap()));
        out.println(colorize("\t\t Operational Military factories..........: "+mils,Attribute.BRIGHT_GREEN_TEXT()));
        if (showFactories || showProductionLines)
            for (var f : militaryFactories)
            {
                f.printReport(out,"\t\t\t");
            }

        if (showProductionLines)
        {
            out.println(colorize("--National Stockpile--"));

            int nameLength = nationalStockpile.keySet().stream().mapToInt(e->e.getName().length()).max().orElse(20);
            for (var pile : nationalStockpile.entrySet())
            {
                String ename = pile.getKey().getName();
                out.println(colorize("\t\t"+ename+(".".repeat(Math.max(0, nameLength + 5 - ename.length())))+":"+String.format("%8d",pile.getValue()) ,Attribute.GREEN_TEXT()));
            }
        }

        out.println(colorize("\t...Resource and Fuel sector...",Attribute.ITALIC(),Attribute.BLUE_TEXT()));
        out.println(colorize("\t\tResource gain bonus......................:",Attribute.BLUE_TEXT())+printGoodModifier(properties.getResource_gain_bonus()));
        out.println(colorize("\t\tResources exported.......................:",Attribute.BLUE_TEXT())+printBadModifier(properties.getResources_to_market()));
        out.println(colorize("\t\tFuel gain per oil........................:",Attribute.BLUE_TEXT())+printGoodModifier(properties.getNatural_fuel_bonus()));
        out.println(colorize("\t\tSynthetic Fuel per refinery..............:",Attribute.BLUE_TEXT())+printGoodModifier(properties.getRefinery_fuel_bonus()));
        out.println(colorize("\t\tSynthetic Rubber per refinery............: ",Attribute.BLUE_TEXT())+properties.getRubber_per_refineries());

        if (refineries.isEmpty())
            out.println(colorize("\t\tNo synthetic refineries owned!", Attribute.BLUE_TEXT()));
        else
            out.println(colorize("\t\tOperational refineries...............:"+refs,Attribute.BLUE_TEXT()));
        if (showFactories)
            for (var f : refineries)
            {
                f.printReport(out,"\t\t\t");
            }

        //Table header
        if (showResources)
        {
            out.println(colorize(("\t\t                           Oil |  Aluminium |  Rubber |  Tungsten |  Steel |  Chromium"),Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD()));
        }
        //Resources available (we re-calculate it here, to display every step)
        int oil=0;
        int steel=0;
        int aluminium=0;
        int rubber=0;
        int chromium=0;
        int tungsten=0;

        for (var s : states)
        {
            //Rounds down!!! this is stupid, but game accurate
            oil+=s.getOil();
            steel+=s.getSteel();
            aluminium+=s.getAluminium();
            rubber+=s.getRubber(properties.getRubber_per_refineries());
            chromium+=s.getChromium();
            tungsten+=s.getTungsten();
        }


        //Seperator used to print table
        String sep= colorize("|",Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD());

        if (showResources) {

            out.println(("\t\tProduced in states:      "+goodInt(oil)+" "+sep+"      "+goodInt(aluminium)+" "+sep+"   "+goodInt(rubber)+" "+sep+"     "+goodInt(tungsten)+" "+sep+"  "+goodInt(steel)+" "+sep+"     "+goodInt(chromium)));
        }

        //THIS IS A ROUNDING MESS, but it is game accurate!
        int newrubber=(int)Math.round(rubber*(1+properties.getResource_gain_bonus()));
        int newoil=(int)Math.round(oil*(1+properties.getResource_gain_bonus()));
        int newaluminium=(int)Math.round(aluminium*(1+properties.getResource_gain_bonus()));
        int newchromium=(int)Math.round(chromium*(1+properties.getResource_gain_bonus()));
        int newsteel=(int)Math.round(steel*(1+properties.getResource_gain_bonus()));
        int newtungsten=(int)Math.round(tungsten*(1+properties.getResource_gain_bonus()));

        if (showResources)
            out.println(("\t\tExcavation tech bonus:   "+goodInt(newoil-oil)+" "+sep+"      "+goodInt(newaluminium-aluminium)+" "+sep+"   "+goodInt(newrubber-rubber)+" "+sep+"     "+goodInt(newtungsten-tungsten)+" "+sep+"  "+goodInt(newsteel-steel)+" "+sep+"     "+goodInt(newchromium-chromium)));

        double rtm = properties.getResources_to_market();

        if (showResources)
            out.println(("\t\tCivilian industry/export:"+goodInt(-(int)(newoil*rtm))+" "+sep+"      "+goodInt(-(int)(newaluminium*rtm))+" "+sep+"   "+goodInt(-(int)(newrubber*rtm))+" "+sep+"     "+goodInt(-(int)(newtungsten*rtm))+" "+sep+"  "+goodInt(-(int)(newsteel*rtm))+" "+sep+"     "+goodInt(-(int)(newchromium*rtm))));
        if (showResources)
            out.println(("\t\tImported:                "+goodInt(tradePolicy.oilImport())+" "+sep+"      "+goodInt(tradePolicy.aluminiumImport())+" "+sep+"   "+goodInt(tradePolicy.rubberImport())+" "+sep+"     "+goodInt(tradePolicy.tungstenImport())+" "+sep+"  "+goodInt(tradePolicy.steelImport())+" "+sep+"     "+goodInt(tradePolicy.chromiumImport())));

        int needSteel    =militaryFactories.stream().mapToInt(MilitaryFactory::getSteelNeeded    ).sum();
        int needAluminium=militaryFactories.stream().mapToInt(MilitaryFactory::getAluminiumNeeded).sum();
        int needRubber   =militaryFactories.stream().mapToInt(MilitaryFactory::getRubberNeeded   ).sum();
        int needTungsten =militaryFactories.stream().mapToInt(MilitaryFactory::getTungstenNeeded ).sum();
        int needChromium =militaryFactories.stream().mapToInt(MilitaryFactory::getChromiumNeeded ).sum();

        //Just get what we have left over, by checking the difference between our calculated data, and the thing actually left over in the national stockpile (any deficit must have been used for production)
        if (showResources)
            out.println(("\t\tMilitary industry:        "+colorize("   -",Attribute.WHITE_TEXT())+" "+sep+"      "+goodInt(-needAluminium)+" "+sep+"   "+goodInt(-needRubber)+" "+sep+"     "+goodInt(-needTungsten)+" "+sep+"  "+goodInt(-needSteel)+" "+sep+"     "+goodInt(-needChromium)));
        if (showResources)
            out.println(("\t\tBalance:                 "+goodInt(nationalOil)+" "+sep+"      "+goodInt(nationalAluminium)+" "+sep+"   "+goodInt(nationalRubber)+" "+sep+"     "+goodInt(nationalTungsten)+" "+sep+"  "+goodInt(nationalSteel)+" "+sep+"     "+goodInt(nationalChromium)));
        else
        {
            //Print short summary
            out.println(("Resource balance: Oil: "+goodInt(nationalOil)+", Aluminium: "+goodInt(nationalAluminium)+", rubber: "+goodInt(nationalRubber)+", tungsten: "+goodInt(nationalTungsten)+", steel: "+goodInt(nationalSteel)+", and chromium "+goodInt(nationalChromium)));
        }

        double baseFuel =properties.getBase_fuel();
        double naturalFuel =properties.getBase_fuel()*(1+properties.getNatural_fuel_bonus())*nationalOil;
        double refineryFuel=properties.getBase_fuel()*(1+properties.getRefinery_fuel_bonus())*refineries.size();

        out.println(colorize("\t\tFuel gain per day........................:",Attribute.BLUE_TEXT())+String.format("%.2f",baseFuel+naturalFuel+refineryFuel));

        //Missing fuel capacity

        out.println(colorize("\t...Construction sector...",Attribute.ITALIC(),Attribute.BRIGHT_YELLOW_TEXT()));
        out.println(colorize("\t\tConstruction speed bonus.................:",Attribute.BRIGHT_YELLOW_TEXT())+ printGoodModifier(properties.getConstruction_speed()));
        out.println(colorize("\t\tCivilian factory construction bonus......:",Attribute.BRIGHT_YELLOW_TEXT())+ printGoodModifier(properties.getCiv_construction_speed_bonus()));
        out.println(colorize("\t\tMilitary factory construction bonus......:",Attribute.BRIGHT_YELLOW_TEXT())+ printGoodModifier(properties.getMil_construction_speed_bonus()));



        out.println(colorize("\t\tOperational military + civilian factories: "+civs,Attribute.BRIGHT_YELLOW_TEXT())+" +"+colorize(" "+mils,Attribute.BRIGHT_GREEN_TEXT())+" = "+factories);
        out.println(colorize("\t\tRequired for consumer goods..............: ",Attribute.BRIGHT_YELLOW_TEXT())+ printBadModifier(cg)+" * "+factories+ " = "+colorize(cg_factories+"",Attribute.RED_TEXT()));
        out.println(colorize("\t\tRequired for special projects............: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize(""+properties.getSpecial_projects_civs(),Attribute.WHITE_TEXT()));
        int exportGoods=tradePolicy.cost();
        out.println(colorize("\t\tProducing export goods...................: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize(""+exportGoods,Attribute.WHITE_TEXT()));
        int unassignedCivs = getUnassignedCivs();
        out.println(colorize("\t\tRemains available for construction.......: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize("+"+unassignedCivs,Attribute.BRIGHT_YELLOW_TEXT()));
        if (showFactories) {
            out.println(colorize("\t\t Our " + civilianFactories.size() + " civilian factories:", Attribute.BRIGHT_YELLOW_TEXT()));
            for (var f : civilianFactories) {
                f.printReport(out,"\t\t\t");
            }
        }


        out.println();
        out.println(colorize("\t\tOngoing construction projects: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize("+"+(constructionLines.size()),Attribute.BRIGHT_YELLOW_TEXT()));

        if (showConstructionLines) {
            for (var line : constructionLines) {
                //Calculate assigned factories directly

                int finish = line.daysRemaining(unassignedCivs,properties);
                out.println(colorize("\t\t\tBuilding " + line.building.getName() + " in " + line.building.getLocation().getName() + " production: " + String.format("%.2f",line.building.getCIC_invested())  + "/" + String.format("%.2f",Building.getCost(line.building.getMyType())) + " CIC ~"+finish+" days remaining Assigned factories " + Math.min(15, unassignedCivs) + "/15", (line.building.getMyType() == Building.type.Civilian ? Attribute.YELLOW_TEXT() : (line.building.getMyType() == Building.type.Military ? Attribute.GREEN_TEXT() : Attribute.BLUE_TEXT()))));
                unassignedCivs = Math.max(0, unassignedCivs - 15);
            }

            //If we have left over civs, round up to get number of construction lines we can start
            if (unassignedCivs > 0) {
                out.println(colorize("\t\t We have " + unassignedCivs + " unassigned civilian factories (" + (unassignedCivs + 14) / 15 + " construction lines)", Attribute.RED_TEXT(), Attribute.BOLD()));
            }
        }

        if (showStates) {
            out.println("--State report--");
            for (var S : states) {
                S.printReport(out, properties.getBuildingSlotBonus(), properties.getRubber_per_refineries(), "\t");
            }

        }

        if (showDecisions)
        {
            out.println("--Decision report--");
            out.println(colorize( buildingDecisions.size()+" building projects can be launched",Attribute.BRIGHT_YELLOW_TEXT()));
            for (int i = 0; i < buildingDecisions.size(); i++)
                buildingDecisions.get(i).display(i,out);
            out.println("More decisions may be available in-game, this list leaves out decisions which will have the same effect");

            for (int i = 0; i < factoryReassignmentDecisions.size(); i++)
            {
                out.println(colorize("Decision: "+(i+buildingDecisions.size()),Attribute.GREEN_TEXT()));
                out.println(colorize("  assign our "+getUnassignedMils()+" unassigned military factories to: ",Attribute.BRIGHT_GREEN_TEXT()));
                factoryReassignmentDecisions.get(i).forEach((E, f)->
                {
                    if (f>0)
                        out.println(colorize("\t"+E.getName()+" ("+E.getShortname()+"): "+f+" factories",Attribute.BRIGHT_GREEN_TEXT()));
                });
            }
            if (!tradeDecisions.isEmpty()) {
                for (int i = 0; i < tradeDecisions.size(); i++) {
                    out.println(colorize("Optional Decision: " + (i + buildingDecisions.size() + factoryReassignmentDecisions.size()), Attribute.BLUE_TEXT()));
                    out.println(colorize("  sign new trade policy, import " + tradeDecisions.get(i).rubberImport()+ " rubber, " + tradeDecisions.get(i).steelImport()+ " steel, " + tradeDecisions.get(i).aluminiumImport()+ " aluminium, " + tradeDecisions.get(i).tungstenImport()+ " tungsten, " + tradeDecisions.get(i).chromiumImport()+ " chromium for "+(tradeDecisions.get(i).cost()-tradePolicy.cost())+" extra civilian factories", Attribute.BRIGHT_BLUE_TEXT()));
                }
                out.println(colorize("Trade deals are optional, just keep stepping to keep the current trade policy Optional:", Attribute.BLUE_TEXT()));
                out.println(colorize("  sign new trade policy, import " + tradePolicy.rubberImport() + " rubber, " + tradePolicy.steelImport() + " steel, " + tradePolicy.aluminiumImport() + " aluminium, " + tradePolicy.tungstenImport() + " tungsten, " + tradePolicy.chromiumImport() + " chromium", Attribute.BRIGHT_BLUE_TEXT()));
            }
        }

        //Now turn the streams into List<String> so we can print them next to each other
        //This is AI generated code
        List<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new ByteArrayInputStream(outputStream.toByteArray())))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            //This should not happen
            throw new RuntimeException(e);
        }

        lines.forEach(Output::println);
    }

    @Override
    public NationState clone() {
        try {
            NationState clone = (NationState) super.clone();
            clone.name=name;
            clone.day=day;
            //This reference copy is fine, since it is not modified
            clone.setup=setup;
            clone.properties=properties.clone();
            //Create new lists, and put copies inside them
            clone.states=new ArrayList<>();
            clone.refineries=new ArrayList<>();
            clone.civilianFactories=new ArrayList<>();
            clone.militaryFactories=new ArrayList<>();
            clone.buildingDecisions =new ArrayList<>();
            //Cloning kills the construction lines
            clone.constructionLines=new ArrayList<>();

            for (var s : states)
            {
                var newState = s.clone();
                clone.states.add(newState);
                //The id should still work after copying
                clone.refineries.addAll(newState.getRefineries());
                clone.civilianFactories.addAll(newState.getCivilianFactories());
                clone.militaryFactories.addAll(newState.getMilitaryFactories());
            }
            //Auto-calculate decisions
            clone.updateDecisionsResourcesAndFactories();
            clone.nationalStockpile = setup.getEquipment().values().stream().collect(Collectors.toMap(e->e, Equipment::getInitial));

            //Hand over the institute of statistics
            clone.instituteOfStatistics=instituteOfStatistics.clone();
            //And make sure the new institute is looking at the right
            clone.openInstituteOfStatistics();

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /// Apply an event to my properties, or my states
    public void apply(Events theseEvents, PrintStream out) {
        if (out!=null)
        {
            out.println(colorize("==An event has happened==", Attribute.BOLD(), Attribute.BLUE_TEXT()));
            out.println(colorize(MyCalendar.getDate(day)+" (day "+day+")",Attribute.WHITE_TEXT()));
            out.println(colorize("    "+ theseEvents.name(),Attribute.ITALIC()));
        }
        if (theseEvents.propertyEvents()!=null)
            for (var propertyEvent : theseEvents.propertyEvents())
               properties.apply(propertyEvent,out);
        if (theseEvents.stateEvents()!=null)
            for (var stateEvent : theseEvents.stateEvents())
                states.get(stateEvent.stateID()).apply(stateEvent,properties,out,refineries,civilianFactories,militaryFactories,day);
        updateDecisionsResourcesAndFactories();
    }



    ///Get currently available non-optional decisions (Trade decisions are optional)
    public int getDecisions()
    {
        return buildingDecisions.size()+ factoryReassignmentDecisions.size();
    }

    /// Trade decisions are optional, they don't need to be taken, and can't be exhausted (if you sign a new policy, you instantly have the option of cancelling it)
    public int getTradeDecisions()
    {
        return tradeDecisions.size();
    }

    private void updateDecisionsResourcesAndFactories()
    {
        buildingDecisions = new ArrayList<>();
        factoryReassignmentDecisions = new ArrayList<>();
        tradeDecisions = new ArrayList<>();
        //Check for idling civilian factories
        int unassignedCivs = getUnassignedCivs()-constructionLines.size()*15;
        while (unassignedCivs > 0)
        {
            //Generate hash-maps of the different construction

            Map<Object,Integer> constructionDecisions = new HashMap<>();

            //Find all new buildings we can build
            for (State s : states)
            {
                //Add either refineries or civilian factories
                if (s.canBuildCivilianFactory(properties.getBuildingSlotBonus()))
                {
                    double CIC_per_day = 5*Math.min(unassignedCivs,15)*(1+0.2*s.getInfrastructureLevel())*(1+properties.getConstruction_speed()+
                            properties.getCiv_construction_speed_bonus());
                    double remainingCost = Building.getCost(Building.type.Civilian);
                    int days =(int)(remainingCost/CIC_per_day+1.0/*Round up*/);

                    Object myHash= Objects.hash('c',s.getInfrastructureLevel());
                    var dec =new BuildDecision(
                            s,
                            new CivilianFactory(s,true),
                            "Add +"+String.format("%.2f",100*properties.getConsumer_goods_ratio())+"% Civilian factory after consumer goods",
                            days);
                    tryAddDecision(constructionDecisions, s, myHash, dec);
                }
                if (s.canBuildRefineryFactory(properties.getBuildingSlotBonus()))
                {
                    double CIC_per_day = 5*Math.min(unassignedCivs,15)*(1+0.2*s.getInfrastructureLevel())*(1+properties.getConstruction_speed());
                    double remainingCost = Building.getCost(Building.type.Refinery);
                    int days =(int)(remainingCost/CIC_per_day+1.0/*Round up*/);

                    //Generate a unique hash with the effects I care about for refineries
                    Object myHash= Objects.hash('r',s.getInfrastructureLevel());
                    var dec = new BuildDecision(s,new Refinery(s,true),"Add +"+ (int)(properties.getRubber_per_refineries()*(1.0+properties.getResource_gain_bonus()))+" rubber on a national basis and "+String.format("%.2f",properties.getBase_fuel()*(1+properties.getRefinery_fuel_bonus()))+" fuel per day",days);
                    tryAddDecision(constructionDecisions, s, myHash, dec);
                }
                if (s.canBuildMilitaryFactory(properties.getBuildingSlotBonus()))
                {
                    double CIC_per_day = 5*Math.min(unassignedCivs,15)*(1+0.2*s.getInfrastructureLevel())*(1+properties.getConstruction_speed()+
                        properties.getMil_construction_speed_bonus());
                    double remainingCost = Building.getCost(Building.type.Military);
                    int days =(int)(remainingCost/CIC_per_day+1.0/*Round up*/);

                    //We can build a factory for any legal type of equipment
                    var Equipment=setup.getEquipment(day);
                    for (var eq : Equipment.values())
                    {
                        //Generate a unique hash with the effects I care about for military production
                        Object myHash= Objects.hash(eq,s.getInfrastructureLevel());
                        var dec = new BuildDecision(s,new MilitaryFactory(s,true),eq,"Add production-line of "+eq.getName()+" ("+eq.getShortname()+")",days);
                        tryAddDecision(constructionDecisions, s, myHash, dec);

                    }
                }
                if (s.canUpgradeInfrastructure())
                {
                    int dSteel    = (int)(s.getNextSteel()*(1+ properties.getResource_gain_bonus()))-(int)(s.getSteel()*(1+ properties.getResource_gain_bonus()));
                    int dAluminium= (int)(s.getNextAluminium()*(1+ properties.getResource_gain_bonus()))- (int)(s.getAluminium()*(1+ properties.getResource_gain_bonus()));
                    //Disregard from refineries, as infrastructure doesn't effect that
                    int dRubber   = (int)(s.getNextRubber()*(1+ properties.getResource_gain_bonus()))-(int)(s.getRubber(0)*(1+ properties.getResource_gain_bonus()));
                    int dTungsten = (int)(s.getNextTungsten()*(1+ properties.getResource_gain_bonus()))-(int)(s.getTungsten()*(1+ properties.getResource_gain_bonus()));
                    int dChromium = (int)(s.getNextChromium()*(1+ properties.getResource_gain_bonus()))-(int)(s.getChromium()*(1+ properties.getResource_gain_bonus()));
                    int dOil      = (int)(s.getNextOil()*(1+ properties.getResource_gain_bonus()))-(int)(s.getOil()*(1+ properties.getResource_gain_bonus()));

                    double CIC_per_day = 5*Math.min(unassignedCivs,15)*(1+0.2*s.getInfrastructureLevel())*(1+properties.getConstruction_speed());
                    double remainingCost = Building.getCost(Building.type.Infrastructure);
                    int days =(int)(remainingCost/CIC_per_day+1.0/*Round up*/);

                    //Generate a unique hash with the effects I care about, when it comes to building infrastructure
                    Object myHash= Objects.hash(dSteel,dAluminium,dRubber,dTungsten,dChromium,dOil,s.getInfrastructureLevel());
                    var dec =new BuildDecision(s.getInfrastructure(),"local construction speed bonus +"+(int)(s.getInfrastructureLevel()*20)+"% -> +"+(int)(s.getInfrastructureLevel()*20+20)+"% "+(dSteel>0?", +"+dSteel+" Steel":"")
                            +(dTungsten>0?", +"+dTungsten+" Tungsten":"")
                            +(dChromium>0?", +"+dChromium+" Chromium":"")
                            +(dAluminium>0?", +"+dAluminium+" Aluminium":"")
                            +(dRubber>0?", +"+dRubber+" Rubber":"")
                            +(dOil>0?", +"+dOil+" Oil":"")+(dSteel+dOil+dChromium+dAluminium+dTungsten+dRubber>0?" (subject to rounding errors)":""),
                            days);
                    //We should NOT add decision if dSteel, dAluminium, dRubber, dTungsten, dChromium, and dOil is the same, the decision type is the same, the decision.building() is the same, decision.location().getInfrastructureLevel() is the same
                    tryAddDecision(constructionDecisions, s, myHash, dec);
                }
            }
            unassignedCivs-=15;
        }

        //Check for idling military factories, knowing the game mechanics, I ASSUME that they are fungible, (i.e. they all start with same efficiency)
        int unassignedMils= getUnassignedMils();

        if (unassignedMils>0) {

            //Then generate all the ways we can assign these mils to the equipment, that will be (Products+Mils-1)!/((Mils-1)!*Products!)
            var availableEquipment = setup.getEquipment(day).values().stream().toList();


            List<Integer> EquipmentPermutations = new ArrayList<>();
            generateList(new ArrayList<>(), EquipmentPermutations, availableEquipment.size(), unassignedMils, 0, unassignedMils);

            //Loop through all permutations
            for (int i = 0; i < EquipmentPermutations.size(); i += availableEquipment.size()) {
                Map<Equipment, Integer> Reassignments = new HashMap<>();
                for (int j = 0; j < availableEquipment.size(); j++) {
                    Reassignments.put(availableEquipment.get(j), EquipmentPermutations.get(j + i));
                }
                factoryReassignmentDecisions.add(Reassignments);
            }
        }

        //Now, we want to update available trade decisions, at the same time we will tell the factories how many resources they have, and updating which civilian factories are labeled as exporting
        nationalAluminium=0;
        nationalOil=0;
        nationalSteel=0;
        nationalRubber=0;
        nationalChromium=0;
        nationalTungsten=0;


        //There are serious rounding errors with saving these as integers, but that is how it is done in game
        for (var s : states)
        {
            nationalOil+=s.getOil();
            nationalChromium+=s.getChromium();
            nationalSteel+=s.getSteel();
            nationalAluminium+=s.getAluminium();
            nationalRubber+=s.getRubber(properties.getRubber_per_refineries());
            nationalTungsten+=s.getTungsten();
        }


        //game accurate rounding mess
        nationalRubber   =(int)Math.round(nationalRubber*(1+properties.getResource_gain_bonus()));
        nationalOil      =(int)Math.round(nationalOil*(1+properties.getResource_gain_bonus()));
        nationalAluminium=(int)Math.round(nationalAluminium*(1+properties.getResource_gain_bonus()));
        nationalChromium =(int)Math.round(nationalChromium*(1+properties.getResource_gain_bonus()));
        nationalSteel    =(int)Math.round(nationalSteel*(1+properties.getResource_gain_bonus()));
        nationalTungsten =(int)Math.round(nationalTungsten*(1+properties.getResource_gain_bonus()));
        double rtm       = properties.getResources_to_market();


        nationalOil      -=(int)(rtm*nationalOil);
        nationalRubber   -=(int)(rtm*nationalRubber);
        nationalAluminium-=(int)(rtm*nationalAluminium);
        nationalTungsten -=(int)(rtm*nationalTungsten);
        nationalChromium -=(int)(rtm*nationalChromium);
        nationalSteel    -=(int)(rtm*nationalSteel);

        //First check what our deficit would be if we did not import:

        int ownRubber   =nationalRubber;
        int ownAluminium=nationalAluminium;
        int ownTungsten =nationalTungsten;
        int ownChromium =nationalChromium;
        int ownSteel    =nationalSteel;

        int factories =0;
        for (var f : militaryFactories)
            if (!f.getUnderConstruction()) {
                factories++;
                if (f.operating())
                {
                    ownRubber   -=f.getRubberNeeded();
                    ownAluminium-=f.getAluminiumNeeded();
                    ownTungsten -=f.getTungstenNeeded();
                    ownChromium -=f.getChromiumNeeded();
                    ownSteel    -=f.getSteelNeeded();
                }
            }


        //To see what contracts we can sign, we need to know how many civs we have after consumer goods
        int civs=0;

        for (var f : civilianFactories)
            if (!f.getUnderConstruction())
                civs++;
        factories+=civs;

        double cg = properties.getConsumer_goods_ratio();
        int cg_factories = (int)(factories*cg);

        int freeFactories= civs-cg_factories;

        //How many contracts do we need to solve all shortages
        int maxRubber   =(int)Math.ceil(Math.max(-ownRubber,0)/8.0);
        int maxAluminium=(int)Math.ceil(Math.max(-ownAluminium,0)/8.0);
        int maxTungsten =(int)Math.ceil(Math.max(-ownTungsten,0)/8.0);
        int maxChromium =(int)Math.ceil(Math.max(-ownChromium,0)/8.0);
        int maxSteel    =(int)Math.ceil(Math.max(-ownSteel,0)/8.0);


        //Try to add a new trade decision, the last will be the one which covers all our needs
        for (int r =0; r<=maxRubber; ++r)
            for (int a =0; a<=maxAluminium; ++a)
                for (int t =0; t<=maxTungsten; ++t)
                    for (int c =0; c<=maxChromium; ++c)
                        for (int s =0; s<=maxSteel; ++s) {
                            if (r + s + t + c + a <= freeFactories)
                            {
                                var tp =new TradePolicy(r, s, t, c, a, 0);
                                if (!tp.equals(tradePolicy)) {
                                    tradeDecisions.add(tp);
                                }
                            }
                        }

        int exportGoods;
        {
            //Then update our existing policy, if it can not be supported
            exportGoods = tradePolicy.cost();

            //If we do not have enough factories for trade, forcefully cancel trade agreements, this is not even a choice
            if (freeFactories < exportGoods) {
                int newExportGoods = 0;
                int rubber = Math.min(tradePolicy.rubberDeals, freeFactories);
                freeFactories -= rubber;
                newExportGoods += rubber;
                int steel = Math.min(tradePolicy.steelDeals, freeFactories);
                freeFactories -= steel;
                newExportGoods += steel;
                int chromium = Math.min(tradePolicy.chromiumDeals, freeFactories);
                freeFactories -= chromium;
                newExportGoods += chromium;
                int tungsten = Math.min(tradePolicy.tungstenDeals, freeFactories);
                freeFactories -= tungsten;
                newExportGoods += tungsten;
                int aluminium = Math.min(tradePolicy.aluminiumDeals, freeFactories);
                freeFactories -= aluminium;
                newExportGoods += aluminium;
                int oil = Math.min(tradePolicy.oilDeals, freeFactories);
                freeFactories -= oil;
                newExportGoods += oil;
                tradePolicy = new TradePolicy(rubber, steel, tungsten, chromium, aluminium, oil);
                exportGoods = newExportGoods;
            }
        }
        //Ok, now that we know our current policy is valid, and we know the player will
        nationalAluminium+=tradePolicy.aluminiumImport();
        nationalOil+=tradePolicy.oilImport();
        nationalSteel+=tradePolicy.steelImport();
        nationalRubber+=tradePolicy.rubberImport();
        nationalChromium+=tradePolicy.chromiumImport();
        nationalTungsten+=tradePolicy.tungstenImport();


        //Now actually update the resources, using the existing contract
        for (var f : militaryFactories)
            if (!f.getUnderConstruction()) {
                if (f.operating())
                {
                    nationalRubber   =f.addRubberSupplied   (nationalRubber);
                    nationalChromium =f.addChromiumSupplied (nationalChromium);
                    nationalAluminium=f.addAluminiumSupplied(nationalAluminium);
                    nationalTungsten =f.addTungstenSupplied (nationalTungsten);
                    nationalSteel    =f.addSteelSupplied    (nationalSteel);
                }
            }

        //And update what they say they produce
        for (int i = 0; i < cg_factories; ++i)
        {
            var f = civilianFactories.get(i);
            if (f.operating())
                f.assign(colorize("Consumer goods", Attribute.WHITE_TEXT()));
        }

        for (int i = cg_factories; i < cg_factories+exportGoods; ++i)
        {
            var f = civilianFactories.get(i);
            if (f.operating())
                f.assign(colorize("Export goods", Attribute.BRIGHT_BLUE_TEXT()));
        }

        for (int j = 0; j <constructionLines.size();++j) {
            var c = constructionLines.get(j);
            for (int i = cg_factories + exportGoods + j * 15; i < cg_factories + exportGoods + j * 15+15 && i < civilianFactories.size(); ++i) {
                var f = civilianFactories.get(i);
                if (f.operating())
                    f.assign(colorize(c.building.getName()+" in "+c.building.getLocation().getName(), Attribute.YELLOW_TEXT()));
            }
        }

        for (int i = cg_factories + exportGoods + constructionLines.size() * 15; i < civilianFactories.size(); ++i) {
            var f = civilianFactories.get(i);
            if (f.operating())
                f.assign(colorize("Awaiting orders", Attribute.BRIGHT_RED_TEXT()));
        }





    }

    /// AI generated code, to generate all possible Lists of positive integers, with length N and sum M, for distributing equipment between factories
    private static void generateList(List<Integer> current, List<Integer> Out, int N, int M, int sumSoFar, int maxValue) {
        if (current.size() == N) {
            if (sumSoFar == M) {
                Out.addAll(current);
            }
            return;
        }
        for (int i = 0; i <= maxValue; ++i) {
            if (sumSoFar + i <= M) {  // Pruning condition
                current.add(i);
                generateList(current, Out, N, M, sumSoFar + i, maxValue);
                current.removeLast();
            }
        }
    }

    /// Try to add a new building decision, but only if we do not have an equivalent decision already added
    /// @param constructionDecisions Map of existing decisions, the object is a hash generated from the effects, the integer points to its id in buildingDecisions for instance: if we already have an infrastructure upgrade from 2 to 3 which adds 5 steel, we will not add another
    private void tryAddDecision(Map<Object, Integer> constructionDecisions, State s, Object myHash, BuildDecision dec) {
        if (!constructionDecisions.containsKey(myHash))
        {
            //Only add the decision if it is unique
            constructionDecisions.put(myHash, buildingDecisions.size());
            buildingDecisions.add(dec);
        }
        else
        {
            //Or if this decision would upgrade infrastructure in more empty slots
            var ExistingVersionState = buildingDecisions.get(constructionDecisions.get(myHash)).location();
            if (ExistingVersionState.getFreeSlots(properties.getBuildingSlotBonus())< s.getFreeSlots(properties.getBuildingSlotBonus()))
            {
                buildingDecisions.set(constructionDecisions.get(myHash),dec);
            }
        }
    }

    ///Step forward a number of days, or until a decision is required, only print if some important event happened
    ///
    public void update(int days, PrintStream out)
    {
        //Days = 0, means we want to continue Until something happens
        for (int d = 0; d < days || days==0; d++) {
            //Start by checking if yesterday created some decisions for us to react to
            if (!buildingDecisions.isEmpty() || !factoryReassignmentDecisions.isEmpty())
                return;

            if (day> setup.getLastDay()) {
                out.println(colorize("We have reached the end of the simulation!",Attribute.RED_TEXT()));
                return;
            }

            ++day;

            // First step, update production
            for (var f : militaryFactories)
            {

                f.update(properties.getEfficiency_cap(),properties.getFactoryOutput(),day);
                if (f.getProduct()!=null)
                    nationalStockpile.put(f.getProduct(),nationalStockpile.get(f.getProduct())+f.getDailyQuantity());
            }


            // Now, update construction, this also updates the displayed products
            int unassignedCivs = getUnassignedCivs();

            for (int i = 0; i<constructionLines.size(); i++) {
                var c = constructionLines.get(i);
                int thisCivs =Math.min(unassignedCivs,15);
                //Add 5 daily CIC plus 20%,40%,60%,80% or 100% bonus per level infrastructure, plus construction speed bonus
                if (c.building.construct(5*thisCivs*(1+0.2*c.building.getLocation().getInfrastructureLevel())*(1+properties.getConstruction_speed()
                        +(c.building instanceof MilitaryFactory ? properties.getMil_construction_speed_bonus() :0.0)
                        +(c.building instanceof CivilianFactory ? properties.getCiv_construction_speed_bonus() :0.0)
                ),day))
                {
                    out.println(colorize("==Construction completed==",Attribute.BOLD(),Attribute.BRIGHT_YELLOW_TEXT()));
                    out.println(colorize(c.building.getName()+" now operational in "+c.building.getLocation().getName()+" on "+ MyCalendar.getDate(day),Attribute.WHITE_TEXT()));
                    constructionLines.remove(i);
                    //Step back down, so we don't skip over the next element
                    --i;
                }
                unassignedCivs-=thisCivs;
                //It is possible that a construction line will idle, if you suddenly lost available factories, (i.e. you started trading or got extra consumer goods due to events)
                if (unassignedCivs<=0)
                    break;
            }

            boolean eventHappened=false;
            //Apply events at the end of the day (I believe it is similar to the game)
            for (Events e : setup.getEvent(day))
            {
                eventHappened=true;
                apply(e,out);
            }

            //If days == 0, any event interrupts us
            if (eventHappened && days==0) {
                return;
            }

            //Weekly stability is applied the night between friday and saturday (as it is in game)
            if (day%7==2)//The day starts on a wednesday, so the first friday is day 2
                properties.setBase_stability(properties.getBase_stability()+ properties.getWeekly_stability());

            //I have not reverse engineered politics perfectly, but this is what I roughly believe happens:
            double newCommunism = properties.getCommunism_support()+getPartyGrowth(properties.getCommunism_support(),properties.getCommunism_growth());
            double newDemocracy = properties.getDemocracy_support()+getPartyGrowth(properties.getDemocracy_support(),properties.getDemocracy_growth());
            double newAutocracy = properties.getAutocracy_support()+getPartyGrowth(properties.getAutocracy_support(),properties.getAutocracy_growth());
            double newFascism   = properties.getFascism_support()  +getPartyGrowth(properties.getFascism_support()  ,properties.getFascism_growth());

            //What do we have too much of, after adding?
            double sum = newAutocracy+newCommunism+newDemocracy+newFascism;
            if (sum>1.0)
            {//Remove proportional to existing support (NOT REALLY TRUE, due to weird rounding errors the game FAVOURS FASCISM! (at least I HOPE those are rounding errors), but I could not figure out algorithm)
                //This is labeled (due to changes in other parties) in game
                newCommunism-=(sum-1.0)*newCommunism/sum;
                newDemocracy-=(sum-1.0)*newDemocracy/sum;
                newAutocracy-=(sum-1.0)*newAutocracy/sum;
                newFascism  -=(sum-1.0)*newFascism/sum;
            }
            else if (sum<=0)//If somehow, nobody believe in anything, everybody will believe in something, somehow
            {
                newDemocracy=0.25;
                newCommunism=0.25;
                newFascism=0.25;
                newAutocracy=0.25;
            }
            else
            {
                //Add inversely proportional to how much we have (unless we have 0)
                //Not really true, in reality the game favours fascism, but I could not reverse engineer the actual algorithm
                double invC =newCommunism==0?0.0:1.0/newCommunism;
                double invD =newDemocracy==0?0.0:1.0/newDemocracy;
                double invA =newAutocracy==0?0.0:1.0/newAutocracy;
                double invF =newFascism==0  ?0.0:1.0/newFascism;

                double invSum = invC+invD+invA+invF;
                newCommunism += (1.0 - sum) * invC / invSum;
                newDemocracy += (1.0 - sum) * invD / invSum;
                newAutocracy += (1.0 - sum) * invA / invSum;
                newFascism += (1.0 - sum) * invF / invSum;
            }

            //Set everything
            properties.setCommunism_support(newCommunism);
            properties.setDemocracy_support(newDemocracy);
            properties.setAutocracy_support(newAutocracy);
            properties.setFascism_support(newFascism);

            updateDecisionsResourcesAndFactories();

            //Tell the institute to do its thing
            instituteOfStatistics.log();
        }
        //No decisions, but we ended anyway
        return;
    }

    /// Get party popularity growth, scaled by existing support, I have not perfectly reverse engineered this
    private double getPartyGrowth(double support, double growth)
    {
        if (growth<0)
            return growth;
        else
        {
            if (support>0.7)
                return growth/7.0;
            else if (support>0.6)
                return growth/6.0;
            else if (support>0.5)
                return growth/5.0;
            else if (support>0.4)
                return growth/4.0;
            else if (support>0.3)
                return growth/3.0;
            else if (support>0.2)
                return  growth/2.0;
            else
                return growth;
        }
    }

    private int getUnassignedMils() {
        int mils=0;
        for (var f : militaryFactories)
            if (!f.getUnderConstruction() && f.getProduct()==null)
                ++mils;
        return mils;
    }

    /// Get our unassigned civilian factories, (the ones used on export, idling or construction)
    private int getUnassignedCivs() {
        int factories =0;
        int civs=0;
        for (var f : militaryFactories)
            if (!f.getUnderConstruction())
                factories++;
        for (var f : civilianFactories)
            if (!f.getUnderConstruction())
            {
                factories++;
                civs++;
            }
        double cg = properties.getConsumer_goods_ratio();
        int cg_factories = (int)(factories*cg);

        return Math.max(0,civs-properties.getSpecial_projects_civs()-cg_factories);
    }
}