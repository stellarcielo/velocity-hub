package com.stellarcielo.velocityHub;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;

import java.io.IOException;

public class GitHubReleaseChecker {

    private final String githubToken;
    private final String repoOwner;
    private final String repoName;
    private final Logger logger;

    public GitHubReleaseChecker(String githubToken, String repoOwner, String repoName, Logger logger) {
        this.githubToken = githubToken;
        this.repoOwner = repoOwner;
        this.repoName = repoName;
        this.logger = logger;
    }

    public void checkForNewRelease() {
        OkHttpClient client = new OkHttpClient();
        String url = String.format("https://api.github.com/repos/%s/%s/releases", repoOwner, repoName);

        Request request = new Request.Builder().url(url).addHeader("Authorization", "token " + githubToken).build();

        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful() && response.body() != null) {
                String responseBody = response.body().toString();
                logger.info("New release info: "+ responseBody);
            } else {
                logger.warn("Failed to get release info: "+ response.code());
            }
        } catch (IOException e) {
            logger.error("An error occurred while retrieving release information: "+ e);
        }
    }

}
