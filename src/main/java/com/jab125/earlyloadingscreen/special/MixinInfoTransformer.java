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

public class MixinInfoTransformer implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!className.equals("org/spongepowered/asm/mixin/transformer/MixinInfo")) return null; try{
        ClassReader classReader = new ClassReader(classfileBuffer);
        ClassNode node = new ClassNode(Opcodes.ASM9);
        classReader.accept(node, ClassReader.SKIP_DEBUG + ClassReader.SKIP_FRAMES);

        //org.spongepowered.asm.mixin.transformer.MixinInfo.shouldApplyMixin()
        // shouldApplyMixin(ZLjava/lang/String;)Z
        {
            Optional<MethodNode> g = node.methods.stream().filter(a -> "shouldApplyMixin".equals(a.name)).findFirst();
            MethodNode methodNode = g.orElseThrow(NullPointerException::new);
//            for (AbstractInsnNode instruction : methodNode.instructions) {
//                if (instruction instanceof VarInsnNode varInsnNode) {
//                    if (varInsnNode.var != 0) varInsnNode.var--;
//                }
//            }
            LabelNode labelNode = Arrays.stream(methodNode.instructions.toArray()).filter(a -> a instanceof LabelNode).map(a -> (LabelNode) a).findFirst().orElseThrow();

            InsnList list = new InsnList();
            LabelNode label = new LabelNode();
//            list.add(new LabelNode());
            list.add(new VarInsnNode(Opcodes.ILOAD, 1));
            list.add(new VarInsnNode(Opcodes.ALOAD, 2));
            list.add(new VarInsnNode(Opcodes.ALOAD, 0));
            list.add(new FieldInsnNode(Opcodes.GETFIELD, "org/spongepowered/asm/mixin/transformer/MixinInfo", "className", "Ljava/lang/String;"));
            list.add(new MethodInsnNode(Opcodes.INVOKESTATIC, "com/jab125/earlyloadingscreen/needed/Hooks", "disableMixin", "(ZLjava/lang/String;Ljava/lang/String;)Z"));
            list.add(new JumpInsnNode(Opcodes.IFEQ, label));
            list.add(new InsnNode(Opcodes.ICONST_0));
            list.add(new InsnNode(Opcodes.IRETURN));
            list.add(label);
            methodNode.instructions.insert(list);
//            INVOKESTATIC sandbox.isTrue ()Z
//    IFEQ L1
//    ICONST_0
//    IRETURN
            //MethodInsnNode f = new MethodInsnNode(Opcodes.ILOAD, "com/jab125/earlyloadingscreen/needed/Hooks", "mixinsEnabled", "()Z");
            //methodNode.instructions.insert(insnNode, new InsnNode(Opcodes.IRETURN));
        }


        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES + ClassWriter.COMPUTE_MAXS);
        node.accept(classWriter);
        try {
            Files.write(Path.of("MixinInfo.class"), classWriter.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return classWriter.toByteArray();}catch (Throwable t) {t.printStackTrace();return null;}
    }
}
