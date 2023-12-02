package com.github.codecnomad.codecclient.command;

import cc.polyfrost.oneconfig.utils.commands.annotations.Command;
import cc.polyfrost.oneconfig.utils.commands.annotations.Main;
import com.github.codecnomad.codecclient.Client;

@SuppressWarnings("unused")
@Command(value = "codecclient", aliases = {"cc"})
public class MainCommand {
        @Main
        public void mainCommand() {
                Client.guiConfig.openGui();
        }
}
