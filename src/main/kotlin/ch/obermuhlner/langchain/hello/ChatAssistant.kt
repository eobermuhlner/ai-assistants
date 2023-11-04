package ch.obermuhlner.langchain.hello

import dev.langchain4j.agent.tool.Tool
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.FileSystemDocumentLoader.loadDocument
import dev.langchain4j.data.document.UrlDocumentLoader
import dev.langchain4j.data.document.splitter.DocumentSplitters
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.memory.chat.MessageWindowChatMemory
import dev.langchain4j.model.embedding.AllMiniLmL6V2EmbeddingModel
import dev.langchain4j.model.embedding.EmbeddingModel
import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.openai.OpenAiStreamingChatModel
import dev.langchain4j.retriever.EmbeddingStoreRetriever
import dev.langchain4j.service.AiServices
import dev.langchain4j.service.TokenStream
import dev.langchain4j.store.embedding.EmbeddingMatch
import dev.langchain4j.store.embedding.EmbeddingStore
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore
import io.github.cosinekitty.astronomy.Time
import io.github.cosinekitty.astronomy.moonPhase
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.nio.file.Path
import java.time.*


interface Assistant {
    fun chat(message: String): String
}

interface StreamingAssistant {
    fun chatStreaming(message: String): TokenStream
}


class Calendar {
//    @Tool("The current date and time")
//    fun currentDateTime(): LocalDateTime {
//        return LocalDateTime.now()
//    }
//
    @Tool("The current date")
    fun currentDate(dummy: String): LocalDate {
        return LocalDate.now()
    }
//
//    @Tool("The day of the week of a given date")
//    fun dayOfWeek(date: LocalDate): String {
//        return date.dayOfWeek.toString()
//    }
//
//    @Tool("The date of today plus days")
//    fun currentDateTimePlusDays(plusDays: Long): LocalDate {
//        return LocalDate.now().plusDays(plusDays)
//    }

    @Tool("Calendar around today showing the days of the week")
    fun calendar(previousDays: Long = 3, nextDays: Long = 40): String {
        val today = LocalDate.now()
        val startDate = today.minusDays(previousDays)
        val endDate = today.plusDays(nextDays)
        val result = StringBuilder()
        var date = startDate
        while(date.isBefore(endDate)) {
            val comment = if (date.equals(today)) "today" else ""
            result.append("$date ${date.dayOfWeek} $comment\n")
            date = date.plusDays(1)
        }
        return result.toString()
    }
}

class Astronomy {
    @Tool("The current phase of the moon in degrees (0 = new moon, 90 = first quarter, 180=full moon, 270=third quarter)")
    fun moonPhaseNowInDegrees(dummy: String): Double {
        return moonPhase(now())
    }

    @Tool("The phase of the moon in degrees (0 = new moon, 90 = first quarter, 180=full moon, 270=third quarter) days from today")
    fun moonPhaseAtDateInDegrees(days: Long): Double {
        val date = LocalDate.now().plusDays(days)
        return moonPhase(Time(
                date.year,
                date.monthValue,
                date.dayOfMonth,
                0,
                0,
                0.0
        ))
    }

//    @Tool("The phase of the moon in degrees (0 = new moon, 90 = first quarter, 180=full moon, 270=third quarter) at a specific date")
//    fun moonPhaseAtDateInDegrees(date: LocalDate): Double {
//        return moonPhase(Time(
//                date.year,
//                date.monthValue,
//                date.dayOfMonth,
//                0,
//                0,
//                0.0
//        ))
//    }

    private fun now(): Time {
        val nowInstant = Instant.now()
        val nowZonedDateTime = nowInstant.atZone(ZoneOffset.UTC)

        return Time(
                nowZonedDateTime.year,
                nowZonedDateTime.monthValue,
                nowZonedDateTime.dayOfMonth,
                nowZonedDateTime.hour,
                nowZonedDateTime.minute,
                nowZonedDateTime.second + nowZonedDateTime.nano / 1000000000.0
        )
    }
}

