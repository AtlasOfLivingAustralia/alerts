package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.converters.JSON
import grails.gorm.transactions.Transactional
import grails.util.Holders
import org.springframework.dao.DataIntegrityViolationException

class QueryController {

    static allowedMethods = [save: "POST", update: "POST", update: "PUT", delete: ["POST"]]
    def queryService
    def userService
    def notificationService
    def messageSource
    def siteLocale = new Locale.Builder().setLanguageTag(Holders.config.siteDefaultLanguage as String).build()

    def index() {
        redirect(action: "list", params: params)
    }

    Map getQueryAndFQ(String str) {
        int startOfQuery = str.indexOf('?')
        def q = ""
        def fq = []

        if (startOfQuery > 0) {
            String queryPart = str.substring(startOfQuery + 1)

            queryPart.split('&').each {
                if (it.startsWith('q=')) {
                    q = it.substring(2)
                } else if (it.startsWith('fq=')) {
                    fq << it.substring(2)
                }
            }
        }
        ['q': q, 'fq': fq]
    }

    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def listBiocacheInconsistent() {
        def inconsistentQueries = []

        def results = [:]

        def queries = Query.findAll()
        queries.each {
            def queryPathParams = getQueryAndFQ(it.queryPath)
            def queryPathUIParams = getQueryAndFQ(it.queryPathForUI)
            def qInconsistent = queryPathParams.q != queryPathUIParams.q
            def fqInconsistent = queryPathParams.fq.size() != queryPathUIParams.fq.size()
            if (!fqInconsistent) {
                queryPathParams.fq.eachWithIndex { param, idx ->
                    if (queryPathUIParams.fq[idx] != param) {
                        fqInconsistent = true
                    }
                }
            }
            if (qInconsistent || fqInconsistent) {
                inconsistentQueries << it
                results.put(it.id, [qInconsistent: qInconsistent, fqInconsistent: fqInconsistent])
            }
        }

        params.max = Math.min(params.max ? params.int('max') : 1000, 10000)
        [queryInstanceList: inconsistentQueries, queryInstanceTotal: inconsistentQueries.size(), results: results]
    }

    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def list() {
        params.max = Math.min(params.max ? params.int('max') : 1000, 10000)
        [queryInstanceList: Query.list(params), queryInstanceTotal: Query.count()]
    }

    @Transactional
    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def create() {
        [queryInstance: new Query(params)]
    }

    @Transactional
    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def save() {
        def queryInstance = new Query(params)
        if (!queryInstance.save(flush: true)) {
            render(view: "create", model: [queryInstance: queryInstance])
            return
        }

        flash.message = message(code: 'default.created.message', args: [message(code: 'query.label', default: 'Query'), queryInstance.id])
        redirect(action: "show", id: queryInstance.id)
    }

    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'notification', redirectAction = 'myAlerts', message = "You don't have permission to view that page.")
    def show() {
        def queryInstance = Query.get(params.id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])
            redirect(action: "list")
            return
        }
        [queryInstance: queryInstance]
    }

    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'admin', redirectAction = 'index', message = "You don't have permission to edit that record.")
    def edit() {
        def queryInstance = Query.get(params.id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])
            redirect(action: "list")
            return
        }

        [queryInstance: queryInstance]
    }

    @Transactional
    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'admin', redirectAction = 'index', message = "You don't have permission to update that record.")
    def update() {
        def queryInstance = Query.get(params.id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])
            redirect(action: "list")
            return
        }

        if (params.version) {
            def version = params.version.toLong()
            if (queryInstance.version > version) {
                queryInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                        [message(code: 'query.label', default: 'Query')] as Object[],
                        "Another user has updated this Query while you were editing")
                render(view: "edit", model: [queryInstance: queryInstance])
                return
            }
        }

        queryInstance.properties = params

        if (!queryInstance.save(flush: true)) {
            render(view: "edit", model: [queryInstance: queryInstance])
            return
        }

        flash.message = message(code: 'default.updated.message', args: [message(code: 'query.label', default: 'Query'), queryInstance.id])
        redirect(action: "show", id: queryInstance.id)
    }

    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'admin', redirectAction = 'index', message = "You don't have permission to delete that record.")
    def delete() {
        def queryInstance = Query.get(params.id)
        if (!queryInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'query.label', default: 'Query'), params.id])
            redirect(action: "list")
            return
        }

        try {
            if (queryInstance.notifications?.size() == 0) {
                queryService.deleteQuery(queryInstance)
                flash.message = message(code: 'default.deleted.message', args: [message(code: 'query.label', default: 'Query'), params.id])
                redirect(action: "list")
            } else {
                flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'query.label', default: 'Query'), params.id])
                redirect(action: "show", id: params.id)
            }
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'query.label', default: 'Query'), params.id])
            redirect(action: "show", id: params.id)
        }
    }

    def subscribers() {
        def queryid = Long.valueOf(params.queryid)
        render view: "subscribers", model: [users: queryService.getSubscribers(queryid), queryid: queryid]
    }

    def unsubscribeAlert() {
        if (!params.useremail || params.useremail.allWhitespace) {
            flash.message = messageSource.getMessage("unsubscribeusers.controller.error.emptyemail", null, "User email can't be empty.", siteLocale)
        } else if (!params.queryid || params.queryid.allWhitespace) {
            flash.message = messageSource.getMessage("unsubscribeusers.controller.error.emptyqueryid", null, "Query Id can't be empty.", siteLocale)
        } else {
            User user = userService.getUserByEmail(params.useremail);
            if (user) {
                notificationService.deleteAlertForUser(user, Long.valueOf(params.queryid))
            } else {
                flash.message = messageSource.getMessage('unsubscribeusers.controller.error.emailnotfound', [params.useremail] as Object[], "User with email: {0} are not found in the system.", siteLocale)
            }
        }
        redirect(action: "subscribers", params: [queryid: params.queryid])
    }

    @AlaSecured(value = 'ROLE_ADMIN', redirectController = 'admin', redirectAction = 'index', message = "You don't have permission to delete that query.")
    def wipe() {
        def result =[:]
        if (params.id && (!params.id.allWhitespace)) {
            def queryId = params.id as Integer
            result = queryService.wipe(queryId)
        } else {
            result['status'] = 1
            result['message'] = "Query id can't be empty."
        }
        render(result as JSON)
    }
}
