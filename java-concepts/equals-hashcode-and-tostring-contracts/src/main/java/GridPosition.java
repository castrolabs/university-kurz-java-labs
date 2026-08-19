import java.util.Objects;

public class GridPosition {

    private final int row;
    private final int col;

    public GridPosition(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    @Override
    public boolean equals(Object o) {
        // TODO-00: Implement value equality: two GridPositions are equal when
        // they have the same row and the same col. Short-circuit on reference
        // equality first, then use `instanceof` to cover both the null check
        // and the type check in one step.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public int hashCode() {
        // TODO-01: Implement a hashCode() consistent with equals() above -
        // every field read by equals() must be read here too.
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    @Override
    public String toString() {
        // TODO-02: Return a readable representation, formatted exactly as
        // "(row, col)", e.g. "(2, 3)".
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public boolean equalsUsingGetClass(Object o) {
        // TODO-03 (optional): Re-implement the equality check from TODO-00,
        // but using getClass() instead of instanceof, to compare the
        // trade-off described in the article.
        throw new UnsupportedOperationException("Not implemented yet.");
    }
}
