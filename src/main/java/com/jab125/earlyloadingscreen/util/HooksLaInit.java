package com.jab125.earlyloadingscreen.util;

import com.jab125.earlyloadingscreen.special.FabricLoaderImplTransformer;
import com.jab125.earlyloadingscreen.special.MixinProcessorTransformer;
import net.bytebuddy.agent.ByteBuddyAgent;
import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.launch.knot.Knot;
import net.minecraftforge.unsafe.UnsafeHacks;

import java.io.InputStream;
import java.lang.instrument.UnmodifiableClassException;
import java.lang.reflect.Method;

public class HooksLaInit implements LanguageAdapter {
    public static boolean failed = false;
    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        throw new Error();
    }

    static {
        //if ("true".equals(System.getProperty("springboard"))) {
        try {
            //if (true) throw new RuntimeException();
            ByteBuddyAgent.install();
            try {
                InputStream resourceAsStream = HooksLaInit.class.getClassLoader().getResourceAsStream("com/jab125/earlyloadingscreen/needed/Hooks.class");
                byte[] bytes = resourceAsStream.readAllBytes();
                final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                UnsafeHacks.setAccessible(defineClass);
                defineClass.invoke(
                        Knot.class.getClassLoader(),
                        "com.jab125.earlyloadingscreen.needed.Hooks",
                        bytes,
                        0,
                        bytes.length
                );
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            try {
                InputStream resourceAsStream = HooksLaInit.class.getClassLoader().getResourceAsStream("com/jab125/earlyloadingscreen/needed/Hooks$MeterDelegate.class");
                byte[] bytes = resourceAsStream.readAllBytes();
                final Method defineClass = ClassLoader.class.getDeclaredMethod("defineClass", String.class, byte[].class, int.class, int.class);
                UnsafeHacks.setAccessible(defineClass);
                defineClass.invoke(
                        Knot.class.getClassLoader(),
                        "com.jab125.earlyloadingscreen.needed.Hooks$MeterDelegate",
                        bytes,
                        0,
                        bytes.length
                );
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
            ByteBuddyAgent.getInstrumentation().addTransformer(new MixinProcessorTransformer(), true);
            ByteBuddyAgent.getInstrumentation().addTransformer(new FabricLoaderImplTransformer(), true);
            try {
                ByteBuddyAgent.getInstrumentation().retransformClasses(FabricLoaderImpl.class);
            } catch (UnmodifiableClassException e) {
                throw new RuntimeException(e);
            }
        } catch (Throwable t) {
            failed = true;
            t.printStackTrace();
        }
    }
}
