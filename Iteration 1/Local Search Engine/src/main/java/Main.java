public class Main {
    public static void main(String[] args) {
        FileRepository repo = new FileRepository();

        long now = System.currentTimeMillis();

        FileData file1 = new FileData(
                "test1.txt",
                "/tmp/test1.txt",
                "Hello this is a test file",
                now
        );

        FileData file2 = new FileData(
                "test2.txt",
                "/tmp/test2.txt",
                "Another file with PostgreSQL content",
                now
        );

        repo.insertFile(file1);
        repo.insertFile(file2);
    }
}