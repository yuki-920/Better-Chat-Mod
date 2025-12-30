package com.yuki920.betterchatmod.client.gui;

import com.yuki920.betterchatmod.BetterChatMod;
import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.config.GuiConfig;

public class ModGuiConfig extends GuiConfig {
    public ModGuiConfig(GuiScreen parentScreen) {
        super(parentScreen,
                new ConfigElement(Config.getConfig().getCategory(Configuration.CATEGORY_GENERAL)).getChildElements(),
                BetterChatMod.MODID,
                false,
                false,
                GuiConfig.getAbridgedConfigPath(Config.getConfig().toString()));
    }
}
