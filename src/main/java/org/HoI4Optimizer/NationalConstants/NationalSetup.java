package org.HoI4Optimizer.NationalConstants;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/// The setup of the nation including everything from  the national focus tree and decisions, this can not be modified by the simulation
public class NationalSetup {

    ///Each month, the national setup has these values
    ///These effects will be linearly interpolated, because there is no telling when exactly they happened
    private static class MonthlySetup
    {
        ///Stability at the start of each month
        public double stability;
        ///General construction speed bonus from technology and events
        public double construction_speed;
        ///Specifically construction speed for military factories, some events boost that specifically
        public double mil_construction_speed;
        ///Specifically construction speed for civilian factories, some events boost that specifically
        public double civ_construction_speed;
        ///Efficiency cap
        public double Efficiency_cap;
        ///Factory output bonus, Not counting effects from stability
        public double base_factory_output;
        ///Fraction of all factories (civilian and military)  required for "consumer goods" (the non-military part of the economy) This is subtracted from CIVILIAN factories
        public double consumer_goods_ratio;
        /// A multiplier, effecting consumer goods, will be further modified by current stability
        public double base_consumer_goods_multiplier;

        ///how many civs (hoi4 player slang for civilian factories) did I get from events in the test game?
        public int civs;
        ///How many civilian factories are earmarked for special projects (mainly intelligence and decryption) each month
        /// I wait until late 1937 to start decrypting Enigma
        /// This is subtracted from available civs before trade and construction,
        public int special_projects_civs;
        ///TEMP for debugging purpose, how many available civs dig I record?
        public int civs_available;
        ///TEMP for debugging purpose, how many mils (hoi4 player slang for military factories) did I record
        ///Should be replaced by "Event" mils
        public int mils;

        // Units of resources produced per day , resources other than oil (not modelled here) can NOT be stored, resources not spend by military production is returned to the civilian sector
        ///arbitrary game units of rubber available for military industry
        public int rubber;
        ///arbitrary game units of steel (for guns, tanks and all the panoply of war) available for military industry per day
        public int steel;
        ///arbitrary game units of tungsten (for artillery shells) available for military industry per day
        public int tungsten;
        ///arbitrary game units of aluminium (for aircraft and support equipment) available for military industry per day
        public int aluminium;
        ///arbitrary game units of chromium available for military industry per day
        ///"Chromium" in game represents many different rare minerals and alloys
        public int chromium;

        ///Names of FREE Building slots with level 5 infrastructure (100% extra construction bonus)
        public List<String> level5_infrastructure_slots;
        ///Names of FREE Building slots with level 4 infrastructure (80% extra construction bonus)
        public List<String> level4_infrastructure_slots;
        ///Names of FREE Building slots with level 3 infrastructure (60% extra construction bonus)
        ///Smallest infrastructure bonus I will not consider anything less developed
        public List<String> level3_infrastructure_slots;

        /// Constructor, the "base" effects are without stability modifying the stats
        public MonthlySetup(
            double stability,
            double construction_speed,
            double mil_construction_speed,
            double civ_construction_speed,
            double base_factory_output,
            double Efficiency_cap,
            double consumer_goods_ratio,
            double base_consumer_goods_multiplier,
            int civs,
            int special_projects_civs,
            int civs_available,
            int mils,
            int rubber,
            int steel,
            int tungsten,
            int aluminium,
            int chromium,
            List<String> level5_infrastructure_slots,
            List<String> level4_infrastructure_slots,
            List<String> level3_infrastructure_slots)
        {
            this.Efficiency_cap=Efficiency_cap;
            this.stability=stability;
            this.construction_speed=construction_speed;
            this.mil_construction_speed=mil_construction_speed;
            this.civ_construction_speed=civ_construction_speed;
            this.base_factory_output=base_factory_output;
            this.consumer_goods_ratio=consumer_goods_ratio;
            this.base_consumer_goods_multiplier =base_consumer_goods_multiplier;
            this.civs=civs;
            this.special_projects_civs=special_projects_civs;
            this.civs_available=civs_available;
            this.mils=mils;
            this.rubber=rubber;
            this.steel=steel;
            this.tungsten=tungsten;
            this.aluminium=aluminium;
            this.chromium=chromium;
        }
    };

