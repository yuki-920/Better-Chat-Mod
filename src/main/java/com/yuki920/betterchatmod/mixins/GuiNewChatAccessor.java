package com.yuki920.betterchatmod.mixins;

import net.minecraft.client.gui.ChatLine;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(net.minecraft.client.gui.GuiNewChat.class)
public interface GuiNewChatAccessor {
    
    @Accessor("chatLines")
    List<ChatLine> getChatLines();
    
    @Accessor("drawnChatLines")
    List<ChatLine> getDrawnChatLines();
}
