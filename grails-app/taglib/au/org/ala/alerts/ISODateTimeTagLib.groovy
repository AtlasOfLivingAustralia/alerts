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

        // Ensure date is a Date object
        if (!(date instanceof Date)) {
            out << ''
            return
        }

        // Format the Date as ISO-8601 in UTC (e.g., 2026-09-01T15:00:00Z)
        def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
        def isoUtc = sdf.format(date)

        // Output a span with the UTC ISO string in data-time
        // Generate a unique ID for this span
        def spanId = "localTime_${System.currentTimeMillis()}_${Math.random().toString().substring(2)}"
        out << "<span id=\"${spanId}\" class=\"ISODateTime\" data-iso=\"${isoUtc}\"></span>"
        // Add inline script to convert this specific span
        out << """
        <script type="text/javascript">
            (function() {
                var el = document.getElementById('${spanId}');
                if (el) {
                    var isoString = el.getAttribute('data-iso');
                    var format = el.getAttribute('data-format') || 'medium';
                    var dt = new Date(isoString); // JavaScript Date constructor handles ISO-8601 strings
                    var options = {};
                    
                    if (format === 'short') {
                        options = { year: '2-digit', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' };
                    } else if (format === 'long') {
                        options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric', hour: '2-digit', minute: '2-digit', second: '2-digit' };
                    } else { // medium (default)
                        options = { weekday: 'short', year: 'numeric', month: 'short', day: '2-digit', hour: '2-digit', minute: '2-digit' };
                    }
                    
                    el.textContent = dt.toLocaleString(undefined, options);
                }
            })();
        </script>
        """
    }
}