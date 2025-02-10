package org.HoI4Optimizer.MyPlotter;

import net.bytebuddy.utility.nullability.NeverNull;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.DoubleSupplier;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/// A class which can log, save, and plot any data
public class DataLogger implements Cloneable{
    public enum logDataType{
        /// The upper and lower limit is 0% and 100%
        PositivePercent,
        /// Lower limit is 0%
        PositiveUnboundedPercent,
        /// The upper and lower limit is -100% and 100%,
        Percent,
        /// No bounds
        UnboundedPercent,
        /// The lower limit is 0
        PositiveReal,
        /// No bounds
        Real,
        /// Lower limit is 0, number is an integer
        PositiveInteger,
        ///  No bounds, must be an integer
        Integer
    }

    @Override
    public DataLogger clone() {
        try {
            DataLogger clone = (DataLogger) super.clone();
            clone.table=new HashMap<>();
            for (var entry : table.entrySet())
            {
                clone.table.put(entry.getKey(),entry.getValue().clone());
            }
            clone.plots=new ArrayList<>();
            clone.plots.addAll(plots);

             return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /// An entry with data each day
    private static class TableEntry implements Cloneable{

        /// Where we get the data from
        @NeverNull private DoubleSupplier valueFunction;

        ///List of data
        @NeverNull private List<Double> data;

        @NeverNull private final logDataType myType;

        ///Starting day
        private int day0;

        /// Overwrite function, but keep data
        public void setFunction(@NeverNull DoubleSupplier valueFunction)
        {
            this.valueFunction=valueFunction;
        }
        public int getDay0() {return day0;}

        /// Name of this entry
        @NeverNull String name;

        /// Initialize a table entry starting some day, monitoring some function
        /// @param day0 the day we start the log
        /// @param func the function we monitor
        /// @param name name of the entry
        private TableEntry(int day0,DoubleSupplier func, String name,logDataType type) {
            valueFunction = func;
            data=new ArrayList<>();
            this.day0=day0;
            this.name=name;
            this.myType=type;
        }

        /// Request data from the function, we assume this is the next day
        void log()
        {
            Double d =valueFunction.getAsDouble();
            data.add(d);
        }

        List<Double> getData()
        {
            return data;
        }

        @Override
        public TableEntry clone() {
            try {
                TableEntry clone = (TableEntry) super.clone();
                clone.name=name;
                //This should be changed, if the object owning this has been cloned, but we can't know if that is the case from inside this class, so that is not our job
                clone.valueFunction=valueFunction;
                clone.day0=day0;
                //Shallow copy *should* be fine, since we are not modifying existing data
                clone.data=new ArrayList<>();
                clone.data.addAll(data);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }


    /// Table with all the data we log
    @NeverNull
    Map<String,TableEntry> table;


    /// Counters used to count some type of data up
    @NeverNull
    Map<String,Double> counters;
    /// Functions which make counters tick up
    @NeverNull
    List<Supplier<Map<String,Double>>> counterFunctions;

    static private final List<Color> colors =List.of(Color.RED,Color.GREEN,Color.BLUE,Color.YELLOW,Color.CYAN,Color.MAGENTA,Color.PINK,Color.GRAY);

    /// List of plots we need to make
    @NeverNull
    List<PlotTemplate> plots;

    int day;

    /// Initialize new data-logger
    public DataLogger()
    {
        day=0;
        table = new HashMap<>();
        plots = new ArrayList<>();
        counters = new HashMap<>();
        counterFunctions = new ArrayList<>();
    }
    /// Either add new log of this function, which returns a double each day
    /// If an existing name is used, we replace the function but keep the existing data (useful in clone constructors, if we want to save and load data)
    /// @param function the function we monitor
    /// @param name name of the variable
    public void setLog(String name,DoubleSupplier function,logDataType type)
    {
        if (table.containsKey(name))
            table.get(name).setFunction(function);
        else
            table.put(name, new TableEntry(day, function, name, type));
    }

    /// Either add or replace multiple logs at the same time, if they are calculated by the same function
    /// If an existing name is used, we replace the function but keep the existing data (useful in clone constructors, if we want to save and load data)
    /// @param function the function we monitor, it returns doubles for every name (for example number of factories on different types of equipment)
    /// @param names names of things we get from the function
    public void setLog(Set<String> names, Supplier<Map<String,Double>> function, logDataType type)
    {

        //Add the function to the list of counters, which we will call each log
        counterFunctions.add(function);
        //Then create faux functions for every name, which just checks the counter
        for (String name : names) {
            if (table.containsKey(name))
                table.get(name).setFunction(()-> counters.getOrDefault(name,0.0));
            else
                table.put(name, new TableEntry(day,()-> counters.getOrDefault(name,0.0), name, type));
        }
    }


    /// Add a plot without used defined colors
    /// @param title title of plot and window, saved as title.pdf
    /// @param lines name of the datasets to plot (must have been added with one of the setLog functions)
    /// @param stacked_areas use stacked area plots
    /// @param yaxis_title title to put on the y-axis
    public void addPlot(
            String title,
            List<String> lines,
            String yaxis_title,
            boolean stacked_areas
    ){
        //Check that each line in the plot actually exists before adding i
        for (String line : lines)
            if (!table.containsKey(line)) {
                StringBuilder nomen = new StringBuilder();
                for (var k : table.keySet())
                    nomen.append(k+" ");
                throw new IllegalArgumentException("No such entry " + line + " valid: " + nomen.toString());
            }
        //This is apparently how you convert a list of a map, I had to look that up
        Map<String,Color> lineColors = IntStream.range(0,lines.size()).boxed().collect(Collectors.toMap(lines::get, i-> colors.get(i%colors.size())));

        plots.add(new PlotTemplate(title, yaxis_title, stacked_areas, lineColors));
    }


    /// Add a plot with user defined colors
    /// @param title title of plot and window, saved as title.pdf
    /// @param lines name of the datasets to plot (must have been added with one of the setLog functions), and user defined color
    /// @param stacked_areas use stacked area plots
    /// @param yaxis_title title to put on the y-axis
    public void addPlot(
            String title,
            Map<String,Color> lines,
            String yaxis_title,
            boolean stacked_areas
    ){
        //Check that each line in the plot actually exists before adding i
        for (Map.Entry<String,Color> entry : lines.entrySet())
            if (!table.containsKey(entry.getKey())) {
                StringBuilder nomen= new StringBuilder();
                for (var k : table.keySet())
                    nomen.append(k);
                throw new IllegalArgumentException("No such entry " + entry.getKey()+" valid: "+nomen.toString());
            }
        plots.add(new PlotTemplate(title, yaxis_title, stacked_areas, lines));
    }
    /// Log for this day, I assume this is called once per day
    public void log()
    {
        //Call all counter functions
        for (var cf : counterFunctions)
        {
            counters.putAll(cf.get());
        }

        for (var entry : table.values())
        {
            entry.log();
        }
        ++day;
    }
    //Open windows with all plots
    public void show(boolean save)
    {
        for (var plot: plots)
        {
            //Create the lines with the data
            List<LinePlotData> lines = new ArrayList<>();
            logDataType type=null;
            for (var line : plot.lines_color_names().entrySet())
            {
                //Add the lines by reading the data from the relevant entries
                var entry =table.get(line.getKey());
                if (type==null)
                    type=entry.myType;
                else if (type== logDataType.PositivePercent || type== logDataType.PositiveUnboundedPercent || type== logDataType.Percent || type== logDataType.UnboundedPercent)
                {
                    if (entry.myType==logDataType.Real || entry.myType==logDataType.PositiveReal || entry.myType==logDataType.Integer)
                        throw  new RuntimeException("Can not mix real and percent data in same plot");
                    //Escalate type if need be
                    else if (type==logDataType.PositivePercent && entry.myType==logDataType.PositiveUnboundedPercent)
                        type=logDataType.PositiveUnboundedPercent;
                    else if ( (type==logDataType.PositivePercent || type==logDataType.PositiveUnboundedPercent) && entry.myType==logDataType.Percent )
                        type=logDataType.Percent;
                    else if ( (type==logDataType.PositivePercent || type==logDataType.PositiveUnboundedPercent || type==logDataType.Percent) && entry.myType==logDataType.UnboundedPercent)
                        type=logDataType.UnboundedPercent;
                }
                else//Real type
                {
                    if (entry.myType== logDataType.PositivePercent || entry.myType== logDataType.PositiveUnboundedPercent || entry.myType== logDataType.Percent || entry.myType== logDataType.UnboundedPercent)
                        throw  new RuntimeException("Can not mix real and percent data in same plot");
                    //Escalate if need be
                    else if (type==logDataType.PositiveReal && entry.myType==logDataType.Real)
                        type=logDataType.Real;
                }
                lines.add(new LinePlotData(
                        line.getKey(),
                        entry.getData(),
                        entry.getDay0(),
                        line.getValue()));
            }

            var frame= new LinePlotFrame(plot.title(), plot.yaxis_title(),lines, type==null?logDataType.Real:type,plot.stacked_areas(),save);
            frame.setVisible(true);
        }
    }
}
