package ch.obermuhlner.langchain.ai

import dev.langchain4j.model.openai.OpenAiChatModel
import java.time.Duration

fun main() {
    val apiKey = System.getenv("OPENAI_API_KEY") ?: "demo"

    val question = """
        List the heaviest animals.
    """.trimIndent()

    val model = OpenAiChatModel.builder().apiKey(apiKey).timeout(Duration.ofSeconds(60)).build()
    val answer = model.generate(question)
    println(answer)
}