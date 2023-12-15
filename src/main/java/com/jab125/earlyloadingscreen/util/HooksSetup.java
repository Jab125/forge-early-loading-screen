package com.jab125.earlyloadingscreen.util;

import com.jab125.earlyloadingscreen.mixin.MainMixin;
import com.jab125.earlyloadingscreen.needed.Hooks;
import net.fabricmc.loader.api.FabricLoader;
import net.neoforged.fml.StartupMessageManager;
import net.neoforged.fml.loading.ImmediateWindowHandler;
import net.neoforged.fml.loading.progress.ProgressMeter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class HooksSetup {
    private static final HashMap<Object, ProgressMeter> meterManagement = new HashMap<>();
    private static boolean already;
    public static void init(String[] args) {
        if (already) return;
        already = true;
        ArrayList<String> strings = new ArrayList<>(Arrays.stream(args).toList());
        strings.add("--fml.mcVersion");
        strings.add(FabricLoader.getInstance().getModContainer("minecraft").map(a -> a.getMetadata().getVersion().getFriendlyString()).orElse("NONE"));
        strings.add("--fml.neoForgeVersion");
        strings.add(FabricLoader.getInstance().getModContainer("fabricloader").map(a -> a.getMetadata().getVersion().getFriendlyString()).orElse("NONE"));
        ImmediateWindowHandler.load("forgeclient", strings.toArray(new String[0]));
        try { HooksSetup.setupHooks(); } catch (Throwable t) {t.printStackTrace();}
        ImmediateWindowHandler.updateProgress("Launching Minecraft");
        ImmediateWindowHandler.renderTick();
    }
    public static void setupHooks() throws NoSuchFieldException, ClassNotFoundException, IllegalAccessException {
        Class<Hooks> aClass = (Class<Hooks>) FabricLoader.class.getClassLoader().loadClass("com.jab125.earlyloadingscreen.needed.Hooks");
        Field stringConsumer = aClass.getField("stringConsumer");
        Consumer<String> f = StartupMessageManager::addModMessage;
        stringConsumer.set(null, f);
        Field onCreate = aClass.getField("onCreate");
        Field stringBiConsumer = aClass.getField("stringBiConsumer");
        BiConsumer<String, Object> k = (s, meterDelegate) -> {
            int size;
            try {
                size = (int) meterDelegate.getClass().getField("size").get(meterDelegate);
            } catch (IllegalAccessException | NoSuchFieldException e) {
                throw new RuntimeException(e);
            }
            meterManagement.put(meterDelegate, StartupMessageManager.addProgressBar(s, size));
        };
        onCreate.set(null, k);
        BiConsumer<Object, String> h = (o, s) -> {
            meterManagement.get(o).label(meterManagement.get(o).originalName + ": " + s);
            meterManagement.get(o).increment();
            if (meterManagement.get(o).current() == meterManagement.get(o).steps())
                meterManagement.get(o).complete();
        };
        stringBiConsumer.set(null, h);

    }
}
