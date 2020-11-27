import grails.util.Holders
import org.springframework.web.servlet.i18n.SessionLocaleResolver

beans = {
    localeResolver(SessionLocaleResolver) {
        defaultLocale= new java.util.Locale(Holders.config.siteDefaultLanguage as String)
    }
}
