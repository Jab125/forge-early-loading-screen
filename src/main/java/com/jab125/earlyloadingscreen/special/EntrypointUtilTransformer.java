package com.jab125.earlyloadingscreen.special;

import net.fabricmc.loader.impl.entrypoint.EntrypointUtils;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import springboard.tweak.classloader.ClassTransformer;

import java.util.Arrays;
import java.util.Optional;

public class EntrypointUtilTransformer implements ClassTransformer {
    @Override
    public boolean shouldTransform(String name) {
       // System.out.println(name);
        return "net.fabricmc.loader.impl.entrypoint.EntrypointUtils".equals(name);
    }

    @Override
    public byte[] transformClass(String className, byte[] in) {
        ClassReader classReader = new ClassReader(in);
        ClassNode node = new ClassNode(Opcodes.ASM9);
        classReader.accept(node, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);

        {
            Optional<MethodNode> g = node.methods.stream().filter(a -> "invoke".equals(a.name)).findFirst();
            MethodNode methodNode = g.orElseThrow(NullPointerException::new); // INVOKEVIRTUAL net/fabricmc/loader/impl/discovery/ModDiscoverer.addCandidateFinder (Lnet/fabricmc/loader/impl/discovery/ModCandidateFinder;)V

            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "entrypoint", "(Ljava/lang/String;)V"));
            methodNode.instructions.insert(list);
        }

        {
            Optional<MethodNode> g = node.methods.stream().filter(a -> "invoke0".equals(a.name)).findFirst();
            MethodNode methodNode = g.orElseThrow(NullPointerException::new); // INVOKEVIRTUAL net/fabricmc/loader/impl/discovery/ModDiscoverer.addCandidateFinder (Lnet/fabricmc/loader/impl/discovery/ModCandidateFinder;)V
            {
                AbstractInsnNode rip = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a instanceof VarInsnNode node1 && node1.var == 4).findFirst().orElseThrow();
                InsnList list = new InsnList();
                list.add(new VarInsnNode(Opcodes.ALOAD, 0));
                list.add(new VarInsnNode(Opcodes.ALOAD, 4));
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "setup", "(Ljava/lang/String;Ljava/util/Collection;)V"));
                methodNode.instructions.insert(rip, list);
            }
            AbstractInsnNode r = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a instanceof VarInsnNode n && n.var == 2).findFirst().orElseThrow();
            InsnList list = new InsnList();
            list.add(new VarInsnNode(Opcodes.ALOAD, 4));
            list.add(new VarInsnNode(Opcodes.ALOAD, 6));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "ran", "(Ljava/util/Collection;Lnet/fabricmc/loader/api/entrypoint/EntrypointContainer;)V"));
            methodNode.instructions.insert(r, list);
        }
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        node.accept(classWriter);
        return classWriter.toByteArray();
    }
}
