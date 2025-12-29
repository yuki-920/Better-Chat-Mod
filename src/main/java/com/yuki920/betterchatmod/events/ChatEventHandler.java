package com.yuki920.betterchatmod.events;

import com.yuki920.betterchatmod.client.gui.AnimatedChatLine;
import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.util.List;

public class ChatEventHandler {

    private String lastMessage = "";
    private int duplicateCount = 1;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if (Config.duplicateMessagesEnabled && message.equals(lastMessage)) {
            duplicateCount++;
            try {
                GuiNewChat chatGUI = Minecraft.getMinecraft().ingameGUI.getChatGUI();
                List<ChatLine> chatLines = ReflectionHelper.getPrivateValue(GuiNewChat.class, chatGUI, "field_146252_h");

                if (!chatLines.isEmpty()) {
                    chatLines.set(0, new AnimatedChatLine(chatLines.get(0).getUpdatedCounter(), new ChatComponentText(message + " [x" + duplicateCount + "]"), chatLines.get(0).getChatLineID()));
                    chatGUI.refreshChat();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            event.setCanceled(true);
        } else {
            duplicateCount = 1;
            lastMessage = message;
        }

        if (Config.mentionSoundEnabled && message.toLowerCase().contains(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) {
            Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0F, 1.0F);
        }
    }
}
