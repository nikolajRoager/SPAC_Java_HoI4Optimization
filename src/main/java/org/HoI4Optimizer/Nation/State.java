package org.HoI4Optimizer.Nation;

import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.bytebuddy.utility.nullability.NeverNull;
import org.HoI4Optimizer.Building.Building;
import org.HoI4Optimizer.Building.SharedBuilding.CivilianFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Factory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Refinery;
import org.HoI4Optimizer.Building.stateBuilding.Infrastructure;
import org.HoI4Optimizer.Building.stateBuilding.StateBuilding;
import org.HoI4Optimizer.Nation.Event.StateEvent;
import org.HoI4Optimizer.NationalConstants.*;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.diogonunes.jcolor.Ansi.colorize;

/// States are the building block of nations, they contain all industry, and slots for building industry
/// In this simulation, all stateEvents are "core territory", non-cores are simulated (like Danzig) are simulated by adding factories by event, when they become available
public class State implements Cloneable {
    private int id;
    /// The type decides my base building slots, can not be changed (the Swedish focus tree is the ONLY way to increase slots, and only in Sweden, so I am going to ignore it)
    private stateType type;
    /// Building slots from events
    private int extraBuildingSlots;

    /// Name of the state, often the name of the capital city
    private String name;


    /// Is this connected to capital network, gives flat 20% boost to mines
    private boolean supplyhub=false;

    private int base_oil=0;
    private int base_rubber=0;
    private int base_steel=0;
    private int base_aluminium=0;
    private int base_tungsten=0;
    private int base_chromium=0;

    private boolean noBuilding=false;

    @NeverNull
    private Infrastructure infrastructure;

    /// Military Factories in this state
    @NeverNull
    private List<MilitaryFactory> militaryFactories;
    /// Civilian Factories in this state
    @NeverNull
    private List<CivilianFactory> civilianFactories;
    /// Rerineries in this state
    @NeverNull
    private List<Refinery> refineries;

    public State() {
        refineries = new ArrayList<>();
        militaryFactories = new ArrayList<>();
        civilianFactories = new ArrayList<>();
        name="temp";
        infrastructure=new Infrastructure(this,0,false);
    }

    public void build (Building building) throws IllegalArgumentException
    {
        if (building instanceof StateBuilding)
            throw new IllegalArgumentException(" State buildings like "+building.getBuildingName()+" can not be build, upgrade instead!");
        else if (building instanceof MilitaryFactory)
            militaryFactories.add((MilitaryFactory)building);
        else if (building instanceof CivilianFactory)
            civilianFactories.add((CivilianFactory)building);
        else if (building instanceof Refinery)
            refineries.add((Refinery)building);

    }

    ///Oil in state Rounded down (this causes some loss, if two stateEvents produce 1.5 oil, we get 2 oil in total just like in game)
    public int getOil()
    {
        //No refineries, they make fuel directly
        return (int)(base_oil*(supplyhub?1.2:1.0)*(1+infrastructure.getLevel()*0.2));
    }
    ///rubber in state Rounded down
    public int getRubber(int rubber_per_refinery)
    {
        //Synthetic rubber is unaffected by infrastructure
        return (int)(base_rubber*(supplyhub?1.2:1.0)*(1+infrastructure.getLevel()*0.2))+refineries.size()*rubber_per_refinery;
    }
    ///Aluminium in state
    public int getAluminium()
    {
        return (int)(base_aluminium*(supplyhub?1.2:1.0)*(1+infrastructure.getLevel()*0.2));
    }
    ///Steel in state
    public int getSteel()
    {
        return (int)(base_steel*(supplyhub?1.2:1.0)*(1+infrastructure.getLevel()*0.2));
    }
    ///Tungsten in state
    public int getTungsten()
    {
        return (int)(base_tungsten*(supplyhub?1.2:1.0)*(1+infrastructure.getLevel()*0.2));
    }
    ///Chromium in state
    public int getChromium()
    {
        return (int)(base_chromium*(supplyhub?1.2:1.0)*(1+infrastructure.getLevel()*0.2));
    }

