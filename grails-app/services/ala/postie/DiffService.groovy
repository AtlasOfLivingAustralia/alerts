package ala.postie

import java.util.zip.GZIPInputStream
import com.jayway.jsonpath.JsonPath

class DiffService {

  static transactional = true

  def serviceMethod() {}

  Boolean hasChangedJsonDiff(QueryResult queryResult){
    if(queryResult.lastResult != null && queryResult.previousResult != null){
      //decompress both and compare lists
      String last = decompressZipped(queryResult.lastResult)
      String previous = decompressZipped(queryResult.previousResult)

      println(JsonPath.read(last, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath))
      println(JsonPath.read(previous, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath))
      List<String> ids1 = JsonPath.read(last, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath)
      List<String> ids2 = JsonPath.read(previous, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath)
      //println("Comparing: " + ids1 +" TO " + ids2)

      List<String> diff = ids1.findAll { !ids2.contains(it) }

      diff.each { println it }

      //println("has the layer list changed: " + !diff.empty)
      !diff.empty
    } else {
      false
    }
  }

  def getNewRecordsFromDiff(QueryResult queryResult){

    def records = []

    if(queryResult.lastResult != null && queryResult.previousResult != null){
      //decompress both and compare lists
      String last = decompressZipped(queryResult.lastResult)
      String previous = decompressZipped(queryResult.previousResult)

      //println(JsonPath.read(last, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath))
      //println(JsonPath.read(previous, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath))

      List<String> ids1 = JsonPath.read(last, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath)
      List<String> ids2 = JsonPath.read(previous, queryResult.query.recordJsonPath + "." +queryResult.query.idJsonPath)
      List<String> diff = ids1.findAll { !ids2.contains(it) }
      //pull together the records that have been added

      def allRecords = JsonPath.read(last, queryResult.query.recordJsonPath)
      allRecords.each { record ->
        if(diff.contains(record.get(queryResult.query.idJsonPath))){
          records.add(record)
        }
      }
    }
    records
  }


  public static String decompressZipped(byte[] zipped){
    GZIPInputStream input = new GZIPInputStream(new ByteArrayInputStream(zipped))
    StringBuffer sb = new StringBuffer()
    List<String> readed = null

    try {
      while (input.available() && !(readed = input.readLines()).isEmpty()) {
        //println(readed.join(""))
        sb.append(readed.join(""))
      }
    } catch (Exception e) {
      //e.printStackTrace()
    }
    input.close()
    sb.toString()
  }
}
