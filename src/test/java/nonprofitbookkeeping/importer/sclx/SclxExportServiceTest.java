package nonprofitbookkeeping.importer.sclx;

import nonprofitbookkeeping.persistence.DocumentRepository;
import nonprofitbookkeeping.persistence.JsonStorageRepository;
import nonprofitbookkeeping.persistence.sclx.AssetRepository;
import nonprofitbookkeeping.persistence.sclx.EventRepository;
import nonprofitbookkeeping.persistence.sclx.OrganizationRepository;
import nonprofitbookkeeping.persistence.sclx.OtherAssetItemRepository;
import nonprofitbookkeeping.persistence.sclx.OutstandingItemRepository;
import nonprofitbookkeeping.persistence.sclx.ReportingPeriodRepository;
import nonprofitbookkeeping.persistence.sclx.SupplyRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SclxExportServiceTest
{
    @Test
    void exportDocument_prefersRawPayload_andIncludesManifestSummary() throws Exception
    {
        String runId = "run-raw";
        SclxDocument rawDocument = sampleDocument("raw");

        StubJsonStorageRepository jsonStorageRepository = new StubJsonStorageRepository(Map.of(
            "sclx.raw." + runId,
            SclxParser.buildDefaultMapper().writeValueAsString(rawDocument)));

        StubDocumentRepository documentRepository = new StubDocumentRepository(Map.of(
            "sclx.importSummary." + runId,
            "{\"warnings\":[\"w1\"],\"counts\":{\"events\":1}}"));

        SclxExportService service = new SclxExportService(
            jsonStorageRepository,
            documentRepository,
            new StubCanonicalExportService(sampleDocument("canonical")),
            new StubOrganizationRepository(Map.of()),
            new StubReportingPeriodRepository(Map.of()),
            new StubEventRepository(Map.of()),
            new StubSclxDocumentRepository(Map.of()),
            new StubOutstandingItemRepository(Map.of()),
            new StubOtherAssetItemRepository(Map.of()),
            new StubAssetRepository(Map.of()),
            new StubSupplyRepository(Map.of()));

        SclxDocument exported = service.exportDocument(runId);

        assertEquals("event-raw", exported.events().get(0).eventId());
        assertEquals("doc-raw", exported.documents().get(0).documentId());

        Object manifestRaw = exported.extensions().get("manifest");
        assertTrue(manifestRaw instanceof Map<?, ?>);
        Map<?, ?> manifest = (Map<?, ?>) manifestRaw;
        assertEquals(runId, manifest.get("runId"));
        assertEquals("raw", manifest.get("source"));
        assertTrue(manifest.containsKey("importSummary"));
    }

    @Test
    void exportDocument_withoutRawPayload_assemblesRunScopedSectionsWithStableIds() throws Exception
    {
        String runId = "run-assembled";

        SclxExportService service = new SclxExportService(
            new StubJsonStorageRepository(Map.of()),
            new StubDocumentRepository(Map.of("sclx.importSummary." + runId, "{\"warnings\":[]}")),
            new StubCanonicalExportService(sampleDocument("canonical")),
            new StubOrganizationRepository(Map.of()),
            new StubReportingPeriodRepository(Map.of()),
            new StubEventRepository(Map.of("run-assembled::event-assembled", event("event-assembled", "Event assembled"))),
            new StubSclxDocumentRepository(Map.of("run-assembled::doc-assembled", document("doc-assembled"))),
            new StubOutstandingItemRepository(Map.of("run-assembled::out-assembled", outstanding("out-assembled"))),
            new StubOtherAssetItemRepository(Map.of("run-assembled::other-assembled", otherAsset("other-assembled"))),
            new StubAssetRepository(Map.of("run-assembled::asset-assembled", asset("asset-assembled"))),
            new StubSupplyRepository(Map.of("run-assembled::supply-assembled", supply("supply-assembled"))));

        SclxDocument exported = service.exportDocument(runId);

        assertEquals("assembled", ((Map<?, ?>) exported.extensions().get("manifest")).get("source"));
        assertEquals("event-assembled", exported.events().get(0).eventId());
        assertEquals("doc-assembled", exported.documents().get(0).documentId());
        assertEquals("out-assembled", exported.outstandingItems().get(0).outstandingItemId());
        assertEquals("other-assembled", exported.otherAssetItems().get(0).otherAssetItemId());
        assertEquals("asset-assembled", exported.assets().get(0).assetId());
        assertEquals("supply-assembled", exported.supplies().get(0).supplyId());

        assertEquals("txn-canonical", exported.transactions().get(0).transactionId());
        assertEquals("acct-canonical-1", exported.chartOfAccounts().get(0).accountId());
    }

    @Test
    void exportDocument_filtersRunScopedPassthroughByRunId() throws Exception
    {
        SclxExportService service = new SclxExportService(
            new StubJsonStorageRepository(Map.of()),
            new StubDocumentRepository(Map.of()),
            new StubCanonicalExportService(sampleDocument("canonical")),
            new StubOrganizationRepository(Map.of()),
            new StubReportingPeriodRepository(Map.of()),
            new StubEventRepository(Map.of(
                "run-a::event-a", event("event-a", "Event A"),
                "run-b::event-b", event("event-b", "Event B"))),
            new StubSclxDocumentRepository(Map.of(
                "run-a::doc-a", document("doc-a"),
                "run-b::doc-b", document("doc-b"))),
            new StubOutstandingItemRepository(Map.of()),
            new StubOtherAssetItemRepository(Map.of()),
            new StubAssetRepository(Map.of()),
            new StubSupplyRepository(Map.of()));

        SclxDocument runA = service.exportDocument("run-a");
        SclxDocument runB = service.exportDocument("run-b");

        assertEquals(List.of("event-a"), runA.events().stream().map(SclxDocument.Event::eventId).toList());
        assertEquals(List.of("event-b"), runB.events().stream().map(SclxDocument.Event::eventId).toList());
        assertEquals(List.of("doc-a"), runA.documents().stream().map(SclxDocument.Document::documentId).toList());
        assertEquals(List.of("doc-b"), runB.documents().stream().map(SclxDocument.Document::documentId).toList());
    }

    private static SclxDocument sampleDocument(String suffix)
    {
        return new SclxDocument(
            "SCLX",
            "1.3",
            OffsetDateTime.parse("2025-03-01T00:00:00Z"),
            null,
            new SclxDocument.Organization("org-" + suffix, "Org " + suffix, null, "USD", null, null, Map.of()),
            null,
            List.of(new SclxDocument.Account("1000", "Cash", "ASSET", null, "DEBIT", BigDecimal.ZERO, List.of(), "acct-" + suffix + "-1", null, null, true, List.of(), Map.of())),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(new SclxDocument.Event("event-" + suffix, "Event " + suffix, null, null, null, Map.of())),
            List.of(new SclxDocument.Document("doc-" + suffix, "INVOICE", "R", null, null, null, Map.of())),
            List.of(new SclxDocument.Transaction(
                "txn-" + suffix,
                LocalDate.of(2025, 2, 1),
                LocalDate.of(2025, 2, 1),
                "Txn",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                List.of(),
                null,
                List.of(),
                Map.of())),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            List.of(),
            Map.of());
    }

    private static nonprofitbookkeeping.model.sclx.Event event(String id, String name)
    {
        nonprofitbookkeeping.model.sclx.Event event = new nonprofitbookkeeping.model.sclx.Event();
        event.setEventId(id);
        event.setName(name);
        return event;
    }

    private static nonprofitbookkeeping.model.sclx.Document document(String id)
    {
        nonprofitbookkeeping.model.sclx.Document document = new nonprofitbookkeeping.model.sclx.Document();
        document.setDocumentId(id);
        return document;
    }

    private static nonprofitbookkeeping.model.sclx.OutstandingItem outstanding(String id)
    {
        nonprofitbookkeeping.model.sclx.OutstandingItem item = new nonprofitbookkeeping.model.sclx.OutstandingItem();
        item.setOutstandingItemId(id);
        return item;
    }

    private static nonprofitbookkeeping.model.sclx.OtherAssetItem otherAsset(String id)
    {
        nonprofitbookkeeping.model.sclx.OtherAssetItem item = new nonprofitbookkeeping.model.sclx.OtherAssetItem();
        item.setOtherAssetItemId(id);
        return item;
    }

    private static nonprofitbookkeeping.model.sclx.Asset asset(String id)
    {
        nonprofitbookkeeping.model.sclx.Asset asset = new nonprofitbookkeeping.model.sclx.Asset();
        asset.setAssetId(id);
        return asset;
    }

    private static nonprofitbookkeeping.model.sclx.Supply supply(String id)
    {
        nonprofitbookkeeping.model.sclx.Supply supply = new nonprofitbookkeeping.model.sclx.Supply();
        supply.setSupplyId(id);
        return supply;
    }

    private static final class StubJsonStorageRepository extends JsonStorageRepository
    {
        private final Map<String, String> values;

        private StubJsonStorageRepository(Map<String, String> values)
        {
            this.values = values;
        }

        @Override
        public Optional<String> load(String key)
        {
            return Optional.ofNullable(this.values.get(key));
        }
    }

    private static final class StubDocumentRepository extends DocumentRepository
    {
        private final Map<String, String> values;

        private StubDocumentRepository(Map<String, String> values)
        {
            this.values = values;
        }

        @Override
        public Optional<String> find(String name)
        {
            return Optional.ofNullable(this.values.get(name));
        }
    }

    private static final class StubCanonicalExportService extends NonprofitBookkeepingSclxExportService
    {
        private final SclxDocument document;

        private StubCanonicalExportService(SclxDocument document)
        {
            this.document = document;
        }

        @Override
        public SclxDocument exportDocument() throws SQLException
        {
            return this.document;
        }
    }

    private static final class StubOrganizationRepository extends OrganizationRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.Organization> values;

        private StubOrganizationRepository(Map<String, nonprofitbookkeeping.model.sclx.Organization> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.Organization> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubReportingPeriodRepository extends ReportingPeriodRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.ReportingPeriod> values;

        private StubReportingPeriodRepository(Map<String, nonprofitbookkeeping.model.sclx.ReportingPeriod> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.ReportingPeriod> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubEventRepository extends EventRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.Event> values;

        private StubEventRepository(Map<String, nonprofitbookkeeping.model.sclx.Event> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.Event> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubSclxDocumentRepository extends nonprofitbookkeeping.persistence.sclx.DocumentRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.Document> values;

        private StubSclxDocumentRepository(Map<String, nonprofitbookkeeping.model.sclx.Document> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.Document> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubOutstandingItemRepository extends OutstandingItemRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.OutstandingItem> values;

        private StubOutstandingItemRepository(Map<String, nonprofitbookkeeping.model.sclx.OutstandingItem> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.OutstandingItem> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubOtherAssetItemRepository extends OtherAssetItemRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.OtherAssetItem> values;

        private StubOtherAssetItemRepository(Map<String, nonprofitbookkeeping.model.sclx.OtherAssetItem> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.OtherAssetItem> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubAssetRepository extends AssetRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.Asset> values;

        private StubAssetRepository(Map<String, nonprofitbookkeeping.model.sclx.Asset> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.Asset> loadAll()
        {
            return this.values;
        }
    }

    private static final class StubSupplyRepository extends SupplyRepository
    {
        private final Map<String, nonprofitbookkeeping.model.sclx.Supply> values;

        private StubSupplyRepository(Map<String, nonprofitbookkeeping.model.sclx.Supply> values)
        {
            this.values = values;
        }

        @Override
        public Map<String, nonprofitbookkeeping.model.sclx.Supply> loadAll()
        {
            return this.values;
        }
    }
}