    /// Get steel if we upgrade infrastructure
    public int getNextSteel()
    {
        return (int)(
                base_steel*(supplyhub?1.2:1.0)*(1+(1+infrastructure.getLevel())*0.2)
        );
    }
    /// Get rubber if we upgrade infrastructure
    public int getNextRubber()
    {
        return (int)(
                base_rubber*(supplyhub?1.2:1.0)*(1+(1+infrastructure.getLevel())*0.2)
        );
    }
    /// Get chromium if we upgrade infrastructure
    public int getNextChromium()
    {
        return (int)(
                base_chromium*(supplyhub?1.2:1.0)*(1+(1+infrastructure.getLevel())*0.2)
        );
    }
    /// Get tungsten if we upgrade infrastructure
    public int getNextTungsten()
    {
        return (int)(
                base_tungsten*(supplyhub?1.2:1.0)*(1+(1+infrastructure.getLevel())*0.2)
        );
    }
    /// Get aluminium if we upgrade infrastructure
    public int getNextAluminium()
    {
        return (int)(
                base_aluminium*(supplyhub?1.2:1.0)*(1+(1+infrastructure.getLevel())*0.2)
        );
    }
    /// Get oil if we upgrade infrastructure
    public int getNextOil()
    {
        return (int)(
                base_oil*(supplyhub?1.2:1.0)*(1+(1+infrastructure.getLevel())*0.2)
        );
    }

    public void setBase_oil(int base_oil) {
        this.base_oil = base_oil;
    }

    public void setSupplyhub(boolean supplyhub) {
        this.supplyhub = supplyhub;
    }

    public void setBase_aluminium(int base_aluminium) {
        this.base_aluminium = base_aluminium;
    }

    public void setBase_chromium(int base_chromium) {
        this.base_chromium = base_chromium;
    }

    public void setBase_rubber(int base_rubber) {
        this.base_rubber = base_rubber;
    }

    public void setBase_steel(int base_steel) {
        this.base_steel = base_steel;
    }

    public void setBase_tungsten(int base_tungsten) {
        this.base_tungsten = base_tungsten;
    }

    public Infrastructure getInfrastructure()
    {
        return infrastructure;
    }
    public int getInfrastructureLevel() {
        return infrastructure.getLevel();}

    /// Used by json deserializer
    public void setInfrastructure(int level)
    {
        infrastructure.setLevel(level,0);
    }

    public void setInfrastructure(int level,int day)
    {
        infrastructure.setLevel(level,day);
    }

    /// Is it possible to
    public boolean canUpgradeInfrastructure() {return infrastructure.getLevel()<Infrastructure.maxLevel && !infrastructure.getUnderConstruction() && !noBuilding;}

    /// Names used to auto-generating names of factories
    private List<String> townNames;
    /// Get our factories as a list
    public List<String> getTownNames() {
        return townNames;
    }

    public String getTownName(Random rand) {
        return townNames.get(rand.nextInt(townNames.size()));
    }

    public void setTownNames(List<String> names) {
        this.townNames = names;
    }
    /// Get our factories as a list
    public void setFactories(List<Factory> factories) {
        militaryFactories=new ArrayList<>();
        civilianFactories=new ArrayList<>();
        refineries=new ArrayList<>();
        for (var f : factories)
        {
            if (f instanceof MilitaryFactory)
                militaryFactories.add((MilitaryFactory)f);
            if (f instanceof CivilianFactory)
                civilianFactories.add((CivilianFactory)f);
            if (f instanceof Refinery)
                refineries.add((Refinery) f);
        }
    }
    /// Get the civilian factories in this state directly from this state right now
    public List<CivilianFactory> getCivilianFactories()
    {
        return civilianFactories;
    }

    /// Get the military factories in this state directly
    public List<MilitaryFactory> getMilitaryFactories() {
        return militaryFactories;
    }
    /// Get the refineries in this state directly
    public List<Refinery> getRefineries() {
        return refineries;
    }

    public int getExtraBuildingSlots() {
        return extraBuildingSlots;
    }
    public void setExtraBuildingSlots(int extraBuildingSlots) {
        this.extraBuildingSlots = extraBuildingSlots;
    }
    public void setName(String name) {
        this.name = name;
    }
    public void setType(stateType type) {
        this.type = type;
    }
    public int getId() {
        return id;
    }
    public void setId(int id) {
        this.id = id;
    }

    /// What is the type of this state? controls the number of building slots
    public stateType getType() {return type;}
    /// What is this place called?
    public String getName() {return name;}

    /// Get the number of building slots in this state, given the level of industry technology
    public int getBuildingSlots(double building_slot_bonus)
    {
        //Each level of industry technology gives +20% to base number from type, this does not affect event slots
        return Math.clamp(extraBuildingSlots + (int)(type.getBuildingSlots()*(1+building_slot_bonus)),0,25);
    }

    /// Get number of free slots
    public int getFreeSlots(double building_slot_bonus)
    {
        return getBuildingSlots(building_slot_bonus)+extraBuildingSlots-militaryFactories.size()-civilianFactories.size()-refineries.size();
    }

