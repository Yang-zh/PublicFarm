package com.khorium.publicfarm.Utils;

import org.bukkit.Material;

/**
 * Created by Khorium on 2016/8/9.
 */
public class Utils {
    public static boolean isCrop(Material type) {
        switch (type) {
            case CROPS:
            case CARROT:
            case NETHER_WARTS:
            case POTATO:
            case WHEAT:
                return true;
            default:
                return false;
        }
    }

    public static int random(int i) {
        return (int) (Math.random() * i) + 1;
    }
}
