package com.stellarcielo.velocityHub;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;

public class versionChecker {

    private final String project_id;
    private final Logger logger;

    private final String currentVersion;

    public versionChecker(String project_id, String currentVersion, Logger logger) {
        this.project_id = project_id;
        this.currentVersion = currentVersion;
        this.logger = logger;
    }

    public void checkForNewRelease() {
        CompletableFuture.runAsync(() -> {
            try {
                String apiUrl = "https://api.modrinth.com/v2/project/" + project_id + "/version";
                HttpURLConnection connection = (HttpURLConnection) new URL(apiUrl).openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("User-Agent", project_id);

                JsonArray array = JsonParser.parseReader(new InputStreamReader(connection.getInputStream())).getAsJsonArray();

                for (JsonElement element : array) {
                    String versionNumber = element.getAsJsonObject().get("version_number").getAsString();
                    String versionType = element.getAsJsonObject().get("version_type").getAsString();

                    if ("release".equalsIgnoreCase(versionType)) {

                        if (!currentVersion.equals(versionNumber)) {
                            logger.warn("New version available :" + versionNumber);
                            logger.warn("Current version : " + currentVersion);
                            logger.warn("Download : https://modrinth.com/plugin/" + project_id + "/version/" + versionNumber);
                        } else {
                            logger.info("Latest version used.");
                        }
                        break;
                    }
                }
            } catch (Exception e) {
                logger.error("An error occurred while retrieving release information: "+ e);
            }
        });
    }
}
