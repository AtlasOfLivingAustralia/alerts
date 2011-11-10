import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.web.util.WebUtils
import com.jayway.jsonpath.JsonPath

println("read it all")

def url = new URL("http://biocache.ala.org.au/ws/occurrences/search?q=*:*&pageSize=10")
def json = IOUtils.toString(url.newReader())


def occurrences = JsonPath.read(json, "\$.occurrences")

occurrences.each { oc ->
  println("data provider: " + oc.dataProviderName)
  println("latitude: " + oc.decimalLatitude)
  println("longitude: " + oc.decimalLongitude)
  println("scientific name: " + oc.scientificName)
  println("basis of record: " + oc.basisOfRecord)
  println("family: " + oc.family)
  println("image: " + oc.image)
}

//println(JsonPath.read(json, "\$.occurrences[0].dataProviderName"))
//println(JsonPath.read(json, "\$.occurrences[0].latitude"))
//println(JsonPath.read(json, "\$.occurrences[0].longitude"))
//println(JsonPath.read(json, "\$.occurrences[0].scientificName"))
//println(JsonPath.read(json, "\$.occurrences[0].family"))







