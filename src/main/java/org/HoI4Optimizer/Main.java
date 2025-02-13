package org.HoI4Optimizer;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

//A frame containing our plots
import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Nation.*;
import org.HoI4Optimizer.NationalConstants.NationalSetup;

import javax.swing.*;

import static com.diogonunes.jcolor.Ansi.colorize;


public class Main
{
    //Main function, controls what commands get done from commandline
    public static void main(String[] args)
    {
        NationalSetup Setup;
        //Load setup
        try {
            if (args.length<2)
                Setup = new NationalSetup("Poland","PlanG38");
            else
                Setup = new NationalSetup(args[0],args[1]);
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        NationState myNation =Setup.buildNation();
        //Quicksaves, they are not saved when the program quits, but can be useed to test stuff
        Map<String,NationState> saves=new HashMap<>();
        //Before every decision, the simulation autosaves
        Map<Integer,NationState> autosaves=new HashMap<>();
        saves.put("start",myNation.clone());


        //Get the number after the word day with regex (^: start of string, day: literal match: (\\d+): multi-digit number captured as group 1, $: end of string
        Pattern autosavePattern = Pattern.compile("^day(\\d+)$");

        //Create a list of commands to interact with the program
        Map<String,CommandlineCommand> commands=new HashMap<>();
        commands.put("print",new CommandlineCommand("print","Print the current state of the nation, with all current stats",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("showResources", "Show detailed breakdown of resources in nation", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showProduction", "Show detailed breakdown of all Military factories and production lines", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showConstruction", "Show detailed breakdown of all National construction projects", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showFactories", "Show detailed breakdown of all Factories: civilian, Military, and Chemical", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showStates", "Show detailed breakdown of all States in the nation", CommandlineCommand.Argument.type.Flag, true, "false"),
                        new CommandlineCommand.Argument("showDecisions", "Show all decisions available now", CommandlineCommand.Argument.type.Flag, true, "false"),
                })));
        commands.put("step",new CommandlineCommand("step","(Only if no decisions need to be made!) simulate forward a specific number of days, or until an event happens, or construction or production decision becomes available",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("days", "how many days should we at most try to step forward, we will stop once a decision needs to be made; if left our (or set to 0) we will step until an event happens", CommandlineCommand.Argument.type.Integer, true, "0"),
                })));
        commands.put("decide",new CommandlineCommand("decide","(Only if decisions are available!) do something with your nation",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("id", "which decision do you want to take", CommandlineCommand.Argument.type.Integer, false, "1"),
                })));
        //Put these two last, so it appears at the bottom when we print
        commands.put("help",new CommandlineCommand("help","Print this list of commands",
                List.of(new CommandlineCommand.Argument[]{
                })));
        //Put these two last, so it appears at the bottom when we print
        commands.put("show",new CommandlineCommand("show","show all data collected by the national institute of statistics during the simulation",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("save", "Save the graphs as pdf, (OVERWRITE existing pdfs)", CommandlineCommand.Argument.type.Flag, true, "false"),
                })));
        //Put these two last, so it appears at the bottom when we print
        commands.put("showFactories",new CommandlineCommand("showFactories","show stats for a particular factory",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("mil", "The factory to show data for, out of range shows all", CommandlineCommand.Argument.type.Integer, true, "999"),
                        new CommandlineCommand.Argument("save", "Save the graphs as pdf, (OVERWRITE existing pdfs)", CommandlineCommand.Argument.type.Flag, true, "false"),
                })));

        commands.put("save",new CommandlineCommand("save","make an in-memory save you can return to (will not persist when you close the program)",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("name", "The name of the save (Should be a single word)", CommandlineCommand.Argument.type.String, false, "mySave"),
                })));

        commands.put("load",new CommandlineCommand("load","try to load your save with this name",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("name", "The name of the save (leave empty to get list of saves)", CommandlineCommand.Argument.type.String, true, "null"),
                })));
        commands.put("quit",new CommandlineCommand("quit","stop program",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("force", "Close all existing windows", CommandlineCommand.Argument.type.Flag, true, "false"),
                })));

        //I use a blue box (or ### without colours) before the text to make it clear where the commands are
        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize(" We have loaded the setup at the start of the game, you now have the following choices", Attribute.BOLD(), Attribute.BLUE_TEXT()));

        boolean printHelp = true;
        do {
            //Load decisions the player can make
            int decisions = myNation.getDecisions();
            int tradeDecisions = myNation.getTradeDecisions();
            autosaves.put(myNation.getDay(),myNation.clone());

            if (printHelp) {
                //Print commands, the index is used to color-code the different commands
                int i = 0;
                for (var C : commands.values())
                    C.print((i++) % CommandlineCommand.command_colors.length);
                printHelp = false;
            }

            //If there are available decisions, tell the user where they can find the
            if (decisions>0)
                System.out.println(colorize("####", Attribute.BOLD(), Attribute.GREEN_TEXT(), Attribute.GREEN_BACK()) + colorize("There are available decisions, write print showDecisions to see them", Attribute.BOLD(), Attribute.GREEN_TEXT()));
            if (tradeDecisions>0)
                System.out.println(colorize("####", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("There are available optional trade decisions, write print showDecisions to see them", Attribute.ITALIC(), Attribute.BLUE_TEXT()));

            System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()));
            System.out.println(colorize(" ",Attribute.MAGENTA_BACK())+" "+colorize("("+ MyCalendar.getDate(myNation.getDay())+" day "+myNation.getDay()+")", Attribute.ITALIC(),Attribute.WHITE_TEXT()));
            System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK())+colorize("$",Attribute.BLACK_TEXT(),Attribute.MAGENTA_BACK())+" ");

            String input = System.console().readLine();

            List<String> command_inputs = new ArrayList<>(List.of(input.split(" ")));
            command_inputs.replaceAll(String::trim);

            //Save some longer commands in lists, so we can read the arguments individually
            String[] printArgs=null;
            String[] stepArgs=null;
            String[] showArgs=null;
            String[] loadArgs=null;
            String[] saveArgs=null;
            String[] showFactoryArgs=null;
            String[] decisionArgs=null;
            String[] quitArg=null;
            try
            {
                printArgs = commands.get("print").match(command_inputs);
                stepArgs = commands.get("step").match(command_inputs);
                showArgs = commands.get("show").match(command_inputs);
                loadArgs = commands.get("load").match(command_inputs);
                saveArgs = commands.get("save").match(command_inputs);
                showFactoryArgs = commands.get("showFactories").match(command_inputs);
                decisionArgs=commands.get("decide").match(command_inputs);
                quitArg=commands.get("quit").match(command_inputs);

                //Check which arguments we match, these will not throw errors as they don't have arguments which can be wrong
                if (commands.get("help").match(command_inputs)!=null)
                {
                    printHelp = true;
                }
                else if (showFactoryArgs!=null)
                {
                    myNation.displayPlots( Integer.parseInt(showFactoryArgs[0]),showFactoryArgs[1].equalsIgnoreCase("true"));
                }
                else if (saveArgs!=null)
                {

                    Matcher matcher = autosavePattern.matcher(saveArgs[0]);
                    //Check if this is an autosave... that is not legal
                    if (matcher.matches() || saveArgs[0].equalsIgnoreCase("null"))
                    {
                        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Illegal save name", Attribute.BOLD(), Attribute.RED_TEXT()));
                    }
                    else {
                        saves.put(saveArgs[0], myNation.clone());
                        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Saved as "+saveArgs[0], Attribute.BOLD(), Attribute.GREEN_TEXT()));
                    }
                }
                else if (loadArgs!=null)
                {

                    Matcher matcher = autosavePattern.matcher(loadArgs[0]);
                    //Check if this is an autosave command, or a manual save:
                    boolean isAutosave = matcher.matches();
                    int thisDay = isAutosave? Integer.parseInt(matcher.group(1)) : -1/*doesn't matter for not autosave*/;
                    isAutosave=isAutosave && autosaves.containsKey(thisDay);

                    if (loadArgs[0].equalsIgnoreCase("null") || (!saves.containsKey(loadArgs[0]) && !isAutosave))
                    {
                        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Valid saves: ", Attribute.BOLD(), Attribute.RED_TEXT()));
                        if (!saves.isEmpty()) {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Manual:", Attribute.BOLD(), Attribute.GREEN_TEXT()));
                            for (String save : saves.keySet())
                                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("\t" + save, Attribute.BOLD(), Attribute.GREEN_TEXT()));
                        }
                        else
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("No manual saves", Attribute.BOLD(), Attribute.GREEN_TEXT()));
                        if (!autosaves.isEmpty()) {
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Autosaves:", Attribute.BOLD(), Attribute.YELLOW_TEXT()));
                            for (int save : autosaves.keySet())
                                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("\tday" + save, Attribute.BOLD(), Attribute.YELLOW_TEXT()));
                        }
                        else
                            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("No autosaves", Attribute.BOLD(), Attribute.YELLOW_TEXT()));
                    }
                    else
                    {
                        //If autosave matches, it takes priority
                        //Disregard the compiler, it is NOT always true
                        if (isAutosave)
                        {
                            myNation = autosaves.get(thisDay).clone();
                        }
                        else//Regular save, we already checked it works
                        {
                            myNation = saves.get(loadArgs[0]).clone();
                        }

                        //Delete auto-saves after this day (they are invalid now)
                        for (int i :autosaves.keySet())
                            if (i>myNation.getDay())
                                autosaves.remove(i);

                    }
                }
                else if (showArgs!=null)
                {
                    myNation.displayPlots( showArgs[0].equalsIgnoreCase("true"));
                }
                else if (quitArg!=null)
                {
                    if (hasOpenWindows()) {

                        //Force quit all windows
                        if (quitArg[0].equalsIgnoreCase("true")) {
                            for (Frame frame : Frame.getFrames())
                                if (frame.isVisible() && frame instanceof JFrame)
                                    frame.dispose(); // Close the window
                            return;
                        }
                        else
                            System.out.println(colorize("You have open windows, either close them or write \"quit force\"", Attribute.BOLD(), Attribute.YELLOW_TEXT()));
                    }
                    else
                        return;
                }
                else if ((printArgs)!=null)
                {
                    myNation.printReport(System.out,printArgs[0].equalsIgnoreCase("true"),printArgs[1].equalsIgnoreCase("true"),printArgs[2].equalsIgnoreCase("true"),printArgs[3].equalsIgnoreCase("true"),printArgs[4].equalsIgnoreCase("true"),printArgs[5].equalsIgnoreCase("true"));
                }
                else if (decisionArgs!=null)
                {
                    myNation.applyDecision(Integer.parseInt(decisionArgs[0]),System.out);
                }
                else if ((stepArgs)!=null)
                {
                    if (decisions>0)
                        System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("You have available decisions!", Attribute.BOLD(), Attribute.RED_TEXT()));
                    else
                        myNation.update(Math.max(0,Integer.parseInt(stepArgs[0])),System.out);
                }
                else
                    System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Write \"help\" to see all valid commands and arguments!", Attribute.BOLD(), Attribute.RED_TEXT()));
            }
            catch (Exception e) {
                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("There was an error parsing the command: "+e.getMessage(), Attribute.BOLD(), Attribute.RED_TEXT()));
            }
        } while (true);
    }

    /// A little helper function for detecting open graphs
    public static boolean hasOpenWindows() {
        for (Frame frame : Frame.getFrames()) {
            if (frame.isVisible() && frame instanceof JFrame) {
                return true;
            }
        }
        return false;
    }

}
