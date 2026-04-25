package app;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import core.Crawler;
import core.Indexer;
import core.ParsedQuery;
import core.PreviewGenerator;
import core.QueryParser;
import model.FileData;
import observer.FilePopularityScorer;
import observer.SearchHistoryManager;
import repository.FileRepository;

import java.util.List;
import java.util.stream.Collectors;

import static spark.Spark.*;

/**
 * REST API server for the FileScan UI.
 *
 * Dependency to add to your build file:
 *
 *   Gradle:  implementation 'com.sparkjava:spark-core:2.9.4'
 *            implementation 'com.google.code.gson:gson:2.10.1'
 *
 *   Maven:   <dependency>
 *              <groupId>com.sparkjava</groupId>
 *              <artifactId>spark-core</artifactId>
 *              <version>2.9.4</version>
 *            </dependency>
 *            <dependency>
 *              <groupId>com.google.code.gson</groupId>
 *              <artifactId>gson</artifactId>
 *              <version>2.10.1</version>
 *            </dependency>
 */
public class WebServer {

    private final FileRepository       repository;
    private final QueryParser          queryParser;
    private final SearchHistoryManager historyManager;
    private final FilePopularityScorer popularityScorer;
    private final Gson                 gson = new Gson();

    public WebServer(FileRepository repository) {
        this.repository       = repository;
        this.queryParser      = new QueryParser();
        this.historyManager   = new SearchHistoryManager(repository);
        this.popularityScorer = new FilePopularityScorer(repository);
    }

    public void start(int port) {
        port(port);

        // ── CORS — allow the browser to call us from file:// or localhost ──
        before((req, res) -> {
            res.header("Access-Control-Allow-Origin",  "*");
            res.header("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
            res.header("Access-Control-Allow-Headers", "Content-Type");
            res.type("application/json");
        });
        options("/*", (req, res) -> "OK");

        // ── Health check ──────────────────────────────────────────────────
        get("/api/ping", (req, res) -> "{\"status\":\"ok\"}");

        // ── POST /api/index ───────────────────────────────────────────────
        // Body:    { "rootDir": "/some/path", "ignoreExt": ".log" }
        // Returns: { "added": N, "updated": N, "deleted": N, "ignored": N }
        post("/api/index", (req, res) -> {
            try {
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String rootDir   = body.get("rootDir").getAsString();
                String ignoreExt = body.has("ignoreExt")
                        ? body.get("ignoreExt").getAsString()
                        : ".NONE";

                // Delegate to the refactored Indexer (see notes in README)
                Crawler crawler = new Crawler(ignoreExt, repository);
                Indexer indexer = new Indexer(crawler, repository);

                Indexer.IndexReport report = indexer.startIndexing(rootDir);

                JsonObject out = new JsonObject();
                out.addProperty("added",   report.added);
                out.addProperty("updated", report.updated);
                out.addProperty("deleted", report.deleted);
                out.addProperty("ignored", report.ignored);
                return gson.toJson(out);

            } catch (Exception e) {
                res.status(500);
                JsonObject err = new JsonObject();
                err.addProperty("error", e.getMessage());
                return gson.toJson(err);
            }
        });

        // ── POST /api/search ──────────────────────────────────────────────
        // Body:    { "query": "content:TODO path:src sort:date" }
        // Returns: { "results": [...], "suggestions": [...] }
        post("/api/search", (req, res) -> {
            try {
                JsonObject body = gson.fromJson(req.body(), JsonObject.class);
                String rawQuery = body.get("query").getAsString();

                ParsedQuery parsed = queryParser.parse(rawQuery);
                List<FileData> files = repository.searchFiles(parsed);

                // Notify observers (history + popularity boost)
                historyManager.onSearchPerformed(rawQuery, parsed, files);
                popularityScorer.onSearchPerformed(rawQuery, parsed, files);

                // Build result DTOs
                List<JsonObject> resultList = files.stream().map(f -> {
                    JsonObject o = new JsonObject();
                    o.addProperty("filename",     f.getFilename());
                    o.addProperty("filepath",     f.getFilepath());
                    o.addProperty("preview",      PreviewGenerator.generatePreview(f.getContent()));
                    o.addProperty("pathScore",    f.getPathScore());
                    o.addProperty("lastModified", f.getFormattedDate());
                    return o;
                }).collect(Collectors.toList());

                // Also return fresh suggestions so the UI updates instantly
                List<String> suggestions = historyManager.getTopSuggestions(6);

                JsonObject out = new JsonObject();
                out.add("results",     gson.toJsonTree(resultList));
                out.add("suggestions", gson.toJsonTree(suggestions));
                return gson.toJson(out);

            } catch (Exception e) {
                res.status(500);
                JsonObject err = new JsonObject();
                err.addProperty("error", e.getMessage());
                return gson.toJson(err);
            }
        });

        // ── GET /api/suggestions ──────────────────────────────────────────
        // Params:  ?limit=6
        // Returns: { "suggestions": ["query1", "query2", ...] }
        get("/api/suggestions", (req, res) -> {
            try {
                int limit = 6;
                String limitParam = req.queryParams("limit");
                if (limitParam != null) limit = Integer.parseInt(limitParam);

                List<String> suggestions = historyManager.getTopSuggestions(limit);

                JsonObject out = new JsonObject();
                out.add("suggestions", gson.toJsonTree(suggestions));
                return gson.toJson(out);

            } catch (Exception e) {
                res.status(500);
                JsonObject err = new JsonObject();
                err.addProperty("error", e.getMessage());
                return gson.toJson(err);
            }
        });

        System.out.println("FileScan UI server running at http://localhost:" + port);
        System.out.println("Open index.html in your browser to use the UI.");
    }
}