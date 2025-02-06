package org.HoI4Optimizer.NationalConstants;

public enum stateType {
    ///tiny Island (Like Midway), tiny Enclaves (Like Gibraltar) do not have room for factories uninhabited regions (Like the Sahara) can't build anything
    tinyPopulation(0),
    ///small island is also classified as pastoral, since it has only 1 slot
    pastoral(1),
    rural(2),
    town(4),
    large_town(5),
    city(6),
    large_city(8),
    metropolis(10),
    megalopolis(12);

    private final int buildingSlots;
    public int getBuildingSlots() {return buildingSlots;}
    stateType(int buildingSlots) {
        this.buildingSlots = buildingSlots;
    }

    @Override
    public String toString() {
        return switch (buildingSlots) {
            case 0 -> "Tiny Population";
            case 1 -> "Pastoral region";
            case 2 -> "Rural region";
            case 4 -> "Small towns";
            case 5 -> "Highly developed rural region";
            case 6 -> "Urban region";
            case 8 -> "Large city";
            case 10 -> "Metropolis";
            case 12 -> "Megalopolis";
            default -> "unknown";
        };
    }
}
