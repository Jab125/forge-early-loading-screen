/*
 * Copyright (c) Forge Development LLC and contributors & Jab125
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.minecraftforge.client.loading;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SplashOverlay;
import net.minecraft.resource.ResourceReload;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;
import java.util.function.LongSupplier;
import java.util.function.Supplier;

import org.lwjgl.glfw.GLFW;

public final class NoVizFallback {
    private static long WINDOW;
    public static LongSupplier windowHandoff(IntSupplier width, IntSupplier height, Supplier<String> title, LongSupplier monitor) {
        return () -> WINDOW = GLFW.glfwCreateWindow(width.getAsInt(), height.getAsInt(), title.get(), monitor.getAsLong(), 0L);
    }

    public static Supplier<SplashOverlay> loadingOverlay(Supplier<MinecraftClient> mc, Supplier<ResourceReload> ri, Consumer<Optional<Throwable>> ex, boolean fadein) {
        return () -> new SplashOverlay(mc.get(), ri.get(), ex, fadein);
    }

    public static Boolean windowPositioning(Optional<Object> monitor, IntConsumer widthSetter, IntConsumer heightSetter, IntConsumer xSetter, IntConsumer ySetter) {
        return Boolean.FALSE;
    }

    public static String glVersion() {
        if (WINDOW != 0) {
            var maj = GLFW.glfwGetWindowAttrib(WINDOW, GLFW.GLFW_CONTEXT_VERSION_MAJOR);
            var min = GLFW.glfwGetWindowAttrib(WINDOW, GLFW.GLFW_CONTEXT_VERSION_MINOR);
            return maj+"."+min;
        } else {
            return "3.2";
        }
    }
}