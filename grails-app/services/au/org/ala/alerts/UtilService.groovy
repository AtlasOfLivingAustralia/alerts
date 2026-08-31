package au.org.ala.alerts

import java.text.SimpleDateFormat

class UtilService {
    /**
     * Formats a date as an ISO-8601 UTC string, so JSON consumers get a stable, timezone-explicit
     * value rather than a locale/server-timezone dependent one. Returns null for a null date.
     */
     static String formatUtc(Date date) {
        if (!date) {
            return null
        }
         SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.timeZone = TimeZone.getTimeZone('UTC')
        sdf.format(date)
    }
}
