# ALA Alerts

This is a small app responsible for sending email alerts when there are changes detected to endpoint web services (subscriptions).

Scheduling is handled by Quartz plugin in the app. It works like RSS - it checks the JSON service and reads the results and then compares that with the previously stored check and if there are new records, it triggers an email. Relies on records being sorted by date loaded. Works with any endpoint that can return date-sorted JSON output.

## Grails 6 updates IMPORTANT >= version 5.2.0 

In Grails 6, Liquibase is bootstrapped directly by Spring Boot during application startup.  
This happens **before** the Grails `external-config` plugin is invoked.

As a result, any beans managed by Spring (such as **Liquibase** and **Quartz**) must have their required
configuration available **at Spring initialization time**.

To support this, the safest way is that database-related properties are defined in an external Spring configuration file,
for example:

```yaml
DB_URL: jdbc:mysql://localhost:3309/alerts
DB_USER: alerts_user
DB_PASSWORD: password
DB_DRIVER: com.mysql.cj.jdbc.Driver
```
The external configuration file is referenced in application.yml using:, 
```spring:
    config:
        import: "optional:file:/data/alerts/config/alerts-config.yml"
```
Spring can also load this external configuration via an environment variable, allowing you to specify your own file, for example:

```
SPRING_CONFIG_ADDITIONAL_LOCATION=file:/data/alerts/config/alerts-config.yml
```
If neither the import nor the environment variable is provided, Spring-managed components will not see the required configuration and may fall back to
defaults (for example, H2 or Quartz RAMJobStore).

## IMPORTANT

To align with the Grails 6 initialization process, the Grails externalConfig plugin has been removed.
All properties previously defined in alerts-config.properties have been migrated to alerts-config.yml and are now loaded exclusively through Spring using SPRING_CONFIG_ADDITIONAL_LOCATION.

# Build status

[![Build Status](https://api.travis-ci.com/AtlasOfLivingAustralia/alerts.svg?branch=develop)](https://app.travis-ci.com/github/AtlasOfLivingAustralia/alerts)

# Dev environment set up 

## Recommend: 
Go folder ```./docker```, run ```docker-compose up```

Docker-compose runs MySql 8.1 on 3306, smtp4dev on 3000, 2525
  

## Alternative, you can install your own environment

What the docker-compose does:
1. Install MySql
1. Log in as root
1. ```create user 'alerts_user'@'localhost' identified as 'alerts_user';```
1. ```grant all privileges on *.* to 'alerts_user'@'localhost';```
1. ```create database alerts```
1. Create /data/alerts/config/alerts-config.yml
1. Use the template in ala-install to get the necessary values


### To check email sending on local dev environment
Run [smtp4dev](https://github.com/rnwood/smtp4dev) via Docker:

`docker run -p 3000:80 -p 2525:25 -d --name smtpdev rnwood/smtp4dev`

Emails will be sent on SMTP port 2525 (configure sending emails via `mail.enabled=true`, `grails.mail.port=2525` and `grails.mail.server=localhost`. Note: emails will not be delivered externally so you don't have to worry about spamming users.

You can view all sent emails via the smtp4dev UI on http://localhost:3000/, inlcuding HTML emails which are nicely displayed.

### 5.2.0 Release

Change logs:
Liquibase were introduced to manage DB changes
Spring uses /data/alerts/config/alerts-config.yaml as default external config file


### 4.2.0 Release

Change logs
DB schema update:
``` ALTER TABLE alerts.query_result MODIFY logs TEXT NULL; ```

### 4.3.0 Release
No DB changes


### 4.4.0 Release
Apply new templates for alerts

#### Change logs
Database update: Check release/4.4.0-release.sql



