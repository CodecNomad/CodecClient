package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Pathfinding {
    Set<Node> open = new HashSet<>();
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

        Node start = new Node(s);
        Node target = new Node(t);

        start.gCost = 0;
        start.hCost = start.distanceTo(target);

        open.add(start);

        long startTime = System.currentTimeMillis();
        while (!open.isEmpty() && (System.currentTimeMillis() - startTime) < 5000) {
            Node currentNode = null;
            double lowestF = Double.POSITIVE_INFINITY;

            for (Node node : open) {
                double fCost = node.getF();
                if (fCost < lowestF) {
                    lowestF = fCost;
                    currentNode = node;
                }
            }

            if (currentNode == null) {
                break;
            }

            open.remove(currentNode);
            closed.add(currentNode);

            if (currentNode.equals(target)) {
                return reconstructPath(currentNode);
            }

            for (BlockPos neighbourPosition : currentNode.getNeighbourPositions()) {
                Node neighbourNode = new Node(neighbourPosition);

                if (neighbourNode.isIn(closed)) {
                    continue;
                }

                if (neighbourNode.getBlockState() == null) {
                    return reconstructPath(currentNode);
                }

                if (!neighbourNode.getBlockMaterial().blocksMovement()) {
                    continue;
                }

                Node node1 = new Node(neighbourNode.position.add(0, 1, 0));
                Node node2 = new Node(neighbourNode.position.add(0, 2, 0));
                Node node3 = new Node(neighbourNode.position.add(0, 3, 0));

                AxisAlignedBB node1BB = null;
                AxisAlignedBB node2BB = null;
                AxisAlignedBB node3BB = null;

                try {
                    node1BB = node1.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node1.getBlockState());
                } catch (Exception ignored) {}
                try {
                    node2BB = node2.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node2.getBlockState());
                } catch (Exception ignored) {}
                try {
                    node3BB = node3.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node3.getBlockState());
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
        return path;
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


        IBlockState getBlockState() {
            return Client.mc.theWorld.getBlockState(this.position);
        }

        Material getBlockMaterial() {
            return getBlockState().getBlock().getMaterial();
        }
    }
}