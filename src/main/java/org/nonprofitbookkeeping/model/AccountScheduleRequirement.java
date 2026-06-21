package org.nonprofitbookkeeping.model;

import jakarta.persistence.*;


@Entity
@Table(name = "account_schedule_requirement",
       uniqueConstraints = @UniqueConstraint(name = "uq_asr", columnNames = {"account_id", "schedule_kind_id"}))
/**
 * Represents the AccountScheduleRequirement component in the nonprofit bookkeeping application.
 */
public class AccountScheduleRequirement
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_kind_id", nullable = false)
    private ScheduleKind scheduleKind;

    @Column(name = "is_required", nullable = false)
    private boolean required = true;

    @Column(length = 500)
    private String notes;

    public Long getId() { return this.id; }
    public Account getAccount() { return this.account; }
    public void setAccount(Account account) { this.account = account; }
    public ScheduleKind getScheduleKind() { return this.scheduleKind; }
    public void setScheduleKind(ScheduleKind scheduleKind) { this.scheduleKind = scheduleKind; }
    public boolean isRequired() { return this.required; }
    public void setRequired(boolean required) { this.required = required; }
    public String getNotes() { return this.notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
