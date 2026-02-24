package au.org.ala.alerts

/**
 * DownloadToken domain class represents a token that allows users to download a file. Each token is associated with a specific file and has an expiration time.
 * The token is generated when a user requests to download a file, and it is stored in the database.
 * The token is valid until the expiration time, after which it cannot be used to download
 */
class DownloadToken {

    String token
    String fileKey
    Date expiresAt
    Date createdAt

    def beforeInsert() {
        createdAt = new Date()
    }

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