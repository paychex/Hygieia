package com.capitalone.dashboard;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties
public class ApiSettings {
    /**
     * TODO The property name 'key' is too vague. This key is used only for encryption. Would suggest to rename it to
     * encryptionKey to be specific. For now (for backwards compatibility) keeping it as it was.
     */
    private String key;
    private boolean logRequest;
    
    private String ldapUrl;
    private String ldapBase;
    private String ldapReferral;
    private String ldapBindDn;
    private String ldapBindPassword;
    private String ldapUsernameAttribute;
    
    // Start global config
    /*
     * Location to place configurations that are consumed by the UI and API. May be moved into a separate location
     * (such as a collection in mongo) in the future.
     */
    @Value("${systemConfig.multipleDeploymentServers:false}")
    private boolean multipleDeploymentServers;
    
    @Value("${systemConfig.ldapAuthentication:false}")
    private boolean ldapAuthentication;
    // End global config
    
    public String getKey() {
        return key;
    }

    public void setKey(final String key) {
        this.key = key;
    }

    public boolean isLogRequest() {
        return logRequest;
    }

    public void setLogRequest(boolean logRequest) {
        this.logRequest = logRequest;
    }
    
    public String getLdapUrl() {
        return ldapUrl;
    }

    public void setLdapUrl(final String ldapUrl) {
        this.ldapUrl = ldapUrl;
    }
    
    public String getLdapBase() {
        return ldapBase;
    }

    public void setLdapBase(final String ldapBase) {
        this.ldapBase = ldapBase;
    }
    
    public String getLdapReferral() {
        return ldapReferral;
    }

    public void setLdapReferral(final String ldapReferral) {
        this.ldapReferral = ldapReferral;
    }
    
    public String getLdapBindDn() {
        return ldapBindDn;
    }

    public void setLdapBindDn(final String ldapBindDn) {
        this.ldapBindDn = ldapBindDn;
    }
    
    public String getLdapBindPassword() {
        return ldapBindPassword;
    }

    public void setLdapBindPassword(final String ldapBindPassword) {
        this.ldapBindPassword = ldapBindPassword;
    }
    
    public String getLdapUsernameAttribute() {
        return ldapUsernameAttribute;
    }

    public void setLdapUsernameAttribute(final String ldapUsernameAttribute) {
        this.ldapUsernameAttribute = ldapUsernameAttribute;
    }
    
    public boolean isMultipleDeploymentServers() {
    	return multipleDeploymentServers;
    }
    
    public boolean isLdapAuthentication() {
    	return ldapAuthentication;
    }
}
