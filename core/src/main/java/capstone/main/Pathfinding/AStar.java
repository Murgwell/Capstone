package capstone.main.Pathfinding;

import java.util.*;

public class AStar {
    // Reusable collections to prevent garbage creation
    private static final Map<NavNode, PathNode> REUSABLE_NODES = new HashMap<>();
    private static final PriorityQueue<PathNode> REUSABLE_OPEN_SET = new PriorityQueue<>(Comparator.comparingDouble(PathNode::fCost));
    private static final Set<NavNode> REUSABLE_CLOSED_SET = new HashSet<>();
    private static final List<NavNode> REUSABLE_PATH = new ArrayList<>();
    
    // Object pool for PathNodes to avoid constant allocation
    private static final Queue<PathNode> PATH_NODE_POOL = new ArrayDeque<>();
    private static final int MAX_POOL_SIZE = 1000;

    public static List<NavNode> findPath(NavMesh navMesh, NavNode start, NavNode target) {
        if (start == null || target == null || !start.walkable || !target.walkable) {
            return Collections.emptyList();
        }

        // Clear and reuse collections
        REUSABLE_NODES.clear();
        REUSABLE_OPEN_SET.clear();
        REUSABLE_CLOSED_SET.clear();
        REUSABLE_PATH.clear();

        // Get or create start node
        PathNode startNode = getPooledPathNode();
        startNode.reset(start);
        startNode.gCost = 0;
        startNode.hCost = heuristic(start, target);
        
        REUSABLE_NODES.put(start, startNode);
        REUSABLE_OPEN_SET.add(startNode);

        while (!REUSABLE_OPEN_SET.isEmpty()) {
            PathNode current = REUSABLE_OPEN_SET.poll();
            
            if (current.navNode == target) {
                List<NavNode> result = reconstructPath(current);
                // Return nodes to pool AFTER path reconstruction
                returnNodesToPool();
                return result;
            }

            REUSABLE_CLOSED_SET.add(current.navNode);

            for (NavNode neighbor : current.navNode.neighbors) {
                if (REUSABLE_CLOSED_SET.contains(neighbor)) continue;

                float tentativeG = current.gCost + distance(current.navNode, neighbor);

                PathNode neighborNode = REUSABLE_NODES.get(neighbor);
                if (neighborNode == null) {
                    neighborNode = getPooledPathNode();
                    neighborNode.reset(neighbor);
                    REUSABLE_NODES.put(neighbor, neighborNode);
                }

                if (tentativeG < neighborNode.gCost || neighborNode.gCost == Float.MAX_VALUE) {
                    neighborNode.gCost = tentativeG;
                    neighborNode.hCost = heuristic(neighbor, target);
                    neighborNode.parent = current;

                    if (!REUSABLE_OPEN_SET.contains(neighborNode)) {
                        REUSABLE_OPEN_SET.add(neighborNode);
                    }
                }
            }
        }

        // No path found - return nodes to pool
        returnNodesToPool();
        return Collections.emptyList();
    }

    private static PathNode getPooledPathNode() {
        PathNode node = PATH_NODE_POOL.poll();
        if (node == null) {
            node = new PathNode(null);
        }
        return node;
    }

    private static void returnNodesToPool() {
        for (PathNode node : REUSABLE_NODES.values()) {
            if (PATH_NODE_POOL.size() < MAX_POOL_SIZE) {
                PATH_NODE_POOL.offer(node);
            }
        }
    }

    private static List<NavNode> reconstructPath(PathNode endNode) {
        REUSABLE_PATH.clear();
        PathNode current = endNode;
        while (current != null) {
            REUSABLE_PATH.add(current.navNode);
            current = current.parent;
        }
        Collections.reverse(REUSABLE_PATH);
        
        // Create a copy since we're reusing REUSABLE_PATH
        return new ArrayList<>(REUSABLE_PATH);
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
