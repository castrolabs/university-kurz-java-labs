namespace ControlFlowLab;

public sealed record SearchResult(int Row, int Column, int Visited);

public static class Flow
{
    public static string Tier(int level)
    {
        string text = "";
        switch (level)
        {
            case 3:
                text += "gold ";
                goto case 2;
            case 2:
                text += "silver ";
                goto case 1;
            case 1:
                text += "bronze";
                break;
            case 0:
            case -1:
                text = "none";
                break;
            default:
                text = "?";
                break;
        }
        return text;
    }

    public static int Retry(Func<int, bool> attempt, int maxAttempts)
    {
        int n = 0;
    again:
        n++;
        if (!attempt(n) && n < maxAttempts) goto again;
        return n;
    }

    public static SearchResult FindFirst(int[][] grid, int goal)
    {
        int visited = 0;
        int foundRow = -1, foundColumn = -1;
    rows:
        for (int r = 0; r < grid.Length; r++)
        {
            for (int c = 0; c < grid[r].Length; c++)
            {
                visited++;
                if (grid[r][c] == goal)
                {
                    foundRow = r;
                    foundColumn = c;
                    break rows;
                }
            }
        }
        return new SearchResult(foundRow, foundColumn, visited);
    }

    public static int CountOpenCells(int[][] grid)
    {
        int open = 0;
    rows:
        foreach (int[] row in grid)
        {
            foreach (int cell in row)
            {
                if (cell == 0) continue rows;
                open++;
            }
        }
        return open;
    }

    public static int RunCommands(int[] commands)
    {
        int total = 0;
        int i = 0;
    loop:
        while (i < commands.Length)
        {
            int command = commands[i++];
            switch (command)
            {
                case 0:
                    continue loop;
                case -1:
                    break loop;
                default:
                    total += command;
                    break;
            }
        }
        return total;
    }
}
