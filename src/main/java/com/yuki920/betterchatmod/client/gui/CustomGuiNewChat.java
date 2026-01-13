package com.yuki920.betterchatmod.client.gui;

import com.google.common.collect.Lists;
import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.IChatComponent;

import java.util.List;

public class CustomGuiNewChat extends GuiNewChat {

    private final List<Long> messageTimestamps = Lists.newArrayList();

    public CustomGuiNewChat(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void drawChat(int updateCounter) {
        if (Config.backgroundEnabled) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0F, 0.0F, -200.0F);
            int alpha = (int) (Config.backgroundOpacity / 100.0F * 255.0F);
            Gui.drawRect(2, this.getChatHeight() - 2, this.getChatWidth() + 4, this.getChatHeight(), alpha << 24);
            GlStateManager.popMatrix();
        }

        if (Config.animationEnabled && !messageTimestamps.isEmpty()) {
            long currentTime = System.currentTimeMillis();
            long lastMessageTime = messageTimestamps.get(0);
            long timeSinceLastMessage = currentTime - lastMessageTime;

            if (timeSinceLastMessage < Config.animationSpeed) {
                float animationProgress = (float) timeSinceLastMessage / (float) Config.animationSpeed;
                float slideOffset = (1.0F - animationProgress) * (float) Minecraft.getMinecraft().fontRendererObj.FONT_HEIGHT;

                GlStateManager.pushMatrix();
                GlStateManager.translate(0.0F, slideOffset, 0.0F);
                super.drawChat(updateCounter);
                GlStateManager.popMatrix();

                // Clean up old timestamps
                if (messageTimestamps.size() > 50) {
                    messageTimestamps.remove(messageTimestamps.size() - 1);
                }
                return;
            }
        }

        super.drawChat(updateCounter);
    }

    @Override
    public void printChatMessage(IChatComponent chatComponent) {
        super.printChatMessage(chatComponent);
        messageTimestamps.add(0, System.currentTimeMillis());
    }
}
