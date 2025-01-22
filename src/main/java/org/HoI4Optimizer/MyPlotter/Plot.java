package org.HoI4Optimizer.MyPlotter;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.util.ArrayList;


/// A background for my plot, with axes, grid and ticks, it is assumed the x axis is in days since 1st of January 1936
public class Plot extends JPanel
{
    //Default values are included as example
    int endDay=1002;//30th september 1938, the Munich Agreement

    //Margin, in pixels
    double margin=30;

    //Max and minimum values of data
    double min=0.0,max=1.0;
    String[] valueNames={"  0%"," 10%"," 20%"," 30%"," 40%"," 50%"," 60%"," 70%"," 80%"," 90%","100%"};

    private class FloatPlot
    {
        public double [] data;//1 datapoint for every month we have data for, assuming the first month is january 1936
        public Color colour;
        public String name;

        FloatPlot(double [] data, Color colour, String name)
        {
            this.data=data;
            this.colour=colour;
            this.name=name;
        }
    };

    ArrayList<FloatPlot> FloatPlots;

    public Plot(int endDay, double min, double max, String[] valueNames)
    {

        FloatPlots=new ArrayList<FloatPlot>();
        this.endDay=endDay;
        this.margin=40;
        this.min=min;
        this.max=max;
        this.valueNames=valueNames;
    }



    public Plot(int endDay, double min, double max, String[] valueNames, double margin)
    {
        this.endDay=endDay;
        this.margin=margin;
        this.min=min;
        this.max=max;
        this.valueNames=valueNames;
    }

    /// Add this linearly interpolated floating point data to the plot with this colour
    public void addPlot(double[] data,Color c, String name)
    {
        FloatPlots.add(new FloatPlot(data,c,name));
    }

    /// Add this integer interpolated integer data to the plot with this colour
    public void addPlot(int[] data,Color c, String name)
    {

    }

    /// Draw the current plot
    //This is largely based on the tutorial huttps://www.javatpoint.com/java-plot
    protected void paintComponent(Graphics g)
    {
        //Basic setup
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        //Enable antialiasing
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //First draw background colour
        g2d.setBackground(Color.WHITE);

        //Width and height of the graph in pixels, as a floating point number because we are going to be doing floating point math
        double width  = getWidth();
        double height = getHeight();

        double pxPerDay = (width-margin*2)/endDay;
        double pxPerData = (height-margin*2)/(max-min);

        //Draw grid with lines the 1st of every month
        String[] ShortMonthNames = {"Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"};
        int[] MonthDays = {31,28,31,30,31,30,31,31,30,31,30,31};
        int year = 36;

        Font myFont = new Font("SansSerif", Font.PLAIN, 16);
        g2d.setFont(myFont);


        g2d.setStroke(new BasicStroke(3));
        //First draw the plots, I want  the grid and text on top
        //Loop through months and plot all lines
        for (int m = 0,day=0,totalDay=0;totalDay<endDay;++day,++totalDay) {
            if (day == 31 && m == 12)
                ++year;

            //True if this is the first of a month or year
            boolean isFirst = totalDay==0;
            boolean isNewYear=totalDay==0;

            if ((year % 4 == 0) && m == 1) {
                if (day == 29) {
                    day=0;
                    m = m + 1;
                    isFirst = true;
                }
            } else if (day == MonthDays[m]) {
                day=0;
                isFirst = true;
                m = (m + 1);
                if (m == 12) {
                    ++year;
                    m = 0;
                    isNewYear = true;
                }
            }

            if (isFirst) {
                for (FloatPlot p : FloatPlots) {
                    g2d.setColor(p.colour);
                    //Location of start and end of the month
                    double x_start = pxPerDay * totalDay+ margin;
                    double x_end = pxPerDay * (totalDay+30)+ margin;


                    //Start and end data
                    double y_start = m+year*12-12*36<p.data.length? p.data[m+year*12-12*36] : p.data[p.data.length-1];
                    double y_end = m+year*12+1-12*36<p.data.length? p.data[m+year*12+1-12*36] : p.data[p.data.length-1];

                    y_start = height-margin-(y_start-min)*pxPerData;//-margin-(y_start-min)*pxPerData;
                    y_end = height-margin-(y_end-min)*pxPerData;
                    g2d.draw(new Line2D.Double(x_start, y_start, x_end, y_end));
                }
            }
        }

        //Advance through the months, update the month and year we are in and draw legend and line
        for (int m = 0,day=0,totalDay=0;totalDay<endDay;++day,++totalDay)
        {
            if (day == 31 && m == 12)
                ++year;

            //True if this is the first of a month or year
            boolean isFirst = totalDay==0;
            boolean isNewYear=totalDay==0;

            if ((year % 4 == 0) && m == 1) {
                if (day == 29) {
                    day=0;
                    m = m + 1;
                    isFirst = true;
                }
            } else if (day == MonthDays[m]) {
                day=0;
                isFirst = true;
                m = (m + 1);
                if (m == 12) {
                    ++year;
                    m = 0;
                    isNewYear = true;
                }
            }

            if (isFirst) {

                double x = pxPerDay * totalDay+ margin;
                if (isNewYear)
                {
                    if (year !=0)
                    {
                        g2d.setStroke(new BasicStroke(2));
                        g2d.setColor(Color.gray);
                        g2d.draw(new Line2D.Double(x, height - margin, x, margin/2));

                    }

                    //Year above
                    g2d.setColor(Color.black);
                    g2d.drawString("19"+year,(int)x,(int)(margin/2));
                }
                else
                {
                    g2d.setStroke(new BasicStroke(1));
                    g2d.setColor(Color.lightGray);
                    g2d.draw(new Line2D.Double(x, height - margin, x, margin));
                }

                //Month below
                g2d.setColor(Color.black);
                g2d.setStroke(new BasicStroke(2));
                g2d.draw(new Line2D.Double(x, height - margin+4, x, height-margin));
                g2d.drawString(ShortMonthNames[m],(int)x,(int)(height-margin)+16);
            }
        }

        //Now add the grid for the values
        for (int i = 0;i < valueNames.length;i++)
        {
            double y = i*(height-margin*2)/ (valueNames.length-1)+margin;
            g2d.setStroke(new BasicStroke(1));
            g2d.setColor(Color.gray);
            g2d.draw(new Line2D.Double(margin, y, width-margin, y));

            g2d.setColor(Color.black);
            g2d.drawString(valueNames[valueNames.length-i-1], (int)0,(int)y);
        }

        g2d.setColor(Color.darkGray);
        g2d.setStroke(new BasicStroke(2));
        //Draw axis and arrowheads
        g2d.draw(new Line2D.Double(margin, margin/2, margin, height-margin));
        g2d.draw(new Line2D.Double(margin+margin/3, margin, margin, margin/2));
        g2d.draw(new Line2D.Double(margin-margin/3, margin, margin, margin/2));
        g2d.draw(new Line2D.Double(margin, height-margin, width-margin/2, height-margin));
        g2d.draw(new Line2D.Double(width-margin/2, height-margin, width-margin, height-margin+margin/3));
        g2d.draw(new Line2D.Double(width-margin/2, height-margin, width-margin, height-margin-margin/3));
    }
}
