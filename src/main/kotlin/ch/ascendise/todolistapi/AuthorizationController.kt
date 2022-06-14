package ch.ascendise.todolistapi

import org.springframework.hateoas.EntityModel
import org.springframework.hateoas.Link
import org.springframework.hateoas.UriTemplate
import org.springframework.hateoas.server.mvc.linkTo
import org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder

@RestController
class AuthorizationController(
    val clientRegistrationRepository: InMemoryClientRegistrationRepository
) {

    @GetMapping("/login")
    fun login(): EntityModel<DummyResponse>{
        val baseUri = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString()
        val links = mutableListOf<Link>()
        clientRegistrationRepository.forEach {
            val link = Link.of(UriTemplate.of("$baseUri/login/${it.registrationId}"), it.registrationId)
            links.add(link)
        }
        links.add(linkTo<AuthorizationController> { login() }.withSelfRel())
        return EntityModel.of(DummyResponse(), links)
    }

    class DummyResponse
}

