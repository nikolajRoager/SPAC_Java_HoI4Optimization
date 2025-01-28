package org.HoI4Optimizer.Nation;

import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;

import static com.diogonunes.jcolor.Ansi.colorize;

/// An even which happens, and effects the National properties
public class Event {
    public enum Target
    {
        ///Stability, without party popularity and other effects
          base_stability,
        /// sum of all permanent effects on stability
          permanent_stability,
        /// What fraction of the population is just plain wrong?
          autocracy_support,
        /// What fraction of the population is good?
          democracy_support,
        /// What fraction of the population is literally evil?
          fascism_support,
        /// What fraction of the population is misguided?
          communism_support,
        /// How quickly is stupidity spreading?
          autocracy_growth,
        /// How quickly is the people being enlightened?
          democracy_growth,
        /// How quickly is the terrible darkness growing?
          fascism_growth,
        /// How quickly is confusion spreading?
          communism_growth,
        ///General construction speed bonus from technology and events
          construction_speed,
        ///construction speed bonus for military factories, added to construction speed
          mil_construction_speed_bonus,
        ///construction speed bonus for civilian factories,  added to construction speed
          civ_construction_speed_bonus,
        ///Efficiency cap
          Efficiency_cap,
        ///Factory output bonus, Not counting effects from stability
          base_factory_output,
        ///Fraction of all factories (civilian and military)  required for "consumer goods" (the non-military part of the economy) This is subtracted from CIVILIAN factories
          base_consumer_goods_ratio,
        /// A multiplier, effecting consumer goods, will be further modified by current stability
          base_consumer_goods_multiplier,
        ///Excavation technology bonus to resource gain on a national level
          resource_gain_bonus,
        /// Base oil income, national basis, unupgraded fuel per oil, and unupgraded income from 1 refinery
          base_fuel,
        /// Additive bonuses to refinery fuel production
          refinery_fuel_bonus,
        /// Additive bonuses or penalties  to fuel per oil
          natural_fuel_bonus,
        /// Fuel capacity for each level of state infrastructure
          fuel_capacity_per_infrastructure,
        /// Baseline fuel capacity of all nations
          basic_fuel_capacity,
        /// Fraction of resources forced to be exported to the market
          resources_to_market,
        /// Percentage bonus to slots in states, applies only to slots from state type
          buildingSlotBonus,
        /// Who's in charge? effects which party popularity gives positive stability
         RulingParty,
        ///How many civilian factories are earmarked for special projects (mainly elligence and decryption) each month
        /// This is subtracted from available civs after calculating consumergoods, but before trade and construction
          special_projects_civs,
        /// Special project steel consumption
          special_steel,
        /// Special project aluminium consumption
          special_aluminium,
        /// Special project tungsten consumption
          special_tungsten,
        /// Special project chromium consumption
          special_chromium,
        /// Special project rubber consumption
          special_rubber,
        /// No access to imports
         embargoed,
        /// This target merges Democratic, Communist, Non-aligned, or Fascist into democratic (if value is 0,1,2, or 3 respectively)
        democraticCoalition,
        /// This target merges Democratic, Communist, Non-aligned, or Fascist into communist (if value is 0,1,2, or 3 respectively)
        communistCoalition,
        /// This target merges Democratic, Communist, Non-aligned, or Fascist into non-aligned (if value is 0,1,2, or 3 respectively)
        nonalignedCoalition,
        /// This target merges Democratic, Communist, Non-aligned, or Fascist into fascist (if value is 0,1,2, or 3 respectively)
        fascistCoalition,
    }


    /// What property is affected by this event, null: do nothing
    private Target target=null;

    /// Add to value (true), or overwrite (false)
    boolean add=true;

    /// A description of the event
    private String name="un-named effect";
    /// value to set to as a double
    private double dvalue=0.0;
    /// value to set to as an int
    private int ivalue=0;
    /// value to set to as a boolean
    private boolean bvalue=false;
    /// value to set to as an ideology
    private NationalProperties.ideology idvalue= NationalProperties.ideology.Democratic;

