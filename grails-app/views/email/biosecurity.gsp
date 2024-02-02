<%@ page contentType="text/html"%>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="org.apache.commons.lang3.StringUtils" %>
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:v="urn:schemas-microsoft-com:vml" xmlns:o="urn:schemas-microsoft-com:office:office">
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
  <meta http-equiv="X-UA-Compatible" content="IE=edge">
  <meta name="format-detection" content="telephone=no">
  <meta name="x-apple-disable-message-reformatting">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>${title}</title>
  <style type="text/css">
  /*--Fira font not needed>
  @media screen {
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 400;
  src: local('Fira Sans Regular'), local('FiraSans-Regular'), url(https://fonts.gstatic.com/s/firasans/v8/va9E4kDNxMZdWfMOD5Vvl4jLazX3dA.woff2) format('woff2');
  unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 400;
  src: local('Fira Sans Regular'), local('FiraSans-Regular'), url(https://fonts.gstatic.com/s/firasans/v8/va9E4kDNxMZdWfMOD5Vvk4jLazX3dGTP.woff2) format('woff2');
  unicode-range: U+0400-045F, U+0490-0491, U+04B0-04B1, U+2116;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 500;
  src: local('Fira Sans Medium'), local('FiraSans-Medium'), url(https://fonts.gstatic.com/s/firasans/v8/va9B4kDNxMZdWfMOD5VnZKveRhf6Xl7Glw.woff2) format('woff2');
  unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 500;
  src: local('Fira Sans Medium'), local('FiraSans-Medium'), url(https://fonts.gstatic.com/s/firasans/v8/va9B4kDNxMZdWfMOD5VnZKveQhf6Xl7Gl3LX.woff2) format('woff2');
  unicode-range: U+0400-045F, U+0490-0491, U+04B0-04B1, U+2116;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 700;
  src: local('Fira Sans Bold'), local('FiraSans-Bold'), url(https://fonts.gstatic.com/s/firasans/v8/va9B4kDNxMZdWfMOD5VnLK3eRhf6Xl7Glw.woff2) format('woff2');
  unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 700;
  src: local('Fira Sans Bold'), local('FiraSans-Bold'), url(https://fonts.gstatic.com/s/firasans/v8/va9B4kDNxMZdWfMOD5VnLK3eQhf6Xl7Gl3LX.woff2) format('woff2');
  unicode-range: U+0400-045F, U+0490-0491, U+04B0-04B1, U+2116;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 800;
  src: local('Fira Sans ExtraBold'), local('FiraSans-ExtraBold'), url(https://fonts.gstatic.com/s/firasans/v8/va9B4kDNxMZdWfMOD5VnMK7eRhf6Xl7Glw.woff2) format('woff2');
  unicode-range: U+0000-00FF, U+0131, U+0152-0153, U+02BB-02BC, U+02C6, U+02DA, U+02DC, U+2000-206F, U+2074, U+20AC, U+2122, U+2191, U+2193, U+2212, U+2215, U+FEFF, U+FFFD;
  }
  @font-face {
  font-family: 'Fira Sans';
  font-style: normal;
  font-weight: 800;
  src: local('Fira Sans ExtraBold'), local('FiraSans-ExtraBold'), url(https://fonts.gstatic.com/s/firasans/v8/va9B4kDNxMZdWfMOD5VnMK7eQhf6Xl7Gl3LX.woff2) format('woff2');
  unicode-range: U+0400-045F, U+0490-0491, U+04B0-04B1, U+2116;
  }
  }
  */
  #outlook a {
    padding: 0;
  }
  .ExternalClass,
  .ReadMsgBody {
    width: 100%;
  }
  .ExternalClass,
  .ExternalClass p,
  .ExternalClass td,
  .ExternalClass div,
  .ExternalClass span,
  .ExternalClass font {
    line-height: 100%;
  }
  div[style*="margin: 14px 0;"],
  div[style*="margin: 16px 0;"] {
    margin: 0 !important;
  }
  @media only screen and (min-width:621px) {
    .pc-container {
      width: 620px !important;
    }
  }
  @media only screen and (max-width:620px) {
    .pc-menu-box-s1 {
      padding: 25px 30px !important
    }
    .pc-heading-s1 .pc-heading-action-in,
    .pc-heading-s1 .pc-heading-col,
    .pc-menu-box-s1 .pc-menu-col,
    .pc-menu-box-s1 .pc-menu-socials-s1 {
      width: 100% !important
    }
    .pc-content-box-s5 .pc-content-box-in {
      padding: 25px 30px 35px !important
    }
    .pc-content-box-s4 {
      padding: 25px 10px 15px !important
    }
    .pc-posts-row-s3 .pc-posts-row-col {
      max-width: 50% !important
    }
    .pc-content-box-s1 {
      padding: 15px 10px !important
    }
    .pc-post-s2 {
      text-align: center !important
    }
    .pc-footer-row-s1 .pc-footer-row-col,
    .pc-post-s2 .pc-post-col {
      max-width: 100% !important
    }
    .pc-post-s2 .pc-post-col-in.pc-m-img {
      padding-bottom: 0 !important
    }
    .pc-post-s2 .pc-post-col-in.pc-m-details {
      padding-top: 16px !important
    }
    .pc-post-s2 .pc-post-img {
      Margin: 0 auto !important
    }
    .pc-post-s2.pc-m-invert {
      direction: ltr !important
    }
    .pc-cta-box-s1 {
      padding: 35px 30px !important
    }
    .pc-footer-box-s1 {
      padding-left: 10px !important;
      padding-right: 10px !important
    }
    .pc-spacing.pc-m-footer-h-46 td,
    .pc-spacing.pc-m-footer-h-57 td {
      font-size: 20px !important;
      height: 20px !important;
      line-height: 20px !important
    }
  }
  @media only screen and (max-width:525px) {
    .pc-menu-box-s1 {
      padding: 15px 20px !important
    }
    .pc-content-box-s5 .pc-content-box-in {
      padding: 15px 20px 25px !important
    }
    .pc-spacing.pc-m-content-7 td {
      font-size: 70px !important;
      height: 70px !important;
      line-height: 70px !important
    }
    .pc-content-box-s4 {
      padding: 15px 0 5px !important
    }
    .pc-posts-row-s3 .pc-posts-row-col {
      max-width: 100% !important
    }
    .pc-content-box-s1,
    .pc-footer-box-s1 {
      padding: 5px 0 !important
    }
    .pc-cta-box-s1 {
      padding: 25px 20px !important
    }
    .pc-cta-s1 .pc-cta-title {
      font-size: 24px !important;
      line-height: 1.42 !important
    }
    .pc-cta-text br,
    .pc-cta-title br,
    .pc-footer-text-s1 br {
      display: none !important
    }
  }
  </style>
  <!--[if mso]>
      <style type="text/css">
         .pc-fb-font{font-family:Helvetica,Arial,sans-serif !important;}
      </style>
      <![endif]-->
  <!--[if gte mso 9]>
      <xml>
         <o:OfficeDocumentSettings>
    <o:AllowPNG/>
    <o:PixelsPerInch>96</o:PixelsPerInch>
  </o:OfficeDocumentSettings>
      </xml>
      <![endif]-->
