package fr.uge.clonewar;

import java.nio.file.Path;
import java.util.Objects;

/**
 * Represents an artefact.
 * @param main The main jar that contains .class
 * @param source The source jar that contains .java
 */
public record Artefact(Path main, Path source) {
  public Artefact {
    Objects.requireNonNull(main);
    Objects.requireNonNull(source);
  }
}