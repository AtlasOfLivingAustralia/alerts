<%@ page contentType="text/html"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD XHTML 1.0 Transitional //EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html><head><title></title><meta http-equiv="Content-Type" content="text/html; charset=utf-8">
    <style type="text/css">
    /* Mobile-specific Styles */
    @media only screen and (max-device-width: 480px) {
        table[class=w0], td[class=w0] { width: 0 !important; }
        table[class=w10], td[class=w10], img[class=w10] { width:10px !important; }
        table[class=w15], td[class=w15], img[class=w15] { width:5px !important; }
        table[class=w30], td[class=w30], img[class=w30] { width:10px !important; }
        table[class=w60], td[class=w60], img[class=w60] { width:10px !important; }
        table[class=w125], td[class=w125], img[class=w125] { width:80px !important; }
        table[class=w130], td[class=w130], img[class=w130] { width:55px !important; }
        table[class=w140], td[class=w140], img[class=w140] { width:90px !important; }
        table[class=w160], td[class=w160], img[class=w160] { width:180px !important; }
        table[class=w170], td[class=w170], img[class=w170] { width:100px !important; }
        table[class=w180], td[class=w180], img[class=w180] { width:80px !important; }
        table[class=w195], td[class=w195], img[class=w195] { width:80px !important; }
        table[class=w220], td[class=w220], img[class=w220] { width:80px !important; }
        table[class=w240], td[class=w240], img[class=w240] { width:180px !important; }
        table[class=w255], td[class=w255], img[class=w255] { width:185px !important; }
        table[class=w275], td[class=w275], img[class=w275] { width:135px !important; }
        table[class=w280], td[class=w280], img[class=w280] { width:135px !important; }
        table[class=w300], td[class=w300], img[class=w300] { width:140px !important; }
        table[class=w325], td[class=w325], img[class=w325] { width:95px !important; }
        table[class=w360], td[class=w360], img[class=w360] { width:140px !important; }
        table[class=w410], td[class=w410], img[class=w410] { width:180px !important; }
        table[class=w470], td[class=w470], img[class=w470] { width:200px !important; }
        table[class=w580], td[class=w580], img[class=w580] { width:280px !important; }
        table[class=w640], td[class=w640], img[class=w640] { width:300px !important; }
        table[class*=hide], td[class*=hide], img[class*=hide], p[class*=hide], span[class*=hide] { display:none !important; }
        table[class=h0], td[class=h0] { height: 0 !important; }
        p[class=footer-content-left] { text-align: center !important; }
        #headline p { font-size: 30px !important; }
        .article-content, #left-sidebar{ -webkit-text-size-adjust: 90% !important; -ms-text-size-adjust: 90% !important; }
        .header-content, .footer-content-left {-webkit-text-size-adjust: 80% !important; -ms-text-size-adjust: 80% !important;}
        img { height: auto; line-height: 100%;}
    }
    /* Client-specific Styles */
    #outlook a { padding: 0; }
    /* Force Outlook to provide a "view in browser" button. */
    body { width: 100% !important; }
    .ReadMsgBody { width: 100%; }
    .ExternalClass { width: 100%; display: block !important; }
    /* Reset Styles */
    body { background-color: #c7c7c7; margin: 0; padding: 0; }
    img { outline: none; text-decoration: none; display: block; }
    br, strong br, b br, em br, i br { line-height: 100%; }
    h1, h2, h3, h4, h5, h6 { line-height: 100% !important; -webkit-font-smoothing: antialiased; }
    h1 a, h2 a, h3 a, h4 a, h5 a, h6 a { color: blue !important; }
    h1 a:active, h2 a:active, h3 a:active, h4 a:active, h5 a:active, h6 a:active { color: red !important; }
    h1 a:visited, h2 a:visited, h3 a:visited, h4 a:visited, h5 a:visited, h6 a:visited { color: purple !important; }
    table td, table tr { border-collapse: collapse; }
    .yshortcuts, .yshortcuts a, .yshortcuts a:link, .yshortcuts a:visited, .yshortcuts a:hover, .yshortcuts a span { color: black; text-decoration: none !important; border-bottom: none !important; background: none !important; }
    /* Body text color for the New Yahoo. This example sets the font of Yahoo's Shortcuts to black. */
    code { white-space: normal; word-break: break-all; }
    #background-table { background-color: #c7c7c7; }
    /* Webkit Elements */
    #top-bar { border-radius: 6px 6px 0px 0px; -moz-border-radius: 6px 6px 0px 0px; -webkit-border-radius: 6px 6px 0px 0px; -webkit-font-smoothing: antialiased; background-color: #3d464c; color: #888888; }
    #top-bar a { font-weight: bold; color: #eeeeee; text-decoration: none; }
    #footer { border-radius: 0px 0px 6px 6px; -moz-border-radius: 0px 0px 6px 6px; -webkit-border-radius: 0px 0px 6px 6px; -webkit-font-smoothing: antialiased; }
    /* Fonts and Content */
    body, td { font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
    .header-content, .footer-content-left, .footer-content-right { -webkit-text-size-adjust: none; -ms-text-size-adjust: none; }
    /* Prevent Webkit and Windows Mobile platforms from changing default font sizes on header and footer. */
    .header-content { font-size: 12px; color: #888888; }
    .header-content a { font-weight: bold; color: #eeeeee; text-decoration: none; }
    #headline p { color: #999999; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; font-size: 36px; text-align: center; margin-top: 0px; margin-bottom: 30px; }
    #headline p a { color: #999999; text-decoration: none; }
    .article-title { font-size: 18px; line-height: 24px; color: #666; font-weight: bold; margin-top: 0px; margin-bottom: 18px; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
    .article-title a { color: #666; text-decoration: none; }
    .article-title.with-meta { margin-bottom: 0; }
    .article-meta { font-size: 13px; line-height: 20px; color: #ccc; font-weight: bold; margin-top: 0; }
    .article-heading { font-size: 16px; line-height: 20px; color: #444444; margin-top: 0px; margin-bottom: 18px; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
    .article-heading a { color: #2f82de; text-decoration: none; }
    .article-content { font-size: 13px; line-height: 18px; color: #444444; margin-top: 0px; margin-bottom: 18px; font-family: 'Helvetica Neue', Arial, Helvetica, Geneva, sans-serif; }
    .article-content a { color: #2f82de; font-weight: bold; text-decoration: none; }
    .article-content img { max-width: 100% }
    .article-content ol, .article-content ul { margin-top: 0px; margin-bottom: 18px; margin-left: 19px; padding: 0; }
    .article-content li { font-size: 13px; line-height: 18px; color: #444444; }
    .article-content li a { color: #2f82de; text-decoration: underline; }
    .article-content p { margin-bottom: 15px; }
    .footer-content-left { font-size: 12px; line-height: 15px; color: #888888; margin-top: 0px; margin-bottom: 15px; }
    .footer-content-left a { color: #eeeeee; font-weight: bold; text-decoration: none; }
    .footer-content-right { font-size: 11px; line-height: 16px; color: #888888; margin-top: 0px; margin-bottom: 15px; }
    .footer-content-right a { color: #eeeeee; font-weight: bold; text-decoration: none; }
    #footer { background-color: #3d464c; color: #CCC; }
    #footer a { color: #eeeeee; text-decoration: none; font-weight: bold; }
    #permission-reminder { white-space: normal; }
    #street-address { color: #ffffff; white-space: normal; }
</style>
<!--[if gte mso 9]>
  <style _tmplitem="430">
    .article-content ol, .article-content ul { margin: 0 0 0 24px; padding: 0; list-style-position: inside; }
  </style>
<![endif]-->
</head>
<body>
<table width="100%" cellpadding="0" cellspacing="0" border="0" id="background-table">
    <tbody>
    <tr>
        <td align="center" bgcolor="#c7c7c7">
            <table class="w640" style="margin:0 10px;" width="640" cellpadding="0" cellspacing="0" border="0">
                <tbody>
                <tr>
                    <td class="w640" width="640" height="20"></td>
                </tr>
                <tr>
                    <td class="w640" width="640">
                        <table id="top-bar" class="w640" width="640" cellpadding="0" cellspacing="0" border="0" bgcolor="#c7c7c7">
                            <tbody>
                            <tr>
                                <td class="w15" width="15"></td>
                                <td class="w325" width="350" valign="middle" align="left">
                                    <table class="w325" width="350" cellpadding="0" cellspacing="0" border="0">
                                        <tbody>
                                        <tr>
                                            <td class="w325" width="350" height="8"></td>
                                        </tr>
                                        </tbody>
                                    </table>
                                    <div class="header-content"><a href="http://www.ala.org.au/">visit the Atlas website</a></div>
                                    <table class="w325" width="350" cellpadding="0" cellspacing="0" border="0">
                                        <tbody>
                                        <tr>
                                            <td class="w325" width="350" height="8"></td>
                                        </tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td class="w30" width="30"></td>
                                <td class="w255" width="255" valign="middle" align="right">
                                    <div class="header-content"><a href="http://twitter.com/atlaslivingaust">follow on Twitter</a></div>
                                </td>
                                <td class="w15" width="15"></td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td id="header" class="w640" width="640" align="center" bgcolor="#3d464c" style="border-top:2px solid #c7c7c7;">
                        <div align="left" style="text-align:left;padding: 10px;">
                            <a href="http://www.ala.org.au/" title="visit the ALA website"><img src="http://www.ala.org.au/wp-content/themes/ala2011/images/logo.png" alt="ALA logo" style="display: block; margin: 5px 15px;"></a>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td class="w640" width="640" height="30" bgcolor="#ffffff"></td>
                </tr>
                <tr id="simple-content-row">
                    <td class="w640" width="640" bgcolor="#ffffff">
                        <table class="w640" width="640" cellpadding="0" cellspacing="0" border="0">
                            <tbody>
                            <tr>
                                <td class="w30" width="30"></td>
                                <td class="w580" width="580">
                                    <repeater>
                                        <layout label="Text with right-aligned image">
                                            <table class="w580" width="580" cellpadding="0" cellspacing="0" border="0">
                                                <tbody>
                                                <tr>
                                                    <td class="w580" width="580">
                                                        <p align="left" class="article-title">
                                                            <singleline label="Title">
                                                                Atlas Blogs and News
                                                            </singleline>
                                                        </p>
                                                    </td>
                                                </tr>
                                                <g:each in="${records}" var="blog">
                                                    <tr>
                                                        <td class="w580" width="580">
                                                            <p align="left" class="article-heading">
                                                                <singleline label="Title">
                                                                    <a href="${blog.url}">${blog.title}</a>
                                                                </singleline>
                                                            </p>
                                                            <table cellpadding="0" cellspacing="0" border="0" align="right">
                                                                <tbody>
                                                                <tr>
                                                                    <td class="w30" width="15"></td>
                                                                    <td><g:if test="${blog.thumbnail}"><img src="${blog.thumbnail}" alt="${blog.title}"></g:if></td>
                                                                </tr>
                                                                <tr>
                                                                    <td class="w30" width="15" height="5"></td>
                                                                    <td></td>
                                                                </tr>
                                                                </tbody>
                                                            </table>
                                                            <div align="left" class="article-content">
                                                                <multiline>
                                                                    ${raw(blog.excerpt)}
                                                                </multiline>
                                                            </div>
                                                            <div align="left" class="article-content"> </div>
                                                        </td>
                                                    </tr>
                                                </g:each>
                                                </tbody>
                                            </table>
                                        </layout>
                                    </repeater>
                                </td>
                                <td class="w30" width="30">
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="w640" width="640" height="15" bgcolor="#ffffff">
                        <div align="left" class="article-content" style="margin:10px 30px;">
                            <multiline>
                                For all the latest blog posts, visit the <a href="http://www.ala.org.au/blogs-news/">Atlas Blog</a>
                                <br/>
                                Modify your preferences or unsubscribe via the <a href="http://alerts.ala.org.au/">&quot;My Email Alerts&quot;</a> page
                            </multiline>
                        </div>
                    </td>
                </tr>

                <tr>
                    <td class="w640" width="640">
                        <table id="footer" class="w640" width="640" cellpadding="0" cellspacing="0" border="0" bgcolor="#c7c7c7">
                            <tbody>
                            <tr>
                                <td class="w30" width="30"></td>
                                <td class="w580 h0" width="360" height="10"></td>
                                <td class="w0" width="60"></td>
                                <td class="w0" width="160"></td>
                                <td class="w30" width="30"></td>
                            </tr>
                            <tr>
                                <td class="w30" width="40" style="padding-left: 20px; padding-top: 10px;">
                                    <a href="http://creativecommons.org/licenses/by/3.0/au/"
                                       title="External link to Creative Commons"><img
                                            src="http://www.ala.org.au/wp-content/themes/ala2011/images/creativecommons.png"
                                            width="88" height="31" alt=""></a>
                                </td>
                                <td class="w580" width="520" valign="top"  style="padding-left: 10px; padding-top: 10px;">
                                    <p style="font-size:11px;padding-left:5px;">This content is licensed under a <a
                                            href="http://creativecommons.org/licenses/by/3.0/au/"
                                            title="External link to Creative Commons" class="external">Creative Commons Attribution
                                        3.0 Australia License</a>. </p>
                                </td>
                                <td class="hide w0" width="160" valign="top">

                                </td>
                                <td class="w30" width="30"></td>
                            </tr>
                            <tr>
                                <td class="w30" width="30"></td>
                                <td class="w580 h0" width="360" height="15"></td>
                                <td class="w0" width="60"></td>
                                <td class="w0" width="160"></td>
                                <td class="w30" width="30"></td>
                            </tr>
                            </tbody>
                        </table>
                    </td>
                </tr>
                <tr>
                    <td class="w640" width="640" height="60"></td>
                </tr>
                </tbody>
            </table>
        </td>
    </tr>
    </tbody>
</table>

<g:render template="/email/unsubscribe"/>

</body>
</html>