</head>
<body class="pc-fb-font" bgcolor="#f4f4f4" style="background-color: #f4f4f4; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 16px; width: 100% !important; Margin: 0 !important; padding: 0; line-height: 1.5; -webkit-font-smoothing: antialiased; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%">
<table style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%" border="0" cellpadding="0" cellspacing="0">
  <tbody>
  <tr>
    <td style="padding: 0; vertical-align: top;" align="center" valign="top">
      <!--[if (gte mso 9)|(IE)]>
                  <table width="620" align="center" border="0" cellspacing="0" cellpadding="0">
                     <tr>
                        <td width="620" align="center" valign="top">
                           <![endif]-->
      <!-- START header-->
      <table class="pc-container" align="center" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%; Margin: 0 auto; max-width: 620px;" width="100%" border="0" cellpadding="0" cellspacing="0">
        <tbody>
        <tr>
          <td align="left" style="vertical-align: top; padding: 0 10px;" valign="top">
            <span class="preheader" style="color: transparent; display: none; height: 0; max-height: 0; max-width: 0; opacity: 0; overflow: hidden; mso-hide: all; visibility: hidden; width: 0;"></span>
            <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
              <tbody>
              <tr>
                <td style="vertical-align: top; padding: 0; height: 20px; font-size: 20px; line-height: 20px;" valign="top">&nbsp;</td>
              </tr>
              </tbody>
            </table>
            <table style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; box-shadow: 0 2px 4px 0 rgba(0, 0, 0, 0.1)" border="0" cellspacing="0" cellpadding="0">
              <tbody>
              <tr>
                <td>
                  <!-- START MODULE: Menu 6 -->
                  <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                    <tbody>
                    <tr>
                      <td class="pc-menu-box-s1" style="vertical-align: top; padding: 20px 40px 10px 40px; background-color: #ffffff;" valign="top" bgcolor="#ffffff">
                        <!--[if (gte mso 9)|(IE)]>
                                                               <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                                  <tr>
                                                                     <td width="195" valign="top">
                                                                        <![endif]-->
                        <!--
                                                                           <table class="pc-menu-col" align="left" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 195px;" width="195">
                                                                             <tbody>
                                                                               <tr>
                                                                                 <td align="left" style="vertical-align: top; padding: 13px 0 10px;" valign="top">
                                                                                   <table class="pc-menu-socials-s1" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
                                                                                     <tbody>
                                                                                       <tr>
                                                                                         <td style="vertical-align: top; font-family: 'Fira Sans', Helvetica, Arial, sans-serif; font-size: 10px; line-height: 1.3; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%; text-align: center;" valign="top" align="center"> <a href="http://example.com" style="text-decoration: none;">
                                                                                 <img src="/assets/email/facebook-dark.png" width="15" height="15" alt="" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; color: #1B1B1B;">
                                                                             </a> <span>&nbsp;&nbsp;</span> <a href="http://example.com" style="text-decoration: none;">
                                                                                 <img src="/assets/email/twitter-dark.png" width="16" height="14" alt="" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; color: #1B1B1B;">
                                                                             </a> <span>&nbsp;&nbsp;</span> <a href="http://example.com" style="text-decoration: none;">
                                                                                 <img src="/assets/email/google-plus-dark.png" width="22" height="15" alt="" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; color: #1B1B1B;">
                                                                             </a> <span>&nbsp;&nbsp;</span> <a href="http://example.com" style="text-decoration: none;">
                                                                                 <img src="/assets/email/instagram-dark.png" width="16" height="15" alt="" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; color: #1B1B1B;">
                                                                             </a> </td>
                                                                                       </tr>
                                                                                     </tbody>
                                                                                   </table>
                                                                                 </td>
                                                                               </tr>
                                                                             </tbody>
                                                                           </table>
                                                                           -->
                        <!--[if (gte mso 9)|(IE)]>
                                                                     </td>
                                                                     <td width="130" valign="top">
                                                                        <![endif]-->
                        <table class="pc-menu-col" align="center" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 130px;" width="130">
                          <tbody>
                          <tr>
                            <td align="center" style="vertical-align: top; padding: 1px 0;" valign="top">
                              <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 130px;" width="130">
                                <tbody>
                                <tr>
                                  <td style="vertical-align: top;" valign="top"> <a href="https://www.ala.org.au" style="text-decoration: none;">
                                    <img src="${assetPath(src: 'email/logo-dark.png')}" height="60"  alt="" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; display: block; font-family: 'Fira Sans', Helvetica, Arial, sans-serif; font-size: 20px; font-weight: 500; color: #212121;">
                                  </a>
                                  </td>
                                </tr>
                                </tbody>
                              </table>
                            </td>
                          </tr>
                          </tbody>
                        </table>
                        <!--[if (gte mso 9)|(IE)]>
                                                           </td>
                                                         <td width="195" valign="top">
                                                 <![endif]-->
                        <table class="pc-menu-col" align="left" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 195px;" width="195">
                          <tbody>
                          <tr>
                            <td style="vertical-align: top;" valign="top"> <span style="mso-hide: all; font-size: 1px; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;">&nbsp;</span> </td>
                          </tr>
                          </tbody>
                        </table>
                        <!--[if (gte mso 9)|(IE)]>
                                                                     </td>
                                                                  </tr>
                                                               </table>
                                                               <![endif]-->
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <!-- END MODULE: Menu 6 -->
                  <!-- START MODULE: Content 6 -->
                  <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                    <tbody>
                    <tr>
                      <td class="pc-content-box-s5" background="/assets/email/biosecurity-alert-header.png" style="vertical-align: top; background-color: #C44D34;  background-image: url('/assets/email/biosecurity-alert-header.png'); background-position: top center; background-size: cover; background-repeat: no-repeat;" width="00" valign="top">
                      <!--[if gte mso 9]>
                        <v:rect xmlns:v="urn:schemas-microsoft-com:vml" fill="true" stroke="false" style="width:600px;height:360px;">
                          <v:fill type="frame" src="/assets/email/content-6-image-2.jpg" color="#1B1B1B"></v:fill>
                          <v:textbox inset="40px,30px,40px,40px">
                            <div>
                               <![endif]-->
                            <div class="pc-content-box-in" style="padding: 10px 40px 0px;">
                              <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                <tbody>
                                <tr>
                                  <td class="pc-heading-s1" style="vertical-align: top;" valign="top">
                                    <!--[if (gte mso 9)|(IE)]>
                                                                                       <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                                                          <tr>
                                                                                             <td width="100" valign="top">
                                                                                                <![endif]-->
                                    <table class="pc-heading-col" border="0" cellpadding="0" cellspacing="0" align="left" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100px; mso-line-height-rule: exactly;" width="100">
                                      <tbody>
                                      <tr>
                                        <td style="vertical-align: top; height: 1px; line-height: 1px; font-size: 1px;" valign="top">&nbsp;</td>
                                      </tr>
                                      </tbody>
                                    </table>
                                    <!--[if (gte mso 9)|(IE)]>
                                                                                             </td>
                                                                                             <td width="320" valign="top">
                                                                                                <![endif]-->
                                    <table class="pc-heading-col" border="0" cellpadding="0" cellspacing="0" align="left" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 320px;" width="320">
                                      <tbody>
                                      <tr>
                                        <td class="pc-fb-font" style="vertical-align: top; padding: 0; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 24px; font-weight: 700; line-height: 1.42; letter-spacing: -0.4px; text-align: center; color: #ffffff;" valign="top" align="center">Biosecurity Alerts</td>
                                      </tr>
                                      </tbody>
                                    </table>
                                    <!--[if (gte mso 9)|(IE)]>
                                                                                             </td>
                                                                                             <td width="100" valign="top" align="right">
                                                                                                <![endif]-->
                                    <table class="pc-heading-col" border="0" cellpadding="0" cellspacing="0" align="left" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100px;" width="100">
                                      <tbody>
                                      <tr>
                                        <td style="vertical-align: top; padding: 13px 0 10px; text-align: right;" valign="top" align="right">
                                          <table class="pc-heading-action-in" border="0" cellpadding="0" cellspacing="0" align="right" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
                                            <tbody>
                                            <tr>
                                              <td style="vertical-align: top;" valign="top">
                                                <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                                  <tbody>
                                                  <tr>
                                                    <td align="center" style="vertical-align: top;" valign="top">
                                                      <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
                                                        <tbody>
                                                        <tr>
                                                        </tr>
                                                        </tbody>
                                                      </table>
                                                    </td>
                                                  </tr>
                                                  </tbody>
                                                </table>
                                              </td>
                                            </tr>
                                            </tbody>
                                          </table>
                                        </td>
                                      </tr>
                                      </tbody>
                                    </table>
                                    <!--[if (gte mso 9)|(IE)]>
                                                                                             </td>
                                                                                          </tr>
                                                                                       </table>
                                                                                       <![endif]-->
                                  </td>
                                </tr>
                                <tr>
                                  <td style="vertical-align: top;" valign="top">
                                    <table class="pc-spacing pc-m-content-7" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                      <tbody>
                                      </tbody>
                                    </table>
                                  </td>
                                </tr>
                                <tr>
                                  <td style="vertical-align: top;" valign="top">
                                    <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                      <tbody>
                                      <tr>
                                        <td class="pc-fb-font" style="vertical-align: top; padding: 2px 30px 0px 30px; line-height: 1.43; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 16px; font-weight: 700; color: #ffffff; text-align: center;" valign="top"> <a style="text-decoration: none; cursor: text; color: #ffffff;">
                                          ${new SimpleDateFormat("dd-MMMM-yyyy").format(new Date())}
                                        </19></a>
                                        </td>
                                      </tr>
                                      <tr>
                                        <td class="pc-fb-font" style="vertical-align: top; padding: 15px 60px 10px 60px; line-height: 1.42; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 16px; font-weight: 400; letter-spacing: -0.4px; color: #ffffff; text-align: center;" valign="top" align="center">Alerts service for new ALA records listing potential invasive species
                                        </td>
                                      </tr>
                                      <tr>
                                      </tr>
                                      </tbody>
                                    </table>
                                  </td>
                                </tr>
                                </tbody>
                              </table>
                            </div>
                            <!--[if gte mso 9]>
                         </div>
                         <p style="margin:0;mso-hide:all">
                            <o:p xmlns:o="urn:schemas-microsoft-com:office:office">&nbsp;</o:p>
                            </p>
                          </v:textbox>
                        </v:rect>
                      <![endif]-->
                      </td>
                    </tr>
                    </tbody>
                  </table>
                  <!-- END MODULE: Content 6 -->
                  <!-- END header-->
                  <!-- START list title -->
                  <!-- START MODULE: Content 5 -->
                  <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                    <tbody>
                    <tr>
                      <td class="pc-content-box-s4" style="vertical-align: top; padding: 20px 20px 10px; background-color: #ffffff" valign="top" bgcolor="#ffffff">
                        <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                          <tbody>
                          <tr>
                            <td class="pc-heading-s1" style="vertical-align: top; padding: 0 20px;" valign="top">
                              <!--[if (gte mso 9)|(IE)]>
                                                                           <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                                              <tr>
                                                                                 <td width="100" valign="top">
                                                                                    <![endif]-->
                              <table class="pc-heading-col" border="0" cellpadding="0" cellspacing="0" align="left" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100px; mso-line-height-rule: exactly;" width="100">
                                <tbody>
                                <tr>
                                  <td style="vertical-align: top; height: 1px; line-height: 1px; font-size: 1px;" valign="top">&nbsp;</td>
                                </tr>
                                </tbody>
                              </table>
                              <!--[if (gte mso 9)|(IE)]>
                                                                                 </td>
                                                                                 <td width="320" valign="top">
                                                                                    <![endif]-->
                              <table class="pc-heading-col" border="0" cellpadding="0" cellspacing="0" align="left" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 320px;" width="420">
                                <tbody>
                                <tr>
                                  <td class="pc-fb-font" style="vertical-align: top; padding: 10px 0 0 0; font-family: 'Lato', Helvetica, Arial, sans-serif; font-size: 22px; font-weight: 500; line-height: 1.42; letter-spacing: -0.4px; color: #212121; text-align: center;" valign="top" align="center">
                                    ${totalRecords} new ${totalRecords == 1 ? 'alert' : 'alerts'} for
                                    <br>
                                    <div title="${speciesListInfo.name}">
                                    <strong>${StringUtils.abbreviate(speciesListInfo.name, 40)}, <a href="${speciesListInfo.url}"> ${speciesListInfo.drId}</a></strong>
                                    </div>
                                  </td>
                                </tr>
                                </tbody>
                              </table>
                              <!--[if (gte mso 9)|(IE)]>
                                                                                 </td>
                                                                                 <td width="100" valign="top">
                                                                                    <![endif]-->
                              <table class="pc-heading-col" border="0" cellpadding="0" cellspacing="0" align="left" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100px;" width="100">
                                <tbody>
                                <tr>
                                  <td style="vertical-align: top; padding: 13px 0 10px; text-align: right;" valign="top" align="right">
                                    <table class="pc-heading-action-in" border="0" cellpadding="0" cellspacing="0" align="right" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
                                      <tbody>
                                      <tr>
                                        <td style="vertical-align: top;" valign="top">
                                          <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                            <tbody>
                                            <tr>
                                              <td align="center" style="vertical-align: top;" valign="top">
                                                <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt;">
                                                  <tbody>
                                                  <tr>
                                                  </tr>
                                                  </tbody>
                                                </table>
                                              </td>
                                            </tr>
                                            </tbody>
                                          </table>
                                        </td>
                                      </tr>
                                      </tbody>
                                    </table>
                                  </td>
                                </tr>
                                </tbody>
                              </table>
                              <!--[if (gte mso 9)|(IE)]>
                                                                                 </td>
                                                                              </tr>
                                                                           </table>
                                                                           <![endif]-->
                            </td>
                          </tr>
                          <tr>
                            <td>
                              <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                <tbody>
                                <tr>
                                  <td class="pc-posts-row-s3" style="vertical-align: top; font-size: 0;" valign="top">
                                  </td>
                                </tr>
                              </table>
                            </td>
                          </tr>
                          </tbody>
                        </table>
                      </td>
                    </tr>
                    </tbody>
                  </table>
                </td>
              </tr>
              </tbody>
            <!-- END MODULE: Content 5 -->
            <!-- END list title -->

              <g:each status="i" in="${records}" var="oc">
                <g:set var="link" value="${query.baseUrlForUI}/occurrences/${oc.uuid}"/>
                <!-- START table row 1 -->
                <tr>
                  <td class="pc-posts-row-s3" style="vertical-align: top; font-size: 0;" valign="top" bgcolor="#ffffff">
                    <!--[if (gte mso 9)|(IE)]>
                                                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                   <tr>
                                                      <td width="33%" valign="top">
                                                         <![endif]-->
                    <div class="pc-posts-row-col" style="display: inline-block; width: 46%; max-width: 280px; vertical-align: top;">
                      <table border="0" bgcolor="#ffffff"cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                        <tbody>
                        <tr>
                          <td style="vertical-align: top; padding: 20px 5px 20px 20px;" valign="top">
                            <!-- Text Block -->
                            <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                              <tbody>
                              <tr>
                                <td class="pc-fb-font" style="vertical-align: top; padding: 0px 0 0 0; line-height: 1.42; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 15px; font-weight: 700; letter-spacing: -0.4px; color: #212121; text-align: left;" valign="top" align="center">
                                  <a href="${link}" style="text-decoration: none; color: #C44D34 ;">${i+1}. <em>${oc.scientificName ?:"N/A"}</em></a>
                                </td>
                              </tr>
                              <tr>
                                <td class="pc-fb-font" style="vertical-align: top; padding: 0; line-height: 1.56; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 13.5px; font-weight: 300; letter-spacing: -0.2px; color: #212121; text-align: left;" valign="top" align="center">
                                  <g:if test="${oc.scientificName && oc.raw_scientificName && oc.scientificName != oc.raw_scientificName}">
                                    Supplied as:<em>{oc.raw_scientificName}</em><br>
                                  </g:if>
                                  <g:if test="${oc.vernacularName}">
                                    Common name: ${oc.vernacularName}<br>
                                  </g:if>
                                  <g:if test="${oc.locality && oc.stateProvince}">
                                    <strong>${oc.locality}; ${oc.stateProvince}</strong>><br>
                                  </g:if>
                                  <g:elseif test="${oc.locality}">
                                    <strong>${oc.locality}</strong><br>
                                  </g:elseif>
                                  <g:elseif test="${oc.stateProvince}">
                                    <strong>${oc.stateProvince}</strong><br>
                                  </g:elseif>
                                  <g:if test="${oc.latLong}">
                                    Coordinates: ${oc.latLong} <br>
                                  </g:if>
                                  <g:if test="${oc.eventDate}">
                                    Time & date: ${new SimpleDateFormat('yyyy-MM-dd HH:mm').format(oc.eventDate)} <br>
                                  </g:if>
                                  <g:if test="${oc.dataResourceName}">
                                    Source: ${oc.dataResourceName} <br>
                                  </g:if>

                                </td>
                              </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                        </tbody>
                      </table>
                    </div>
                  <!--[if (gte mso 9)|(IE)]>
                                                      </td>
                                                      <td width="33%" valign="top">
                                                         <![endif]-->
                    <g:if test="${oc.thumbnailUrl || oc.smallImageUrl }">&nbsp;&nbsp;
                      <div class="pc-posts-row-col" style="display: inline-block; width: 27%; max-width: 280px; vertical-align: top;">
                        <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                          <tbody>
                          <tr>
                            <td style="vertical-align: top; padding: 20px 20px 5px 5px;" valign="top">
                              <!-- First Image -->
                              <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                                <tbody>
                                <tr>
                                  <td style="vertical-align: top; text-align: center;" valign="top" align="center">

                                    <a href="${query.baseUrlForUI}/occurrences/${oc.uuid}">
                                      <img src="${oc.thumbnailUrl ?: oc.smallImageUrl}"  alt="${message(code: "biocache.alt.image.for.record")}" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; display: block; color: #212121; border-radius: 6px; max-width: 100%; max-height: 118px; Margin: 0 auto"/>
                                    </a>

                                  </td>
                                </tr>
                                </tbody>
                              </table>
                            </td>
                          </tr>
                          </tbody>
                        </table>
                      </div>
                    </g:if>
                  <!--[if (gte mso 9)|(IE)]>
                                                      </td>
                                                      <td width="33%" valign="top">
                                                         <![endif]-->
                    <div class="pc-posts-row-col" style="display: inline-block; width: 27%; max-width: 280px; vertical-align: top;">
                      <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                        <tbody>
                        <tr>
                          <td style="vertical-align: top; padding: 20px 20px 5px 5px;" valign="top">
                            <!-- Second Image (Map) -->
                            <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                              <tbody>
                              <tr>
                                <td style="vertical-align: top; text-align: center;" valign="top" align="center">
                                  <g:if test="${oc.latLong}">
                                    <img src="https://maps.googleapis.com/maps/api/staticmap?center=${oc.latLong}&markers=|${oc.latLong}&zoom=5&size=240x200&maptype=roadmap&key=${grailsApplication.config.getProperty('google.apikey')}" alt="location preview map" style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; display: block; color: #212121; border-radius: 6px; max-width: 100%; height: auto; Margin: 0 auto"/>
                                  </g:if>
                                </td>
                              </tr>
                              </tbody>
                            </table>
                          </td>
                        </tr>
                        </tbody>
                      </table>
                    </div>
                    <!--[if (gte mso 9)|(IE)]>
                                                      </td>
                                                   </tr>
                                                </table>
                                                <![endif]-->
                  </td>
                </tr>
                <!-- END table row 1 -->
              </g:each>
              <tr>
                <td class="pc-posts-row-s3" style="vertical-align: top; font-size: 0;" valign="top" bgcolor="#ffffff">
                  <!--[if (gte mso 9)|(IE)]>
                                                <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                   <tr>
                                                      <td width="33%" valign="top">
                                                         <![endif]-->
                  <div class="pc-posts-row-col" style="display: inline-block; width: 100%; max-width: 620px; vertical-align: top;">
                    <table border="0" bgcolor="#ffffff"cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                      <tbody>
                      <tr>
                        <td style="vertical-align: top; padding: 20px 5px 20px 20px;" valign="top">
                          <!-- Text Block -->
                          <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                            <tbody>
                            <tr>
                              <td class="pc-fb-font" style="vertical-align: top; padding: 0; line-height: 1.56; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 14px; font-weight: 300; letter-spacing: -0.2px; color: #212121; text-align: center;" valign="top" halign="center">
                                <a href="${query.baseUrlForUI + query.queryPathForUI}">
                                  <button style="background-color: #C44D34;  cursor: pointer;  border: 0; border-radius: 10px; color: white; padding: 11px 19px; text-align: center; display: inline-block; font-size: 16px;"><strong>View all records online</strong>
                                  </button>
                                </a>
                              </td>
                            </tr>
                            </tbody>
                          </table>
                        </td>
                      </tr>
                      </tbody>
                    </table>
                  </div>
                  <!--[if (gte mso 9)|(IE)]>
                                                      </td>
                                                      <td width="33%" valign="top">
                                                         <![endif]-->
                  <div class="pc-posts-row-col" style="display: inline-block; width: 27%; max-width: 280px; vertical-align: top;">
                    <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                      <tbody>
                      </tbody>
                    </table>
                  </div>
                  <!--[if (gte mso 9)|(IE)]>
                                                      </td>
                                                   </tr>
                                                </table>
                                                <![endif]-->
                </td>
              </tr>
              <!-- END MODULE: Call to action 1 -->
              <!-- START MODULE: Footer 1: QC CTA -->
              <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                <tbody>
                <tr>
                  <td class="pc-footer-box-s1" style="vertical-align: top; padding: 21px 20px 14px; background-color: #C44D34" valign="top" bgcolor="#C44D34">
                    <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" max-width="100%">
                      <tbody>
                      <tr>
                        <td class="pc-footer-row-s1" style="vertical-align: top; font-size: 0; -webkit-text-size-adjust: 100%; -ms-text-size-adjust: 100%;" valign="top">
                          <!--[if (gte mso 9)|(IE)]>
                                                                  <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                                     <tr>
                                                                        <td width="280" style="width:280px;" valign="top">
                                                                           <![endif]-->
                          <div class="pc-footer-row-col" style="display: inline-block; width: 100%; max-width: 280px; vertical-align: top;">
                            <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                            </table>
                          </div>
                          <!--[if (gte mso 9)|(IE)]>
                                                                        </td>
                                                                        <td width="280" style="width:280px;" valign="top">
                                                                           <![endif]-->
                      <tbody>
                      <td style="vertical-align: top; padding: 5px 50px 5px 50px;" valign="top">
                        <table border="0" cellpadding="0" cellspacing="0" class="pc-footer-text-s1" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                          <tbody>
                          <tr>
                            <td class="pc-fb-font" style="vertical-align: top; padding: 0; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 14px; line-height: 1.43; letter-spacing: -0.2px; color: #ffffff;" valign="top">
                              <p>If you notice a record has been misidentified, we encourage you to use your expertise to improve the quality of Australia’s biosecurity data.</p>
                              <p>Please either annotate the record in the provider platform itself or notify us at <a href="mailto:biosecurity@ala.org.au" style="color: #f2f2f2; font-weight: 700;">biosecurity@ala.org.au</a> and we can make the suggested change.</p>
                            </td>
                          </tr>
                          </tbody>
                        </table>
                        <table class="pc-spacing pc-m-footer-h-46" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                          <tbody>
                          </tbody>
                        </table>
                        <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                          <tbody>
                          <tr>
                          </tr>
                          <tr>
                          </tr>
                          </tbody>
                        </table>
                      </td>
                      </tbody>
                      <!--[if (gte mso 9)|(IE)]>
                                                                     </td></tr>
                                                                  </table>
                                                                  <![endif]-->
                  </td>
                </tr>
                </tbody>
              </table>
          </td>
        </tr>
        </tbody>
      </table>
      <!-- END MODULE: Footer 1: QC CTA -->
      <!-- Footer 2: Acknowledgement of Country -->
      <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" max-width="100%">
        <tbody>
        <tr>
          <td class="pc-footer-box-s1" style="vertical-align: top; padding: 21px 20px 14px; background-color: #212121" valign="top" bgcolor="#212121">
            <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
              <tbody>
              <!--[if (gte mso 9)|(IE)]>
                                                            <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                                               <tr>
                                                                  <td width="280" style="width:280px;" valign="top">
                                                                     <![endif]-->
              <!--[if (gte mso 9)|(IE)]>
                                                                  </td>
                                                                  <td width="280" style="width:280px;" valign="top">
                                                                     <![endif]-->
              <div class="pc-footer-row-col" style="display: inline-block; width: 100%; max-width: 280px; vertical-align: top;">
                <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                  <tbody>
                  <tr>
                    <td style="vertical-align: top; padding: 5px 50px 5px 50px;" valign="top">
                      <table border="0" cellpadding="0" cellspacing="0" class="pc-footer-text-s1" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                        <tbody>
                        <tr>
                          <td class="pc-fb-font" style="vertical-align: top; padding: 0; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 14px; line-height: 1.43; letter-spacing: -0.2px; color: #ffffff;" valign="top">
                            <p>The Atlas of Living Australia acknowledges Australia’s Traditional Owners and pays respect to the past and present Elders of the nation’s Aboriginal and Torres Strait Islander communities.</p>
                          </p>
                            We honour and celebrate the spiritual, cultural and customary connections of Traditional Owners to Country and the biodiversity that forms part of that Country.</p>
                          </td>
                        </tr>
                        </tbody>
                      </table>
                      <table class="pc-spacing pc-m-footer-h-46" border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                        <tbody>
                        </tbody>
                      </table>
                      <table border="0" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
                        <tbody>
                        <tr>
                        </tr>
                        <tr>
                        </tr>
                        </tbody>
                      </table>
                    </td>
                  </tr>
                  </tbody>
                </table>
              </div>
              <!--[if (gte mso 9)|(IE)]>
                                     </td>
                                   </tr>
                                </table>
                            <![endif]-->
          </td>
        </tr>
        </tbody>
      </table>
    </td>
  </tr>
  </tbody>
