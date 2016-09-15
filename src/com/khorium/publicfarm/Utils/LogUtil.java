package com.khorium.publicfarm.Utils;

import com.khorium.publicfarm.PublicFarm;

/**
 *
 * Created by Khorium on 2016/8/9.
 */
public class LogUtil {

    private static boolean debug = false;

    private static PublicFarm publicFarm;

    static {
        publicFarm = PublicFarm.instance;
    }

    public static void logInfo(String info) {
        if (debug && publicFarm!=null) {
            publicFarm.getLogger().info(info);
        }
    }

    public static void sendError(String info) {
        if (publicFarm!=null) {
            publicFarm.getLogger().info(info);
        }
    }


}
