package ala.postie

import org.apache.commons.io.FilenameUtils

class BiocacheImageUrlTagLib {

  static namespace = 'biocacheImage'

  def imageTag = { attrs, body ->
    String url = attrs.imageUrl.replaceAll('/data/biocache-media/', 'http://biocache.ala.org.au/biocache-media/')
    //convert to thumb URL
    String extension = "." + FilenameUtils.getExtension(url)
    url = url.replace(extension, "__small" + extension)
    out << '<img src="' + url + '" alt="image for record"/>'
  }
}