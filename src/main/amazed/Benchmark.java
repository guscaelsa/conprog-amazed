package amazed;

import amazed.maze.Amazed;

public class Benchmark {
    public static void main(String[] args)
            throws InterruptedException
    {
        String map = "maps/large.map";
        boolean sequential = false;
        int forkAfter = 20;
        int runs = 10;

        System.out.print(map);
        if (sequential) {
            System.out.println(" @ -");
        } else {
            System.out.println(" @ " + forkAfter);
        }

        for (int i=0; i<runs; ++i) {
            Amazed amazed = new Amazed(map, sequential, forkAfter, -1);
            long start = System.currentTimeMillis();
            amazed.solve(false);
            long stop = System.currentTimeMillis();
            long elapsed = stop - start;
            System.out.println(elapsed);
        }
    }
}
