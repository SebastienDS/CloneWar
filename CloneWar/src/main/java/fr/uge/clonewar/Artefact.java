package fr.uge.clonewar;

import java.nio.file.Path;
import java.util.Objects;

public record Artefact(Path main, Path source) {
  public Artefact {
    Objects.requireNonNull(main);
    Objects.requireNonNull(source);
  }
}
