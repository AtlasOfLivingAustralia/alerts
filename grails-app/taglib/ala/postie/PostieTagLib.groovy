package ala.postie

//import org.codehaus.groovy.grails.plugins.springsecurity.AuthorizeTools
import java.text.NumberFormat
import java.text.DecimalFormat
import org.codehaus.groovy.grails.web.util.StreamCharBuffer
import grails.converters.JSON
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import au.org.ala.cas.util.AuthenticationCookieUtils

class PostieTagLib {

    static namespace = 'cl'

    def authService

    def grailsApplication

    def loggedInName = {
        def userName = authService.username()
        if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << "logged in as ${AuthenticationCookieUtils.getUserName(request)}"
        } else {
            out << "no cookie found " + userName
        }
    }

  /**
     * Generate the login link for the banner.
     *
     * Will be to log in or out based on current auth status.
     * TODO: also check for user principal in request in case cookies are disabled
     * 
     * @attr showUser if supplied, the username will be shown before the logout link
     * @attr fixedAppUrl if supplied will be used for logout instead of the current page
     */
   def loginoutLink2011 = { attrs ->
       def requestUri = grailsApplication.config.security.cas.casServerName + request.forwardURI
       if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
           // currently logged in
           if (attrs.showUser) {
               out << "<span id='logged-in'>Logged in as ${loggedInUsername()}</span>"
             }
           out << link(controller: 'public', action: 'logout',
                       params: [casUrl: grailsApplication.config.security.cas.logoutUrl,
                               appUrl: attrs.fixedAppUrl ?: requestUri]) {'Logout'}
         } else {
           // currently logged out
           out << "<a href='https://auth.ala.org.au/cas/login?service=${requestUri}'><span>Log in</span></a>"
         }
     }

   def loggedInUsername = {
       if (grailsApplication.config.security.cas.bypass) {
           out << 'cas bypassed'
         }
       else if (request.getUserPrincipal()) {
           out << request.getUserPrincipal().name
         }
       else if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
           out << AuthenticationCookieUtils.getUserName(request) + '*'
         }
     }

    /**
     * Generate the link the login link for the banner.
     *
     * Will be to log in or out based on current auth status.
     */
    def loginoutLink = {

        def userName = authService.username()
        def serverName = grailsApplication.config.serverName
        def requestUri = serverName + request.forwardURI
        if (userName) {
            // currently logged in
            out << "<a id='${userName}' href='${resource(file:'logout', dir:'user')}?casUrl=${grailsApplication.config.security.cas.logoutUrl}&appUrl=${requestUri}'><span>Log out</span></a>"
        } else {
            // currently logged out
            out << "<a href='https://auth.ala.org.au/cas/login?service=${requestUri}'><span>Log in</span></a>"
        }
    }

    /**
     * Decorates the role if present
     *
     * @attrs role the role to display
     */
    def roleIfPresent = { attrs, body ->
        out << (!attrs.role ? '' : ' - ' + attrs.role.encodeAsHTML())
    }

    /**
     * Indicates user can edit if admin
     *
     * @attrs admin - is the user an admin for the collection
     */
    def adminIfPresent = { attrs, body ->
        out << (attrs.admin ? '(Authorised to edit this collection)' : '')
    }

    /**
     * <g:ifAllGranted role="ROLE_COLLECTION_EDITOR,ROLE_COLLECTION_ADMIN">
     *  All the listed roles must be granted for the tag to output its body.
     * </g:ifAllGranted>
     */
    def ifAllGranted = { attrs, body ->
        def granted = true
        if (grailsApplication.config.security.cas.bypass) {
            granted = true
        } else {
            def roles = attrs.role.toString().tokenize(',')
            roles.each {
                if (!request.isUserInRole(it)) {
                    granted = false
                }
            }
        }
        if (granted) {
            out << body()
        }
    }

    /**
     * <cl:ifGranted role="ROLE_COLLECTION_ADMIN">
     *  The specified role must be granted for the tag to output its body.
     * </g:ifGranted>
     */
    def ifGranted = { attrs, body ->
        if (grailsApplication.config.security.cas.bypass || request.isUserInRole(attrs.role)) {
            out << body()
        }
    }

    /**
     * <cl:ifNotGranted role="ROLE_COLLECTION_ADMIN">
     *  The specified role must be missing for the tag to output its body.
     * </g:ifNotGranted>
     */
    def ifNotGranted = { attrs, body ->
        if (!grailsApplication.config.security.cas.bypass && !request.isUserInRole(attrs.role)) {
            out << body()
        }
    }

    def isLoggedIn = { attrs, body ->
        if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << body()
        }
    }

    def isNotLoggedIn = { attrs, body ->
        if (!AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << body()
        }
    }


    private boolean isAdmin() {
        return grailsApplication.config.security.cas.bypass || request?.isUserInRole(ProviderGroup.ROLE_ADMIN)
    }
}