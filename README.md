# Vector.dev Pipeline Monitoring System

This project provides a monitoring system for Vector.dev pipelines, along with sources and sinks, using a combination of Telnet, system commands, and HTTP health checks. It reads configuration from a YAML file, checks the health of services, and publishes alerts to a Slack webhook if any service is down or errors are detected.

## Features

- Monitors the connection status of sources and sinks using Telnet.
- Checks the systemd service status and logs for errors.
- Performs HTTP health checks on specified pipelines.
- Publishes alerts to a specified Slack webhook if any issues are detected.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven
- A Slack webhook URL for receiving alerts

### Configuration

The configuration for the monitoring system is provided in the `services.yml` file. Here is an example configuration:

```yaml
pipelines:
    - name: "fortinet"
      port: 8686
sources:
    - name: "redis"
      host: "127.0.0.1"
      port: 6379
sinks:
    - name: "clickhouse"
      host: "127.0.0.1"
      port: 8123
    - name: "redis"
      host: "127.0.0.1"
      port: 6379
service:
    unit: ["vector.service", "fortinet.service"]
webhook:
    url: "<your_webhook_url>"
