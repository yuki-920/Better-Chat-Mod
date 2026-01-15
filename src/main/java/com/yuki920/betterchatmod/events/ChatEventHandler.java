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

        // Do not process action bar messages, or if the mod is disabled
        if (event.type == 2 || !Config.duplicateMessagesEnabled) {
            return;
        }

        List<ChatLine> drawnChatLines = getDrawnChatLines();
        if (drawnChatLines == null || drawnChatLines.isEmpty()) {
            return;
        }

        String currentMessageText = event.message.getUnformattedText();
        String sanitizedCurrent = currentMessageText.replaceAll("\\p{C}", "").trim();

        if (sanitizedCurrent.isEmpty()) {
            return;
        }

        // We will determine which lines to replace and what the base text is.
        List<ChatLine> linesToDelete = null;
        String baseTextToCompare;
        int currentStack = 1;

        // --- Strategy 1: Check single-line message ---
        // This handles most server/plugin messages and single-line player chat.
        ChatLine lastLine = drawnChatLines.get(0);
        String lastLineText = lastLine.getChatComponent().getUnformattedText();

        Matcher lastLineMatcher = STACK_PATTERN.matcher(lastLineText);
        if (lastLineMatcher.matches()) {
            baseTextToCompare = lastLineMatcher.group(1);
            currentStack = Integer.parseInt(lastLineMatcher.group(2));
        } else {
            baseTextToCompare = lastLineText;
            currentStack = 1;
        }

        // Compare the sanitized new message with the sanitized base of the last line.
        if (sanitizedCurrent.equals(baseTextToCompare.replaceAll("\\p{C}", "").trim())) {
            linesToDelete = new ArrayList<>();
            linesToDelete.add(lastLine);
        } else {
            // --- Strategy 2: Check multi-line message ---
            // If the single-line check failed, it could be a wrapped message.
            int latestUpdateCounter = lastLine.getUpdatedCounter();
            List<ChatLine> potentialMultiLine = new ArrayList<>();
            for (ChatLine line : drawnChatLines) {
                if (line.getUpdatedCounter() == latestUpdateCounter) {
                    potentialMultiLine.add(line);
                } else {
                    break;
                }
            }

            // Only proceed if it's actually a multi-line message; otherwise, strategy 1 would have passed.
            if (potentialMultiLine.size() > 1) {
                StringBuilder lastMessageBuilder = new StringBuilder();
                // Iterate in reverse to assemble the message in the correct order
                for (int i = potentialMultiLine.size() - 1; i >= 0; i--) {
                    lastMessageBuilder.append(potentialMultiLine.get(i).getChatComponent().getUnformattedText());
                }
                String fullLastMessageText = lastMessageBuilder.toString();

                Matcher fullMsgMatcher = STACK_PATTERN.matcher(fullLastMessageText);
                if (fullMsgMatcher.matches()) {
                    baseTextToCompare = fullMsgMatcher.group(1);
                    currentStack = Integer.parseInt(fullMsgMatcher.group(2));
                } else {
                    baseTextToCompare = fullLastMessageText;
                    currentStack = 1;
                }

                if (sanitizedCurrent.equals(baseTextToCompare.replaceAll("\\p{C}", "").trim())) {
                    linesToDelete = potentialMultiLine;
                }
            }
        }

        // If a match was found with either strategy, perform the stack.
        if (linesToDelete != null) {
            // Cancel the original event to prevent the message from being added automatically
            event.setCanceled(true);

            // Manually delete all lines from the previous stacked message
            for (int i = 0; i < linesToDelete.size(); i++) {
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
