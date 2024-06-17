package com.vecops;

import java.util.List;

class Pipeline {
    public String name;
    public int port;
}

class Source {
    public String name;
    public String host;
    public int port;
}

class Sink {
    public String name;
    public String host;
    public int port;
}

class Service {
    public List<String> unit;
}

class Webhook {
    public String url;
}

class Config {
    public List<Pipeline> pipelines;
    public List<Source> sources;
    public List<Sink> sinks;
    public Service service;
    public Webhook webhook;
}
