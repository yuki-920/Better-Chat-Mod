package com.yuki920.betterchatmod.config;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class Config {

    private static Configuration config;

    public static boolean animationEnabled = true;
    public static int animationSpeed = 100;
    public static boolean backgroundEnabled = false;
    public static int backgroundOpacity = 0; // Renamed from backgroundAlpha, default changed to 0
    public static boolean duplicateMessagesEnabled = true;
    public static boolean mentionSoundEnabled = true;
    public static String mentionNickname = "";

    public static void init(File configFile) {
        config = new Configuration(configFile);
        config.load();
        load();
    }

    public static void load() {
        animationEnabled = config.getBoolean("Animation Enabled", "General", animationEnabled, "Enable or disable chat animations.");
        animationSpeed = config.getInt("Animation Speed", "General", animationSpeed, 0, 500, "Set the chat animation speed in milliseconds.");
        backgroundEnabled = config.getBoolean("Background Enabled", "General", backgroundEnabled, "Enable or disable the transparent chat background.");
        backgroundOpacity = config.getInt("Background Opacity", "General", backgroundOpacity, 0, 100, "Set the chat background opacity (0-100%).");
        duplicateMessagesEnabled = config.getBoolean("Duplicate Messages Enabled", "General", duplicateMessagesEnabled, "Enable or disable duplicate message stacking.");
        mentionSoundEnabled = config.getBoolean("Mention Sound Enabled", "General", mentionSoundEnabled, "Enable or disable the mention sound.");
        mentionNickname = config.getString("Mention Nickname", "General", mentionNickname, "The custom nickname to be used for mention sounds.");

        if (config.hasChanged()) {
            config.save();
        }
    }

    public static void save() {
        config.get("General", "Animation Enabled", animationEnabled).set(animationEnabled);
        config.get("General", "Animation Speed", animationSpeed).set(animationSpeed);
        config.get("General", "Background Enabled", backgroundEnabled).set(backgroundEnabled);
        config.get("General", "Background Opacity", backgroundOpacity).set(backgroundOpacity);
        config.get("General", "Duplicate Messages Enabled", duplicateMessagesEnabled).set(duplicateMessagesEnabled);
        config.get("General", "Mention Sound Enabled", mentionSoundEnabled).set(mentionSoundEnabled);
        config.get("General", "Mention Nickname", mentionNickname).set(mentionNickname);
        config.save();
    }
}
