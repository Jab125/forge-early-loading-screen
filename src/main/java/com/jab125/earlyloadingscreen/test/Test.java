package com.jab125.earlyloadingscreen.test;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;

public class Test implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        System.out.println("It's alive!");
    }
}
