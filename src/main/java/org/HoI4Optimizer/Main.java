package org.HoI4Optimizer;

import java.io.IOException;
import java.util.*;
import java.util.List;

//A frame containing our plots
import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Building.stateBuilding.stateBuilding;
import org.HoI4Optimizer.Nation.*;
import org.HoI4Optimizer.Nation.Events.PropertyEvent;
import org.HoI4Optimizer.NationalConstants.NationalSetup;

import static com.diogonunes.jcolor.Ansi.colorize;


public class Main
{
    //Main function, controls what commands get done from commandline
    public static void main(String[] args)
    {

        NationalSetup Setup;
        //Load setup
        try {
            Setup = new NationalSetup("Poland","PlanE39");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        var MyNation =Setup.buildNation();
        MyNation.apply(new BuildingDecision(MyNation.getStates().getFirst(), BuildingDecision.buildingDecisionType.BuildInfrastructure));
        //A little class to store commands, arguments and their descriptions,

        //Create a list of commands to interact with the program
        Map<String,CommandlineCommand> commands=new HashMap<>();
        commands.put("print",new CommandlineCommand("print","Print the current state of the nation, with all current stats",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("showResources", "Show detailed breakdown of resources in nation", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showProduction", "Show detailed breakdown of all Military factories and production lines", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showConstruction", "Show detailed breakdown of all National construction projects", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showFactories", "Show detailed breakdown of all Factories: civilian, Military, and Chemical", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showStates", "Show detailed breakdown of all States in the nation", CommandlineCommand.Argument.type.Flag, true, "false"),
                })));
        commands.put("event",new CommandlineCommand("event","Create an event for the nation now, or some day in the future (launches event JSon editor)",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("day", "When will this event be executed? skip to execute now.", CommandlineCommand.Argument.type.Integer, true, "0"),
                })));
        commands.put("step",new CommandlineCommand("step","simulate forward a specific number of days, or until a construction or production decision becomes available",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("days", "how many days should we at most try to step forward, we will stop once a decision needs to be made", CommandlineCommand.Argument.type.Integer, true, "1"),
                })));
        //Put these two last, so it appears at the bottom when we print
        commands.put("help",new CommandlineCommand("help","Print this list of commands",
                List.of(new CommandlineCommand.Argument[]{
                })));
        commands.put("quit",new CommandlineCommand("quit","stop program",
                List.of(new CommandlineCommand.Argument[]{
                })));

        //I use a blue box (or ### without colours) before the text to make it clear where the commands are
        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize(" We have loaded the setup at the start of the game, you now have the following choices", Attribute.BOLD(), Attribute.BLUE_TEXT()));

        boolean printHelp = true;
        do {


            if (printHelp) {
                //Print commands, the index is used to color-code the different commands
                int i = 0;
                for (var C : commands.values())
                    C.print((i++) % CommandlineCommand.command_colors.length);
                printHelp = false;
            }
            else
                System.out.println(colorize("####", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize(" write \"help\" to see commands", Attribute.BOLD(), Attribute.BLUE_TEXT()));

            System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()));
            System.out.println(colorize(" ",Attribute.MAGENTA_BACK())+" "+colorize(MyNation.getDay()+" ("+Calender.getDate(MyNation.getDay())+")", Attribute.ITALIC(),Attribute.WHITE_TEXT()));
            System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK())+colorize("$",Attribute.BLACK_TEXT(),Attribute.MAGENTA_BACK())+" ");
            String input = System.console().readLine();

            List<String> coms = List.of(input.split(" "));

            for (var C : coms)
            {
                C=C.trim();
            }

            //Save some longer commands in lists, so we can read the arguments individually
            var PrintArgs=commands.get("print").match(coms);
            var eventArgs=commands.get("event").match(coms);
            var stepArgs=commands.get("step").match(coms);
            //Check which arguments we match
            if (commands.get("help").match(coms)!=null)
            {
                printHelp = true;
            }
            else if (commands.get("quit").match(coms)!=null)
            {
                return;
            }
            else if ((PrintArgs)!=null)
            {
                MyNation.printReport(System.out,PrintArgs[0].equalsIgnoreCase("true"),PrintArgs[1].equalsIgnoreCase("true"),PrintArgs[2].equalsIgnoreCase("true"),PrintArgs[3].equalsIgnoreCase("true"),PrintArgs[4].equalsIgnoreCase("true"));
            }
            else if ((stepArgs)!=null)
            {
                MyNation.update(Math.max(1,Integer.parseInt(stepArgs[0])),System.out);
            }
            else if (eventArgs!=null)
            {
                Event thisEvent=null;
                int day=Integer.parseInt(eventArgs[0]);
                boolean instant = day==0;

                //A little quick event editor
                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize(" Started event editor for new event to be applied "+(instant?"now":"at "+Calender.getDate(day)), Attribute.BOLD(), Attribute.GREEN_TEXT()));

                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Enter data, finish with enter", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Name: ", Attribute.BOLD(), Attribute.RED_TEXT(),Attribute.RAPID_BLINK()));
                String nameInput = System.console().readLine();

                //Keep asking the user for a valid modify until they acquiesce
                PropertyEvent.Target target=null;
                while (target==null) {
                    System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       The following targets exist: ", Attribute.ITALIC(), Attribute.GREEN_TEXT()));

                    var targets = PropertyEvent.Target.values();
                    for (int j = 0; j < targets.length; ++j) {
                        System.out.print(colorize("\t" + j + ":", Attribute.ITALIC(), Attribute.BRIGHT_WHITE_TEXT()) + colorize(targets[j] + (((j % 5 == 0) || (j + 1 == targets.length)) ? "\n" : ""), Attribute.WHITE_TEXT(), Attribute.ITALIC()));
                    }


                    System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Enter one of the names, or their number", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                    System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Target: ", Attribute.BOLD(), Attribute.RED_TEXT(), Attribute.RAPID_BLINK()));
                    String targetInput = System.console().readLine().trim();

                    //At best the user inputted an int
                    try {
                        int id = Integer.parseInt(targetInput);
                        target = targets[id];
                    } catch (NumberFormatException e) {
                        //At worst, we have to find it
                        boolean noneFound = true;
                        for (var t : targets) {
                            if (t.toString().equalsIgnoreCase(targetInput)) {
                                target = t;
                                noneFound = false;
                                break;
                            }
                        }
                        if (noneFound)
                        {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Not a valid modify!", Attribute.BOLD(), Attribute.BRIGHT_RED_TEXT()));
                        }
                    }
                }

                //Ideology values can not be added to, everything else can be added to
                if (target!= PropertyEvent.Target.democraticCoalition && target!= PropertyEvent.Target.fascistCoalition && target!= PropertyEvent.Target.communistCoalition && target != PropertyEvent.Target.nonalignedCoalition && target != PropertyEvent.Target.RulingParty)
                {
                    //Add to existing value, or replace value
                    boolean add;
                    System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Set new value, or add to value? write add or set finish with enter", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                    System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Operation: ", Attribute.BOLD(), Attribute.RED_TEXT()));

                    while (true) {
                        String operationInput = System.console().readLine().trim();
                        if (operationInput.equalsIgnoreCase("add"))
                        {
                            add=true;
                            break;
                        } else if (operationInput.equalsIgnoreCase("set")) {
                            add=false;
                            break;
                        }
                        else
                        {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Write add or set!", Attribute.BOLD(), Attribute.BRIGHT_RED_TEXT()));
                        }
                    }

                    //These are all the integers
                    if (target== PropertyEvent.Target.special_steel || target== PropertyEvent.Target.special_aluminium || target== PropertyEvent.Target.special_chromium|| target== PropertyEvent.Target.special_rubber || target== PropertyEvent.Target.special_tungsten || target== PropertyEvent.Target.special_projects_civs)
                    {
                        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       write new integer value? finish with enter", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Value: ", Attribute.BOLD(), Attribute.RED_TEXT()));


                        while (true) {
                            String targetInput = System.console().readLine().trim();

                            //At best the user inputted an int
                            try {
                                int integer = Integer.parseInt(targetInput);
                                thisEvent=new Event(nameInput,target,integer,add);
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Not convertible to integer!, try again: ", Attribute.BOLD(), Attribute.BRIGHT_RED_TEXT()));
                            }
                        }
                    }
                    else
                    {//This is some form of double
                        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       write new floating point value? finish with enter", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Value: ", Attribute.BOLD(), Attribute.RED_TEXT()));


                        while (true) {
                            String targetInput = System.console().readLine().trim();

                            //At best the user inputted an int
                            try {
                                double decimal= Double.parseDouble(targetInput);
                                thisEvent=new Event(nameInput,target,decimal,add);
                                break;
                            } catch (NumberFormatException e) {
                                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Not convertible to integer!, try again: ", Attribute.BOLD(), Attribute.BRIGHT_RED_TEXT()));
                            }
                        }
                    }
                }
                else
                {
                    //These are the only cases where we expect an ideology
                    Ideology id;
                    switch (target)
                    {
                        case nonalignedCoalition->{
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       What ideology should join the non-aligned party (converting all their support to non-aligned)", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        }
                        case fascistCoalition->{
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       What ideology should join the fascists (converting all their support to fascism)", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        }
                        case democraticCoalition -> {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       What ideology should join the democrats (converting all their support to democracy)", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        }
                        case communistCoalition -> {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       What ideology should join the communists (converting all their support to communism)", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        }
                        case RulingParty -> {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       What ideology should take power?", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                        }
                    }
                    System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Write either: ", Attribute.ITALIC(), Attribute.GREEN_TEXT())+colorize("democratic",Attribute.BLUE_TEXT())+colorize(", ",Attribute.GREEN_TEXT())+colorize("communist",Attribute.RED_TEXT())+colorize(", ",Attribute.GREEN_TEXT())+colorize("non-align")+colorize(", or ",Attribute.GREEN_TEXT())+colorize("fascist",Attribute.YELLOW_TEXT()));
                    while(true)
                    {
                        System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     ideology: ", Attribute.ITALIC(), Attribute.RED_TEXT()));
                        String targetInput = System.console().readLine().trim();
                        if (targetInput.equalsIgnoreCase("democratic") || targetInput.equalsIgnoreCase("democracy") || targetInput.equalsIgnoreCase("d"))
                        {
                            id= Ideology.Democratic;
                            break;
                        } else if (targetInput.equalsIgnoreCase("communist") || targetInput.equalsIgnoreCase("communism") || targetInput.equalsIgnoreCase("c")) {
                            id= Ideology.Communist;
                            break;
                        } else if (targetInput.equalsIgnoreCase("nonaligned") || targetInput.equalsIgnoreCase("authoritarian") || targetInput.equalsIgnoreCase("autocratic") || targetInput.equalsIgnoreCase("a") || targetInput.equalsIgnoreCase("n")) {
                            id= Ideology.Nonaligned;
                            break;
                        } else if (targetInput.equalsIgnoreCase("fascist") || targetInput.equalsIgnoreCase("evil") || targetInput.equalsIgnoreCase("fascism") || targetInput.equalsIgnoreCase("f")) {
                            id= Ideology.Fascist;
                            break;
                        }
                        System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     not a valid ideology, must be: ", Attribute.ITALIC(), Attribute.RED_TEXT())+colorize("democratic",Attribute.BLUE_TEXT())+colorize(", ",Attribute.GREEN_TEXT())+colorize("communist",Attribute.RED_TEXT())+colorize(", ",Attribute.GREEN_TEXT())+colorize("non-align")+colorize(", or ",Attribute.GREEN_TEXT())+colorize("fascist",Attribute.YELLOW_TEXT()));
                    }
                    thisEvent=new Event(nameInput,target,id);
                }

                //Ok now either apply it now, or add it to the list of events
                if (day==0)
                {
                    MyNation.apply(thisEvent,System.out);
                }
            }
        } while (true);
    }
}