package net.minebo.smp.classes;

import org.bukkit.Bukkit;

public class ClassThread extends Thread {

    public ClassThread() {
        super("Class Thread");
    }

    public void run() {
        while (true) {
            try {
                if (Bukkit.getOnlinePlayers().size() >= 1) {
                    ClassManager.runCheck();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                Thread.sleep(100);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
