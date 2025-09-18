package top.syshub.k_Explosion;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.AdviceAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.security.ProtectionDomain;

public class ExplosionTransformer implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain, byte[] classfileBuffer) {

        if (className.equals("net/minecraft/world/level/ServerExplosion")) return modifyClass(classfileBuffer);
        return null;
    }

    private byte[] modifyClass(byte[] classBytes) {
        ClassReader reader = new ClassReader(classBytes);
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new ExplosionClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);
        return writer.toByteArray();
    }
}

class ExplosionClassVisitor extends ClassVisitor {

    public ExplosionClassVisitor(ClassVisitor cv) {
        super(Opcodes.ASM9, cv);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);

        if (name.equals("shouldAffectBlocklikeEntities") && descriptor.equals("()Z")) return new ExplosionMethodVisitor(mv, access, name, descriptor);
        return mv;
    }
}

class ExplosionMethodVisitor extends AdviceAdapter {
    public ExplosionMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM9, mv, access, name, desc);
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (opcode == IRETURN) {
            int resultLocal = newLocal(Type.BOOLEAN_TYPE);

            mv.visitVarInsn(ISTORE, resultLocal);

            Label skip = new Label();
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "net/minecraft/world/level/ServerExplosion", "source", "Lnet/minecraft/world/entity/Entity;");
            mv.visitJumpInsn(IFNULL, skip);

            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, "net/minecraft/world/level/ServerExplosion", "source", "Lnet/minecraft/world/entity/Entity;");
            mv.visitMethodInsn(INVOKEVIRTUAL, "net/minecraft/world/entity/Entity", "isInWater", "()Z", false);
            mv.visitJumpInsn(IFEQ, skip);

            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, resultLocal);

            mv.visitLabel(skip);
            mv.visitVarInsn(ILOAD, resultLocal);
        }
    }
}