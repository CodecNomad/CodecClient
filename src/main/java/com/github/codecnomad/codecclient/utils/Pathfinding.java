package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object other) {
        return this.pos.equals(((Node) other).pos);
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

        long startTime = System.currentTimeMillis();
        while (!openSet.isEmpty() && System.currentTimeMillis() - startTime < 100) {
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
                    for (int y = -2; y <= 2; y++) {
                        if (x == 0 && y == 0 && z == 0) {
                            continue;
                        }

                        BlockPos neighborPos = new BlockPos(currentNode.pos.add(x, y, z));
                        Node neighbor = new Node(neighborPos);

                        if (canWalkTrough(neighborPos)) {
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

    private boolean canWalkTrough(BlockPos pos) {
        IBlockState blockState = Client.mc.theWorld.getBlockState(pos);
        AxisAlignedBB blockAABB = blockState.getBlock().getCollisionBoundingBox(Client.mc.theWorld, pos, blockState);
        if (blockAABB == null) {
            return true;
        }

        return blockAABB.maxY - blockAABB.minY < 0.625;
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