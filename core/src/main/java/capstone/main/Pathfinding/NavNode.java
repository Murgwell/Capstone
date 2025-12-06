package capstone.main.Pathfinding;

import com.badlogic.gdx.math.Vector2;

public class NavNode {
    public int x, y;
    public boolean walkable;
    public NavNode[] neighbors;
    public final Vector2 worldPos; // precomputed world position

    public NavNode(int x, int y, boolean walkable, float nodeSize) {
        this.x = x;
        this.y = y;
        this.walkable = walkable;
        this.neighbors = new NavNode[0];
        this.worldPos = new Vector2(x * nodeSize + nodeSize / 2f, y * nodeSize + nodeSize / 2f);
    }
}
