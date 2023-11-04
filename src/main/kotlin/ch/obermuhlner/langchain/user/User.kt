package ch.obermuhlner.langchain.user

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "name")
    var name: String = "",

    @Column(name = "description", length = 10240)
    var description: String = "",

    @Column(name = "openaiApiKey", length = 1024)
    var openaiApiKey: String = "demo",
) {
    override fun toString() = "User{$id}"
}
