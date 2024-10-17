<%@ page contentType="text/html" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta charset="UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge">
    <meta name="format-detection" content="telephone=no">
    <meta name="x-apple-disable-message-reformatting">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>${title}</title>
  </head>
  <body style="background-color: #f4f4f4;padding: 0;line-height: 1.5;-webkit-font-smoothing: antialiased;-webkit-text-size-adjust: 100%;-ms-text-size-adjust: 100%;width: 100% !important;margin: 0 !important;">
    <div class="main-shadow-div default-font" style="vertical-align: top;font-family: 'Lato', Helvetica, Arial, sans-serif;font-size: 16px;font-weight: 500;line-height: 1.42;letter-spacing: -0.4px;mso-table-lspace: 0;mso-table-rspace: 0;box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);width: 100%;max-width: 620px;margin: 20px auto 0;">
      <div class="center-white-div" style="display: flex;vertical-align: top;justify-content: center;align-items: center;background-color: #ffffff;padding: 20px 20px 10px 20px;">
        <a href="https://www.ala.org.au" style="text-decoration: none;">
        <img src="${grailsApplication.config.grails.serverURL + '/assets/email/logo-dark.png'}" height="60" alt="" style="border: 0;line-height: 100%;outline: 0;">
        </a>
      </div>
      <div class="background-image-div padding " style="padding: 20px 10px 20px 10px;vertical-align: top; text-align:center;background-image: url(${grailsApplication.config.grails.serverURL}/assets/email/banner-ocean.png);">
        <div class="default-font large-white-font" style="vertical-align: top;font-family: 'Lato', Helvetica, Arial, sans-serif;font-size: 24px;font-weight: 700;line-height: 1.42;letter-spacing: -0.4px;color: #ffffff;">Latest ALA Blog Updates</div>
        <br>
        <div class="default-font" style="vertical-align: top;font-family: 'Lato', Helvetica, Arial, sans-serif;font-size: 16px;font-weight: 500;line-height: 1.42;letter-spacing: -0.4px;color: #ffffff;">
          <b>${new SimpleDateFormat("dd MMMM yyyy").format(new Date())}</b>
        </div>
      </div>
      <g:each status="i" in="${records}" var="blog">
        <div class="blog-div" style="padding: 20px 20px 20px 20px;background-color: white;display: flex;flex-direction: row;justify-content: center;">
          <div style="display: inline-flex; flex-direction: row; justify-content: space-between; flex-grow: 1; ">
            %{--            
            <div name="thumbnail-image" class="blog-thumbnail-div" style="width: 30%;padding: 10px 0 10px 0;align-items: center;max-width: 130px;">
              --}%
              %{--              
              <g:if test="${blog?._links?.'wp:featuredmedia'}">--}%
                %{--                <%--}%
                  %{--                  if (blog._links['wp:featuredmedia'].size() > 0) {--}%
                  %{--                    // Choose the first one. Need to be updated if we have multiple images--}%
                  %{--                    def featuredMedia = blog._links['wp:featuredmedia'][0]--}%
                  %{--                    def imageUrl = "${featuredMedia?.href}"--}%
                  %{--                    def url = new URL(imageUrl)--}%
                  %{--                    def connection = url.openConnection() as HttpURLConnection--}%
                  %{--                    int responseCode = connection.responseCode--}%
                  %{--                    if (responseCode == 404) {--}%
                  %{--                      // Handle the case where the image URL returns a 404 status code--}%
                  %{--                       out <<  "<img src=\"${grailsApplication.config.grails.serverURL + '/assets/email/no-img-av-ALAsilver.png'}\" height='80' alt='Sorry, no image available' > "--}%
                  %{--                    } else {--}%
                  %{--                      // Handle the case where the image URL returns a different status code--}%
                  %{--                      out << "<img src=\"${imageUrl}\" alt=\"${raw(blog.title.rendered)}\">"--}%
                  %{--                    }--}%
                  %{--                  }--}%
                  %{--                %>--}%
                %{--              
              </g:if>
              --}%
              %{--              
              <g:else>--}%
                %{--                <i class="bi bi-images"></i>--}%
                %{--              
              </g:else>
              --}%
              %{--            
            </div>
            --}%
            <%
              // Simulate the data you get from the controller
              String title= blog.title.rendered.replaceAll(/&#8211;|&#8212;|–|—/, "-")
              String excerpt = blog.excerpt.rendered.replaceAll(/&#8211;|&#8212;|–|—/, "-")
              %>
            <div style="width: 100%;">
              <a class="blog-title-font ala-color" href="${blog.link}" style="font-size: 15px;line-height: 1.42;font-weight: 700;letter-spacing: -0.2px;text-align: left;color: #C44D34 !important;"><b>${raw(title)}</b></a>
              <div class="blog-content-font" style="font-size: 13px;line-height: 1.42;font-weight: 300;letter-spacing: -0.2px;text-align: left;">${raw(excerpt)}</div>
            </div>
          </div>
        </div>
      </g:each>
      <div class="button-div" style="padding: 20px 20px 20px 20px;background-color: white;display: flex;flex-direction: row;justify-content: center;">
        <a href="${grailsApplication.config.ala.baseURL + '/blog'}">
        <button class="record-button" style="background-color: #C44D34;cursor: pointer;border: 0;border-radius: 10px;color: white;padding: 11px 19px;text-align: center;display: inline-block;font-size: 16px;"><strong>All ALA news &amp; updates</strong>
        </button>
        </a>
      </div>
      <div class="info-div" style="padding: 20px 70px 14px 70px;background-color: #C44D34;font-size: 14px;line-height: 1.43;letter-spacing: -0.2px;color: #ffffff;text-align: center;">
        <p>The Atlas of Living Australia acknowledges Australia's Traditional Owners and pays respect to the past and present Elders of the nation's Aboriginal and Torres Strait Islander communities.</p>
        <p>
          We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to Country and the biodiversity that forms part of that Country.
        </p>
      </div>
      <div class="info-div normal-font-color" style="padding: 20px 70px 14px 70px;background-color: #ffffff;font-size: 14px;line-height: 1.43;letter-spacing: -0.2px;color: #000000;text-align: center;">
        <div>
          <img src="${grailsApplication.config.grails.serverURL}/assets/email/ncris.png" alt="Affiliated orgs" usemap="#orgsMap" height="80" style="border: 0;line-height: 100%;outline: 0;">
          <map name="orgsMap">
            <area shape="rect" coords="0,0,100,100" href="https://www.education.gov.au/ncris" alt="NCRIS">
            <area shape="rect" coords="100,0,180,100" href="https://csiro.au" alt="CSIRO">
            <area shape="rect" coords="180,0,300,100" href="https://www.gbif.org/" alt="GBIF">
          </map>
          <div>
            You are receiving this email because you opted in to ALA blog alerts.
            <div>
              <p>Our mailing address is: </p>
              Atlas of Living Australia <br> GPO Box 1700<br> Canberra, ACT 2601<br>Australia
            </div>
            <br>
            Don't want to receive these emails? You can <a href="${unsubscribeOne}" style="color: #C44D34;">unsubscribe</a>.
          </div>
        </div>
      </div>
    </div>
  </body>
</html>