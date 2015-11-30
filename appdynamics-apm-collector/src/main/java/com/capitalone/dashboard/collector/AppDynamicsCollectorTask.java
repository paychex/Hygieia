package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.repository.*;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class AppDynamicsCollectorTask extends CollectorTask<AppDynamicsCollector> {
    private static final Log LOG = LogFactory.getLog(AppDynamicsCollectorTask.class);

    private final AppDynamicsCollectorRepository appDynamicsCollectorRepository;
    private final AppDynamicsApplicationRepository appDynamicsApplicationRepository;
    private final ApplicationPerformanceRepository applicationPerformanceRepository;
    private final AppDynamicsClient appDynamicsClient;
    private final AppDynamicsSettings appDynamicsSettings;
    private final ComponentRepository dbComponentRepository;
    private final int CLEANUP_INTERVAL = 3600000;

    @Autowired
    public AppDynamicsCollectorTask(TaskScheduler taskScheduler,
                                    AppDynamicsCollectorRepository appDynamicsCollectorRepository,
                                    AppDynamicsApplicationRepository appDynamicsApplicationRepository,
                                    ApplicationPerformanceRepository applicationPerformanceRepository,
                                    AppDynamicsSettings appDynamicsSettings,
                                    AppDynamicsClient appDynamicsClient,
                                    ComponentRepository dbComponentRepository) {
        super(taskScheduler, "AppDynamics");
        this.appDynamicsCollectorRepository = appDynamicsCollectorRepository;
        this.appDynamicsApplicationRepository = appDynamicsApplicationRepository;
        this.applicationPerformanceRepository = applicationPerformanceRepository;
        this.appDynamicsClient = appDynamicsClient;
        this.appDynamicsSettings = appDynamicsSettings;
        this.dbComponentRepository = dbComponentRepository;
    }

    @Override
    public AppDynamicsCollector getCollector() {
        return AppDynamicsCollector.prototype(appDynamicsSettings.getServers());
    }

    @Override
    public BaseCollectorRepository<AppDynamicsCollector> getCollectorRepository() {
        return appDynamicsCollectorRepository;
    }

    @Override
    public String getCron() {
        return appDynamicsSettings.getCron();
    }

    @Override
    public void collect(AppDynamicsCollector collector) {
        long start = System.currentTimeMillis();

		// Clean up every hour
		if ((start - collector.getLastExecuted()) > CLEANUP_INTERVAL) {
			clean(collector);
		}
        for (String instanceUrl : collector.getSonarServers()) {
            logInstanceBanner(instanceUrl);



            List<AppDynamicsApplication> projects = appDynamicsClient.getApplications(instanceUrl);
            int projSize = ((projects != null) ? projects.size() : 0);
            log("Fetched projects   " + projSize , start);

            addNewProjects(projects, collector);

            refreshData(enabledApplications(collector, instanceUrl));

            log("Finished", start);
        }
    }


	/**
	 * Clean up unused sonar collector items
	 *
	 * @param collector
	 *            the {@link AppDynamicsCollector}
	 */

	private void clean(AppDynamicsCollector collector) {
		Set<ObjectId> uniqueIDs = new HashSet<>();
		for (com.capitalone.dashboard.model.Component comp : dbComponentRepository
				.findAll()) {
			if (comp.getCollectorItems() != null && !comp.getCollectorItems().isEmpty()) {
				List<CollectorItem> itemList = comp.getCollectorItems().get(
						CollectorType.CodeQuality);
				if (itemList != null) {
					for (CollectorItem ci : itemList) {
						if (ci != null && ci.getCollectorId().equals(collector.getId())){
							uniqueIDs.add(ci.getId());
						}
					}
				}
			}
		}
		List<AppDynamicsApplication> jobList = new ArrayList<>();
		Set<ObjectId> udId = new HashSet<>();
		udId.add(collector.getId());
		for (AppDynamicsApplication job : appDynamicsApplicationRepository.findByCollectorIdIn(udId)) {
			if (job != null) {
				job.setEnabled(uniqueIDs.contains(job.getId()));
				jobList.add(job);
			}
		}
        appDynamicsApplicationRepository.save(jobList);
	}

    private void refreshData(List<AppDynamicsApplication> appDynamicsApplications) {
        long start = System.currentTimeMillis();
        int count = 0;

        for (AppDynamicsApplication application : appDynamicsApplications) {
            ApplicationPerformance codeQuality = appDynamicsClient.currentAPMMetrics(application);
            if (codeQuality != null && isNewPerformanceData(application, codeQuality)) {
                codeQuality.setCollectorItemId(application.getId());
                applicationPerformanceRepository.save(codeQuality);
                count++;
            }
        }

        log("Updated", start, count);
    }

    private List<AppDynamicsApplication> enabledApplications(AppDynamicsCollector collector, String instanceUrl) {
        return appDynamicsApplicationRepository.findEnabledApplications(collector.getId(), instanceUrl);
    }

    private void addNewProjects(List<AppDynamicsApplication> projects, AppDynamicsCollector collector) {
        long start = System.currentTimeMillis();
        int count = 0;

        for (AppDynamicsApplication project : projects) {

            if (isNewProject(collector, project)) {
                project.setCollectorId(collector.getId());
                project.setEnabled(false);
                project.setDescription(project.getApplicationName());
                appDynamicsApplicationRepository.save(project);
                count++;
            }
        }
        log("New projects", start, count);
    }

    private boolean isNewProject(AppDynamicsCollector collector, AppDynamicsApplication application) {
        return appDynamicsApplicationRepository.findSonarProject(
                collector.getId(), application.getInstanceUrl(), application.getApplicationId()) == null;
    }

    private boolean isNewPerformanceData(AppDynamicsApplication project, ApplicationPerformance applicationPerformance) {
        return applicationPerformanceRepository.findByCollectorItemIdAndTimestamp(
                project.getId(), applicationPerformance.getTimestamp()) == null;
    }

    private void log(String marker, long start) {
        log(marker, start, null);
    }

    private void log(String text, long start, Integer count) {
        long end = System.currentTimeMillis();
        String elapsed = ((end - start) / 1000) + "s";
        String token2 = "";
        String token3;
        if (count == null) {
            token3 = StringUtils.leftPad(elapsed, 30 - text.length());
        } else {
            String countStr = count.toString();
            token2 = StringUtils.leftPad(countStr, 20 - text.length() );
            token3 = StringUtils.leftPad(elapsed, 10 );
        }
        LOG.info(text + token2 + token3);
    }

    private void logInstanceBanner(String instanceUrl) {
        LOG.info("------------------------------");
        LOG.info(instanceUrl);
        LOG.info("------------------------------");
    }
}
