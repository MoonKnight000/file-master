package uz.murodjon.filemaster.auth.service.impl

import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import uz.murodjon.filemaster.auth.dto.CategoryUsage
import uz.murodjon.filemaster.auth.dto.ChangePasswordRequest
import uz.murodjon.filemaster.auth.dto.GoogleSignInRequest
import uz.murodjon.filemaster.auth.dto.IssuedSession
import uz.murodjon.filemaster.auth.dto.LoginRequest
import uz.murodjon.filemaster.auth.dto.UserLimits
import uz.murodjon.filemaster.auth.dto.UserResponse
import uz.murodjon.filemaster.auth.dto.UserUsage
import uz.murodjon.filemaster.files.dto.FileDto
import uz.murodjon.filemaster.auth.dto.RegisterRequest
import uz.murodjon.filemaster.auth.dto.UpdateProfileRequest
import uz.murodjon.filemaster.auth.model.Session
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.auth.repository.SessionRepository
import uz.murodjon.filemaster.auth.repository.UserRepository
import uz.murodjon.filemaster.auth.service.AuthService
import uz.murodjon.filemaster.auth.service.GoogleTokenVerifier
import uz.murodjon.filemaster.common.Ids
import uz.murodjon.filemaster.config.AppProperties
import uz.murodjon.filemaster.auth.enums.UserPlan
import uz.murodjon.filemaster.conversion.repository.ConversionJobRepository
import uz.murodjon.filemaster.exception.AvatarFileNotFoundException
import uz.murodjon.filemaster.exception.UnauthenticatedException
import uz.murodjon.filemaster.exception.ValidationException
import uz.murodjon.filemaster.files.repository.StoredFileRepository
import uz.murodjon.filemaster.storage.StorageService
import java.time.Instant

