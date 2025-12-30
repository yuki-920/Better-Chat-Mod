package com.yuki920.betterchatmod.commands;

import com.yuki920.betterchatmod.client.gui.ModGuiConfig;
import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ChatComponentText;

import java.util.List;

public class BetterChatCommand extends CommandBase {

    @Override
    public String getCommandName() {
        return "betterchat";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/betterchat <gui|setnick|setting> [value]";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        String setting = args[0].toLowerCase();

        if (setting.equals("gui")) {
            Minecraft.getMinecraft().addScheduledTask(() -> Minecraft.getMinecraft().displayGuiScreen(new ModGuiConfig()));
            return;
        }

        if (setting.equals("setnick")) {
            if (args.length < 2) {
                sender.addChatMessage(new ChatComponentText("Usage: /betterchat setnick <nickname|reset>"));
                return;
            }
            String nickname = args[1];
            if (nickname.equalsIgnoreCase("reset")) {
                Config.mentionNickname = "";
                sender.addChatMessage(new ChatComponentText("Mention nickname has been reset."));
            } else {
                Config.mentionNickname = nickname;
                sender.addChatMessage(new ChatComponentText("Mention nickname set to: " + nickname));
            }
            Config.save();
            return;
        }

        if (args.length < 2) {
            sender.addChatMessage(new ChatComponentText(getCommandUsage(sender)));
            return;
        }

        String value = args[1].toLowerCase();

        try {
            if (setting.equals("animation")) {
                Config.animationEnabled = value.equals("on");
            } else if (setting.equals("animation_speed")) {
                Config.animationSpeed = parseInt(value, 0, 500);
            } else if (setting.equals("background")) {
                Config.backgroundEnabled = value.equals("on");
            } else if (setting.equals("background_opacity")) {
                Config.backgroundOpacity = parseInt(value, 0, 100);
            } else if (setting.equals("duplicate_messages")) {
                Config.duplicateMessagesEnabled = value.equals("on");
            } else if (setting.equals("mention_sound")) {
                Config.mentionSoundEnabled = value.equals("on");
            } else {
                sender.addChatMessage(new ChatComponentText("Unknown setting: " + setting));
                return;
            }
        } catch (NumberInvalidException e) {
            sender.addChatMessage(new ChatComponentText("Invalid number for " + setting));
            return;
        }

        Config.save();
        sender.addChatMessage(new ChatComponentText("Setting " + setting + " updated to " + value));
    }

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args, BlockPos pos) {
        if (args.length == 1) {
            return getListOfStringsMatchingLastWord(args, "animation", "animation_speed", "background", "background_opacity", "duplicate_messages", "mention_sound", "setnick", "gui");
        } else if (args.length == 2) {
            String setting = args[0].toLowerCase();
            if (setting.equals("animation") || setting.equals("background") || setting.equals("duplicate_messages") || setting.equals("mention_sound")) {
                return getListOfStringsMatchingLastWord(args, "on", "off");
            } else if (setting.equals("setnick")) {
                return getListOfStringsMatchingLastWord(args, "reset");
            }
        }
        return null;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender sender) {
        return true;
    }
}
