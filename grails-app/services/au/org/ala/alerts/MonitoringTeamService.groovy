package au.org.ala.alerts

class MonitoringTeamService {

    def getTeamMembers(String teamName) {
        def monitoringTeamMembers = []
        MonitoringTeam.withTransaction { status ->
            monitoringTeamMembers = MonitoringTeam.findAllByTeam(teamName)
        }
        return monitoringTeamMembers
    }

    def deleteTeamMember(String teamName, Serializable id) {
        MonitoringTeam.withTransaction { status ->
            def member = MonitoringTeam.findByTeamAndId(teamName, id as Long)
            if (member) {
                member.delete(flush: true)
                return true
            } else {
                return false
            }
        }
    }

    /**
     * Adds an email to a monitoring team. Idempotent - an email already in the team is returned as-is.
     *
     * @return the persisted MonitoringTeam instance
     */
    MonitoringTeam addTeamMember(String teamName, String email) {
        MonitoringTeam.withTransaction { status ->
            def existing = MonitoringTeam.findByTeamAndEmail(teamName, email)
            if (existing) {
                return existing
            }
            return new MonitoringTeam(team: teamName, email: email).save(flush: true, failOnError: true)
        }
    }

    def getEmails(String teamName) {
        def emails = []
        MonitoringTeam.withTransaction { status ->
            def members = MonitoringTeam.findAllByTeam(teamName)
            emails = members.collect { it.email }
        }
        return emails
    }

}
