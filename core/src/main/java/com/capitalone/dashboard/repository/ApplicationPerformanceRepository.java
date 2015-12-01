package com.capitalone.dashboard.repository;

import com.capitalone.dashboard.model.ApplicationPerformance;
import com.capitalone.dashboard.model.CodeQuality;
import org.bson.types.ObjectId;
import org.springframework.data.querydsl.QueryDslPredicateExecutor;
import org.springframework.data.repository.CrudRepository;

/**
 * Repository for {@link CodeQuality} data.
 */
public interface ApplicationPerformanceRepository extends CrudRepository<ApplicationPerformance, ObjectId>, QueryDslPredicateExecutor<ApplicationPerformance> {

    /**
     * Finds the {@link ApplicationPerformance} data point at the given timestamp for a specific
     * {@link com.capitalone.dashboard.model.CollectorItem}.
     *
     * @param collectorItemId collector item id
     * @param timestamp timstamp
     * @return a {@link ApplicationPerformance}
     */
    CodeQuality findByCollectorItemIdAndTimestamp(ObjectId collectorItemId, long timestamp);
}
