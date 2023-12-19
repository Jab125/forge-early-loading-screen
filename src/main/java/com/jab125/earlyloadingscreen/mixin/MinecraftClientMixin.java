package com.jab125.earlyloadingscreen.mixin;

import com.jab125.earlyloadingscreen.needed.Hooks;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Overlay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.client.resource.ResourceReloadLogger;
import net.minecraft.util.Util;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import net.neoforged.neoforge.client.gui.LoadingErrorScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

    @Unique
    private Overlay overlay;
    @Unique
    private MinecraftClient.LoadingContext context;
    @Shadow public abstract void setOverlay(@Nullable Overlay overlay);
    @Shadow @Nullable private CompletableFuture<Void> resourceReloadFuture;
    @Shadow protected abstract void checkGameData();
    @Shadow @Final private ResourceReloadLogger resourceReloadLogger;
    @Shadow protected abstract void handleResourceReloadException(Throwable throwable, @Nullable MinecraftClient.LoadingContext loadingContext);
    @Shadow protected abstract void onFinishedLoading(@Nullable MinecraftClient.LoadingContext loadingContext);


    @Shadow public abstract void setScreen(@Nullable Screen screen);

    @ModifyVariable(method = "<init>", at = @At("STORE"))
    private MinecraftClient.LoadingContext captureVar(MinecraftClient.LoadingContext context) {
        return this.context = context;
    }
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"))
    private void redirectSetOverlay(MinecraftClient instance, Overlay overlay) {
        ImmediateWindowHandler.acceptGameLayer(null);
        this.overlay = overlay;
        // ClassLoader loader = instance.getClass().getClassLoader();
        // Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        var g = ImmediateWindowHandler.<SplashOverlay>loadingOverlay(() -> this, this::get, this::accept, false).get();
        // Thread.currentThread().setContextClassLoader(loader);
        setOverlay(g);
    }

    @Inject(method = "onInitFinished", at = @At("RETURN"), cancellable = true)
    private void onInitFinished(MinecraftClient.LoadingContext loadingContext, CallbackInfoReturnable<Runnable> cir) {
        if (FabricLoader.getInstance().getObjectShare().get("dd:fjkdj") instanceof Map t) {
            this.setScreen(new LoadingErrorScreen(t, List.of(), Path.of(".").toFile()));
            cir.setReturnValue(() -> {});
        }
    }

    private void accept(Optional<Throwable> error) {
        Util.ifPresentOrElse(error, (throwable) -> {
            this.handleResourceReloadException(throwable, context);
        }, () -> {
            if (SharedConstants.isDevelopment) {
                this.checkGameData();
            }

            this.resourceReloadLogger.finish();
            this.onFinishedLoading(context);
        });
    }

    private Object get() {
        SplashOverlayAccessor overlay2 = (SplashOverlayAccessor) overlay;
        return overlay2.getReload();
    }
}
