package org.HoI4Optimizer.MyPlotter;

import java.awt.*;
import java.util.Map;

/// A container for some type of plot the logger is required to produce
/// @param title Title of the plot
/// @param yaxis_title Title on the y-axis
/// @param stacked_areas Display as a stacked area plot? if not, we will display it as lines
/// @param lines_color_names A map where the key is the TableEntry name, and the value is the Color to plot, and the String is the name to put in the legend
/*class private*/ record PlotTemplate(
        String title,
        String yaxis_title,
        boolean stacked_areas,
        Map<String, Color> lines_color_names){}
