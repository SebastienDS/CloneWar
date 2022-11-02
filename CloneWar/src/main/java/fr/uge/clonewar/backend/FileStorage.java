package fr.uge.clonewar.backend;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.helidon.webserver.BadRequestException;
import io.helidon.webserver.NotFoundException;


public final class FileStorage {

  private final Path storageDir;


  public FileStorage() {
    try {
      storageDir = Files.createTempDirectory("temp_dir");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public List<String> listFiles() {
    try (var dir = Files.list(storageDir)) {
      return dir.filter(Files::isRegularFile)
          .map(storageDir::relativize)
          .map(Path::toString)
          .toList();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public Path create(String filename) {
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


  public Path lookup(String filename) {
    Path file = storageDir.resolve(filename);
    if (!file.getParent().equals(storageDir)) {
      throw new BadRequestException("Invalid file name");
    }
    if (!Files.exists(file)) {
      throw new NotFoundException("file not found");
    }
    if (!Files.isRegularFile(file)) {
      throw new BadRequestException("Not a file");
    }
    return file;
  }
}