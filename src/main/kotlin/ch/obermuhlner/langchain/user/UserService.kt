package ch.obermuhlner.langchain.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.util.Optional

@Service
class UserService @Autowired constructor(private val userRepository: UserRepository) {

    // Create a new user
    fun createUser(user: User): User {
        return userRepository.save(user)
    }

    // Retrieve all users
    fun findAllUsers(): List<User> {
        return userRepository.findAll()
    }

    // Retrieve a user by ID
    fun findUserById(id: Long): User? {
        val userOptional: Optional<User> = userRepository.findById(id)
        return if (userOptional.isPresent) userOptional.get() else null
    }

    // Update a user by ID
    fun updateUserById(id: Long, updatedUser: User): User? {
        if (userRepository.existsById(id)) {
            updatedUser.id = id
            return userRepository.save(updatedUser)
        }
        return null
    }

    // Delete a user by ID
    fun deleteUserById(id: Long): Boolean {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id)
            return true
        }
        return false
    }
}
