import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class FileRepository {
    private static final String URL = "jdbc:postgresql://localhost:5432/searchengine";
    private static final String USER = "postgres";
    private static final String PASSWORD = "postgres";

    public void insertFile(FileData file) {
        String sql = "INSERT INTO files (filename, filepath, content, last_modified) VALUES (?, ?, ?, ?)";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            System.out.println("Connected to database!");

            stmt.setString(1, file.getFilename());
            stmt.setString(2, file.getFilepath());
            stmt.setString(3, file.getContent());
            stmt.setLong(4, file.getLastModified());

            stmt.executeUpdate();
            System.out.println("Inserted: " + file.getFilename());

        } catch (SQLException e) {
            System.out.println("Error:");
            e.printStackTrace();
        }
    }
}