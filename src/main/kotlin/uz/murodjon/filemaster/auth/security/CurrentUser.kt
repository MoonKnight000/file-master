package uz.murodjon.filemaster.auth.security

/** Inject the authenticated user into a controller method: `@CurrentUser user: User`. */
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class CurrentUser
