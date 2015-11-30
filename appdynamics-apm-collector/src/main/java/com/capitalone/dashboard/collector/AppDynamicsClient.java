package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.AppDynamicsApplication;
import com.capitalone.dashboard.model.ApplicationPerformance;

import java.util.List;

public interface AppDynamicsClient {

    List<AppDynamicsApplication> getApplications(String instanceUrl);

    ApplicationPerformance currentAPMMetrics(AppDynamicsApplication application);

}
