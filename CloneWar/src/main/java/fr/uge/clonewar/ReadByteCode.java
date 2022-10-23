package fr.uge.clonewar;

import org.objectweb.asm.*;

import java.io.IOException;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.Arrays;

public class ReadByteCode {

    public static String opcodeToString(int opcode){
        return switch (opcode) {
            case Opcodes.NOP -> "NOP";
            case Opcodes.ACONST_NULL -> "CONST_NULL";
            case Opcodes.ICONST_M1 -> "CONST_M1";
            case Opcodes.ICONST_0 -> "CONST_0";
            case Opcodes.ICONST_1 -> "CONST_1";
            case Opcodes.ICONST_2 -> "CONST_2";
            case Opcodes.ICONST_3 -> "CONST_3";
            case Opcodes.ICONST_4 -> "CONST_4";
            case Opcodes.ICONST_5 -> "CONST_5";
            case Opcodes.LCONST_0 -> "CONST_0";
            case Opcodes.LCONST_1 -> "CONST_1";
            case Opcodes.FCONST_0 -> "CONST_0";
            case Opcodes.FCONST_1 -> "CONST_1";
            case Opcodes.FCONST_2 -> "CONST_2";
            case Opcodes.DCONST_0 -> "CONST_0";
            case Opcodes.DCONST_1 -> "CONST_1";
            case Opcodes.IALOAD -> "LOAD";
            case Opcodes.LALOAD -> "LOAD";
            case Opcodes.FALOAD -> "LOAD";
            case Opcodes.DALOAD -> "LOAD";
            case Opcodes.AALOAD -> "LOAD";
            case Opcodes.BALOAD -> "LOAD";
            case Opcodes.CALOAD -> "LOAD";
            case Opcodes.SALOAD -> "LOAD";
            case Opcodes.IASTORE -> "STORE";
            case Opcodes.LASTORE -> "STORE";
            case Opcodes.FASTORE -> "STORE";
            case Opcodes.DASTORE -> "STORE";
            case Opcodes.AASTORE -> "STORE";
            case Opcodes.BASTORE -> "STORE";
            case Opcodes.CASTORE -> "STORE";
            case Opcodes.SASTORE-> "STORE";
            case Opcodes.POP -> "POP";
            case Opcodes.POP2 -> "POP";
            case Opcodes.DUP -> "DUP";
            case Opcodes.DUP_X1 -> "DUP";
            case Opcodes.DUP_X2 -> "DUP";
            case Opcodes.DUP2 -> "DUP";
            case Opcodes.DUP2_X1 -> "DUP";
            case Opcodes.DUP2_X2 -> "DUP";
            case Opcodes.SWAP -> "SWAP";
            case Opcodes.IADD -> "ADD";
            case Opcodes.LADD -> "ADD";
            case Opcodes.FADD -> "ADD";
            case Opcodes.DADD -> "ADD";
            case Opcodes.ISUB -> "SUB";
            case Opcodes.LSUB -> "SUB";
            case Opcodes.FSUB -> "SUB";
            case Opcodes.DSUB -> "SUB";
            case Opcodes.IMUL -> "MUL";
            case Opcodes.LMUL -> "MUL";
            case Opcodes.FMUL -> "MUL";
            case Opcodes.DMUL -> "MUL";
            case Opcodes.IDIV -> "DIV";
            case Opcodes.LDIV -> "DIV";
            case Opcodes.FDIV -> "DIV";
            case Opcodes.DDIV -> "DIV";
            case Opcodes.IREM -> "REM";
            case Opcodes.LREM -> "REM";
            case Opcodes.FREM -> "REM";
            case Opcodes.DREM -> "REM";
            case Opcodes.INEG -> "NEG";
            case Opcodes.LNEG -> "NEG";
            case Opcodes.FNEG -> "NEG";
            case Opcodes.DNEG -> "NEG";
            case Opcodes.ISHL -> "SHL";
            case Opcodes.LSHL -> "SHL";
            case Opcodes.ISHR -> "SHR";
            case Opcodes.LSHR -> "SHR";
            case Opcodes.IUSHR -> "SHR";
            case Opcodes.LUSHR -> "SHR";
            case Opcodes.IAND -> "AND";
            case Opcodes.LAND -> "AND";
            case Opcodes.IOR -> "OR";
            case Opcodes.LOR -> "OR";
            case Opcodes.IXOR -> "XOR";
            case Opcodes.LXOR -> "XOR";
            case Opcodes.I2L -> "I2L";
            case Opcodes.I2F -> "I2F";
            case Opcodes.I2D -> "I2D";
            case Opcodes.L2I -> "L2I";
            case Opcodes.L2F -> "L2F";
            case Opcodes.L2D -> "L2D";
            case Opcodes.F2I -> "F2I";
            case Opcodes.F2L -> "F2L";
            case Opcodes.F2D -> "F2D";
            case Opcodes.D2I -> "D2I";
            case Opcodes.D2F -> "D2F";
            case Opcodes.D2L -> "D2L";
            case Opcodes.I2B -> "I2B";
            case Opcodes.I2S -> "I2S";
            case Opcodes.LCMP -> "CMP";
            case Opcodes.FCMPL -> "CMP";
            case Opcodes.FCMPG -> "CMP";
            case Opcodes.DCMPL -> "CMP";
            case Opcodes.DCMPG -> "CMP";
            case Opcodes.IRETURN -> "RETURN";
            case Opcodes.LRETURN -> "RETURN";
            case Opcodes.FRETURN -> "RETURN";
            case Opcodes.DRETURN -> "RETURN";
            case Opcodes.ARETURN -> "RETURN";
            case Opcodes.RETURN -> "RETURN";
            case Opcodes.ARRAYLENGTH -> "ARRAYLENGTH";
            case Opcodes.ATHROW -> "THROW";
            case Opcodes.MONITORENTER -> "MONITORENTER";
            case Opcodes.MONITOREXIT -> "MONITOREXIT";
            case Opcodes.INVOKEVIRTUAL -> "INVOKEVIRTUAL";
            case Opcodes.INVOKEDYNAMIC -> "INVOKEDYNAMIC";
            case Opcodes.INVOKEINTERFACE -> "INVOKEINTERFACE";
            case Opcodes.INVOKESPECIAL -> "INVOKESPECIAL";
            case Opcodes.INVOKESTATIC -> "INVOKESTATIC";
            case Opcodes.BIPUSH -> "BIPUSH";
            case Opcodes.SIPUSH -> "SIPUSH";
            case Opcodes.NEWARRAY -> "NEWARRAY";
            case Opcodes.ALOAD -> "LOAD";
            case Opcodes.ASTORE -> "STORE";
            case Opcodes.ILOAD -> "LOAD";
            case Opcodes.LLOAD -> "LOAD";
            case Opcodes.FLOAD -> "LOAD";
            case Opcodes.DLOAD -> "LOAD";
            case Opcodes.ISTORE -> "STORE";
            case Opcodes.LSTORE -> "STORE";
            case Opcodes.FSTORE -> "STORE";
            case Opcodes.DSTORE -> "STORE";
            case Opcodes.RET -> "RET";
            case Opcodes.NEW -> "NEW";
            case Opcodes.ANEWARRAY -> "NEWARRAY";
            case Opcodes.CHECKCAST -> "CHECKCAST";
            case Opcodes.INSTANCEOF -> "INSTANCEOF";
            case Opcodes.GETSTATIC -> "GETSTATIC";
            case Opcodes.PUTSTATIC -> "PUTSTATIC";
            case Opcodes.GETFIELD -> "GETFIELD";
            case Opcodes.PUTFIELD -> "PUTFIELD";
            case Opcodes.IFEQ -> "IFEQ";
            case Opcodes.IFNE -> "IFNE";
            case Opcodes.IFLT -> "IFLT";
            case Opcodes.IFGE -> "IFGE";
            case Opcodes.IFGT -> "IFGT";
            case Opcodes.IFLE -> "IFLE";
            case Opcodes.IF_ICMPEQ -> "IF_ICMPEQ";
            case Opcodes.IF_ICMPNE -> "IF_ICMPNE";
            case Opcodes.IF_ICMPLT -> "IF_ICMPLT";
            case Opcodes.IF_ICMPGE -> "IF_ICMPGE";
            case Opcodes.IF_ICMPGT -> "IF_ICMPGT";
            case Opcodes.IF_ICMPLE -> "IF_ICMPLE";
            case Opcodes.IF_ACMPEQ -> "IF_ACMPEQ";
            case Opcodes.IF_ACMPNE -> "IF_ACMPNE";
            case Opcodes.GOTO -> "GOTO";
            case Opcodes.JSR -> "JSR";
            case Opcodes.IFNULL -> "IFNULL";
            case Opcodes.IFNONNULL -> "IFNONNULL";
            case Opcodes.I2C -> "I2C";
            default -> throw new IllegalStateException("Unexpected value: " + opcode);
        };
    }


