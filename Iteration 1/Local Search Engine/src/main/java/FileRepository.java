import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class FileRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/searchengine";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void saveOrUpdateFile(FileData file) {
        String checkSql = "SELECT last_modified FROM files WHERE filepath = ?";
        String insertSql = "INSERT INTO files (filename, filepath, content, last_modified) VALUES (?, ?, ?, ?)";
        String updateSql = "UPDATE files SET filename = ?, content = ?, last_modified = ? WHERE filepath = ?";

        try (Connection conn = getConnection();
             PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {

            checkStmt.setString(1, file.getFilepath());
            ResultSet rs = checkStmt.executeQuery();

            if (rs.next()) {
                long dbLastModified = rs.getLong("last_modified");
                if (file.getLastModified() > dbLastModified) {
                    // File exists but is older, update it
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, file.getFilename());
                        updateStmt.setString(2, file.getContent());
                        updateStmt.setLong(3, file.getLastModified());
                        updateStmt.setString(4, file.getFilepath());
                        updateStmt.executeUpdate();
                        Indexer.incrementUpdated();
                    }
                } else {
                    Indexer.incrementIgnored(); // No change
                }
            } else {
                // File does not exist, so insert it
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, file.getFilename());
                    insertStmt.setString(2, file.getFilepath());
                    insertStmt.setString(3, file.getContent());
                    insertStmt.setLong(4, file.getLastModified());
                    insertStmt.executeUpdate();
                    Indexer.incrementAdded();
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error for file: " + file.getFilepath() + " - " + e.getMessage());
        }
    }

    public List<FileData> searchFiles(String query) {
        List<FileData> results = new ArrayList<>();
        // PostgreSQL full-text search
        String searchSql = "SELECT filename, filepath, content, last_modified FROM files " + "WHERE to_tsvector('english', content) @@ plainto_tsquery('english', ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSql)) {

            pstmt.setString(1, query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new FileData(
                        rs.getString("filename"),
                        rs.getString("filepath"),
                        rs.getString("content"),
                        rs.getLong("last_modified")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return results;
    }
}