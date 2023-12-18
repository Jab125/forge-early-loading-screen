package com.jab125.earlyloadingscreen.special;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MixinProcessorTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals("org/spongepowered/asm/mixin/transformer/MixinProcessor")) return null;
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassNode node = new ClassNode(Opcodes.ASM9);
        classReader.accept(node, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);

        {
            Optional<MethodNode> g = node.methods.stream().filter(a -> "selectConfigs".equals(a.name)).findFirst();
            MethodNode methodNode = g.orElseThrow(NullPointerException::new);
            {
                InsnList list = new InsnList();
                list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "mixinInit", "()V"));
                methodNode.instructions.insert(list);
            }
            {
                List<AbstractInsnNode> abstractInsnNodes = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a.getOpcode() == Opcodes.RETURN).toList();
                for (AbstractInsnNode abstractInsnNode : abstractInsnNodes) {
                    InsnList list = new InsnList();
                    list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "mixinPostInit", "()V"));
                    methodNode.instructions.insertBefore(abstractInsnNode, list);
                }
            }
        }
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        node.accept(classWriter);
        return classWriter.toByteArray();
    }
}
