package com.jab125.earlyloadingscreen.mixin;

import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.server.packs.resources.ReloadInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(LoadingOverlay.class)
public interface SplashOverlayAccessor {
    @Accessor
    ReloadInstance getReload();
}
