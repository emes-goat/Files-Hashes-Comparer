package com.emes;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;
import org.hibernate.validator.constraints.Length;

@Entity
@IdClass(HashedFileId.class)
public class HashedFile {

  @Id
  @NotNull
  String path;

  @Id
  @NotNull
  @Length(min = 64, max = 64)
  String hash;

  @Id
  @NotNull
  Instant timestamp;

  public HashedFile() {
  }

  HashedFile(String path, String hash, Instant timestamp) {
    this.path = path;
    this.hash = hash;
    this.timestamp = timestamp;
  }
}

class HashedFileId implements Serializable {

  private String path;
  private String hash;
  private Instant timestamp;

  public HashedFileId() {
  }

  public HashedFileId(String path, String hash, Instant timestamp) {
    this.path = path;
    this.hash = hash;
    this.timestamp = timestamp;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HashedFileId that = (HashedFileId) o;
    return Objects.equals(path, that.path) &&
        Objects.equals(hash, that.hash) &&
        Objects.equals(timestamp, that.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(path, hash, timestamp);
  }
}