package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;


@Entity
@Table(name = "merchant")
/**
 * Represents the Merchant component in the nonprofit bookkeeping application.
 */
public class Merchant
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200, unique = true)
    private String name;

    @Lob
    private String notes;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    public Long getId() { return this.id; }
    public String getName() { return this.name; }
    public void setName(String name) { this.name = name; }
    public String getNotes() { return this.notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public boolean isActive() { return this.active; }
    public void setActive(boolean active) { this.active = active; }
}
