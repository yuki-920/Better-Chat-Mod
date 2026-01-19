package com.yuki920.betterchatmod.events;

import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChatEventHandler {

    private String lastMessageKey = null;
    private IChatComponent lastMessageComponent = null;
    private int stackCount = 0;

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
                // To fully remove a message, we may need to remove multiple lines
                // depending on how it was wrapped. We identify all lines belonging
                // to the last message by their shared updateCounter.
                int lastUpdateCounter = drawnChatLines.get(0).getUpdatedCounter();
                drawnChatLines.removeIf(line -> line.getUpdatedCounter() == lastUpdateCounter);
            }

            @SuppressWarnings("unchecked")
            List<ChatLine> chatLines = (List<ChatLine>) chatLinesField.get(chatGUI);
            if (!chatLines.isEmpty()) {
                int lastUpdateCounter = chatLines.get(0).getUpdatedCounter();
                chatLines.removeIf(line -> line.getUpdatedCounter() == lastUpdateCounter);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        // Handle mention sound for all message types except action bar
        if (event.type != 2) {
            handleMentionSound(event.message);
        }

        // Per instructions, only process event types 0 and 1.
        // Also, skip if the mod feature is disabled.
        if (event.type == 2 || !Config.duplicateMessagesEnabled) {
            // If the message is different, reset the counter.
            this.lastMessageKey = null;
            this.lastMessageComponent = null;
            this.stackCount = 0;
            return;
        }

        // Create the normalized key for comparison using the strictest method, as per instructions.
        String currentMessageKey = event.message.getUnformattedText()
                .replace('\u00A0', ' ') // Replace non-breaking spaces
                .replaceAll("[\u200B-\u200D\uFEFF]", "") // Remove zero-width characters
                .replaceAll("[\r\n]", "") // Remove newline characters
                .replaceAll("\\s+", " ") // Collapse consecutive spaces
                .trim();

        // If the new message is the same as the last one, stack it.
        if (currentMessageKey.equals(this.lastMessageKey)) {
            this.stackCount++;

            // Cancel the event to prevent the original message from appearing.
            event.setCanceled(true);

            // Delete the previous message from the chat GUI.
            deleteLastChatLine();

            // Create a new component for display, preserving the original's style and events.
            IChatComponent newComponent = this.lastMessageComponent.createCopy();
            String counterText = " §7[x" + this.stackCount + "]";
            newComponent.appendSibling(new ChatComponentText(counterText));

            // Print the new, stacked message to the chat.
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(newComponent);
        } else {
            // If the message is different, reset the state.
            this.lastMessageKey = currentMessageKey;
            this.lastMessageComponent = event.message.createCopy(); // Store the original component for future use
            this.stackCount = 1;
            // Do not cancel the event, allowing the new message to be displayed normally.
        }
    }

    private void handleMentionSound(IChatComponent message) {
        if (!Config.mentionSoundEnabled) return;

        String messageText = message.getUnformattedText().toLowerCase();
        String playerName = Minecraft.getMinecraft().thePlayer.getName().toLowerCase();
        String realName = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();
        String customNick = Config.mentionNickname.toLowerCase();

        boolean mentioned = message.getUnformattedText().toLowerCase().contains(playerName) || messageText.contains(realName);
        if (!customNick.isEmpty() && messageText.contains(customNick)) {
            mentioned = true;
        }

        if (mentioned) {
            Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0F, 1.0F);
        }
    }
}
