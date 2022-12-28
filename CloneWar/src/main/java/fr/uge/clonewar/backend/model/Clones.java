package fr.uge.clonewar.backend.model;

import java.util.List;
import java.util.Objects;

public record Clones(Artefact reference, List<Clone> clones) {
  public record Clone(Artefact artefact, int percentage) {
    public Clone {
      Objects.requireNonNull(artefact);
    }
  }

  public Clones {
    Objects.requireNonNull(reference);
    Objects.requireNonNull(clones);
    clones = List.copyOf(clones);
  }
}
