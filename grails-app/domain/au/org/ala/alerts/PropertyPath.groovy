package au.org.ala.alerts

class PropertyPath {
  Query query
  String jsonPath
  String name
  boolean fireWhenNotZero = false
  boolean fireWhenChange = false

  String toString(){
      "name: " + name + ", path: " + jsonPath + ", fireWhenNotZero: " + fireWhenNotZero + ", fireWhenChange: " + fireWhenChange
 }
}