    ///load a list of stateEvents from json
    public static List<State> loadState(String filePath, NationalSetup setup) throws IOException {
        //For randomly generating names
        Random rand = new Random();

        ObjectMapper objectMapper = new ObjectMapper();
        var List = objectMapper.readValue(new File(filePath), new TypeReference<List<State>>() {});
        //Now loop through every state, and update the factory names
        for (var S : List)
        {
            for (var f : S.militaryFactories)
            {
                // Update military factory to produce the right thing
                //if the productName is null (was not set in json file), then this factory can be freely assigned to anything
                //Short circuit logic means
                if (!((MilitaryFactory) f).getProductName().equals("null"))
                {
                    //What we should produce
                    var product = setup.getEquipment().get(((MilitaryFactory) f).getProductName());
                    if (product==null)
                        throw new RuntimeException("Loaded factory producing "+((MilitaryFactory) f).getProductName()+" but that does not exist!");
                    else
                        ((MilitaryFactory) f).setProduct(product,0);
                }
                f.setLocation(S);
            }

            for (var f : S.civilianFactories)
            {
                f.setLocation(S);
            }
            for (var f : S.refineries)
            {
                f.setLocation(S);
            }
        }
        return List;
    }
    /// Give a written report of the state
    public void printReport(PrintStream out,double building_slot_bonus,int rubber_per_refinery,String prefix)
    {
        out.println(colorize(prefix+"~~State of "+name+"~~", Attribute.BOLD()) );
        out.println(colorize(prefix+"\tinfrastructure "+infrastructure.getLevel()+"/5",Attribute.YELLOW_TEXT()));
        out.println(colorize(prefix+"\tBuilding slots unlocked "+getBuildingSlots(building_slot_bonus) +" ("+type.getBuildingSlots()+" from "+type.toString()+(extraBuildingSlots==0?"":" "+extraBuildingSlots+" from events")+(building_slot_bonus==0?"":String.format("+%.2f from tech",building_slot_bonus*100))+")",Attribute.GREEN_TEXT()));
        out.println(colorize(prefix+"\tCan build civilian factories: "+(canBuildCivilianFactory(building_slot_bonus)?"yes":"no"),Attribute.BRIGHT_YELLOW_TEXT()));
        out.println(colorize(prefix+"\tCan build military factories: "+(canBuildMilitaryFactory(building_slot_bonus)?"yes":"no"),Attribute.BRIGHT_GREEN_TEXT()));
        out.println(colorize(prefix+"\tCan build refinery factories: "+(canBuildRefineryFactory(building_slot_bonus)?"yes":"no"),Attribute.BLUE_TEXT()));
        if (!civilianFactories.isEmpty())
        {
            out.println(colorize(prefix+"\t"+civilianFactories.size()+" Civilian Factories:",Attribute.BRIGHT_YELLOW_TEXT()));
        }
        if (!refineries.isEmpty()) {
            out.println(colorize(prefix +"\t"+ refineries.size() + " Refineries:",Attribute.BLUE_TEXT()));
        }
        if (!militaryFactories.isEmpty()) {
            out.println(colorize(prefix +"\t"+ militaryFactories.size() + " Military Factories:",Attribute.BRIGHT_GREEN_TEXT()));
        }

        out.print(colorize(String.format(prefix+"\t                     %4s %9s %6s %8s %5s %8s%n", "Oil", "Aluminium", "Rubber", "Tungsten","Steel","Chromium"),Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD()));
        //Resources available
        out.print(colorize(String.format(prefix+"\tBase resources......:%3s %6s %8s %7s %6s %7s%n",  base_oil, base_aluminium, base_rubber, base_tungsten, base_steel, base_chromium),Attribute.GREEN_TEXT()));
        out.print(colorize(String.format(prefix+"\tWith infrastructure.:%3s %6s %8s %7s %6s %7s%n", getOil() , getAluminium(), getRubber(rubber_per_refinery), getTungsten(), getSteel(), getChromium()),Attribute.GREEN_TEXT()));
    }

