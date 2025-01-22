package org.HoI4Optimizer.NationalConstants;

public enum stateType {
    wasteland(0),//tiny Island and Enclave are always classified as wasteland since they also don't have slots
    pastoral(1),//small island is always classified as pastoral, since it has only 1 slot
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
}
