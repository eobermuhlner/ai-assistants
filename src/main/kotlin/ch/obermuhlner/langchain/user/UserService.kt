package ch.obermuhlner.langchain.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserService @Autowired constructor(private val userRepository: UserRepository): UserDetailsService {

    fun createUser(user: User): User {
        return userRepository.save(user)
    }

    fun findAllUsers(): List<User> {
        return userRepository.findAll()
    }

    fun findUserById(id: Long): User? {
        val userOptional: Optional<User> = userRepository.findById(id)
        return if (userOptional.isPresent) userOptional.get() else null
    }

    fun findUserByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    fun updateUserById(id: Long, updatedUser: User): User? {
        if (userRepository.existsById(id)) {
            updatedUser.id = id
            return userRepository.save(updatedUser)
        }
        return null
    }

    fun deleteUserById(id: Long): Boolean {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
            return true
        }
        return false
    }

    override fun loadUserByUsername(username: String): UserDetails {
        val user = userRepository.findByUsername(username) ?: throw UsernameNotFoundException("User not found: $username")
        val authorities = user.authorities
            .split(",")
            .map { it.trim() }
            .map { SimpleGrantedAuthority(it) }

        return org.springframework.security.core.userdetails.User
            .withUsername(user.username)
            .password(user.password)
            .authorities(authorities)
            .build()
    }
}
