package hackathon.lottery.repository;

import java.sql.Array;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

final class JdbcUtils {
    private JdbcUtils() {
    }

    static int[] toIntArray(Array sqlArray) throws SQLException {
        if (sqlArray == null) {
            return null;
        }
        Object array = sqlArray.getArray();
        if (array instanceof Integer[] boxed) {
            int[] result = new int[boxed.length];
            for (int i = 0; i < boxed.length; i++) {
                result[i] = boxed[i];
            }
            return result;
        }
        if (array instanceof Object[] objects) {
            int[] result = new int[objects.length];
            for (int i = 0; i < objects.length; i++) {
                result[i] = ((Number) objects[i]).intValue();
            }
            return result;
        }
        throw new SQLException("Unsupported SQL array type: " + array.getClass());
    }

    static Array createSqlArray(Connection connection, int[] numbers) throws SQLException {
        Integer[] boxed = new Integer[numbers.length];
        for (int i = 0; i < numbers.length; i++) {
            boxed[i] = numbers[i];
        }
        return connection.createArrayOf("integer", boxed);
    }
}
