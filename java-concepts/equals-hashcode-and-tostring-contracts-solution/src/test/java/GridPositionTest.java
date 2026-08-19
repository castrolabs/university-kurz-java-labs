import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GridPosition")
class GridPositionTest {

    @Test
    @DisplayName("should be equal to itself (reflexive)")
    void shouldBeEqualToItself() {
        GridPosition p = new GridPosition(2, 3);

        assertEquals(p, p);
    }

    @Test
    @DisplayName("should be equal to another instance with the same coordinates")
    void shouldBeEqualToAnotherInstanceWithTheSameCoordinates() {
        GridPosition a = new GridPosition(2, 3);
        GridPosition b = new GridPosition(2, 3);

        assertEquals(a, b);
        assertEquals(b, a);
    }

    @Test
    @DisplayName("should not be equal to an instance with different coordinates")
    void shouldNotBeEqualToAnInstanceWithDifferentCoordinates() {
        GridPosition a = new GridPosition(2, 3);
        GridPosition b = new GridPosition(3, 2);

        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("should not be equal to null")
    void shouldNotBeEqualToNull() {
        GridPosition p = new GridPosition(2, 3);

        assertFalse(p.equals(null));
    }

    @Test
    @DisplayName("should not be equal to an object of a different type")
    void shouldNotBeEqualToAnObjectOfADifferentType() {
        GridPosition p = new GridPosition(2, 3);

        assertFalse(p.equals("(2, 3)"));
    }

    @Test
    @DisplayName("should have the same hashCode as an equal instance")
    void shouldHaveTheSameHashCodeAsAnEqualInstance() {
        GridPosition a = new GridPosition(5, 7);
        GridPosition b = new GridPosition(5, 7);

        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("should find a logically equal position in a HashSet even though it's a different instance")
    void shouldFindALogicallyEqualPositionInAHashSet() {
        Set<GridPosition> visited = new HashSet<>();
        visited.add(new GridPosition(2, 3));

        assertTrue(visited.contains(new GridPosition(2, 3)),
                "a HashSet lookup relies on equals() and hashCode() agreeing with each other");
    }

    @Test
    @DisplayName("should not grow when adding a logically equal position twice")
    void shouldNotGrowWhenAddingALogicallyEqualPositionTwice() {
        Set<GridPosition> visited = new HashSet<>();
        visited.add(new GridPosition(2, 3));
        visited.add(new GridPosition(2, 3));

        assertEquals(1, visited.size());
    }

    @Test
    @DisplayName("should treat logically equal keys as the same HashMap key")
    void shouldTreatLogicallyEqualKeysAsTheSameHashMapKey() {
        Map<GridPosition, String> labels = new HashMap<>();
        labels.put(new GridPosition(0, 0), "origin");

        assertEquals("origin", labels.get(new GridPosition(0, 0)));
    }

    @Test
    @DisplayName("should format toString as (row, col)")
    void shouldFormatToStringAsRowCol() {
        GridPosition p = new GridPosition(4, 9);

        assertEquals("(4, 9)", p.toString());
    }

    @Test
    @DisplayName("should behave like equals() for two instances with the same coordinates (optional)")
    void shouldBehaveLikeEqualsForTwoInstancesWithTheSameCoordinatesOptional() {
        GridPosition a = new GridPosition(1, 1);
        GridPosition b = new GridPosition(1, 1);

        assertTrue(a.equalsUsingGetClass(b));
    }

    @Test
    @DisplayName("should reject a different type using getClass() (optional)")
    void shouldRejectADifferentTypeUsingGetClassOptional() {
        GridPosition a = new GridPosition(1, 1);

        assertFalse(a.equalsUsingGetClass("(1, 1)"));
        assertFalse(a.equalsUsingGetClass(null));
    }
}
