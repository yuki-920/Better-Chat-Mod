package com.yuki920.betterchatmod;

import net.minecraftforge.common.config.Configuration;
import java.io.File;

public class ChatConfig {
    public static int compactChatTime = 60;
    public static boolean consecutiveCompactChat = false;
    public static boolean playSoundOnMention = true;
    public static String mentionSound = "random.orb";
    public static float mentionVolume = 1.0F;
    public static float mentionPitch = 1.0F;
    
    private Configuration config;

    public ChatConfig(File configFile) {
        config = new Configuration(configFile);
        load();
    }

    public void load() {
        config.load();
        
        compactChatTime = config.getInt("compactChatTime", "compact", 60, 1, 300,
            "Time in seconds to compact duplicate messages");
        consecutiveCompactChat = config.getBoolean("consecutiveCompactChat", "compact", false,
            "Only compact consecutive messages");
        
        playSoundOnMention = config.getBoolean("playSoundOnMention", "mention", true,
            "Play sound when your name is mentioned in chat");
        mentionSound = config.getString("mentionSound", "mention", "random.orb",
            "Sound to play when mentioned (e.g. random.orb, random.levelup)");
        mentionVolume = config.getFloat("mentionVolume", "mention", 1.0F, 0.0F, 1.0F,
            "Volume of the mention sound");
        mentionPitch = config.getFloat("mentionPitch", "mention", 1.0F, 0.5F, 2.0F,
            "Pitch of the mention sound");
        
        if (config.hasChanged()) {
            config.save();
        }
    }
}
