package ch.obermuhlner.langchain.document

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface DocumentSegmentRepository : JpaRepository<DocumentSegment, Long>