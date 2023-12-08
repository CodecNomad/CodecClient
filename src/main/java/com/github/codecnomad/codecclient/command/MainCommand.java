package com.github.codecnomad.codecclient.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand;
import com.github.codecnomad.codecclient.Client;
import com.github.codecnomad.codecclient.ui.Config;
import com.github.codecnomad.codecclient.utils.Chat;
import com.github.codecnomad.codecclient.utils.Pathfinding;
import com.github.codecnomad.codecclient.utils.Render;
import com.github.codecnomad.codecclient.utils.Walker;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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

        Collection<BlockPos> path = new ArrayList<>();
        public static Pathfinding pathfinding = new Pathfinding();

        @SubCommand
        public void add(int x, int y, int z) {
                MinecraftForge.EVENT_BUS.register(this);
                path = pathfinding.createPath(Client.mc.thePlayer.getPosition().add(0, -1, 0), new BlockPos(x, y - 1, z));
                if (path != null) {
                        Chat.sendMessage(String.format("Added waypoint: %d", waypoints.size()));
                        waypoints.clear();
                        waypoints.addAll(path);
                }
                else {
                        Chat.sendMessage("Failed to find path..");
                }
        }

        @SubscribeEvent
        public void renderWorld(RenderWorldLastEvent event) {
                if (path != null) {
                        for (BlockPos pos : path) {
                                Render.drawOutlinedFilledBoundingBox(pos.add(0, 1, 0), Config.VisualColor.toJavaColor(), event.partialTicks);
                        }
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