package liquibase.change.core;

import liquibase.Scope;
import liquibase.change.AbstractChange;
import liquibase.change.Change;
import liquibase.change.ChangeMetaData;
import liquibase.change.DatabaseChange;
import liquibase.change.DatabaseChangeProperty;
import liquibase.database.Database;
import liquibase.exception.UnexpectedLiquibaseException;
import liquibase.exception.ValidationErrors;
import liquibase.serializer.LiquibaseSerializable;
import liquibase.sql.Sql;
import liquibase.statement.SqlStatement;
import liquibase.statement.core.RuntimeStatement;
import org.apache.commons.lang3.StringUtils;

@DatabaseChange(name="output", description = "Logs a message and continues execution.", priority = ChangeMetaData.PRIORITY_DEFAULT, since = "3.3")
public class OutputChange extends AbstractChange {

    private String message;
    private String target;

    @Override
    public ValidationErrors validate(Database database) {
        ValidationErrors validate = super.validate(database);
        validate.checkRequiredField("message", getMessage());
        return validate;
    }

    @DatabaseChangeProperty(description = "Message to send to output", exampleValue = "Make sure you feed the cat", serializationType = LiquibaseSerializable.SerializationType.DIRECT_VALUE)
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = StringUtils.trimToNull(message);
    }

    @DatabaseChangeProperty(description = "Target for message. Possible values: STDOUT, STDERR, FATAL, WARN, INFO, DEBUG. Default: STDERR",
        exampleValue = "STDERR")
    public String getTarget() {
        if (target == null) {
            return "STDERR";
        }
        return target;
    }

    public void setTarget(String target) {
        this.target = StringUtils.trimToNull(target);
    }


    @Override
    public SqlStatement[] generateStatements(Database database) {
        return new SqlStatement[] { new RuntimeStatement() {
            @Override
            public Sql[] generate(Database database) {
                if (! shouldExecuteChange(database)) {
                    return null;
                }

                String target = getTarget();
                if ("STDOUT".equalsIgnoreCase(target)) {
                    System.out.println(getMessage());
                } else if ("STDERR".equalsIgnoreCase(target)) {
                    System.err.println(getMessage());
                } else if ("DEBUG".equalsIgnoreCase(target)) {
                    Scope.getCurrentScope().getLog(getClass()).fine(getMessage());
                } else if ("INFO".equalsIgnoreCase(target)) {
                    Scope.getCurrentScope().getLog(getClass()).info(getMessage());
                } else if ("WARN".equalsIgnoreCase(target) || "WARNING".equalsIgnoreCase(target)) {
                    Scope.getCurrentScope().getLog(getClass()).warning(getMessage());
                } else if ("SEVERE".equalsIgnoreCase(target) || "FATAL".equalsIgnoreCase(target) || "ERROR"
                    .equalsIgnoreCase(target)) {
                    Scope.getCurrentScope().getLog(getClass()).severe(getMessage());
                } else {
                    throw new UnexpectedLiquibaseException("Unknown target: "+target);
                }
                return null;
            }
        }};
    }

    @Override
    public String getConfirmationMessage() {
        return "Output: "+getMessage();
    }

    @Override
    public String getSerializedObjectNamespace() {
        return STANDARD_CHANGELOG_NAMESPACE;
    }

    @Override
    public Object getSerializableFieldValue(String field) {
        Object value = super.getSerializableFieldValue(field);
        if ("target".equals(field) && StringUtils.isEmpty(value.toString())) {
            return null;
        }
        return value;
    }

    @Override
    protected Change[] createInverses() {
        return EMPTY_CHANGE;
    }
}
