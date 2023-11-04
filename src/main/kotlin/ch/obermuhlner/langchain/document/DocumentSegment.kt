package ch.obermuhlner.langchain.document

import ch.obermuhlner.langchain.assistant.Assistant
import ch.obermuhlner.langchain.user.User
import com.fasterxml.jackson.annotation.JsonIgnore
import jakarta.persistence.*

@Entity
@Table(name = "document_segments")
class DocumentSegment(
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        var id: Long? = null,

        @Column(name = "embedding", length = 10240)
        var embedding: String = "",

        @Column(name = "text", length = 10240)
        var text: String = "",

        @Column(name = "metadata", length = 10240)
        var metadata: String = "",

        @JsonIgnore
        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "document_id")
        var document: Document? = null,
) {
        override fun toString() = "DocumentSegment{$id}"
}