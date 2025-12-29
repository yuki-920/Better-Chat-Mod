package com.yuki920.betterchatmod.client.gui;

import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class CustomGuiNewChat extends GuiNewChat {

    public CustomGuiNewChat(Minecraft mcIn) {
        super(mcIn);
    }

    @Override
    public void drawChat(int updateCounter) {
        if (Config.backgroundEnabled) {
            GL11.glPushMatrix();
            GL11.glTranslatef(0.0F, 0.0F, -200.0F);
            Gui.drawRect(2, this.getChatHeight() - 2, this.getChatWidth() + 4, this.getChatHeight(), Config.backgroundAlpha << 24);
            GL11.glPopMatrix();
        }
        super.drawChat(updateCounter);
    }

    @Override
    public void printChatMessage(IChatComponent chatComponent) {
        super.printChatMessage(chatComponent);
        // This is where we would add a timestamp to the chat line for animation,
        // but for simplicity, we will just override drawChat for now.
        // A more complex animation would require more extensive modifications.
    }
}
