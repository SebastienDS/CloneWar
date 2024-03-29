package fr.uge.clonewar;


import org.objectweb.asm.*;

import java.io.*;
import java.lang.module.ModuleFinder;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadByteCode {

  private record Tuple(int line, String opcode) {}

  private final HashMap<String, TreeMap<Integer, List<String>>> files = new HashMap<>();
  private int line;
  private final Path jar;

  public ReadByteCode(Path jar) {
    Objects.requireNonNull(jar);
    this.jar = jar;
  }

  /**
   * Performs an action for each element of analyzed instructions.
   * @param consumer The action to perform on the instruction
   */
  public void forEach(BiConsumer<? super String, ? super Instruction> consumer) {
    Objects.requireNonNull(consumer);
    forEachIterator((f, iterator) ->
        consumeInstructions(iterator, instruction -> consumer.accept(f, instruction)));
  }

  private void forEachIterator(BiConsumer<? super String, ? super Iterator<ReadByteCode.Tuple>> consumer) {
    Objects.requireNonNull(consumer);
    stream()
        .forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
  }

  private Stream<Map.Entry<String, Iterator<ReadByteCode.Tuple>>> stream() {
    return files.entrySet()
        .stream()
        .map(entry -> Map.entry(entry.getKey(), getInstructionsIterator(entry.getValue())));
  }

  private static Iterator<ReadByteCode.Tuple> getInstructionsIterator(TreeMap<Integer, List<String>> instructions) {
    return instructions.entrySet()
        .stream()
        .flatMap(entry -> entry.getValue().stream()
            .map(value -> new Tuple(entry.getKey(), value)))
        .iterator();
  }

  @Override
  public String toString() {
    return files.entrySet()
        .stream()
        .map(entry -> entry.getKey() + " : " + entry.getValue())
        .collect(Collectors.joining("\n"));
  }

  /**
   * Performs an analysis over the main jar to extract instructions.
   * @param javaFiles The java files used to match with right class files
   * @throws IOException if an I/O error occurs
   */
  public void analyze(Set<String> javaFiles) throws IOException {
    Objects.requireNonNull(javaFiles);

    var finder = ModuleFinder.of(jar);
    var moduleReference = finder.findAll().stream().findFirst().orElseThrow();

    try (var reader = moduleReference.open()) {
      var jarFiles = reader.list()
          .filter(f -> f.endsWith(".class"))
          .filter(f -> javaFiles.contains(extractExtension(f).getKey()));
      for (var filename: (Iterable<String>) jarFiles::iterator) {
        try (var inputStream = reader.open(filename).orElseThrow()) {
          var instructions = analyzeByteCode(inputStream);
          files.put(filename, instructions);
        }
      }
    }
  }

  private TreeMap<Integer, List<String>> analyzeByteCode(InputStream inputStream) throws IOException {
    var instructions = new TreeMap<Integer, List<String>>();
    line = 0;

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
        }

        @Override
        public RecordComponentVisitor visitRecordComponent(String name, String descriptor, String signature) {
          return null;
        }

        @Override
        public FieldVisitor visitField(int access, String name, String descriptor, String signature, Object value) {
          return null;
        }

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
          return new MethodVisitor(Opcodes.ASM9) {
            @Override
            public void visitInsn(int opcode) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode));
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode) + " " + name);
            }

            @Override
            public void visitIntInsn(int opcode, int operand) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode) + " " + operand);
            }

            @Override
            public void visitVarInsn(int opcode, int varIndex) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode) + " " + varIndex);
            }

            @Override
            public void visitTypeInsn(int opcode, String type) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode));
            }

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode) + " " + descriptor);
            }

            @Override
            public void visitJumpInsn(int opcode, Label label) {
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(opcode));
            }

            @Override
            public void visitInvokeDynamicInsn(String name, String descriptor, Handle bootstrapMethodHandle,
                                               Object ... bootstrapMethodArguments){
              instructions.computeIfAbsent(line, k -> new ArrayList<>())
                  .add(opcodeToString(Opcodes.INVOKEDYNAMIC) + " " + name);
            }

            @Override
            public void visitLineNumber(int visitedLine, Label label) {
              line = visitedLine;
            }
          };
        }
      },0);
    return instructions;
  }

  private static String opcodeToString(int opcode) {
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

  /**
   * Extracts the extension from the filename.
   * @param filename The filename
   * @return Tuple of filename without extension and the extension
   */
  public static Map.Entry<String, String> extractExtension(String filename) {
    var extensionIndex = filename.indexOf('.');
    var extension = filename.substring(extensionIndex);

    var classNameSeparator = filename.indexOf('$');
    var className = classNameSeparator != -1
        ? filename.substring(0, classNameSeparator)
        : filename.substring(0, extensionIndex);
    return Map.entry(className, extension);
  }

  /**
   * Extract source files from the source jar.
   * @param jar The source jar
   * @return List of Tuple that contains filename linked with content
   * @throws IOException if an I/O error occurs
   */
  public static List<Map.Entry<String, String>> extractSources(Path jar) throws IOException {
    var finder = ModuleFinder.of(jar);
    var moduleReference = finder.findAll().stream().findFirst().orElseThrow();

    try (var reader = moduleReference.open()) {
      return reader.list()
          .filter(f -> f.contains(".java"))
          .map(filename -> {
            try (var inputStream = reader.open(filename).orElseThrow();
                 var bufferedReader = new BufferedReader(new InputStreamReader(inputStream))) {
              var content = bufferedReader.lines().collect(Collectors.joining("\n"));
              return Map.entry(filename, content);
            } catch (IOException e) {
              throw new UncheckedIOException(e);
            }
          }).toList();
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private static void consumeInstructions(Iterator<Tuple> instructions, Consumer<? super Instruction> consumer) {
    if (!instructions.hasNext()) {
      return;
    }
    var hash = 0;
    var size = 5;
    var fifo = new ArrayDeque<Instruction>(size);
    for (int i = 0; i < size; i++) {
      if (instructions.hasNext()) {
        hash = addHash(fifo, instructions, hash);
      }
    }

    consumer.accept(new Instruction(peek(fifo).line(), hash));
    while (instructions.hasNext()) {
      hash = rollingHash(fifo, instructions, hash, consumer);
    }
  }

  private static Instruction peek(ArrayDeque<Instruction> fifo) {
    return fifo.peek();
  }

  private static int addHash(ArrayDeque<Instruction> fifo, Iterator<ReadByteCode.Tuple> iterator, int hash) {
    var nextTuple = getNextTuple(iterator);
    hash += nextTuple.hash();
    fifo.add(nextTuple);
    return hash;
  }

  private static int rollingHash(ArrayDeque<Instruction> fifo, Iterator<ReadByteCode.Tuple> iterator,
                                 int hash, Consumer<? super Instruction> consumer) {
    var lastElement = fifo.remove();
    hash -= lastElement.hash();
    hash = addHash(fifo, iterator, hash);
    consumer.accept(new Instruction(peek(fifo).line(), hash));
    return hash;
  }

  private static Instruction getNextTuple(Iterator<ReadByteCode.Tuple> iterator){
    var nextElement = iterator.next();
    var nextElementHash = nextElement.opcode().hashCode();
    return new Instruction(nextElement.line(), nextElementHash);
  }
}
