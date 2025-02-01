package org.HoI4Optimizer.NationalConstants;

import org.HoI4Optimizer.Nation.Event.Events;
import org.HoI4Optimizer.Nation.NationState;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/// The setup of the nation including everything from  the national focus tree and decisions, this can not be modified by the simulation
public class NationalSetup {
    /// All equipment in the simulation
    private final Map<String,Equipment> equipmentList;

    private final NationState nationStart;

    //Events which happens to the nation on particular days
    private Map<Integer, List<Events>> events;

    /// get all events happening this day
    public List<Events> getEvent(int day)
    {
        var list = events.get(day);
        if (list == null)
            return new ArrayList<>();
        else
            return list;
    }

    public Map<String,Equipment> getEquipmentList()
    {
        return equipmentList;
    }

    /// Copy the starting setup of the nation
    public NationState buildNation()
    {
        return nationStart.clone();
    }
    public NationalSetup(String countryname,String eventsname) throws IOException {
        try {
            equipmentList = Equipment.loadEquipment(Paths.get(countryname,"Equipment.json").toString());
        } catch (IOException e) {
            throw new IOException("Error loading "+countryname+"/Equipment.json:\n"+e.getMessage());
        }
        try {
            nationStart =new NationState(countryname,this);
        } catch (IOException e) {
            throw new IOException("Error loading "+countryname+" files\n"+e.getMessage());
        }
        try {
            events= Events.loadEvents(Paths.get(countryname,eventsname+".json").toString()) ;
        } catch (IOException e) {
            throw new IOException("Error loading "+countryname+" files\n"+e.getMessage());
        }
    }
}
