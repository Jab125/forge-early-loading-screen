package com.jab125.earlyloadingscreen.util;

import net.fabricmc.loader.api.LanguageAdapter;
import net.fabricmc.loader.api.LanguageAdapterException;
import net.fabricmc.loader.api.ModContainer;
import net.minecraftforge.fml.earlydisplay.DisplayWindow;
import springboard.init.Init;

public class HooksLaInit implements LanguageAdapter {
    @Override
    public <T> T create(ModContainer mod, String value, Class<T> type) throws LanguageAdapterException {
        return Init.throwsUncheckedReturn(new Throwable());
    }

    static {
        if ("true".equals(System.getProperty("springboard"))) {
            // can't do init here because lwjgl isn't in classpath :/
        }
    }
}
