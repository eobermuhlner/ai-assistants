package ch.obermuhlner.langchain.chat

import ch.obermuhlner.langchain.document.base64ToFloatArray
import ch.obermuhlner.langchain.document.jsonToMap
import ch.obermuhlner.langchain.hello.Astronomy
import ch.obermuhlner.langchain.hello.Calendar
import ch.obermuhlner.langchain.hello.ChatAssistant
import ch.obermuhlner.langchain.hello.Weather
import dev.langchain4j.data.document.Document
import dev.langchain4j.data.document.Metadata
import dev.langchain4j.data.embedding.Embedding
import dev.langchain4j.data.message.AiMessage
import dev.langchain4j.data.segment.TextSegment
import dev.langchain4j.model.output.Response
import dev.langchain4j.service.OnCompleteOrOnError
import dev.langchain4j.service.OnError
import dev.langchain4j.service.OnStart
import dev.langchain4j.service.TokenStream
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionTemplate
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@Service
class ChatService(
    @Autowired val chatRepository: ChatRepository,
    @Autowired val messageRepository: MessageRepository,
    @Autowired private val transactionTemplate: TransactionTemplate,
) {

    private val logger = LoggerFactory.getLogger(ChatService::class.java)

    private val assistantCache = mutableMapOf<Long, ChatAssistant>()

    @Transactional
    fun createChat(chat: Chat): Chat {
        return chatRepository.save(chat)
    }

    fun findAllChats(): List<Chat> {
        return chatRepository.findAll()
    }

    fun getChat(chatId: Long): Chat {
        return chatRepository.findById(chatId).orElseThrow { RuntimeException("Chat not found") }
    }

    fun getMessages(chatId: Long): List<Message> {
        val chat = getChat(chatId)
        return chat.messages
    }

    fun addDocument(chatId: Long, document: Document) {
        val chat = getChat(chatId)

        val assistant = getChatAssistant(chat)
        assistant.addDocument(document)
    }

    fun addUrlDocument(chatId: Long, documentUrl: String) {
        val chat = getChat(chatId)

        val assistant = getChatAssistant(chat)
        assistant.addUrlDocument(documentUrl)
    }

    data class ChatAndMessage(val chat: Chat, val message: Message)

    fun addMessage(chatId: Long, messageContent: String): CompletableFuture<Message> {
        return CompletableFuture.supplyAsync {
            val chat = chatRepository.findById(chatId).orElseThrow { RuntimeException("Chat not found") }
            val message = Message(content = messageContent, chat = chat)
            chat.messages.add(message)
            messageRepository.save(message)
            chat
        }.thenCompose { chat ->
            generateResponseMessage(chat, messageContent).thenApply { responseMessage ->
                ChatAndMessage(chat, responseMessage)
            }
        }.thenApplyAsync { chatAndMessage ->
            transactionTemplate.execute {
                addResponseMessage(chatAndMessage.chat, chatAndMessage.message)
            }
            chatAndMessage.message
        }
    }

    fun addMessageStreaming(chatId: Long, messageContent: String): TokenStream {
        val chat = chatRepository.findById(chatId).orElseThrow { RuntimeException("Chat not found") }
        val message = Message(content = messageContent, chat = chat)
        chat.messages.add(message)
        messageRepository.save(message)

        return generateResponseMessageStreaming(chat, messageContent)
    }

    private fun addResponseMessage(chat: Chat, message: Message) {
        chat.messages.add(message)
        messageRepository.save(message)
    }

    private fun toTools(tools: String): List<Any> {
        return tools.split(",").map { it.trim() }.mapNotNull {
            when (it) {
                "Calendar" -> Calendar()
                "Weather" -> Weather()
                "Astronomy" -> Astronomy()
                else -> null
            }
        }
    }

    private fun generateResponseMessage(chat: Chat, userMessage: String): CompletableFuture<Message> {
        return CompletableFuture.supplyAsync {
            val chatAssistant = getChatAssistant(chat)
            println(userMessage)
            val response = chatAssistant.chat(userMessage)
            println(response)
            println()

            Message(content = response, chat = chat)
        }
    }

    private fun generateResponseMessageStreaming(chat: Chat, userMessage: String): TokenStream {
        val chatAssistant = getChatAssistant(chat)
        println(userMessage)
        val responseStream = chatAssistant.chatStreaming(userMessage)

        val duplicatedTokenStream = TokenStreamDuplicator(responseStream, {
            print(it)
        }, {
            println()
            val responseText = it.content().text()
            val responseMessage = Message(content = responseText, chat = chat)
            addResponseMessage(chat, responseMessage)
        }, {
        })

        return duplicatedTokenStream
    }

    @Synchronized
    private fun getChatAssistant(chat: Chat): ChatAssistant {
        return assistantCache.computeIfAbsent(chat.id!!) { _ ->
            val assistantDescription = chat.assistant?.description ?: "Unknown assistant"
            val forceUseDocuments = chat.assistant?.forceUseDocuments ?: false
            val tools = toTools(chat.assistant?.tools ?: "")
            val documents = chat.assistant?.documents ?: emptyList()

            val userDescription = chat.user?.description ?: "Unknown user"
            val openaiApiKey = chat.user?.openaiApiKey ?: "demo"

            val assistant = ChatAssistant(assistantDescription, userDescription, openaiApiKey, forceUseDocuments, tools)
            logger.debug("Creating ChatAssistant ${chat.assistant?.id} for user ${chat.user?.id} with ${chat.messages.size} messages")
            for (message in chat.messages) {
                assistant.addMessage(message.content)
            }
            for (document in documents) {
                for (segment in document.segments) {
                    val embedding = Embedding(base64ToFloatArray(segment.embedding))
                    val metadata = Metadata(jsonToMap(segment.metadata))
                    val textSegment = TextSegment.from(segment.text, metadata)
                    assistant.embeddingStore.add(embedding, textSegment)
                }
            }

            assistant
        }
    }

    @Synchronized
    fun clearChatCache() {
        assistantCache.clear()
    }

//    private fun toFloatArray(embedding: String): FloatArray {
//        return embedding.drop(1).dropLast(1).split(", ").map { it.toFloat() }.toFloatArray()
//    }
}

