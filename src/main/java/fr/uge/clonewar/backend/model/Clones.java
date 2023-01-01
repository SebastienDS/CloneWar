package fr.uge.clonewar.backend.model;

import java.util.List;
import java.util.Objects;

/**
 * Represents Clones item that will be sent by the api
 * @param reference The artefact reference
 * @param clones The list of others artefacts
 */
public record Clones(Artefact reference, List<Clone> clones) {
  /**
   * Represents a Clone
   * @param artefact The compared artefact
   * @param percentage the percentage of similarity
   */
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