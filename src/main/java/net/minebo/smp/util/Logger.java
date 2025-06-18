package net.minebo.smp.util;

import net.minebo.smp.kSMP;

import java.util.logging.Level;

public class Logger {
    public static void log(String msg) {
        kSMP.instance.getLogger().log(Level.INFO, msg);
    }
}
