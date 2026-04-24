package repository;
import core.ParsedQuery;
import model.FileData;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class FileRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/searchengine";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public enum SaveStatus { ADDED, UPDATED, IGNORED }

    public SaveStatus saveOrUpdateFile(FileData file) {
        String checkSql = "SELECT last_modified FROM files WHERE filepath = ?";
        String insertSql = "INSERT INTO files (filename, filepath, content, last_modified, path_score) VALUES (?, ?, ?, ?, ?)";
        String updateSql = "UPDATE files SET filename = ?, content = ?, last_modified = ?, path_score = ? WHERE filepath = ?";

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
                        updateStmt.setInt(4, file.getPathScore());
                        updateStmt.setString(5, file.getFilepath());
                        updateStmt.executeUpdate();

                        return SaveStatus.UPDATED;
                    }
                } else {
                    return SaveStatus.IGNORED;
                }
            } else {
                // File does not exist, so insert it
                try (PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {
                    insertStmt.setString(1, file.getFilename());
                    insertStmt.setString(2, file.getFilepath());
                    insertStmt.setString(3, file.getContent());
                    insertStmt.setLong(4, file.getLastModified());
                    insertStmt.setInt(5, file.getPathScore());
                    insertStmt.executeUpdate();

                    return SaveStatus.ADDED;
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error for file: " + file.getFilepath() + " - " + e.getMessage());
        }
        return null;
    }

    public int deleteStaleFiles(Set<String> validPaths, String rootDirectory) {
        String selectSql = "SELECT filepath FROM files";
        String deleteSql = "DELETE FROM files WHERE filepath = ?";

        int nr_deleted_files = 0;

        try (Connection conn = getConnection();
             Statement selectStmt = conn.createStatement();
             ResultSet rs = selectStmt.executeQuery(selectSql);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteSql)) {

            while (rs.next()) {
                String dbPath = rs.getString("filepath");

                // If the database path falls under the directory we just scanned,
                // but it wasn't found during the crawl, it must have been deleted.
                if (dbPath.startsWith(rootDirectory) && !validPaths.contains(dbPath)) {
                    deleteStmt.setString(1, dbPath);
                    deleteStmt.executeUpdate();
                    nr_deleted_files++;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error deleting stale files: " + e.getMessage());
        }
        return nr_deleted_files;
    }

    public List<FileData> searchFiles(ParsedQuery query) {
        List<FileData> results = new ArrayList<>();

        List<String> conditions = new ArrayList<>();
        List<String> params = new ArrayList<>();

        for (String term : query.getContentTerms()){
            conditions.add("to_tsvector('simple', content) @@ plainto_tsquery('simple', ?)");
            params.add(term);
        }
        for (String term : query.getPathTerms()){
            conditions.add("filepath ILIKE ?");
            params.add("%" + term + "%");
        }
        for (String term : query.getFreeTerms()){
            conditions.add("(filename ILIKE ? OR to_tsvector('simple', content) @@ plainto_tsquery('simple', ?))");
            params.add("%" + term + "%");
            params.add(term);
        }

        if (conditions.isEmpty()) return List.of();

        String searchSql = "SELECT filename, filepath, content, last_modified, path_score "
                         + "FROM files WHERE "
                         + String.join(" AND ", conditions) // AND between all conditions
                         + " ORDER BY path_score DESC";


        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(searchSql)) {

            String nameQuery = "%" + query + "%"; // wrap the query in '%' for the ILIKE search

            for (int i = 0; i < params.size(); i++) {
                pstmt.setString(i + 1, params.get(i)); // JDBC is 1-indexed
            }

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                results.add(new FileData(
                        rs.getString("filename"),
                        rs.getString("filepath"),
                        rs.getString("content"),
                        rs.getLong("last_modified"),
                        rs.getInt("path_score")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Search error: " + e.getMessage());
        }
        return results;
    }
}