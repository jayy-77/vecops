# ![alt text](https://datadog-opensource.imgix.net/img/vector.png?w=1200&h=630&auto=format&fit=fill)
Vector.dev Pipeline Monitoring System

This project provides a monitoring system for Vector.dev pipelines, along with sources and sinks, using a combination of Telnet, system commands, and HTTP health checks. It reads configuration from a YAML file, checks the health of services, and publishes alerts to a webhook if any service is down or errors are detected.

## Features

- Monitors the connection status of sources and sinks using Telnet.
- Checks the systemd service status and logs for errors.
- Performs HTTP health checks on specified pipelines.
- Publishes alerts to a specified webhook if any issues are detected.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 8 or higher
- Apache Maven
- A webhook URL for receiving alerts

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
```
## Building and Running

1. Clone the repository
```bat
git clone https://github.com/yourusername/vector-pipeline-monitoring.git
cd vector-pipeline-monitoring
```
2. Build the project using maven
```bat
mvn clean install
```
3. Run the application
```bat
java -jar target/vector-pipeline-monitoring-1.0-SNAPSHOT.jar
```

## Usage 
### Configuration
 Update the `services.yml` file with your source, sinks, pipeline, and service details.

### Monitoring
Run the application to start monitoring the configured services. The application will log the status and send alerts to the specified webhook if any issues are detected.

## Contributing
Contributions are welcome! Please fork this repository and submit a pull request for any features, bug fixes, or improvements.

## License
This project is licensed under the MIT License.

## Acknowledgements
- [Apache Commons](https://commons.apache.org/proper/commons-net/) Net for Telnet support.
- [Jackson Databind](https://github.com/FasterXML/jackson-databind) and [Jackson Dataformat](https://github.com/FasterXML/jackson-databind) YAML for YAML parsing.
- [Vector.dev](https://vector.dev) for the pipeline infrastructure.
