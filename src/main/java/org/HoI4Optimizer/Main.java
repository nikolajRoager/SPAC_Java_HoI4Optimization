package org.HoI4Optimizer;

import java.io.IOException;
import java.util.*;
import java.util.List;

//A frame containing our plots
import com.diogonunes.jcolor.Attribute;
import org.HoI4Optimizer.Building.SharedBuilding.CivilianFactory;
import org.HoI4Optimizer.Nation.*;
import org.HoI4Optimizer.Nation.decision.decision;
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

        NationState MyNation =Setup.buildNation();
        MyNation.apply(new decision(MyNation.getStates().getFirst(),new CivilianFactory()));
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

            System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()));
            System.out.println(colorize(" ",Attribute.MAGENTA_BACK())+" "+colorize("("+Calender.getDate(MyNation.getDay())+" day "+MyNation.getDay()+")", Attribute.ITALIC(),Attribute.WHITE_TEXT()));
            System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK())+colorize("$",Attribute.BLACK_TEXT(),Attribute.MAGENTA_BACK())+" ");
            String input = System.console().readLine();

            List<String> command_inputs = new ArrayList<>(List.of(input.split(" ")));
            command_inputs.replaceAll(String::trim);

            //Save some longer commands in lists, so we can read the arguments individually
            String[] PrintArgs=null;
            String[] stepArgs=null;
            try {
                PrintArgs = commands.get("print").match(command_inputs);
                stepArgs = commands.get("step").match(command_inputs);
            }
            catch (Exception e) {
                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("There was an error parsing the command: "+e.getMessage(), Attribute.BOLD(), Attribute.RED_TEXT()));
            }
            //Check which arguments we match, these will not throw errors as they don't have arguments which can be wrong
            if (commands.get("help").match(command_inputs)!=null)
            {
                printHelp = true;
            }
            else if (commands.get("quit").match(command_inputs)!=null)
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
            else
                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("Write \"help\" to see all valid commands and arguments!", Attribute.BOLD(), Attribute.RED_TEXT()));
        } while (true);
    }
}