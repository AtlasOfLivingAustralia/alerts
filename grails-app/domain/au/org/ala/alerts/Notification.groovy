package au.org.ala.alerts

class Notification {

  Query query
  User user

  String toString(){
        "Query: " + query.id + " for user ID: " + user.id + ", Email" + user.email
  }
}
