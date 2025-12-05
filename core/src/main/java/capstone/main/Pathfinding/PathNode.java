package capstone.main.Pathfinding;

public class PathNode {
    public NavNode navNode;
    public PathNode parent;
    public float gCost; // cost from start
    public float hCost; // heuristic to target

    public PathNode(NavNode navNode) {
        this.navNode = navNode;
    }

    public float fCost() {
        return gCost + hCost;
    }
}
