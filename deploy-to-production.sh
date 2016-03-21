#!/bin/bash
# Simple script for start
export JAVA_HOME=/opt/java/production
cd /opt/maven/builds/OpenWayback-devel
echo "using production build cofiguration"
cp pom.xml.production pom.xml
cp src/main/webapp/WEB-INF/wayback.xml.production src/main/webapp/WEB-INF/wayback.xml
mvn package
echo "reverting to development build info"
cp pom.xml.development pom.xml
cp src/main/webapp/WEB-INF/wayback.xml.development src/main/webapp/WEB-INF/wayback.xml
trap " " INT
echo "making production WAR backkup into ~/, removing production WAR, press ctr+c after WAR is removed"
cp /opt/tomcat/production/webapps/ROOT.war ~/ROOT.war.`date -Iseconds` && rm /opt/tomcat/production/webapps/ROOT.war; tail -f /opt/tomcat/production/logs/catalina.out
trap - INT
echo "copying builded WAR into production tomcat, ctr+c after WAR is deployed"
cp /opt/maven/builds/OpenWayback-devel/target/openwayback-production-2.3.0.war /opt/tomcat/production/webapps/ROOT.war && tail -f /opt/tomcat/production/logs/catalina.out
