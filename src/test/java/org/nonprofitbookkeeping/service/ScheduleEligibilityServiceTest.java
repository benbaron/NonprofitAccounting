package org.nonprofitbookkeeping.service;

import nonprofitbookkeeping.core.Database;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.nonprofitbookkeeping.model.Account;
import org.nonprofitbookkeeping.model.AccountSubtype;
import org.nonprofitbookkeeping.persistence.Jpa;

import java.lang.reflect.Field;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScheduleEligibilityServiceTest
{
    @TempDir
    Path tempDir;

    private Jpa jpa;

    @AfterEach
    void tearDown()
    {
        if (jpa != null)
        {
            jpa.close();
        }
    }

    @Test
    void normalizedAccountRule_overridesSubtypeRule() throws Exception
    {
        Database.init(tempDir.resolve("eligibility-normalized-override"));
        Database.get().ensureSchema();

        long accountId;
        long receivableKindId;
        long payableKindId;
        long releaseId;
        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            st.execute("INSERT INTO chart_of_accounts(name, version, status) VALUES ('TEST','v1','ACTIVE')");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('1100', 1, '1100', 'Receivable Test', 'ASSET', 'RECEIVABLE', 0, 'DEBIT', TRUE, TRUE)");
            st.execute("INSERT INTO schedule_kind(code, name) VALUES ('RECEIVABLE','Receivable Schedule')");
            st.execute("INSERT INTO schedule_kind(code, name) VALUES ('PAYABLE','Payable Schedule')");
            st.execute("INSERT INTO config_release(release_code, status, effective_from, created_by) VALUES ('R1','ACTIVE', CURRENT_DATE, 'test')");
            receivableKindId = lookupId(c, "schedule_kind", "code", "RECEIVABLE");
            payableKindId = lookupId(c, "schedule_kind", "code", "PAYABLE");
            releaseId = lookupId(c, "config_release", "release_code", "R1");

            // Subtype default says RECEIVABLE required.
            st.execute("INSERT INTO schedule_requirement_rule(config_release_id, subject_kind, subtype, schedule_kind_id, requirement_level, valid_from, precedence, created_by) " +
                "VALUES (" + releaseId + ", 'SUBTYPE', 'RECEIVABLE', " + receivableKindId + ", 'REQUIRED', CURRENT_DATE, 100, 'test')");
            // Account override excludes RECEIVABLE and adds PAYABLE optional.
            st.execute("INSERT INTO schedule_requirement_rule(config_release_id, subject_kind, account_id, schedule_kind_id, requirement_level, valid_from, precedence, created_by) " +
                "VALUES (" + releaseId + ", 'ACCOUNT', 1, " + receivableKindId + ", 'EXCLUDED', CURRENT_DATE, 10, 'test')");
            st.execute("INSERT INTO schedule_requirement_rule(config_release_id, subject_kind, account_id, schedule_kind_id, requirement_level, valid_from, precedence, created_by) " +
                "VALUES (" + releaseId + ", 'ACCOUNT', 1, " + payableKindId + ", 'OPTIONAL', CURRENT_DATE, 10, 'test')");

            try (ResultSet rs = st.executeQuery("SELECT id FROM account WHERE account_number='1100'"))
            {
                rs.next();
                accountId = rs.getLong(1);
            }
        }

        Account account = accountWith(accountId, AccountSubtype.RECEIVABLE);
        jpa = new Jpa();
        ScheduleEligibilityService service = new ScheduleEligibilityService(jpa);

        assertEquals(Set.of("PAYABLE"), service.allowedScheduleKindCodes(account));
    }

    @Test
    void legacyFallback_usedWhenNormalizedRulesAbsent() throws Exception
    {
        Database.init(tempDir.resolve("eligibility-legacy-fallback"));
        Database.get().ensureSchema();

        long accountId;
        long payableKindId;
        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            st.execute("INSERT INTO chart_of_accounts(name, version, status) VALUES ('TEST','v1','ACTIVE')");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('2100', 1, '2100', 'Payable Test', 'LIABILITY', 'PAYABLE', 0, 'CREDIT', TRUE, TRUE)");
            st.execute("INSERT INTO schedule_kind(code, name) VALUES ('PAYABLE','Payable Schedule')");
            payableKindId = lookupId(c, "schedule_kind", "code", "PAYABLE");
            st.execute("INSERT INTO account_subtype_schedule_default(subtype, schedule_kind_id) VALUES ('PAYABLE', " + payableKindId + ")");

            try (ResultSet rs = st.executeQuery("SELECT id FROM account WHERE account_number='2100'"))
            {
                rs.next();
                accountId = rs.getLong(1);
            }
        }

        Account account = accountWith(accountId, AccountSubtype.PAYABLE);
        jpa = new Jpa();
        ScheduleEligibilityService service = new ScheduleEligibilityService(jpa);

        assertEquals(Set.of("PAYABLE"), service.allowedScheduleKindCodes(account));
    }

    @Test
    void normalizedRules_ignoreInactiveOrOutOfWindowReleases() throws Exception
    {
        Database.init(tempDir.resolve("eligibility-release-window"));
        Database.get().ensureSchema();

        long accountId;
        long inventoryKindId;
        long otherAssetKindId;
        long oldReleaseId;
        long activeReleaseId;
        try (Connection c = Database.get().getConnection(); Statement st = c.createStatement())
        {
            st.execute("INSERT INTO chart_of_accounts(name, version, status) VALUES ('TEST','v1','ACTIVE')");
            st.execute("INSERT INTO account(account_number, chart_id, code, name, account_type, subtype, opening_balance, normal_balance, is_posting, is_active) " +
                "VALUES ('3100', 1, '3100', 'Inventory Test', 'ASSET', 'INVENTORY', 0, 'DEBIT', TRUE, TRUE)");
            st.execute("INSERT INTO schedule_kind(code, name) VALUES ('INVENTORY','Inventory Schedule')");
            st.execute("INSERT INTO schedule_kind(code, name) VALUES ('OTHER_ASSET','Other Asset Schedule')");
            inventoryKindId = lookupId(c, "schedule_kind", "code", "INVENTORY");
            otherAssetKindId = lookupId(c, "schedule_kind", "code", "OTHER_ASSET");

            st.execute("INSERT INTO config_release(release_code, status, effective_from, effective_to, created_by) VALUES ('OLD','RETIRED', DATEADD('DAY', -30, CURRENT_DATE), DATEADD('DAY', -1, CURRENT_DATE), 'test')");
            st.execute("INSERT INTO config_release(release_code, status, effective_from, created_by) VALUES ('ACTIVE_NOW','ACTIVE', CURRENT_DATE, 'test')");
            oldReleaseId = lookupId(c, "config_release", "release_code", "OLD");
            activeReleaseId = lookupId(c, "config_release", "release_code", "ACTIVE_NOW");

            // Retired release row should be ignored.
            st.execute("INSERT INTO schedule_requirement_rule(config_release_id, subject_kind, subtype, schedule_kind_id, requirement_level, valid_from, precedence, created_by) " +
                "VALUES (" + oldReleaseId + ", 'SUBTYPE', 'INVENTORY', " + otherAssetKindId + ", 'REQUIRED', CURRENT_DATE, 100, 'test')");
            // Active release row should be used.
            st.execute("INSERT INTO schedule_requirement_rule(config_release_id, subject_kind, subtype, schedule_kind_id, requirement_level, valid_from, precedence, created_by) " +
                "VALUES (" + activeReleaseId + ", 'SUBTYPE', 'INVENTORY', " + inventoryKindId + ", 'REQUIRED', CURRENT_DATE, 100, 'test')");

            try (ResultSet rs = st.executeQuery("SELECT id FROM account WHERE account_number='3100'"))
            {
                rs.next();
                accountId = rs.getLong(1);
            }
        }

        Account account = accountWith(accountId, AccountSubtype.INVENTORY);
        jpa = new Jpa();
        ScheduleEligibilityService service = new ScheduleEligibilityService(jpa);

        assertEquals(Set.of("INVENTORY"), service.allowedScheduleKindCodes(account));
    }

    private static Account accountWith(long id, AccountSubtype subtype) throws Exception
    {
        Account account = new Account();
        account.setSubtype(subtype);

        Field idField = Account.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(account, id);

        return account;
    }

    private static long lookupId(Connection c, String table, String keyColumn, String keyValue) throws Exception
    {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                 "SELECT id FROM " + table + " WHERE " + keyColumn + " = '" + keyValue + "'"))
        {
            rs.next();
            return rs.getLong(1);
        }
    }
}
