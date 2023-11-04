package ch.obermuhlner.langchain.document

import ch.obermuhlner.langchain.assistant.Assistant
import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.document.DocumentType
import dev.langchain4j.data.document.parser.MsOfficeDocumentParser
import dev.langchain4j.data.document.parser.PdfDocumentParser
import dev.langchain4j.data.document.parser.TextDocumentParser
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile
import java.io.ByteArrayInputStream
import java.io.IOException

@RestController
@RequestMapping("/api/document-segments")
class DocumentSegmentRestController(
    @Autowired private val documentSegmentService: DocumentSegmentService
) {

    @GetMapping
    fun findAllDocumentSegments(): ResponseEntity<List<DocumentSegment>> {
        val documentSegments = documentSegmentService.findAllDocumentSegments()
        return ResponseEntity(documentSegments, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getDocumentSegmentById(@PathVariable id: Long): ResponseEntity<DocumentSegment> {
        val documentSegment = documentSegmentService.getDocumentSegmentById(id)
        return if (documentSegment != null) {
            ResponseEntity(documentSegment, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteDocumentSegment(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = documentSegmentService.deleteDocumentSegment(id)
        return if (deleted) {
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
