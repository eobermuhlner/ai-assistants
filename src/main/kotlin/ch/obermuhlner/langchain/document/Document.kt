package ch.obermuhlner.langchain.document

import ch.obermuhlner.langchain.assistant.Assistant
import ch.obermuhlner.langchain.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "documents")
class Document(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "name")
        var name: String = "",

        @Column(name = "title")
        var title: String = "",

        @Column(name = "description", length = 1024)
        var description: String = "",

        @Column(name = "metadata", length = 10240)
        var metadata: String = "",

        @JsonIgnore
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "assistant_id")
        var assistant: Assistant? = null,

        @OneToMany(mappedBy = "document", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
        val segments: MutableList<DocumentSegment> = mutableListOf(),
) {
        override fun toString() = "Document{$id}"
}