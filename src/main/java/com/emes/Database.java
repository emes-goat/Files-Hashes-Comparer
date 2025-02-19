package com.emes;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;

public class Database {

  private final Jdbi jdbi;

  public Database(Path databaseFile) {
    jdbi = Jdbi.create("jdbc:sqlite:" + databaseFile);
    jdbi.withHandle(handle -> handle.execute("""
        CREATE TABLE IF NOT EXISTS hashed_file
        (
          path      varchar(255),
          hash      varchar(64),
          timestamp timestamp,
          PRIMARY KEY (path, hash, timestamp)
        );
        """));
  }

  public void saveAll(List<HashedFile> hashes) {
    jdbi.withHandle(handle -> {
      var batch = handle.prepareBatch("INSERT INTO hashed_file VALUES(:path, :hash, :timestamp)");

      hashes.forEach(it ->
          batch.bind("path", it.path())
              .bind("hash", it.hash())
              .bind("timestamp", it.timestamp())
              .add()
      );

      return batch.execute();
    });
  }

  public List<HashedFile> findFilesFromLastTwoScans() {
    return jdbi.withHandle(handle -> {
      var latestTimestamps = handle.select(
              "SELECT DISTINCT timestamp FROM hashed_file ORDER BY TIMESTAMP DESC LIMIT 2")
          .mapTo(Instant.class)
          .list();

      return handle
          .registerRowMapper(HashedFile.class, ConstructorMapper.of(HashedFile.class))
          .select(
              "SELECT * FROM hashed_file WHERE timestamp = :newer OR timestamp = :older")
          .bind("newer", latestTimestamps.getFirst())
          .bind("older", latestTimestamps.getLast())
          .mapTo(HashedFile.class)
          .list();
    });
  }
}