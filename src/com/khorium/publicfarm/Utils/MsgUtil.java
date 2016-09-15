package com.khorium.publicfarm.Utils;

import com.khorium.publicfarm.PublicFarm;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;

/**
 *
 * Created by Khorium on 2016/8/8.
 */
public class MsgUtil {
    private static YamlConfiguration messages;

    public static void loadCfgMessages(PublicFarm publicForm) {
        File messageFile = new File(publicForm.getDataFolder(), "messages.yml");
        if (!messageFile.exists()) {
            publicForm.saveResource("messages.yml", true);
        }
        // Store it
        messages = YamlConfiguration.loadConfiguration(messageFile);
        messages.options().copyDefaults(true);
        // Load default messages
        InputStream defMessageStream = publicForm.getResource("messages.yml");
        YamlConfiguration defMessages = YamlConfiguration.loadConfiguration(defMessageStream);
        messages.setDefaults(defMessages);
    }

    public static String getMessage(String loc, String... strings) {
        String raw = messages.getString(loc);
        if (raw == null || raw.isEmpty()) {
            return "Invalid message: " + loc;
        }
        if (strings == null) {
            return raw;
        }
        for (int i = 0; i < strings.length; i++) {
            raw = raw.replace("{" + i + "}", strings[i]==null ? "null" : strings[i]);
        }
        return raw;
    }
}
