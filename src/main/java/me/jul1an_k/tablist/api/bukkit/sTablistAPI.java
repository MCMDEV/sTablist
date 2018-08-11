package me.jul1an_k.tablist.api.bukkit;

import me.jul1an_k.tablist.bukkit.Tablist;
import me.jul1an_k.tablist.bukkit.api.impl.tablistapi.TablistAPI_1_13;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import me.jul1an_k.tablist.bukkit.api.impl.tablistapi.TablistAPI_Glowstone;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class sTablistAPI {

    private static sTablistAPI implemantation;

    public abstract void sendTabList(Player player, String header, String footer);

    public abstract void sendActionBar(Player player, String message);

    public abstract void sendTitle(Player player, String title, String subtitle, int fadein, int stay, int fadeout, boolean clear, boolean reset);

    public abstract int getPing(Player player);

    public abstract String getVersion();

    public static sTablistAPI getImpl() {
        return implemantation;
    }

    public static void setupTablistAPI() {
        try {
            Class.forName("net.glowstone.entity.GlowPlayer");

            implemantation = new TablistAPI_Glowstone();

            System.out.println("[sTablistAPI] TablistAPI Implementation set to Glowstone. (1.12.2)");

            return;
        } catch(ClassNotFoundException ignored) {}

        try {
            Class.forName("org.bukkit.craftbukkit.v1_13_R1.entity.CraftPlayer");

            implemantation = new TablistAPI_1_13();

            System.out.println("[sTablistAPI] TablistAPI Implementation set to 1.13.");

            return;
        } catch(ClassNotFoundException ignored) {}

        System.err.println("[sTablistAPI] Found no compatible TablistAPI Implementation. Disabling plugin...");
        Bukkit.getPluginManager().disablePlugin(JavaPlugin.getPlugin(Tablist.class));
    }

    protected boolean compareMinecraftVersionServerIsHigherOrEqual(String version) {
        String serverVersion = Bukkit.getVersion();
        serverVersion = serverVersion.substring(serverVersion.indexOf("(MC: ") + 5, serverVersion.length());
        serverVersion = serverVersion.substring(0, serverVersion.lastIndexOf(")"));
        String[] serverVersionArray = serverVersion.split("\\.");
        String[] toCompareVersionArray = version.split("\\.");

        if(serverVersionArray.length == 2) {
            int serverFirst = Integer.valueOf(serverVersionArray[0]);
            int toCompareFirst = Integer.valueOf(toCompareVersionArray[0]);
            if(toCompareFirst != serverFirst) {
                return toCompareFirst < serverFirst;
            }
            int serverSecond = Integer.valueOf(serverVersionArray[1]);
            int toCompareSecond = Integer.valueOf(toCompareVersionArray[1]);
            if(toCompareSecond != serverSecond) {
                return toCompareSecond < serverSecond;
            }
            return toCompareVersionArray.length != 3;
        }
        if(serverVersionArray.length == 3) {
            int serverFirst = Integer.valueOf(serverVersionArray[0]);
            int toCompareFirst = Integer.valueOf(toCompareVersionArray[0]);
            if(toCompareFirst != serverFirst) {
                return toCompareFirst < serverFirst;
            }
            int serverSecond = Integer.valueOf(serverVersionArray[1]);
            int toCompareSecond = Integer.valueOf(toCompareVersionArray[1]);
            if(toCompareSecond != serverSecond) {
                return toCompareSecond < serverSecond;
            }
            if(toCompareVersionArray.length != 3) {
                return true;
            }
            int serverThird = Integer.valueOf(serverVersionArray[2]);
            int toCompareThird = Integer.valueOf(toCompareVersionArray[2]);
            if(toCompareThird != serverThird) {
                return toCompareThird < serverThird;
            }
            return true;
        }
        return false;
    }

}
