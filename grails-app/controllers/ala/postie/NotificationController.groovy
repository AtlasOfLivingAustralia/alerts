package ala.postie

class NotificationController {

    def notificationService
    def emailService
    def userService
    def authService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def myalerts = { redirect(action: "myAlerts", params: params) }

    def myAlerts = {

      User user = userService.getUser()
      log.debug('Viewing my alerts :  ' + user)

      //enabled alerts
      def notificationInstanceList = Notification.findAllByUser(user)

      //split into custom and non-custom...
      def enabledQueries = notificationInstanceList.collect { it.query }
      def enabledIds =  enabledQueries.collect { it.id }

      //all types
      def allAlertTypes = Query.findAllByCustom(false)

      allAlertTypes.removeAll { enabledIds.contains(it.id) }
      def customQueries = enabledQueries.findAll { it.custom }
      def standardQueries = enabledQueries.findAll { !it.custom }

      [disabledQueries:allAlertTypes, enabledQueries:standardQueries, customQueries:customQueries, frequencies:Frequency.listOrderByPeriodInSeconds(), user:user]
    }

    def addMyAlert = {
      log.debug('add my alert '+ params.id)
      def notificationInstance = new Notification()
      notificationInstance.query =  Query.findById(params.id)
      notificationInstance.user = userService.getUser()
      //does this already exist?
      def exists = Notification.findByQueryAndUser(notificationInstance.query, notificationInstance.user)
      if(!exists){
        notificationInstance.save(flush: true)
      }
      return null
    }

    def deleteMyAlert = {

      def user = userService.getUser()
      def query = Query.get(params.id)
      log.debug('Deleting my alert :  ' + params.id + ' for user : ' + authService.username())

      def notificationInstance = Notification.findByUserAndQuery(user, query)
      if (notificationInstance) {
        log.debug('Deleting my notification :  ' + params.id)
        notificationInstance.each { it.delete(flush: true) }
      } else {
        log.error('*** Unable to find  my notification - no delete :  ' + params.id)
      }
      return null
    }

    def deleteMyAlertWR = {
      def user = userService.getUser()

      //this is a hack to get around a CAS issue
      if(user == null){
        user = User.findByEmail(params.userId)
      }

      def query = Query.get(params.id)
      log.debug('Deleting my alert :  ' + params.id + ' for user : ' + authService.username())

      def notificationInstance = Notification.findByUserAndQuery(user, query)
      if (notificationInstance) {
        log.debug('Deleting my notification :  ' + params.id)
        notificationInstance.each { it.delete(flush: true) }
      } else {
        log.error('*** Unable to find  my notification - no delete :  ' + params.id)
      }
      redirect(action:'myAlerts')
    }


    def changeFrequency ={
        def user = userService.getUser()
        log.debug("Changing frequency to: " + params.frequency)
        user.frequency = ala.postie.Frequency.findByName(params.frequency)
        user.save(true)
        return null
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
