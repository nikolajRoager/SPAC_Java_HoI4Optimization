package org.HoI4Optimizer;

import javax.swing.*;
import java.awt.*;

//A frame containing our plots
import org.HoI4Optimizer.NationalConstants.NationalSetup;
import org.HoI4Optimizer.MyPlotter.Plot;


public class Main
{
    //Main simulation
    public static void main(String[] args)
    {
        NationalSetup Setup = new NationalSetup("Poland.csv",Calender.getDay(1936,5,1)/*I first built 2 levels of infrastructure in Katowice, this is important for my strategy and can not be changed until it finish this day*/);

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
        resourceFrame.setVisible(true);
    }
}
