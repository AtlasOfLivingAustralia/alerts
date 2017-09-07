<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from ${query.resourceName}</title></head>
  <style>
  /* Gallery styling */
  .imgCon {
      display: inline-block;
      /* margin-right: 8px; */
      text-align: center;
      line-height: 1.3em;
      background-color: #DDD;
      color: #DDD;
      font-size: 12px;
      /*text-shadow: 2px 2px 6px rgba(255, 255, 255, 1);*/
      /* padding: 5px; */
      /* margin-bottom: 8px; */
      margin: 2px 4px 2px 0;
      position: relative;
  }
  .imgCon img {
      height: 100px;
      min-width: 180px;
  }
  .imgCon .meta {
      opacity: 0.6;
      position: absolute;
      bottom: 0;
      left: 0;
      right: 0;
      overflow: hidden;
      text-align: left;
      padding: 4px 5px 2px 5px;
      background-color: #DDD;
      color: #000;
      width:100%;
      font-weight:bold;
  }
  .imgCon .brief {
      color: black;
      background-color: white;
  }
  .imgCon .detail {
      color: white;
      background-color: black;
      opacity: 0.7;
  }
  </style>
  <body>
    <h2>${title}</h2>
    <p>
      <g:message code="${message}" default="${message}" args="${[totalRecords]}"/>
    </p>
    <p>To <b>view all</b> the records that have added/changed, <a href="${moreInfo}"><button>click here</button></a></p>
    <p>To <b>disable this alert</b> or to manage your alerts, <a href="${stopNotification}"><button>click here</button></a>.
      Your current settings are to receive alerts ${frequency}.
    </p>
    <style type="text/css">
    	body { font-family:Arial; }
    	table { border-collapse: collapse; border: 1px solid #CCC; padding:2px; }
		td, th { border: 1px solid #CCC; padding:4px; }
        img { max-width:140px; max-height:160px; }
        .detail { font-size: 11px;}
        .imageCol { padding:0; margin:0; }
        .linkCell { }
    </style>
    <g:if test="${records}">
    <div style="display: block;">
    <g:each in="${records}" var="oc">
      <div class="imgCon">
        <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}"><img src="${oc.smallImageUrl}" alt="Image for record ${oc.scientificName}"/></a>
        <div class="meta"><i>${oc.scientificName}</i><br/>${oc.vernacularName}</div>
      </div>
    </g:each>
    </div>
    </g:if>

    <g:render template="/email/unsubscribe"/>

  </body>
</html>