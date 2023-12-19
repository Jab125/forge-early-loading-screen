package com.jab125.earlyloadingscreen.special;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Optional;

import static org.objectweb.asm.Opcodes.*;

public class EntrypointUtilTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals("net/fabricmc/loader/impl/FabricLoaderImpl")) return null;
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassNode node = new ClassNode(Opcodes.ASM9);
        classReader.accept(node, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);

        {
            Optional<MethodNode> g = node.methods.stream().filter(a -> "invokeEntrypoints".equals(a.name)).findFirst();
            MethodNode methodNode = g.orElseThrow(NullPointerException::new); // INVOKEVIRTUAL net/fabricmc/loader/impl/discovery/ModDiscoverer.addCandidateFinder (Lnet/fabricmc/loader/impl/discovery/ModCandidateFinder;)V

            {
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "entrypoint", "(Ljava/lang/String;)V"));
                methodNode.instructions.insert(list);
            }

            {
                AbstractInsnNode rip = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a instanceof VarInsnNode node1 && node1.var == 5).findFirst().orElseThrow();
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 1));
                list.add(new VarInsnNode(ALOAD, 5));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "setup", "(Ljava/lang/String;Ljava/util/Collection;)V"));
                methodNode.instructions.insert(rip, list);
            }
            try {
                AbstractInsnNode aload8 = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a instanceof VarInsnNode n && n.var == 8 && n.getOpcode() == ALOAD).findFirst().orElseThrow();
                methodNode.instructions.insertBefore(aload8, new VarInsnNode(ALOAD, 7));
                methodNode.instructions.insertBefore(aload8, new VarInsnNode(ALOAD, 8));
                methodNode.instructions.insertBefore(aload8, new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "modLoadingError", "(Lnet/fabricmc/loader/api/entrypoint/EntrypointContainer;Ljava/lang/Throwable;)V"));
                methodNode.instructions.insertBefore(aload8, new InsnNode(RETURN));
                int i = methodNode.instructions.indexOf(aload8);
                AbstractInsnNode astore4 = Arrays.stream(methodNode.instructions.toArray()).filter(a -> methodNode.instructions.indexOf(a) > i && a instanceof VarInsnNode n && n.var == 4 && n.getOpcode() == ASTORE).findFirst().orElseThrow();
                System.out.println("Try remove");
                while (methodNode.instructions.get(i) != astore4) {
                    System.out.println("Removed " + methodNode.instructions.get(i));
                    methodNode.instructions.remove(methodNode.instructions.get(i));
                }
                System.out.println("Removed " + methodNode.instructions.get(i));
                methodNode.instructions.remove(methodNode.instructions.get(i));
            } catch (Throwable t) {
                t.printStackTrace();
            }
            {
                AbstractInsnNode r = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a instanceof VarInsnNode n && n.var == 3).findFirst().orElseThrow();
                InsnList list = new InsnList();
                list.add(new VarInsnNode(ALOAD, 5));
                list.add(new VarInsnNode(ALOAD, 7));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "ran", "(Ljava/util/Collection;Lnet/fabricmc/loader/api/entrypoint/EntrypointContainer;)V"));
                methodNode.instructions.insert(r, list);
            }
            {
                InsnList list = new InsnList();
                LabelNode label = new LabelNode();
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "entrypointsDisabled", "()Z"));
                list.add(new JumpInsnNode(Opcodes.IFEQ, label));
                list.add(new InsnNode(RETURN));
                list.add(label);
                methodNode.instructions.insert(list);
            }
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        node.accept(classWriter);
        try {
            Files.write(Path.of("FabricLoaderImpl.class"), classWriter.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classWriter.toByteArray();
    }
}
