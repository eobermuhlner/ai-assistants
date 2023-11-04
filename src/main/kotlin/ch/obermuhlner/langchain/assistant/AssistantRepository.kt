package ch.obermuhlner.langchain.assistant

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface AssistantRepository : JpaRepository<Assistant, Long> {
    // You can add custom queries or methods here if needed
}
