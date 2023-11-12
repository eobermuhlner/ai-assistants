package ch.obermuhlner.langchain.chat

import ch.obermuhlner.langchain.document.toDocumentParser
import ch.obermuhlner.langchain.security.PrincipalService
import ch.obermuhlner.langchain.user.User
import ch.obermuhlner.langchain.user.UserService
import dev.langchain4j.data.document.DocumentType
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.WebAsyncTask
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.OutputStream
import java.security.Principal
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch


@RestController
@RequestMapping("/api/chats")
class ChatRestController @Autowired constructor(
    private val principalService: PrincipalService,
    private val chatService: ChatService,
    private val userService: UserService
) {

    @PostMapping
    fun createChat(@RequestBody chat: Chat, user: User): ResponseEntity<Chat> {
        chat.user = user
        val createdChat = chatService.createChat(chat)
        return ResponseEntity(createdChat, HttpStatus.CREATED)
    }

    @GetMapping
    fun findAllChats(user: User): ResponseEntity<List<Chat>> {
        val chats = chatService.findChatsByUser(user)
        return ResponseEntity(chats, HttpStatus.OK)
    }

    @GetMapping("/{chatId}")
    fun getChat(@PathVariable chatId: Long, principal: Principal): ResponseEntity<Chat> {
        val chat = principalService.getChat(principal, chatId)
        return ResponseEntity(chat, HttpStatus.OK)
    }

    @GetMapping("/{chatId}/messages")
    fun addMessage(@PathVariable chatId: Long, principal: Principal): ResponseEntity<List<Message>> {
        principalService.getChat(principal, chatId)

        val messages = chatService.getMessages(chatId)
        return ResponseEntity(messages, HttpStatus.OK)
    }

    private val asyncTimeoutMillis = 120_000L
    @PostMapping("/{chatId}/messages")
    fun addMessage(@PathVariable chatId: Long, @RequestBody message: Message, principal: Principal): WebAsyncTask<CompletableFuture<ResponseEntity<Message>>> {
        principalService.getChat(principal, chatId)

        val messageContent = message.content
        return WebAsyncTask(asyncTimeoutMillis) {
            chatService.addMessage(chatId, messageContent)
                .thenApply { responseMessage ->
                    ResponseEntity(responseMessage, HttpStatus.CREATED)
                }
        }
    }

    @PostMapping("/{chatId}/messages/streaming")
    fun addMessageStreaming(@PathVariable chatId: Long, @RequestBody message: Message): ResponseEntity<StreamingResponseBody> {
        val streamingResponseBody = StreamingResponseBody { os: OutputStream ->
            val latch = CountDownLatch(1)
            val messageContent = message.content
            val tokenStream = chatService.addMessageStreaming(chatId, messageContent)
            tokenStream
                .onNext {
                    os.write(it.toByteArray())
                    os.flush()
                }
                .onComplete {
                    latch.countDown()
                }
                .onError { }
                .start()

            latch.await()
        }

        return ResponseEntity.ok()
            .header("Content-Type", "application/text-plain")
            .body(streamingResponseBody)
    }

    @PostMapping("/{chatId}/messages/server-sent-event")
    fun addMessageServerSentEvent(@PathVariable chatId: Long, @RequestBody message: Message): SseEmitter {
        val sseEmitter = SseEmitter()

        val messageContent = message.content
        val tokenStream = chatService.addMessageStreaming(chatId, messageContent)

        tokenStream.onNext {
                sseEmitter.send(it)
            }
            .onComplete {
                sseEmitter.complete()
            }
            .onError {
                sseEmitter.completeWithError(it)
            }
            .start()

        return sseEmitter
    }

    @PostMapping("/{chatId}/documents/upload")
    fun uploadFile(@PathVariable chatId: Long, @RequestParam("file") file: MultipartFile): ResponseEntity<String> {
        try {
            val documentType = DocumentType.of(file.originalFilename)
            val parser = toDocumentParser(documentType)
            val document = parser.parse(ByteArrayInputStream(file.bytes))
            document.metadata().add("file", file.originalFilename)
            chatService.addDocument(chatId, document)

            return ResponseEntity.status(HttpStatus.OK).body("File uploaded successfully.")
        } catch (e: IOException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the file.")
        }
    }

    @PostMapping("/{chatId}/documents/url")
    fun uploadUrl(@PathVariable chatId: Long, @RequestParam("url") url: String): ResponseEntity<String> {
        try {
            chatService.addUrlDocument(chatId, url)

            return ResponseEntity.status(HttpStatus.OK).body("URL uploaded successfully.")
        } catch (e: IOException) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload the URL.")
        }
    }

    @GetMapping("/clear-cache")
    fun clearChatCache() {
        chatService.clearChatCache()
    }

    @GetMapping("/{chatId}/documents/summary/{documentId}")
    fun summarizeDocument(@PathVariable chatId: Long, @PathVariable documentId: Long): String {
        return chatService.summarizeDocument(chatId, documentId)
    }
}
