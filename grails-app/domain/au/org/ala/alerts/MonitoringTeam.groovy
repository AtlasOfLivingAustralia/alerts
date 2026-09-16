package au.org.ala.alerts

class MonitoringTeam {
    String team
    String email

    static constraints = {
        team nullable: false, blank: false
        email nullable: false, blank: false, email: true
    }

    static mapping = {
        table 'monitoring_team'
        version false
    }

    String toString() {
        "${team} <${email}>"
    }
}
