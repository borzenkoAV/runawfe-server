package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.audit.dao.ProcessLogDAO;
import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddTestTablePatch extends DBPatch {
    @Autowired
    private ProcessLogDAO processLogDAO;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = new LinkedList<String>();
        List<ColumnDef> columns = new LinkedList<DBPatch.ColumnDef>();
        columns.add(new ColumnDef("DEFINITION_ID", dialect.getTypeName(Types.BIGINT), false));
        columns.add(new ColumnDef("PROCESS_COUNT", dialect.getTypeName(Types.BIGINT), false));
        sql.add(getDDLCreateTable("TEST_549", columns, null));
        sql.add(getDDLCreateUniqueKey("TEST_549", "IX_TEST_549_DEFINITION_ID", "DEFINITION_ID"));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        List<Object[]> rows = session.createSQLQuery("SELECT DEFINITION_ID, COUNT(*) FROM BPM_PROCESS GROUP BY DEFINITION_ID").list();
        log.info("Found " + rows.size());
        SQLQuery insertQuery = session.createSQLQuery("INSERT INTO TEST_549 (DEFINITION_ID, PROCESS_COUNT) VALUES (:definitionId, :processCount)");
        for (Object[] row : rows) {
            Long definitionId = ((Number) row[0]).longValue();
            Long processCount = ((Number) row[1]).longValue();
            insertQuery.setParameter("definitionId", definitionId);
            insertQuery.setParameter("processCount", processCount);
            insertQuery.executeUpdate();
        }
    }

}