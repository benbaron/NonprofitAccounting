package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;


@Entity
@Table(name = "activity")
/**
 * Represents the Activity component in the nonprofit bookkeeping application.
 */
public class Activity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 64, unique = true)
    private String code;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Long getId() { return this.id; }
    public String getCode() { return this.code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public boolean isActive() { return this.active; }
    public void setActive(boolean active) { this.active = active; }
}
