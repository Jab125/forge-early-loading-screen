package com.jab125.earlyloadingscreen.mixin.compat.fabricevents;

import com.jab125.earlyloadingscreen.util.LeSpecial;
import net.fabricmc.fabric.api.event.Event;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Event.class, remap = false)
public abstract class EventMixin<T> implements LeSpecial<T> {
    @Shadow public abstract T invoker();

    @Inject(method = "invoker", at = @At("HEAD"), cancellable = true)
    private void invoker(CallbackInfoReturnable<T> cir) {
        if (djkl$asjklh$ashj()) cir.setReturnValue(alskdjj$jdslfjk$dkljsk());
    }

    @Override
    public boolean djkl$asjklh$ashj() {
        return false;
    }

    @Override
    public T alskdjj$jdslfjk$dkljsk() {
        return invoker();
    }
}
