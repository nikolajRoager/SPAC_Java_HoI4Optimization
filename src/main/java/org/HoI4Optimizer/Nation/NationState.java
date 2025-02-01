package org.HoI4Optimizer.Nation;


import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Building.SharedBuilding.CivilianFactory;
import org.HoI4Optimizer.Building.SharedBuilding.MilitaryFactory;
import org.HoI4Optimizer.Building.SharedBuilding.Refinery;
import org.HoI4Optimizer.Calender;
import org.HoI4Optimizer.Building.*;
import org.HoI4Optimizer.Nation.Event.Events;
import org.HoI4Optimizer.Nation.decision.decision;
import org.HoI4Optimizer.NationalConstants.NationalSetup;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.diogonunes.jcolor.Ansi.colorize;

/// The state of a nation, at some point in time, it is essentially a "save" we can return to
public class NationState  implements Cloneable
{
    /// A construction line for some building, with between 0 and 15 civilian factories assigned
    private static class constructionLine{
        /// The  thing we build
        public Building building;
        constructionLine(Building building){
            this.building = building;
        }
    }

    /// What time are we looking at now?
    private int day=0;

    /// A very long list of modifiers
    private NationalProperties properties;

    /// Reference to the Setup for this nation, has effects to modify propertyEvents above on certain days
    private NationalSetup setup;

    private String name;
    private List<State> states;

    /// All stateEvents in this nation, including all factories
    public List<State> getStates() {return states;}
    //Refineries don't need to be references from the nation directly
    /// References to all military factories in all core stateEvents
    public List<MilitaryFactory> militaryFactories;

    /// References to all civilian factories in all core stateEvents
    public List<CivilianFactory> civilianFactories;

    /// References to all civilian factories in all core stateEvents
    public List<Refinery> refineries;

    public int getDay() {return day;}

    public void setStates(List<State> states) {this.states = states;}

    public List<constructionLine> constructionLines;

