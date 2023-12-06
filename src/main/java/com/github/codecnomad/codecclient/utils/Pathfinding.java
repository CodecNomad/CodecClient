package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

class Node {
    public Node parent;
    public double f;
    public double g;
    public BlockPos p;

    public Node(BlockPos p, double f) {
        this.p = p;
        this.f = f;
    }
}

public class Pathfinding {
    private final Collection<Node> O = new ArrayList<>();
    private final Collection<Node> C = new ArrayList<>();

    public List<BlockPos> createPath(BlockPos s, BlockPos t) {
        double l = s.distanceSq(t);
        O.add(new Node(s, l));

        for (int i = 0; i < l*2 && !O.isEmpty(); i++) {
            Node n = null;
            double b = Double.MAX_VALUE;
            for (Node n1 : O) {
                if (n1.f < b) {
                    n = n1;
                    b = n1.f;
                }
            }

            if (Objects.requireNonNull(n).p.equals(t)) {
                List<BlockPos> p = new ArrayList<>();
                while (n != null) {
                    p.add(0, n.p);
                    n = n.parent;
                }

                return p;
            }

            for (int x = -1; x <= 1; x++) {
                for (int z = -1; z <= 1; z++) {
                    for (int y = -1; y <= 1; y++) {
                        BlockPos mp = new BlockPos(n.p.add(x, y, z));
                        Node m = new Node(mp, mp.distanceSq(t));

                        if (Client.mc.theWorld.rayTraceBlocks(new Vec3(mp.getX(), mp.getY(), mp.getZ()), new Vec3(n.p.getX(), n.p.getY(), n.p.getZ())) != null) {
                            continue;
                        }

                        if (Client.mc.theWorld.getBlockState(m.p).getBlock() != Blocks.air || (Client.mc.theWorld.getBlockState(m.p).getBlock() == Blocks.air && Client.mc.theWorld.getBlockState(m.p.add(0, -1, 0)).getBlock() == Blocks.air)) {
                            continue;
                        }

                        if (!contains(m, O) && !contains(m, C)) {
                            m.g = s.distanceSq(m.p);
                            m.parent = n;
                            O.add(m);
                        } else {
                            m.g = n.p.distanceSq(m.p);
                            if (m.f < m.g) {
                                m.parent = n;
                                if (contains(m, C)) {
                                    C.remove(m);
                                    O.add(m);
                                }
                            }
                        }
                    }
                }
            }
            O.remove(n);
            C.add(n);
        }

        return null;
    }

    private boolean contains(Node node, Collection<Node> collection) {
        for (Node n : collection) {
            if (n.p.equals(node.p)) {
                return true;
            }
        }
        return false;
    }
}
