//package ch.obermuhlner.langchain.assistant
//
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.stereotype.Controller
//import org.springframework.ui.Model
//import org.springframework.web.bind.annotation.*
//
//@Controller
//class AssistantController {
//
//    @Autowired
//    private lateinit var assistantService: AssistantService
//
//    // Mapping for Create Assistant Form
//    @GetMapping("/assistants/create")
//    fun createAssistantForm(model: Model): String {
//        model.addAttribute("assistant", Assistant())
//        return "create-assistant"
//    }
//
//    @PostMapping("/assistants")
//    fun createAssistant(@ModelAttribute assistant: Assistant): String {
//        assistantService.createAssistant(assistant)
//        return "redirect:/assistants"
//    }
//
//    // Mapping for List Assistants
//    @GetMapping("/assistants")
//    fun listAssistants(model: Model): String {
//        val assistants = assistantService.getAllAssistants()
//        model.addAttribute("assistants", assistants)
//        return "list-assistants"
//    }
//
//    // Mapping for Edit Assistant Form
//    @GetMapping("/assistants/{id}")
//    fun editAssistantForm(@PathVariable id: Long, model: Model): String {
//        val assistant = assistantService.getAssistantById(id)
//        model.addAttribute("assistant", assistant)
//        return "edit-assistant"
//    }
//
//    @PostMapping("/assistants/{id}")
//    fun editAssistant(@PathVariable id: Long, @ModelAttribute assistant: Assistant): String {
//        assistant.id = id
//        assistantService.updateAssistant(id, assistant)
//        return "redirect:/assistants"
//    }
//
//    // Mapping for Delete Confirmation
//    @GetMapping("/assistants/delete/{id}")
//    fun deleteAssistantForm(@PathVariable id: Long, model: Model): String {
//        val assistant = assistantService.getAssistantById(id)
//        model.addAttribute("assistant", assistant)
//        return "delete-assistant"
//    }
//
//    @DeleteMapping("/assistants/{id}")
//    fun deleteAssistant(@PathVariable id: Long): String {
//        assistantService.deleteAssistant(id)
//        return "redirect:/assistants"
//    }
//}
