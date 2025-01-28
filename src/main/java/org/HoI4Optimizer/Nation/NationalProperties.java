package org.HoI4Optimizer.Nation;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;

/// A class with properties, which we want to be able to load from a json file
/* Deliberately left package-private*/public class NationalProperties implements Cloneable
{
    /// Countries have 1 of 4 ideologies
    /// Roughly in order of how good/bad they are
    public enum ideology
    {
        /// Good ideology, keep
        Democratic,
        /// Misguided compassion
        Communist,
        /// Miscellaneous category, including monarchies, theocracies, dictatorships and Finland
        Nonaligned,
        /// Literally evil
        Fascist,
    };

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
    private double Efficiency_cap;
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
    private ideology RulingParty;
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
    /// No access to imports
    private boolean embargoed=false;

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
            clone.Efficiency_cap=Efficiency_cap;
            clone.embargoed=embargoed;
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


    public boolean getEmbargoed() {
        return embargoed;
    }

    public void setEmbargoed(boolean embargoed) {
        this.embargoed = embargoed;
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

    public void setBasic_fuel_capacity(double basic_fuel_capacity) {
        this.basic_fuel_capacity = basic_fuel_capacity;
    }

    public void setFuel_capacity_per_infrastructure(double fuel_capacity_per_infrastructure) {
        this.fuel_capacity_per_infrastructure = fuel_capacity_per_infrastructure;
    }

    public void setResources_to_market(double resources_to_market) {
        this.resources_to_market = resources_to_market;
    }

    public double getBasic_fuel_capacity() {
        return basic_fuel_capacity;
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

    /// How much rubber do our chemical industry produce per plant
    public void setRubber_per_refineries(int rubber_per_refineries) {
        this.rubber_per_refineries = rubber_per_refineries;
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
        return Efficiency_cap;
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

    public ideology getRulingParty() {
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

    public void setBase_factory_output(double base_factory_output) {
        this.base_factory_output = base_factory_output;
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setAutocracy_support(double autocracy_support) {
        this.autocracy_support = autocracy_support;
    }

    public void setBase_consumer_goods_multiplier(double base_consumer_goods_multiplier) {
        this.base_consumer_goods_multiplier = base_consumer_goods_multiplier;
    }

    public void setBase_consumer_goods_ratio(double base_consumer_goods_ratio) {
        this.base_consumer_goods_ratio = base_consumer_goods_ratio;
    }

    public void setBase_stability(double base_stability) {
        this.base_stability = base_stability;
    }

    public void setCiv_construction_speed_bonus(double civ_construction_speed_bonus) {
        this.civ_construction_speed_bonus = civ_construction_speed_bonus;
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setCommunism_support(double communism_support) {
        this.communism_support = communism_support;
    }

    public void setConstruction_speed(double construction_speed) {
        this.construction_speed = construction_speed;
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setDemocracy_support(double democracy_support) {
        this.democracy_support = democracy_support;
    }

    public void setEfficiency_cap(double efficiency_cap) {
        Efficiency_cap = efficiency_cap;
    }

    /// should only be used by json deserializer! this does not re-normalize other parties (necessary to make loading work)
    public void setFascism_support(double fascism_support) {
        this.fascism_support = fascism_support;
    }

    public void setMil_construction_speed_bonus(double mil_construction_speed_bonus) {
        this.mil_construction_speed_bonus = mil_construction_speed_bonus;
    }

    public void setResource_gain_bonus(double resource_gain_bonus) {
        this.resource_gain_bonus = resource_gain_bonus;
    }

    public void setRulingParty(ideology rulingParty) {
        RulingParty = rulingParty;
    }

    public void setSpecial_aluminium(int special_aluminium) {
        this.special_aluminium = special_aluminium;
    }

    public void setSpecial_chromium(int special_chromium) {
        this.special_chromium = special_chromium;
    }

    public void setSpecial_projects_civs(int special_projects_civs) {
        this.special_projects_civs = special_projects_civs;
    }

    public void setSpecial_rubber(int special_rubber) {
        this.special_rubber = special_rubber;
    }

    public void setSpecial_steel(int special_steel) {
        this.special_steel = special_steel;
    }

    public void setSpecial_tungsten(int special_tungsten) {
        this.special_tungsten = special_tungsten;
    }

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
    public void formCoalition(ideology Ideology, ideology Partner)
    {
        double NewSupport=0;
        switch(Partner)
        {
            case Democratic -> {NewSupport= democracy_support;democracy_support=0;}//Oh no
            case Fascist -> {NewSupport= fascism_support;fascism_support=0;}//Good riddance
            case Nonaligned -> {NewSupport= autocracy_support;autocracy_support=0;}//history finaly caught up with you
            case Communist -> {NewSupport= communism_support;communism_support=0;}//that was inevitable
            default -> {NewSupport=0;}
        }
        switch(Ideology)
        {
            case Democratic -> { democracy_support+=NewSupport;}//Liberty, Equality, and Solidarity!
            case Fascist -> {fascism_support+=NewSupport;}//... oh no...
            case Nonaligned -> {autocracy_support+=NewSupport;}//not good
            case Communist -> {communism_support+=NewSupport;}//That is not going to work
            default -> {communism_support+=NewSupport;}//Anarchism!
        }
    }

    //Derived properties: calculated from the others in a predictable way
    /// Derived total stability, permanent stability effects, plus stability (clamped)
    public double getStability() {
        return permanent_stability+Math.clamp(base_stability,0,1)+getRulingParty_support()*.15;
    }

    /// Actual consumer goods ratio required
    public double getConsumer_goods_ratio() {
        double stab = getStability();
        //Up to 20% multiplicative reduction
        return base_consumer_goods_ratio*base_consumer_goods_multiplier*(1+(stab>0.5? (stab-0.5)*2*.2 : 0 ));
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
        return objectMapper.readValue(new File(filePath), new TypeReference<NationalProperties>() {});
    }
}
