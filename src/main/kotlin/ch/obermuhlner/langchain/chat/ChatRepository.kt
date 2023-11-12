package ch.obermuhlner.langchain.chat

import ch.obermuhlner.langchain.user.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ChatRepository : JpaRepository<Chat, Long> {

    fun findByUser(user: User): List<Chat>
}