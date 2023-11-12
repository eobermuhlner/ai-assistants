package ch.obermuhlner.langchain.security

import ch.obermuhlner.langchain.chat.Chat
import ch.obermuhlner.langchain.chat.ChatService
import ch.obermuhlner.langchain.user.User
import ch.obermuhlner.langchain.user.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.security.Principal

@Service
class PrincipalService @Autowired constructor(
    private val userService: UserService,
    private val chatService: ChatService
) {
    private fun getUser(principal: Principal): User {
        return userService.findUserByUsername(principal.name)!!
    }

    fun getChat(principal: Principal, chatId: Long): Chat {
        val user = getUser(principal)
        val chat = chatService.getChat(chatId)
        if (chat.user!!.id != user.id) {
            throw RuntimeException("Chat not found")
        }
        return chat
    }
}