package com.yuki920.betterchatmod.client.gui;

import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiSlider;

import java.io.IOException;

public class ModGuiConfig extends GuiScreen {

    private GuiButton animationButton;
    private GuiSlider animationSpeedSlider;
    private GuiButton backgroundButton;
    private GuiSlider backgroundOpacitySlider;
    private GuiButton duplicateMessagesButton;
    private GuiButton mentionSoundButton;
    private GuiTextField mentionNicknameField;

    @Override
    public void initGui() {
        super.initGui();
        int centerX = this.width / 2;
        int y = this.height / 2 - 80;

        // Animation Enabled Button
        this.buttonList.add(animationButton = new GuiButton(0, centerX - 100, y, 200, 20, "Animation: " + (Config.animationEnabled ? "On" : "Off")));
        y += 24;

        // Animation Speed Slider
        this.buttonList.add(animationSpeedSlider = new GuiSlider(1, centerX - 100, y, 200, 20, "Animation Speed: ", "ms", 0, 500, Config.animationSpeed, false, true));
        y += 24;

        // Background Enabled Button
        this.buttonList.add(backgroundButton = new GuiButton(2, centerX - 100, y, 200, 20, "Background: " + (Config.backgroundEnabled ? "On" : "Off")));
        y += 24;

        // Background Opacity Slider
        this.buttonList.add(backgroundOpacitySlider = new GuiSlider(3, centerX - 100, y, 200, 20, "Background Opacity: ", "%", 0, 100, Config.backgroundOpacity, false, true));
        y += 24;

        // Duplicate Messages Button
        this.buttonList.add(duplicateMessagesButton = new GuiButton(4, centerX - 100, y, 200, 20, "Duplicate Messages: " + (Config.duplicateMessagesEnabled ? "On" : "Off")));
        y += 24;

        // Mention Sound Button
        this.buttonList.add(mentionSoundButton = new GuiButton(5, centerX - 100, y, 200, 20, "Mention Sound: " + (Config.mentionSoundEnabled ? "On" : "Off")));
        y += 24;

        // Mention Nickname Text Field
        this.mentionNicknameField = new GuiTextField(6, this.fontRendererObj, centerX - 100, y, 200, 20);
        this.mentionNicknameField.setMaxStringLength(32);
        this.mentionNicknameField.setText(Config.mentionNickname);
    }

    @Override
    protected void actionPerformed(GuiButton button) throws IOException {
        switch (button.id) {
            case 0:
                Config.animationEnabled = !Config.animationEnabled;
                animationButton.displayString = "Animation: " + (Config.animationEnabled ? "On" : "Off");
                break;
            case 2:
                Config.backgroundEnabled = !Config.backgroundEnabled;
                backgroundButton.displayString = "Background: " + (Config.backgroundEnabled ? "On" : "Off");
                break;
            case 4:
                Config.duplicateMessagesEnabled = !Config.duplicateMessagesEnabled;
                duplicateMessagesButton.displayString = "Duplicate Messages: " + (Config.duplicateMessagesEnabled ? "On" : "Off");
                break;
            case 5:
                Config.mentionSoundEnabled = !Config.mentionSoundEnabled;
                mentionSoundButton.displayString = "Mention Sound: " + (Config.mentionSoundEnabled ? "On" : "Off");
                break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        this.drawCenteredString(this.fontRendererObj, "Better Chat Mod Settings", this.width / 2, this.height / 2 - 100, 0xFFFFFF);
        this.mentionNicknameField.drawTextBox();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.mentionNicknameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.mentionNicknameField.textboxKeyTyped(typedChar, keyCode);
        if (keyCode == 1) { // Escape key
            this.mc.displayGuiScreen(null);
        }
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        this.mentionNicknameField.updateCursorCounter();
    }

    @Override
    public void onGuiClosed() {
        Config.animationSpeed = this.animationSpeedSlider.getValueInt();
        Config.backgroundOpacity = this.backgroundOpacitySlider.getValueInt();
        Config.mentionNickname = this.mentionNicknameField.getText();
        Config.save();
    }
}
