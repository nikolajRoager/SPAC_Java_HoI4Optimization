package org.HoI4Optimizer.Nation.Event;

import org.HoI4Optimizer.Nation.Ideology;

/// An effect which modifies a single national property
///@param modify What property is affected by this event, null: do nothing
///@param value value to set to as a double, may be cast to an int or ideology (in the order they are defined in Nation.Ideology), or boolean (value>0.5)
///@param add add to value (true), or overwrite (false)
public record PropertyEvent
(
                Target modify,
                double value,
                boolean add
)
{
    public enum Target
    {
        ///Stability, without party popularity and other effects
          base_stability,
        /// sum of all permanent effects on stability
          permanent_stability,
        /// What fraction of the population is just plain wrong?
          autocracy_support,
        /// What fraction of the population is good?
          democracy_support,
        /// What fraction of the population is literally evil?
          fascism_support,
        /// What fraction of the population is misguided?
          communism_support,
        /// How quickly is stupidity spreading?
          autocracy_growth,
        /// How quickly is the people being enlightened?
          democracy_growth,
        /// How quickly is the terrible darkness growing?
          fascism_growth,
        /// How quickly is confusion spreading?
          communism_growth,
        ///General construction speed bonus from technology and events
          construction_speed,
        ///construction speed bonus for military factories, added to construction speed
          mil_construction_speed_bonus,
        ///construction speed bonus for civilian factories,  added to construction speed
          civ_construction_speed_bonus,
        ///Efficiency cap
          efficiency_cap,
        ///Factory output bonus, Not counting effects from stability
          base_factory_output,
        ///Fraction of all factories (civilian and military)  required for "consumer goods" (the non-military part of the economy) This is subtracted from CIVILIAN factories
          base_consumer_goods_ratio,
        /// A multiplier, effecting consumer goods, will be further modified by current stability
          base_consumer_goods_multiplier,
        ///Excavation technology bonus to resource gain on a national level
          resource_gain_bonus,
        /// Base oil income, national basis, unupgraded fuel per oil, and unupgraded income from 1 refinery
          base_fuel,
        /// Additive bonuses to refinery fuel production
          refinery_fuel_bonus,
        /// Additive bonuses or penalties  to fuel per oil
          natural_fuel_bonus,
        /// Fuel capacity for each level of state infrastructure
          fuel_capacity_per_infrastructure,
        /// Baseline fuel capacity of all nations
          basic_fuel_capacity,
        /// Fraction of resources forced to be exported to the market
          resources_to_market,
        /// Percentage bonus to slots in stateEvents, applies only to slots from state type
          buildingSlotBonus,
        /// Who's in charge? effects which party popularity gives positive stability
         RulingParty,
        ///How many civilian factories are earmarked for special projects (mainly elligence and decryption) each month
        /// This is subtracted from available civs after calculating consumergoods, but before trade and construction
          special_projects_civs,
        /// Special project steel consumption
          special_steel,
        /// Special project aluminium consumption
          special_aluminium,
        /// Special project tungsten consumption
          special_tungsten,
        /// Special project chromium consumption
          special_chromium,
        /// Special project rubber consumption
          special_rubber,
        /// This modify merges Democratic, Communist, Non-aligned, or Fascist into democratic (if value is 0,1,2, or 3 respectively)
        democraticCoalition,
        /// This modify merges Democratic, Communist, Non-aligned, or Fascist into communist (if value is 0,1,2, or 3 respectively)
        communistCoalition,
        /// This modify merges Democratic, Communist, Non-aligned, or Fascist into non-aligned (if value is 0,1,2, or 3 respectively)
        nonalignedCoalition,
        /// This modify merges Democratic, Communist, Non-aligned, or Fascist into fascist (if value is 0,1,2, or 3 respectively)
        fascistCoalition,
    }

    //Faux variable: cast value to an ideology
    public Ideology ideology()
    {
        return Ideology.values()[Math.clamp((int) value,0,Ideology.values().length)];
    }
}
