package org.HoI4Optimizer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.*;
import java.util.List;

//A frame containing our plots
import com.diogonunes.jcolor.Attribute;
import net.bytebuddy.asm.MemberSubstitution;
import org.HoI4Optimizer.Nation.Event;
import org.HoI4Optimizer.NationalConstants.NationalSetup;
import org.HoI4Optimizer.MyPlotter.Plot;

import static com.diogonunes.jcolor.Ansi.colorize;

public class Main
{
    //Main function, controls what commands get done from commandline
    public static void main(String[] args)
    {
        NationalSetup Setup;
        //Load setup
        try {
            Setup = new NationalSetup("Poland");
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return;
        }

        var MyNation =Setup.buildNation();
        //A little class to store commands, arguments and their descriptions,

        //Create a list of commands to interact with the program
        Map<String,CommandlineCommand> commands=new HashMap<>();
        commands.put("print",new CommandlineCommand("print","Print the current state of the nation, with all current stats",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("showResources", "Show detailed breakdown of resources in nation", CommandlineCommand.Argument.type.Flag, true, 0.0),
                        new CommandlineCommand.Argument("showProduction", "Show detailed breakdown of all Military factories and production lines", CommandlineCommand.Argument.type.Flag, true, 0.0),
                        new CommandlineCommand.Argument("showConstruction", "Show detailed breakdown of all National construction projects", CommandlineCommand.Argument.type.Flag, true, 0.0),
                        new CommandlineCommand.Argument("showFactories", "Show detailed breakdown of all Factories: civilian, Military, and Chemical", CommandlineCommand.Argument.type.Flag, true, 0.0),
                        new CommandlineCommand.Argument("showStates", "Show detailed breakdown of all States in the nation", CommandlineCommand.Argument.type.Flag, true, 0.0),
                })));
        commands.put("quit",new CommandlineCommand("quit","stop program",
                List.of(new CommandlineCommand.Argument[]{
                })));
        commands.put("event",new CommandlineCommand("event","Create an event for the nation now, or some day in the future (launches event JSon editor)",
                List.of(new CommandlineCommand.Argument[]{
                        new CommandlineCommand.Argument("day", "When will this event be executed? skip to execute now.", CommandlineCommand.Argument.type.Integer, true, 0.0),
                })));


        do {

            //I use a blue box (or ### without colours) before the text to make it clear where the commands are
            System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize(" We have loaded the setup at the start of the game, you now have the following choices", Attribute.BOLD(), Attribute.BLUE_TEXT()));

            //Print commands, the index is used to color-code the different commands
            int i = 0;
            for (var C : commands.values())
                C.print((i++) % CommandlineCommand.command_colors.length);
            //NationStart.printReport(System.out,true,true,true,true,true);
            System.out.print(colorize(">>>>", Attribute.BOLD()));
            String input = System.console().readLine();

            List<String> coms = List.of(input.split(" "));
            coms.forEach(s -> s = s.trim());

            //Check which arguments we match
            if (commands.get("quit").match(coms)!=null)
            {
                return;
            }
            var PrintArgs=commands.get("print").match(coms);
            if ((PrintArgs)!=null)
            {
                MyNation.printReport(System.out,PrintArgs[0]>0.5,PrintArgs[1]>0.5,PrintArgs[2]>0.5,PrintArgs[3]>0.5,PrintArgs[4]>0.5);

            }
            var eventArgs=commands.get("event").match(coms);
            if (eventArgs!=null)
            {
                boolean instant =(eventArgs[0]==0);
                //A little quick JSon editor
                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize(" Started event editor for new event to be applied "+(instant?"now":"at "+Calender.getDate((int)eventArgs[0])), Attribute.BOLD(), Attribute.GREEN_TEXT()));

                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Enter data, finish with enter", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Name: ", Attribute.BOLD(), Attribute.RED_TEXT(),Attribute.RAPID_BLINK()));
                String nameInput = System.console().readLine();
                System.out.println(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       The following targets exist: ", Attribute.ITALIC(), Attribute.GREEN_TEXT()));

                var targets = Event.Target.values();
                for (int j = 0; j < targets.length; ++j)
                {
                    System.out.print(colorize("\t"+j+":",Attribute.ITALIC(),Attribute.BRIGHT_WHITE_TEXT())+colorize(targets[j]+(((j%5==0) || (j+1==targets.length))?"\n":""),Attribute.WHITE_TEXT(),Attribute.ITALIC()));
                }
                System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("       Enter one of the names, or their number", Attribute.ITALIC(), Attribute.GREEN_TEXT()));
                System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     Target: ", Attribute.BOLD(), Attribute.RED_TEXT(),Attribute.RAPID_BLINK()));
                //System.out.print(colorize("###", Attribute.BOLD(), Attribute.BLUE_TEXT(), Attribute.BLUE_BACK()) + colorize("     enter name, finish with enter: ", Attribute.BOLD(), Attribute.RED_TEXT(),Attribute.RAPID_BLINK()));
                //String nameInput = System.console().readLine();
                return;

            }
        } while (true);

        /*
        //First show the unchanging national setup for our nation
        //create an instance of JFrame class
        JFrame constructionFrame = new JFrame();
        JFrame productionFrame = new JFrame();
        JFrame resourceFrame = new JFrame();

        var RPlotter=new Plot(Calender.getDay(1938,7,1),0,60,new String[]{"0","10","20","30","40","50","60"});
        RPlotter.addPlot(Setup.get_rubber(), Color.black,"Rubber");
        RPlotter.addPlot(Setup.get_steel(), Color.blue,"Steel");
        RPlotter.addPlot(Setup.get_tungsten(), Color.darkGray,"Tungsten");
        RPlotter.addPlot(Setup.get_aluminium(), Color.lightGray,"Aluminium");
        RPlotter.addPlot(Setup.get_chromium(), Color.lightGray,"Chromium");

        var PPlotter=new Plot(Calender.getDay(1938,7,1),-0.5,1.0,new String[]{"-50%","-40%","-30%","-20%","-10%","  0%"," 10%"," 20%"," 30%"," 40%"," 50%"," 60%"," 70%"," 80%"," 90%","100%"});
        PPlotter.addPlot(Setup.get_stability(), Color.PINK,"Stability");
        //Related to production
        PPlotter.addPlot(Setup.get_factory_output(), Color.BLUE,"Factory output bonus");
        PPlotter.addPlot(Setup.get_efficiency_cap(), Color.magenta,"Factory efficiency cap");

        var CPlotter=new Plot(Calender.getDay(1938,7,1),-0.5,1.0,new String[]{"-50%","-40%","-30%","-20%","-10%","  0%"," 10%"," 20%"," 30%"," 40%"," 50%"," 60%"," 70%"," 80%"," 90%","100%"});
        //Related to construction
        CPlotter.addPlot(Setup.get_mil_construction_speed(), Color.GREEN,"Military Factory Construction Speed bonus");
        CPlotter.addPlot(Setup.get_civ_construction_speed(), Color.ORANGE,"Civilian Factory Construction Speed bonus");
        CPlotter.addPlot(Setup.get_construction_speed(), Color.YELLOW,"Other Construction Speed bonus");
        CPlotter.addPlot(Setup.get_true_consumer_goods_ratio(), Color.cyan,"Consumer goods ratio");

        //MyPlotter.Plot extends JPanel, and creates a plot of some data, until the given date
        constructionFrame.add(CPlotter);
        productionFrame.add(PPlotter);
        resourceFrame.add(RPlotter);

        //set size, layout and location for frames.
        productionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        productionFrame.setSize(1800, 600);
        productionFrame.setLocation(100, 200);
        productionFrame.setTitle("Polish Production Bonuses, January 1936 to September 1938");
        productionFrame.setVisible(true);
        constructionFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        constructionFrame.setSize(1800, 600);
        constructionFrame.setLocation(100, 200);
        constructionFrame.setTitle("Polish Construction Bonuses, January 1936 to September 1938");
        constructionFrame.setVisible(true);

        resourceFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        resourceFrame.setSize(1800, 600);
        resourceFrame.setLocation(100, 200);
        resourceFrame.setTitle("Polish Resource availability, January 1936 to September 1938");
        resourceFrame.setVisible(true);*/
    }
}