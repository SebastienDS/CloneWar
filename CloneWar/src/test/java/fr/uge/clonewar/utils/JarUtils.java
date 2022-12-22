package fr.uge.clonewar.utils;

import fr.uge.clonewar.backend.FileStorage;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarUtils {

  public static void createJarFromProgram(FileStorage storage, Path jarPath, String name, String program) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    JavaFileObject compilationUnit = new StringJavaFileObject(name, program);

    var fileManager =
        new SimpleJavaFileManager<>(compiler.getStandardFileManager(null, null, null));

    JavaCompiler.CompilationTask compilationTask = compiler.getTask(
        null, fileManager, null, null, null, List.of(compilationUnit));

    compilationTask.call();

    var file = fileManager.getGeneratedOutputFiles().get(0);
    var path = storage.create(name + ".class");
    Files.write(path, file.getBytes());

    var jar = openJar(jarPath.toString());
    addFile(jar, storage.storageDir().toString(), path.toString());
    jar.close();
  }

  private static JarOutputStream openJar(String jarFile) throws IOException {
    return new JarOutputStream(new FileOutputStream(jarFile), getManifest());
  }

  private static Manifest getManifest() {
    var manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    return manifest;
  }

  private static void addFile(JarOutputStream target, String rootPath, String source) throws IOException {
    String remaining = "";
    if (rootPath.endsWith(File.separator)) {
      remaining = source.substring(rootPath.length());
    } else {
      remaining = source.substring(rootPath.length() + 1);
    }
    String name = remaining.replace('\\', '/');
    name = replacePackage(name);

    var entry = new JarEntry(name);
    entry.setTime(new File(source).lastModified());
    target.putNextEntry(entry);

    BufferedInputStream in = new BufferedInputStream(new FileInputStream(source));
    byte[] buffer = new byte[1024];
    while (true) {
      int count = in.read(buffer);
      if (count == -1) {
        break;
      }
      target.write(buffer, 0, count);
    }
    target.closeEntry();
    in.close();
  }

  private static String replacePackage(String name) {
    return name.replaceAll("\\.(?=.*\\.)", "/");
  }

  private static class StringJavaFileObject extends SimpleJavaFileObject {
    private final String code;

    public StringJavaFileObject(String name, String code) {
      super(URI.create("string:///" + name.replace('.', '/') + Kind.SOURCE.extension),
          Kind.SOURCE);
      this.code = code;
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors) {
      return code;
    }
  }

  private static class ClassJavaFileObject extends SimpleJavaFileObject {
    private final ByteArrayOutputStream outputStream;

    protected ClassJavaFileObject(String className, Kind kind) {
      super(URI.create("mem:///" + className.replace('.', '/') + kind.extension), kind);
      outputStream = new ByteArrayOutputStream();
    }

    @Override
    public OutputStream openOutputStream() {
      return outputStream;
    }

    public byte[] getBytes() {
      return outputStream.toByteArray();
    }

  }

  private static class SimpleJavaFileManager<E extends JavaFileManager> extends ForwardingJavaFileManager<E> {
    private final List<ClassJavaFileObject> outputFiles;

    protected SimpleJavaFileManager(E fileManager) {
      super(fileManager);
      outputFiles = new ArrayList<>();
    }

    @Override
    public JavaFileObject getJavaFileForOutput(Location location, String className, JavaFileObject.Kind kind, FileObject sibling) throws IOException {
      ClassJavaFileObject file = new ClassJavaFileObject(className, kind);
      outputFiles.add(file);
      return file;
    }

    public List<ClassJavaFileObject> getGeneratedOutputFiles() {
      return outputFiles;
    }
  }
}
