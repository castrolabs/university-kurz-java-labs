import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OrderStatusWorkflow")
class OrderStatusWorkflowTest {

    @Test
    @DisplayName("nextStatus should advance through the normal workflow")
    void nextStatusShouldAdvanceThroughNormalWorkflow() {
        assertEquals(OrderStatus.PAID, OrderStatusWorkflow.nextStatus(OrderStatus.PLACED));
        assertEquals(OrderStatus.SHIPPED, OrderStatusWorkflow.nextStatus(OrderStatus.PAID));
        assertEquals(OrderStatus.DELIVERED, OrderStatusWorkflow.nextStatus(OrderStatus.SHIPPED));
    }

    @Test
    @DisplayName("nextStatus should keep terminal statuses unchanged")
    void nextStatusShouldKeepTerminalStatusesUnchanged() {
        assertEquals(OrderStatus.DELIVERED, OrderStatusWorkflow.nextStatus(OrderStatus.DELIVERED));
        assertEquals(OrderStatus.CANCELLED, OrderStatusWorkflow.nextStatus(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("shippingPhase should group PLACED and PAID as preparing")
    void shippingPhaseShouldGroupPlacedAndPaidAsPreparing() {
        assertEquals("preparing", OrderStatusWorkflow.shippingPhase(OrderStatus.PLACED));
        assertEquals("preparing", OrderStatusWorkflow.shippingPhase(OrderStatus.PAID));
    }

    @Test
    @DisplayName("shippingPhase should group SHIPPED and DELIVERED together")
    void shippingPhaseShouldGroupShippedAndDeliveredTogether() {
        assertEquals("in transit or delivered", OrderStatusWorkflow.shippingPhase(OrderStatus.SHIPPED));
        assertEquals("in transit or delivered", OrderStatusWorkflow.shippingPhase(OrderStatus.DELIVERED));
    }

    @Test
    @DisplayName("shippingPhase should report cancelled orders separately")
    void shippingPhaseShouldReportCancelledSeparately() {
        assertEquals("cancelled", OrderStatusWorkflow.shippingPhase(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("describe should compute the PAID message from a block-bodied yield arm")
    void describeShouldComputePaidMessageFromBlockBodiedArm() {
        assertEquals("Payment received; awaiting shipment", OrderStatusWorkflow.describe(OrderStatus.PAID));
    }

    @Test
    @DisplayName("describe should return a message for every other status")
    void describeShouldReturnMessageForEveryOtherStatus() {
        assertEquals("Order placed", OrderStatusWorkflow.describe(OrderStatus.PLACED));
        assertEquals("Order shipped", OrderStatusWorkflow.describe(OrderStatus.SHIPPED));
        assertEquals("Order delivered", OrderStatusWorkflow.describe(OrderStatus.DELIVERED));
        assertEquals("Order cancelled", OrderStatusWorkflow.describe(OrderStatus.CANCELLED));
    }

    @Test
    @DisplayName("parseStatus should parse every known code")
    void parseStatusShouldParseEveryKnownCode() {
        assertEquals(OrderStatus.PLACED, OrderStatusWorkflow.parseStatus("PLACED"));
        assertEquals(OrderStatus.PAID, OrderStatusWorkflow.parseStatus("PAID"));
        assertEquals(OrderStatus.SHIPPED, OrderStatusWorkflow.parseStatus("SHIPPED"));
        assertEquals(OrderStatus.DELIVERED, OrderStatusWorkflow.parseStatus("DELIVERED"));
        assertEquals(OrderStatus.CANCELLED, OrderStatusWorkflow.parseStatus("CANCELLED"));
    }

    @Test
    @DisplayName("parseStatus should reject a null code explicitly, not via NullPointerException")
    void parseStatusShouldRejectNullCodeExplicitly() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OrderStatusWorkflow.parseStatus(null));

        assertEquals("status code must not be null", exception.getMessage());
    }

    @Test
    @DisplayName("parseStatus should reject an unknown code")
    void parseStatusShouldRejectUnknownCode() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> OrderStatusWorkflow.parseStatus("ARCHIVED"));

        assertEquals("unknown status code: ARCHIVED", exception.getMessage());
    }

    @Test
    @DisplayName("parseStatus should reject an empty code")
    void parseStatusShouldRejectEmptyCode() {
        assertThrows(IllegalArgumentException.class, () -> OrderStatusWorkflow.parseStatus(""));
    }

    @Test
    @DisplayName("priority should rank terminal statuses lowest (bonus)")
    void priorityShouldRankTerminalStatusesLowest() {
        assertEquals(0, OrderStatusWorkflow.priority(OrderStatus.DELIVERED));
        assertEquals(0, OrderStatusWorkflow.priority(OrderStatus.CANCELLED));
        assertEquals(1, OrderStatusWorkflow.priority(OrderStatus.PLACED));
        assertEquals(2, OrderStatusWorkflow.priority(OrderStatus.PAID));
        assertEquals(3, OrderStatusWorkflow.priority(OrderStatus.SHIPPED));
    }
}
