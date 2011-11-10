import org.apache.commons.io.IOUtils
import org.codehaus.groovy.grails.web.util.WebUtils
import com.jayway.jsonpath.JsonPath

println("read it all")

def json1 = IOUtils.toString((new URL("http://localhost/layers.json")).newReader())
def json2 = IOUtils.toString((new URL("http://localhost/layers-edit.json")).newReader())


List<String> ids1 = JsonPath.read(json1, "\$.layerList.id")
List<String> ids2 = JsonPath.read(json2, "\$.layerList.id")

List<String> diff = ids1.findAll { !ids2.contains(it) }

diff.each { println it }

//println(JsonPath.read(json, "\$.occurrences[0].dataProviderName"))
//println(JsonPath.read(json, "\$.occurrences[0].latitude"))
//println(JsonPath.read(json, "\$.occurrences[0].longitude"))
//println(JsonPath.read(json, "\$.occurrences[0].scientificName"))
//println(JsonPath.read(json, "\$.occurrences[0].family"))







