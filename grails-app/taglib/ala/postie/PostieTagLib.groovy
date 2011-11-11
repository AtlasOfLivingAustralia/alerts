package ala.postie

//import org.codehaus.groovy.grails.plugins.springsecurity.AuthorizeTools
import java.text.NumberFormat
import java.text.DecimalFormat
import org.codehaus.groovy.grails.web.util.StreamCharBuffer
import grails.converters.JSON
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import org.codehaus.groovy.grails.web.converters.exceptions.ConverterException
import au.org.ala.cas.util.AuthenticationCookieUtils

class PostieTagLib {
    //def authenticateService

    static namespace = 'cl'

    def authService

    def loggedInName = {
        def userName = authService.username()
        if (AuthenticationCookieUtils.cookieExists(request, AuthenticationCookieUtils.ALA_AUTH_COOKIE)) {
            out << "logged in as ${AuthenticationCookieUtils.getUserName(request)}"
        } else {
            out << "no cookie found " + userName
        }
    }

    /**
     * Generate the link the login link for the banner.
     *
     * Will be to log in or out based on current auth status.
     */
    def loginoutLink = {

        def userName = authService.username()
        def serverName = ConfigurationHolder.config.security.cas.serverName
        def requestUri = serverName + request.forwardURI
        if (userName) {
            // currently logged in
            out << "<a id='${userName}' href='${resource(file:'logout', dir:'user')}?casUrl=${ConfigurationHolder.config.security.cas.logoutUrl}&appUrl=${requestUri}'><span>Log out</span></a>"
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
        if (ConfigurationHolder.config.security.cas.bypass) {
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
        if (ConfigurationHolder.config.security.cas.bypass || request.isUserInRole(attrs.role)) {
            out << body()
        }
    }

    /**
     * <cl:ifNotGranted role="ROLE_COLLECTION_ADMIN">
     *  The specified role must be missing for the tag to output its body.
     * </g:ifNotGranted>
     */
    def ifNotGranted = { attrs, body ->
        if (!ConfigurationHolder.config.security.cas.bypass && !request.isUserInRole(attrs.role)) {
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

    def loggedInUsername = { attrs ->
        if (ConfigurationHolder.config.security.cas.bypass) {
            out << 'cas bypassed'
        } else if (request.getUserPrincipal()) {
        	out << request.getUserPrincipal().name
        }
    }

    private boolean isAdmin() {
        return ConfigurationHolder.config.security.cas.bypass || request?.isUserInRole(ProviderGroup.ROLE_ADMIN)
    }
}