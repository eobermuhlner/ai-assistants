package ch.obermuhlner.langchain.chat

import ch.obermuhlner.langchain.assistant.Assistant
import ch.obermuhlner.langchain.user.User
import jakarta.persistence.*

@Entity
@Table(name = "chats")
class Chat(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "name")
        var name: String = "",

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "user_id")
        var user: User? = null,

        @ManyToOne(fetch = FetchType.EAGER)
        @JoinColumn(name = "assistant_id")
        var assistant: Assistant? = null,

        @OneToMany(mappedBy = "chat", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
        val messages: MutableList<Message> = mutableListOf(),
) {
        override fun toString() = "Chat{$id}"
}