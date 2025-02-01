package org.HoI4Optimizer.Nation;

import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.HoI4Optimizer.Nation.Events.PropertyEvent;
import org.apache.commons.lang3.NotImplementedException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import static com.diogonunes.jcolor.Ansi.colorize;

/// A class with properties, which we want to be able to load from a json file
/// BTW this is not a record, since records are immutable, and these things very much do change all the time
/*Intentionally package private*/ class NationalProperties implements Cloneable
{
    ///Stability, without party popularity and other effects
    private double base_stability=0;
    /// sum of all permanent effects on stability
    private double permanent_stability;
    /// What fraction of the population is just plain wrong?
    private double autocracy_support;
    /// What fraction of the population is good?
    private double democracy_support;
    /// What fraction of the population is literally evil?
    private double fascism_support;
    /// What fraction of the population is misguided?
    private double communism_support;
    /// How quickly is stupidity spreading?
    private double autocracy_growth;
    /// How quickly is the people being enlightened?
    private double democracy_growth;
    /// How quickly is the terrible darkness growing?
    private double fascism_growth;
    /// How quickly is confusion spreading?
    private double communism_growth;
    ///General construction speed bonus from technology and events
    private double construction_speed;
    ///construction speed bonus for military factories, added to construction speed
    private double mil_construction_speed_bonus;
    ///construction speed bonus for civilian factories,  added to construction speed
    private double civ_construction_speed_bonus;
    ///Efficiency cap
    private double efficiency_cap;
    ///Factory output bonus, Not counting effects from stability
    private double base_factory_output;
    ///Fraction of all factories (civilian and military)  required for "consumer goods" (the non-military part of the economy) This is subtracted from CIVILIAN factories
    private double base_consumer_goods_ratio;
    /// A multiplier, effecting consumer goods, will be further modified by current stability
    private double base_consumer_goods_multiplier;
    ///Excavation technology bonus to resource gain on a national level
    private double resource_gain_bonus;
    /// Base oil income, national basis, unupgraded fuel per oil, and unupgraded income from 1 refinery
    private double base_fuel =48;
    /// Additive bonuses to refinery fuel production
    private double refinery_fuel_bonus=0;
    /// Additive bonuses or penalties  to fuel per oil
    private double natural_fuel_bonus=-0.6;
    /// Fuel capacity for each level of state infrastructure
    private double fuel_capacity_per_infrastructure=1500;
    /// Baseline fuel capacity of all nations
    private double basic_fuel_capacity=500;
    /// Fraction of resources forced to be exported to the market
    private double resources_to_market=0.25;
    /// Percentage bonus to slots in states, applies only to slots from state type
    private double buildingSlotBonus=0;
    /// Who's in charge? effects which party popularity gives positive stability
    private Ideology RulingParty;
    ///How many civilian factories are earmarked for special projects (mainly intelligence and decryption) each month
    /// This is subtracted from available civs after calculating consumergoods, but before trade and construction
    private int special_projects_civs;
    /// Special project steel consumption
    private int special_steel;
    /// Special project aluminium consumption
    private int special_aluminium;
    /// Special project tungsten consumption
    private int special_tungsten;
    /// Special project chromium consumption
    private int special_chromium;
    /// Special project rubber consumption
    private int special_rubber;

    @Override
    public NationalProperties clone() {
        try {
            NationalProperties clone = (NationalProperties) super.clone();
            clone.base_factory_output=base_factory_output;
            clone.buildingSlotBonus=buildingSlotBonus;
            clone.autocracy_support =autocracy_support;
            clone.base_consumer_goods_ratio=base_consumer_goods_ratio;
            clone.base_fuel=base_fuel;
            clone.base_consumer_goods_multiplier=base_consumer_goods_multiplier;
            clone.base_stability=base_stability;
            clone.basic_fuel_capacity=basic_fuel_capacity;
            clone.civ_construction_speed_bonus=civ_construction_speed_bonus;
            clone.communism_support=communism_support;
            clone.construction_speed=construction_speed;
            clone.democracy_support=democracy_support;
            clone.efficiency_cap = efficiency_cap;
            clone.fascism_support=fascism_support;
            clone.fuel_capacity_per_infrastructure=fuel_capacity_per_infrastructure;
            clone.mil_construction_speed_bonus=mil_construction_speed_bonus;
            clone.natural_fuel_bonus=natural_fuel_bonus;
            clone.permanent_stability=permanent_stability;
            clone.refinery_fuel_bonus=refinery_fuel_bonus;
            clone.resources_to_market=resources_to_market;
            clone.resource_gain_bonus=resource_gain_bonus;
            clone.rubber_per_refineries=rubber_per_refineries;
            clone.special_aluminium=special_aluminium;
            clone.special_chromium=special_chromium;
            clone.special_rubber=special_rubber;
            clone.special_steel=special_steel;
            clone.special_projects_civs=special_projects_civs;
            clone.RulingParty=RulingParty;
            clone.special_tungsten=special_tungsten;
            clone.autocracy_growth=autocracy_growth;
            clone.democracy_growth=democracy_growth;
            clone.communism_growth=communism_growth;
            clone.fascism_growth=fascism_growth;
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    public void setAutocracy_support(double autocracy_support) {
        this.autocracy_support = autocracy_support;
    }

    public void setBasic_fuel_capacity(double basic_fuel_capacity) {
        this.basic_fuel_capacity = basic_fuel_capacity;
    }

    public void setRubber_per_refineries(int rubber_per_refineries) {
        this.rubber_per_refineries = rubber_per_refineries;
    }

    public double getAutocracy_growth() {
        return autocracy_growth;
    }

    public double getCommunism_growth() {
        return communism_growth;
    }

    public double getDemocracy_growth() {
        return democracy_growth;
    }

    public double getFascism_growth() {
        return fascism_growth;
    }

    public void setAutocracy_growth(double autocracy_growth) {
        this.autocracy_growth = autocracy_growth;
    }

    public void setCommunism_growth(double communism_growth) {
        this.communism_growth = communism_growth;
    }

    public void setDemocracy_growth(double democracy_growth) {
        this.democracy_growth = democracy_growth;
    }

    public void setFascism_growth(double fascism_growth) {
        this.fascism_growth = fascism_growth;
    }

    public double getBuildingSlotBonus() {
        return buildingSlotBonus;
    }

    public void setBuildingSlotBonus(double buildingSlotBonus) {
        this.buildingSlotBonus = buildingSlotBonus;
    }

    public double getResources_to_market() {
        return resources_to_market;
    }

    public void setFuel_capacity_per_infrastructure(double fuel_capacity_per_infrastructure) {
        this.fuel_capacity_per_infrastructure = fuel_capacity_per_infrastructure;
    }

    public void setResources_to_market(double resources_to_market) {
        this.resources_to_market = resources_to_market;
    }

    public double getFuel_capacity_per_infrastructure() {
        return fuel_capacity_per_infrastructure;
    }

    public void setBase_fuel(double base_oil) {
        this.base_fuel = base_oil;
    }

    public double getBase_fuel() {
        return base_fuel;
    }

    public double getNatural_fuel_bonus() {
        return natural_fuel_bonus;
    }

    public double getRefinery_fuel_bonus() {
        return refinery_fuel_bonus;
    }

    public void setNatural_fuel_bonus(double natural_fuel_bonus) {
        this.natural_fuel_bonus = natural_fuel_bonus;
    }

    public void setRefinery_fuel_bonus(double refinery_fuel_bonus) {
        this.refinery_fuel_bonus = refinery_fuel_bonus;
    }

    /// How much rubber do our chemical industry produce per plant
    private int rubber_per_refineries;

    /// How much rubber do our chemical industry produce per plant
    public int getRubber_per_refineries() {
        return rubber_per_refineries;
    }

    /// Who are just plain wrong? number from 0 to 1
    public double getAutocracy_support() {
        return autocracy_support;
    }

    /// Does not include stability effect
    public double getBase_consumer_goods_multiplier() {
        return base_consumer_goods_multiplier;
    }

    /// Does not include multipliers
    public double getBase_consumer_goods_ratio() {
        return base_consumer_goods_ratio;
    }

    /// Does not include stability effect
    public double getBase_factory_output() {
        return base_factory_output;
    }

    /// Base stability effect, clamped betwixt 0 and 1
    public double getBase_stability() {
        return base_stability;
    }

    public double getCiv_construction_speed_bonus() {
        return civ_construction_speed_bonus;
    }

    public double getCommunism_support() {
        return communism_support;
    }

    public double getConstruction_speed() {
        return construction_speed;
    }

    public double getDemocracy_support() {
        return democracy_support;
    }

    public double getEfficiency_cap() {
        return efficiency_cap;
    }

    public double getFascism_support() {
        return fascism_support;
    }

    public double getMil_construction_speed_bonus() {
        return mil_construction_speed_bonus;
    }
    public double getResource_gain_bonus() {
        return resource_gain_bonus;
    }

    public double getPermanent_stability() {
        return permanent_stability;
    }

    public Ideology getRulingParty() {
        return RulingParty;
    }

    public int getSpecial_aluminium() {
        return special_aluminium;
    }

    public int getSpecial_chromium() {
        return special_chromium;
    }

    public int getSpecial_projects_civs() {
        return special_projects_civs;
    }

    public int getSpecial_rubber() {
        return special_rubber;
    }

    public int getSpecial_steel() {
        return special_steel;
    }

    public int getSpecial_tungsten() {
        return special_tungsten;
    }

    /// Clamped to above -1.0 (-100% = no production), has not upper limit
    public void setBase_factory_output(double base_factory_output) {
        this.base_factory_output = Math.max(-1.0,base_factory_output);
    }

    /// Consumer goods multiplier, do not modify by adding!, instead multiply with (1+thingToAdd)
    public void setBase_consumer_goods_multiplier(double base_consumer_goods_multiplier) {
        this.base_consumer_goods_multiplier = Math.max(base_consumer_goods_multiplier,0.0);
    }

    /// Base consumer goods ratio, between 0.0 and 1.0
    public void setBase_consumer_goods_ratio(double base_consumer_goods_ratio) {
        this.base_consumer_goods_ratio = Math.clamp(base_consumer_goods_ratio, 0.0, 1.0);
    }

    /// Base stability before permanent effects are applied, is between 0 and 1
    public void setBase_stability(double base_stability) {
        this.base_stability = Math.clamp(base_stability,0.0,1.0);
    }

    /// Clamped to above -1.0 (-100% = no construction), has not upper limit
    public void setCiv_construction_speed_bonus(double civ_construction_speed_bonus) {
        this.civ_construction_speed_bonus = Math.max(-1.0,civ_construction_speed_bonus);
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setCommunism_support(double communism_support) {
        this.communism_support = communism_support;
    }

    /// Clamped to above -1.0 (-100% = no construction), has not upper limit
    public void setConstruction_speed(double construction_speed) {
        this.construction_speed = Math.max(-1.0,construction_speed);
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setDemocracy_support(double democracy_support) {
        this.democracy_support = democracy_support;
    }

    /// Clamped to between 0.0 and 1.0
    public void setEfficiency_cap(double efficiency_cap) {
        this.efficiency_cap = Math.clamp(efficiency_cap,0.0,1.0);
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setFascism_support(double fascism_support) {
        this.fascism_support = fascism_support;
    }

    /// Clamped to above -1.0 (-100% = no construction), has not upper limit
    public void setMil_construction_speed_bonus(double mil_construction_speed_bonus) {
        this.mil_construction_speed_bonus = Math.max(-1.0, mil_construction_speed_bonus);
    }

    /// Clamped to above -1.0 (-100% = no resources), has not upper limit
    public void setResource_gain_bonus(double resource_gain_bonus) {
        this.resource_gain_bonus = Math.max(-1.0, resource_gain_bonus);
    }

    /// Set ruling party without changing supports
    public void setRulingParty(Ideology rulingParty) {
        RulingParty = rulingParty;
    }

    /// Clamped to a positive number or 0
    public void setSpecial_aluminium(int special_aluminium) {
        this.special_aluminium = Math.max(0,special_aluminium);
    }

    /// Clamped to a positive number or 0
    public void setSpecial_chromium(int special_chromium) {
        this.special_chromium = Math.max(0,special_chromium);
    }

    /// Clamped to a positive number or 0
    public void setSpecial_projects_civs(int special_projects_civs) {
        this.special_projects_civs = Math.max(0,special_projects_civs);
    }

    /// Clamped to a positive number or 0
    public void setSpecial_rubber(int special_rubber) {
        this.special_rubber = Math.max(0,special_rubber);
    }

    /// Clamped to a positive number or 0
    public void setSpecial_steel(int special_steel) {
        this.special_steel = Math.max(0,special_steel);
    }

    /// Clamped to a positive number or 0
    public void setSpecial_tungsten(int special_tungsten) {
        this.special_tungsten = Math.max(0,special_tungsten);
    }

    /// Permanent stability effects is not clamped, you can have effects going above 100%, but the resulting stability is clamped, anything above that can act as a "buffer"
    public void setPermanent_stability(double permanent_stability) {
        this.permanent_stability = permanent_stability;
    }


    /// Get support for ruling party (number between 0 and 1)
    public double getRulingParty_support()
    {
        switch(RulingParty)
        {
            case Democratic -> {return democracy_support;}
            case Fascist -> {return fascism_support;}
            case Nonaligned -> {return autocracy_support;}
            case Communist -> {return communism_support;}
            default -> {return communism_support;}//Anarchism!
        }
    }

    /// Convert all Partner ideology support to this ideology
    public void formCoalition(Ideology Ideology, Ideology Partner)
    {
        double NewSupport;
        switch(Partner)
        {
            case Democratic -> {NewSupport= democracy_support;democracy_support=0;}//Oh, no
            case Fascist -> {NewSupport= fascism_support;fascism_support=0;}//Good riddance
            case Nonaligned -> {NewSupport= autocracy_support;autocracy_support=0;}//history finally caught up with you
            case Communist -> {NewSupport= communism_support;communism_support=0;}//that was inevitable
            default -> NewSupport=0;
        }
        switch(Ideology)
        {
            case Democratic -> democracy_support+=NewSupport;//Liberty, Equality, and Solidarity!
            case Fascist -> fascism_support+=NewSupport;//... oh no...
            case Nonaligned -> autocracy_support+=NewSupport;//not good
            case Communist -> communism_support+=NewSupport;//That is not going to work
            default -> communism_support+=NewSupport;//Anarchism!
        }
    }

    //Derived properties: calculated from the others in a predictable way
    /// Derived total stability, permanent stability effects, plus stability (clamped)
    public double getStability() {
        return Math.clamp(permanent_stability+Math.clamp(base_stability,0,1)+getRulingParty_support()*.15,0,1);
    }

    /// Actual consumer goods ratio required
    public double getConsumer_goods_ratio() {
        double stab = getStability();
        //Up to 20% multiplicative reduction from stability
        return Math.clamp(base_consumer_goods_ratio*base_consumer_goods_multiplier*(1+(stab>0.5? (stab-0.5)*2*.2 : 0 )),0.1/*There is a minimum ratio*/,1.0);
    }

    /// Actual factory output, includig stability effects
    public double getFactoryOutput()
    {

        double stab = getStability();
        if (stab>0.5)
        {
            //Up to 20% additive bonus
            return (stab-0.5)*2*0.2+base_factory_output;
        }
        else
        {
            //Up to 50% additive penalty
            return (stab-0.5)*2*0.5+base_factory_output;
        }
    }

    ///Properties from json
    public static NationalProperties loadProperties(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<>() {
        });
    }

    /// Apply events, and optionally print what happens
    /// @param out Where to print to, use null if you don't want anything printed
    /// @param event event to apply
    public void apply(PropertyEvent event, PrintStream out)
    {
        Attribute GoodOutcome= Attribute.GREEN_TEXT();
        Attribute BadOutcome=Attribute.RED_TEXT();
        Attribute MiddlingOutcome=Attribute.WHITE_TEXT();

        switch (event.modify())
        {
            case null ->
            {
                if (out!=null)
                {
                    out.println(colorize("    No effect",MiddlingOutcome));
                }
            }
            //Start with fuel and oil related effects
            case base_fuel -> {
                if (event.add())
                {
                    setBase_fuel(base_fuel+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value())+" base fuel gain, is now "+String.format("%.2f", base_fuel),event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setBase_fuel(event.value());
                    if (out!=null)
                        out.println(colorize("    Set base fuel gain "+String.format("%.2f",event.value()),event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            //Extra fuel bonus from oil
            case natural_fuel_bonus -> {
                if (event.add())
                {
                    setNatural_fuel_bonus(natural_fuel_bonus+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% fuel per oil bonus , is now "+String.format("%.2f",base_fuel*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setNatural_fuel_bonus(event.value());
                    if (out!=null)
                        out.println(colorize("    Set fuel per oil bonus "+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case fuel_capacity_per_infrastructure -> {

                if (event.add())
                {
                    setFuel_capacity_per_infrastructure(fuel_capacity_per_infrastructure+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value())+" fuel capacity per infrastructure, is now "+String.format("%.2f",fuel_capacity_per_infrastructure),event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setFuel_capacity_per_infrastructure(event.value());
                    if (out!=null)
                        out.println(colorize("    Set fuel capacity per infrastructure "+String.format("%.2f",event.value()),event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case basic_fuel_capacity -> {
                if (event.add())
                {
                    setBase_fuel(base_fuel+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value())+" base fuel capacity, is now "+String.format("%.2f",base_fuel),event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setBase_fuel(event.value());
                    if (out!=null)
                        out.println(colorize("    Set base fuel capacity "+String.format("%.2f",event.value()),event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case refinery_fuel_bonus -> {
                if (event.add())
                {
                    setRefinery_fuel_bonus(refinery_fuel_bonus+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt refinery fuel bonus, is now "+String.format("%.2f",refinery_fuel_bonus*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setRefinery_fuel_bonus(event.value());
                    if (out!=null)
                        out.println(colorize("    Set refinery fuel bonus "+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case RulingParty -> {
                setRulingParty(event.ideology());
                if (out!=null)
                {
                    out.println(colorize("    Set ruling party...", Attribute.WHITE_TEXT()));
                    switch (event.ideology())
                    {
                        case Nonaligned -> out.println(colorize("    Oh no! The non-aligned party has taken control of the government",Attribute.WHITE_TEXT()));
                        case Communist  -> out.println(colorize("    Bad news! the communist revolution has overthrown the government",Attribute.RED_TEXT()));
                        case Democratic -> out.println(colorize("    The nation is now a democracy!",Attribute.BLUE_TEXT()));
                        case Fascist ->    out.println(colorize("    Catastrophe! a fascist dictator has taken control of the government",Attribute.YELLOW_TEXT()));
                    }
                }
            }
            case fascistCoalition -> {
                formCoalition(Ideology.Fascist,event.ideology());
                if (out!=null)
                {
                    out.println(colorize("    Form Fascist coalition...",Attribute.YELLOW_TEXT()));
                    switch (event.ideology())
                    {
                        case Nonaligned -> out.println(colorize("    In an effort to hold onto power, the non-aligned party has merged with the fascist party",Attribute.YELLOW_TEXT()));
                        case Communist  -> out.println(colorize("    The working class has been ensnared by a fascist demagogue: all communist are now Fascist",Attribute.RED_TEXT()));
                        case Democratic -> out.println(colorize("    Oh Terror, all democratically leaning people are now fascist",Attribute.BLUE_TEXT()));
                        case Fascist ->    out.println(colorize("    Breaking news The Fascist party is, in fact, Fascist",Attribute.YELLOW_TEXT()));
                    }
                }
            }
            case communistCoalition -> {
                formCoalition(Ideology.Communist,event.ideology());
                if (out!=null)
                {
                    out.println(colorize("    Form Communist coalition...",Attribute.YELLOW_TEXT()));
                    switch (event.ideology())
                    {
                        case Nonaligned -> out.println(colorize("    The communists ate the rich, and took over all their autocratic support",Attribute.RED_TEXT()));
                        case Communist  -> out.println(colorize("    The communist party has switched to a competing communist ideology",Attribute.RED_TEXT()));
                        case Democratic -> out.println(colorize("    Oh no, the democratic party developed class consciousness and became communist",Attribute.RED_TEXT()));
                        case Fascist ->    out.println(colorize("    The communists beat up the fascist so badly, that all their supporters switched sides",Attribute.RED_TEXT()));
                    }
                }
            }
            case democraticCoalition -> {
                formCoalition(Ideology.Democratic,event.ideology());
                if (out!=null)
                {
                    out.println(colorize("    Form democratic coalition...",Attribute.BLUE_TEXT()));
                    switch (event.ideology())
                    {
                        case Nonaligned -> out.println(colorize("    The autocratic party has come to its senses and now supports democracy",Attribute.BLUE_TEXT()));
                        case Communist  -> out.println(colorize("    The communist saw the errors of their ways and became social democrats",Attribute.BLUE_TEXT()));
                        case Democratic -> out.println(colorize("    The people celebrates its liberty",Attribute.BLUE_TEXT()));
                        case Fascist ->    out.println(colorize("    The fascists realised they were the bevent.add()ies, and now support democracy",Attribute.BLUE_TEXT()));
                    }
                }
            }
            case nonalignedCoalition -> {
                formCoalition(Ideology.Nonaligned,event.ideology());
                if (out!=null)
                {
                    out.println(colorize("    Form non-aligned coalition...",Attribute.WHITE_TEXT()));
                    switch (event.ideology())
                    {
                        case Nonaligned -> out.println(colorize("    The autocratic party still doesn't really believe in anything",Attribute.WHITE_TEXT()));
                        case Communist  -> out.println(colorize("    The communist party leaders have lost touch with the proletariat and has become autocrats",Attribute.WHITE_TEXT()));
                        case Democratic -> out.println(colorize("    Democracy has declined into oligarchy and autocracy",Attribute.WHITE_TEXT()));
                        case Fascist ->    out.println(colorize("    The fascists have lost their fanaticism and become normal authoritarians",Attribute.WHITE_TEXT()));
                    }
                }
            }
            case fascism_growth -> {
                if (event.add())
                {
                    setFascism_growth(fascism_growth+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt fascist daily growth, is now "+String.format("%.2f",fascism_growth*100)+"% pt per day",event.value()<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setFascism_growth(event.value());
                    if (out!=null)
                        out.println(colorize("    Set fascist daily growth "+String.format("%.2f",event.value()*100)+"% pt per day",event.value()<0? GoodOutcome:BadOutcome));
                }
            }
            case communism_growth ->
            {
                if (event.add())
                {
                    setCommunism_growth(communism_growth+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt communist daily growth, is now "+String.format("%.2f",communism_growth)+"% pt per day",event.value()*100<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setCommunism_growth(event.value());
                    if (out!=null)
                        out.println(colorize("    Set communist daily growth "+String.format("%.2f",event.value()*100)+"% pt per day",event.value()<0? GoodOutcome:BadOutcome));
                }
            }
            case autocracy_growth -> {
                if (event.add())
                {
                    setAutocracy_growth(autocracy_growth+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt non-aligned daily growth, is now "+String.format("%.2f",autocracy_growth*100)+"% pt per day",event.value()<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setAutocracy_growth(event.value());
                    if (out!=null)
                        out.println(colorize("    Set non-aligned daily growth "+String.format("%.2f",event.value()*100)+"% pt per day",event.value()<0? GoodOutcome:BadOutcome));
                }
            }
            case democracy_growth -> {
                if (event.add())
                {
                    setDemocracy_growth(democracy_growth+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt democracy daily growth, is now "+String.format("%.2f",democracy_growth*100)+"% pt per day",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setDemocracy_growth(event.value());
                    if (out!=null)
                        out.println(colorize("    Set democracy daily growth "+String.format("%.2f",event.value()*100)+"% pt per day",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case fascism_support -> {
                throw new NotImplementedException();
                /*if (event.add())
                {
                    setFascism_support(properties.getFascism_support()+event.value());
                    if (out!=null){
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+" % pt Fascism support, is now "+String.format("%.2f",properties.getFascism_support()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    setFascism_support(event.value());
                    if (out!=null){
                        out.println(colorize("    Set Fascism support"+String.format("%.2f",event.value()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                }
                }

                 */
            }
            case democracy_support -> {
                throw new NotImplementedException();
                /*
                if (event.add())
                {
                    setDemocracy_support(properties.getDemocracy_support()+event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt democracy support, is now "+String.format("%.2f",properties.getDemocracy_support()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    setDemocracy_support(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set democracy support"+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                }
                }

                 */
            }
            case autocracy_support -> {
                throw new NotImplementedException();
                /*if (event.add())
                {
                    setAutocracy_support(properties.getAutocracy_support()+event.value());
                    if (out!=null){
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt non-aligned support, is now "+String.format("%.2f",autocracy_growth*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    setAutocracy_support(event.value());
                    if (out!=null){
                        out.println(colorize("    Set non-aligned support "+String.format("%.2f",event.value()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                }
                }
                 */
            }
            case communism_support -> {
                throw new NotImplementedException();
                /*if (event.add())
                {
                    setCommunism_support(properties.getCommunism_support()+event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt Communism support, is now "+String.format("%.2f",properties.getCommunism_support()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    setCommunism_support(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set Communism support "+String.format("%.2f",event.value()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                }
                }
                 */
            }
            case efficiency_cap -> {
                if (event.add())
                {
                    setEfficiency_cap(efficiency_cap+event.value());
                    if (out!=null){
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% efficiency cap, is now "+String.format("%.2f",efficiency_cap*100),event.value()>0? GoodOutcome:BadOutcome));
                    }
                }
                else
                {
                    setEfficiency_cap(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set efficiency cap "+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                    }
                }
            }
            case base_stability -> {
                if (event.add())
                {
                    setBase_stability(base_stability+event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt base stability, is now "+String.format("%.2f",base_stability*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total factory output is now " + String.format("%.2f", getFactoryOutput() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    }

                }
                else
                {
                    setBase_stability(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set base stability "+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total factory output is now " + String.format("%.2f", getFactoryOutput() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case permanent_stability -> {
                if (event.add())
                {
                    setPermanent_stability(permanent_stability+event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",event.value())+"% pt permanent stability bonus, is now "+String.format("%.2f",permanent_stability*100)+"% pt",event.value()>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total factory output is now " + String.format("%.2f", getFactoryOutput() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setPermanent_stability(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set permanent stability "+String.format("%.2f",event.value()*100)+"% pt",event.value()>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",getStability()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total factory output is now " + String.format("%.2f", getFactoryOutput() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case base_factory_output -> {
                if (event.add())
                {
                    setBase_factory_output(base_factory_output+event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + String.format("%.2f", event.value() * 100) + "% pt base factory output, is now " + String.format("%.2f", base_factory_output * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total factory output is now " + String.format("%.2f", getFactoryOutput() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setBase_stability(event.value());
                    if (out!=null) {
                        out.println(colorize("    Set base factory output " + String.format("%.2f", event.value() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total factory output is now " + String.format("%.2f", getFactoryOutput() * 100) + "%", event.value() > 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case construction_speed -> {
                if (event.add())
                {
                    setConstruction_speed(construction_speed+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt construction speed, is now "+String.format("%.2f",construction_speed*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setConstruction_speed(event.value());
                    if (out!=null)
                        out.println(colorize("    Set construction bonus "+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case civ_construction_speed_bonus -> {
                if (event.add())
                {
                    setCiv_construction_speed_bonus(civ_construction_speed_bonus+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt event.add()itional construction bonus for civilian factories, is now "+String.format("%.2f",civ_construction_speed_bonus*100)+"% pt",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setCiv_construction_speed_bonus(event.value());
                    if (out!=null)
                        out.println(colorize("    Set event.add()itional construction bonus for civilian factories "+String.format("%.2f",event.value()*100)+"% pt",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case mil_construction_speed_bonus -> {
                if (event.add())
                {
                    setMil_construction_speed_bonus(mil_construction_speed_bonus+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt event.add()itional construction bonus for military factories, is now "+String.format("%.2f",mil_construction_speed_bonus*100)+"% pt",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setMil_construction_speed_bonus(event.value());
                    if (out!=null)
                        out.println(colorize("    Set event.add()itional construction bonus for military factories "+String.format("%.2f",event.value()*100)+"% pt",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case resource_gain_bonus -> {
                if (event.add())
                {
                    setResource_gain_bonus(resource_gain_bonus+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt resource gain bonus, is now "+String.format("%.2f",resource_gain_bonus*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setResource_gain_bonus(event.value());
                    if (out!=null)
                        out.println(colorize("    Set resource gain bonus "+String.format("%.2f",event.value()*100)+"%",event.value()>0? GoodOutcome:BadOutcome));
                }
            }
            case resources_to_market -> {
                if (event.add())
                {
                    setResources_to_market(resources_to_market+event.value());
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",event.value()*100)+"% pt resources to market, is now "+String.format("%.2f",resources_to_market*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    setResources_to_market(event.value());
                    if (out!=null)
                        out.println(colorize("    Set resources to market "+String.format("%.2f",event.value()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                }
            }
            case base_consumer_goods_ratio -> {
                if (event.add())
                {
                    setBase_consumer_goods_ratio(base_consumer_goods_ratio+event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + String.format("%.2f", event.value()*100) + "% pt to base factories on consumer goods, is now " + String.format("%.2f", base_consumer_goods_ratio*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setBase_consumer_goods_ratio(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set base % of factories on consumer goods ratio: "+String.format("%.2f",event.value()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case base_consumer_goods_multiplier -> {
                if (event.add())
                    throw new RuntimeException("event.add() modify for consumer goods multiplier is not, and can not be, supporter");
                else
                {
                    setBase_consumer_goods_multiplier(1+event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set consumer goods multiplier: "+String.format("1+%.2f",event.value()*100),event.value()<0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",getConsumer_goods_ratio()*100) + " ", event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case buildingSlotBonus -> {
                if (event.add())
                {
                    setBuildingSlotBonus(buildingSlotBonus*100+event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + String.format("%.2f", event.value()*100) + "% pt to building slots bonus, is now " + String.format("%.2f", base_consumer_goods_multiplier*100) + "%", event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setBuildingSlotBonus(event.value());
                    if (out!=null)
                    {
                        out.println(colorize("    Set building slots bonus: "+String.format("%.2f",event.value()*100)+"%",event.value()<0? GoodOutcome:BadOutcome));
                    }
                }
            }
            case special_steel -> {
                if (event.add())
                {
                    setSpecial_steel(special_steel+(int)event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + event.value() + " steel per day for special projects, is now " + special_steel, event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setSpecial_steel((int)event.value());
                    if (out!=null)
                        out.println(colorize("    Set steel per day for special projects to " + event.value(), event.value() < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_chromium -> {
                if (event.add())
                {
                    setSpecial_chromium(special_chromium+(int)event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + event.value() + " chromium per day for special projects, is now " + special_chromium, event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setSpecial_chromium((int)event.value());
                    if (out!=null)
                        out.println(colorize("    Set chromium per day for special projects to " + event.value(), event.value() < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_tungsten -> {
                if (event.add())
                {
                    setSpecial_tungsten(special_tungsten+(int)event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + event.value() + " tungsten per day for special projects, is now " +special_tungsten, event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setSpecial_tungsten((int)event.value());
                    if (out!=null)
                        out.println(colorize("    Set tungsten per day for special projects to " + event.value(), event.value() < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_aluminium -> {
                if (event.add())
                {
                    setSpecial_aluminium(special_aluminium+(int)event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + event.value() + " aluminium per day for special projects, is now " + special_aluminium, event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setSpecial_aluminium((int)event.value());
                    if (out!=null)
                        out.println(colorize("    Set aluminium per day for special projects to " + event.value(), event.value() < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_rubber -> {
                if (event.add())
                {
                    setSpecial_rubber(special_rubber+(int)event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + event.value() + " rubber per day for special projects, is now " + special_rubber, event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setSpecial_rubber((int)event.value());
                    if (out!=null)
                        out.println(colorize("    Set rubber per day for special projects to " + event.value(), event.value() < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_projects_civs -> {
                if (event.add())
                {
                    setSpecial_projects_civs(special_projects_civs+(int)event.value());
                    if (out!=null) {
                        out.println(colorize("    Add " + event.value() + " civilian factories for special projects, is now " + special_projects_civs, event.value() < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    setSpecial_projects_civs((int)event.value());
                    if (out!=null)
                        out.println(colorize("    Set civilian factories for special projects to " + event.value(), event.value() < 0 ? GoodOutcome : BadOutcome));
                }
            }
        }
    }
}
