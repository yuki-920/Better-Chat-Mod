package com.yuki920.betterchatmod.mixins;

import com.yuki920.betterchatmod.ChatHandler;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiNewChat.class)
public class MixinGuiNewChat {
    
    @Inject(method = "printChatMessageWithOptionalDeletion", at = @At("HEAD"))
    private void onPrintChatMessage(IChatComponent chatComponent, int chatLineId, CallbackInfo ci) {
        ChatHandler.appendMessageCounter(chatComponent, false);
    }
    
    @Inject(method = "setChatLine", at = @At("HEAD"))
    private void onSetChatLine(ChatLine line, int lineId, int updateCounter, boolean refresh, CallbackInfo ci) {
        ChatHandler.setChatLine_addToList(line);
    }
    
    @Inject(method = "setChatLine", at = @At("RETURN"))
    private void afterSetChatLine(ChatLine line, int lineId, int updateCounter, boolean refresh, CallbackInfo ci) {
        ChatHandler.resetMessageHash();
    }
}
