package amazed;

import amazed.maze.Amazed;

public class Benchmark {
    public static void main(String[] args)
            throws InterruptedException
    {
        String map = "maps/vast.map";
        boolean sequential = false;
        int forkAfter = 20;
        int runs = 30;

        System.out.print(map);
        if (sequential) {
            System.out.println(" @ -");
        } else {
            System.out.println(" @ " + forkAfter);
        }

        for (int i=0; i<runs; ++i) {
            Amazed amazed = new Amazed(map, sequential, forkAfter, -1);
            long start = System.currentTimeMillis();
            boolean solved = amazed.solve(false);
            long stop = System.currentTimeMillis();
            long elapsed = stop - start;
            if (solved) {
                System.out.print("✔ ");
            } else {
                System.out.print("✗ ");
            }
            System.out.println(elapsed);
        }
    }
}
