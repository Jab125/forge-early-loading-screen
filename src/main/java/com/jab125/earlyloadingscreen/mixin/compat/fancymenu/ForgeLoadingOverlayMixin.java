//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.jab125.earlyloadingscreen.mixin.compat.fancymenu;

import de.keksuccino.fancymenu.FancyMenu;
import de.keksuccino.fancymenu.events.SoftMenuReloadEvent;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.custom.MainMenuHandler;
import de.keksuccino.konkrete.events.client.GuiScreenEvent;

import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.resource.ResourceReload;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import net.minecraftforge.fml.earlydisplay.DisplayWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(ForgeLoadingOverlay.class)
public abstract class ForgeLoadingOverlayMixin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static boolean animationsLoaded = false;
    private static boolean firstScreenInit = true;
    private MenuHandlerBase menuHandler = null;

    public ForgeLoadingOverlayMixin() {
    }

    @Inject(method = "<init>(Lnet/minecraft/client/MinecraftClient;Lnet/minecraft/resource/ResourceReload;Ljava/util/function/Consumer;Lnet/minecraftforge/fml/earlydisplay/DisplayWindow;)V", at = @At("RETURN"))
    private void onConstructFancyMenu(final MinecraftClient mc, final ResourceReload reloader, final Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow, CallbackInfo info) {
        if (!animationsLoaded) {
            FancyMenu.initConfig();
            animationsLoaded = true;
            LOGGER.info("[FANCYMENU] Pre-loading animations if enabled in config..");
            AnimationHandler.preloadAnimations();
        }

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V"))
    private void beforeRenderScreenFancyMenu(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen != null && this.menuHandler != null && MenuCustomization.isMenuCustomizable(MinecraftClient.getInstance().currentScreen)) {
            this.menuHandler.onRenderPre(new GuiScreenEvent.DrawScreenEvent.Pre(MinecraftClient.getInstance().currentScreen, graphics, mouseX, mouseY, partial));
        }

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;render(Lnet/minecraft/client/gui/DrawContext;IIF)V", shift = Shift.AFTER))
    private void afterRenderScreenFancyMenu(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen != null && this.menuHandler != null && MenuCustomization.isMenuCustomizable(MinecraftClient.getInstance().currentScreen)) {
            if (this.menuHandler instanceof MainMenuHandler) {
                MinecraftClient.getInstance().currentScreen.renderBackground(graphics);
            }

            this.menuHandler.onRenderPost(new GuiScreenEvent.DrawScreenEvent.Post(MinecraftClient.getInstance().currentScreen, graphics, mouseX, mouseY, partial));
        }

    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;init(Lnet/minecraft/client/MinecraftClient;II)V", shift = Shift.AFTER))
    private void afterInitScreenFancyMenu(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen != null) {
            AnimationHandler.setReady(true);
            MenuCustomization.allowScreenCustomization = true;
            this.menuHandler = MenuHandlerRegistry.getHandlerFor(MinecraftClient.getInstance().currentScreen);
            if (this.menuHandler != null && firstScreenInit) {
                this.menuHandler.onSoftReload(new SoftMenuReloadEvent(MinecraftClient.getInstance().currentScreen));
            }

            firstScreenInit = false;
            MenuCustomization.setIsNewMenu(true);
            MinecraftClient.getInstance().setScreen(MinecraftClient.getInstance().currentScreen);
        }

    }
}
