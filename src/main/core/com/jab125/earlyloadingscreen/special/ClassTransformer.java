package com.jab125.earlyloadingscreen.special;

public interface ClassTransformer {
    boolean shouldTransform(String name);

    byte[] transformClass(String className, byte[] in);
}
