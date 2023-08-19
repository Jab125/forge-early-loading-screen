package com.jab125.earlyloadingscreen.mixin;

import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SplashOverlay.class)
public interface SplashOverlayAccessor {
    @Accessor
    ResourceReload getReload();
}
