package com.jab125.earlyloadingscreen.util;

import net.fabricmc.loader.api.FabricLoader;

public class Provider implements Runnable {
    @Override
    public void run() {
        HooksSetup.init(FabricLoader.getInstance().getLaunchArguments(true));
    }
}
