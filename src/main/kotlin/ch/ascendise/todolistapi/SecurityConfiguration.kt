package ch.ascendise.todolistapi

import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    override fun configure(http: HttpSecurity)
    {
        http
            .csrf{ it.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()) }
            .authorizeRequests{
                it.antMatchers("/", "/login").permitAll()
                .anyRequest().authenticated()
            }
            .exceptionHandling{
                it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.NOT_FOUND))
            }
            .oauth2Login{ oauth2 ->
                oauth2.authorizationEndpoint {it.baseUri("/login")}
            }
            .logout {
                it.invalidateHttpSession(true)
                it.clearAuthentication(true)
                it.logoutUrl("/logout")
                it.logoutSuccessUrl("/login")
            }
    }
}