package ch.obermuhlner.langchain.assistant

import ch.obermuhlner.langchain.document.Document
import jakarta.persistence.*

@Entity
@Table(name = "assistants")
class Assistant(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name")
    var name: String = "",

    @Column(name = "description", length = 10240)
    var description: String = "",

    @Column(name = "force_use_documents")
    var forceUseDocuments: Boolean = false,

    @Column(name = "tools", length = 1024)
    var tools: String = "",

    @OneToMany(mappedBy = "assistant", cascade = [CascadeType.ALL], fetch = FetchType.EAGER)
    val documents: MutableList<Document> = mutableListOf(),
    ) {
    override fun toString() = "Assistant{$id}"
}