package ch.obermuhlner.langchain.assistant

import ch.obermuhlner.langchain.assistant.Assistant
import ch.obermuhlner.langchain.assistant.AssistantService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/assistants")
class AssistantRestController(
    @Autowired private val assistantService: AssistantService
) {

    // Create a new Assistant
    @PostMapping
    fun createAssistant(@RequestBody assistant: Assistant): ResponseEntity<Assistant> {
        val createdAssistant = assistantService.createAssistant(assistant)
        return ResponseEntity(createdAssistant, HttpStatus.CREATED)
    }

    @GetMapping
    fun findAllAssistants(): ResponseEntity<List<Assistant>> {
        val assistants = assistantService.findAllAssistants()
        return ResponseEntity(assistants, HttpStatus.OK)
    }

    @GetMapping("/{id}")
    fun getAssistantById(@PathVariable id: Long): ResponseEntity<Assistant> {
        val assistant = assistantService.getAssistantById(id)
        return if (assistant != null) {
            ResponseEntity(assistant, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @PutMapping("/{id}")
    fun updateAssistant(
            @PathVariable id: Long,
            @RequestBody updatedAssistant: Assistant
    ): ResponseEntity<Assistant> {
        val assistant = assistantService.updateAssistant(id, updatedAssistant)
        return if (assistant != null) {
            ResponseEntity(assistant, HttpStatus.OK)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }

    @DeleteMapping("/{id}")
    fun deleteAssistant(@PathVariable id: Long): ResponseEntity<Void> {
        val deleted = assistantService.deleteAssistant(id)
        return if (deleted) {
            ResponseEntity(HttpStatus.NO_CONTENT)
        } else {
            ResponseEntity(HttpStatus.NOT_FOUND)
        }
    }
}
