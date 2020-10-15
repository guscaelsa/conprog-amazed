package amazed.solver;

import amazed.maze.Maze;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;

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
    List<ForkJoinSolver> children = new ArrayList<>();
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
    }

    public ForkJoinSolver(Maze maze, Set<Integer> visited, Map<Integer, Integer> predecessor, int start) {
        this(maze);
        this.predecessor.putAll(predecessor); // copy the parent's predecessor

        // this is really ugly, but it's seems to be the way this class was designed
        this.visited = visited;
        this.start = start;
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
        int player = maze.newPlayer(start);
        frontier.push(start);

        while (!frontier.empty()) {
            int current = frontier.pop();

            if (maze.hasGoal(current)) {
                maze.move(player, current);
                return pathFromTo(maze.start(), current);
            }

            if (visited.add(current)) { // "true if this set did not already contain the specified element"
                maze.move(player, current);

                for (int nb: maze.neighbors(current)) {
                    // if nb has not been already visited,
                    // nb can be reached from current (i.e., current is nb's predecessor)
                    if (!visited.contains(nb)) {
                        predecessor.put(nb, current);
                    }

                    if (true /*should fork?*/) {
                        ForkJoinSolver child = new ForkJoinSolver(maze, visited, predecessor, nb);
                        children.add(child);
                        child.fork();
                    } else {
                        frontier.push(nb);
                    }
                }
            }
        }
        //maze.move(player, maze.start());
        // all nodes explored
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