    /// Apply this even to the national properties
    public void apply(NationalProperties properties, PrintStream out)
    {
        var GoodOutcome=Attribute.GREEN_TEXT();
        var BadOutcome=Attribute.RED_TEXT();
        var MiddlingOutcome=Attribute.WHITE_TEXT();
        if (out!=null)
        {
            out.println(colorize("==An event has happened==",Attribute.BOLD(),Attribute.SLOW_BLINK()));
            out.println(colorize("    "+name,Attribute.ITALIC()));
        }
        switch (target)
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
                if (add)
                {
                    properties.setBase_fuel(properties.getBase_fuel()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+" base fuel gain, is now "+String.format("%.2f",properties.getBase_fuel()),dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setBase_fuel(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set base fuel gain "+String.format("%.2f",dvalue),dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            //Extra fuel bonus from oil
            case natural_fuel_bonus -> {
                if (add)
                {
                    properties.setNatural_fuel_bonus(properties.getNatural_fuel_bonus()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% fuel per oil bonus , is now "+String.format("%.2f",properties.getBase_fuel())+"%",dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setNatural_fuel_bonus(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set fuel per oil bonus "+String.format("%.2f",dvalue)+"%",dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            case fuel_capacity_per_infrastructure -> {

                if (add)
                {
                    properties.setFuel_capacity_per_infrastructure(properties.getFuel_capacity_per_infrastructure()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+" fuel capacity per infrastructure, is now "+String.format("%.2f",properties.getFuel_capacity_per_infrastructure()),dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setFuel_capacity_per_infrastructure(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set fuel capacity per infrastructure "+String.format("%.2f",dvalue),dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            case basic_fuel_capacity -> {
                if (add)
                {
                    properties.setBase_fuel(properties.getBase_fuel()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+" base fuel capacity, is now "+String.format("%.2f",properties.getBase_fuel()),dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setBase_fuel(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set base fuel capacity "+String.format("%.2f",dvalue),dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            case refinery_fuel_bonus -> {
                if (add)
                {
                    properties.setRefinery_fuel_bonus(properties.getRefinery_fuel_bonus()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt refinery fuel bonus, is now "+String.format("%.2f",properties.getRefinery_fuel_bonus())+"%",dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setRefinery_fuel_bonus(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set refinery fuel bonus "+String.format("%.2f",dvalue)+"%",dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            case embargoed -> {
                properties.setEmbargoed(bvalue);
                if (out!=null)
                    out.println(colorize("    "+(bvalue?"We are now embargoed, we can not trade":"We are no longer embargoed, we can trade"),!bvalue? GoodOutcome:BadOutcome));
            }
            case RulingParty -> {
                properties.setRulingParty(idvalue);
                if (out!=null)
                {
                    out.println(colorize("    Set ruling party...",Attribute.WHITE_TEXT()));
                    switch (idvalue)
                    {
                        case Nonaligned -> out.println(colorize("    Oh no! The non-aligned party has taken control of the government",Attribute.WHITE_TEXT()));
                        case Communist  -> out.println(colorize("    Bad news! the communist revolution has overthrown the government",Attribute.RED_TEXT()));
                        case Democratic -> out.println(colorize("    The nation is now a democracy!",Attribute.BLUE_TEXT()));
                        case Fascist ->    out.println(colorize("    Catastrophe! a fascist dictator has taken control of the government",Attribute.YELLOW_TEXT()));
                    }
                }
            }
            case fascistCoalition -> {
                properties.formCoalition(NationalProperties.ideology.Fascist,idvalue);
                if (out!=null)
                {
                    out.println(colorize("    Form Fascist coalition...",Attribute.YELLOW_TEXT()));
                    switch (idvalue)
                    {
                        case Nonaligned -> out.println(colorize("    In an effort to hold onto power, the autocratic party has merged with the fascist party",Attribute.YELLOW_TEXT()));
                        case Communist  -> out.println(colorize("    The working class has been ensnared by a fascist demagogue: all communist are now Fascist",Attribute.RED_TEXT()));
                        case Democratic -> out.println(colorize("    Oh Terror, all democratically leaning people are now fascist",Attribute.BLUE_TEXT()));
                        case Fascist ->    out.println(colorize("    Breaking news The Fascist party is, in fact, Fascist",Attribute.YELLOW_TEXT()));
                    }
                }
            }
            case communistCoalition -> {
                properties.formCoalition(NationalProperties.ideology.Communist,idvalue);
                if (out!=null)
                {
                    out.println(colorize("    Form Communist coalition...",Attribute.YELLOW_TEXT()));
                    switch (idvalue)
                    {
                        case Nonaligned -> out.println(colorize("    The communists ate the rich, and took over all their autocratic support",Attribute.RED_TEXT()));
                        case Communist  -> out.println(colorize("    The communist party has switched to a competing communist ideology",Attribute.RED_TEXT()));
                        case Democratic -> out.println(colorize("    Oh no, the democratic party developed class consciousness and became communist",Attribute.RED_TEXT()));
                        case Fascist ->    out.println(colorize("    The communists beat up the fascist so badly, that all their supporters switched sides",Attribute.RED_TEXT()));
                    }
                }
            }
            case democraticCoalition -> {
                properties.formCoalition(NationalProperties.ideology.Democratic,idvalue);
                if (out!=null)
                {
                    out.println(colorize("    Form democratic coalition...",Attribute.BLUE_TEXT()));
                    switch (idvalue)
                    {
                        case Nonaligned -> out.println(colorize("    The autocratic party has come to its senses and now supports democracy",Attribute.BLUE_TEXT()));
                        case Communist  -> out.println(colorize("    The communist saw the errors of their ways and became social democrats",Attribute.BLUE_TEXT()));
                        case Democratic -> out.println(colorize("    The people celebrates its liberty",Attribute.BLUE_TEXT()));
                        case Fascist ->    out.println(colorize("    The fascists realised they were the baddies, and now support democracy looked at each other, said \"wait, are we the baddies\" and promptly became democratic",Attribute.BLUE_TEXT()));
                    }
                }
            }
            case nonalignedCoalition -> {
                properties.formCoalition(NationalProperties.ideology.Nonaligned,idvalue);
                if (out!=null)
                {
                    out.println(colorize("    Form non-aligned coalition...",Attribute.WHITE_TEXT()));
                    switch (idvalue)
                    {
                        case Nonaligned -> out.println(colorize("    The autocratic party still doesn't really believe in anything",Attribute.WHITE_TEXT()));
                        case Communist  -> out.println(colorize("    The communist party leaders have lost touch with the proletariat and has become autocrats",Attribute.WHITE_TEXT()));
                        case Democratic -> out.println(colorize("    Democracy has declined into oligarchy and autocracy",Attribute.WHITE_TEXT()));
                        case Fascist ->    out.println(colorize("    The fascists have lost their fanaticism and become normal authoritarians",Attribute.WHITE_TEXT()));
                    }
                }
            }
            case fascism_growth -> {
                if (add)
                {
                    properties.setFascism_growth(properties.getFascism_growth()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt fascist daily growth, is now "+String.format("%.2f",properties.getFascism_growth())+"% pt per day",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setFascism_growth(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set fascist daily growth "+String.format("%.2f",dvalue)+"% pt per day",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case communism_growth ->
            {
                if (add)
                {
                    properties.setCommunism_growth(properties.getCommunism_growth()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt communist daily growth, is now "+String.format("%.2f",properties.getCommunism_growth())+"% pt per day",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setCommunism_growth(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set communist daily growth "+String.format("%.2f",dvalue)+"% pt per day",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case autocracy_growth -> {
                if (add)
                {
                    properties.setAutocracy_growth(properties.getAutocracy_growth()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt non-aligned daily growth, is now "+String.format("%.2f",properties.getAutocracy_growth())+"% pt per day",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setAutocracy_growth(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set non-aligned daily growth "+String.format("%.2f",dvalue)+"% pt per day",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case democracy_growth -> {
                if (add)
                {
                    properties.setDemocracy_growth(properties.getDemocracy_support()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt democracy daily growth, is now "+String.format("%.2f",properties.getDemocracy_growth())+"% pt per day",dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setDemocracy_growth(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set democracy daily growth "+String.format("%.2f",dvalue)+"% pt per day",dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            case fascism_support -> {
                if (add)
                {
                    properties.setFascism_support(properties.getFascism_support()+dvalue);
                    if (out!=null){
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+" % pt Fascism support, is now "+String.format("%.2f",properties.getFascism_support())+"%",dvalue<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    properties.setFascism_support(dvalue);
                    if (out!=null){
                        out.println(colorize("    Set Fascism support"+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                }
                }
            }
            case democracy_support -> {
                if (add)
                {
                    properties.setDemocracy_support(properties.getDemocracy_support()+dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt democracy support, is now "+String.format("%.2f",properties.getDemocracy_support())+"%",dvalue>0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    properties.setDemocracy_support(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set democracy support"+String.format("%.2f",dvalue)+"%",dvalue>0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                }
                }
            }
            case autocracy_support -> {
                if (add)
                {
                    properties.setAutocracy_support(properties.getAutocracy_support()+dvalue);
                    if (out!=null){
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt non-aligned support, is now "+String.format("%.2f",properties.getAutocracy_growth())+"%",dvalue<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    properties.setAutocracy_support(dvalue);
                    if (out!=null){
                        out.println(colorize("    Set non-aligned support "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                }
                }
            }
            case communism_support -> {
                if (add)
                {
                    properties.setCommunism_support(properties.getCommunism_support()+dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt Communism support, is now "+String.format("%.2f",properties.getCommunism_support())+"%",dvalue<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                }
                }
                else
                {
                    properties.setCommunism_support(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set Communism support "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                    out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                }
                }
            }
            case Efficiency_cap -> {
                if (add)
                {
                    properties.setEfficiency_cap(properties.getEfficiency_cap()+dvalue);
                    if (out!=null){
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% efficiency cap, is now "+String.format("%.2f",properties.getEfficiency_cap()),dvalue>0? GoodOutcome:BadOutcome));
                    }
                }
                else
                {
                    properties.setEfficiency_cap(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set efficiency cap "+String.format("%.2f",dvalue)+"%",dvalue>0? GoodOutcome:BadOutcome));
                    }
                }
            }
            case base_stability -> {
                if (add)
                {
                    properties.setBase_stability(properties.getBase_stability()+dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt base stability, is now "+String.format("%.2f",properties.getBase_stability())+"%",dvalue>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    }

                }
                else
                {
                    properties.setBase_stability(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set base stability "+String.format("%.2f",dvalue)+"%",dvalue>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case permanent_stability -> {
                if (add)
                {
                    properties.setPermanent_stability(properties.getPermanent_stability()+dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt permanent stability bonus, is now "+String.format("%.2f",properties.getPermanent_stability())+"% pt",dvalue>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setPermanent_stability(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set permanent stability "+String.format("%.2f",dvalue)+"% pt",dvalue>0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total stability is now " + String.format("%.2f",properties.getStability()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue > 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case base_factory_output -> {
                if (add)
                {
                    properties.setBase_factory_output(properties.getBase_factory_output()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt base factory output, is now "+String.format("%.2f",properties.getFactoryOutput())+"%",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setBase_stability(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set factory output "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case construction_speed -> {
                if (add)
                {
                    properties.setConstruction_speed(properties.getConstruction_speed()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt construction speed, is now "+String.format("%.2f",properties.getConstruction_speed())+"%",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setConstruction_speed(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set construction bonus "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case civ_construction_speed_bonus -> {
                if (add)
                {
                    properties.setCiv_construction_speed_bonus(properties.getCiv_construction_speed_bonus()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt additional construction bonus for civilian factories, is now "+String.format("%.2f",properties.getCiv_construction_speed_bonus())+"% pt",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setCiv_construction_speed_bonus(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set additional construction bonus for civilian factories "+String.format("%.2f",dvalue)+"% pt",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case mil_construction_speed_bonus -> {
                if (add)
                {
                    properties.setMil_construction_speed_bonus(properties.getMil_construction_speed_bonus()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt additional construction bonus for military factories, is now "+String.format("%.2f",properties.getMil_construction_speed_bonus())+"% pt",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setMil_construction_speed_bonus(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set additional construction bonus for military factories "+String.format("%.2f",dvalue)+"% pt",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case resource_gain_bonus -> {
                if (add)
                {
                    properties.setResource_gain_bonus(properties.getResource_gain_bonus()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt resource gain bonus, is now "+String.format("%.2f",properties.getResource_gain_bonus())+"%",dvalue<0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setResource_gain_bonus(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set resource gain bonus "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                }
            }
            case resources_to_market -> {
                if (add)
                {
                    properties.setResources_to_market(properties.getResources_to_market()+dvalue);
                    if (out!=null)
                        out.println(colorize("    Add "+String.format("%.2f",dvalue)+"% pt resources to market, is now "+String.format("%.2f",properties.getResources_to_market())+"%",dvalue>0? GoodOutcome:BadOutcome));
                }
                else
                {
                    properties.setResources_to_market(dvalue);
                    if (out!=null)
                        out.println(colorize("    Set resources to market "+String.format("%.2f",dvalue)+"%",dvalue>0? GoodOutcome:BadOutcome));
                }
            }
            case base_consumer_goods_ratio -> {
                if (add)
                {
                    properties.setBase_consumer_goods_ratio(properties.getBase_consumer_goods_ratio()+dvalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + String.format("%.2f", dvalue) + "% pt to base factories on consumer goods, is now " + String.format("%.2f", properties.getBase_consumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setBase_consumer_goods_ratio(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set base % of factories on consumer goods ratio: "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case base_consumer_goods_multiplier -> {
                if (add)
                {
                    properties.setBase_consumer_goods_multiplier(properties.getBase_consumer_goods_multiplier()+dvalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + String.format("%.2f", dvalue) + "% pt to consumer goods modifier, is now " + String.format("%.2f", properties.getBase_consumer_goods_multiplier()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setBase_consumer_goods_multiplier(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set consumer goods multiplier: "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                        out.println(colorize("    Total consumer goods ratio is now " + String.format("%.2f",properties.getConsumer_goods_ratio()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
            }
            case buildingSlotBonus -> {
                if (add)
                {
                    properties.setBuildingSlotBonus(properties.getBuildingSlotBonus()+dvalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + String.format("%.2f", dvalue) + "% pt to building slots bonus, is now " + String.format("%.2f", properties.getBase_consumer_goods_multiplier()) + "%", dvalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setBuildingSlotBonus(dvalue);
                    if (out!=null)
                    {
                        out.println(colorize("    Set building slots bonus: "+String.format("%.2f",dvalue)+"%",dvalue<0? GoodOutcome:BadOutcome));
                    }
                }
            }
            case special_steel -> {
                if (add)
                {
                    properties.setSpecial_steel(properties.getSpecial_steel()+ivalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + ivalue + " steel per day for special projects, is now " + properties.getSpecial_steel(), ivalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setSpecial_steel(ivalue);
                    if (out!=null)
                        out.println(colorize("    Set steel per day for special projects to " + ivalue, ivalue < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_chromium -> {
                if (add)
                {
                    properties.setSpecial_chromium(properties.getSpecial_chromium()+ivalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + ivalue + " chromium per day for special projects, is now " + properties.getSpecial_steel(), ivalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setSpecial_chromium(ivalue);
                    if (out!=null)
                        out.println(colorize("    Set chromium per day for special projects to " + ivalue, ivalue < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_tungsten -> {
                if (add)
                {
                    properties.setSpecial_tungsten(properties.getSpecial_tungsten()+ivalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + ivalue + " tungsten per day for special projects, is now " + properties.getSpecial_steel(), ivalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setSpecial_tungsten(ivalue);
                    if (out!=null)
                        out.println(colorize("    Set tungsten per day for special projects to " + ivalue, ivalue < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_aluminium -> {
                if (add)
                {
                    properties.setSpecial_aluminium(properties.getSpecial_aluminium()+ivalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + ivalue + " aluminium per day for special projects, is now " + properties.getSpecial_steel(), ivalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setSpecial_aluminium(ivalue);
                    if (out!=null)
                        out.println(colorize("    Set aluminium per day for special projects to " + ivalue, ivalue < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_rubber -> {
                if (add)
                {
                    properties.setSpecial_rubber(properties.getSpecial_rubber()+ivalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + ivalue + " rubber per day for special projects, is now " + properties.getSpecial_steel(), ivalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setSpecial_rubber(ivalue);
                    if (out!=null)
                        out.println(colorize("    Set rubber per day for special projects to " + ivalue, ivalue < 0 ? GoodOutcome : BadOutcome));
                }
            }
            case special_projects_civs -> {
                if (add)
                {
                    properties.setSpecial_projects_civs(properties.getSpecial_projects_civs()+ivalue);
                    if (out!=null) {
                        out.println(colorize("    Add " + ivalue + " civilian factories for special projects, is now " + properties.getSpecial_projects_civs(), ivalue < 0 ? GoodOutcome : BadOutcome));
                    }
                }
                else
                {
                    properties.setSpecial_projects_civs(ivalue);
                    if (out!=null)
                        out.println(colorize("    Set civilian factories for special projects to " + ivalue, ivalue < 0 ? GoodOutcome : BadOutcome));
                }
            }
        }
    }


    public void setName(String name) {
        this.name = name;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public void setValue(boolean value) {
        bvalue = value;
        dvalue = value?1.0:0.0;
        ivalue = value?1:0;
    }
    public void setValue(int value) {
        ivalue = value;
        dvalue = value;
        bvalue = value>0;
        switch (value%4)
        {
            case 0 -> idvalue= NationalProperties.ideology.Democratic;
            case 1 -> idvalue= NationalProperties.ideology.Communist;
            case 2 -> idvalue= NationalProperties.ideology.Nonaligned;
            case 3 -> idvalue= NationalProperties.ideology.Fascist;
        }
    }
    public void setValue(double value) {
        ivalue = (int)value;
        dvalue = value;
        bvalue = value>0;
        switch (ivalue%4)
        {
            case 0 -> idvalue= NationalProperties.ideology.Democratic;
            case 1 -> idvalue= NationalProperties.ideology.Communist;
            case 2 -> idvalue= NationalProperties.ideology.Nonaligned;
            case 3 -> idvalue= NationalProperties.ideology.Fascist;
        }
    }
    public void setValue(NationalProperties.ideology value) {
        idvalue=value;
        switch (idvalue)
        {
            case NationalProperties.ideology.Democratic -> {
                ivalue=0;
                dvalue=0;
                bvalue=false;
            }
            case NationalProperties.ideology.Communist -> {
                ivalue=1;
                dvalue=1;
                bvalue=true;
            }
            case NationalProperties.ideology.Nonaligned -> {
                ivalue=2;
                dvalue=2;
                bvalue=true;
            }
            case NationalProperties.ideology.Fascist -> {
                ivalue=3;
                dvalue=3;
                bvalue=true;
            }
        }
    }
    ///Load single event from json string
    public static Event loadEvent(String jsonString) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new StringReader(jsonString), new TypeReference<Event>() {});
    }
}
