package capstone.main.Pathfinding;

import java.util.*;

public class AStar {

    public static List<NavNode> findPath(NavMesh navMesh, NavNode start, NavNode target) {
        if (start == null || target == null || !start.walkable || !target.walkable) return Collections.emptyList();

        Map<NavNode, PathNode> allNodes = new HashMap<>();
        PriorityQueue<PathNode> openSet = new PriorityQueue<>(Comparator.comparingDouble(PathNode::fCost));
        Set<NavNode> closedSet = new HashSet<>();

        PathNode startNode = new PathNode(start);
        startNode.gCost = 0;
        startNode.hCost = heuristic(start, target);
        allNodes.put(start, startNode);
        openSet.add(startNode);

        while (!openSet.isEmpty()) {
            PathNode current = openSet.poll();
            if (current.navNode == target) return reconstructPath(current);

            closedSet.add(current.navNode);

            for (NavNode neighbor : current.navNode.neighbors) {
                if (closedSet.contains(neighbor)) continue;

                float tentativeG = current.gCost + distance(current.navNode, neighbor);

                PathNode neighborNode = allNodes.getOrDefault(neighbor, new PathNode(neighbor));
                if (tentativeG < neighborNode.gCost || !allNodes.containsKey(neighbor)) {
                    neighborNode.gCost = tentativeG;
                    neighborNode.hCost = heuristic(neighbor, target);
                    neighborNode.parent = current;
                    allNodes.put(neighbor, neighborNode);

                    if (!openSet.contains(neighborNode)) {
                        openSet.add(neighborNode);
                    }
                }
            }
        }

        return Collections.emptyList(); // no path found
    }

    private static List<NavNode> reconstructPath(PathNode endNode) {
        List<NavNode> path = new ArrayList<>();
        PathNode current = endNode;
        while (current != null) {
            path.add(current.navNode);
            current = current.parent;
        }
        Collections.reverse(path);
        return path;
    }

    private static float heuristic(NavNode a, NavNode b) {
        // Diagonal distance
        float dx = Math.abs(a.x - b.x);
        float dy = Math.abs(a.y - b.y);
        return dx + dy + (float)(Math.sqrt(2) - 2) * Math.min(dx, dy);
    }

    private static float distance(NavNode a, NavNode b) {
        // Euclidean distance
        float dx = a.x - b.x;
        float dy = a.y - b.y;
        return (float) Math.sqrt(dx*dx + dy*dy);
    }
}
