package org.rfcx.guardian.admin.comms.swm;

import android.content.ContentValues;
import android.content.Context;

import org.json.JSONArray;
import org.rfcx.guardian.utility.misc.ArrayUtils;
import org.rfcx.guardian.utility.misc.DbUtils;
import org.rfcx.guardian.utility.rfcx.RfcxRole;

import java.util.Date;
import java.util.List;

public class SwmMetaDb {

    public SwmMetaDb(Context context, String appVersion) {
        this.VERSION = RfcxRole.getRoleVersionValue(appVersion);
        this.DROP_TABLE_ON_UPGRADE = ArrayUtils.doesStringArrayContainString(DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS, appVersion);
        this.dbSwmDiagnostic = new DbSwmDiagnostic(context);
    }

    private int VERSION = 1;
    static final String DATABASE = "swm-meta";
    static final String C_MEASURED_AT = "measured_at";
    static final String C_SIGNAL_STRENGTH = "signal_strength";
    private static final String[] ALL_COLUMNS = new String[] { C_MEASURED_AT, C_SIGNAL_STRENGTH };

    static final String[] DROP_TABLES_ON_UPGRADE_TO_THESE_VERSIONS = new String[] { }; // "0.6.43"
    private boolean DROP_TABLE_ON_UPGRADE = false;

    private String createColumnString(String tableName) {
        StringBuilder sbOut = new StringBuilder();
        sbOut.append("CREATE TABLE ").append(tableName)
                .append("(").append(C_MEASURED_AT).append(" INTEGER")
                .append(", ").append(C_SIGNAL_STRENGTH).append(" INTEGER")
                .append(")");
        return sbOut.toString();
    }

    public class DbSwmDiagnostic {

        final DbUtils dbUtils;
        public String FILEPATH;

        private String TABLE = "diagnostic";

        public DbSwmDiagnostic(Context context) {
            this.dbUtils = new DbUtils(context, DATABASE, TABLE, VERSION, createColumnString(TABLE), DROP_TABLE_ON_UPGRADE);
            FILEPATH = DbUtils.getDbFilePath(context, DATABASE, TABLE);
        }

        public int insert(long measuredAt, int signalStrength) {

            ContentValues values = new ContentValues();
            values.put(C_MEASURED_AT, measuredAt);
            values.put(C_SIGNAL_STRENGTH, signalStrength);

            return this.dbUtils.insertRow(TABLE, values);
        }

        public JSONArray getLatestRowAsJsonArray() {
            return this.dbUtils.getRowsAsJsonArray(TABLE, ALL_COLUMNS, null, null, null);
        }

        private List<String[]> getAllRows() {
            return this.dbUtils.getRows(TABLE, ALL_COLUMNS, null, null, null);
        }

        public void clearRowsBefore(Date date) {
            this.dbUtils.deleteRowsOlderThan(TABLE, C_MEASURED_AT, date);
        }

        public String getConcatRows() {
            return DbUtils.getConcatRows(getAllRows());
        }

        public String getConcatRowsWithLabelPrepended(String labelToPrepend) {
            return DbUtils.getConcatRowsWithLabelPrepended(labelToPrepend, getAllRows());
        }

    }
    public final DbSwmDiagnostic dbSwmDiagnostic;
}