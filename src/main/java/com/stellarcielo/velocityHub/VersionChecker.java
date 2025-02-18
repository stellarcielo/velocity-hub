package com.stellarcielo.velocityHub;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class VersionChecker {

    private static final String CURRENT_VERSION = "1.6-SNAPSHOT";
    private static final String GITHUB_API_URL = "https://api.github.com/repos/stellarcielo/velocity-hub/releases/latest";

    public static void checkForUpdate() {

        String githubToken = System.getenv("TOKEN");
        if (githubToken == null || githubToken.isEmpty()) {
            throw new IllegalStateException("You need to set the TOKEN environment variable.");
        }

        try {
            URL url = new URL(GITHUB_API_URL + "?&client_id=\"Ov23lieLh7UXSOk9mMCL\"&client_secret=\"" + githubToken +"\"");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("get");
            connection.setRequestProperty("Accept", "application/vnd.github.v3+json");

            if (connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder content = new StringBuilder();
                String line;

                while ((line = in.readLine()) != null) {
                    content.append(line);
                }
                in.close();

                String latestVersion = parseVersion(content.toString());

                if (!CURRENT_VERSION.equals(latestVersion)) {
                    System.out.println("New version " + latestVersion + " is available!");
                }
            }
        } catch (Exception e) {
            System.out.println("Version check failed: " + e.getMessage());
        }
    }

    public static String parseVersion(String jsonResponce) {
        int index = jsonResponce.indexOf("\"tag_name\":\"");
        if (index != -1) {
            int start = index + 12;
            int end = jsonResponce.indexOf("\"", start);
            return jsonResponce.substring(start, end);
        }
        return CURRENT_VERSION;
    }

}
