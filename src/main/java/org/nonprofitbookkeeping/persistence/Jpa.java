package org.nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Simple JPA bootstrap helper for a desktop (RESOURCE_LOCAL) application.
 *
 * <p>Schema creation is owned by Flyway plus the temporary compatibility path in
 * {@code Database.ensureSchema()}. Hibernate should validate mappings against
 * the existing database instead of mutating schema.</p>
 */
@ApplicationScoped
public class Jpa
{
    private final EntityManagerFactory emf;

    public Jpa()
    {
        this.emf = Persistence.createEntityManagerFactory("scaLedgerPU", properties());
    }

    private static Map<String, Object> properties()
    {
        Map<String, Object> props = new HashMap<>();
        props.put("jakarta.persistence.jdbc.driver", "org.h2.Driver");
        props.put("jakarta.persistence.jdbc.url", jdbcUrl());
        props.put("jakarta.persistence.jdbc.user", jdbcUser());
        props.put("jakarta.persistence.jdbc.password", jdbcPass());
        props.put("hibernate.hbm2ddl.auto", "validate");
        props.put("hibernate.show_sql", "false");
        return props;
    }

    private static String jdbcUrl()
    {
        if (Database.isInitialized())
        {
            return Database.get().getJdbcUrl();
        }
        Path defaultDb = Path.of(System.getProperty("user.home"), ".nonprofitbookkeeping", "nonprofitbookkeeping");
        return "jdbc:h2:file:" + defaultDb.toAbsolutePath() + ";AUTO_SERVER=TRUE;MODE=MySQL";
    }

    private static String jdbcUser()
    {
        return Database.isInitialized() ? Database.get().getUser() : "sa";
    }

    private static String jdbcPass()
    {
        return Database.isInitialized() ? Database.get().getPass() : "";
    }

    public EntityManager em()
    {
        return emf.createEntityManager();
    }

    public void close()
    {
        emf.close();
    }
}
