package capstone.main.Pathfinding;

import java.util.*;

/**
 * Memory-efficient pathfinding cache for enemies
 * Prevents recalculating the same paths repeatedly
 */
public class PathfindingCache {
    private static final Map<String, CachedPath> pathCache = new HashMap<>();
    private static final long CACHE_DURATION = 2000; // 2 seconds in milliseconds
    private static final int MAX_CACHE_SIZE = 100;

    private static class CachedPath {
        final List<NavNode> path;
        final long timestamp;

        CachedPath(List<NavNode> path) {
            this.path = new ArrayList<>(path);
            this.timestamp = System.currentTimeMillis();
        }

        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_DURATION;
        }
    }

    public static List<NavNode> getCachedPath(NavMesh navMesh, NavNode start, NavNode target) {
        if (start == null || target == null) return Collections.emptyList();

        // Create cache key
        String key = start.x + "," + start.y + "->" + target.x + "," + target.y;

        // Check if we have a valid cached path
        CachedPath cached = pathCache.get(key);
        if (cached != null && cached.isValid()) {
            return new ArrayList<>(cached.path);
        }

        // Calculate new path
        List<NavNode> newPath = AStar.findPath(navMesh, start, target);

        // Cache the result (if cache isn't full)
        if (pathCache.size() < MAX_CACHE_SIZE) {
            pathCache.put(key, new CachedPath(newPath));
        } else {
            // Clean up old entries
            cleanupCache();
            pathCache.put(key, new CachedPath(newPath));
        }

        return newPath;
    }

    private static void cleanupCache() {
        Iterator<Map.Entry<String, CachedPath>> iterator = pathCache.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, CachedPath> entry = iterator.next();
            if (!entry.getValue().isValid()) {
                iterator.remove();
            }
        }
    }

    public static void clearCache() {
        pathCache.clear();
    }
}
