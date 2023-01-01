package fr.uge.clonewar.backend;


import java.io.Closeable;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

import io.helidon.webserver.BadRequestException;


/**
 * Represents a temporary directory.
 */
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

  /**
   * Gets the directory path.
   * @return The directory path
   */
  public Path storageDir() {
    return storageDir;
  }


  /**
   * Create a temporary file
   * @param filename The filename
   * @return The created file's path
   * @throws IllegalStateException if the storage has already been clean
   */
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

  /**
   * Delete a file.
   * @param path The file to delete
   * @throws IOException if an I/O error occurs
   * @throws IllegalStateException if the storage has already been clean
   */
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

  /**
   * Close the storage and delete all created files.
   * @throws IllegalStateException if the storage has already been clean
   * @throws UncheckedIOException if an I/O error occurs
   */
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