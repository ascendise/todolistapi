package ch.ascendise.todolistapi

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties
class ApplicationConfig(
    var allowedOriginPatterns: String = "*"
) {
}