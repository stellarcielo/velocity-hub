package com.stellarcielo.velocityHub;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;

public class GitHubReleaseChecker {

    private final String repoOwner;
    private final String repoName;
    private final String clientId;
    private final String clientSecret;
    private final Logger logger;

    private static final String CURRENT_VERSION = "1.6-SNAPSHOT";

    public GitHubReleaseChecker(String repoOwner, String repoName, String clientId, String clientSecret,  Logger logger) {
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.logger = logger;

    }

    public void checkForNewRelease() {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("https://api.github.com/repos/%s/%s/releases/latest?&client_id=\"%s\"&client_secret=\"%s\"", repoOwner, repoName, clientId, clientSecret);

        Request request = new Request.Builder().url(url).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String latestVersion = parseVersion(response.toString());
                if (CURRENT_VERSION.equals(latestVersion)) {
                    logger.info("New release version: " + latestVersion);
                } else{
                    logger.info("The latest version is used");
                }
            } else {
                logger.warn("Failed to get release info: "+ response.code());
            }
        } catch (IOException e) {
            logger.error("An error occurred while retrieving release information: "+ e);
        }
    }

    private static String parseVersion(String jsonResponce) {
        int index = jsonResponce.indexOf("\"tag_name\":\"");
        if (index != -1) {
            int start = index + 12;
            int end = jsonResponce.indexOf("\"", start);
            return jsonResponce.substring(start, end);
        }
        return CURRENT_VERSION;
    }
}
