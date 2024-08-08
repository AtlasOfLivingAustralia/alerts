package au.org.ala.alerts

class Frequency {
    transient static String HOURLY  = 'hourly'
    transient static String DAILY  = 'daily'
    transient static String WEEKLY  = 'weekly'
    transient static String MONTHLY  = 'monthly'

    String name
    Integer periodInSeconds = 86400l   //default to daily
    Date lastChecked

    static constraints = {
        lastChecked nullable: true
    }

    static mapping = {
        version false
    }

    boolean isFrequency(String frequency) {
        name == frequency
    }

    String toString() { name }
}
