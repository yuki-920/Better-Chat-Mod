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

    private static final Pattern STACK_PATTERN = Pattern.compile("^(.*) \\[x(\\d+)]$");

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

    private List<ChatLine> getDrawnChatLines() {
        GuiNewChat chatGUI = Minecraft.getMinecraft().ingameGUI.getChatGUI();
        if (drawnChatLinesField == null) return null;
        try {
            @SuppressWarnings("unchecked")
            List<ChatLine> drawnChatLines = (List<ChatLine>) drawnChatLinesField.get(chatGUI);
            return drawnChatLines;
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        // Mention sound should work for any message type except action bar
        if (event.type != 2) {
            handleMentionSound(event.message);
        }

        // Only process standard chat messages for stacking
        if (event.type != 0 || !Config.duplicateMessagesEnabled) {
            return;
        }

        List<ChatLine> drawnChatLines = getDrawnChatLines();
        if (drawnChatLines == null || drawnChatLines.isEmpty()) {
            return;
        }

        // Group all lines from the last message using the update counter
        int latestUpdateCounter = drawnChatLines.get(0).getUpdatedCounter();
        List<ChatLine> lastMessageLines = new ArrayList<>();
        for (ChatLine line : drawnChatLines) {
            if (line.getUpdatedCounter() == latestUpdateCounter) {
                lastMessageLines.add(line);
            } else {
                // Stop as soon as we hit a line from a previous message
                break;
            }
        }

        // Reconstruct the full message text from all its lines
        StringBuilder lastMessageBuilder = new StringBuilder();
        // Iterate in reverse to assemble the message in the correct order
        for (int i = lastMessageLines.size() - 1; i >= 0; i--) {
            lastMessageBuilder.append(lastMessageLines.get(i).getChatComponent().getUnformattedText());
        }
        String lastMessageText = lastMessageBuilder.toString();
        String currentMessageText = event.message.getUnformattedText();

        // Check for an existing stack and deconstruct the message
        Matcher matcher = STACK_PATTERN.matcher(lastMessageText);
        String lastMessageBase = lastMessageText;
        int currentStack = 1;

        if (matcher.matches()) {
            lastMessageBase = matcher.group(1);
            currentStack = Integer.parseInt(matcher.group(2));
        }

        // If the new message matches the base of the last one, stack it
        if (currentMessageText.equals(lastMessageBase)) {
            // Cancel the original event to prevent the message from being added automatically
            event.setCanceled(true);

            // Manually delete all lines from the previous stacked message
            for (int i = 0; i < lastMessageLines.size(); i++) {
                deleteLastChatLine();
            }

            // Create the new message with the incremented stack count
            int newStack = currentStack + 1;
            IChatComponent newComponent = event.message.createCopy();
            newComponent.appendText(" [x" + newStack + "]");

            // Manually add the new, updated message to the chat GUI
            Minecraft.getMinecraft().ingameGUI.getChatGUI().printChatMessage(newComponent);
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
