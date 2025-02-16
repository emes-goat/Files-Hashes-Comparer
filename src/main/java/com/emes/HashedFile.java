package com.emes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import org.hibernate.validator.constraints.Length;

@Entity
@Table(indexes = {
    @Index(columnList = "timestamp", name = "timestamp_index"),
})
public class HashedFile {

  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  Integer id;

  @NotNull
  String path;

  @NotNull
  @Length(min = 64, max = 64)
  String hash;

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