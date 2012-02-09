package ala.postie

class ProxyController {
   def logout = {
       session.invalidate()
       redirect(url:"${params.casUrl}?url=${params.appUrl}")
   }
}
