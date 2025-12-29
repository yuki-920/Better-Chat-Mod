package com.yuki920.betterchatmod.client.gui;

import net.minecraft.client.gui.ChatLine;
import net.minecraft.util.IChatComponent;

public class AnimatedChatLine extends ChatLine {

    private final long creationTime;

    public AnimatedChatLine(int p_i45000_1_, IChatComponent p_i45000_2_, int p_i45000_3_) {
        super(p_i45000_1_, p_i45000_2_, p_i45000_3_);
        this.creationTime = System.currentTimeMillis();
    }

    public long getCreationTime() {
        return this.creationTime;
    }
}
