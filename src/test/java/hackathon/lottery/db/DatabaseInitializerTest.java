package hackathon.lottery.db;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseInitializerTest {

    @Test
    void loadStatements_splitsSchemaFile() throws Exception {
        DatabaseInitializer initializer = new DatabaseInitializer();
        var method = DatabaseInitializer.class.getDeclaredMethod("loadStatements");
        method.setAccessible(true);
        String[] statements = (String[]) method.invoke(initializer);

        assertTrue(statements.length >= 4);
        assertTrue(Arrays.stream(statements).anyMatch(s -> s.contains("CREATE TABLE IF NOT EXISTS draws")));
        assertTrue(Arrays.stream(statements).anyMatch(s -> s.contains("CREATE TABLE IF NOT EXISTS tickets")));
    }
}
