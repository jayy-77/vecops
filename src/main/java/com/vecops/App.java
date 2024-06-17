package com.vecops;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.commons.net.telnet.TelnetClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

class App {
    private static final String HEALTH_BASE = "http://127.0.0.1:";
    private static final String YML = "services.yml";
    private static final int TIMEOUT = 1000;

    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

        try {
            Config config = mapper.readValue(new File(YML), Config.class);

            for (Source source : config.sources) {
                if (!checkServerConnection(source.host, source.port, TIMEOUT)) {
                    String msg = "Alert: " + source.name + " source has lost the connection.\nService: " + source.name + "\nHost: " + source.host + "\nPort: " + source.port;
                    publishToSlack(config.webhook.url, msg);
                }
            }

            for (Sink sink : config.sinks) {
                if (!checkServerConnection(sink.host, sink.port, TIMEOUT)) {
                    String msg = "Alert: " + sink.name + " sink has lost the connection.\nService: " + sink.name + "\nHost: " + sink.host + "\nPort: " + sink.port;
                    publishToSlack(config.webhook.url, msg);
                }
            }

            for (String unit : config.service.unit) {
                runJournalctlCommand(config.webhook.url, unit, new String[]{"journalctl", "-u", unit, "--since", "5 minute ago"});
                runJournalctlCommand(config.webhook.url, unit, new String[]{"systemctl", "is-active", unit});
            }

            for (Pipeline pipeline : config.pipelines) {
                if (!healthCheck(pipeline.port)) {
                    publishToSlack(config.webhook.url, pipeline.name + " Alert: pipeline healthcheck failed.\nPort: " + pipeline.port);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean checkServerConnection(String ip, int port, int timeout) {
        TelnetClient telnet = new TelnetClient();
        telnet.setDefaultTimeout(timeout);

        try {
            telnet.connect(ip, port);
            return true;
        } catch (Exception e) {
            return false;
        } finally {
            try {
                telnet.disconnect();
            } catch (Exception e) {
                System.out.println("Error while disconnecting: " + e.getMessage());
            }
        }
    }

    private static void runJournalctlCommand(String webhook, String unit, String[] command) {
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(processBuilder.start().getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                if (line.toLowerCase().contains("error") || line.toLowerCase().contains("err")) {
                    publishToSlack(webhook, line);
                }

                if (line.contains("inactive")) {
                    publishToSlack(webhook, unit + " is " + line);
                }
            }
            int exitCode = processBuilder.start().waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void publishToSlack(String webhook, String message) {
        String payload = "{\"text\":\"" + message + "\"}";

        try {
            URL url = new URL(webhook);
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

            httpConn.setDoOutput(true);
            httpConn.setRequestMethod("POST");
            httpConn.setRequestProperty("Content-Type", "application/json");
            httpConn.setRequestProperty("Accept", "application/json");

            try (OutputStream os = httpConn.getOutputStream()) {
                byte[] input = payload.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = httpConn.getResponseCode();
            System.out.println("Response Code: " + responseCode);
        } catch (Exception e) {
            System.out.println("Error while publishing to Slack: " + e.getMessage());
        }
    }

    private static boolean healthCheck(int port) {
        try {
            URL url = new URL(HEALTH_BASE + port + "/health");
            HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();
            int responseCode = httpConn.getResponseCode();
            System.out.println(responseCode);
            return responseCode == HttpURLConnection.HTTP_OK;
        } catch (Exception e) {
            System.out.println("Health check failed: " + e.getMessage());
            return false;
        }
    }
}
