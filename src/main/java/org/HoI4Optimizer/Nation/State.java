package org.HoI4Optimizer.Nation;

import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.HoI4Optimizer.Building.*;
import org.HoI4Optimizer.Building.SharedBuilding.CivilianFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Factory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Refinery;
import org.HoI4Optimizer.NationalConstants.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.diogonunes.jcolor.Ansi.colorize;

/// States are the building block of nations, they contain all industry, and slots for building industry
/// In this simulation, all states are "core territory", non-cores are simulated (like Danzig) are simulated by adding factories by event, when they become available
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

    private Infrastructure infrastructure;

    /// Military Factories in this state
    private List<MilitaryFactory> militaryFactories;
    /// Civilian Factories in this state
    private List<CivilianFactory> civilianFactories;
    /// Rerineries in this state
    private List<Refinery> refineries;

    ///Oil in state Rounded down (this causes some loss, if two states produce 1.5 oil, we get 2 oil in total just like in game)
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

    public int getInfrastructure() {
        return infrastructure.getLevel();}

    public void setInfrastructure(int level)
    {
        infrastructure=new Infrastructure(this,level,false);
    }

    /// Is it possible to
    public boolean canUpgradeInfrastructure() {return infrastructure.getLevel()<Infrastructure.maxLevel && !infrastructure.getUnderConstruction();}

    /// Names used to auto-generating names of factories
    private List<String> factoryNames;
    /// Get our factories as a list
    public List<String> getFactoryNames() {
        return factoryNames;
    }
    public void setFactoryNames(List<String> names) {
        this.factoryNames= names;
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
        return civilianFactories==null?new ArrayList<>():civilianFactories;
    }

    /// Get the military factories in this state directly
    public List<MilitaryFactory> getMilitaryFactories() {
        return militaryFactories==null?new ArrayList<>():militaryFactories;
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

    ///load a list of states from json
    public static List<State> loadState(String filePath, NationalSetup setup) throws IOException {
        //For randomly generating names
        Random rand = new Random();

        ObjectMapper objectMapper = new ObjectMapper();
        var List = objectMapper.readValue(new File(filePath), new TypeReference<List<State>>() {});
        //Now loop through every state, and update the factory names
        for (var S : List)
        {
            //Make sure that factories is not null
            if (S.militaryFactories==null)
                S.militaryFactories=new ArrayList<>();
            if (S.civilianFactories==null)
                S.civilianFactories=new ArrayList<>();
            if (S.refineries==null)
                S.refineries=new ArrayList<>();
            for (var f : S.militaryFactories)
            {
                // Update military factory to produce the right thing
                //if the productName is null (was not set in json file), then this factory can be freely assigned to anything
                //Short circuit logic means
                if (!((MilitaryFactory) f).getProductName().equals("null"))
                {
                    //What we should produce
                    var product = setup.getEquipmentList().get(((MilitaryFactory) f).getProductName());
                    if (product==null)
                        throw new RuntimeException("Loaded factory producing "+((MilitaryFactory) f).getProductName()+" but that does not exist!");
                    else
                        ((MilitaryFactory) f).setProduct(product);
                }
                f.setLocation(S);

                f.generateName(S.factoryNames.get(rand.nextInt(0,S.factoryNames.size())));
            }

            for (var f : S.civilianFactories)
            {
                f.generateName(S.factoryNames.get(rand.nextInt(0,S.factoryNames.size())));
                f.setLocation(S);
            }
            for (var f : S.refineries)
            {
                f.generateName(S.factoryNames.get(rand.nextInt(0,S.factoryNames.size())));
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
        out.println(colorize(prefix+"\tBuilding slots unlocked "+getBuildingSlots(building_slot_bonus),Attribute.GREEN_TEXT()));
        out.println(colorize(prefix+"\tCan build civilian factories: "+(canBuildCivilianFactory(building_slot_bonus)?"yes":"no"),Attribute.BRIGHT_YELLOW_TEXT()));
        out.println(colorize(prefix+"\tCan build military factories: "+(canBuildMilitaryFactory(building_slot_bonus)?"yes":"no"),Attribute.BRIGHT_GREEN_TEXT()));
        out.println(colorize(prefix+"\tCan build refinery factories: "+(canBuildRefineryFactory(building_slot_bonus)?"yes":"no"),Attribute.BLUE_TEXT()));
        if (!civilianFactories.isEmpty())
        {
            out.println(colorize(prefix+"\t"+civilianFactories.size()+" Civilian Factories:",Attribute.BRIGHT_YELLOW_TEXT()));
         //   for (var F : civilianFactories)
         //   {
         //       F.printReport(out,prefix+'\t');
         //   }
        }
        if (!refineries.isEmpty()) {
            out.println(colorize(prefix +"\t"+ refineries.size() + " Refineries:",Attribute.BLUE_TEXT()));
         //   for (var F : refineries) {
         //       F.printReport(out, prefix + '\t');
         //   }
        }
        if (!militaryFactories.isEmpty()) {
            out.println(colorize(prefix +"\t"+ militaryFactories.size() + " Military Factories:",Attribute.BRIGHT_GREEN_TEXT()));
         //   for (var F : militaryFactories) {
         //       F.printReport(out, prefix + '\t');
         //   }
        }
        out.print(colorize(String.format(prefix+"\t                     %4s %9s %6s %8s %5s %8s%n", "Oil", "Aluminium", "Rubber", "Tungsten","Steel","Chromium"),Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD()));
        //Resources available
        out.print(colorize(String.format(prefix+"\tBase resources......:%3s %6s %8s %7s %6s %7s%n",  base_oil, base_aluminium, base_rubber, base_tungsten, base_steel, base_chromium),Attribute.GREEN_TEXT()));
        out.print(colorize(String.format(prefix+"\tWith Industry bonus.:%3s %6s %8s %7s %6s %7s%n", getOil() , getAluminium(), getRubber(rubber_per_refinery), getTungsten(), getSteel(), getChromium()),Attribute.GREEN_TEXT()));
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
        var New = new CivilianFactory(factoryNames.get(rand.nextInt(0,factoryNames.size())),this,true);
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
        var New = new MilitaryFactory(factoryNames.get(rand.nextInt(0,factoryNames.size())),this,true);
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
        var New = new Refinery(factoryNames.get(rand.nextInt(0,factoryNames.size())),this,true);
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
        return (getBuildingSlots(building_slot_bonus)-civilianFactories.size()-militaryFactories.size()-refineries.size()>0);
    }

    public boolean canBuildMilitaryFactory(double building_slot_bonus) {
        //check if there are no military factories in construction
        for (var f : militaryFactories)
            if (f.getUnderConstruction())
            {
                return false;
            }
        //If then, see if we have free slots
        return (getBuildingSlots(building_slot_bonus)-civilianFactories.size()-militaryFactories.size()-refineries.size()>0);
    }

    public boolean canBuildRefineryFactory(double building_slot_bonus) {
        //check if there are no refineries factories in construction
        for (var f : refineries)
            if (f.getUnderConstruction())
            {
                return false;
            }
        //If then, see if we have free slots
        return (getBuildingSlots(building_slot_bonus)-civilianFactories.size()-militaryFactories.size()-refineries.size()>0);
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
}
