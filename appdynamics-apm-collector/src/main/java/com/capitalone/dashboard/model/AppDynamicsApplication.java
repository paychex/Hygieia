package com.capitalone.dashboard.model;

public class AppDynamicsApplication extends CollectorItem {
    private static final String INSTANCE_URL = "instanceUrl";
    private static final String APPLICATION_NAME = "applicationName";
    private static final String APPLICATION_ID = "applicationId";

    public String getInstanceUrl() {
        return (String) getOptions().get(INSTANCE_URL);
    }

    public void setInstanceUrl(String instanceUrl) {
        getOptions().put(INSTANCE_URL, instanceUrl);
    }

    public String getApplicationId() {
        return (String) getOptions().get(APPLICATION_ID);
    }

    public void setApplicationId(String id) {
        getOptions().put(APPLICATION_ID, id);
    }

    public String getApplicationName() {
        return (String) getOptions().get(APPLICATION_NAME);
    }

    public void setApplicationName(String name) {
        getOptions().put(APPLICATION_NAME, name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AppDynamicsApplication that = (AppDynamicsApplication) o;
        return getApplicationId().equals(that.getApplicationId()) && getInstanceUrl().equals(that.getInstanceUrl());
    }

    @Override
    public int hashCode() {
        int result = getInstanceUrl().hashCode();
        result = 31 * result + getApplicationId().hashCode();
        return result;
    }
}
