package org.HoI4Optimizer.MyPlotter;

import net.bytebuddy.utility.nullability.NeverNull;
import org.HoI4Optimizer.Nation.NationState;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.DoubleSupplier;
import java.util.function.Function;

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

            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }

    /// An entry with data each day
    private static class TableEntry implements Cloneable{

        /// Where we get the data from
        @NeverNull DoubleSupplier valueFunction;

        ///List of data
        @NeverNull List<Double> data;

        @NeverNull private logDataType myType;

        ///Starting day
        int day0;

        /// Overwrite function, but keep data
        public void setFunction(@NeverNull DoubleSupplier valueFunction)
        {
            this.valueFunction=valueFunction;
        }

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
            data.add(valueFunction.getAsDouble());
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
                clone.data=List.copyOf(data);
                return clone;
            } catch (CloneNotSupportedException e) {
                throw new AssertionError();
            }
        }
    }

    /// Table with all the data we log
    @NeverNull
    Map<String,TableEntry> table;

    int day;

    /// Initialize new data-logger
    public DataLogger()
    {
        day=0;
        table=new HashMap<>();
    }

    /// Either add new log of this function, which returns a double each day
    /// If an existing name is used, we replace the function but keep the existing data (useful in clone constructors, if we want to save and load data)
    /// @param function the function we monitor
    /// @param name name of the variable
    public void setLog(String name,DoubleSupplier function,logDataType type)
    {
        if (table.containsKey(name))
        {
            table.get(name).setFunction(function);

        }
        else
            table.put(name,new TableEntry(day,function,name,type));
    }

    /// Log for this day, I assume this is called once per day
    public void log()
    {
        for (var entry : table.values())
        {
            entry.log();
        }
        ++day;
    }


}
