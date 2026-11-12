namespace ControlFlowLab;

public sealed record SearchResult(int Row, int Column, int Visited);

public static class Flow
{
    public static string Tier(int level)
    {
        // TODO-00: a switch STATEMENT where each higher tier also earns the lower ones, using
        // `goto case` because C# forbids falling through (CS0163):
        //   3 -> "gold silver bronze"
        //   2 -> "silver bronze"
        //   1 -> "bronze"
        //   0 and -1 (two labels, one section) -> "none"
        //   anything else (default) -> "?"
        // Build the text with += and finish each section with `break` or `goto case`.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static int Retry(Func<int, bool> attempt, int maxAttempts)
    {
        // TODO-01: call attempt(n) with n = 1, 2, 3... until it returns true or n reaches
        // maxAttempts. Return the number of the last attempt made. Use a label and `goto`
        // (a backward jump) instead of a loop, to see that it is legal. A loop is the better
        // tool here; the exercise is about the mechanism.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static SearchResult FindFirst(int[][] grid, int goal)
    {
        // TODO-02: scan the grid row by row. Count every cell you look at in Visited. When a
        // cell equals goal, leave BOTH loops with a labeled `break` and return its position.
        // When the goal is missing, return Row = -1, Column = -1 and the total visited.
        throw new NotImplementedException("Not implemented yet.");
    }

    public static int CountOpenCells(int[][] grid)
    {
        // TODO-03: a 0 blocks the rest of its row. Count the non-zero cells that come before
        // the first 0 of each row, using a labeled `continue` to jump to the next row.
        //   [[1,0,1],[2,3,9],[4,5,6]] -> 1 + 3 + 3 = 7
        throw new NotImplementedException("Not implemented yet.");
    }

    public static int RunCommands(int[] commands)
    {
        // TODO-04: a while loop over the commands with a switch inside. Inside the switch a
        // plain `break` only leaves the switch, so label the loop:
        //   0   -> skip this command (continue the loop)
        //   -1  -> stop everything (break the loop)
        //   any other value -> add it to the total
        // Return the total.
        throw new NotImplementedException("Not implemented yet.");
    }
}
