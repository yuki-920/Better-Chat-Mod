package com.yuki920.betterchatmod;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = BetterChatMod.MODID, version = BetterChatMod.VERSION, name = BetterChatMod.NAME)
public class BetterChatMod {
    public static final String MODID = "betterchatmod";
    public static final String NAME = "Better Chat Mod";
    public static final String VERSION = "1.0.0";

    @Mod.Instance(MODID)
    public static BetterChatMod instance;

    public static ChatConfig config;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        config = new ChatConfig(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ChatHandler());
    }
}
