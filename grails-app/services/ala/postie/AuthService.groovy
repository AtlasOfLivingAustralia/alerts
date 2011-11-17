package ala.postie

import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.commons.ConfigurationHolder

class AuthService {

  static transactional = true

  def serviceMethod() {}

  def username() {
    return (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.email?.toString()?.toLowerCase()  ) ?: null
  }

  def displayName() {
    if (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.firstname) {
      ((RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.firstname) +
              " " + (RequestContextHolder.currentRequestAttributes()?.getUserPrincipal()?.attributes?.lastname))
    } else {
      null
    }
  }

  protected boolean userInRole(role) {
    return ConfigurationHolder.config.security.cas.bypass ||
            RequestContextHolder.currentRequestAttributes()?.isUserInRole(role) // || isAdmin()
  }
}
