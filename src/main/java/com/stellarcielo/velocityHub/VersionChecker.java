package com.stellarcielo.velocityHub;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;

public class VersionChecker {

    private static final Pattern NUMERIC_VERSION = Pattern.compile("^v?(\\d+(?:\\.\\d+)*)");
    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final String projectId;
    private final Logger logger;

    private final String currentVersion;

    public VersionChecker(String projectId, String currentVersion, Logger logger) {
        this.projectId = projectId;
        this.currentVersion = currentVersion;
        this.logger = logger;
    }

    public void checkForNewRelease() {
        HttpClient client = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.modrinth.com/v2/project/" + projectId + "/version"))
                .timeout(TIMEOUT)
                .header("User-Agent", "stellarcielo/velocity-hub/" + currentVersion)
                .GET()
                .build();

        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        logger.warn("Failed to check for updates: Modrinth returned HTTP {}", response.statusCode());
                        return;
                    }
                    handleVersions(JsonParser.parseString(response.body()).getAsJsonArray());
                })
                .exceptionally(e -> {
                    logger.warn("An error occurred while retrieving release information: {}", e.toString());
                    return null;
                });
    }

    private void handleVersions(JsonArray versions) {
        List<Integer> current = parseVersion(currentVersion);
        if (current == null) {
            logger.warn("Skipping update check because the current version \"{}\" could not be parsed.", currentVersion);
            return;
        }

        // Do not rely on the API order; pick the highest release version instead
        String latestVersion = null;
        List<Integer> latest = null;
        for (JsonElement element : versions) {
            JsonObject version = element.getAsJsonObject();
            if (!"release".equalsIgnoreCase(version.get("version_type").getAsString())) {
                continue;
            }

            String versionNumber = version.get("version_number").getAsString();
            List<Integer> parsed = parseVersion(versionNumber);
            if (parsed != null && (latest == null || compare(parsed, latest) > 0)) {
                latest = parsed;
                latestVersion = versionNumber;
            }
        }

        if (latest == null) {
            return;
        }

        if (compare(latest, current) > 0) {
            logger.warn("New version available : " + latestVersion);
            logger.warn("Current version : " + currentVersion);
            logger.warn("Download : https://modrinth.com/plugin/" + projectId + "/version/" + latestVersion);
        } else {
            logger.info("Latest version used.");
        }
    }

    // "1.10-SNAPSHOT" -> [1, 10]
    static List<Integer> parseVersion(String version) {
        Matcher matcher = NUMERIC_VERSION.matcher(version.trim());
        if (!matcher.find()) {
            return null;
        }

        List<Integer> parts = new ArrayList<>();
        for (String part : matcher.group(1).split("\\.")) {
            try {
                parts.add(Integer.parseInt(part));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return parts;
    }

    // Missing parts are treated as 0, so 1.10 == 1.10.0
    static int compare(List<Integer> a, List<Integer> b) {
        int length = Math.max(a.size(), b.size());
        for (int i = 0; i < length; i++) {
            int x = i < a.size() ? a.get(i) : 0;
            int y = i < b.size() ? b.get(i) : 0;
            if (x != y) {
                return Integer.compare(x, y);
            }
        }
        return 0;
    }
}
