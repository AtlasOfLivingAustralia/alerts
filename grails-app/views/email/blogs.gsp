<%@ page contentType="text/html"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="format-detection" content="telephone=no">
  <meta name="x-apple-disable-message-reformatting">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${title}</title>
  <style>

  .ala-color {
    color: #C44D34 !important;
  }

  .default-font {
    vertical-align: top;
    font-family: 'Lato', Helvetica, Arial, sans-serif;
    font-size: 16px;
    font-weight: 500;
    line-height: 1.42;
    letter-spacing: -0.4px;
  }

  .large-white-font {
    font-weight: 700;
    font-size: 24px; /* Adjust the font size as needed */
    color: #ffffff; /* Adjust the color as needed */
  }

  .padding {
    padding: 20px 10px 20px 10px;
  }

  body {
    background-color: #f4f4f4;
    width: 100% !important;
    margin: 0 !important;
    padding: 0;
    line-height: 1.5;
    -webkit-font-smoothing: antialiased;
    -webkit-text-size-adjust: 100%;
    -ms-text-size-adjust: 100%;
  }

  .main-shadow-div {
    mso-table-lspace: 0pt;
    mso-table-rspace: 0pt;
    box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1);
    width: 100%;
    max-width: 620px;
    margin: 20px auto 0px;
  }

  .center-white-div{
    display: flex;
    vertical-align: top;
    justify-content: center;
    align-items: center;
    background-color: #ffffff;
    padding: 20px 20px 10px 20px;
  }

  .background-image-div {
    vertical-align: top;
    background-image: url("${grailsApplication.config.grails.serverURL+'/assets/email/banner-ocean.png'}");
    background-position: top center;
    background-size: cover;
    background-repeat: no-repeat;
    background-color: #003A70;
    color: #ffffff;
    text-align: center;
  }

  img {
    border: 0;
    line-height: 100%;
    outline: 0;
  }

  .blog-thumbnail-div {
    padding: 10px 0px 10px 0px;
    align-items: center;
    max-width: 130px;
  }

  .blog-div {
    padding: 20px 20px 20px 20px;
    background-color: white;
    display: flex;
    flex-direction: row;
    justify-content: center;
  }

  .blog-title-font {
    font-size: 15px;
    line-height: 1.42;
    font-weight: 700;
    letter-spacing: -0.2px;
    text-align: left;
  }
  .blog-content-font {
    font-size: 13px;
    line-height: 1.42;
    font-weight: 300;
    letter-spacing: -0.2px;
    text-align: left;
  }

  /*.blog-content-div {*/
  /*  padding: 10px 20px 10px 20px;*/
  /*}*/

  .info-div {
    padding: 20px 70px 14px 70px;
    background-color: #C44D34;
    font-size: 14px;
    line-height: 1.43;
    letter-spacing: -0.2px;
    color: #ffffff;
    text-align: center;
  }

  .button-div {
    padding: 20px 20px 20px 20px;
    background-color: white;
    display: flex;
    flex-direction: row;
    justify-content: center;
  }

  .record-button {
    background-color: #C44D34;
    cursor: pointer;
    border: 0;
    border-radius: 10px;
    color: white;
    padding: 11px 19px;
    text-align: center;
    display: inline-block;
    font-size: 16px;
  }

  .normal-font-color {
    color: #000000;
    background-color: #ffffff;
  }

  @media only screen and (min-width:812px) and (orientation: landscape){
    .main-shadow-div {
      width: 620px !important;
    }
  }

  @media only screen and (max-width: 375px) {
    .main-shadow-div {
      width: 100%; /* Set width to 100% to make it full-width on mobile phones */
    }
  }
  </style>
</head>
<body>
    <div class="main-shadow-div default-font">
      <div class="center-white-div">
        <a href="https://www.ala.org.au" style="text-decoration: none;">
          <img src="${grailsApplication.config.grails.serverURL + '/assets/email/logo-dark.png'}" height="60" alt="" >
        </a>
      </div>
      <div class="background-image-div padding ">
        <div class="default-font large-white-font">Latest ALA Blog Updates</div>
        <br>
        <div class="default-font">
          <b>${new SimpleDateFormat("dd MMMM yyyy").format(new Date())}</b>
        </div>
      </div>

      <g:each status="i" in="${records}" var="blog">
        <div class="blog-div" >
          <div style="display: inline-flex; flex-direction: row; justify-content: space-between; flex-grow: 1; ">
%{--            <div name="thumbnail-image" class="blog-thumbnail-div" style="width: 30%;">--}%
%{--              <g:if test="${blog?._links?.'wp:featuredmedia'}">--}%
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
%{--                       out <<  "<img src=\"${grailsApplication.config.grails.serverURL + '/assets/email/no-img-av-ALAsilver.png'}\" height='80' alt='Sorry, no image availabe' > "--}%
%{--                    } else {--}%
%{--                      // Handle the case where the image URL returns a different status code--}%
%{--                      out << "<img src=\"${imageUrl}\" alt=\"${raw(blog.title.rendered)}\">"--}%
%{--                    }--}%
%{--                  }--}%
%{--                %>--}%
%{--              </g:if>--}%
%{--              <g:else>--}%
%{--                <i class="bi bi-images"></i>--}%
%{--              </g:else>--}%
%{--            </div>--}%
            <div style="width: 100%;">
              <a class="blog-title-font ala-color" href="${blog.link}" ><b>${raw(blog.title.rendered)}</b></a>
              <div class="blog-content-font">${raw(blog.excerpt.rendered)}</div>
            </div>
          </div>
        </div>
      </g:each>

      <div class="button-div" >
        <a href="${grailsApplication.config.ala.baseURL + '/blog'}">
          <button class="record-button" ><strong>All ALA news & updates</strong>
          </button>
        </a>
      </div>

      <div class="info-div" >
        <p>The Atlas of Living Australia acknowledges Australia's Traditional Owners and pays respect to the past and present Elders of the nation's Aboriginal and Torres Strait Islander communities.</p>
        <p>
          We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to Country and the biodiversity that forms part of that Country.</p>
      </div>

      <div class="info-div normal-font-color">
        <div>
          <img src="${grailsApplication.config.grails.serverURL}/assets/email/ncris.png" alt="Affiliated orgs" usemap="#orgsMap"  height="80" >
          <map name="orgsMap">
            <area shape="rect" coords="0,0,100,100" href="https://www.education.gov.au/ncris" alt="NCRIS">
            <area shape="rect" coords="100,0,180,100" href="https://csiro.au" alt="CSIRO">
            <area shape="rect" coords="180,0,300,100" href="https://www.gbif.org/" alt="GBIF">
          </map>
          <p>You are receiving this email because you opted in to ALA blog alerts.
            <div>
              <p>Our mailing address is: </p>
              Atlas of Living Australia <br/> GPO Box 1700<br/> Canberra, ACT 2601<br/>Australia
            </div>
            <br>
            Don't want to receive these emails? You can <a href="${unsubscribeOne}" style="color: #C44D34;">unsubscribe</a>.
          </p>
        </div>
      </div>
    </div>

  </body>
</html>