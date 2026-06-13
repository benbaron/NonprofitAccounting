package nonprofitbookkeeping.persistence;

import nonprofitbookkeeping.core.Database;
import nonprofitbookkeeping.core.FlywayMigrationRunner;
import nonprofitbookkeeping.model.AccountSide;
import nonprofitbookkeeping.model.AccountingEntry;
import nonprofitbookkeeping.model.AccountingTransaction;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

class JournalRepositoryPostingSupportTest {
    @TempDir Path tempDir;

    @Test
    void findById_loadsEntriesAndInfo_andReserveNextIdAdvances() throws Exception {
        Database.init(tempDir.resolve("jr-posting-support"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        seedAccounts();

        JournalRepository repo = new JournalRepository();
        AccountingTransaction txn = new AccountingTransaction();
        txn.setId(42);
        txn.setDate("2026-04-30");
        txn.setMemo("seed");
        txn.setInfo(Map.of("module", "TEST", "domain_record_id", "x-1"));
        LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
        entries.add(new AccountingEntry(new BigDecimal("25.00"), "1000", AccountSide.DEBIT, "Cash"));
        entries.add(new AccountingEntry(new BigDecimal("25.00"), "4000", AccountSide.CREDIT, "Revenue"));
        txn.setEntries(entries);
        repo.upsertTransaction(txn);

        AccountingTransaction loaded = repo.findTransactionById(42).orElseThrow();
        assertEquals("seed", loaded.getMemo());
        assertEquals(2, loaded.getEntries().size());
        assertEquals("TEST", loaded.getInfo().get("module"));

        int nextId = repo.reserveNextTransactionId();
        assertTrue(nextId > 42);
    }

    private void seedAccounts() throws Exception {
        try (Connection c = Database.get().getConnection();
             PreparedStatement ps = c.prepareStatement("MERGE INTO account(account_number, name, account_type, increase_side) KEY(account_number) VALUES (?,?,?,?)")) {
            ps.setString(1, "1000"); ps.setString(2, "Cash"); ps.setString(3, "ASSET"); ps.setString(4, "DEBIT"); ps.addBatch();
            ps.setString(1, "4000"); ps.setString(2, "Revenue"); ps.setString(3, "INCOME"); ps.setString(4, "CREDIT"); ps.addBatch();
            ps.executeBatch();
        }
    }

    @Test
    void reserveNextId_concurrentReservationsRemainUniqueWhenPersisted() throws Exception {
        Database.init(tempDir.resolve("jr-posting-concurrency"));
        FlywayMigrationRunner.migrateCurrentDatabaseIfEnabled();
        Database.get().ensureSchema();
        seedAccounts();
        JournalRepository repo = new JournalRepository();

        int workers = 6;
        ExecutorService pool = Executors.newFixedThreadPool(workers);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(workers);
        Set<Integer> ids = ConcurrentHashMap.newKeySet();

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < workers; i++) {
            final int idx = i;
            futures.add(pool.submit(() -> {
                try {
                    start.await();
                    int id = repo.reserveNextTransactionId();
                    AccountingTransaction txn = new AccountingTransaction();
                    txn.setId(id);
                    txn.setDate("2026-04-30");
                    txn.setMemo("concurrent-" + idx);
                    txn.setInfo(Map.of());
                    LinkedHashSet<AccountingEntry> entries = new LinkedHashSet<>();
                    entries.add(new AccountingEntry(new BigDecimal("10.00"), "1000", AccountSide.DEBIT, "Cash"));
                    entries.add(new AccountingEntry(new BigDecimal("10.00"), "4000", AccountSide.CREDIT, "Revenue"));
                    txn.setEntries(entries);
                    repo.upsertTransaction(txn);
                    ids.add(id);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            }));
        }

        start.countDown();
        done.await();
        for (Future<?> f : futures) {
            f.get();
        }
        pool.shutdownNow();
        assertEquals(workers, ids.size());
    }
}
