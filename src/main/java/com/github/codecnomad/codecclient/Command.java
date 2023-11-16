package com.github.codecnomad.codecclient;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;

import java.util.Collections;
import java.util.List;

public class Command extends CommandBase {
    @Override
    public String getCommandName() {
        return "codecclient";
    }

    @Override
    public List<String> getCommandAliases() {
        return Collections.singletonList("cc");
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return null;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Config.state = true;
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }
}
