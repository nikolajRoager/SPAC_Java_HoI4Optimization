package org.HoI4Optimizer.Nation.Event;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static com.diogonunes.jcolor.Ansi.colorize;

/// An package of events which happens on a pre-determined day, has a certain name, and has any number of effects on either national propertyEvents, stateEvents, or Equipment
///@param name A name or description of what just happened
///@param propertyEvents Effects on the properties of the nation
///@param stateEvents Effects on a single state
public record Events (
    String name,
    List<PropertyEvent> propertyEvents,
    List<StateEvent> stateEvents)
{
    /// Only to be used by json deserializer
    public static TreeMap<Integer,List<Events>> loadEvents(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<>() {});
    }
}
