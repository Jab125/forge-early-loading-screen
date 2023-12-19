package com.jab125.earlyloadingscreen.needed;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.entrypoint.EntrypointStorage;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Hooks {
    public static Consumer<String> stringConsumer = s -> {};
    public static BiConsumer<String, MeterDelegate> onCreate = (s, b) -> {};
    public static BiConsumer<MeterDelegate, String> stringBiConsumer = (s, b) -> {};
    private static final HashMap<Collection<EntrypointContainer<?>>, MeterDelegate> map = new HashMap<>();
    private static boolean hookSetup;
    public static Map<ModContainer, List<Throwable>> errors = new HashMap<>();
    public static void entrypoint(String s) {
        setupHooks();
        if (!errors.isEmpty()) return;
        stringConsumer.accept("Running entrypoint: " + s);
    }

    public static void mixinInit() {
        setupHooks();
        if (!errors.isEmpty()) return;
        stringConsumer.accept("Setting up mixin configs.");
    }

    public static void mixinPostInit() {
      //  setupHooks();
        //stringConsumer.accept("Finished setting up mixin configs.");
    }

    private static void setupHooks() {
        if (hookSetup) { return; }
        try {
            System.out.println("Setting up hooks");
            FabricLoader.getInstance().getEntrypoints("early-loading-screen-hook-provider", Runnable.class).get(0).run();
            hookSetup = true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void setup(String s2, Collection<EntrypointContainer<?>> entrypointContainers) {
        setupHooks();
        try {
            Class<MeterDelegate> aClass = (Class<MeterDelegate>) FabricLoader.class.getClassLoader().loadClass("com.jab125.earlyloadingscreen.needed.Hooks$MeterDelegate");
            System.out.println(aClass.getClassLoader());
            MeterDelegate s = aClass.getConstructor(int.class).newInstance(entrypointContainers.size());
            map.put(entrypointContainers, s);
            if (onCreate != null) {
                onCreate.accept(s2, s);
            }
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | IllegalAccessException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static void ran(Collection<EntrypointContainer<?>> entrypointContainers, EntrypointContainer<?> container) {
        setupHooks();
        MeterDelegate meterDelegate = map.get(entrypointContainers);
        try {
            Object getProvider = container.getClass().getMethod("getProvider").invoke(container);
            Object getMetadata = getProvider.getClass().getMethod("getMetadata").invoke(getProvider);
            Method getName = getMetadata.getClass().getDeclaredMethod("getName");
            getName.setAccessible(true);
            meterDelegate.update((String) getName.invoke(getMetadata));
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
       // meterDelegate.update(container.getProvider().getMetadata().getName());
    }

    // we copy forge lol
    public static void modLoadingError(EntrypointContainer<?> d, Throwable t) throws IllegalAccessException, NoSuchFieldException {
        setupHooks();
        System.out.println("mod loading error: " + t);
        ModContainer c = d.getProvider();
        if (!errors.containsKey(c)) errors.put(c, new ArrayList<>());
        errors.get(c).add(t);
        FabricLoader.getInstance().getObjectShare().put("dd:fjkdj", errors);
        stringConsumer.accept("ERROR DURING MOD LOADING");
        FabricLoaderImpl instance = FabricLoaderImpl.INSTANCE;
        Field entrypointStorage = FabricLoaderImpl.class.getDeclaredField("entrypointStorage");
        entrypointStorage.setAccessible(true);
        EntrypointStorage storage = (EntrypointStorage) entrypointStorage.get(instance);
        Field entryMap = EntrypointStorage.class.getDeclaredField("entryMap");
        entryMap.setAccessible(true);
        Map<String, List<?>> map = (Map<String, List<?>>) entryMap.get(storage);
        map.clear();
        System.out.println(map);
    }

    public static class MeterDelegate {

        public final int size;

        public MeterDelegate(int size) {
            this.size = size;
        }
        public void update(String modName) {
            if (stringBiConsumer != null) {
                stringBiConsumer.accept(this, modName);
            }
        }
    }
}
