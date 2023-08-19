//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.jab125.earlyloadingscreen.mixin.compat.drippy;

import com.mojang.blaze3d.systems.RenderSystem;
import de.keksuccino.drippyloadingscreen.DrippyLoadingScreen;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayMenuHandler;
import de.keksuccino.drippyloadingscreen.customization.DrippyOverlayScreen;
import de.keksuccino.drippyloadingscreen.customization.deepcustomization.DeepCustomizationLayers;
import de.keksuccino.drippyloadingscreen.customization.items.Items;
import de.keksuccino.drippyloadingscreen.customization.placeholders.Placeholders;
import de.keksuccino.drippyloadingscreen.mixin.MixinCache;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinLoadingOverlay;
import de.keksuccino.drippyloadingscreen.mixin.mixins.client.IMixinMinecraft;
import de.keksuccino.fancymenu.api.item.CustomizationItemContainer;
import de.keksuccino.fancymenu.api.item.CustomizationItemRegistry;
import de.keksuccino.fancymenu.events.InitOrResizeScreenEvent;
import de.keksuccino.fancymenu.menu.animation.AnimationHandler;
import de.keksuccino.fancymenu.menu.button.ButtonCachedEvent;
import de.keksuccino.fancymenu.menu.fancy.MenuCustomization;
import de.keksuccino.fancymenu.menu.fancy.gameintro.GameIntroHandler;
import de.keksuccino.fancymenu.menu.fancy.helper.ui.popup.FMNotificationPopup;
import de.keksuccino.fancymenu.menu.fancy.item.CustomizationItemBase;
import de.keksuccino.fancymenu.menu.fancy.item.items.ticker.TickerCustomizationItemContainer;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerBase;
import de.keksuccino.fancymenu.menu.fancy.menuhandler.MenuHandlerRegistry;
import de.keksuccino.konkrete.events.client.ClientTickEvent;
import de.keksuccino.konkrete.events.client.GuiScreenEvent;
import de.keksuccino.konkrete.gui.screens.popup.PopupHandler;
import de.keksuccino.konkrete.input.StringUtils;
import java.awt.Color;
import java.io.File;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.IntSupplier;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.FontManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.resource.ResourceReload;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.DummyProfiler;
import net.minecraftforge.client.loading.ForgeLoadingOverlay;
import net.minecraftforge.fml.earlydisplay.DisplayWindow;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin({ForgeLoadingOverlay.class})
public class ForgeLoadingOverlayMixin {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final File CHECKED_FOR_OLD_LAYOUTS_FILE;
    private static final File LEGACY_LAYOUT_DIR;
    @Shadow
    private float progress;
    private static boolean initialized;
    private static DrippyOverlayScreen drippyOverlayScreen;
    private static DrippyOverlayMenuHandler drippyOverlayHandler;
    private int lastScreenWidth = 0;
    private int lastScreenHeight = 0;
    private double renderScale = 0.0;
    private boolean overlayScaled = false;
    private static final IntSupplier BACKGROUND_COLOR;

