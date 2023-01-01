package fr.uge.clonewar.utils;

import fr.uge.clonewar.Artefact;

import javax.tools.*;
import java.io.*;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class JarBuilder {
  private final Path directory;
  private final Path mainPath;
  private final Path sourcePath;
  private final JarOutputStream main;
  private final JarOutputStream source;
  private boolean done;

  public JarBuilder(Path directory, String name, Manifest manifest) throws IOException {
    Objects.requireNonNull(directory);
    Objects.requireNonNull(name);
    Objects.requireNonNull(manifest);
    this.directory = directory;
    mainPath = Files.createTempFile(directory, name, ".jar");
    sourcePath = Files.createTempFile(directory, name, ".jar");

    main = new JarOutputStream(new FileOutputStream(mainPath.toFile()), manifest);
    source = new JarOutputStream(new FileOutputStream(sourcePath.toFile()), manifest);
  }

  public JarBuilder(Path directory, String name) throws IOException {
    this(directory, name, getDefaultManifest());
  }

  public void addFile(String name, String content) throws IOException {
    Objects.requireNonNull(name);
    Objects.requireNonNull(content);
    requireInConstruction();

    var path = compile(name, content);
    var file = replacePackage(name);
    writeToJar(main, file + ".class", Files.readAllBytes(path));
    writeToJar(source, file + ".java", content.getBytes());
  }

  private static void writeToJar(JarOutputStream jar, String name, byte[] bytes) throws IOException {
    var entry = new JarEntry(name);
    jar.putNextEntry(entry);
    jar.write(bytes);
    jar.closeEntry();
  }

  private static String replacePackage(String name) {
    return name.replace('.', '/');
  }

  public Artefact get() throws IOException {
    done = true;
    main.close();
    source.close();
    return new Artefact(mainPath, sourcePath);
  }

  private void requireInConstruction() {
    if (done) {
      throw new IllegalStateException();
    }
  }

  private static Manifest getDefaultManifest() {
    var manifest = new Manifest();
    manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
    return manifest;
  }

  private Path compile(String name, String content) throws IOException {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

    JavaFileObject compilationUnit = new StringJavaFileObject(name, content);

    var fileManager =
        new SimpleJavaFileManager<>(compiler.getStandardFileManager(null, null, null));

    JavaCompiler.CompilationTask compilationTask = compiler.getTask(
        null, fileManager, null, null, null, List.of(compilationUnit));

    compilationTask.call();

    var file = fileManager.getGeneratedOutputFiles().get(0);
    var path = Files.createTempFile(directory, name, ".class");
    Files.write(path, file.getBytes());
    return path;
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
      var file = new ClassJavaFileObject(className, kind);
      outputFiles.add(file);
      return file;
    }

    public List<ClassJavaFileObject> getGeneratedOutputFiles() {
      return outputFiles;
    }
  }
}
