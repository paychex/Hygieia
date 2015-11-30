package com.capitalone.dashboard.collector;

import com.capitalone.dashboard.model.*;
import com.capitalone.dashboard.util.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestOperations;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
public class DefaultAppDynamicsClient implements AppDynamicsClient {
    private static final Log LOG = LogFactory.getLog(DefaultAppDynamicsClient.class);

    private static final String BASE_API_URL_SUFFIX = "/controller/rest/applications";
    private static final String APPLICATION_LIST_SUFFIX = "?output=JSON";
    private static final int TIME_INTERVAL = 5;
    private static final String URL_RESOURCE_DETAILS = "/metric-data?metric-path=Overall%20Application%20Performance|*&time-range-type=BEFORE_NOW&duration-in-mins=" + TIME_INTERVAL + "N&rollup=true&output=JSON";

    private static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    private static final String ID = "id";
    private static final String NAME = "name";
    private static final String KEY = "key";
    private static final String VERSION = "version";
    private static final String MSR = "msr";
    private static final String ALERT = "alert";
    private static final String ALERT_TEXT = "alert_text";
    private static final String VALUE = "val";
    private static final String FORMATTED_VALUE = "frmt_val";
    private static final String STATUS_WARN = "WARN";
    private static final String STATUS_ALERT = "ALERT";
    private static final String DATE = "date";

    private final RestOperations rest;
    private final AppDynamicsSettings sonarSettings;

    @Autowired
    public DefaultAppDynamicsClient(Supplier<RestOperations> restOperationsSupplier, AppDynamicsSettings settings) {
        this.rest = restOperationsSupplier.get();
        this.sonarSettings = settings;
    }

    @Override
    public List<AppDynamicsApplication> getApplications(String instanceUrl) {
        List<AppDynamicsApplication> projects = new ArrayList<>();
        String url = instanceUrl + BASE_API_URL_SUFFIX + APPLICATION_LIST_SUFFIX;

        try {

            for (Object obj : parseAsArray(url)) {
                JSONObject prjData = (JSONObject) obj;

                AppDynamicsApplication application = new AppDynamicsApplication();
                application.setInstanceUrl(instanceUrl);
                application.setApplicationId(str(prjData, ID));
                application.setApplicationName(str(prjData, NAME));
                projects.add(application);
            }

        } catch (ParseException e) {
            LOG.error("Could not parse response from: " + url, e);
        } catch (RestClientException rce) {
            LOG.error(rce);
        }

        return projects;
    }

    @Override
    public ApplicationPerformance currentAPMMetrics(AppDynamicsApplication application) {
        String url = String.format(
                application.getInstanceUrl() + URL_RESOURCE_DETAILS, application.getApplicationId(), sonarSettings.getMetrics());

        try {
            JSONArray jsonArray = parseAsArray(url);

            if (!jsonArray.isEmpty()) {
                JSONObject prjData = (JSONObject) jsonArray.get(0);

                ApplicationPerformance applicationPerformance = new ApplicationPerformance();
                applicationPerformance.setName(str(prjData, NAME));
                applicationPerformance.setUrl(application.getInstanceUrl() + "/dashboard/index/" + application.getApplicationId());

                applicationPerformance.setTimestamp(timestamp(prjData, DATE));
                applicationPerformance.setVersion(str(prjData, VERSION));

                for (Object metricObj : (JSONArray) prjData.get(MSR)) {
                    JSONObject metricJson = (JSONObject) metricObj;

                    ApplicationPerformanceMetric metric = new ApplicationPerformanceMetric(str(metricJson, KEY));
                    metric.setValue(metricJson.get(VALUE));
                    metric.setFormattedValue(str(metricJson, FORMATTED_VALUE));
                    metric.setStatus(metricStatus(str(metricJson, ALERT)));
                    metric.setStatusMessage(str(metricJson, ALERT_TEXT));
                    applicationPerformance.getMetrics().add(metric);
                }

                return applicationPerformance;
            }

        } catch (ParseException e) {
            LOG.error("Could not parse response from: " + url, e);
        } catch (RestClientException rce) {
            LOG.error(rce);
        }

        return null;
    }

    private JSONArray parseAsArray(String url) throws ParseException {
        return (JSONArray) new JSONParser().parse(rest.getForObject(url, String.class));
    }

    private long timestamp(JSONObject json, String key) {
        Object obj = json.get(key);
        if (obj != null) {
            try {
                return new SimpleDateFormat(DATE_FORMAT).parse(obj.toString()).getTime();
            } catch (java.text.ParseException e) {
                LOG.error(obj + " is not in expected format " + DATE_FORMAT, e);
            }
        }
        return 0;
    }

    private String str(JSONObject json, String key) {
        Object obj = json.get(key);
        return obj == null ? null : obj.toString();
    }
    @SuppressWarnings("unused")
    private Integer integer(JSONObject json, String key) {
        Object obj = json.get(key);
        return obj == null ? null : (Integer) obj;
    }

    @SuppressWarnings("unused")
    private BigDecimal decimal(JSONObject json, String key) {
        Object obj = json.get(key);
        return obj == null ? null : new BigDecimal(obj.toString());
    }

    @SuppressWarnings("unused")
    private Boolean bool(JSONObject json, String key) {
        Object obj = json.get(key);
        return obj == null ? null : Boolean.valueOf(obj.toString());
    }

    private ApplicationPerformanceMetricStatus metricStatus(String status) {
        if (StringUtils.isBlank(status)) {
            return ApplicationPerformanceMetricStatus.Ok;
        }

        switch(status) {
            case STATUS_WARN:  return ApplicationPerformanceMetricStatus.Warning;
            case STATUS_ALERT: return ApplicationPerformanceMetricStatus.Alert;
            default:           return ApplicationPerformanceMetricStatus.Ok;
        }
    }
}
