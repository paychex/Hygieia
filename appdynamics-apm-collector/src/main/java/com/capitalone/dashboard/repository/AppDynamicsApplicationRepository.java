package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.AppDynamicsApplication;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface AppDynamicsApplicationRepository extends BaseCollectorItemRepository<AppDynamicsApplication> {

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, options.projectId : ?2}")
    AppDynamicsApplication findSonarProject(ObjectId collectorId, String instanceUrl, String projectId);

    @Query(value="{ 'collectorId' : ?0, options.instanceUrl : ?1, enabled: true}")
    List<AppDynamicsApplication> findEnabledApplications(ObjectId collectorId, String instanceUrl);
}
