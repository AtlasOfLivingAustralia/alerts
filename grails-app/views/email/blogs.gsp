<%@ page contentType="text/html"%>
<html>
  <head><title>Email alert from Atlas of Living Australia</title></head>
  <body>
    <h2>${title}</h2>
    <p>To view details, <a href="${moreInfo}">click here</a></p>
    <p>To manage your alerts, <a href="${stopNotification}">click here</a></p>
    <g:each in="${records}" var="blog">
      <h1>${blog.title}</h1>
      <g:if test="${blog.thumbnail != null}">
        <img src="${blog.thumbnail}" alt="${blog.title}">
      </g:if>
      <p>
        ${blog.excerpt}
      </p>
      <p>To view the complete blog, click <a href="${blog.url}">here</a></p>
    </g:each>
  </body>
</html>