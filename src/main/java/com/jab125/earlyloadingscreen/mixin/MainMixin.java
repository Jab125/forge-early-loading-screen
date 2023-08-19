package com.jab125.earlyloadingscreen.mixin;

import com.jab125.earlyloadingscreen.util.HooksSetup;
import net.minecraft.Bootstrap;
import net.minecraft.client.main.Main;
import net.minecraftforge.fml.loading.ImmediateWindowHandler;
import net.minecraftforge.fml.loading.progress.ProgressMeter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;

import static com.jab125.earlyloadingscreen.Loader.progressWindowTick;

@Mixin(Main.class)
public class MainMixin {
    @Inject(method = "main", at = @At("HEAD"))
    private static void injectMain(String[] args, CallbackInfo ci) {
     //   progressWindowTick.run();
        //while(true)
        //ImmediateWindowHandler.acceptGameLayer(null);
    }

    @Inject(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/util/telemetry/GameLoadTimeEvent;setBootstrapTime(J)V", shift = At.Shift.AFTER))
    private static void inject2(String[] args, CallbackInfo ci) {
        net.minecraftforge.fml.loading.BackgroundWaiter.runAndTick(Bootstrap::initialize/*Bootstrap.initialize()*/, progressWindowTick);
    }

    @Redirect(method = "main", at = @At(value = "INVOKE", target = "Lnet/minecraft/Bootstrap;initialize()V"))
    private static void inject3() {
        //net.minecraftforge.fml.loading.BackgroundWaiter.runAndTick(Bootstrap::initialize/*Bootstrap.initialize()*/, progressWindowTick);
    }
}
