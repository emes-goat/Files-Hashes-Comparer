package com.emes;

import static org.hibernate.cfg.HikariCPSettings.HIKARI_MAX_SIZE;
import static org.hibernate.cfg.JdbcSettings.FORMAT_SQL;
import static org.hibernate.cfg.JdbcSettings.HIGHLIGHT_SQL;
import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_PASSWORD;
import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_URL;
import static org.hibernate.cfg.JdbcSettings.JAKARTA_JDBC_USER;
import static org.hibernate.cfg.JdbcSettings.SHOW_SQL;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.cfg.Configuration;
import org.hibernate.tool.schema.Action;

public class Database {

  private final Configuration configuration;

  public Database(Path databaseFile) {
    configuration = new Configuration()
        .addAnnotatedClass(HashedFile.class)
        .setProperty(JAKARTA_JDBC_URL, "jdbc:h2:./" + databaseFile)
        .setProperty(AvailableSettings.JAKARTA_HBM2DDL_DATABASE_ACTION, Action.UPDATE)
        .setProperty(JAKARTA_JDBC_USER, "sa")
        .setProperty(JAKARTA_JDBC_PASSWORD, "")
        .setProperty(HIKARI_MAX_SIZE, 1)
        .setProperty(SHOW_SQL, false)
        .setProperty(FORMAT_SQL, false)
        .setProperty(HIGHLIGHT_SQL, false);
  }

  public void saveAll(List<HashedFile> hashes) {
    try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
      sessionFactory.inTransaction(session -> hashes.forEach(session::persist));
    }
  }

  public List<HashedFile> findFilesFromLastTwoScans() {
    try (SessionFactory sessionFactory = configuration.buildSessionFactory()) {
      var latestTimestamps = sessionFactory.fromTransaction(session ->
          session.createSelectionQuery(
                  "select distinct timestamp from HashedFile order by timestamp desc limit 2",
                  Instant.class)
              .getResultList()
      );

      return sessionFactory.fromTransaction(session ->
          session.createSelectionQuery(
                  "select path, hash, timestamp from HashedFile where timestamp in (:timestamps)",
                  HashedFile.class)
              .setParameter("timestamps", latestTimestamps)
              .getResultList());
    }
  }
}