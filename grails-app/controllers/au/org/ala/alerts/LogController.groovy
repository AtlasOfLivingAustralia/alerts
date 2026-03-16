package au.org.ala.alerts

import au.org.ala.web.AlaSecured
import grails.gorm.transactions.Transactional
import grails.validation.ValidationException


class LogController {
    static allowedMethods = [save: "POST", update: "PUT", delete: "DELETE"]

    def index(Integer max) {
        params.max = Math.min(max ?: 20, 100)
        params.sort = 'executedAt'
        params.order = 'desc'
        respond ErrorLog.list(params), model: [errorLogCount: ErrorLog.count()]
    }

    def show(Long id) {
        respond ErrorLog.get(id)
    }


    @Transactional
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def update() {
        def errorLog = ErrorLog.get(params.id)
        if (!errorLog) {
            notFound()
            return
        }
        // Only update the reviewed field from the checkbox
        errorLog.reviewed = params.reviewed == 'true'
        try {
            errorLog.save(flush: true)
        } catch (ValidationException e) {
            respond errorLog.errors, view: 'edit'
            return
        }
        redirect(action: "index")
    }

    @Transactional
    @AlaSecured(value = ['ROLE_ADMIN', 'ROLE_BIOSECURITY_ADMIN'], anyRole = true)
    def delete(Long id) {
        def errorLog = ErrorLog.get(id)
        if (errorLog == null) {
            notFound()
            return
        }
        errorLog.delete(flush: true)
        redirect(action: "index")
    }

    protected void notFound() {
        render status: 404, text: 'Log not found'
    }
}
