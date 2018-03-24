package fr.xephi.authme.datasource.columnshandler;

import ch.jalu.datasourcecolumns.data.DataSourceValue;
import ch.jalu.datasourcecolumns.data.DataSourceValues;
import ch.jalu.datasourcecolumns.data.UpdateValues;
import ch.jalu.datasourcecolumns.sqlimplementation.PredicateSqlGenerator;
import ch.jalu.datasourcecolumns.sqlimplementation.PreparedStatementGenerator;
import ch.jalu.datasourcecolumns.sqlimplementation.ResultSetValueRetriever;
import ch.jalu.datasourcecolumns.sqlimplementation.SqlColumnsHandler;
import fr.xephi.authme.data.auth.PlayerAuth;
import fr.xephi.authme.datasource.AuthMeColumns;
import fr.xephi.authme.datasource.ColumnContext;
import fr.xephi.authme.settings.Settings;
import fr.xephi.authme.settings.properties.DatabaseSettings;

import java.sql.Connection;
import java.sql.SQLException;

import static fr.xephi.authme.datasource.SqlDataSourceUtils.logSqlException;

/**
 * Wrapper of {@link SqlColumnsHandler} for the AuthMe data table.
 * Wraps exceptions and provides better support for operations based on a {@link PlayerAuth} object.
 */
public final class AuthMeColumnsHandler {

    private final SqlColumnsHandler<ColumnContext, String> internalHandler;

    private AuthMeColumnsHandler(SqlColumnsHandler<ColumnContext, String> internalHandler) {
        this.internalHandler = internalHandler;
    }

    /**
     * Creates a column handler for SQLite.
     *
     * @param connection the connection to the database
     * @param settings plugin settings
     * @return created column handler
     */
    public static AuthMeColumnsHandler createForSqlite(Connection connection, Settings settings) {
        ColumnContext columnContext = new ColumnContext(settings);
        String tableName = settings.getProperty(DatabaseSettings.MYSQL_TABLE);
        String nameColumn = settings.getProperty(DatabaseSettings.MYSQL_COL_NAME);

        SqlColumnsHandler<ColumnContext, String> sqlColHandler =
            new SqlColumnsHandler<>(connection, columnContext, tableName, nameColumn);
        return new AuthMeColumnsHandler(sqlColHandler);
    }

    /**
     * Creates a column handler for MySQL.
     *
     * @param preparedStatementGenerator supplier of SQL prepared statements with a connection to the database
     * @param settings plugin settings
     * @return created column handler
     */
    public static AuthMeColumnsHandler createForMySql(PreparedStatementGenerator preparedStatementGenerator,
                                                      Settings settings) {
        ColumnContext columnContext = new ColumnContext(settings);
        String tableName = settings.getProperty(DatabaseSettings.MYSQL_TABLE);
        String nameColumn = settings.getProperty(DatabaseSettings.MYSQL_COL_NAME);

        SqlColumnsHandler<ColumnContext, String> sqlColHandler = new SqlColumnsHandler<>(preparedStatementGenerator,
            columnContext, tableName, nameColumn, new ResultSetValueRetriever<>(columnContext),
            new PredicateSqlGenerator<>(columnContext));
        return new AuthMeColumnsHandler(sqlColHandler);
    }

    /**
     * Changes a column from a specific row to the given value.
     *
     * @param name name of the account to modify
     * @param column the column to modify
     * @param value the value to set the column to
     * @param <T> the column type
     * @return true upon success, false otherwise
     */
    public <T> boolean update(String name, AuthMeColumns<T> column, T value) {
        try {
            return internalHandler.update(name, column, value);
        } catch (SQLException e) {
            logSqlException(e);
            return false;
        }
    }

    /**
     * Updates a row to have the values as retrieved from the PlayerAuth object.
     *
     * @param auth the player auth object to modify and to get values from
     * @param columns the columns to update in the row
     * @return true upon success, false otherwise
     */
    public boolean update(PlayerAuth auth, AuthMeColumns<?>... columns) {
        try {
            return internalHandler.update(auth.getNickname(), auth, columns);
        } catch (SQLException e) {
            logSqlException(e);
            return false;
        }
    }

    /**
     * Updates a row to have the given values.
     *
     * @param name the name of the account to modify
     * @param updateValues the values to set on the row
     * @return true upon success, false otherwise
     */
    public boolean update(String name, UpdateValues<ColumnContext> updateValues) {
        try {
            return internalHandler.update(name.toLowerCase(), updateValues);
        } catch (SQLException e) {
            logSqlException(e);
            return false;
        }
    }

    /**
     * Retrieves the given column from a given row.
     *
     * @param name the account name to look up
     * @param column the column whose value should be retrieved
     * @param <T> the column type
     * @return the result of the lookup
     */
    public <T> DataSourceValue<T> retrieve(String name, AuthMeColumns<T> column) throws SQLException {
        return internalHandler.retrieve(name.toLowerCase(), column);
    }

    /**
     * Retrieves multiple values from a given row.
     *
     * @param name the account name to look up
     * @param columns the columns to retrieve
     * @return map-like object with the requested values
     */
    public DataSourceValues retrieve(String name, AuthMeColumns<?>... columns) throws SQLException {
        return internalHandler.retrieve(name.toLowerCase(), columns);
    }
}
