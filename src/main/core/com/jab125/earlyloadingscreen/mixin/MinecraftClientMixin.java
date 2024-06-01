package com.jab125.earlyloadingscreen.mixin;

import net.minecraft.SharedConstants;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Unique
    private Overlay overlay;
    @Unique
    private Minecraft.GameLoadCookie context;
    @Shadow public abstract void setOverlay(@Nullable Overlay overlay);
    @Shadow @Nullable private CompletableFuture<Void> pendingReload;
    @Shadow protected abstract void selfTest();
    @Shadow @Final private ResourceLoadStateTracker reloadStateTracker;
    @Shadow protected abstract void rollbackResourcePacks(Throwable throwable, @Nullable Minecraft.GameLoadCookie loadingContext);
    @Shadow protected abstract void onResourceLoadFinished(@Nullable Minecraft.GameLoadCookie loadingContext);


    @SuppressWarnings("InvalidInjectorMethodSignature")
    @ModifyVariable(method = "<init>", at = @At("STORE"))
    private Minecraft.GameLoadCookie captureVar(Minecraft.GameLoadCookie context) {
        return this.context = context;
    }
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;setOverlay(Lnet/minecraft/client/gui/screens/Overlay;)V"))
    private void redirectSetOverlay(Minecraft instance, Overlay overlay) {
        ImmediateWindowHandler.acceptGameLayer(null);
        this.overlay = overlay;
        var g = ImmediateWindowHandler.<LoadingOverlay>loadingOverlay(() -> this, this::get, this::accept, false).get();
        setOverlay(g);
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    @Unique
    private void accept(Optional<Throwable> error) {
        Util.ifElse(error, (throwable) -> {
            this.rollbackResourcePacks(throwable, context);
        }, () -> {
            if (SharedConstants.IS_RUNNING_IN_IDE) {
                this.selfTest();
            }

            this.reloadStateTracker.finishReload();
            this.onResourceLoadFinished(context);
        });
    }

    @Unique
    private Object get() {
        SplashOverlayAccessor overlay2 = (SplashOverlayAccessor) overlay;
        return overlay2.getReload();
    }
}
