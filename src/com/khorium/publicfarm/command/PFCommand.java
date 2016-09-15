package com.khorium.publicfarm.command;

/**
 * commands for switch
 *
 * Created by Khorium on 2016/8/8.
 */
public enum PFCommand {
    SET,LIST,DELETE,RELOAD,NOVALUE;

    public static PFCommand getCommand(String command) {
        try {
            return valueOf(command.toUpperCase());
        } catch (Exception e) {
            return NOVALUE;
        }
    }
}
