
import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * todo check if this is still needed
 */
@Configuration
class S3ClientConfig {

    @Bean
    AmazonS3 amazonS3() {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new DefaultAWSCredentialsProviderChain())
                .withRegion("ap-southeast-2")
                .build()
    }
}