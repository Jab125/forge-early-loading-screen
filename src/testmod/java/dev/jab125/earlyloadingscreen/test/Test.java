package dev.jab125.earlyloadingscreen.test;

import net.fabricmc.loader.api.entrypoint.PreLaunchEntrypoint;
import net.neoforged.fml.loading.progress.ProgressMeter;
import net.neoforged.fml.loading.progress.StartupNotificationManager;

public class Test implements PreLaunchEntrypoint {
    @Override
    public void onPreLaunch() {
        System.out.println("It's alive!");
        long l = System.currentTimeMillis() + 5000;
        ProgressMeter earlyLoadingScreenSetup = StartupNotificationManager.addProgressBar("Example Mod Setup", 5000);
        while(System.currentTimeMillis() < l) {
            earlyLoadingScreenSetup.setAbsolute(5000 - (int)(l - System.currentTimeMillis()));
            earlyLoadingScreenSetup.label(String.format("Example Mod Setup %.2f%% completed", earlyLoadingScreenSetup.progress() * 100));
        }
        earlyLoadingScreenSetup.setAbsolute(5000);
        earlyLoadingScreenSetup.complete();
    }
}
