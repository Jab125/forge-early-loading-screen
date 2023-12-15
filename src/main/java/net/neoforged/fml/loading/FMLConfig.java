/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.loading;

import java.util.List;

public class FMLConfig {
    public static int getIntConfigValue(int i) {
        return switch (i) {
            case ConfigValue.EARLY_WINDOW_HEIGHT -> 480;
            case ConfigValue.EARLY_WINDOW_WIDTH -> 854;
            case ConfigValue.EARLY_WINDOW_FBSCALE -> 1;
            default -> 0;
        };
    }

    public static String getConfigValue(int i) {
        return switch (i) {
            case ConfigValue.EARLY_WINDOW_PROVIDER -> "fmlearlywindow";
            default -> "";
        };
    }

    public static <T> List<T> getListConfigValue(int i) {
        return List.of();
    }

    public static boolean getBoolConfigValue(int i) {
        return switch (i) {
            case ConfigValue.EARLY_WINDOW_CONTROL -> true;
            default -> false;
        };
    }

    public static void updateConfig(Object o, Object i) {

    }

    public static class ConfigValue {
        public static final int EARLY_WINDOW_SKIP_GL_VERSIONS = 209;
        public static final int EARLY_WINDOW_MAXIMIZED = 15;
        public static final int EARLY_WINDOW_SQUIR = 29087;
        public static final int EARLY_WINDOW_WIDTH = 29080;
        public static final int EARLY_WINDOW_HEIGHT = 2098;
        public static final int EARLY_WINDOW_FBSCALE = 20839;
        public static final int EARLY_WINDOW_PROVIDER = 230984923;
        public static final int EARLY_WINDOW_CONTROL = 292783;
    }
}