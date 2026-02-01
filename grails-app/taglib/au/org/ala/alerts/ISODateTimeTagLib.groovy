package au.org.ala.alerts

import java.text.SimpleDateFormat

class ISODateTimeTagLib {

    // Define the namespace for GSP usage
    static namespace = "alerts"

    /**
     * Outputs a <span> element containing the given Date in ISO-8601 UTC format.
     *
     * This is intended for JavaScript to read the UTC time and render it in the
     * browser's local timezone.
     *
     * Usage in GSP:
     *   <alerts:ISODateTime date="${job.nextFireTime}" />
     *
     * Attributes:
     *   date - a java.util.Date object (required)
     */
    def ISODateTime = { attrs, body ->
        def date = attrs.date
        if (!date) {
            out << ''
            return
        }

        // Format the Date as ISO-8601 in UTC (e.g., 2026-01-23T03:30:00Z)
        def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        def isoUtc = sdf.format(date)

        // Output a span with the UTC ISO string in data-time
        out << "<span class=\"ISODateTime\" data-time=\"${isoUtc}\"></span>"
    }
}