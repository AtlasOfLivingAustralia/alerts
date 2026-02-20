package au.org.ala.alerts

class DownloadToken {

    String token
    String fileKey
    Date expiresAt
    Date createdAt

    static constraints = {
        token unique: true, nullable: false, maxSize: 64
        fileKey nullable: false, maxSize: 512
        expiresAt nullable: false
        createdAt nullable: true
    }

    static mapping = {
        table 'download_token'
        version false
    }
}