    public ForgeLoadingOverlayMixin() {
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onConstruct(final MinecraftClient mc, final ResourceReload reloader, final Consumer<Optional<Throwable>> errorConsumer, DisplayWindow displayWindow, CallbackInfo info) {
        if (!initialized) {
            DrippyLoadingScreen.initConfig();
            LOGGER.info("[DRIPPY LOADING SCREEN] Initializing fonts for text rendering..");
           // this.loadFonts();
            Placeholders.registerAll();
            Items.registerAll();
            DeepCustomizationLayers.registerAll();
            LOGGER.info("[DRIPPY LOADING SCREEN] Calculating animation sizes for FancyMenu..");
            AnimationHandler.setupAnimationSizes();
            initialized = true;
        }

        this.handleInitOverlay();
    }

    @Inject(method = "render", at = @At("HEAD"))
    private void onRenderPre(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        MixinCache.cachedCurrentLoadingScreenProgress = this.progress;
        this.handleInitOverlay();
        this.scaleOverlayStart(graphics);
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.onRenderPre(new GuiScreenEvent.DrawScreenEvent.Pre(drippyOverlayScreen, graphics, mouseX, mouseY, partial));
            });
        }

        this.scaleOverlayEnd(graphics);
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void onRenderPost(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        this.scaleOverlayStart(graphics);
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.onRenderPost(new GuiScreenEvent.DrawScreenEvent.Post(drippyOverlayScreen, graphics, mouseX, mouseY, partial));
            });
        }

        this.scaleOverlayEnd(graphics);
        MixinCache.cachedCurrentLoadingScreenProgress = this.progress;
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraftforge/fml/earlydisplay/DisplayWindow;render(I)V"))
    private void onBackgroundRendered(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        this.scaleOverlayStart(graphics);
        if (drippyOverlayScreen != null) {
            this.runMenuHandlerTask(() -> {
                drippyOverlayHandler.drawToBackground(new GuiScreenEvent.BackgroundDrawnEvent(drippyOverlayScreen, graphics));
            });
        }

        this.scaleOverlayEnd(graphics);
    }

    @Inject(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"))
    private void onClose(DrawContext graphics, int mouseX, int mouseY, float partial, CallbackInfo info) {
        if (MinecraftClient.getInstance().currentScreen != null) {
            this.checkForOldLayouts();
        }

    }

    private void checkForOldLayouts() {
        if (LEGACY_LAYOUT_DIR.isDirectory()) {
            String[] layoutFilesList = LEGACY_LAYOUT_DIR.list((dir, name) -> {
                return name.toLowerCase().endsWith(".dllayout");
            });
            if (layoutFilesList.length > 0 && !CHECKED_FOR_OLD_LAYOUTS_FILE.isFile()) {
                try {
                    CHECKED_FOR_OLD_LAYOUTS_FILE.createNewFile();
                    PopupHandler.displayPopup(new FMNotificationPopup(300, new Color(0, 0, 0, 0), 240, (Runnable)null, StringUtils.splitLines(I18n.translate("drippyloadingscreen.legacy_support.old_layouts", new Object[0]), "\n")));
                } catch (Exception var3) {
                    var3.printStackTrace();
                }
            }
        }

    }

//    @ModifyArg( // TODO
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"
//),
//        index = 0
//    )
//    private float overrideBackgroundColorInClearColor0(float f) {
//        int i2 = BACKGROUND_COLOR.getAsInt();
//        return (float)(i2 >> 16 & 255) / 255.0F;
//    }

//    @ModifyArg( // TODO
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"
//),
//        index = 1
//    )
//    private float overrideBackgroundColorInClearColor1(float f) {
//        int i2 = BACKGROUND_COLOR.getAsInt();
//        return (float)(i2 >> 8 & 255) / 255.0F;
//    }

//    @ModifyArg( // TODO
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clearColor(FFFF)V"
//),
//        index = 2
//    )
//    private float overrideBackgroundColorInClearColor2(float f) {
//        int i2 = BACKGROUND_COLOR.getAsInt();
//        return (float)(i2 & 255) / 255.0F;
//    }

//    @ModifyArg( // TODO
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;replaceAlpha(II)I"
//),
//        index = 0
//    )
//    private int overrideBackgroundColorInReplaceAlpha(int originalColor) {
//        return BACKGROUND_COLOR.getAsInt();
//    }

//    @ModifyArg( //TODO
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/screens/LoadingOverlay;replaceAlpha(II)I"
//),
//        index = 1
//    )
//    private int setCustomBackgroundOpacityInReplaceAlpha(int alpha) {
//        float opacity = Math.max(0.0F, Math.min(1.0F, (float)alpha / 255.0F));
//        this.setCustomBackgroundOpacity(opacity);
//        if (!(Boolean)DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
//            this.setOverlayOpacity(opacity);
//        }
//
//        return alpha;
//    }

//    @Inject( // TODO
//        method = {"drawProgressBar"},
//        at = {@At("HEAD")},
//        cancellable = true
//    )
//    private void replaceOriginalProgressBar(DrawContext graphics, int p_96184_, int p_96185_, int p_96186_, int p_96187_, float opacity, CallbackInfo info) {
//        info.cancel();
//        if ((Boolean)DrippyLoadingScreen.config.getOrDefault("early_fade_out_elements", false)) {
//            this.setOverlayOpacity(opacity);
//        }
//
//        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//        if (drippyOverlayHandler != null && drippyOverlayHandler.progressBarItem != null && drippyOverlayScreen != null) {
//            if (!drippyOverlayHandler.progressBarItem.useOriginalSizeAndPosCalculation) {
//                this.scaleOverlayStart(graphics);
//            }
//
//            drippyOverlayHandler.progressBarItem.render(graphics, drippyOverlayScreen);
//            this.scaleOverlayEnd(graphics);
//        }
//
//    }

//    @ModifyArg( // TODO
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"
//),
//        index = 1
//    )
//    private int renderOriginalLogoOffscreenSetXMin(int xMinOriginal) {
//        return -1000000;
//    }

//    @ModifyArg(
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"
//),
//        index = 2
//    )
//    private int renderOriginalLogoOffscreenSetYMin(int yMinOriginal) {
//        return -1000000;
//    }

//    @ModifyArg(
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"
//),
//        index = 3
//    )
//    private int renderOriginalLogoOffscreenSetXMax(int xMaxOriginal) {
//        return -1000000;
//    }

//    @ModifyArg(
//        method = {"render"},
//        at = @At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/GuiGraphics;blit(Lnet/minecraft/resources/ResourceLocation;IIIIFFIIII)V"
//),
//        index = 4
//    )
//    private int renderOriginalLogoOffscreenSetYMax(int yMaxOriginal) {
//        return -1000000;
//    }
//
//    @Inject(
//        method = {"render"},
//        at = {@At(
//    value = "INVOKE",
//    target = "Lnet/minecraft/client/gui/GuiGraphics;fill(Lnet/minecraft/client/renderer/RenderType;IIIII)V"
//)}
//    )
//    private void clearColorBeforeFillDrippy(DrawContext graphics, int p_282704_, int p_283650_, float p_283394_, CallbackInfo info) {
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//    }

//    @Inject(
//        method = {"render"},
//        at = {@At(
//    value = "INVOKE",
//    target = "Lcom/mojang/blaze3d/platform/GlStateManager;_clear(IZ)V",
//    shift = Shift.AFTER
//)}
//    )
//    private void clearColorAfterBackgroundRenderingDrippy(DrawContext graphics, int p_282704_, int p_283650_, float p_283394_, CallbackInfo info) {
//        graphics.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
//    }
//
//    @Inject(
//        method = {"render"},
//        at = {@At(
//    value = "INVOKE",
//    target = "Lcom/mojang/blaze3d/systems/RenderSystem;disableBlend()V",
//    shift = Shift.AFTER
//)}
//    )
//    private void renderCustomizableInstanceOfLogo(DrawContext graphics, int p_96179_, int p_96180_, float p_96181_, CallbackInfo info) {
//        if (drippyOverlayHandler != null && drippyOverlayHandler.logoItem != null && drippyOverlayScreen != null) {
//            if (!drippyOverlayHandler.logoItem.useOriginalSizeAndPosCalculation) {
//                this.scaleOverlayStart(graphics);
//            }
//
//            drippyOverlayHandler.logoItem.render(graphics, drippyOverlayScreen);
//            this.scaleOverlayEnd(graphics);
//        }
//
//    }

    private void setCustomBackgroundOpacity(float opacity) {
        if (drippyOverlayHandler != null) {
            drippyOverlayHandler.backgroundOpacity = opacity;
        }

    }

    private void setOverlayOpacity(float opacity) {
        if (opacity < 0.02F) {
            opacity = 0.02F;
        }

        if (drippyOverlayHandler != null) {
            List<CustomizationItemBase> l = new ArrayList();
            l.addAll(drippyOverlayHandler.frontRenderItems);
            l.addAll(drippyOverlayHandler.backgroundRenderItems);
            Iterator var3 = l.iterator();

            while(var3.hasNext()) {
                CustomizationItemBase i = (CustomizationItemBase)var3.next();
                i.opacity = opacity;
                if (i.opacity <= 0.02F) {
                    i.visible = false;
                }
            }

            if (drippyOverlayHandler.logoItem != null) {
                drippyOverlayHandler.logoItem.opacity = opacity;
                if (drippyOverlayHandler.logoItem.opacity <= 0.02F) {
                    drippyOverlayHandler.logoItem.hidden = true;
                }
            }

            if (drippyOverlayHandler.progressBarItem != null) {
                drippyOverlayHandler.progressBarItem.opacity = opacity;
                if (drippyOverlayHandler.progressBarItem.opacity <= 0.02F) {
                    drippyOverlayHandler.progressBarItem.hidden = true;
                }
            }
        }

    }

//    private void loadFonts() {
//        try {
//            MixinCache.gameThreadRunnables.add(() -> {
//                try {
//                    FontManager fontManager = ((IMixinMinecraft)MinecraftClient.getInstance()).getFontManagerDrippy();
//                    fontManager.reload((FontManager.ProviderIndex)fontManager.loadIndex(MinecraftClient.getInstance().getResourceManager(), Util.getMainWorkerExecutor()).get(), DummyProfiler.INSTANCE);
//                } catch (Exception var1) {
//                    LOGGER.info("[DRIPPY LOADING SCREEN] Failed to load fonts!");
//                    var1.printStackTrace();
//                }
//
//            });
//        } catch (Exception var2) {
//            var2.printStackTrace();
//        }
//
//    }

    private void handleInitOverlay() {
        try {
            CustomizationItemContainer tickerItem = CustomizationItemRegistry.getItem("fancymenu_customization_item_ticker");
            if (tickerItem != null) {
                ((TickerCustomizationItemContainer)tickerItem).onClientTick(new ClientTickEvent.Post());
            }

            int screenWidth = MinecraftClient.getInstance().getWindow().getScaledWidth();
            int screenHeight = MinecraftClient.getInstance().getWindow().getScaledHeight();
            if (drippyOverlayScreen == null) {
                drippyOverlayScreen = new DrippyOverlayScreen();
                MenuHandlerBase b = MenuHandlerRegistry.getHandlerFor(drippyOverlayScreen);
                if (b != null) {
                    Map<String, MenuHandlerBase> m = this.getMenuHandlerRegistryMap();
                    if (m != null) {
                        m.remove(DrippyOverlayScreen.class.getName());
                    }
                }

                MenuHandlerBase c = new DrippyOverlayMenuHandler();
                MenuHandlerRegistry.registerHandler(c);
                drippyOverlayHandler = (DrippyOverlayMenuHandler)c;
                this.initOverlay(screenWidth, screenHeight);
                this.lastScreenWidth = screenWidth;
                this.lastScreenHeight = screenHeight;
            }

            if (screenWidth != this.lastScreenWidth || screenHeight != this.lastScreenHeight) {
                this.initOverlay(screenWidth, screenHeight);
            }

            this.lastScreenWidth = screenWidth;
            this.lastScreenHeight = screenHeight;
        } catch (Exception var6) {
            var6.printStackTrace();
        }

    }

    private @Nullable Map<String, MenuHandlerBase> getMenuHandlerRegistryMap() {
        try {
            Field f = MenuHandlerRegistry.class.getDeclaredField("handlers");
            f.setAccessible(true);
            return (Map)f.get(MenuHandlerRegistry.class);
        } catch (Exception var2) {
            var2.printStackTrace();
            return null;
        }
    }

    private void initOverlay(int screenWidth, int screenHeight) {
        this.runMenuHandlerTask(() -> {
            try {
                drippyOverlayScreen.width = screenWidth;
                drippyOverlayScreen.height = screenHeight;
                double oriScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
                drippyOverlayHandler.onInitPre(new InitOrResizeScreenEvent.Pre(drippyOverlayScreen));
                drippyOverlayHandler.onButtonsCached(new ButtonCachedEvent(drippyOverlayScreen, new ArrayList(), false));
                this.renderScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
                MinecraftClient.getInstance().getWindow().setScaleFactor(oriScale);
            } catch (Exception var5) {
                var5.printStackTrace();
            }

        });
    }

    private void scaleOverlayStart(DrawContext graphics) {
        this.overlayScaled = true;
        double guiScale = MinecraftClient.getInstance().getWindow().getScaleFactor();
        float scale = (float)(1.0 * (1.0 / guiScale) * this.renderScale);
        if (drippyOverlayHandler != null) {
            List<CustomizationItemBase> l = new ArrayList();
            l.addAll(drippyOverlayHandler.frontRenderItems);
            l.addAll(drippyOverlayHandler.backgroundRenderItems);

            CustomizationItemBase i;
            for(Iterator var6 = l.iterator(); var6.hasNext(); i.customGuiScale = (float)this.renderScale) {
                i = (CustomizationItemBase)var6.next();
            }
        }

        graphics.getMatrices().push();
        graphics.getMatrices().scale(scale, scale, scale);
    }

    private void scaleOverlayEnd(DrawContext graphics) {
        if (this.overlayScaled) {
            graphics.getMatrices().pop();
            this.overlayScaled = false;
        }

    }

    private void runMenuHandlerTask(Runnable run) {
        try {
            boolean gameIntroDisplayed = GameIntroHandler.introDisplayed;
            GameIntroHandler.introDisplayed = true;
            MenuHandlerBase menuHandler = MenuHandlerRegistry.getLastActiveHandler();
            MenuHandlerRegistry.setActiveHandler(DrippyOverlayScreen.class.getName());
            boolean allowCustomizations = MenuCustomization.allowScreenCustomization;
            MenuCustomization.allowScreenCustomization = true;
            boolean animationsReady = AnimationHandler.isReady();
            AnimationHandler.setReady(true);
            Screen s = MinecraftClient.getInstance().currentScreen;
            if (s == null || !(s instanceof DrippyOverlayScreen)) {
                MinecraftClient.getInstance().currentScreen = drippyOverlayScreen;
                run.run();
                MinecraftClient.getInstance().currentScreen = s;
            }

            GameIntroHandler.introDisplayed = gameIntroDisplayed;
            MenuCustomization.allowScreenCustomization = allowCustomizations;
            AnimationHandler.setReady(animationsReady);
            if (menuHandler != null) {
                MenuHandlerRegistry.setActiveHandler(menuHandler.getMenuIdentifier());
            }
        } catch (Exception var7) {
            var7.printStackTrace();
        }

    }

    static {
        CHECKED_FOR_OLD_LAYOUTS_FILE = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/.checked_for_old_layouts");
        LEGACY_LAYOUT_DIR = new File(DrippyLoadingScreen.MOD_DIR.getPath(), "/customization");
        initialized = false;
        drippyOverlayScreen = null;
        drippyOverlayHandler = null;
        BACKGROUND_COLOR = () -> {
            return drippyOverlayHandler != null && drippyOverlayHandler.customBackgroundColor != null ? drippyOverlayHandler.customBackgroundColor.getRGB() : IMixinLoadingOverlay.getBrandBackgroundDrippy().getAsInt();
        };
    }
}
