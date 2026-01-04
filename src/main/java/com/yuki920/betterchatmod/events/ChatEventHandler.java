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

        ChatLine lastLine = drawnChatLines.get(0);
        IChatComponent lastComponent = lastLine.getChatComponent();
        String lastMessageText = lastComponent.getUnformattedText();
        String currentMessageText = event.message.getUnformattedText();

        Matcher matcher = STACK_PATTERN.matcher(lastMessageText);

        String lastMessageBase = lastMessageText;
        int currentStack = 1;
        IChatComponent baseComponent = event.message.createCopy();

        if (matcher.matches()) {
            lastMessageBase = matcher.group(1);
            currentStack = Integer.parseInt(matcher.group(2));
            // To preserve formatting, we rebuild the base component from the last stacked message
            String json = IChatComponent.Serializer.componentToJson(lastComponent);
            String baseJson = json.replaceFirst(",?\" \\[x\\d+]\"", "");
            baseComponent = IChatComponent.Serializer.jsonToComponent(baseJson);
        }

        if (currentMessageText.equals(lastMessageBase)) {
            deleteLastChatLine();
            int newStack = currentStack + 1;
            IChatComponent newComponent = baseComponent.createCopy();
            newComponent.appendText(" [x" + newStack + "]");
            event.message = newComponent;
        }
    }

    private void handleMentionSound(IChatComponent message) {
        if (!Config.mentionSoundEnabled) return;

        String messageText = message.getUnformattedText().toLowerCase();
        String playerName = Minecraft.getMinecraft().thePlayer.getName().toLowerCase();
        String realName = Minecraft.getMinecraft().getSession().getUsername().toLowerCase();
        String customNick = Config.mentionNickname.toLowerCase();

        boolean mentioned = messageText.contains(playerName) || messageText.contains(realName);
        if (!customNick.isEmpty() && messageText.contains(customNick)) {
            mentioned = true;
        }

        if (mentioned) {
            Minecraft.getMinecraft().thePlayer.playSound("random.orb", 1.0F, 1.0F);
        }
    }
}
