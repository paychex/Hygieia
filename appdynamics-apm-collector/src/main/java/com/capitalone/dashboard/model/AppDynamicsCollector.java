package com.capitalone.dashboard.model;

import java.util.ArrayList;
import java.util.List;

public class AppDynamicsCollector extends Collector {
    private List<String> sonarServers = new ArrayList<>();

    public List<String> getSonarServers() {
        return sonarServers;
    }

    public static AppDynamicsCollector prototype(List<String> servers) {
        AppDynamicsCollector protoType = new AppDynamicsCollector();
        protoType.setName("Sonar");
        protoType.setCollectorType(CollectorType.CodeQuality);
        protoType.setOnline(true);
        protoType.setEnabled(true);
        protoType.getSonarServers().addAll(servers);
        return protoType;
    }
}
