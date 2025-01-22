package org.HoI4Optimizer.NationalConstants;

/// A piece of military equipment
public class Equipment {
    /// Name of this exact make of equipment
    private final String name;
    /// Name of this exact make of equipment
    public String getName() {return name;}
    ///The unit cost in Military Industrial Capacity
    private final double unit_cost;
    ///The unit cost in Military Industrial Capacity
    public double getUnit_cost() {return unit_cost;}
    /// When is this unlocked after January 1 1936:
    private final int ResearchTime;
    /// When is this unlocked after January 1 1936:
    public int getResearchTime() {return ResearchTime;}

    /// Next upgraded model of this thing
    private final Equipment NextGen;
    /// Next upgraded model of this thing
    public Equipment getNext_gen() {return NextGen;}
    /// (Only relevant if this is a NextGen), is this a small upgrade which factories can switch to for only 10% efficiency loss
    /// A small upgrade could be for Example Bf 109 F ->  Bf 109 G, a large could be Panzer III -> Panther
    private final boolean smallUpgrade;
    /// (Only relevant if this is a NextGen), is this a small upgrade which factories can switch to for only 10% efficiency loss
    /// A small upgrade could be for Example Bf 109 F ->  Bf 109 G, a large could be Panzer III -> Panther
    public boolean isSmallUpgrade() {
        return smallUpgrade;
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

    /// Constructor, create a piece of equipment
    public Equipment(String name, double unit_cost, int ResearchTime, boolean smallUpgrade) {
        this.name = name;
        this.unit_cost = unit_cost;
        this.ResearchTime = ResearchTime;
        this.NextGen = null;
        this.smallUpgrade = smallUpgrade;
    }
}
