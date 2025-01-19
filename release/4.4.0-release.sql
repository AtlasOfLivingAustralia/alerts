-- One-time update for the 4.3.1 release:

-- 1. Annotations now uses its own template. To update the template, run the following query:
update alerts.query set email_template='/email/annotations' where name='Annotations';

--  2. My Annotations now uses its own template. To update the template, run the following query:
update alerts.query set email_template='/email/myAnnotations' where name='My Annotations';
--- check


-- 3. Annotation on records for Dataset / collections / species  now uses its "Annotation" template. To update the template, run the following query:

UPDATE alerts.query
SET email_template = '/email/annotations'
WHERE name LIKE 'New annotations on%';


--- disable fire_when_change for spatial layer
UPDATE alerts.property_path
SET fire_when_change = false
WHERE query_id IN (
    SELECT query_id
    FROM query
    WHERE email_template = '/email/layers'
);

-- 5. Data Resource using Collectory Service now share the same template with datasets. To update the template, run the following query:
-- 5.1
-- update alerts.query set email_template="/email/datasets" where email_template='/email/dataresource';


-- 6. Check base_url and query_path for the queries which name is "Annotations" and "My Annotations"

--      if base_url is like:
--       https://biocache.ala.org.au
--       and query_path is like:
--       /ws/occurrences/search?fq=user_assertions:*&q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record
--
--       then they should be changed to:
--       https://biocache.ala.org.au/ws
--       and
--       /occurrences/search?fq=user_assertions:*&q=last_assertion_date:[___DATEPARAM___%20TO%20*]&sort=last_assertion_date&dir=desc&pageSize=20&facets=basis_of_record

UPDATE alerts.query
SET
    base_url = CONCAT(base_url, '/ws'),      -- Add '/ws' to the end of base_url
    query_path = SUBSTRING(query_path, 4)    -- Remove '/ws' from the start of query_path
WHERE
        query_path LIKE '/ws%';                  -- Only apply if query_path starts with '/ws'


-- Update some queries using api.test.ala.org.au and api.ala.org.au

-- UPDATE alerts.query
-- SET
--     base_url = 'https://biocache-ws-test.ala.org.au/ws'
-- WHERE
--         base_url LIKE 'https://api.test.ala.org.au/occurrences%';


UPDATE alerts.query
SET
    base_url = 'https://biocache.ala.org.au/ws'
WHERE
        base_url LIKE 'https://api.ala.org.au/occurrences%';


--- Special cases for test environments:
--- Check if "Citizen science records with images" and "New images" are under 'biocacheImages' tab
--- If not, run the following queries to update them:
SELECT * FROM alerts.query where name='Citizen science records with images';
update alerts.query set email_template='/email/biocacheImages'  where name='Citizen science records with images';

SELECT * FROM alerts.query where name='New images';
update alerts.query set email_template='/email/biocacheImages'  where name='New images';

--- Update query for species list
--- Find the query id which need to be updated. ID:556
UPDATE `alerts`.`query`
    SET `base_url` = 'https://lists.ala.org.au/ws',
        `id_json_path` = 'dataResourceUid',
        `query_path` = '/speciesList?max=___MAX___&offset=___OFFSET___',
        `record_json_path` = '$.lists[*]',
        `email_template` = '/email/specieslist',
        `base_url_forui` = 'https://lists.ala.org.au'
    WHERE (`id` = 'Replace it');

-- 4. Species List Annotations now share the same template with datasets. To update the template, run the following query:
-- 4.1  Update fire_when_change to false for species list queries

--- NOT WORKING
--- ************************
UPDATE alerts.property_path
SET fire_when_change = false,
    json_path = '$.lists'
WHERE query_id = [species list query id: 556];


--- Update query of datasets to match query of datasetResource

--- If datasetResource query already exists, then update the query id of the subscribers to datasetResource query id
--- Find the query id which needs to migrate subscribers to datasetResource. e.g. query id: 7
select * from notification where query_id = [id:7];
--- find subscribers details
select * from user where id in (SELECT user_id FROM alerts.notification where query_id=[id]);

update query_id=[dataresourse id] where query_id=[id];
--- check if there are duplicate subscribers
--- delete datasets query by clicking on delete button in the query admin page

--- If datasetResource query does not exist, then create a new query with the following details
UPDATE `alerts`.`query`
SET `base_url` = 'https://biocache-ws.ala.org.au/ws',
    `id_json_path` = 'i18nCode',
    `query_path` = '/occurrences/search?q=*:*&facet=true&flimit=-1&facets=dataResourceUid&pageSize=0',
    `record_json_path` = '$.facetResults[0].fieldResult[*]',
    `email_template` = '/email/dataresource',
    `base_url_forui` = 'https://collections.ala.org.au'
WHERE (`id` = 'Replace it:7');

update property_path set json_path='$.facetResults[0].fieldResult', fire_when_change=false  where id=[property_path id:12];