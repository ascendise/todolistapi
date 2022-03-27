package ch.ascendise.todolistapi

import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.web.csrf.CookieCsrfTokenRepository

@Configuration
class SecurityConfiguration : WebSecurityConfigurerAdapter() {

    protected override fun configure(http: HttpSecurity)
    {
        http
            .oauth2Login()
            .and().csrf().csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
            .and().authorizeRequests().anyRequest().authenticated()
    }
}