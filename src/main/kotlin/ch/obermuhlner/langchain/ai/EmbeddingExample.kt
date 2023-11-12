package ch.obermuhlner.langchain.ai

import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.openai.OpenAiEmbeddingModel
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore


class EmbeddingOracle {
    private val embeddingStore = InMemoryEmbeddingStore<TextSegment>()
    //private val embeddingModel = AllMiniLmL6V2EmbeddingModel()
    private val embeddingModel = OpenAiEmbeddingModel.withApiKey("demo")

    fun add(text: String) {
        val textSegment = TextSegment.from(text)
        val embeddingResponse = embeddingModel.embed(textSegment)
        embeddingStore.add(embeddingResponse.content(), textSegment)
    }

    fun find(text: String, maxResults: Int = 1): List<EmbeddingMatch<TextSegment>> {
        return embeddingStore.findRelevant(embeddingModel.embed(TextSegment.from(text)).content(), maxResults)
    }
}

fun main() {
    val embeddingOracle = EmbeddingOracle()

    embeddingOracle.add("blue")
    embeddingOracle.add("white")
    embeddingOracle.add("green")
    embeddingOracle.add("red")
    embeddingOracle.add("black")
    embeddingOracle.add("yellow")
    embeddingOracle.add("gray")

    for (query in listOf("sky", "grass", "blood", "sky at night", "fog", "snow")) {
        val matches = embeddingOracle.find(query, 99)
        println("QUERY: $query")
        for (match in matches) {
            println("${match.score()} ${match.embedded()} ")
        }
        println()
    }

}