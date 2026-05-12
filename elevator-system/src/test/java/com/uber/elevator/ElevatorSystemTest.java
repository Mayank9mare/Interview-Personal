package com.uber.elevator;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ElevatorSystemTest {
    @Test
    void pickupAssignsNearestIdleElevator() {
        ElevatorSystem system = new ElevatorSystem(2, 0, 10);
        system.requestDrop(1, 8);
        for (int i = 0; i < 8; i++) system.step();

        int assigned = system.requestPickup(7, ElevatorSystem.Direction.DOWN);

        assertEquals(1, assigned);
    }

    @Test
    void elevatorMovesToRequestedDropFloor() {
        ElevatorSystem system = new ElevatorSystem(1, 0, 10);
        system.requestDrop(0, 3);

        for (int i = 0; i < 3; i++) system.step();

        assertEquals(3, system.getElevator(0).getFloor());
        assertEquals(ElevatorSystem.Direction.IDLE, system.getElevator(0).getDirection());
    }
}