class Weather {
    val client = OkHttpClient()

    @Tool("The hourly weather forecast")
    private fun weatherForecastHourly(latitude: Double, longitude: Double, days: Int): String {
        if (latitude == 0.0 && longitude == 0.0) {
            return "Unknown location"
        }

        val url = HttpUrl.Builder()
                .scheme("https")
                .host("api.open-meteo.com")
                .addPathSegment("v1")
                .addPathSegment("forecast")
                .addQueryParameter("latitude", latitude.toString())
                .addQueryParameter("longitude", longitude.toString())
                .addQueryParameter("timezone", "auto")
                .addQueryParameter("hourly", "temperature_2m,precipitation_probability,rain,showers,snowfall,snow_depth,weathercode,cloudcover,visibility")
                .addQueryParameter("forecast_days", "$days")
                .build()

        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            //println("Weather Response: $responseData")
            return responseData ?: "No response"
        } else {
            return "Request failed: ${response.code}"
        }
    }

    @Tool("The hourly astrophotography forecast")
    private fun astrophotographyForecastHourly(latitude: Double, longitude: Double, days: Int): String {
        if (latitude == 0.0 && longitude == 0.0) {
            return "Unknown location"
        }

        val url = HttpUrl.Builder()
                .scheme("https")
                .host("api.open-meteo.com")
                .addPathSegment("v1")
                .addPathSegment("forecast")
                .addQueryParameter("latitude", latitude.toString())
                .addQueryParameter("longitude", longitude.toString())
                .addQueryParameter("timezone", "auto")
                .addQueryParameter("hourly", "cloudcover,visibility,precipitation_probability,temperature_2m")
                .addQueryParameter("forecast_days", "$days")
                .build()

        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            //println("Weather Response: $responseData")
            return responseData ?: "No response"
        } else {
            return "Request failed: ${response.code}"
        }
    }

    @Tool("The daily weather forecast")
    private fun weatherForecastDaily(latitude: Double, longitude: Double, days: Int): String {
        if (latitude == 0.0 && longitude == 0.0) {
            return "Unknown location"
        }

        val url = HttpUrl.Builder()
                .scheme("https")
                .host("api.open-meteo.com")
                .addPathSegment("v1")
                .addPathSegment("forecast")
                .addQueryParameter("latitude", latitude.toString())
                .addQueryParameter("longitude", longitude.toString())
                .addQueryParameter("timezone", "auto")
                .addQueryParameter("daily", "temperature_2m_min,temperature_2m_max,precipitation_sum,precipitation_hours,precipitation_probability_max,precipitation_probability_min,precipitation_probability_mean,weathercode")
                .addQueryParameter("forecast_days", "$days")
                .build()

        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            //println("Weather Response: $responseData")
            return responseData ?: "No response"
        } else {
            return "Request failed: ${response.code}"
        }
    }
}

class WebPage {
    val client = OkHttpClient()

    @Tool("The webpage")
    fun webpage(url: String): String {
        val request = Request.Builder()
                .url(url)
                .build()

        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseData = response.body?.string()
            return responseData ?: "No response"
        } else {
            return "Request failed: ${response.code}"
        }
    }
}

