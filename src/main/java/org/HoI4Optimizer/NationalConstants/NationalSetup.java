package org.HoI4Optimizer.NationalConstants;

import org.HoI4Optimizer.Nation.Event.Events;
import org.HoI4Optimizer.Nation.NationState;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/// The setup of the nation including everything from  the national focus tree and decisions, this can not be modified by the simulation
public class NationalSetup {
    /// All equipment in the simulation
    private final Map<String,Equipment> equipmentList;

    private final NationState nationStart;

    //The day the simulation end, will be the latest event (A dedicated "war was beginning" event is assumed to exist)
    private final int lastDay;

    //Events which happens to the nation on particular days, sorted with a treeMap
    private final TreeMap<Integer, List<Events>> events;

    public int getLastDay() {return lastDay;}

    /// get all events happening this day
    public List<Events> getEvent(int day)
    {
        var list = events.get(day);
        if (list == null)
            return new ArrayList<>();
        else
            return list;
    }

    /// Get all equipment
    public Map<String,Equipment> getEquipment()
    {
        return equipmentList;
    }

    /// Get equipment which can be produced by this day, and which are not outdated
    public Map<String,Equipment> getEquipment(int day){
        return equipmentList.entrySet().stream().filter(
                entry ->
                {
                    var eq =entry.getValue();
                    //Is this thing researched before today?
                    return eq.getResearchTime()<=day &&
                            //And is it the final version?
                            (eq.getNextGen()==null ||
                                    //Or is the next-gen upgrade yet to enter service?
                                    eq.getNextGen().getResearchTime()>day);
                }
                //Turn all the things which fit this back into a map and return this
        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)
                );
    }
    /// Copy the starting setup of the nation
    public NationState buildNation()
    {
        return nationStart.clone();
    }
    public NationalSetup(String countryname, String eventsname) throws IOException {
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
            lastDay=events.lastKey();
        } catch (IOException e) {
            throw new IOException("Error loading "+countryname+" files\n"+e.getMessage());
        }
    }
}
