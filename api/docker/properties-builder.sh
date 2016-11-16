#!/bin/bash

# if we are linked, use that info
if [ "$MONGO_PORT" != "" ]; then
  # Sample: MONGO_PORT=tcp://172.17.0.20:27017
  export SPRING_DATA_MONGODB_HOST=`echo $MONGO_PORT|sed 's;.*://\([^:]*\):\(.*\);\1;'`
  export SPRING_DATA_MONGODB_PORT=`echo $MONGO_PORT|sed 's;.*://\([^:]*\):\(.*\);\2;'`
fi

echo "SPRING_DATA_MONGODB_HOST: $SPRING_DATA_MONGODB_HOST"
echo "SPRING_DATA_MONGODB_PORT: $SPRING_DATA_MONGODB_PORT"


cat > dashboard.properties <<EOF
#Database Name - default is test
dbname=${SPRING_DATA_MONGODB_DATABASE:-dashboard}

#Database HostName - default is localhost
dbhost=${SPRING_DATA_MONGODB_HOST:-10.0.1.1}

#Database Port - default is 27017
dbport=${SPRING_DATA_MONGODB_PORT:-9999}

#Database Username - default is blank
dbusername=${SPRING_DATA_MONGODB_USERNAME:-db}

#Database Password - default is blank
dbpassword=${SPRING_DATA_MONGODB_PASSWORD:-dbpass}

#LDAP server URL (required if systemConfig.ldapAuthentication=true)
ldapUrl=${LDAP_URL:-ldap://localhost:389}
#LDAP base directory to search users from (not required, Example: 'ou=People')
ldapBase=${LDAP_BASE}
#Distinguished Name to use for binding (not required if LDAP server supports anonymous binding, Example: 'cn=user,dc=company,dc=com')
ldapBindDn=${LDAP_BIND_DN}
#Password for the above DN
ldapBindPassword=${LDAP_BIND_PASSWORD}
#The username attribute to use as search filter (not required, defaults to 'uid')
ldapUsernameAttribute=${LDAP_USERNAME_ATTRIBUTE}
#Property to indicate how to handle referrals (not required, Can be 'ignore', 'follow' or 'throw')
ldapReferral=${LDAP_REFERRAL}

feature.dynamicPipeline=${FEATURE_DYNAMIC_PIPELINE:-disabled}
systemConfig.multipleDeploymentServers=${CONFIG_GLOBAL_MULTIPLE_DEPLOYMENT_SERVERS:-false}
systemConfig.ldapAuthentication=${CONFIG_GLOBAL_LDAP_AUTHENTICATION:-false}
EOF
