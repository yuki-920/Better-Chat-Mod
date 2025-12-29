package com.yuki920.betterchatmod.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class Config {

    private static Configuration config;

    public static boolean animationEnabled = true;
    public static int animationSpeed = 100;
    public static boolean backgroundEnabled = false;
    public static int backgroundAlpha = 100;
    public static boolean duplicateMessagesEnabled = true;
    public static boolean mentionSoundEnabled = true;

    public static void init(File configFile) {
        config = new Configuration(configFile);
        config.load();
        load();
    }

    public static void load() {
        animationEnabled = config.getBoolean("Animation Enabled", "General", animationEnabled, "Enable or disable chat animations.");
        animationSpeed = config.getInt("Animation Speed", "General", animationSpeed, 0, 500, "Set the chat animation speed in milliseconds.");
        backgroundEnabled = config.getBoolean("Background Enabled", "General", backgroundEnabled, "Enable or disable the transparent chat background.");
        backgroundAlpha = config.getInt("Background Alpha", "General", backgroundAlpha, 0, 100, "Set the chat background alpha (0-100).");
        duplicateMessagesEnabled = config.getBoolean("Duplicate Messages Enabled", "General", duplicateMessagesEnabled, "Enable or disable duplicate message stacking.");
        mentionSoundEnabled = config.getBoolean("Mention Sound Enabled", "General", mentionSoundEnabled, "Enable or disable the mention sound.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        config.get("General", "Animation Enabled", animationEnabled).set(animationEnabled);
        config.get("General", "Animation Speed", animationSpeed).set(animationSpeed);
        config.get("General", "Background Enabled", backgroundEnabled).set(backgroundEnabled);
        config.get("General", "Background Alpha", backgroundAlpha).set(backgroundAlpha);
        config.get("General", "Duplicate Messages Enabled", duplicateMessagesEnabled).set(duplicateMessagesEnabled);
        config.get("General", "Mention Sound Enabled", mentionSoundEnabled).set(mentionSoundEnabled);
        config.save();
    }
}
