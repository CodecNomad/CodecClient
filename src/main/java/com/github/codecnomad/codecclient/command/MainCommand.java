package com.github.codecnomad.codecclient.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.utils.Chat;
import com.github.codecnomad.codecclient.utils.Pathfinding;
import com.github.codecnomad.codecclient.utils.Walker;
import net.minecraft.util.BlockPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@SuppressWarnings("unused")
@Command(value = "codecclient", aliases = {"codec"})
public class MainCommand {
        List<BlockPos> waypoints = new ArrayList<>();
        @Main
        public void mainCommand() {
                Client.guiConfig.openGui();
        }

        @SubCommand
        public void add(int x, int y, int z) {
                Collection<BlockPos> path = new Pathfinding().createPath(Client.mc.thePlayer.getPosition().add(0, -1, 0), new BlockPos(x, y - 1, z));
                if (path != null) {
                        Chat.sendMessage(String.format("Added waypoint: %d", waypoints.size()));
                        waypoints.clear();
                        waypoints.addAll(path);
                }
                else {
                        Chat.sendMessage("Failed to find path..");
                }
        }

        @SubCommand
        public void clear() {
                waypoints.clear();
        }

        @SubCommand
        public void start() {
                Chat.sendMessage("STARTED!!");
                new Walker(waypoints, () -> Chat.sendMessage("Walked it!!")).start();
        }
}
