package capstone.main.Pathfinding;

public class PathNode {
    public NavNode navNode;
    public PathNode parent;
    public float gCost = Float.MAX_VALUE; // cost from start
    public float hCost; // heuristic to target

    public PathNode(NavNode navNode) {
        this.navNode = navNode;
    }

    // Reset method for object pooling
    public void reset(NavNode navNode) {
        this.navNode = navNode;
        this.parent = null;
        this.gCost = Float.MAX_VALUE;
        this.hCost = 0;
    }

    public float fCost() {
        return gCost + hCost;
    }
}