    public static void main(String[] args) throws IOException {
        var finder = ModuleFinder.of(Path.of("simd.jar"));
        var moduleReference = finder.findAll().stream().findFirst().orElseThrow();

        try(var reader = moduleReference.open()) {
            for(var filename: (Iterable<String>) reader.list()::iterator) {
                if (!filename.endsWith(".class")) {
                    continue;
                }
                System.out.println(filename);
                try(var inputStream = reader.open(filename).orElseThrow()) {
                    var classReader = new ClassReader(inputStream);
                    classReader.accept(new ClassVisitor(Opcodes.ASM9) {

                        private static String modifier(int access) {
                            if (Modifier.isPublic(access)) {
                                return "public";
                            }
                            if (Modifier.isPrivate(access)) {
                                return "private";
                            }
                            if (Modifier.isProtected(access)) {
                                return "protected";
                            }
                            return "";
                        }

                        @Override
                        public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                            System.err.println("class " + modifier(access) + " " + name + " " + superName + " " + (interfaces != null? Arrays.toString(interfaces): ""));
                        }

                        @Override
                        public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
                            System.err.println("  component " + name + " " + ClassDesc.ofDescriptor(descriptor).displayName());
                            return null;
                        }

                        @Override
                        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
                            System.err.println("  field " + modifier(access) + " " + name + " " + ClassDesc.ofDescriptor(descriptor).displayName() + " " + signature);
                            return null;
                        }

                        @Override
                        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                            System.err.println("  method " + modifier(access) + " " + name + " " + MethodTypeDesc.ofDescriptor(descriptor).displayDescriptor() + " " + signature);
                            return new MethodVisitor(Opcodes.ASM9) {
                                @Override
                                public void visitInsn(int opcode) {
                                    System.err.println(opcodeToString(opcode));
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    System.err.println(opcodeToString(opcode) + " " + name);
                                }

                                @Override
                                public void visitIntInsn(int opcode, int operand){
                                    System.err.println(opcodeToString(opcode) + " " + operand);
                                }

                                @Override
                                public void visitVarInsn(int opcode, int varIndex){
                                    System.err.println(opcodeToString(opcode) + " " +varIndex);
                                }

                                @Override
                                public void visitTypeInsn(int opcode, String type){
                                    System.err.println(opcodeToString(opcode) + " " +  type);
                                }

                                @Override
                                public void visitFieldInsn(int opcode, String owner, String name, String descriptor){
                                    System.err.println(opcodeToString(opcode) + " " + name);
                                }

                                @Override
                                public void visitJumpInsn(int opcode, Label label){
                                    System.err.println(opcodeToString(opcode));
                                }
                            };
                        }
                    }, 0);
                }
            }
        }
    }
}
