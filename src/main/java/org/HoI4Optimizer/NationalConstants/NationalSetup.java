package org.HoI4Optimizer.NationalConstants;

import org.HoI4Optimizer.Nation.NationState;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;

/// The setup of the nation including everything from  the national focus tree and decisions, this can not be modified by the simulation
public class NationalSetup {
    /// All equipment in the simulation
    private final Map<String,Equipment> equipmentList;
    ///How many months do we have data for?
    private int months;

    private final NationState nationStart;

    public Map<String,Equipment> getEquipmentList()
    {
        return equipmentList;
    }

    /// Copy the starting setup of the nation
    public NationState buildNation()
    {
        return nationStart.clone();
    }
    public NationalSetup(String countryname) throws IOException {
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
    }
}
