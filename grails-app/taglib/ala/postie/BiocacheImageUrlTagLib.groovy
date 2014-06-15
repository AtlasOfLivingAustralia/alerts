package ala.postie

class BiocacheImageUrlTagLib {

  static namespace = 'biocacheImage'

  def imageTag = { attrs, body ->
    out << '<img src="' + attrs.imageUrl + '" alt="image for record"/>'
  }
}