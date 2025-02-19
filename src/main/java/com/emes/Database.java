package com.emes;

import java.nio.file.Path;
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
          PRIMARY KEY (path, hash)
        );
        """));
  }

  public void saveAll(List<HashedFile> hashes) {
    jdbi.withHandle(handle -> {
      handle.execute("DELETE FROM hashed_file");

      var batch = handle.prepareBatch("INSERT INTO hashed_file VALUES(:path, :hash)");

      hashes.forEach(it ->
          batch.bind("path", it.path())
              .bind("hash", it.hash())
              .add()
      );

      return batch.execute();
    });
  }

  public List<HashedFile> readPreviousHashes() {
    return jdbi.withHandle(handle ->
        handle.registerRowMapper(HashedFile.class, ConstructorMapper.of(HashedFile.class))
            .select(
                "SELECT * FROM hashed_file")
            .mapTo(HashedFile.class)
            .list());
  }
}