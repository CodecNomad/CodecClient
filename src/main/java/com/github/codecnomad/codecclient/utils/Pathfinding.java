package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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

                        if (isAllowed(Client.mc.theWorld.getBlockState(neighborPos).getBlock()) || !isAllowed(Client.mc.theWorld.getBlockState(neighborPos.add(0, 1, 0)).getBlock()) || !isAllowed(Client.mc.theWorld.getBlockState(neighborPos.add(0, 2, 0)).getBlock())) {
                            closedSet.add(neighbor);
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

    private boolean isAllowed(Block block) {
        final List<Class<? extends Block>> allowedBlocks = new ArrayList<>();

        allowedBlocks.add(Blocks.grass.getClass());

        allowedBlocks.add(Blocks.air.getClass());
        allowedBlocks.add(Blocks.skull.getClass());
        allowedBlocks.add(Blocks.nether_wart.getClass());
        allowedBlocks.add(Blocks.wheat.getClass());
        allowedBlocks.add(Blocks.carrots.getClass());
        allowedBlocks.add(Blocks.water.getClass());
        allowedBlocks.add(Blocks.tallgrass.getClass());
        allowedBlocks.add(Blocks.double_plant.getClass());
        allowedBlocks.add(Blocks.yellow_flower.getClass());
        allowedBlocks.add(Blocks.red_flower.getClass());
        allowedBlocks.add(Blocks.vine.getClass());
        allowedBlocks.add(Blocks.redstone_wire.getClass());
        allowedBlocks.add(Blocks.snow_layer.getClass());
        allowedBlocks.add(Blocks.torch.getClass());
        allowedBlocks.add(Blocks.cocoa.getClass());
        allowedBlocks.add(Blocks.end_portal.getClass());
        allowedBlocks.add(Blocks.tripwire.getClass());
        allowedBlocks.add(Blocks.web.getClass());
        allowedBlocks.add(Blocks.flower_pot.getClass());
        allowedBlocks.add(Blocks.wooden_pressure_plate.getClass());
        allowedBlocks.add(Blocks.stone_pressure_plate.getClass());
        allowedBlocks.add(Blocks.redstone_torch.getClass());
        allowedBlocks.add(Blocks.lever.getClass());
        allowedBlocks.add(Blocks.stone_button.getClass());
        allowedBlocks.add(Blocks.wooden_button.getClass());
        allowedBlocks.add(Blocks.carpet.getClass());
        allowedBlocks.add(Blocks.standing_sign.getClass());
        allowedBlocks.add(Blocks.wall_sign.getClass());
        allowedBlocks.add(Blocks.rail.getClass());
        allowedBlocks.add(Blocks.detector_rail.getClass());
        allowedBlocks.add(Blocks.activator_rail.getClass());
        allowedBlocks.add(Blocks.golden_rail.getClass());
        allowedBlocks.add(Blocks.stone_stairs.getClass());
        allowedBlocks.add(Blocks.brick_stairs.getClass());
        allowedBlocks.add(Blocks.stone_slab.getClass());
        allowedBlocks.add(Blocks.jungle_stairs.getClass());
        allowedBlocks.add(Blocks.wooden_slab.getClass());
        allowedBlocks.add(Blocks.wooden_slab.getClass());

        for (Class<? extends Block> allowedBlockClass : allowedBlocks) {
            if (allowedBlockClass.isInstance(block)) {
                return true;
            }
        }
        return false;
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
