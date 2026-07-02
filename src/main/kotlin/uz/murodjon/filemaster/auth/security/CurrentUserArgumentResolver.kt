package uz.murodjon.filemaster.auth.security

import org.springframework.core.MethodParameter
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.bind.support.WebDataBinderFactory
import org.springframework.web.context.request.NativeWebRequest
import org.springframework.web.method.support.HandlerMethodArgumentResolver
import org.springframework.web.method.support.ModelAndViewContainer
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.exception.UnauthenticatedException

/**
 * Injects the authenticated [User] (set by [uz.murodjon.filemaster.security.TokenAuthenticationFilter] in the security
 * context) into `@CurrentUser user: User` controller parameters.
 */
@Component
class CurrentUserArgumentResolver : HandlerMethodArgumentResolver {

    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(CurrentUser::class.java) &&
            parameter.parameterType == User::class.java

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Any {
        val principal = SecurityContextHolder.getContext().authentication?.principal
        return principal as? User ?: throw UnauthenticatedException()
    }
}
