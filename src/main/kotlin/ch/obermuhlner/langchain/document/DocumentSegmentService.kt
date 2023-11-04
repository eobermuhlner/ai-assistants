package ch.obermuhlner.langchain.document

import ch.obermuhlner.langchain.assistant.Assistant
import dev.langchain4j.data.document.DocumentType
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream

@Service
class DocumentSegmentService(
    @Autowired private val documentSegmentRepository: DocumentSegmentRepository
) {

    fun getDocumentSegmentById(id: Long): DocumentSegment? {
        return documentSegmentRepository.findById(id).orElse(null)
    }

    fun deleteDocumentSegment(id: Long): Boolean {
        if (documentSegmentRepository.existsById(id)) {
            documentSegmentRepository.deleteById(id)
            return true
        }
        return false
    }

    fun findAllDocumentSegments(): List<DocumentSegment> {
        return documentSegmentRepository.findAll()
    }

}