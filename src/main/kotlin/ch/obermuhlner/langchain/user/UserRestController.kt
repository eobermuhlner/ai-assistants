package ch.obermuhlner.langchain.user

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserRestController @Autowired constructor(private val userService: UserService) {

    // Create a new user
    @PostMapping
    fun createUser(@RequestBody user: User): ResponseEntity<User> {
        val createdUser = userService.createUser(user)
        return ResponseEntity(createdUser, HttpStatus.CREATED)
    }

    // Retrieve all users
    @GetMapping
    fun findAllUsers(): ResponseEntity<List<User>> {
        val users = userService.findAllUsers()
        return ResponseEntity(users, HttpStatus.OK)
    }

    // Retrieve a user by ID
    @GetMapping("/{id}")
    fun findUserById(@PathVariable id: Long): ResponseEntity<User?> {
        val user = userService.findUserById(id)
        return if (user != null) {
            ResponseEntity(user, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    // Update a user by ID
    @PutMapping("/{id}")
    fun updateUserById(@PathVariable id: Long, @RequestBody updatedUser: User): ResponseEntity<User?> {
        val user = userService.updateUserById(id, updatedUser)
        return if (user != null) {
            ResponseEntity(user, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    // Delete a user by ID
    @DeleteMapping("/{id}")
    fun deleteUserById(@PathVariable id: Long): ResponseEntity<Unit> {
        val deleted = userService.deleteUserById(id)
        return if (deleted) {
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
