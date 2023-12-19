/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.neoforge.client.loading;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.mojang.blaze3d.systems.VertexSorter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.render.*;
import net.minecraft.util.Identifier;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import net.neoforged.fml.StartupMessageManager;
import net.neoforged.fml.earlydisplay.ColourScheme;
import net.neoforged.fml.earlydisplay.DisplayWindow;
import net.neoforged.fml.loading.progress.ProgressMeter;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL30C;

/**
 * This is an implementation of the LoadingOverlay that calls back into the early window rendering, as part of the
 * game loading cycle. We completely replace the {@link #render(DrawContext, int, int, float)} call from the parent
 * with one of our own, that allows us to blend our early loading screen into the main window, in the same manner as
 * the Mojang screen. It also allows us to see and tick appropriately as the later stages of the loading system run.
 *
 * It is somewhat a copy of the superclass render method.
 */
public class NeoForgeLoadingOverlay extends SplashOverlay {
    private final MinecraftClient minecraft;
    private final ResourceReload reload;
    private final Consumer<Optional<Throwable>> onFinish;
    private final DisplayWindow displayWindow;
    private final ProgressMeter progress;
    private long fadeOutStart = -1L;

    public NeoForgeLoadingOverlay(final MinecraftClient mc, final ResourceReload reloader, final Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow) {
        super(mc, reloader, errorConsumer, false);
        this.minecraft = mc;
        this.reload = reloader;
        this.onFinish = errorConsumer;
        this.displayWindow = displayWindow;
        displayWindow.addMojangTexture(mc.getTextureManager().getTexture(new Identifier("textures/gui/title/mojangstudios.png")).getGlId());
        this.progress = StartupMessageManager.prependProgressBar("Minecraft Progress", 100);
    }

    public static Supplier<SplashOverlay> newInstance(Supplier<MinecraftClient> mc, Supplier<ResourceReload> ri, Consumer<Optional<Throwable>> handler, DisplayWindow window) {
        return () -> new NeoForgeLoadingOverlay(mc.get(), ri.get(), handler, window);
    }

    @Override
    public void render(final @NotNull DrawContext graphics, final int mouseX, final int mouseY, final float partialTick) {
        long millis = Util.getMeasuringTimeMs();
        float fadeouttimer = this.fadeOutStart > -1L ? (float) (millis - this.fadeOutStart) / 1000.0F : -1.0F;
        progress.setAbsolute(MathHelper.clamp((int) (this.reload.getProgress() * 100f), 0, 100));
        var fade = 1.0F - MathHelper.clamp(fadeouttimer - 1.0F, 0.0F, 1.0F);
        var colour = this.displayWindow.context().colourScheme().background();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fade);
        if (fadeouttimer >= 1.0F) {
            if (this.minecraft.currentScreen != null) {
                this.minecraft.currentScreen.render(graphics, 0, 0, partialTick);
            }
            displayWindow.render(0xff);
        } else {
            GlStateManager._clearColor(colour.redf(), colour.greenf(), colour.bluef(), 1f);
            GlStateManager._clear(GlConst.GL_COLOR_BUFFER_BIT, MinecraftClient.IS_SYSTEM_MAC);
            displayWindow.render(0xFF);
        }
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
        var fbWidth = this.minecraft.getWindow().getWidth();
        var fbHeight = this.minecraft.getWindow().getHeight();
        GL30C.glViewport(0, 0, fbWidth, fbHeight);
        final var twidth = this.displayWindow.context().width();
        final var theight = this.displayWindow.context().height();
        var wscale = (float) fbWidth / twidth;
        var hscale = (float) fbHeight / theight;
        var scale = this.displayWindow.context().scale() * Math.min(wscale, hscale) / 2f;
        var wleft = MathHelper.clamp(fbWidth * 0.5f - scale * twidth, 0, fbWidth);
        var wtop = MathHelper.clamp(fbHeight * 0.5f - scale * theight, 0, fbHeight);
        var wright = MathHelper.clamp(fbWidth * 0.5f + scale * twidth, 0, fbWidth);
        var wbottom = MathHelper.clamp(fbHeight * 0.5f + scale * theight, 0, fbHeight);
        GlStateManager.glActiveTexture(GlConst.GL_TEXTURE0);
        RenderSystem.disableCull();
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, fade);
        RenderSystem.getModelViewMatrix().identity();
        RenderSystem.setProjectionMatrix(new Matrix4f().setOrtho(0.0F, fbWidth, 0.0F, fbHeight, 0.1f, -0.1f), VertexSorter.BY_Z);
        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
        // This is fill in around the edges - it's empty solid colour
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
        // top box from hpos
        addQuad(bufferbuilder, 0, fbWidth, wtop, fbHeight, colour, fade);
        // bottom box to hpos
        addQuad(bufferbuilder, 0, fbWidth, 0, wtop, colour, fade);
        // left box to wpos
        addQuad(bufferbuilder, 0, wleft, wtop, wbottom, colour, fade);
        // right box from wpos
        addQuad(bufferbuilder, wright, fbWidth, wtop, wbottom, colour, fade);
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());

        // This is the actual screen data from the loading screen
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlConst.GL_SRC_ALPHA, GlConst.GL_ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexColorProgram);
        RenderSystem.setShaderTexture(0, displayWindow.getFramebufferTextureId());
        bufferbuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
        bufferbuilder.vertex(wleft, wbottom, 0f).texture(0, 0).color(1f, 1f, 1f, fade).next();
        bufferbuilder.vertex(wright, wbottom, 0f).texture(1, 0).color(1f, 1f, 1f, fade).next();
        bufferbuilder.vertex(wright, wtop, 0f).texture(1, 1).color(1f, 1f, 1f, fade).next();
        bufferbuilder.vertex(wleft, wtop, 0f).texture(0, 1).color(1f, 1f, 1f, fade).next();
        GL30C.glTexParameterIi(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MIN_FILTER, GlConst.GL_NEAREST);
        GL30C.glTexParameterIi(GlConst.GL_TEXTURE_2D, GlConst.GL_TEXTURE_MAG_FILTER, GlConst.GL_NEAREST);
        BufferRenderer.drawWithGlobalProgram(bufferbuilder.end());
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1f);

        if (fadeouttimer >= 2.0F) {
            this.minecraft.setOverlay(null);
            this.displayWindow.close();
        }

        if (this.fadeOutStart == -1L && this.reload.isComplete()) {
            progress.complete();
            this.fadeOutStart = Util.getMeasuringTimeMs();
            try {
                this.reload.throwException();
                this.onFinish.accept(Optional.empty());
            } catch (Throwable throwable) {
                this.onFinish.accept(Optional.of(throwable));
            }

            if (this.minecraft.currentScreen != null) {
                this.minecraft.currentScreen.init(this.minecraft, this.minecraft.getWindow().getScaledWidth(), this.minecraft.getWindow().getScaledHeight());
            }
        }
    }

    private static void addQuad(BufferVertexConsumer bufferbuilder, float x0, float x1, float y0, float y1, ColourScheme.Colour colour, float fade) {
        bufferbuilder.vertex(x0, y0, 0f).color(colour.redf(), colour.greenf(), colour.bluef(), fade).next();
        bufferbuilder.vertex(x0, y1, 0f).color(colour.redf(), colour.greenf(), colour.bluef(), fade).next();
        bufferbuilder.vertex(x1, y1, 0f).color(colour.redf(), colour.greenf(), colour.bluef(), fade).next();
        bufferbuilder.vertex(x1, y0, 0f).color(colour.redf(), colour.greenf(), colour.bluef(), fade).next();
    }
}