package com.jab125.earlyloadingscreen;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Map;

public class SafeUtils {
    public static boolean isErrored() {
        return FabricLoader.getInstance().getObjectShare().get("dd:fjkdj") instanceof Map;
    }
}
