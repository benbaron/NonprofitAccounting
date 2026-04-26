package org.nonprofitbookkeeping.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "alias_review_queue",
       indexes = {
           @Index(name = "ix_alias_review_queue_status", columnList = "status, alias_domain"),
           @Index(name = "ix_alias_review_queue_norm", columnList = "normalization_key, alias_domain")
       })
public class AliasReviewQueue
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "alias_domain", nullable = false, length = 20)
    private String aliasDomain;

    @Column(name = "alias_id")
    private Long aliasId;

    @Column(name = "alias_text", nullable = false, length = 400)
    private String aliasText;

    @Column(name = "normalization_key", nullable = false, length = 400)
    private String normalizationKey;

    @Column(name = "candidate_count", nullable = false)
    private int candidateCount;

    @Column(name = "candidate_ids", length = 400)
    private String candidateIds;

    @Column(name = "reason", nullable = false, length = 200)
    private String reason;

    @Column(name = "status", nullable = false, length = 20)
    private String status = "OPEN";

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolver", length = 80)
    private String resolver;

    @Column(name = "resolution_note", length = 1000)
    private String resolutionNote;

    public Long getId() { return this.id; }
    public String getAliasDomain() { return this.aliasDomain; }
    public void setAliasDomain(String aliasDomain) { this.aliasDomain = aliasDomain; }
    public Long getAliasId() { return this.aliasId; }
    public void setAliasId(Long aliasId) { this.aliasId = aliasId; }
    public String getAliasText() { return this.aliasText; }
    public void setAliasText(String aliasText) { this.aliasText = aliasText; }
    public String getNormalizationKey() { return this.normalizationKey; }
    public void setNormalizationKey(String normalizationKey) { this.normalizationKey = normalizationKey; }
    public int getCandidateCount() { return this.candidateCount; }
    public void setCandidateCount(int candidateCount) { this.candidateCount = candidateCount; }
    public String getCandidateIds() { return this.candidateIds; }
    public void setCandidateIds(String candidateIds) { this.candidateIds = candidateIds; }
    public String getReason() { return this.reason; }
    public void setReason(String reason) { this.reason = reason; }
    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return this.createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getResolvedAt() { return this.resolvedAt; }
    public void setResolvedAt(LocalDateTime resolvedAt) { this.resolvedAt = resolvedAt; }
    public String getResolver() { return this.resolver; }
    public void setResolver(String resolver) { this.resolver = resolver; }
    public String getResolutionNote() { return this.resolutionNote; }
    public void setResolutionNote(String resolutionNote) { this.resolutionNote = resolutionNote; }
}
