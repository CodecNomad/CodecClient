package com.github.codecnomad.codecclient.utils;

import com.github.codecnomad.codecclient.Client;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class Node {
    public Node parent;
    public double f;
    public double g;
    public BlockPos p;

    public Node(BlockPos p) {
        this.p = p;
    }
}

public class Pathfinding {
    Collection<Node> O;
    Collection<Node> C;

    public Pathfinding() {
        O = new ArrayList<>();
        C = new ArrayList<>();
    }

    public List<BlockPos> getPath(BlockPos s, BlockPos t) {
        C.clear();
        O.clear();
        O.add(new Node(s));

        int upperBound = (int) (s.distanceSq(t) * 2);
        int i = 0;
        while (!O.isEmpty()) {
            if (i > upperBound) {
                return new ArrayList<>();
            }
            i++;

            Node n = null;
            double bestF = Double.MAX_VALUE;
            for (Node node : O) {
                if (node.f < bestF) {
                    n = node;
                    bestF = node.f;
                }
            }

            O.remove(n);
            C.add(n);

            assert n != null;
            if (n.p.equals(t)) {
                List<BlockPos> path = new ArrayList<>();
                while (n != null) {
                    path.add(0, n.p);
                    n = n.parent;
                }
                return path;
            }

            List<Node> nb = new ArrayList<>();
            nb.add(new Node(n.p.add(0, 0, 1)));
            nb.add(new Node(n.p.add(0, 0, -1)));
            nb.add(new Node(n.p.add(1, 0, 0)));
            nb.add(new Node(n.p.add(-1, 0, 0)));

            nb.add(new Node(n.p.add(0, -1, 0)));
            nb.add(new Node(n.p.add(0, 1, 0)));


            for (Node m : nb) {
                if (Client.mc.theWorld.getBlockState(m.p).getBlock() != Blocks.air) {
                    continue;
                }

                if (!isNotInCollection(new Node(m.p.add(0, 1, 0)), O)) {
                    continue;
                }

                if (isNotInCollection(m, C)) {
                    m.g = n.g + 1;
                    m.f = m.g + m.p.distanceSq(t);
                    m.parent = n;
                    if (isNotInCollection(m, O)) {
                            O.add(m);
                    } else {
                        Node existingNode = getNodeFromCollection(m, O);
                        assert existingNode != null;
                        if (existingNode.f > m.f) {
                            existingNode.parent = n;
                            existingNode.g = m.g;
                            existingNode.f = m.f;
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean isNotInCollection(Node node, Collection<Node> collection) {
        for (Node n : collection) {
            if (n.p.equals(node.p)) {
                return false;
            }
        }
        return true;
    }

    private Node getNodeFromCollection(Node node, Collection<Node> collection) {
        for (Node n : collection) {
            if (n.p.equals(node.p)) {
                return n;
            }
        }
        return null;
    }
}
