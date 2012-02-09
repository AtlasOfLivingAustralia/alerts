package au.org.ala

import au.org.ala.auth.AuthenticationCookieUtils

class HeaderFooterTagLib {

  def grailsApplication
  
  static namespace = 'hf'     // namespace for headers and footers

  /**
   * All includes assume the existence of these config declarations:
   *
   *  ala.baseURL - usually "http://www.ala.org.au"
   *  bie.baseURL - usually "http://bie.ala.org.au"
   *  bie.searchPath - usually "/search"
   */

  /**
   * Display the page banner. Includes login/logout link and search box.
   *
   * @attr logoutUrl the local url that should invalidate the session and redirect to the auth
   *  logout url - defaults to {CH.config.grails.serverURL}/session/logout
   * @attr loginReturnToUrl where to go after logging in - defaults to current page
   * @attr logoutReturnToUrl where to go after logging out - defaults to current page
   * @attr loginReturnUrl where to go after login - defaults to current page
   * @attr casLoginUrl - defaults to {CH.config.security.cas.loginUrl}
   * @attr casLogoutUrl - defaults to {CH.config.security.cas.logoutUrl}
   * @attr ignoreCookie - if true the helper cookie will not be used to determine login - defaults to false
   */
  def banner = { attrs ->
    out << load('banner', attrs)
  }

  /**
   * Display the main menu.
   *
   * Note that highlighting of the current menu item is done by including the apropriate class in the
   * body tag, eg class="collections".
   */
  def menu = {
    out << load('menu', [:])
  }

  /**
   * Displays the page footer.
   */
  def footer = {
    out << load('footer', [:])
  }

  /**
   * Cache for includes. Expires after 30mins or when clearCache is called.
   */
  def hfCache = [
          banner: [timestamp: new Date().time, content: ""],
          menu: [timestamp: new Date().time, content: ""],
          footer: [timestamp: new Date().time, content: ""]]

  /**
   * Call this tag from a controller to clear the cache.
   */
  def clearCache = {
    hfCache.each { key, obj -> hfCache[key].content = ""}
    println "cache cleared"
  }

  /**
   * Get the content from cache of the web.
   * @param which specifies the include
   * @param attrs any specified params
   * @return
   */
  String load(which, attrs) {
    def content
    if (hfCache[which].content == "" || (new Date().time > hfCache[which].timestamp + 1800000)) {
      content = getContent(which)
      hfCache[which].content = content
      hfCache[which].timestamp = new Date().time
    }
    else {
      content = hfCache[which].content
      //println "using cache"
    }
    return transform(content, attrs)
  }

  /**
   * Loads the content from the web.
   * @param which specifies the include
   * @return
   */
  String getContent(which) {
    def url = grailsApplication.config.headerAndFooter.baseURL + '/' + which + ".html"
    //println url
    def conn = new URL(url).openConnection()
    try {
      conn.setConnectTimeout(10000)
      conn.setReadTimeout(50000)
      return conn.content.text
    } catch (SocketTimeoutException e) {
      log.warn "Timed out getting ${which} template. URL= ${url}."
      println "Timed out getting ${which} template. URL= ${url}."
    } catch (Exception e) {
      log.warn "Failed to get ${which} template. ${e.getClass()} ${e.getMessage()} URL= ${url}."
      println "Failed to get ${which} template. ${e.getClass()} ${e.getMessage()} URL= ${url}."
    }
    return ""
  }

  /**
   * Does the appropriate substitutions on the included content.
   * @param content
   * @param attrs any specified params to override defaults
   * @return
   */
  String transform(content, attrs) {
    content = content.replaceAll(/::centralServer::/, grailsApplication.config.ala.baseURL)
    content = content.replaceAll(/::searchServer::/, grailsApplication.config.bie.baseURL)
    content = content.replaceAll(/::searchPath::/, grailsApplication.config.bie.searchPath)
    if (content =~ "::loginLogoutListItem::") {
      // only do the work if it is needed
      content = content.replaceAll(/::loginLogoutListItem::/, buildLoginoutLink(attrs))
    }
    return content
  }

  /**
   * Builds the login or logout link based on current login status.
   * @param attrs any specified params to override defaults
   * @return
   */
  String buildLoginoutLink(attrs) {
    def requestUri = removeContext(grailsApplication.config.grails.serverURL) + request.forwardURI
    def logoutUrl = attrs.logoutUrl ?: grailsApplication.config.grails.serverURL + "/session/logout"
    def loginReturnToUrl = attrs.loginReturnToUrl ?: requestUri
    def logoutReturnToUrl = attrs.logoutReturnToUrl ?: requestUri
    def casLoginUrl = attrs.casLoginUrl ?: grailsApplication.config.security.cas.loginUrl
    def casLogoutUrl = attrs.casLogoutUrl ?: grailsApplication.config.security.cas.logoutUrl

    if ((attrs.ignoreCookie != "true" &&
            AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) ||
            request.userPrincipal) {
      return "<a href='${logoutUrl}" +
              "?casUrl=${casLogoutUrl}" +
              "&appUrl=${logoutReturnToUrl}'>Logout</a>"
    } else {
      // currently logged out
      return "<a href='${casLoginUrl}?service=${loginReturnToUrl}'><span>Log in</span></a>"
    }
  }

  /**
   * Remove the context path and params from the url.
   * @param urlString
   * @return
   */
  private String removeContext(urlString) {
    def url = urlString.toURL()
    return url.protocol + "://"+ url.host + ":" + url.port
  }
}