class ChatAssistant(
    private val assistantDescription: String,
    private val userDescription: String,
    private val apiKey: String,
    private val forceUseDocuments: Boolean = false,
    tools: List<Any> = listOf(),
    maxMessages: Int = 20) {

    val embeddingModel: EmbeddingModel = AllMiniLmL6V2EmbeddingModel()
    val embeddingStore: EmbeddingStore<TextSegment> = InMemoryEmbeddingStore<TextSegment>()
    val ingestor = EmbeddingStoreIngestor.builder()
            .documentSplitter(DocumentSplitters.recursive(500, 0))
            .embeddingModel(embeddingModel)
            .embeddingStore(embeddingStore)
            .build()

    val model = OpenAiChatModel.builder()
            .apiKey(apiKey)
            .timeout(Duration.ofSeconds(60))
            .logRequests(true)
            .logRequests(true)
            .build()
    val streamingModel = OpenAiStreamingChatModel.builder()
        .apiKey(apiKey)
        .timeout(Duration.ofSeconds(60))
        .logRequests(true)
        .logRequests(true)
        .build()
    val chatMemory = MessageWindowChatMemory.withMaxMessages(maxMessages)
    val ai = AiServices.builder(Assistant::class.java)
            .chatLanguageModel(model)
            .chatMemory(chatMemory)
            .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
            .tools(tools)
            .build()
    val aiStreaming = AiServices.builder(StreamingAssistant::class.java)
        .streamingChatLanguageModel(streamingModel)
        .chatMemory(chatMemory)
        .retriever(EmbeddingStoreRetriever.from(embeddingStore, embeddingModel))
        .tools(tools)
        .build()

    init {
        val systemMessage = """
            $assistantDescription
            Write all output in markdown format.
    
            Use the following information about myself, the user:
            $userDescription
        """.trimIndent()
        chatMemory.add(dev.langchain4j.data.message.SystemMessage(systemMessage))
    }

    fun addMessage(message: String) {
        chatMemory.add(dev.langchain4j.data.message.UserMessage(message))
    }

    fun addDocument(document: Document) {
        ingestor.ingest(document)
    }

    fun addFileDocument(documentPath: Path) {
        val document: Document = loadDocument(documentPath)
        ingestor.ingest(document)
    }

    fun addUrlDocument(documentUrl: String) {
        val document: Document = UrlDocumentLoader.load(documentUrl)
        ingestor.ingest(document)
    }

    fun chat(message: String): String {
        if (forceUseDocuments) {
            return chatWithDocuments(message)
        } else {
            return ai.chat(message)
        }
    }

    fun chatStreaming(message: String): TokenStream {
        if (forceUseDocuments) {
            return chatWithDocumentsStreaming(message)
        } else {
            return aiStreaming.chatStreaming(message)
        }
    }

    private fun findRelevant(queryText: String, maxResults: Int = 3, minScore: Double = 0.0): List<EmbeddingMatch<TextSegment>> {
        val queryResponse = embeddingModel.embed(queryText)
        val queryEmbedding = queryResponse.content() ?: return emptyList()

        return embeddingStore.findRelevant(queryEmbedding, maxResults, minScore)
    }

    private fun chatWithDocuments(message: String): String {
        val messageWithDocumentInformation = findRelevantInformationInDocuments(message)
        return ai.chat(messageWithDocumentInformation)
    }

    private fun chatWithDocumentsStreaming(message: String): TokenStream {
        val messageWithDocumentInformation = findRelevantInformationInDocuments(message)
        return aiStreaming.chatStreaming(messageWithDocumentInformation)
    }

    private fun findRelevantInformationInDocuments(message: String): String {
        val matches = findRelevant(message)

        val information: String = matches.joinToString("\n\n") { match ->
            val score = match.score()

            """
            Source: ${match.embedded().metadata()} score: $score 
            ```
            ${match.embedded().text()}
            ```
            """.trimIndent()
        }

        val messageWithDocumentInformation = """
            Answer the following question to the best of your ability:
            
            Question:
            $message
            
            Base your answer only on the following information in triple quotes, when required cite sources and score:
            
            Information:
            $information
        """.trimIndent()

        return messageWithDocumentInformation
    }
}

val userSpecification = """
Name: Eric
Location: Rapperswil-Jona, Switzerland
Latitude: 47.2266° N
Longitude: 8.8184° E
Hobbies: Astrophotography, Diving, Programming
Languages: German, English, Portuguese, French, Italian        
"""

val travelAssistantSpecification = """
You are a professional expert travel planner and assistant, explaining your recommendations in detail but short and concise.
You summarize weather reports in a natural way, hiding numeric weather code.
"""

