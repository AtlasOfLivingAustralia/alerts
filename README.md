# ALA Alerts

This is a small app responsible for sending email alerts when there are changes detected to endpoint web services (subscriptions).

Scheduling is handled by Quartz plugin in the app. It works like RSS - it checks the JSON service and reads the results and then compares that with the previously stored check and if there are new records, it triggers an email. Relies on records being sorted by date loaded. Works with any endpoint that can return date-sorted JSON output.

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
1. Create /data/alerts/config/alerts-config.properties
1. Use the template in ala-install to get the necessary values


### To check email sending on local dev environment
Run [smtp4dev](https://github.com/rnwood/smtp4dev) via Docker:

`docker run -p 3000:80 -p 2525:25 -d --name smtpdev rnwood/smtp4dev`

Emails will be sent on SMTP port 2525 (configure sending emails via `mail.enabled=true`, `grails.mail.port=2525` and `grails.mail.server=localhost`. Note: emails will not be delivered externally so you don't have to worry about spamming users.

You can view all sent emails via the smtp4dev UI on http://localhost:3000/, inlcuding HTML emails which are nicely displayed.


### 4.2.0 Release

Change logs
DB schema update:
``` ALTER TABLE alerts.query_result MODIFY logs TEXT NULL; ```

### 4.2.0 Release
No DB changes


### 4.3.1 Release
Apply new templates for other alerts

#### Change logs

Database update:

1. Annotations now uses its own template. To update the template, run the following query:

    ```update alerts.query set email_template="/email/annotations" where name="Annotations"```


2. My Annotations now uses its own template. To update the template, run the following query:

    ```update alerts.query set email_template="/email/myAnnotations" where name="My Annotations"```

* Check Annotation query to see if it works with the Api GW

    ```select * from alerts.query where name="Annotations"```


3. Annotation on records for Dataset / collections / species  now uses its "Annotation" template. To update the template, run the following query:

    ```       
              UPDATE alerts.query 
              SET email_template = '/email/annotations'
              WHERE name LIKE 'New annotations on%';
   ```
4. Species List Annotations now share the same template with datasets. To update the template, run the following query:
    Update fire_when_change to false for species list queries

    ```
    UPDATE alerts.property_path
    SET fire_when_change = false
    WHERE query_id IN (
        SELECT query_id 
        FROM query 
        WHERE email_template = '/email/specieslists'
    );
    ```

    ```update alerts.query set email_template="/email/datasets" where email_template='/email/specieslists' ```

5. Data Resource now share the same template with datasets. To update the template, run the following query:

    ```update alerts.query set email_template="/email/datasets" where email_template='/email/dataresource'; ```

6. Check base_url and query_path for the queries which name is "Annotations" and "My Annotations"
    

     if base_url is like:
      ```https://biocache.ala.org.au```
    
      and query_path is like:
    
      ```/ws/occurrences/search?fq=user_assertions:*&q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record```
    
      then they should be changed to:
    
      ```https://biocache.ala.org.au/ws```
    
      and
    
      ```/occurrences/search?fq=user_assertions:*&q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record```

```
    -- Update the base_url and query_path
    UPDATE alerts.query
    SET 
        base_url = CONCAT(base_url, '/ws'),      -- Add '/ws' to the end of base_url
        query_path = SUBSTRING(query_path, 4)    -- Remove '/ws' from the start of query_path
    WHERE 
        query_path LIKE '/ws%';                  -- Only apply if query_path starts with '/ws'
```

Update some queries using api.test.ala.org.au and api.ala.org.au
```
  UPDATE alerts.query
    SET 
        base_url = 'https://biocache-ws-test.ala.org.au/ws'      
    WHERE 
        base_url LIKE 'https://api.test.ala.org.au/occurrences%';      
```

```
  UPDATE alerts.query
    SET 
        base_url = 'https://biocache.ala.org.au/ws'      
    WHERE 
        base_url LIKE 'https://api.ala.org.au/occurrences%';     
```

