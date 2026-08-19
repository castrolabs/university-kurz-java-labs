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
        if (this == o) return true;
        if (!(o instanceof GridPosition p)) return false;
        return row == p.row && col == p.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }

    @Override
    public String toString() {
        return "(" + row + ", " + col + ")";
    }

    public boolean equalsUsingGetClass(Object o) {
        if (o == null || o.getClass() != getClass()) return false;
        GridPosition p = (GridPosition) o;
        return row == p.row && col == p.col;
    }
}
