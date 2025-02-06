package org.HoI4Optimizer.MyPlotter;

import java.awt.*;
import java.util.List;

/// The data in a single line in the plot
/// @param name name shown in legend
/// @param data data to plot, as a double each day
/// @param color color used for plot and legend
/// @param startDay the day the data starts, every data point after that is a day
public record LinePlotData(
        String name,
        List<Double> data,
        int startDay,
        Color color
) {

}
