package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;
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

    @SuppressWarnings("EmptyFinallyBlock")
    public List<BlockPos> createPath(BlockPos s, BlockPos t) {
        open.clear();
        closed.clear();

        Node start = new Node(s);
        Node target = new Node(t);

        start.gCost = 0;
        start.hCost = start.distanceTo(target);

        open.add(start);

        long startTime = System.currentTimeMillis();
        while (!open.isEmpty() && (System.currentTimeMillis() - startTime) < 100) {
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

                if (!neighbourNode.getBlockMaterial().blocksMovement()) {
                    continue;
                }

                Node node1 = new Node(neighbourNode.position.add(0, 1, 0));
                Node node2 = new Node(neighbourNode.position.add(0, 2, 0));
                Node node3 = new Node(neighbourNode.position.add(0, 3, 0));
                Node node4 = new Node(neighbourNode.position.add(0, 4, 0));

                AxisAlignedBB node1BB;
                AxisAlignedBB node2BB;
                AxisAlignedBB node3BB;
                AxisAlignedBB node4BB;

                try {
                    node1BB = node1.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node1.getBlockState());
                } finally {}
                try {
                    node2BB = node2.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node2.getBlockState());
                } finally {}
                try {
                    node3BB = node3.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node3.getBlockState());
                } finally {}
                try {
                    node4BB = node4.getBlockState().getBlock().getCollisionBoundingBox(Client.mc.theWorld, node1.position, node4.getBlockState());
                } finally {}

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

                if (node4BB != null) {
                    allBB += (node4BB.maxY - node4BB.minY);
                }

                if (allBB > 0.4) {
                    continue;
                }

                if (Client.mc.theWorld.rayTraceBlocks(
                        new Vec3(currentNode.position.getX(), currentNode.position.getY() + 1, currentNode.position.getZ()),
                        new Vec3(neighbourNode.position.getX(), neighbourNode.position.getY() + 1, neighbourNode.position.getZ())) != null ||
                        Client.mc.theWorld.rayTraceBlocks(
                                new Vec3(currentNode.position.getX(), currentNode.position.getY() + 2, currentNode.position.getZ()),
                                new Vec3(neighbourNode.position.getX(), neighbourNode.position.getY() + 2, neighbourNode.position.getZ())) != null ||
                        Client.mc.theWorld.rayTraceBlocks(
                                new Vec3(currentNode.position.getX(), currentNode.position.getY() + 3, currentNode.position.getZ()),
                                new Vec3(neighbourNode.position.getX(), neighbourNode.position.getY() + 3, neighbourNode.position.getZ())) != null
                ) {
                    continue;
                }

                double tentativeGCost = currentNode.gCost + currentNode.distanceTo(neighbourNode);

                if (!neighbourNode.isIn(open) || tentativeGCost < neighbourNode.gCost) {
                    neighbourNode.parent = currentNode;
                    neighbourNode.gCost = tentativeGCost;
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

            for (int x = -1; x <= 1; x++) {
                for (int y = -1; y <= 1; y++) {
                    for (int z = -1; z <= 1; z++) {
                        neighbourPositions.add(this.position.add(x, y, z));
                    }
                }
            }

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
            for (Node node : set) {
                if (node.position.equals(this.position)) {
                    return true;
                }
            }
            return false;
        }

        IBlockState getBlockState() {
            return Client.mc.theWorld.getBlockState(this.position);
        }

        Material getBlockMaterial() {
            return getBlockState().getBlock().getMaterial();
        }
    }
}