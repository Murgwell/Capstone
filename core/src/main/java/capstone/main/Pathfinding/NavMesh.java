package capstone.main.Pathfinding;

import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.List;

public class NavMesh {

    private final int width, height; // grid size in tiles
    private final float nodeSize;    // size of each node in world units (1 tile = 1 unit)
    private NavNode[][] nodes;

    public NavMesh(int tilesWide, int tilesHigh, ArrayList<Rectangle> obstacles) {
        this.nodeSize = 1.0f;  // each node is 1x1 world unit
        this.width = tilesWide;
        this.height = tilesHigh;
        nodes = new NavNode[width][height];

        // Generate nodes
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                boolean walkable = true;
                for (Rectangle r : obstacles) {
                    // check collision using world coords
                    if (r.contains(x + 0.5f, y + 0.5f)) {
                        walkable = false;
                        break;
                    }
                }
                nodes[x][y] = new NavNode(x, y, walkable);
            }
        }

        // Connect neighbors
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                ArrayList<NavNode> neighbors = new ArrayList<>();
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx == 0 && dy == 0) continue;
                        int nx = x + dx;
                        int ny = y + dy;
                        if (nx >= 0 && nx < width && ny >= 0 && ny < height && nodes[nx][ny].walkable) {
                            neighbors.add(nodes[nx][ny]);
                        }
                    }
                }
                nodes[x][y].neighbors = neighbors.toArray(new NavNode[0]);
            }
        }
    }

    public NavNode getNode(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return null;
        return nodes[x][y];
    }

    // Convert world position to node
    public NavNode getNodeByWorldPos(float worldX, float worldY) {
        int x = (int) Math.floor(worldX);
        int y = (int) Math.floor(worldY);
        return getNode(x, y);
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public float getNodeSize() { return nodeSize; }

    public List<NavNode> getNodes() {
        List<NavNode> allNodes = new ArrayList<>();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                if (nodes[i][j] != null) allNodes.add(nodes[i][j]);
            }
        }
        return allNodes;
    }

}