val astrophotographyAssistantSpecification = """
You are a professional expert astronomer and astrophotographer.
You summarize weather reports concisely, optimized for astrophotography, focusing on cloud cover and visibility).
You calculate events correctly in the calendar.
"""

val librarianAssistantSpecification = """
You are a professional expert librarian.
You answer questions concisely after consulting stored documents and only provide information from the documents.
You always cite sources.
"""

val softwareAssistantSpecification = """
You are a professional expert software developer.
"""

fun ChatAssistant.printChat(message: String) {
    println(message)

    val response = this.chat(message)
    println(response)
    println()
}

fun ChatAssistant.printChatStreaming(message: String) {
    println(message)

    val tokenStream = this.chatStreaming(message)
    tokenStream.onNext(System.out::print)
        .onError(Throwable::printStackTrace)
        .start();

    println()
}

fun main() {
    val apiKey = System.getenv("OPENAI_API_KEY") ?: "demo"
    val tools = listOf(Calendar(), Weather(), Astronomy())
    //val tools = listOf<Any>()
    val assistant = ChatAssistant(
        astrophotographyAssistantSpecification,
        userSpecification,
        apiKey,
        false,
        tools)

    //assistant.addFileDocument(Path.of("./Cloud_Native_Spring_in_Action.pdf"))
    //assistant.addFileDocument(Path.of("./story_about_happy_carrot.txt"))
    //assistant.addUrlDocument("https://github.com/cosinekitty/astronomy/raw/master/source/kotlin/doc/index.md")
    //assistant.addUrlDocument("https://raw.githubusercontent.com/eobermuhlner/big-math/master/ch.obermuhlner.math.big/src/main/java/ch/obermuhlner/math/big/BigDecimalMath.java")


//    assistant.printChat("Who am I?")
//    assistant.printChat("Who are you?")
//    assistant.printChat("What date is today ?")
//    assistant.printChat("What day of the week is today ?")
//    assistant.printChat("How is the weather forecast for today in Marrakesh?")
//    assistant.printChat("How is the daily weather forecast for next week in Jona, Switzerland?")
//    assistant.printChat("Wie ist das Wetter morgen in Jona?")
//    assistant.printChat("Wie warm muss ich mich heute in Jona anziehen?")
//    assistant.printChat("am Montag?")
//    assistant.printChat("Welche Kleider soll ich für nächste Woche in Oslo packen?")
//    assistant.printChat("Can I do astrophotography tonight in Jona? I need at least 1 hour without clouds.")
//    assistant.printChat("List the best times for astrophotography next 5 days in Jona")
//    assistant.printChat("Is the moon tonight too bright for astrophotography?")
//    assistant.printChat("What is the phase of the moon now.")
//    assistant.printChat("What are my latitude/longitude.")
//    assistant.printChat("What is the date of the next saturday.")
//    assistant.printChat("What will be the date, phase of the moon and the weather forecast next saturday?")
//    assistant.printChat("List approaches to make a cloud application more resilient")

//    assistant.printChat("Who was the first president of the US?")

//    assistant.printChat("Who invaded Veggieville?")
//    assistant.printChat("Who invaded Veggieville? Cite sources")
//    assistant.printChatStreaming("List approaches to resilience with a short code example. Cite sources")

//    assistant.printChat("List moon related astronomy functions.")

//    assistant.printChat("How is the log function implemented. Analyze for bugs. Find potential optimizations. Explain the mathematical concepts.")

    assistant.printChatStreaming("""
        Show the hourly weather report for tonight 20:00 until tomorrow 08:00 in the following format:
        
        Moonphase: {moonphase degrees}° {moonphase description} Brightness: {moonphase estimated brightness %}
        - {date} {time} : Cloud cover: {cloud cover %} Visibility: {visibility} Precipitation: {precipitation probability %} Temperature: {temperature}
        
        {short weather summary whether astrophotography is possible}
    """.trimIndent())

}