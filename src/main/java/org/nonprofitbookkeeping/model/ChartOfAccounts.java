package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;
import java.time.*;


@Entity
@Table(name = "chart_of_accounts")
/**
 * Represents the ChartOfAccounts component in the nonprofit bookkeeping application.
 */
public class ChartOfAccounts
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(nullable = false, length = 50)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChartStatus status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt = Instant.now();

    public Long getId() { return this.id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getVersion() { return this.version; }
    public void setVersion(String version) { this.version = version; }
    public ChartStatus getStatus() { return this.status; }
    public void setStatus(ChartStatus status) { this.status = status; }
    public Instant getCreatedAt() { return this.createdAt; }
    public Instant getUpdatedAt() { return this.updatedAt; }
    public void touchUpdatedAt() { this.updatedAt = Instant.now(); }
}
