package com.yuki920.betterchatmod.events;

import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.List;

public class ChatEventHandler {

    private IChatComponent lastMessageComponent;
    private String lastMessageUnformatted = "";
    private int duplicateCount = 1;

    private static Field drawnChatLinesField;
    private static Field chatLinesField;

    static {
        try {
            drawnChatLinesField = GuiNewChat.class.getDeclaredField("field_146253_i");
            drawnChatLinesField.setAccessible(true);
            chatLinesField = GuiNewChat.class.getDeclaredField("field_146252_h");
            chatLinesField.setAccessible(true);
        } catch (NoSuchFieldException e) {
            try {
                drawnChatLinesField = GuiNewChat.class.getDeclaredField("drawnChatLines");
                drawnChatLinesField.setAccessible(true);
                chatLinesField = GuiNewChat.class.getDeclaredField("chatLines");
                chatLinesField.setAccessible(true);
            } catch (NoSuchFieldException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void deleteLastChatLine() {
        GuiNewChat chatGUI = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        if (drawnChatLinesField == null || chatLinesField == null) return;

        try {
            @SuppressWarnings("unchecked")
            List<ChatLine> drawnChatLines = (List<ChatLine>) drawnChatLinesField.get(chatGUI);
            if (!drawnChatLines.isEmpty()) {
                drawnChatLines.remove(0);
            }

            @SuppressWarnings("unchecked")
            List<ChatLine> chatLines = (List<ChatLine>) chatLinesField.get(chatGUI);
            if (!chatLines.isEmpty()) {
                chatLines.remove(0);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        String messageUnformatted = event.message.getUnformattedText();

        if (messageUnformatted.isEmpty()) {
            lastMessageUnformatted = "";
            lastMessageComponent = null;
            duplicateCount = 1;
            return;
        }

        if (Config.duplicateMessagesEnabled && messageUnformatted.equals(lastMessageUnformatted)) {
            duplicateCount++;
            deleteLastChatLine();

            IChatComponent newMessage = this.lastMessageComponent.createCopy();
            newMessage.appendText(" [x" + duplicateCount + "]");
            event.message = newMessage;

        } else {
            duplicateCount = 1;
            this.lastMessageUnformatted = messageUnformatted;
            this.lastMessageComponent = event.message.createCopy();
        }

        if (Config.mentionSoundEnabled) {
            String messageText = event.message.getUnformattedText().toLowerCase();
            String playerName = Minecraft.getMinecraft().thePlayer.getName().toLowerCase();
            String realName = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();

            if (messageText.contains(playerName) || messageText.contains(realName)) {
                Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0F, 1.0F);
            }
        }
    }
}
