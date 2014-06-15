package au.org.ala.alerts

class ProxyController {
   def logout = {
       session.invalidate()
       redirect(url:"${params.casUrl}?url=${params.appUrl}")
   }
}
