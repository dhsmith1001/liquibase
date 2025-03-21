package liquibase.datatype.core;

import liquibase.change.core.LoadDataChange;
import liquibase.database.Database;
import liquibase.database.core.SnowflakeDatabase;
import liquibase.datatype.DataTypeInfo;
import liquibase.datatype.DatabaseDataType;
import liquibase.datatype.LiquibaseDataType;
import liquibase.servicelocator.PrioritizedService;
import liquibase.statement.DatabaseFunction;
import org.apache.commons.lang3.StringUtils;

@DataTypeInfo(name = "timestamp_ntz", aliases = {"java.sql.Types.DATETIME", "datetime", "timestampntz"}, minParameters = 0, maxParameters = 0, priority = PrioritizedService.PRIORITY_DATABASE)
public class TimestampNTZTypeSnowflake extends LiquibaseDataType {

    @Override
    public DatabaseDataType toDatabaseDataType(Database database) {

        if (database instanceof SnowflakeDatabase) {
            return new DatabaseDataType("TIMESTAMP_NTZ", getParameters());
        }

        return super.toDatabaseDataType(database);
    }

    @Override
    public LoadDataChange.LOAD_DATA_TYPE getLoadTypeName() {
        return LoadDataChange.LOAD_DATA_TYPE.DATE;
    }

    @Override
    public boolean supports(Database database) {
        return database instanceof SnowflakeDatabase;
    }

    @Override
    public int getPriority() {
        return PRIORITY_DATABASE;
    }

    @Override
    public String objectToSql(Object value, Database database) {
        if (value instanceof java.sql.Timestamp) {
            return String.format("TO_TIMESTAMP_NTZ(%s)", database.getDateLiteral(((java.sql.Timestamp) value)));
        }
        return super.objectToSql(value, database);
    }

    @Override
    public Object sqlToObject(String value, Database database) {
        if (StringUtils.containsIgnoreCase(value, "cast")) {
            return new DatabaseFunction(value);
        }
        return super.sqlToObject(value, database);
    }
}
