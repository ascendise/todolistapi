package ch.ascendise.todolistapi

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.web.filter.CorsFilter


@Configuration
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    @Autowired
    private lateinit var appConfig: ApplicationConfig

    override fun configure(http: HttpSecurity)
    {
        http
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .cors().and()
            .csrf().disable()
            .authorizeRequests{
                it.antMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                    .anyRequest().authenticated()
            }
            .exceptionHandling{
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.NOT_FOUND))
            }
            .oauth2ResourceServer { it.jwt() }
    }

    @Bean
    fun corsFilter(): CorsFilter? {
        val source = UrlBasedCorsConfigurationSource()
        val config = CorsConfiguration()
        config.allowCredentials = true
        config.allowedOriginPatterns = appConfig.allowedOriginPatterns.split(",")
        config.allowedHeaders = listOf("*")
        config.allowedMethods = listOf("*")
        source.registerCorsConfiguration("/**", config)
        return CorsFilter(source)
    }

}