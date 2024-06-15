package com.vecops;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.commons.net.telnet.TelnetClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.net.HttpURLConnection;
import java.net.URL;
import java.io.OutputStream;

class App {
    public static void main(String args[]) {
        final String YML = "services.yml";
        final int TIMOUT = 1000;

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());

         try {
            Config config = mapper.readValue(new File(YML), Config.class);

            for(Source sources: config.sources) {
                if(!check_server_connection(sources.host, sources.port, TIMOUT)) {
                    String msg = sources.name + " source has lost the connection.";
                    PublishToSlack(config.webhook.url, msg);
                }
            }

            for(Sink sinks: config.sinks) {
                if(!check_server_connection(sinks.host, sinks.port, TIMOUT)) {
                    String msg = sinks.name + " sink has lost the connection.";
                    PublishToSlack(config.webhook.url, msg);
                }
            }

            for(String unit : config.service.unit){
                JournalctlReader(config.webhook.url, unit);
            }

        } catch (IOException e) { e.printStackTrace(); }

    }

    public static boolean check_server_connection(String ip, int port, int timeout){
        TelnetClient telnet = new TelnetClient();
        telnet.setDefaultTimeout(timeout);

        try {
            telnet.connect(ip, port);
            return true;
        } 
        
        catch (Exception e) { return false; } 
        
        finally {
            try { telnet.disconnect(); } 
            catch (Exception e) { System.out.println("Something went wrong while disconnecting"); }
        }
    }

    public static void JournalctlReader (String webhook,String unit) {
        String[] command = {"journalctl", "-u", unit, "--since", "5 minute ago"};

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("error") || line.contains("ERROR") || line.contains("ERR")) {
                    PublishToSlack(webhook, line);
                }
            }
            int exitCode = process.waitFor();
            System.out.println("Exited with code: " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void PublishToSlack(String webhook, String message) {
        String payload = "{\"text\":\"" + message + "\"}";

        try{
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
        } 

        catch(Exception e){ System.out.println("Something went wron while publishing to slack."); }
    }
}