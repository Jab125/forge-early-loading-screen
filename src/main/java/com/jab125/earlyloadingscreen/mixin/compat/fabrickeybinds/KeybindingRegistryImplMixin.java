package com.jab125.earlyloadingscreen.mixin.compat.fabrickeybinds;

import com.jab125.earlyloadingscreen.SafeUtils;
import net.fabricmc.fabric.impl.client.keybinding.KeyBindingRegistryImpl;
import net.minecraft.client.option.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(value = KeyBindingRegistryImpl.class, remap = false)
public class KeybindingRegistryImplMixin {
    @Inject(method = "addCategory", at = @At("HEAD"), cancellable = true)
    private static void addCategory(String categoryTranslationKey, CallbackInfoReturnable<Boolean> cir) {
        if (SafeUtils.isErrored()) cir.setReturnValue(true);
    }
    @Inject(method = "registerKeyBinding", at = @At("HEAD"), cancellable = true)
    private static void registerKeyBinding(KeyBinding binding, CallbackInfoReturnable<KeyBinding> cir) {
        if (SafeUtils.isErrored()) cir.setReturnValue(binding);
    }
}
