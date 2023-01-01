package fr.uge.clonewar.backend.model;


import java.util.Objects;

/**
 * Represents an Artefact item that will be sent by the api.
 * @param id The artefactId
 * @param name The name of the artefact
 * @param insertionDate The insertion date
 */
public record Artefact(int id, String name, long insertionDate) {
  public Artefact {
    Objects.requireNonNull(name);
  }
}