</table>
</td>
</tr>
</tbody>
</td>
</tr>
</tbody>
</table>
<!-- END MODULE: Footer 2 Acknowledgement of Country  -->
<!-- START MODULE: Footer 3 Logos + unsubscribe -->
<!--[if (gte mso 9)|(IE)]>
                                       <table width="100%" border="0" cellspacing="0" cellpadding="0">
                                          <tr>
                                             <td width="280" style="width:280px;" valign="top">
                                                <![endif]-->
<!--[if (gte mso 9)|(IE)]>
                                             </td>
                                             <td width="280" style="width:280px;" valign="top">
                                                <![endif]-->
<table class="pc-container mso-table-lspace: 0pt; mso-table-rspace: 0pt" border="0" align="center" cellpadding="0" cellspacing="0" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 600px;">
  <tbody>
  <tr>
    <td style="vertical-align: top; padding: 20px 20px 0px 20px; background-color: #ffffff" valign="top">
      <table border="0" cellpadding="0" cellspacing="0" class="pc-footer-text-s1" style="mso-table-lspace: 0pt; mso-table-rspace: 0pt; width: 100%;" width="100%">
        <tbody>
        <tr>
          <td style="vertical-align: top;" align="center" valign="top" width: 130px;>
            <img src="/assets/email/ncris.png" alt="Affiliated orgs" usemap="#orgsMap"  height="80"  style="border: 0; line-height: 100%; outline: 0; -ms-interpolation-mode: bicubic; display: block; font-family: 'Fira Sans', Helvetica, Arial, sans-serif; font-size: 20px; font-weight: 500; color: #212121;">
            <map name="orgsMap">
              <area shape="rect" coords="0,0,100,100" href="https://www.education.gov.au/ncris" alt="NCRIS">
              <area shape="rect" coords="100,0,180,100" href="https://csiro.au" alt="CSIRO">
              <area shape="rect" coords="180,0,300,100" href="https://www.gbif.org/" alt="GBIF">
              <!-- Add more areas for link3 and link4 as needed -->
            </map>
          </td>
        </tr>
        <tr>
          <td style="vertical-align: top;" align="center" valign="top" width: 130px;>
            <p style="vertical-align: top; padding: 5px 50px 9px 50px; font-family: 'Roboto', Helvetica, Arial, sans-serif; font-size: 14px; line-height: 1.43; letter-spacing: -0.2px; color: #212121;" valign="top">You are receiving this email because you opted in to ALA biosecurity alerts.
            </br>
              Don't want to receive these emails? You can <a href="${unsubscribeOne}" style="color: #C44D34;">unsubscribe</a>.
            </p>
          </td>
        </tr>
        </tbody>
      </table>
    </td>
  </tr>
  </tbody>
</table>
</div>
<!--[if (gte mso 9)|(IE)]>
                                             </td>
                                          </tr>
                                       </table>
                                       <![endif]-->
</td>
</tr>
</tbody>
</table>
</td>
</tr>
</tbody>
</table>
</table>
</td>
</tr>
</tbody>
</td>
</tr>
</tbody>
</table>
</div>
<!-- END MODULE: Footer 3 Logos + unsubscribe  -->
</body>
</html>