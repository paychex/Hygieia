package com.capitalone.dashboard.service;

import com.capitalone.dashboard.ApiSettings;
import com.capitalone.dashboard.model.Authentication;
import com.capitalone.dashboard.repository.AuthenticationRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.DirContextSource;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.ldap.support.LdapUtils;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationRepository authenticationRepository;
    private final ApiSettings apiSettings;
    private final LdapTemplate ldapTemplate;

    @Autowired
    public AuthenticationServiceImpl(
            AuthenticationRepository authenticationRepository, ApiSettings apiSettings) {
        this.authenticationRepository = authenticationRepository;
        this.apiSettings = apiSettings;
        
        DirContextSource contextSource = new DirContextSource();     
        contextSource.setUrl(this.apiSettings.getLdapUrl());
        if (!StringUtils.isEmpty(this.apiSettings.getLdapBase())) {
        	contextSource.setBase(this.apiSettings.getLdapBase());
        }
        if (!StringUtils.isEmpty(this.apiSettings.getLdapReferral())) {
        	contextSource.setReferral(this.apiSettings.getLdapReferral());
        }
        if (!StringUtils.isEmpty(this.apiSettings.getLdapBindDn())) {
        	contextSource.setUserDn(this.apiSettings.getLdapBindDn());
        }
        if (!StringUtils.isEmpty(this.apiSettings.getLdapBindPassword())) {
        	contextSource.setPassword(this.apiSettings.getLdapBindPassword());
        }
        contextSource.afterPropertiesSet();
        
        this.ldapTemplate = new LdapTemplate(contextSource);
    }

    @Override
    public Iterable<Authentication> all() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Authentication get(ObjectId id) {

        Authentication authentication = authenticationRepository.findOne(id);
        return authentication;
    }

    @Override
    public String create(String username, String password) {
    	if (!apiSettings.isLdapAuthentication()) {
	        Authentication authentication = new Authentication(username, password);
	        try {
	            authenticationRepository.save(authentication);
	            return "User is created";
	        } catch (DuplicateKeyException e) {
	            return "User already exists";
	        }
    	} else {
    		return "LDAP authentication enabled";
    	}
    }

    @Override
    public String update(String username, String password) {
    	if (!apiSettings.isLdapAuthentication()) {
	        Authentication authentication = authenticationRepository.findByUsername(username);
	        if (null != authentication) {
	            authentication.setPassword(password);
	            authenticationRepository.save(authentication);
	            return "User is updated";
	        } else {
	            return "User Does not Exist";
	        }
    	} else {
    		return "LDAP authentication enabled";
    	}
    }

    @Override
    public void delete(ObjectId id) {
    	if (!apiSettings.isLdapAuthentication()) {
	        Authentication authentication = authenticationRepository.findOne(id);
	        if (authentication != null) {
	            authenticationRepository.delete(authentication);
	        }
    	}
    }

    @Override
    public void delete(String username) {
    	if (!apiSettings.isLdapAuthentication()) {
	        Authentication authentication = authenticationRepository
	                .findByUsername(username);
	        if (authentication != null) {
	            authenticationRepository.delete(authentication);
	        }
    	}
    }

    @Override
    public boolean authenticate(String username, String password) {
        boolean flag = false;
        
        if (apiSettings.isLdapAuthentication()) {
        	EqualsFilter filter = new EqualsFilter(StringUtils.isEmpty(apiSettings.getLdapUsernameAttribute()) ? "uid" : apiSettings.getLdapUsernameAttribute(), username);
        	flag = ldapTemplate.authenticate(LdapUtils.emptyLdapName(), filter.toString(), password);
        } else {
	        Authentication authentication = authenticationRepository.findByUsername(username);
	
	        if (authentication != null && authentication.checkPassword(password)) {
	            flag = true;
	        }
        }
        
        return flag;
    }

}