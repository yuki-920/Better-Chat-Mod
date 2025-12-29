package com.yuki920.betterchatmod.events;

import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatEventHandler {

    private String lastMessage = "";
    private int duplicateCount = 1;

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String message = event.message.getUnformattedText();

        if (Config.duplicateMessagesEnabled && message.equals(lastMessage)) {
            duplicateCount++;
            Minecraft.getMinecraft().ingameGUI.getChatGUI().deleteChatLine(1);
            event.message = new ChatComponentText(message + " [x" + duplicateCount + "]");
        } else {
            duplicateCount = 1;
            lastMessage = message;
        }

        if (Config.mentionSoundEnabled && message.toLowerCase().contains(Minecraft.getMinecraft().thePlayer.getName().toLowerCase())) {
            Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0F, 1.0F);
        }
    }
}
