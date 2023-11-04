package ch.obermuhlner.langchain.assistant

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class AssistantService(
    @Autowired private val assistantRepository: AssistantRepository
) {

    // Create a new Assistant
    fun createAssistant(assistant: Assistant): Assistant {
        return assistantRepository.save(assistant)
    }

    // Get an Assistant by ID
    fun getAssistantById(id: Long): Assistant? {
        return assistantRepository.findById(id).orElse(null)
    }

    // Update an existing Assistant
    fun updateAssistant(id: Long, updatedAssistant: Assistant): Assistant? {
        val existingAssistant = assistantRepository.findById(id)
        if (existingAssistant.isPresent) {
            val assistant = existingAssistant.get()
            assistant.name = updatedAssistant.name
            assistant.description = updatedAssistant.description
            return assistantRepository.save(assistant)
        }
        return null
    }

    // Delete an Assistant by ID
    fun deleteAssistant(id: Long): Boolean {
        if (assistantRepository.existsById(id)) {
            assistantRepository.deleteById(id)
            return true
        }
        return false
    }

    fun findAllAssistants(): List<Assistant> {
        return assistantRepository.findAll()
    }
}