@Service
class AuthServiceImpl(
    private val users: UserRepository,
    private val sessions: SessionRepository,
    private val passwordEncoder: PasswordEncoder,
    private val googleVerifier: GoogleTokenVerifier,
    private val files: StoredFileRepository,
    private val jobs: ConversionJobRepository,
    private val storage: StorageService,
    private val props: AppProperties,
) : AuthService {

    private val sessionTtlSeconds = 24 * 60 * 60L

    @Transactional
    override fun createGuestSession(): IssuedSession {
        val user = users.save(User(guest = true, plan = UserPlan.FREE))
        return issue(user)
    }

    @Transactional
    override fun register(request: RegisterRequest): IssuedSession {
        val email = request.email?.trim()?.lowercase()
            ?.takeIf { it.contains("@") && it.length >= 3 }
            ?: throw ValidationException("A valid email is required.")
        val password = request.password?.takeIf { it.length >= 6 }
            ?: throw ValidationException("Password must be at least 6 characters.")

        users.findByEmailIgnoreCaseAndActiveTrue(email).ifPresent {
            throw ValidationException("Email already in use.")
        }

        // If the caller is browsing as a guest, upgrade that guest in place (keeps their files).
        val current = currentPrincipal()
        val user = (current?.takeIf { it.guest } ?: User()).apply {
            guest = false
            this.email = email
            name = request.name?.trim()?.takeIf { it.isNotEmpty() } ?: name
            passwordHash = passwordEncoder.encode(password)
            updatedTimestamp = Instant.now().epochSecond
        }
        return issue(users.save(user))
    }

    @Transactional
    override fun login(request: LoginRequest): IssuedSession {
        val email = request.email?.trim()?.lowercase()
            ?: throw UnauthenticatedException("Invalid email or password.")
        val password = request.password ?: throw UnauthenticatedException("Invalid email or password.")

        val user = users.findByEmailIgnoreCaseAndActiveTrue(email).orElse(null)
            ?: throw UnauthenticatedException("Invalid email or password.")
        val hash = user.passwordHash
        if (hash == null || !passwordEncoder.matches(password, hash)) {
            throw UnauthenticatedException("Invalid email or password.")
        }
        return issue(user)
    }

    @Transactional
    override fun loginWithGoogle(request: GoogleSignInRequest): IssuedSession {
        val idToken = request.idToken?.trim()?.takeIf { it.isNotEmpty() }
            ?: throw ValidationException("`idToken` is required.")
        val account = googleVerifier.verify(idToken)

        // Link by Google id first, then fall back to a matching email; otherwise upgrade the
        // current guest in place (keeps their files) or create a fresh account.
        val existing = users.findByGoogleIdAndActiveTrue(account.googleId).orElse(null)
            ?: users.findByEmailIgnoreCaseAndActiveTrue(account.email).orElse(null)

        val user = (existing ?: currentPrincipal()?.takeIf { it.guest } ?: User()).apply {
            guest = false
            googleId = account.googleId
            email = account.email
            if (name.isNullOrBlank()) name = account.name
            updatedTimestamp = Instant.now().epochSecond
        }
        return issue(users.save(user))
    }

    @Transactional
    override fun logout(token: String?) {
        val raw = token?.removePrefix("Bearer ")?.trim()?.takeIf { it.isNotEmpty() } ?: return
        sessions.findByToken(raw).ifPresent { sessions.delete(it) }
    }

    @Transactional(readOnly = true)
    override fun requireUser(bearer: String?): User {
        val token = bearer?.removePrefix("Bearer ")?.trim()
            ?.takeIf { it.isNotEmpty() }
            ?: throw UnauthenticatedException()
        val session = sessions.findByToken(token).orElse(null)
            ?: throw UnauthenticatedException()
        if (session.expiresTimestamp < Instant.now().epochSecond) {
            throw UnauthenticatedException("Session expired.")
        }
        return session.user
    }

    @Transactional(readOnly = true)
    override fun me(user: User): UserResponse = user.toMeResponse()

    @Transactional
    override fun updateProfile(user: User, request: UpdateProfileRequest): UserResponse {
        request.name?.trim()?.let { name ->
            if (name.isEmpty()) throw ValidationException("Name must not be blank.")
            if (name.length < 2) throw ValidationException("Name must be at least 2 characters.")
            if (name.length > 100) throw ValidationException("Name must be at most 100 characters.")
            user.name = name
        }
        request.avatarId?.let { avatarId ->
            val file = files.findByIdAndUserIdAndActiveTrue(avatarId, user.id!!)
                .orElseThrow { AvatarFileNotFoundException("Avatar file not found or does not belong to you.") }
            user.avatar = file
        }
        user.updatedTimestamp = Instant.now().epochSecond
        return users.save(user).toMeResponse()
    }

    @Transactional
    override fun changePassword(user: User, request: ChangePasswordRequest) {
        val hash = user.passwordHash
            ?: throw ValidationException("This account has no password to change.")
        val current = request.currentPassword
            ?: throw ValidationException("Current password is required.")
        if (!passwordEncoder.matches(current, hash)) {
            throw ValidationException("Current password is incorrect.")
        }
        val newPassword = request.newPassword?.takeIf { it.length >= 6 }
            ?: throw ValidationException("New password must be at least 6 characters.")

        user.passwordHash = passwordEncoder.encode(newPassword)
        user.updatedTimestamp = Instant.now().epochSecond
        users.save(user)
    }

    @Transactional
    override fun deleteAccount(user: User) {
        val userId = user.id!!
        val now = Instant.now().epochSecond

        // Purge the user's files: drop the stored objects, then soft-delete the rows.
        val owned = files.findByUserIdAndActiveTrue(userId)
        owned.forEach { runCatching { storage.delete(it.absolutePath) } }
        owned.forEach {
            it.active = false
            it.deletedTimestamp = now
        }
        files.saveAll(owned)

        // End every session so the closed account can't keep making requests.
        sessions.deleteByUserId(userId)

        // Soft-delete the account itself and release its unique email/googleId for reuse.
        user.active = false
        user.deletedTimestamp = now
        user.email = null
        user.googleId = null
        user.passwordHash = null
        user.updatedTimestamp = now
        users.save(user)
    }

    private fun User.toMeResponse(): UserResponse {
        val userId = id!!
        val pro = plan == UserPlan.PREMIUM
        val retentionMinutes = props.limits.retentionMinutes
        val byCategory = files.countActiveByCategory(userId)
            .map { CategoryUsage(it.category, it.count) }
            .sortedBy { it.category.token }
        val avatar = avatar?.let { FileDto(it, retentionMinutes) }
        return UserResponse(
            id = userId,
            guest = guest,
            name = name,
            email = email,
            avatar = avatar,
            plan = plan,
            limits = UserLimits(
                maxFileBytes = props.limits.maxFileBytes,
                batchConvert = pro || props.limits.freeBatchConvert,
                retentionMinutes = retentionMinutes,
            ),
            usage = UserUsage(
                fileCount = files.countByUserIdAndActiveTrue(userId),
                storageBytes = files.sumActiveBytes(userId),
                conversionCount = jobs.countByUserId(userId),
                byCategory = byCategory,
            ),
        )
    }

    private fun issue(user: User): IssuedSession {
        val expiresTimestamp = Instant.now().epochSecond + sessionTtlSeconds
        val token = Ids.token()
        sessions.save(Session(token = token, user = user, expiresTimestamp = expiresTimestamp))
        return IssuedSession(token, expiresTimestamp, user)
    }

    private fun currentPrincipal(): User? =
        SecurityContextHolder.getContext().authentication?.principal as? User
}
