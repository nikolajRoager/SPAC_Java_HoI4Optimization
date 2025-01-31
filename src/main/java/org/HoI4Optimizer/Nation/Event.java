package org.HoI4Optimizer.Nation;

import com.diogonunes.jcolor.Attribute;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.HoI4Optimizer.Calender;
import org.HoI4Optimizer.NationalConstants.NationalSetup;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.diogonunes.jcolor.Ansi.colorize;

/// An event is something which happens on a pre-determined day, has a certain name, and has any number of effects on either national properties, states, or Equipment
public class Event {
    /// A name describing what just happened
    String name;

    ///Effects on the properties of the nation
    private List<PropertyEvent> properties;
    ///Effects on a single state
    private List<StateEvent> states;


    public void setName(String name) {
        this.name = name;
    }
    public void setProperties(List<PropertyEvent> properties) {
        this.properties = properties;
    }
    public void setStates(List<StateEvent> states) {
        this.states = states;
    }

    /// Create a single property event, usually created from the commandline
    public Event(String name, PropertyEvent.Target target, boolean bvalue)
    {
        this.name=name;
        properties =new ArrayList<PropertyEvent>();
        states =new ArrayList<StateEvent>();
        properties.add(new PropertyEvent(target,bvalue));
    }

    /// Only to be used by json deserializer
    public Event()
    {
        this.name="no event";
        properties =new ArrayList<PropertyEvent>();
        states =new ArrayList<StateEvent>();
    }

    /// Create a single property event, usually created from the commandline
    public Event(String name, PropertyEvent.Target target, double dvalue, boolean add)
    {
        this.name=name;
        properties =new ArrayList<PropertyEvent>();
        states =new ArrayList<StateEvent>();
        properties.add(new PropertyEvent(target,dvalue,add));
    }

    /// Create a single property event, usually created from the commandline
    public Event(String name, PropertyEvent.Target target, int ivalue, boolean add)
    {
        this.name=name;
        properties =new ArrayList<PropertyEvent>();
        states =new ArrayList<StateEvent>();
        properties.add(new PropertyEvent(target,ivalue,add));
    }

    /// Create a single property event, usually created from the commandline
    public Event(String name, PropertyEvent.Target target, NationalProperties.ideology idvalue)
    {
        this.name=name;
        properties =new ArrayList<PropertyEvent>();
        states =new ArrayList<StateEvent>();
        properties.add(new PropertyEvent(target,idvalue));
    }
    /// Apply all effects to national properties of some nation
    /// @param NationalStates States in this nation, included here so we can apply this to different copies of the same nation
    public void apply(NationalProperties properties, List<State> NationalStates, int day, PrintStream out)
    {
        if (out!=null)
        {
            out.println(colorize("==An event has happened on "+ Calender.getDate(day)+"==", Attribute.BOLD(),Attribute.SLOW_BLINK()));
            out.println(colorize("    "+name,Attribute.ITALIC()));
        }

        for (var propertyEvent : this.properties)
        {
            propertyEvent.apply(properties, out);
        }
        for (var stateEvent : this.states)
        {
            stateEvent.apply(NationalStates, out);
        }
    }

    public static Map<Integer,List<Event>> loadEvents(String filePath) throws IOException {
        //For randomly generating names
        Random rand = new Random();

        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(new File(filePath), new TypeReference<>() {});
    }
}
