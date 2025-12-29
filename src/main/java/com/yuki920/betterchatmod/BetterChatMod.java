package com.yuki920.betterchatmod;

import com.yuki920.betterchatmod.commands.BetterChatCommand;
import com.yuki920.betterchatmod.events.ChatEventHandler;
import com.yuki920.betterchatmod.events.RenderGameOverlayHandler;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.ClientCommandHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;

@Mod(modid = "betterchatmod", version = "1.0")
public class BetterChatMod {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        com.yuki920.betterchatmod.config.Config.init(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        ClientCommandHandler.instance.registerCommand(new BetterChatCommand());
        MinecraftForge.EVENT_BUS.register(new ChatEventHandler());
        MinecraftForge.EVENT_BUS.register(new RenderGameOverlayHandler(Minecraft.getMinecraft()));
    }
}
