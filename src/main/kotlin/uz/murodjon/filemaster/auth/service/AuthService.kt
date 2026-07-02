package uz.murodjon.filemaster.auth.service

import uz.murodjon.filemaster.auth.dto.ChangePasswordRequest
import uz.murodjon.filemaster.auth.dto.GoogleSignInRequest
import uz.murodjon.filemaster.auth.dto.IssuedSession
import uz.murodjon.filemaster.auth.dto.LoginRequest
import uz.murodjon.filemaster.auth.dto.UserResponse
import uz.murodjon.filemaster.auth.dto.RegisterRequest
import uz.murodjon.filemaster.auth.dto.UpdateProfileRequest
import uz.murodjon.filemaster.auth.model.User
import uz.murodjon.filemaster.exception.UnauthenticatedException

interface AuthService {
    fun createGuestSession(): IssuedSession

    /** Creates a real account. If the caller is currently a guest, that guest is upgraded
     *  in place (keeping its files); otherwise a new user is created. */
    fun register(request: RegisterRequest): IssuedSession

    fun login(request: LoginRequest): IssuedSession

    /** Signs in with a Google ID token: links/creates the account by Google identity, upgrading
     *  the current guest in place when there is one, then issues a session. */
    fun loginWithGoogle(request: GoogleSignInRequest): IssuedSession

    /** Ends the session behind [token] (no-op if it doesn't exist). */
    fun logout(token: String?)

    /** Resolves the user behind a bearer token, or throws [UnauthenticatedException]. */
    fun requireUser(bearer: String?): User

    /** Current account, plan/limits and usage snapshot (backs `GET /v1/me`). */
    fun me(user: User): UserResponse

    /** Updates the user's profile (name/avatar) and returns the refreshed account view. */
    fun updateProfile(user: User, request: UpdateProfileRequest): UserResponse

    /** Changes the password of an email account after verifying the current one. */
    fun changePassword(user: User, request: ChangePasswordRequest)

    /** Permanently closes the account: purges files, ends all sessions, soft-deletes the user. */
    fun deleteAccount(user: User)
}
