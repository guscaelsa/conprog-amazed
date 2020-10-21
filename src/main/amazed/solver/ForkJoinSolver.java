package amazed.solver;

import amazed.maze.Maze;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <code>ForkJoinSolver</code> implements a solver for
 * <code>Maze</code> objects using a fork/join multi-thread
 * depth-first search.
 * <p>
 * Instances of <code>ForkJoinSolver</code> should be run by a
 * <code>ForkJoinPool</code> object.
 */


public class ForkJoinSolver
    extends SequentialSolver
{
    protected List<ForkJoinSolver> children = new ArrayList<>();
    protected AtomicBoolean shutdown;

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal.
     */
    public ForkJoinSolver(Maze maze) {
        super(maze);
    }

    /**
     * Creates a solver that searches in <code>maze</code> from the
     * start node to a goal, forking after <code>forkAfter</code> visited
     * nodes.
     */
    public ForkJoinSolver(Maze maze, int forkAfter) {
        this(maze);
        this.forkAfter = forkAfter;
        //this.shutdown = new Flag();
        this.shutdown =  new AtomicBoolean(false);
    }

    public ForkJoinSolver(Maze maze, Set<Integer> visited, Map<Integer, Integer> predecessor, int start, AtomicBoolean shutdown, int forkAfter) {
        this(maze);
        this.predecessor.putAll(predecessor); // copy the parent's predecessor
        this.forkAfter = forkAfter;

        // this is really ugly, but it seems to be the way this class was designed
        this.visited = visited;
        this.start = start;
        this.shutdown = shutdown;
    }

    @Override
    protected void initStructures() {
        visited = new ConcurrentSkipListSet<>(); // shared, so must be thread-safe
        predecessor = new HashMap<>(); // merged together on join()
        frontier = new Stack<>(); // completely local
    }

    /**
     * Searches for and returns the path, as a list of node
     * identifiers, that goes from the start node to a goal node in
     * the maze. If such a path cannot be found (because there are no
     * goals, or all goals are unreacheable), the method returns
     * <code>null</code>.
     */
    @Override
    public List<Integer> compute() {
        return parallelSearch();
    }

    private List<Integer> parallelSearch() {
        // System.out.println("QQQ " + System.identityHashCode(visited));
        int count = 0;
        int player = maze.newPlayer(start);
        frontier.push(start);

        while (!frontier.empty()) {
            int current = frontier.pop();

            if (visited.add(current)) { // "true if this set did not already contain the specified element"
                maze.move(player, current);

                if (maze.hasGoal(current)) {
                    shutdown.set(true);
                    System.out.println("found goal");
                    return pathFromTo(maze.start(), current);
                }

                for (int nb: maze.neighbors(current)) {
                    if (shutdown.get()) {
                        System.out.print(".");
                        break;
                    }

                    if (predecessor.containsKey(nb)) {
                        continue;
                    }
                    predecessor.put(nb, current);

                    if (count >= forkAfter /*should fork?*/) {
                        ForkJoinSolver child = new ForkJoinSolver(maze, visited, predecessor, nb, shutdown, forkAfter);
                        children.add(child);
                        child.fork();
                        count = 0;
                    } else {
                        frontier.push(nb);
                        count++;
                    }
                }
            }
            if (shutdown.get()) {
                System.out.print("!");
                break;
            }
        }
        //maze.move(player, maze.start());

        // all nodes explored, wait for children
        for (ForkJoinSolver child : children) {
            List<Integer> result = child.join();
            if (result != null) {
                return result;
            }
        }
        // no goal found
        return null;
    }
}
