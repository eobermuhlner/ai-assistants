package ch.obermuhlner.langchain.document

import com.fasterxml.jackson.databind.ObjectMapper
import dev.langchain4j.data.document.DocumentParser
import dev.langchain4j.data.document.DocumentType
import dev.langchain4j.data.document.parser.MsOfficeDocumentParser
import dev.langchain4j.data.document.parser.PdfDocumentParser
import dev.langchain4j.data.document.parser.TextDocumentParser
import com.fasterxml.jackson.module.kotlin.readValue

fun toDocumentParser(type: DocumentType?): DocumentParser {
    return when (type) {
        DocumentType.TXT, DocumentType.HTML, DocumentType.UNKNOWN -> TextDocumentParser(type)
        DocumentType.PDF -> PdfDocumentParser()
        DocumentType.DOC, DocumentType.XLS, DocumentType.PPT -> MsOfficeDocumentParser(type)
        else -> throw RuntimeException(String.format("Unknown document type $type"))
    }
}

fun toJson(any: Any): String {
    val objectMapper = ObjectMapper()
    return objectMapper.writeValueAsString(any)
}

fun jsonToMap(jsonString: String): Map<String, String> {
    val objectMapper = ObjectMapper()
    return objectMapper.readValue(jsonString)
}