    /// Start construction of a new civilian factory
    public Infrastructure buildInfrastructure() throws RuntimeException
    {
        if (!canUpgradeInfrastructure())
            throw new RuntimeException("Can not upgrade infrastructure in "+name);
        //Create a new factory, with a randomly generated town name
        Random rand = new Random();
        infrastructure = new Infrastructure(this,infrastructure.getLevel()+1,true);
        return infrastructure;
    }
    /// Start construction of a new civilian factory
    public CivilianFactory buildCivilianFactory(double building_slot_bonus) throws RuntimeException
    {
        if (!canBuildCivilianFactory(building_slot_bonus))
            throw new RuntimeException("Can not build civilian factory in "+name);
        //Create a new factory, with a randomly generated town name
        Random rand = new Random();
        var New = new CivilianFactory(this,true);
        civilianFactories.add(New);
        return New;
    }
    /// Start construction of a new military factory
    public MilitaryFactory buildMilitaryFactory(double building_slot_bonus) throws RuntimeException
    {
        if (!canBuildMilitaryFactory(building_slot_bonus))
            throw new RuntimeException("Can not build military factory in "+name);
        //Create a new factory, with a randomly generated town name
        Random rand = new Random();
        var New = new MilitaryFactory(this,true);
        militaryFactories.add(New);
        return New;
    }
    /// Start construction of a new refinery
    public Refinery buildRefinery(double building_slot_bonus) throws RuntimeException
    {
        if (!canBuildRefineryFactory(building_slot_bonus))
            throw new RuntimeException("Can not build refinery in "+name);
        //Create a new factory, with a randomly generated town name
        Random rand = new Random();
        var New = new Refinery(this,true);
        refineries.add(New);
        return New;
    }

    public boolean canBuildCivilianFactory(double building_slot_bonus) {
        //check if there are no civilian factories in construction
        for (var f : civilianFactories)
            if (f.getUnderConstruction())
            {
                return false;
            }
        //If then, see if we have free slots
        return getFreeSlots(building_slot_bonus)>0 &&!noBuilding;
    }

    public boolean canBuildMilitaryFactory(double building_slot_bonus) {
        //check if there are no military factories in construction
        for (var f : militaryFactories)
            if (f.getUnderConstruction())
            {
                return false;
            }
        //If then, see if we have free slots
        return getFreeSlots(building_slot_bonus)>0 &&!noBuilding;
    }

    public boolean canBuildRefineryFactory(double building_slot_bonus) {
        //check if there are no refineries factories in construction
        for (var f : refineries)
            if (f.getUnderConstruction())
            {
                return false;
            }
        //If then, see if we have free slots
        return getFreeSlots(building_slot_bonus)>0 && !noBuilding && refineries.size()<3/*At most 3 refineries per state, a hard limit in game*/;
    }

