package capstone.main.Pathfinding;

import java.util.*;

public class Dijkstra {

    public static List<NavNode> findPath(NavMesh navMesh, NavNode start, NavNode goal) {
        if (start == null || goal == null) return new ArrayList<>();

        Map<NavNode, Float> dist = new HashMap<>();
        Map<NavNode, NavNode> prev = new HashMap<>();
        Set<NavNode> visited = new HashSet<>();
        PriorityQueue<NavNode> pq = new PriorityQueue<>(Comparator.comparingDouble(dist::get));

        // Initialize distances
        for (NavNode node : navMesh.getNodes()) {
            dist.put(node, Float.MAX_VALUE);
        }
        dist.put(start, 0f);
        pq.add(start);

        while (!pq.isEmpty()) {
            NavNode current = pq.poll();
            if (visited.contains(current)) continue;
            visited.add(current);

            // Stop early if we reached goal
            if (current == goal) break;

            if (current.neighbors != null) {
                for (NavNode neighbor : current.neighbors) {
                    if (neighbor == null || !neighbor.walkable) continue;

                    // Distance between nodes (Manhattan for grid)
                    float alt = dist.get(current) + 1f; // all edges = 1
                    if (alt < dist.get(neighbor)) {
                        dist.put(neighbor, alt);
                        prev.put(neighbor, current);
                        pq.add(neighbor);
                    }
                }
            }
        }

        // Reconstruct path
        List<NavNode> path = new ArrayList<>();
        NavNode step = goal;
        while (step != null && prev.containsKey(step)) {
            path.add(0, step);
            step = prev.get(step);
        }

        // Include start if path is valid
        if (!path.isEmpty() && path.get(0) != start) path.add(0, start);

        return path;
    }
}
