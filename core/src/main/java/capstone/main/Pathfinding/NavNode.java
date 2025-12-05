package capstone.main.Pathfinding;

public class NavNode {
    public int x, y; // grid coordinates
    public boolean walkable;
    public NavNode[] neighbors;

    public NavNode(int x, int y, boolean walkable) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
    }
}
