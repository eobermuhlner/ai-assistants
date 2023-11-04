package ch.obermuhlner.langchain.chat

import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "messages")
class Message(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "content", length = 10240)
        var content: String = "",

        @JsonIgnore
        @ManyToOne
        @JoinColumn(name = "chat_id")
        var chat: Chat? = null,
) {
        override fun toString() = "Message{$id}"
}
