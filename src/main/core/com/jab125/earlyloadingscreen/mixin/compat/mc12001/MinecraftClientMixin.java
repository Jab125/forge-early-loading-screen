package com.jab125.earlyloadingscreen.mixin.compat.mc12001;

import com.jab125.earlyloadingscreen.mixin.SplashOverlayAccessor;
import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Unique
    private Overlay overlay;

    @Shadow public abstract void setOverlay(@Nullable Overlay overlay);

    @Shadow @Nullable private CompletableFuture<Void> pendingReload;

    @Dynamic @Shadow protected abstract void method_24226(Throwable throwable);

    @Shadow protected abstract void selfTest();

    @Shadow @Final private ResourceLoadStateTracker reloadStateTracker;

    @Dynamic @Shadow protected abstract void method_51736();

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void redirectSetOverlay(Minecraft instance, Overlay overlay) {
        ImmediateWindowHandler.acceptGameLayer(null);
        this.overlay = overlay;
        // ClassLoader loader = instance.getClass().getClassLoader();
       // Thread.currentThread().setContextClassLoader(ClassLoader.getSystemClassLoader());
        var g = ImmediateWindowHandler.<LoadingOverlay>loadingOverlay(() -> this, this::get, this::accept, false).get();
       // Thread.currentThread().setContextClassLoader(loader);
        setOverlay(g);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Unique
    private void accept(Optional<Throwable> throwable) {
        Util.ifElse(throwable, this::method_24226, () -> {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                this.selfTest();
            }

            this.reloadStateTracker.finishReload();
            this.method_51736();
        });
    }

    @Unique
    private Object get() {
        SplashOverlayAccessor overlay2 = (SplashOverlayAccessor) overlay;
        return overlay2.getReload();
    }
}