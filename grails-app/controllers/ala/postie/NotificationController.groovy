package ala.postie

class NotificationController {

    def notificationService
    def emailService
    def authService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def myalerts = { redirect(action: "myAlerts", params: params) }

    def myAlerts = {

      //enabled alerts
      def notificationInstanceList = Notification.findAllByUserEmail(authService.username().toString().toLowerCase())

      //split into custom and non-custom...
      def enabledQueries = notificationInstanceList.collect { it.query }
      def enabledIds =  enabledQueries.collect { it.id }

      //all types
      def allAlertTypes = Query.findAll()

      allAlertTypes.removeAll { enabledIds.contains(it.id) }

      [disabledQueries:allAlertTypes, enabledQueries:enabledQueries]
    }

    def addMyAlert = {
        def notificationInstance = new Notification()
        notificationInstance.query =  Query.findById(params.id)
        notificationInstance.userEmail = authService.username().toString().toLowerCase()
        if (notificationInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'notification.label', default: 'Notification'), notificationInstance.id])}"
            redirect(action: "myAlerts")
        }
    }

    def deleteMyAlert ={
        def userEmail = authService.username().toString().toLowerCase()
        def query = Query.get(params.id)
        def notificationInstance = Notification.findByUserEmailAndQuery(userEmail, query)
        if (notificationInstance) {
            notificationInstance.each { it.delete(flush: true) }
            flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
            redirect(action: "myAlerts")
        }
    }

    def checkNow = {
      Notification notification = Notification.get(params.id)
      boolean sendUpdateEmail = notificationService.checkStatus(notification.query)
      if(sendUpdateEmail){
        emailService.sendNotificationEmail(notification)
      }
      redirect(action: "show", params: params)
    }

    def index = {

      //if is ADMIN, then index page
      //else redirect to /notification/myAlerts
      if(authService.userInRole("ADMIN")){
        redirect(action: "admin")
      } else {
        redirect(action: "myAlerts")
      }
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [notificationInstanceList: Notification.list(params), notificationInstanceTotal: Notification.count()]
    }

    def create = {
        def notificationInstance = new Notification()
        notificationInstance.properties = params
        return [notificationInstance: notificationInstance]
    }

    def save = {
        def notificationInstance = new Notification(params)
        if (notificationInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'notification.label', default: 'Notification'), notificationInstance.id])}"
            redirect(action: "show", id: notificationInstance.id)
        }
        else {
            render(view: "create", model: [notificationInstance: notificationInstance])
        }
    }

    def show = {
        def notificationInstance = Notification.get(params.id)
        if (!notificationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
            redirect(action: "list")
        }
        else {
            [notificationInstance: notificationInstance]
        }
    }

    def edit = {
        def notificationInstance = Notification.get(params.id)
        if (!notificationInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [notificationInstance: notificationInstance]
        }
    }

    def update = {
        def notificationInstance = Notification.get(params.id)
        if (notificationInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (notificationInstance.version > version) {
                    
                    notificationInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'notification.label', default: 'Notification')] as Object[], "Another user has updated this Notification while you were editing")
                    render(view: "edit", model: [notificationInstance: notificationInstance])
                    return
                }
            }
            notificationInstance.properties = params
            if (!notificationInstance.hasErrors() && notificationInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'notification.label', default: 'Notification'), notificationInstance.id])}"
                redirect(action: "show", id: notificationInstance.id)
            }
            else {
                render(view: "edit", model: [notificationInstance: notificationInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def notificationInstance = Notification.get(params.id)
        if (notificationInstance) {
            try {
                notificationInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'notification.label', default: 'Notification'), params.id])}"
            redirect(action: "list")
        }
    }
}
