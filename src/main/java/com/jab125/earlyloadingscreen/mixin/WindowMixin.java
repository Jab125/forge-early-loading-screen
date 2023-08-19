package com.jab125.earlyloadingscreen.mixin;

import net.minecraft.client.WindowEventHandler;
import net.minecraft.client.WindowSettings;
import net.minecraft.client.util.Monitor;
import net.minecraft.client.util.MonitorTracker;
import net.minecraft.client.util.Window;
import net.minecraftforge.fml.loading.ImmediateWindowHandler;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

// BARF
@Mixin(Window.class)
public abstract class WindowMixin {

    private Monitor monitor;

    @Shadow @Nullable public abstract Monitor getMonitor();

    @Shadow private int width;

    @Shadow private int windowedWidth;

    @Shadow private int height;

    @Shadow private int windowedHeight;

    @Shadow private int x;

    @Shadow private int windowedX;

    @Shadow private int y;

    @Shadow private int windowedY;

    @Shadow private int framebufferHeight;

    @Shadow protected abstract void updateFramebufferSize();

    @Shadow private int framebufferWidth;

    @ModifyVariable(method = "<init>", ordinal = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J", shift = At.Shift.AFTER))
    Monitor monitor(Monitor monitor) {
        if (!ImmediateWindowHandler.positionWindow(Optional.ofNullable(monitor), w -> width = this.windowedWidth = w, h -> this.height = this.windowedHeight = h, x -> this.x = this.windowedX = x, y -> this.y = this.windowedY = y)) {
            return monitor;
        }
        this.monitor = monitor;
        return null;
    }

    @ModifyVariable(method = "<init>", ordinal = 0, at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwMakeContextCurrent(J)V", shift = At.Shift.BEFORE))
    Monitor monitor2(Monitor value) {
        if (this.monitor != null) {
            return monitor;
        }
        return value;
    }
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwCreateWindow(IILjava/lang/CharSequence;JJ)J"))
    long init(int width, int height, CharSequence title, long monitor, long share) {
        return ImmediateWindowHandler.setupMinecraftWindow(() -> width, () -> height, () -> (String) title, () -> monitor);
        // return 0;
    }

    @Inject(method = "updateFramebufferSize", at = @At(value = "TAIL"))
    void updateFramebufferSize(CallbackInfo ci) {
        if (this.framebufferHeight == 0 || this.framebufferWidth == 0) ImmediateWindowHandler.updateFBSize(width -> this.framebufferWidth = width, height -> this.framebufferHeight = height);
    }
}