    ///Get a list of stability each month
    public double[] get_stability()
    {
        double[] stability=new double[months];
        for (int i=0; i<months; i++)
            stability[i]= monthlySetups.get(i).stability;
        return stability;
    }
    ///Get a list of construction_speed each month
    public double[] get_construction_speed()
    {
        double[] construction_speed=new double[months];
        for (int i=0; i<months; i++)
            construction_speed[i]= monthlySetups.get(i).construction_speed;
        return construction_speed;
    }
    ///Get a list of civ_construction_speed each month
    public double[] get_civ_construction_speed()
    {
        double[] civ_construction_speed=new double[months];
        for (int i=0; i<months; i++)
            civ_construction_speed[i]= monthlySetups.get(i).civ_construction_speed;
        return civ_construction_speed;
    }
    ///Get a list of mil_construction_speed each month
    public double[] get_mil_construction_speed()
    {
        double[] mil_construction_speed=new double[months];
        for (int i=0; i<months; i++)
            mil_construction_speed[i]= monthlySetups.get(i).mil_construction_speed;
        return mil_construction_speed;
    }
    ///Get a list of efficiency cap each month
    public double[] get_efficiency_cap()
    {
        double[] efficiency_cap=new double[months];
        for (int i=0; i<months; i++)
            efficiency_cap[i]= monthlySetups.get(i).Efficiency_cap;
        return efficiency_cap;
    }
    ///Get a list of consumer_goods_ratio each month
    public double[] get_consumer_goods_ratio()
    {
        double[] consumer_goods_ratio=new double[months];
        for (int i=0; i<months; i++)
            consumer_goods_ratio[i]= monthlySetups.get(i).consumer_goods_ratio;
        return consumer_goods_ratio;
    }
    ///Get a list of base_factory_output each month
    public double[] get_factory_output()
    {
        double[] factory_output=new double[months];
        for (int i=0; i<months; i++)
            factory_output[i]= monthlySetups.get(i).base_factory_output+
                    (monthlySetups.get(i).stability>0.5?
                            2.0*(monthlySetups.get(i).stability-0.5)*0.2 ://Positive stability gives up to 20% bonus
                            -5.0*(0.5-monthlySetups.get(i).stability)*0.2//And negative gives a BRUTAL -50%
                    );
        return factory_output;
    }
    ///Get a list of consumer_goods_multiplier each month
    public double[] get_consumer_goods_multiplier()
    {
        double[] consumer_goods_multiplier=new double[months];
        for (int i=0; i<months; i++)
            consumer_goods_multiplier[i]= monthlySetups.get(i).base_consumer_goods_multiplier+
                    1*(1+monthlySetups.get(i).stability>0.5?
                            -0.2*(monthlySetups.get(i).stability-0.5)*0.2 ://Positive stability gives up to 20% less consumer goods
                            0//Negative stability has no effect
                            );
        return consumer_goods_multiplier;
    }
    ///Get a list of true consumer_goods_ratio each month (applying the factor)
    public double[] get_true_consumer_goods_ratio()
    {
        double[] out=new double[months];
        double[] consumer_goods_ratio=get_consumer_goods_ratio();
        double[] consumer_goods_multiplier=get_consumer_goods_multiplier();
        for (int i=0; i<months; i++)
           out[i]=consumer_goods_ratio[i]*consumer_goods_multiplier[i];
        return out;
    }
    ///Get a list of civs each month
    public double[] get_civs()
    {
        double[] civs=new double[months];
        for (int i=0; i<months; i++)
            civs[i]= monthlySetups.get(i).civs;
        return civs;
    }
    ///Get a list of special_projects_civs each month
    public double[] get_special_projects_civs()
    {
        double[] special_projects_civs=new double[months];
        for (int i=0; i<months; i++)
            special_projects_civs[i]= monthlySetups.get(i).special_projects_civs;
        return special_projects_civs;
    }
    ///Get a list of civs_available each month
    public double[] get_civs_available()
    {
        double[] civs_available=new double[months];
        for (int i=0; i<months; i++)
            civs_available[i]= monthlySetups.get(i).civs_available;
        return civs_available;
    }
    ///Get a list of mils each month
    public double[] get_mils()
    {
        double[] mils=new double[months];
        for (int i=0; i<months; i++)
            mils[i]= monthlySetups.get(i).mils;
        return mils;
    }
    ///Get a list of chromium each month
    public double[] get_chromium()
    {
        double[] chromium=new double[months];
        for (int i=0; i<months; i++)
            chromium[i]= monthlySetups.get(i).chromium;
        return chromium;
    }
    ///Get a list of rubber each month
    public double[] get_rubber()
    {
        double[] rubber=new double[months];
        for (int i=0; i<months; i++)
            rubber[i]= monthlySetups.get(i).rubber;
        return rubber;
    }
    ///Get a list of aluminium each month
    public double[] get_aluminium()
    {
        double[] aluminium=new double[months];
        for (int i=0; i<months; i++)
            aluminium[i]= monthlySetups.get(i).aluminium;
        return aluminium;
    }
    ///Get a list of steel each month
    public double[] get_steel()
    {
        double[] steel=new double[months];
        for (int i=0; i<months; i++)
            steel[i]= monthlySetups.get(i).steel;
        return steel;
    }
    ///Get a list of tungsten each month
    public double[] get_tungsten()
    {
        double[] tungsten=new double[months];
        for (int i=0; i<months; i++)
            tungsten[i]= monthlySetups.get(i).tungsten;
        return tungsten;
    }


