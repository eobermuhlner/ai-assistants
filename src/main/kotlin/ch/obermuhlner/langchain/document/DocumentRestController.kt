package ch.obermuhlner.langchain.document

import ch.obermuhlner.langchain.assistant.AssistantService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.IOException

@RestController
@RequestMapping("/api/documents")
class DocumentRestController(
    @Autowired private val documentService: DocumentService,
    @Autowired private val assistantService: AssistantService
) {

    @PostMapping("/assistant/{assistantId}/upload")
    fun uploadFile(@PathVariable assistantId: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        try {
            val assistant = assistantService.getAssistantById(assistantId)
            val name = file.originalFilename ?: "Untitled"
            documentService.createDocument(assistant, name, ByteArrayInputStream(file.bytes))

            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully.")
        } catch (e: IOException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the file.")
        }
    }

    @GetMapping
    fun findAllDocuments(): ResponseEntity<List<Document>> {
        val documents = documentService.findAllDocuments()
        return ResponseEntity(documents, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getDocumentById(@PathVariable id: Long): ResponseEntity<Document> {
        val document = documentService.getDocumentById(id)
        return if (document != null) {
            ResponseEntity(document, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PutMapping("/{id}")
    fun updateDocument(
        @PathVariable id: Long,
        @RequestBody updatedDocument: Document
    ): ResponseEntity<Document> {
        val document = documentService.updateDocument(id, updatedDocument)
        return if (document != null) {
            ResponseEntity(document, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteDocument(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = documentService.deleteDocument(id)
        return if (deleted) {
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
