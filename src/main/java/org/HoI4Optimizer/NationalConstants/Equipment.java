package org.HoI4Optimizer.NationalConstants;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/// A piece of military equipment
public class Equipment {

    /// Possible brand names for this product, may be empty "state" "national" and none can always be used (though with lower priority)
    public List<String> brandNames;
    /// Possible names the auto-generated factory name can use
    public List<String> factoryNames;

    private int Id;

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    /// Name of this exact make of equipment, a unique name is prefered but not required
    private String name;

    /// A short descriptive which should be the same for different models of same thing (for example "riffle")
    private String shortname;

    public List<String> getBrandNames() {
        return brandNames;
    }

    public List<String> getFactoryNames() {
        return factoryNames;
    }

    public void setBrandNames(List<String> brandNames) {
        this.brandNames = brandNames;
    }

    public void setFactoryNames(List<String> factoryNames) {
        this.factoryNames = factoryNames;
    }

    public String getShortname() {
        return shortname;
    }
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /// Name of this exact make of equipment
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /// The unit cost in Military Industrial Capacity
    private double unit_cost;

    /// The unit cost in Military Industrial Capacity
    public double getUnit_cost() {
        return unit_cost;
    }

    public void setUnit_cost(double unit_cost) {
        this.unit_cost = unit_cost;
    }

    /// When is this unlocked after January 1 1936:
    private int researchTime;

    /// When is this unlocked after January 1 1936:
    public int getResearchTime() {
        return researchTime;
    }

    public void setResearchTime(int ResearchTime) {
        this.researchTime = ResearchTime;
    }

    /// Next upgraded model of this thing
    private Equipment nextGen;

    /// Next upgraded model of this thing
    public Equipment getNextGen() {
        return nextGen;
    }

    public void setNextGen(Equipment NextGen) {
        this.nextGen = NextGen;
    }

    /// (Only relevant if this is a NextGen), is this a small upgrade which factories can switch to for only 10% efficiency loss
    /// A small upgrade could be for Example Bf 109 F ->  Bf 109 G, a large could be Panzer III -> Panther
    private boolean smallUpgrade;

    /// (Only relevant if this is a NextGen), is this a small upgrade which factories can switch to for only 10% efficiency loss
    /// A small upgrade could be for Example Bf 109 F ->  Bf 109 G, a large could be Panzer III -> Panther
    public boolean getSmallUpgrade() {
        return smallUpgrade;
    }

    public void setSmallUpgrade(boolean smallUpgrade) {
        this.smallUpgrade = smallUpgrade;
    }

    /// How much rubber is required per production line?
    private int rubber;
    /// How much steel is required per production line?
    private int steel;
    /// How much aluminium is required per production line?
    private int aluminium;
    /// How much tungsten is required per production line?
    private int tungsten;
    /// How much Chromium is required per production line?
    private int chromium;

    /// How much aluminium is required per production line?
    public int getAluminium() {
        return aluminium;
    }

    /// How much tungsten is required per production line?
    public int getTungsten() {
        return tungsten;
    }

    /// How much rubber is required per production line?
    public int getRubber() {
        return rubber;
    }

    /// How much steel is required per production line?
    public int getSteel() {
        return steel;
    }

    /// How much Chromium is required per production line?
    public int getChromium() {
        return chromium;
    }

    public void setAluminium(int aluminium) {
        this.aluminium = aluminium;
    }

    public void setTungsten(int tungsten) {
        this.tungsten = tungsten;
    }

    public void setRubber(int rubber) {
        this.rubber = rubber;
    }

    public void setSteel(int steel) {
        this.steel = steel;
    }

    public void setChromium(int chromium) {
        this.chromium = chromium;
    }

    /// Constructor, this is meant to be used by the Json loader
    public Equipment() {
        this.Id = 0;
        this.name = "Consumer Good";
        this.unit_cost = 1.0;
        this.researchTime = 0;
        this.nextGen = null;
        this.smallUpgrade = false;
        this.rubber = 0;
        this.steel = 0;
        this.chromium = 0;
        this.aluminium = 0;
        this.tungsten = 0;
    }

    ///load a list of equipment from json, identified by a unique name
    public static Map<String,Equipment> loadEquipment(String filePath) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        //First simply load the list
        var EquipList= objectMapper.readValue(new File(filePath), new TypeReference<List<Equipment>>() {});
        //The un-pack it so everything is both in the list, and reference to in its upgrades
        var Out = new HashMap<String,Equipment>();
        for (var E : EquipList) {
            Out.put(E.name,E);
            for (var Next = E.nextGen; Next!=null; Next=Next.nextGen)
                if (!Out.containsKey(Next.name))
                    Out.put(Next.name,Next);}

        return Out;
    }
}
