package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.List;
import java.util.*;

public class Pathfinding {
    PriorityQueue<Node> open = new PriorityQueue<>(Comparator.comparingDouble(Node::getF));
    Set<Node> closed = new HashSet<>();

    @SubscribeEvent
    public void lastWorld(RenderWorldLastEvent event) {
        for (Node node : open) {
            Render.drawOutlinedFilledBoundingBox(node.position, Color.green, event.partialTicks);
        }

        for (Node node : closed) {
            Render.drawOutlinedFilledBoundingBox(node.position, Color.red, event.partialTicks);
        }
    }

    public List<BlockPos> createPath(BlockPos s, BlockPos t) {
        open.clear();
        closed.clear();

        Node start = new Node(s.add(0.5, 0.5, 0.5));
        Node target = new Node(t.add(0.5, 0.5, 0.5));

        start.gCost = 0;
        start.hCost = start.distanceTo(target);

        open.add(start);

        long startTime = System.currentTimeMillis();
        while (!open.isEmpty() && (System.currentTimeMillis() - startTime) < 5000) {
            Node currentNode = open.poll();

            if (currentNode == null) {
                break;
            }

            closed.add(currentNode);

            if (currentNode.equals(target)) {
                return reconstructPath(currentNode);
            }

            for (BlockPos neighbourPosition : currentNode.getNeighbourPositions()) {
                Node neighbourNode = new Node(neighbourPosition.add(0.5, 0.5, 0.5));

                if (neighbourNode.getBlockState() == null) {
                    return reconstructPath(currentNode);
                }

                if (!neighbourNode.getBlockMaterial().blocksMovement()) {
                    continue;
                }

                if (neighbourNode.isIn(closed)) {
                    continue;
                }

                Node node1 = new Node(neighbourNode.position.add(0, 1, 0).add(0.5, 0.5, 0.5));
                Node node2 = new Node(neighbourNode.position.add(0, 2, 0).add(0.5, 0.5, 0.5));
                Node node3 = new Node(neighbourNode.position.add(0, 3, 0).add(0.5, 0.5, 0.5));

                IBlockState node1BS = node1.getBlockState();
                IBlockState node2BS = node2.getBlockState();
                IBlockState node3BS = node3.getBlockState();

                AxisAlignedBB node1BB = null;
                AxisAlignedBB node2BB = null;
                AxisAlignedBB node3BB = null;

                try {
                    node1BB = node1BS.getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node1BS);
                } catch (Exception ignored) {}
                try {
                    node2BB = node2BS.getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node2BS);
                } catch (Exception ignored) {}
                try {
                    node3BB = node3BS.getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node3BS);
                } catch (Exception ignored) {}

                double allBB = 0;
                if (node1BB != null) {
                    allBB += (node1BB.maxY - node1BB.minY);
                }

                if (node2BB != null) {
                    allBB += (node2BB.maxY - node2BB.minY);
                }

                if (node3BB != null) {
                    allBB += (node3BB.maxY - node3BB.minY);
                }

                if (allBB > 0.2) {
                    continue;
                }

                double gCost = currentNode.distanceTo(neighbourNode);

                if (!neighbourNode.isIn(open) || gCost < neighbourNode.gCost) {
                    neighbourNode.parent = currentNode;
                    neighbourNode.gCost = gCost;
                    neighbourNode.hCost = neighbourNode.distanceTo(target);

                    open.add(neighbourNode);
                }
            }
        }
        return null;
    }

    private List<BlockPos> reconstructPath(Node currentNode) {
        List<BlockPos> path = new ArrayList<>();
        while (currentNode != null) {
            path.add(0, currentNode.position);
            currentNode = currentNode.parent;
        }
        return smoothPath(path);
    }

    private List<BlockPos> smoothPath(List<BlockPos> path) {
        List<BlockPos> smoothedPath = new ArrayList<>();
        if (path.isEmpty()) {
            return smoothedPath;
        }

        int k = 0;
        smoothedPath.add(path.get(0));

        for (int i = 1; i < path.size() - 1; i++) {
            if (!canSee(smoothedPath.get(k), path.get(i + 1))) {
                k++;
                smoothedPath.add(smoothedPath.size(), path.get(i));
            }
        }

        smoothedPath.add(smoothedPath.size(), path.get(path.size() - 1));

        return smoothedPath;
    }


    boolean canSee(BlockPos start, BlockPos end) {
        return Client.mc.theWorld.rayTraceBlocks(Math.fromBlockPos(start.add(0, 1, 0)), Math.fromBlockPos(end.add(0, 1, 0)), false, true, false) == null && Client.mc.theWorld.rayTraceBlocks(Math.fromBlockPos(start.add(0, 2, 0)), Math.fromBlockPos(end.add(0, 2, 0)), false, true, false) == null;
    }

    private static class Node {
        BlockPos position;
        Node parent;
        double gCost;
        double hCost;

        Node(BlockPos pos) {
            position = pos;
        }

        double getF() {
            return gCost + hCost;
        }

        List<BlockPos> getNeighbourPositions() {
            List<BlockPos> neighbourPositions = new ArrayList<>();

            neighbourPositions.add(this.position.add(1, 0, 0));
            neighbourPositions.add(this.position.add(-1, 0, 0));
            neighbourPositions.add(this.position.add(0, 0, 1));
            neighbourPositions.add(this.position.add(0, 0, -1));

            neighbourPositions.add(this.position.add(1, -1, 0));
            neighbourPositions.add(this.position.add(-1, -1, 0));
            neighbourPositions.add(this.position.add(0, -1, 1));
            neighbourPositions.add(this.position.add(0, -1, -1));


            neighbourPositions.add(this.position.add(1, 1, 0));
            neighbourPositions.add(this.position.add(-1, 1, 0));
            neighbourPositions.add(this.position.add(0, 1, 1));
            neighbourPositions.add(this.position.add(0, 1, -1));

            return neighbourPositions;
        }

        double distanceTo(Node other) {
            return this.position.distanceSq(other.position);
        }

        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        @Override
        public boolean equals(Object other) {
            return this.position.equals(((Node) other).position);
        }

        boolean isIn(Set<Node> set) {
            return set.stream().anyMatch(node -> node.position.equals(this.position));
        }

        boolean isIn(PriorityQueue<Node> set) {
            return set.stream().anyMatch(node -> node.position.equals(this.position));
        }


        IBlockState getBlockState() {
            return Client.mc.theWorld.getBlockState(this.position);
        }

        Material getBlockMaterial() {
            return getBlockState().getBlock().getMaterial();
        }
    }
}