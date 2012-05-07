<%@ page contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Web services Tests</title>
</head>
<body>

<h1>Custom Alert Test links</h1>
<h2>Create Alerts</h2>
<ul>
  <li>
    <g:link controller="webservice" action="createTaxonAlert"
            params="${[taxonGuid:'urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537', taxonName:'Macropus rufus : Red Kangaroo']}">
      Create taxon alert for Red Kangaroo
    </g:link>
  </li>
  <li>
    <g:link controller="webservice" action="createRegionAlert"
            params="${[regionName:'New South Wales', layerId:'state']}">
      Create region alert for NSW
    </g:link>
  </li>
  <li>
    <g:link controller="webservice" action="createTaxonRegionAlert"
            params="${[taxonGuid:'urn:lsid:biodiversity.org.au:afd.taxon:17c9fd64-3c07-4df5-a33d-eda1e065e99f', taxonName:'Insecta', regionName:'New South Wales', layerId:'state']}">
      Create taxon & region alert for Insects and NSW
    </g:link>
  </li>
  <li>
    <g:link controller="webservice" action="createSpeciesGroupRegionAlert"
            params="${[speciesGroup:'Insects', regionName:'New South Wales', layerId:'state']}">
      Create species group & region alert for Insects and NSW
    </g:link>
  </li>

  <li>
    <g:link controller="webservice" action="createBiocacheNewRecordsAlert" params="${[
            webserviceQuery:'/ws/occurrences/search?q=*:*',
            uiQuery:'/occurrences/search?q=*:*',
            queryDisplayName:'AVH',
            baseUrlForWS:'http://biocache.ala.org.au',
            baseUrlForUI:'http://avh-demo.ala.org.au',
            resourceName:'Australian Virtual Herbarium'
    ]}">Create an AdHoc biocache alerts for new records AVH records
    </g:link>
  </li>

  <li>
    <g:link controller="webservice" action="createBiocacheNewAnnotationsAlert" params="${[
            webserviceQuery:'/ws/occurrences/search?q=*:*',
            uiQuery:'/occurrences/search?q=*:*',
            queryDisplayName:'Annotations in AVH',
            baseUrlForWS:'http://biocache.ala.org.au',
            baseUrlForUI:'http://avh-demo.ala.org.au',
            resourceName:'Australian Virtual Herbarium'
    ]}">Create an AdHoc biocache alerts for new annotations AVH records
    </g:link>
  </li>
</ul>

<h2>Check Alerts - for embedding JSON</h2>
<ul>
  <li>
    <g:link controller="webservice" action="taxonAlerts"
            params="${[taxonGuid:'urn:lsid:biodiversity.org.au:afd.taxon:aa745ff0-c776-4d0e-851d-369ba0e6f537', taxonName:'Macropus rufus : Red Kangaroo',
            redirect:'http://bie.ala.org.au/species/Macropus+rufus']}">
      Create taxon alert for Red Kangaroo
    </g:link>
  </li>
  <li>
    <g:link controller="webservice" action="regionAlerts"
            params="${[regionName:'New South Wales', layerId:'state', redirect:'http://regions.ala.org.au/states/New South Wales']}">
      Create region alert for NSW
    </g:link>
  </li>
  <li>
    <g:link controller="webservice" action="taxonRegionAlerts"
            params="${[taxonGuid:'urn:lsid:biodiversity.org.au:afd.taxon:17c9fd64-3c07-4df5-a33d-eda1e065e99f', taxonName:'Insecta', regionName:'New South Wales', layerId:'state', redirect:'http://regions.ala.org.au/states/New South Wales']}">
      Create taxon & region alert for Insects and NSW
    </g:link>
  </li>
  <li>
    <g:link controller="webservice" action="speciesGroupRegionAlerts"
            params="${[speciesGroup:'Insects', regionName:'New South Wales', layerId:'state', redirect:'http://regions.ala.org.au/states/New South Wales']}">
      Create species group & region alert for Insects and NSW
    </g:link>
  </li>
</ul>

</body>
</html>