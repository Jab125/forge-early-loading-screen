package com.jab125.earlyloadingscreen.mixin;

import net.minecraft.client.main.Main;
import net.minecraft.server.Bootstrap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.jab125.earlyloadingscreen.Loader.progressWindowTick;

@Mixin(Main.class)
public class MainMixin {
    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/telemetry/events/GameLoadTimesEvent;setBootstrapTime(J)V", shift = At.Shift.AFTER))
    private static void inject2(String[] args, CallbackInfo ci) {
        net.neoforged.fml.loading.BackgroundWaiter.runAndTick(Bootstrap::bootStrap, progressWindowTick);
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/Bootstrap;bootStrap()V"))
    private static void inject3() {
        // Skip the method entirely
    }
}