    /// Load setup from a folder, assumed to be start of the simulation
    public NationState(String countryName,NationalSetup setup) throws IOException {
        try {states=State.loadState(Paths.get(countryName,"States.json").toString(),setup);}
        catch (IOException e) {throw new IOException("Error loading stateEvents of "+countryName+":"+e.getMessage());}

        try {properties=NationalProperties.loadProperties(Paths.get(countryName,"Properties.json").toString());}
        catch (IOException e) {throw new IOException("Error loading propertyEvents of "+countryName+":"+e.getMessage());}

        // Set up references to all military and civilian factories loaded
        civilianFactories=new ArrayList<>();
        militaryFactories=new ArrayList<>();
        refineries=new ArrayList<>();

        constructionLines=new ArrayList<>();

        for (var s : states)
        {
            civilianFactories.addAll(s.getCivilianFactories());
            militaryFactories.addAll(s.getMilitaryFactories());
            refineries.addAll(s.getRefineries());
        }

        name=countryName;
        this.setup=setup;
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

    public void apply(decision decision) throws RuntimeException
    {
        /*
        if (!states.contains(buildingDecision.getTarget()))
            throw new RuntimeException("Target state does not exist");
        switch (buildingDecision.getDecisionType())
        {
            case BuildRefinery -> {
                var New = buildingDecision.getTarget().buildRefinery(properties.getBuildingSlotBonus());
                refineries.add(New);
                constructionLines.add(new constructionLine(New));
            }
            case BuildCivilianFactory ->
            {
                var New = buildingDecision.getTarget().buildCivilianFactory(properties.getBuildingSlotBonus());
                civilianFactories.add(New);
                constructionLines.add(new constructionLine(New));
            }
            case BuildMilitaryFactory ->
            {
                var New = buildingDecision.getTarget().buildMilitaryFactory(properties.getBuildingSlotBonus());
                militaryFactories.add(New);
                constructionLines.add(new constructionLine(New));
            }
            case BuildInfrastructure -> {
                var New = buildingDecision.getTarget().buildInfrastructure();
                constructionLines.add(new constructionLine(New));
            }
        }*/
    }



    /// Give a written report of the nation
    /// @param Output where we print to (for example System.out)
    /// @param showProductionLines Show military factories with all their production lines
    /// @param showConstructionLines Show ongoing construction
    /// @param showFactories Also print all civilian and military factories and refineries
    /// @param showResources Show detailed breakdown of all resources
    /// @param showStates Show detailed breakdown of all stateEvents
    public void printReport(PrintStream Output,boolean showResources, boolean showProductionLines,boolean showConstructionLines , boolean showFactories,boolean showStates)
    {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        PrintStream out=new PrintStream(outputStream);

        out.println(colorize("==Status report for "+name+" at "+ Calender.getDate(day)+"==",Attribute.BOLD()));
        out.println(colorize("--Political report---"));
        double stab = properties.getStability();
        out.println(colorize("\tSocial stability..............:"+(int)(stab*100)+"%",(stab<0.3334?Attribute.BRIGHT_RED_TEXT():(stab>0.6667?Attribute.BRIGHT_GREEN_TEXT():Attribute.BRIGHT_YELLOW_TEXT()))));

        switch(properties.getRulingParty())
        {
            case Nonaligned ->
            {
                out.println(colorize("\tNon-aligned Government support:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.WHITE_BACK()),Attribute.BOLD()       ,Attribute.WHITE_TEXT()     ));
                out.println(colorize("\tDemocratic opposition support.:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK())                         ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tCommunist opposition support..:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK())                   ,Attribute.RED_TEXT()));
                out.println(colorize("\tFascist opposition support....:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK())                       ,Attribute.YELLOW_TEXT()    ));
            }
            case Democratic->
            {
                out.println(colorize("\tDemocratic Government support.:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK()),Attribute.BOLD()        ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tNon-aligned opposition support:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.WHITE_BACK())                        ,Attribute.WHITE_TEXT()     ));
                out.println(colorize("\tCommunist opposition support..:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK())                   ,Attribute.RED_TEXT()));
                out.println(colorize("\tFascist opposition support....:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK())                       ,Attribute.YELLOW_TEXT()    ));
            }
            case Communist ->
            {
                out.println(colorize("\tCommunist Government support..:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK()),Attribute.BOLD() ,Attribute.RED_TEXT()));
                out.println(colorize("\tDemocratic opposition support.:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK())                        ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tNon-aligned opposition support:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.WHITE_BACK())                       ,Attribute.WHITE_TEXT()     ));
                out.println(colorize("\tFascist opposition support....:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK())                      ,Attribute.YELLOW_TEXT()    ));
            }
            case Fascist->
            {
                out.println(colorize("\tFascist Government support....:"+ printBarPlot((int)(100*properties.getFascism_support())  ,Attribute.YELLOW_BACK()),Attribute.BOLD(),Attribute.YELLOW_TEXT() ));
                out.println(colorize("\tDemocratic opposition support.:"+ printBarPlot((int)(100*properties.getDemocracy_support()),Attribute.BLUE_BACK())                ,Attribute.BLUE_TEXT()      ));
                out.println(colorize("\tNon-aligned opposition support:"+ printBarPlot((int)(100*properties.getAutocracy_support()),Attribute.RED_BACK())          ,Attribute.RED_TEXT()));
                out.println(colorize("\tCommunist opposition support..:"+ printBarPlot((int)(100*properties.getCommunism_support()),Attribute.RED_BACK())          ,Attribute.RED_TEXT()));
            }
        }

        int factories =0;
        for (var f : militaryFactories)
            if (!f.getUnderConstruction())
                factories++;
        for (var f : civilianFactories)
            if (!f.getUnderConstruction())
                factories++;

        double cg = properties.getConsumer_goods_ratio();
        int cg_factories = (int)(factories*cg);

        out.println(colorize("--Industrial report--"));
        out.println(colorize("\t...Military Sector...",Attribute.ITALIC(),Attribute.BRIGHT_GREEN_TEXT()));
        out.println(colorize("\t\t Factory output bonus..............:",Attribute.BRIGHT_GREEN_TEXT())+ printGoodModifier(properties.getFactoryOutput()));
        out.println(colorize("\t\t Efficiency cap....................:",Attribute.BRIGHT_GREEN_TEXT())+ printGoodModifier(properties.getEfficiency_cap()));
        out.println(colorize("\t\t Military factories................: "+militaryFactories.size(),Attribute.BRIGHT_GREEN_TEXT()));
        if (showFactories || showProductionLines)
            for (var f : militaryFactories)
            {
                f.printReport(out,"\t\t\t");
            }

        out.println(colorize("\t...Resource and Fuel sector...",Attribute.ITALIC(),Attribute.BLUE_TEXT()));
        out.println(colorize("\t\tResource gain bonus................:",Attribute.BLUE_TEXT())+printGoodModifier(properties.getResource_gain_bonus()));
        out.println(colorize("\t\tResources exported.................:",Attribute.BLUE_TEXT())+printBadModifier(properties.getResources_to_market()));
        out.println(colorize("\t\tFuel gain per oil..................:",Attribute.BLUE_TEXT())+printGoodModifier(properties.getNatural_fuel_bonus()));
        out.println(colorize("\t\tSynthetic Fuel per refinery........:",Attribute.BLUE_TEXT())+printGoodModifier(properties.getRefinery_fuel_bonus()));
        out.println(colorize("\t\tSynthetic Rubber per refinery......: ",Attribute.BLUE_TEXT())+properties.getRubber_per_refineries());

        if (refineries.isEmpty())
            out.println(colorize("\t\tNo synthetic refineries owned!", Attribute.BLUE_TEXT()));
        else
            out.println(colorize("\t\tRefineries.....................:"+refineries.size(),Attribute.BLUE_TEXT()));
        if (showFactories)
            for (var f : refineries)
            {
                f.printReport(out,"\t\t\t");
            }

        //Table header
        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %4s %9s %6s %8s %5s %8s%n", "", "Oil", "Aluminium", "Rubber", "Tungsten","Steel","Chromium"),Attribute.BRIGHT_WHITE_TEXT(),Attribute.BOLD()));
        //Resources available
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



        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "Production in stateEvents", oil, aluminium, rubber, tungsten, steel, chromium),Attribute.GREEN_TEXT()));
        //THIS IS A ROUNDING MESS, but it is game accurate!
        rubber*= (int) (1+properties.getResource_gain_bonus());
        oil*=(int)(1+properties.getResource_gain_bonus());
        aluminium*=(int)(1+properties.getResource_gain_bonus());
        chromium*=(int)(1+properties.getResource_gain_bonus());
        steel*=(int)(1+properties.getResource_gain_bonus());
        tungsten*=(int)(1+properties.getResource_gain_bonus());
        double rtm = properties.getResources_to_market();
        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "+"+(int)(100* properties.getResource_gain_bonus())+"% excavation tech", oil, aluminium, rubber, tungsten, steel, chromium),Attribute.GREEN_TEXT()));
        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "Exporting "+(int)(100*rtm)+"%", (int)(rtm*oil), (int)(rtm*aluminium), (int)(rtm*rubber), (int)(rtm*tungsten), (int)(rtm*steel), (int)(rtm*chromium)),Attribute.RED_TEXT()));
        oil-=(int)(rtm*oil);
        rubber-=(int)(rtm*rubber);
        aluminium-=(int)(rtm*aluminium);
        tungsten-=(int)(rtm*tungsten);
        steel-=(int)(rtm*steel);
        chromium-=(int)(rtm*chromium);

        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "Import", 0, 0, 0, 0, 0, 0),Attribute.GREEN_TEXT()));
        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "Special projects", "-", 0, 0, 0, 0, 0),Attribute.RED_TEXT()));
        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "Used for production", "-", 0, 0, 0, 0, 0),Attribute.RED_TEXT()));
        if (showResources)
            out.print(colorize(String.format("\t\t%-21s %3s %6s %8s %7s %6s %7s%n", "Balance", oil, aluminium, rubber, tungsten, steel, chromium),Attribute.WHITE_TEXT()));
        else
        {
            //Print short summary
            out.println(colorize("Balance: ",Attribute.BLUE_TEXT())+" oil: "+oil+" aluminium: "+aluminium+" rubber: "+rubber+" tungsten: "+tungsten+" steel: "+steel+" chromium: "+chromium);
        }

        double baseFuel =properties.getBase_fuel();
        double naturalFuel =properties.getBase_fuel()*(1+properties.getNatural_fuel_bonus())*oil;
        double refineryFuel=properties.getBase_fuel()*(1+properties.getRefinery_fuel_bonus())*refineries.size();

        out.println(colorize("\t\tFuel gain per day..................:",Attribute.BLUE_TEXT())+String.format("%.2f",baseFuel+naturalFuel+refineryFuel));

        //Missing fuel capacity

        out.println(colorize("\t...Construction sector...",Attribute.ITALIC(),Attribute.BRIGHT_YELLOW_TEXT()));
        out.println(colorize("\t\tConstruction speed bonus...........:",Attribute.BRIGHT_YELLOW_TEXT())+ printGoodModifier(properties.getConstruction_speed()));
        out.println(colorize("\t\tCivilian factory construction bonus:",Attribute.BRIGHT_YELLOW_TEXT())+ printGoodModifier(properties.getCiv_construction_speed_bonus()));
        out.println(colorize("\t\tMilitary factory construction bonus:",Attribute.BRIGHT_YELLOW_TEXT())+ printGoodModifier(properties.getMil_construction_speed_bonus()));
        out.println(colorize("\t\tMilitary + Civilian factories......: "+civilianFactories.size(),Attribute.BRIGHT_YELLOW_TEXT())+" +"+colorize(" "+militaryFactories.size(),Attribute.BRIGHT_GREEN_TEXT())+" = "+factories);
        out.println(colorize("\t\tRequired for consumer goods........:",Attribute.BRIGHT_YELLOW_TEXT())+ printBadModifier(cg)+" * "+factories+ " = "+colorize(cg_factories+"",Attribute.RED_TEXT()));
        out.println(colorize("\t\tRequired for special projects......: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize(""+properties.getSpecial_projects_civs(),Attribute.WHITE_TEXT()));
        int exportGoods=0;
        out.println(colorize("\t\tProducing export goods.............: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize(""+0,Attribute.WHITE_TEXT()));
        int unassignedCivs = (civilianFactories.size()-properties.getSpecial_projects_civs()-cg_factories-exportGoods);
        out.println(colorize("\t\tRemains available for construction.: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize("+"+unassignedCivs,Attribute.BRIGHT_YELLOW_TEXT()));
        if (showFactories) {
            out.println(colorize("\t\t Our " + civilianFactories.size() + " civilian factories:", Attribute.BRIGHT_YELLOW_TEXT()));
            for (var f : civilianFactories) {
                if (exportGoods > 0) {
                    --exportGoods;
                    f.printReport(out, colorize("Trade goods", Attribute.YELLOW_TEXT()), "\t\t\t");
                } else if (cg_factories > 0) {
                    --cg_factories;
                    f.printReport(out, colorize("Consumer products", Attribute.RED_TEXT()), "\t\t\t");
                } else
                    f.printReport(out, colorize("Construction", Attribute.BRIGHT_YELLOW_TEXT()), "\t\t\t");
            }
        }


        out.println();
        out.println(colorize("\t\tOngoing construction projects: ",Attribute.BRIGHT_YELLOW_TEXT())+colorize("+"+(constructionLines.size()),Attribute.BRIGHT_YELLOW_TEXT()));

        if (showConstructionLines) {
            for (var line : constructionLines) {
                //Calculate assigned factories directly
                out.println(colorize("\t\t\tBuilding " + line.building.getName() + " in " + line.building.getLocation().getName() + " production: " + line.building.getCIC_invested() + "/" + Building.getCost(line.building.getMyType()) + " CIC Assigned factories " + Math.min(15, unassignedCivs) + "/15", (line.building.getMyType() == Building.type.Civilian ? Attribute.YELLOW_TEXT() : (line.building.getMyType() == Building.type.Military ? Attribute.GREEN_TEXT() : Attribute.BLUE_TEXT()))));
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

            for (var s : states)
            {
                var newState = s.clone();
                clone.states.add(newState);
                clone.refineries.addAll(newState.getRefineries());
                clone.civilianFactories.addAll(newState.getCivilianFactories());
                clone.militaryFactories.addAll(newState.getMilitaryFactories());
            }


            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /// Apply an event to my propertyEvents, or my stateEvents
    public void apply(Events theseEvents, PrintStream out) {
        if (out!=null)
        {
            out.println(colorize("==An event has happened==", Attribute.BOLD(), Attribute.BLUE_TEXT()));
            out.println(colorize(Calender.getDate(day)+" (day "+day+")",Attribute.WHITE_TEXT()));
            out.println(colorize("    "+ theseEvents.name(),Attribute.ITALIC()));
        }
        if (theseEvents.propertyEvents()!=null)
            for (var propertyEvent : theseEvents.propertyEvents())
               properties.apply(propertyEvent,out);
        if (theseEvents.stateEvents()!=null)
            for (var stateEvent : theseEvents.stateEvents())
                states.get(stateEvent.stateID()).apply(stateEvent,properties,out);
    }

    ///Step forward a number of days, or until a decision is required, only print if some important event happened
    public void update(int days,PrintStream out)
    {
        for (int d = 0; d < days; d++) {
            ++day;

            // First step, update production
            // TBD

            // Now, update construction
            //First get the number of civilian factories available for construction
            int factories =0;
            for (var f : militaryFactories)
                if (!f.getUnderConstruction())
                    factories++;
            for (var f : civilianFactories)
                if (!f.getUnderConstruction())
                    factories++;

            double cg = properties.getConsumer_goods_ratio();
            int cg_factories = (int)(factories*cg);
            int exportGoods=0;
            int unassignedCivs = (civilianFactories.size()-properties.getSpecial_projects_civs()-cg_factories-exportGoods);

            boolean finished=false;

            for (int i = constructionLines.size()-1; i>=0; i--) {
                var c = constructionLines.get(i);
                //Add 5 daily CIC plus 20%,40%,60%,80% or 100% bonus per level infrastructure, plus construction speed bonus
                if (c.building.construct(5*Math.min(unassignedCivs,15)*(1+0.2*c.building.getLocation().getInfrastructure())*(1+properties.getConstruction_speed()
                        +(c.building instanceof MilitaryFactory ? properties.getMil_construction_speed_bonus() :0.0)
                        +(c.building instanceof CivilianFactory ? properties.getCiv_construction_speed_bonus() :0.0)
                )))
                {
                    finished=true;
                    constructionLines.remove(i);
                }

                //We used up to 15 factories on this
                unassignedCivs=Math.max(0,unassignedCivs-15);
            }

            //Apply events at the end of the day (I believe it is similar to the game)
            for (Events e : setup.getEvent(day))
            {
                apply(e,out);
            }

            //Oh no, a decision has to be made of what to do with the free factories!
            //This will only trigger one day late, just like in game!
            if (unassignedCivs>0)
            {
                out.println(colorize("Stopped due to unassigned civilian factories!",Attribute.BRIGHT_YELLOW_TEXT()));
                return;
            }
        }
    }
}