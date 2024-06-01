/*
 * Copyright (c) Forge Development LLC and contributors
 * SPDX-License-Identifier: LGPL-2.1-only
 */

package net.neoforged.fml.loading;

import java.nio.file.Path;

import net.fabricmc.loader.api.FabricLoader;

public enum FMLPaths {
    GAMEDIR(FabricLoader.getInstance().getGameDir());
    private Path absolutePath;

    FMLPaths(Path path) {
        this.absolutePath = path;
    }

    public Path get() {
        return absolutePath;
    }
}
