package com.yuki920.betterchatmod.client.gui;

import com.google.common.collect.Lists;
import com.yuki920.betterchatmod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ChatLine;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiNewChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import net.minecraft.client.gui.GuiUtilRenderComponents;

import java.util.Iterator;
import java.util.List;

public class CustomGuiNewChat extends GuiNewChat {

    private final Minecraft mc;
    private final List<ChatLine> sentMessages = Lists.<ChatLine>newArrayList();
    private final List<ChatLine> chatLines = Lists.<ChatLine>newArrayList();
    private int scrollPos;
    private boolean isScrolled;

    public CustomGuiNewChat(Minecraft mcIn) {
        super(mcIn);
        this.mc = mcIn;
    }

    @Override
    public void drawChat(int updateCounter) {
        if (this.mc.gameSettings.chatVisibility != EntityPlayer.EnumChatVisibility.HIDDEN) {
            int i = this.getLineCount();
            boolean flag = false;
            int j = 0;
            int k = this.chatLines.size();
            float f = this.mc.gameSettings.chatOpacity * 0.9F + 0.1F;

            if (k > 0) {
                if (this.getChatOpen()) {
                    flag = true;
                }

                float f1 = this.getChatScale();
                int l = MathHelper.ceiling_float_int((float) this.getChatWidth() / f1);
                GlStateManager.pushMatrix();
                GlStateManager.translate(2.0F, 20.0F, 0.0F);
                GlStateManager.scale(f1, f1, 1.0F);

                for (int i1 = 0; i1 + this.scrollPos < this.chatLines.size() && i1 < i; ++i1) {
                    ChatLine chatline = this.chatLines.get(i1 + this.scrollPos);

                    if (chatline != null) {
                        int j1 = updateCounter - chatline.getUpdatedCounter();

                        if (j1 < 200 || flag) {
                            double d0 = (double) j1 / 200.0D;
                            d0 = 1.0D - d0;
                            d0 = d0 * 10.0D;
                            d0 = MathHelper.clamp_double(d0, 0.0D, 1.0D);
                            d0 = d0 * d0;
                            int l1 = (int) (255.0D * d0);

                            if (flag) {
                                l1 = 255;
                            }

                            l1 = (int) ((float) l1 * f);
                            ++j;

                            if (l1 > 3) {
                                int i2 = 0;
                                int j2 = -i1 * 9;

                                if (Config.backgroundEnabled) {
                                    drawRect(i2, j2 - 9, i2 + l + 4, j2, (int) (Config.backgroundAlpha / 100.0F * 255.0F) << 24);
                                }

                                String s = chatline.getChatComponent().getFormattedText();
                                GlStateManager.enableBlend();
                                GlStateManager.translate(0, 0, 50);

                                if (Config.animationEnabled && chatline instanceof AnimatedChatLine) {
                                    long time = ((AnimatedChatLine) chatline).getCreationTime();
                                    long timeDiff = System.currentTimeMillis() - time;
                                    if (timeDiff < Config.animationSpeed) {
                                        float percent = (float) timeDiff / (float) Config.animationSpeed;
                                        GlStateManager.translate(0, 9 - 9 * percent, 0);
                                    }
                                }

                                this.mc.fontRendererObj.drawStringWithShadow(s, (float) i2, (float) (j2 - 8), 16777215 + (l1 << 24));
                                GlStateManager.disableAlpha();
                                GlStateManager.disableBlend();
                            }
                        }
                    }
                }

                GlStateManager.popMatrix();
            }
        }
    }

    @Override
    public void printChatMessage(IChatComponent chatComponent) {
        this.printChatMessageWithOptionalDeletion(chatComponent, 0);
    }

    @Override
    public void printChatMessageWithOptionalDeletion(IChatComponent chatComponent, int chatLineId) {
        this.setChatLine(chatComponent, chatLineId, this.mc.ingameGUI.getUpdateCounter(), false);
    }

    private void setChatLine(IChatComponent chatComponent, int chatLineId, int updateCounter, boolean displayOnly) {
        if (chatLineId != 0) {
            this.deleteChatLine(chatLineId);
        }

        int i = MathHelper.floor_float((float) this.getChatWidth() / this.getChatScale());
        List<IChatComponent> list = GuiUtilRenderComponents.func_178908_a(chatComponent, i, this.mc.fontRendererObj, false, false);
        boolean flag = this.getChatOpen();

        for (IChatComponent ichatcomponent : list) {
            if (flag && this.scrollPos > 0) {
                this.isScrolled = true;
                this.scroll(1);
            }

            this.chatLines.add(0, new AnimatedChatLine(updateCounter, ichatcomponent, chatLineId));
        }

        while (this.chatLines.size() > 100) {
            this.chatLines.remove(this.chatLines.size() - 1);
        }

        if (!displayOnly) {
            this.sentMessages.add(0, new AnimatedChatLine(updateCounter, chatComponent, chatLineId));

            while (this.sentMessages.size() > 100) {
                this.sentMessages.remove(this.sentMessages.size() - 1);
            }
        }
    }
}
