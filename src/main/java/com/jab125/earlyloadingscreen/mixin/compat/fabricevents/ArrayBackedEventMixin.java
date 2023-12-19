package com.jab125.earlyloadingscreen.mixin.compat.fabricevents;

import com.jab125.earlyloadingscreen.SafeUtils;
import com.jab125.earlyloadingscreen.util.LeSpecial;
import net.fabricmc.fabric.api.event.Event;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Function;

@Mixin(targets = "net.fabricmc.fabric.impl.base.event.ArrayBackedEvent", remap = false)
public abstract class ArrayBackedEventMixin<T> extends Event<T> implements LeSpecial<T> {
    @Shadow protected abstract void rebuildInvoker(int newLength);

    @Shadow private T[] handlers;
    @Mutable
    @Unique
    private @Final Class clazz;

    @Inject(at = @At("TAIL"), method = "<init>")
    private void init(Class type, Function invokerFactory, CallbackInfo ci) {
        clazz = type;
    }

    @Override
    public T alskdjj$jdslfjk$dkljsk() {
        handlers = (T[]) Array.newInstance(clazz, 0);
        rebuildInvoker(0);
        return invoker;
    }

    @Override
    public boolean djkl$asjklh$ashj() {
        return SafeUtils.isErrored();
    }
}
