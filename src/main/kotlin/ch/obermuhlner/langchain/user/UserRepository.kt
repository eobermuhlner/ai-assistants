package ch.obermuhlner.langchain.user

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UserRepository : JpaRepository<User, Long> {
    // You can add custom queries or methods here if needed
}
