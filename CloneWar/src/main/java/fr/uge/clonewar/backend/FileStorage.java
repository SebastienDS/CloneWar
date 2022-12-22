package fr.uge.clonewar.backend;


import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.NotFoundException;


public final class FileStorage implements Closeable {

  private final Path storageDir;
  private boolean cleaned;


  public FileStorage() {
    try {
      storageDir = Files.createTempDirectory("temp_dir");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Path storageDir() {
    return storageDir;
  }

  public Path create(String filename) {
    Objects.requireNonNull(filename);
    requireOpen();
    Path file = storageDir.resolve(filename);
    if (!file.getParent().equals(storageDir)) {
      throw new BadRequestException("Invalid file name");
    }
    try {
      Files.createFile(file);
    } catch (IOException ex) {
      throw new UncheckedIOException(ex);
    }
    return file;
  }

//  public Path lookup(String filename) {
//    Objects.requireNonNull(filename);
//    requireOpen();
//    Path file = storageDir.resolve(filename);
//    if (!file.getParent().equals(storageDir)) {
//      throw new BadRequestException("Invalid file name");
//    }
//    if (!Files.exists(file)) {
//      throw new NotFoundException("file not found");
//    }
//    if (!Files.isRegularFile(file)) {
//      throw new BadRequestException("Not a file");
//    }
//    return file;
//  }

  public void delete(Path path) throws IOException {
    Objects.requireNonNull(path);
    requireOpen();
    Files.delete(path);
  }

  private void deleteFiles() throws IOException {
    try (var dir = Files.list(storageDir)) {
      dir.forEach(f -> {
        try {
          Files.delete(f);
        } catch (IOException e) {
          throw new UncheckedIOException(e);
        }
      });
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
  }

  private void requireOpen() {
    if (cleaned) {
      throw new IllegalStateException("Storage already cleared");
    }
  }

  @Override
  public void close() {
    requireOpen();
    cleaned = true;
    try {
      deleteFiles();
      Files.delete(storageDir);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}