package com.yuki920.betterchatmod;

import com.yuki920.betterchatmod.mixins.GuiNewChatAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.event.HoverEvent;
import net.minecraft.util.ChatComponentStyle;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class ChatHandler {

    private static final Map<Integer, ChatEntry> chatMessageMap = new HashMap<>();
    private static final Map<Integer, Set<ChatLine>> messagesForHash = new HashMap<>();
    
    private static final String chatTimestampRegex = "^(?:\\[\\d\\d:\\d\\d(:\\d\\d)?(?: AM| PM|)]|<\\d\\d:\\d\\d>) ";
    private static final DecimalFormat decimalFormat = new DecimalFormat("#,###");

    public static int currentMessageHash = -1;
    private int ticks;
    
    private Set<String> playerNames = new HashSet<>();

    @SubscribeEvent
    public void tick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            Minecraft mc = Minecraft.getMinecraft();
            
            // プレイヤー名を更新
            if (mc.thePlayer != null) {
                playerNames.clear();
                playerNames.add(mc.thePlayer.getName());
                playerNames.add(mc.thePlayer.getDisplayNameString());
            }
            
            if (ticks++ >= 12000) {
                long time = System.currentTimeMillis();
                for (Map.Entry<Integer, ChatEntry> entry : chatMessageMap.entrySet()) {
                    if ((time - entry.getValue().lastSeenMessageMillis) > (ChatConfig.compactChatTime * 1000L)) {
                        messagesForHash.remove(entry.getKey());
                    }
                }
                ticks = 0;
            }
        }
    }

    @SubscribeEvent
    public void changeWorld(WorldEvent.Load event) {
        ticks = 0;
    }

    @SubscribeEvent
    public void onChatReceived(ClientChatReceivedEvent event) {
        if (event.type == 2) return; // ゲーム情報は無視
        
        Minecraft mc = Minecraft.getMinecraft();
        
        // メンション検出
        if (ChatConfig.playSoundOnMention && mc.thePlayer != null) {
            String message = event.message.getUnformattedText().toLowerCase();
            for (String name : playerNames) {
                if (!name.isEmpty() && message.contains(name.toLowerCase())) {
                    playMentionSound();
                    break;
                }
            }
        }
    }
    
    private void playMentionSound() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null) {
            mc.thePlayer.playSound(
                ChatConfig.mentionSound, 
                ChatConfig.mentionVolume, 
                ChatConfig.mentionPitch
            );
        }
    }

    public static void appendMessageCounter(IChatComponent chatComponent, boolean refresh) {
        if (!refresh) {
            String message = cleanColor(chatComponent.getFormattedText()).trim();
            if (message.isEmpty() || isDivider(message)) {
                return;
            }

            currentMessageHash = getChatComponentHash(chatComponent);
            long currentTime = System.currentTimeMillis();

            if (!chatMessageMap.containsKey(currentMessageHash)) {
                chatMessageMap.put(currentMessageHash, new ChatEntry(1, currentTime));
            } else {
                ChatEntry entry = chatMessageMap.get(currentMessageHash);
                if ((currentTime - entry.lastSeenMessageMillis) > (ChatConfig.compactChatTime * 1000L)) {
                    chatMessageMap.put(currentMessageHash, new ChatEntry(1, currentTime));
                } else {
                    boolean deleted = deleteMessageByHash(currentMessageHash);
                    if (!deleted) {
                        chatMessageMap.put(currentMessageHash, new ChatEntry(1, currentTime));
                    } else {
                        entry.messageCount++;
                        entry.lastSeenMessageMillis = currentTime;
                        chatComponent.appendSibling(new ChatComponentIgnored(
                            EnumChatFormatting.GRAY + " [x]" + decimalFormat.format(entry.messageCount) + "]"));
                    }
                }
            }
        }
    }

    public static void setChatLine_addToList(ChatLine line) {
        if (currentMessageHash != -1) {
            messagesForHash.computeIfAbsent(currentMessageHash, k -> new HashSet<>()).add(line);
        }
    }

    public static void resetMessageHash() {
        currentMessageHash = -1;
    }

    private static boolean deleteMessageByHash(int hashCode) {
        if (!messagesForHash.containsKey(hashCode) || messagesForHash.get(hashCode).isEmpty()) {
            return false;
        }

        Minecraft mc = Minecraft.getMinecraft();
        final Set<ChatLine> toRemove = messagesForHash.get(hashCode);
        messagesForHash.remove(hashCode);

        final int normalSearchLength = 100;
        final int wrappedSearchLength = 300;

        boolean removedMessage = false;
        {
            GuiNewChatAccessor chatAccessor = (GuiNewChatAccessor) mc.ingameGUI.getChatGUI();
            List<ChatLine> chatLines = chatAccessor.getChatLines();
            
            for (int index = 0; index < chatLines.size() && index < normalSearchLength; index++) {
                final ChatLine chatLine = chatLines.get(index);

                if (toRemove.contains(chatLine)) {
                    removedMessage = true;
                    chatLines.remove(index);
                    index--;

                    if (index < 0 || index >= chatLines.size()) {
                        continue;
                    }

                    index = getMessageIndex(chatLines, index, chatLine);
                } else if (ChatConfig.consecutiveCompactChat) {
                    break;
                }
            }
        }

        if (!removedMessage) {
            return false;
        }

        {
            GuiNewChatAccessor chatAccessor = (GuiNewChatAccessor) mc.ingameGUI.getChatGUI();
            List<ChatLine> chatLinesWrapped = chatAccessor.getDrawnChatLines();
            
            for (int index = 0; index < chatLinesWrapped.size() && index < wrappedSearchLength; index++) {
                final ChatLine chatLine = chatLinesWrapped.get(index);
                if (toRemove.contains(chatLine)) {
                    chatLinesWrapped.remove(index);
                    index--;

                    if (index <= 0 || index >= chatLinesWrapped.size()) {
                        continue;
                    }

                    index = getMessageIndex(chatLinesWrapped, index, chatLine);
                } else if (ChatConfig.consecutiveCompactChat) {
                    break;
                }
            }
        }

        return true;
    }

    private static int getMessageIndex(List<ChatLine> chatMessageList, int index, ChatLine chatLine) {
        final ChatLine prevLine = chatMessageList.get(index);
        if (isDivider(cleanColor(prevLine.getChatComponent().getUnformattedText())) &&
            Math.abs(chatLine.getUpdatedCounter() - prevLine.getUpdatedCounter()) <= 2) {
            chatMessageList.remove(index);
        }

        if (index >= chatMessageList.size()) {
            return index;
        }

        final ChatLine nextLine = chatMessageList.get(index);
        if (isDivider(cleanColor(nextLine.getChatComponent().getUnformattedText())) &&
            Math.abs(chatLine.getUpdatedCounter() - nextLine.getUpdatedCounter()) <= 2) {
            chatMessageList.remove(index);
        }

        index--;

        return index;
    }

    private static int getChatStyleHash(ChatStyle style) {
        final HoverEvent hoverEvent = style.getChatHoverEvent();
        HoverEvent.Action hoverAction = null;
        int hoverChatHash = 0;

        if (hoverEvent != null) {
            hoverAction = hoverEvent.getAction();
            hoverChatHash = getChatComponentHash(hoverEvent.getValue());
        }

        return Objects.hash(style.getColor(),
            style.getBold(),
            style.getItalic(),
            style.getUnderlined(),
            style.getStrikethrough(),
            style.getObfuscated(),
            hoverAction, hoverChatHash,
            style.getChatClickEvent(),
            style.getInsertion());
    }

    private static int getChatComponentHash(IChatComponent chatComponent) {
        List<Integer> siblingHashes = new ArrayList<>();
        for (IChatComponent sibling : chatComponent.getSiblings()) {
            if (!(sibling instanceof ChatComponentIgnored) && sibling instanceof ChatComponentStyle) {
                siblingHashes.add(getChatComponentHash(sibling));
            }
        }

        if (chatComponent instanceof ChatComponentIgnored) {
            return Objects.hash(siblingHashes);
        }

        String unformattedText = chatComponent.getUnformattedText();
        String cleanedMessage = unformattedText.replaceAll(chatTimestampRegex, "").trim();
        return Objects.hash(cleanedMessage, siblingHashes, getChatStyleHash(chatComponent.getChatStyle()));
    }

    private static boolean isDivider(String clean) {
        clean = clean.replaceAll(chatTimestampRegex, "").trim();
        boolean divider = true;
        if (clean.length() < 5) {
            divider = false;
        } else {
            for (int i = 0; i < clean.length(); i++) {
                final char c = clean.charAt(i);
                if (c != '-' && c != '=' && c != '\u25AC') {
                    divider = false;
                    break;
                }
            }
        }

        return divider;
    }

    private static String cleanColor(String in) {
        return in.replaceAll("(?i)\\u00A7.", "");
    }

    static class ChatEntry {
        int messageCount;
        long lastSeenMessageMillis;

        ChatEntry(int messageCount, long lastSeenMessageMillis) {
            this.messageCount = messageCount;
            this.lastSeenMessageMillis = lastSeenMessageMillis;
        }
    }
}
