package ua.edu.nupp.kpp.server;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CourseworkTest {

    private static Work work(double safety, double mobility, double hours) {
        return new Work(null, 1L, "Тестова робота", safety, mobility, hours, new BigDecimal("100.00"));
    }

    @Test
    void factor1WithoutWorksIsZero() {
        assertEquals(0.0, PriorityCalculator.factor1(List.of()), 0.001);
    }

    @Test
    void factor1TakesMaxSafetyScore() {
        List<Work> works = List.of(work(70, 60, 2.5), work(40, 20, 1.0));
        assertEquals(70.0, PriorityCalculator.factor1(works), 0.001);
    }

    @Test
    void factor3ForOrder1Is875() {
        List<Work> works = List.of(work(90, 40, 1.5), work(90, 40, 2.0));
        assertEquals(8.75, PriorityCalculator.factor3(works), 0.001);
    }

    @Test
    void priorityForOrder1Is5219() {
        assertEquals(52.19, PriorityCalculator.calculate(90, 40, 8.75), 0.001);
    }

    @Test
    void priorityRejectsFactorAbove100() {
        assertThrows(IllegalArgumentException.class, () -> PriorityCalculator.calculate(101, 10, 10));
    }

    @Test
    void createdToDiagnosedAllowed() {
        assertTrue(ServiceOrderService.isAllowedTransition(OrderState.CREATED, OrderState.DIAGNOSED));
    }

    @Test
    void diagnosedToRepairAllowed() {
        assertTrue(ServiceOrderService.isAllowedTransition(OrderState.DIAGNOSED, OrderState.REPAIR));
    }

    @Test
    void repairToCompletedAllowed() {
        assertTrue(ServiceOrderService.isAllowedTransition(OrderState.REPAIR, OrderState.COMPLETED));
    }

    @Test
    void createdToCompletedRejected() {
        assertFalse(ServiceOrderService.isAllowedTransition(OrderState.CREATED, OrderState.COMPLETED));
    }

    @Test
    void clientValidationWorks() {
        Client client = new Client(null, "", "+380501112233", null);
        assertThrows(IllegalArgumentException.class, () -> ClientService.validate(client));
    }

    @Test
    void carValidationWorks() {
        Car car = new Car(null, 1L, "SHORT", "BI1234AA", "Skoda", "Octavia", 2018, 87500);
        assertThrows(IllegalArgumentException.class, () -> CarService.validate(car));
    }

    @Test
    void workValidationWorks() {
        Work invalid = work(120, 40, 1.5);
        assertThrows(IllegalArgumentException.class, () -> WorkService.validate(invalid));
    }
}
