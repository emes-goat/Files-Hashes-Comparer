package com.emes;

import static org.hibernate.cfg.JdbcSettings.FORMAT_SQL;
import static org.hibernate.cfg.JdbcSettings.HIGHLIGHT_SQL;
import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_PASSWORD;
import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_URL;
import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_USER;
import static org.hibernate.cfg.JdbcSettings.SHOW_SQL;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class Database {

  private final Configuration configuration = new Configuration()
      .addAnnotatedClass(HashedFile.class)
      .setProperty(JAKARTA_JDBC_USER, "sa")
      .setProperty(JAKARTA_JDBC_PASSWORD, "")
      .setProperty("hibernate.agroal.maxSize", 1)
      .setProperty(SHOW_SQL, true)
      .setProperty(FORMAT_SQL, true)
      .setProperty(HIGHLIGHT_SQL, true);

  public void saveAll(List<HashedFile> hashes, Path databaseFile) {
    var createTable = Files.notExists(databaseFile);

    configuration.setProperty(JAKARTA_JDBC_URL, "jdbc:sqlite:" + databaseFile);

    try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
      if (createTable) {
        sessionFactory.getSchemaManager().exportMappedObjects(true);
      }

      sessionFactory.inTransaction(session -> hashes.forEach(session::persist));
    }
  }

  public Pair<Instant, Instant> findLastTwoTimestamps(Path databaseFile) {
    var createTable = Files.notExists(databaseFile);

    configuration.setProperty(JAKARTA_JDBC_URL, "jdbc:sqlite:" + databaseFile.toString());

    try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
      if (createTable) {
        sessionFactory.getSchemaManager().exportMappedObjects(true);
      }

      sessionFactory.inTransaction(session -> {
        session.createSelectionQuery("select timestamp from HashedFile order by timestamp desc", Instant.class)
            .stream().limit(2)
            .forEach(it -> {
              System.out.println(it);
            });
      });
    }

    return new Pair<>();
  }
}