class TokenStreamDuplicator(private val original: TokenStream,
                            private val duplicatedTokenHandler: Consumer<String>?,
                            private val duplicatedCompletionHandler: Consumer<Response<AiMessage>>? ,
                            private val duplicatedErrorHandler: Consumer<Throwable>?) : TokenStream {
    override fun onNext(tokenHandler: Consumer<String>?): OnCompleteOrOnError {
        return object : OnCompleteOrOnError {
            override fun onComplete(completionHandler: Consumer<Response<AiMessage>>): OnError {
                return object : OnError {
                    override fun onError(errorHandler: Consumer<Throwable>): OnStart {
                        return DuplicatedTokenStream(original, duplicatedTokenHandler, duplicatedCompletionHandler, duplicatedErrorHandler, tokenHandler, completionHandler, errorHandler)
                    }

                    override fun ignoreErrors(): OnStart {
                        return DuplicatedTokenStream(original, duplicatedTokenHandler, duplicatedCompletionHandler, duplicatedErrorHandler, tokenHandler, completionHandler, null)
                    }
                }
            }

            override fun onError(errorHandler: Consumer<Throwable>): OnStart {
                return DuplicatedTokenStream(original, duplicatedTokenHandler, duplicatedCompletionHandler, duplicatedErrorHandler, tokenHandler, null, errorHandler)
            }

            override fun ignoreErrors(): OnStart {
                return DuplicatedTokenStream(original, duplicatedTokenHandler, duplicatedCompletionHandler, duplicatedErrorHandler, tokenHandler, null, null)
            }
        }
    }

    class DuplicatedTokenStream(private val original: TokenStream,
                                private val duplicatedTokenHandler: Consumer<String>?,
                                private val duplicatedCompletionHandler: Consumer<Response<AiMessage>>? ,
                                private val duplicatedErrorHandler: Consumer<Throwable>?,
                                private val tokenHandler: Consumer<String>?,
                                private val completionHandler: Consumer<Response<AiMessage>>? ,
                                private val errorHandler: Consumer<Throwable>?) : OnStart {
        override fun start() {
            original.onNext {
                duplicatedTokenHandler?.accept(it)
                tokenHandler?.accept(it)
            }
                .onComplete {
                    duplicatedCompletionHandler?.accept(it)
                    completionHandler?.accept(it)
                }
                .onError {
                    duplicatedErrorHandler?.accept(it)
                    errorHandler?.accept(it)
                }.start()
        }
    }
}