    List<MonthlySetup> monthlySetups= new ArrayList<MonthlySetup>();

    /// All equipment in the simulation
    private List<Equipment> equipmentList;

    int months;//How many months do we have data for?

    //what day after game start is building allowed?
    int buildingStart;
    public NationalSetup(String filename,int buildingStart)
    {
        this.buildingStart = buildingStart;
        try {
            List<Equipment> equipmentList = Equipment.EquipmentLoader.loadEquipment("equipment.json");
            for (Equipment equipment : equipmentList) {
                System.out.println("Id: " + equipment.getId());
                System.out.println("Name: " + equipment.getName());
                if (equipment.getNextGen() != null) {
                    System.out.println("Next Gen Id: " + equipment.getNextGen().getId());
                } else {
                    System.out.println("No Next Gen");
                }
                System.out.println();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        try {

            //Read the csv file line by line
            BufferedReader in = new BufferedReader(new FileReader(filename));
            months = 0;
            int States=0;//How many states are there in this CSV file?
            //First read the header
            String line=in.readLine();
            //Split into components, and read where exactly the different stats are stored
            String[] headers = line.split(",");


            //I can not guarantee that the columns are in any particular order, so let us build a dictionary to extract the data
            //A dictionary of what basic data lives at what column
            Map<String,Integer> columnDictionary= new HashMap<>();
            //And a dictionary for level 3,4 and 5 states and their names
            Map<String,Integer> level5StateDictionary=new HashMap<>();
            Map<String,Integer> level4StateDictionary=new HashMap<>();
            Map<String,Integer> level3StateDictionary=new HashMap<>();

            //Regex match for capturing infrastructure level and state from the header
            Pattern InfrastructurePattern=Pattern.compile("Infrastructure ([0-5])\\s+(.+)");
            for (int i=0;i<headers.length;i++)
            {
                //First check if this begins with "Infrastructure 5 NAME"
                var infrastructureMatcher = InfrastructurePattern.matcher(headers[i]);

                //Put this states with infrastructure 3 or above in the dictionary, anything below is not worth investing in
                if (infrastructureMatcher.matches())
                {
                    int infrastructure = Integer.parseInt(infrastructureMatcher.group(1));
                    String state = infrastructureMatcher.group(2);
                    if (infrastructure==5)
                        level5StateDictionary.put(state.trim(),i);
                    else if (infrastructure==4)
                        level4StateDictionary.put(state.trim(),i);
                    else if (infrastructure==3)
                        level3StateDictionary.put(state.trim(),i);
                }
                else
                    columnDictionary.put(headers[i].trim(),i);
            }

            if(!columnDictionary.containsKey("Stability"))
                throw new RuntimeException("Stability not set");
            if(!columnDictionary.containsKey(("General Construction")))
                throw new RuntimeException("General Construction speed not set");
            if(!columnDictionary.containsKey(("Military Construction")))
                throw new RuntimeException("Military ConstructionStability not set");
            if(!columnDictionary.containsKey(("Civilian Construction")))
                throw new RuntimeException("Civilian ConstructionStability not set");
            if(!columnDictionary.containsKey(("Factory Output")))
                throw new RuntimeException("Factory Output not set");
            if(!columnDictionary.containsKey(("Efficiency Cap")))
                throw new RuntimeException("EfficiencyCap not set");
            if(!columnDictionary.containsKey(("Consumer Goods Ratio")))
                throw new RuntimeException("Consumer Goods Ratio not set");
            if(!columnDictionary.containsKey(("Consumer Goods Factor")))
                throw new RuntimeException("Consumer Goods Factor not set");
            if(!columnDictionary.containsKey(("Civilian Factories")))
                throw new RuntimeException("Civilian Factories not set");
            if(!columnDictionary.containsKey(("Special Projects")))
                throw new RuntimeException("Special Projects not set");
            if(!columnDictionary.containsKey(("Available Factories")))
                throw new RuntimeException("Available Factories not set");
            if(!columnDictionary.containsKey(("Military Factories")))
                throw new RuntimeException("Military Factories not set");
            if(!columnDictionary.containsKey(("Rubber")))
                throw new RuntimeException("Rubber not set");
            if(!columnDictionary.containsKey(("Steel")))
                throw new RuntimeException("Steel not set");
            if(!columnDictionary.containsKey(("Aluminium")))
                throw new RuntimeException("Aluminium not set");
            if(!columnDictionary.containsKey(("Tungsten")))
                throw new RuntimeException("Tungsten not set");
            if(!columnDictionary.containsKey(("Chromium")))
                throw new RuntimeException("Chromium not set");

            States = level3StateDictionary.size()+level4StateDictionary.size()+level5StateDictionary.size();

            line = in.readLine();

            while (line != null) {
                String[] entries = line.split(",");

                List<String> Level5Slots = new ArrayList<>();
                List<String> Level4Slots = new ArrayList<>();
                List<String> Level3Slots = new ArrayList<>();
                for (var l5s : level5StateDictionary.entrySet())
                    for (int k=0; k <Integer.parseInt(entries[l5s.getValue()].trim()); k++)
                        Level5Slots.add(l5s.getKey());
                for (var l4s : level4StateDictionary.entrySet())
                    for (int k=0; k <Integer.parseInt(entries[l4s.getValue()].trim()); k++)
                        Level4Slots.add(l4s.getKey());
                for (var l3s : level3StateDictionary.entrySet())
                    for (int k=0; k <Integer.parseInt(entries[l3s.getValue()].trim()); k++)
                        Level3Slots.add(l3s.getKey());

                var thisMonth = new MonthlySetup(
                        Double.parseDouble(entries[columnDictionary.get("Stability")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("General Construction")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("Military Construction")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("Civilian Construction")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("Factory Output")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("Efficiency Cap")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("Consumer Goods Ratio")].trim()),
                        Double.parseDouble(entries[columnDictionary.get("Consumer Goods Factor")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Civilian Factories")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Special Projects")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Available Factories")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Military Factories")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Rubber")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Steel")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Aluminium")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Tungsten")].trim()),
                        Integer.parseInt(entries[columnDictionary.get("Chromium")].trim()),
                        Level5Slots,
                        Level4Slots,
                        Level3Slots
                );

                monthlySetups.add(thisMonth);

                System.out.println(line);
                // read next line
                line = in.readLine();
            }

            months=monthlySetups.size();

            in.close();
        } catch (IOException e) {//Whatever, just rethrow
            throw new RuntimeException(e);
        }
    }
}