    @Override
    public State clone() {
        try {
            State clone = (State) super.clone();
            clone.id                =id;
            clone.type              =type;
            clone.extraBuildingSlots=extraBuildingSlots;
            clone.name              =name;
            clone.infrastructure    =infrastructure.clone();
            clone.infrastructure.setLocation(this);
            clone.supplyhub         =supplyhub;
            clone.base_oil          =base_oil;
            clone.base_rubber       =base_rubber;
            clone.base_steel        =base_steel;
            clone.base_aluminium    =base_aluminium;
            clone.base_tungsten     =base_tungsten;
            clone.base_chromium     =base_chromium;

            //Make lists of factories
            clone.militaryFactories =new ArrayList<>();//militaryFactories;
            clone.civilianFactories =new ArrayList<>();//civilianFactories;
            clone.refineries        =new ArrayList<>();//refineries;

            for (var f : militaryFactories)
            {
                var New =f.clone();
                New.setLocation(clone);
                clone.militaryFactories.add(New);
            }
            for (var f : civilianFactories)
            {
                var New =f.clone();
                New.setLocation(clone);
                clone.civilianFactories.add(New);
            }
            for (var f : refineries)
            {
                var New =f.clone();
                New.setLocation(clone);
                clone.refineries.add(New);
            }

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    void setNoBuilding(boolean noBuilding) {
        this.noBuilding = noBuilding;
    }

    /// Adepply events, and optionally print what happens
    /// @param out Where to print to, use null if you don't want anything printed
    /// @param event event to apply
    void apply(StateEvent event,NationalProperties properties, PrintStream out, List<Refinery> nationalRefineries, List<CivilianFactory> nationalCivs, List<MilitaryFactory> nationalMils,int day)
    {

        Attribute GoodOutcome= Attribute.GREEN_TEXT();
        Attribute BadOutcome=Attribute.RED_TEXT();
        Attribute MiddlingOutcome=Attribute.WHITE_TEXT();

        final Attribute outcomeColour = event.number() > 0 ? GoodOutcome : BadOutcome;
        switch (event.modify())
        {
            case null -> {
                if (out!=null)
                {
                    out.println(colorize("    No effect",MiddlingOutcome));
                }
            }
            case noBuilding -> {
                noBuilding = event.number()>0;
                if (out!=null)
                {
                    if (noBuilding)
                        out.println(colorize("    New building projects in "+name+" is now allowed",GoodOutcome));
                    else
                        out.println(colorize("    New building projects in "+name+" is now blocked",BadOutcome));
                }
            }
            case Oil -> {
                base_oil=Math.max(base_oil+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" oil in "+name+" total is now "+base_oil, outcomeColour));
            }
            case Steel -> {
                base_steel=Math.max(base_steel+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" steel in "+name+" total is now "+base_steel, outcomeColour));
            }
            case Rubber -> {
                base_rubber=Math.max(base_rubber+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" rubber in "+name+" total is now "+base_rubber, outcomeColour));
            }
            case Chromium -> {
                base_chromium=Math.max(base_chromium+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" chromium in "+name+" total is now "+base_chromium, outcomeColour));
            }
            case Tungsten -> {
                base_tungsten=Math.max(base_tungsten+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" tungsten in "+name+" total is now "+base_tungsten, outcomeColour));
            }
            case Aluminium -> {
                base_aluminium=Math.max(base_aluminium+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" aluminium in "+name+" total is now "+base_aluminium, outcomeColour));
            }
            case Slot -> {
                extraBuildingSlots=Math.max(extraBuildingSlots+event.number(),0);
                if (out!=null)
                    out.println(colorize("    Add "+event.number()+" building slots in "+name+" total is now "+extraBuildingSlots, outcomeColour));
            }
            case Refinery -> {
                if (getFreeSlots(properties.getBuildingSlotBonus())>0) {
                    int n = Math.min(getFreeSlots(properties.getBuildingSlotBonus()),event.number());
                    for (int i = 0; i < n; ++i) {
                        var New = new Refinery(this, false/*Instantly construct*/);
                        refineries.add(New);
                        nationalRefineries.add(New);
                    }
                    if (out != null)
                        if (n==event.number())
                            out.println(colorize("    Add " + event.number() + " refineries in " + name + " total is now " + refineries.size(), outcomeColour));
                        else
                            out.println(colorize("    Added "+ n +"/"+ event.number() + " refineries in " + name + " (due to lack of slots) total is now " + refineries.size(), outcomeColour));
                }
                else if (out != null)
                    out.println(colorize("    Event to add " + event.number() + " refineries in " + name + " failed due to lack of building slots!", Attribute.BRIGHT_RED_TEXT(),Attribute.BOLD()));
            }
            case Civilian -> {
                if (getFreeSlots(properties.getBuildingSlotBonus())>0) {
                    int n = Math.min(getFreeSlots(properties.getBuildingSlotBonus()),event.number());
                    for (int i = 0; i < n; ++i) {
                        var New = new CivilianFactory(this, false/*Instantly construct*/);
                        civilianFactories.add(New);
                        nationalCivs.add(New);
                    }if (out != null)
                        if (n==event.number())
                            out.println(colorize("    Add " + event.number() + " civilian factories in " + name + " total is now " + civilianFactories.size(), outcomeColour));
                        else
                            out.println(colorize("    Added "+ n +"/"+ event.number() + " civilian factories in " + name + " (due to lack of slots) total is now " + civilianFactories.size(), outcomeColour));
                }
                else if (out != null)
                    out.println(colorize("    Event to add " + event.number() + " civilian factories in " + name + " failed due to lack of building slots!", Attribute.BRIGHT_RED_TEXT(),Attribute.BOLD()));
            }
            case Military -> {
                if (getFreeSlots(properties.getBuildingSlotBonus())>0) {
                    int n = Math.min(getFreeSlots(properties.getBuildingSlotBonus()),event.number());
                    for (int i = 0; i < n; ++i) {
                        var New = new MilitaryFactory(this, false/*Instantly construct*/);
                        militaryFactories.add(New);
                        nationalMils.add(New);
                    }
                    if (out != null)
                        if (n==event.number())
                            out.println(colorize("    Add " + event.number() + " military factories in " + name + " total is now " + militaryFactories.size(), outcomeColour));
                        else
                            out.println(colorize("    Added "+ n +"/"+ event.number() + " military factories in " + name + " (due to lack of slots) total is now " + militaryFactories.size(), outcomeColour));
                }
                else if (out != null)
                    out.println(colorize("    Event to add " + event.number() + " military factories in " + name + " failed due to lack of building slots!", Attribute.BRIGHT_RED_TEXT(),Attribute.BOLD()));
            }
            case Infrastructure -> {
                if (infrastructure.canUpgrade())
                {
                    infrastructure.setLevel(infrastructure.getLevel()+ event.number(),day);
                    if (out!=null)
                    {
                        out.println(colorize("    Build "+event.number()+" level of infrastructure in "+name+" total is now "+infrastructure.getLevel(), outcomeColour));
                    }

                }
            }
        }
    }
}
