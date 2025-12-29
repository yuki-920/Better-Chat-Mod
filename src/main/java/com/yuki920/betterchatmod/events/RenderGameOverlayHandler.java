package com.yuki920.betterchatmod.events;

import com.yuki920.betterchatmod.client.gui.CustomGuiNewChat;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngame;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.Field;

public class RenderGameOverlayHandler {

    private final CustomGuiNewChat customGuiNewChat;

    public RenderGameOverlayHandler(Minecraft mc) {
        this.customGuiNewChat = new CustomGuiNewChat(mc);
    }

    @SubscribeEvent
    public void onRenderGameOverlay(RenderGameOverlayEvent.Pre event) {
        if (event.type == RenderGameOverlayEvent.ElementType.CHAT) {
            try {
                Field field = ReflectionHelper.findField(GuiIngame.class, "persistantChatGUI", "field_73840_e");
                field.setAccessible(true);
                field.set(Minecraft.getMinecraft().ingameGUI, customGuiNewChat);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
