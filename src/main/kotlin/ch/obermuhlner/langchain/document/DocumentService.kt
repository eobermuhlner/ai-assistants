package ch.obermuhlner.langchain.document

import ch.obermuhlner.langchain.assistant.Assistant
import dev.langchain4j.data.document.DocumentType
import dev.langchain4j.data.document.UrlDocumentLoader
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import java.io.InputStream
import java.nio.ByteBuffer
import java.util.Base64

@Service
class DocumentService(
    @Autowired private val documentRepository: DocumentRepository,
    @Autowired private val documentSegmentRepository: DocumentSegmentRepository
) {

    val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()

    fun createDocument(document: Document): Document {
        return documentRepository.save(document)
    }

    fun createDocument(assistant: Assistant?, name: String, inputStream: InputStream) {
        val documentType = DocumentType.of(name)
        val parser = toDocumentParser(documentType)
        val langchainDocument = parser.parse(inputStream)
        langchainDocument.metadata().add("name", name)

        createDocument(assistant, langchainDocument)
    }

    fun createDocumentUrl(assistant: Assistant?, url: String) {
        val langchainDocument: dev.langchain4j.data.document.Document = UrlDocumentLoader.load(url)
        langchainDocument.metadata().add("name", url)

        createDocument(assistant, langchainDocument)
    }

    private fun createDocument(assistant: Assistant?, langchainDocument: dev.langchain4j.data.document.Document) {
        val name = langchainDocument.metadata("name") ?: langchainDocument.metadata("file") ?: langchainDocument.metadata("url") ?: "Untitled"
        val metadata = toJson(langchainDocument.metadata().asMap())
        val document = Document(name = name, assistant = assistant, metadata = metadata)
        createDocumentSegments(document, langchainDocument)
        documentRepository.save(document)
    }

    private fun createDocumentSegments(document: Document, langchainDocument: dev.langchain4j.data.document.Document) {
        val documentSplitter = DocumentSplitters.recursive(500, 0)
        val textSegments = documentSplitter.split(langchainDocument)
        document.segments.addAll(textSegments.map {
            val embeddingResponse = embeddingModel.embed(it.text())
            val embeddingString = floatArrayToBase64(embeddingResponse.content().vector())
            val metadata = toJson(it.metadata().asMap())
            DocumentSegment(embedding = embeddingString, text = it.text(), document = document, metadata = metadata)
        })
    }

    fun getDocumentById(id: Long): Document? {
        return documentRepository.findById(id).orElse(null)
    }

    fun updateDocument(id: Long, updatedDocument: Document): Document? {
        val existingDocument = documentRepository.findById(id)
        if (existingDocument.isPresent) {
            val document = existingDocument.get()
            document.name = updatedDocument.name
            document.title = updatedDocument.title
            document.description = updatedDocument.description
            document.metadata = updatedDocument.metadata

            document.segments.clear()
            document.segments.addAll(updatedDocument.segments)
            return documentRepository.save(document)
        }
        return null
    }

    fun deleteDocument(id: Long): Boolean {
        if (documentRepository.existsById(id)) {
            documentRepository.deleteById(id)
            return true
        }
        return false
    }

    fun findAllDocuments(): List<Document> {
        return documentRepository.findAll()
    }
}