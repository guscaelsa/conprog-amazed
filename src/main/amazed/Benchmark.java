package amazed;

import amazed.maze.Amazed;

import java.util.concurrent.ForkJoinPool;

public class Benchmark {
    public static void main(String[] args)
            throws InterruptedException
    {
        String map = "maps/vast.map";
        boolean sequential = false;
        int forkAfter = 10;
        int runs = 10;

        System.out.print(map);
        if (sequential) {
            System.out.print(" @ -");
        } else {
            System.out.print(" @ " + forkAfter);
        }
        System.out.print(" [" + ForkJoinPool.getCommonPoolParallelism() + "]");
        System.out.println();

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
