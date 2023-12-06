package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.*;

class Node {
    public Node parent;
    public double h;
    public double g;
    public BlockPos pos;

    public Node(BlockPos p) {
        this.pos = p;
    }

    public double getF() {
        return g + h;
    }
}

public class Pathfinding {
    public List<BlockPos> createPath(BlockPos s, BlockPos t) {
        final Set<Node> openSet = new HashSet<>();
        final Set<Node> closedSet = new HashSet<>();

        Node startNode = new Node(s);
        startNode.g = 0;
        startNode.h = s.distanceSq(t);
        openSet.add(startNode);

        for (int i = 0; !openSet.isEmpty() && i < s.distanceSq(t) * 2; i++) {
            Node currentNode = null;
            double lowestF = Double.MAX_VALUE;

            for (Node node : openSet) {
                double f = node.getF();
                if (f < lowestF) {
                    lowestF = f;
                    currentNode = node;
                }
            }

            if (currentNode == null) {
                return null;
            }

            if (currentNode.pos.equals(t)) {
                return reconstructPath(currentNode);
            }

            openSet.remove(currentNode);
            closedSet.add(currentNode);

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        BlockPos neighborPos = new BlockPos(currentNode.pos.add(x, y, z));
                        Node neighbor = new Node(neighborPos);

                        if (!Client.mc.theWorld.getBlockState(neighborPos).getBlock().isFullBlock()) {
                            continue;
                        }

                        if (Client.mc.theWorld.rayTraceBlocks(new Vec3(currentNode.pos.getX(), currentNode.pos.getY() + 1, currentNode.pos.getZ()), new Vec3(neighborPos.getX(), neighborPos.getY() + 1, neighborPos.getZ())) != null) {
                            continue;
                        }

                        if (Client.mc.theWorld.getBlockState(neighborPos.add(0, 1, 0)).getBlock().isFullBlock()) {
                            continue;
                        }

                        if (Client.mc.theWorld.getBlockState(neighborPos.add(0, 2, 0)).getBlock().isFullBlock()) {
                            continue;
                        }

                        double tentativeGScore = currentNode.g + currentNode.pos.distanceSq(neighborPos);

                        if (closedSet.contains(neighbor)) {
                            continue;
                        }

                        if (!openSet.contains(neighbor) || tentativeGScore < neighbor.g) {
                            neighbor.parent = currentNode;
                            neighbor.g = tentativeGScore;
                            neighbor.h = neighborPos.distanceSq(t);

                            openSet.add(neighbor);
                        }
                    }
                }
            }
        }

        return null;
    }

    private List<BlockPos> reconstructPath(Node currentNode) {
        List<BlockPos> path = new ArrayList<>();
        while (currentNode != null) {
            path.add(0, currentNode.pos);
            currentNode = currentNode.parent;
        }
        return path;
    }

}
