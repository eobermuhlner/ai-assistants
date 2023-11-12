package ch.obermuhlner.langchain.user

import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @Column(name = "username")
    var username: String = "",

    @Column(name = "password")
    var password: String = "",

    @Column(name = "authorities")
    var authorities: String = "",

    @Column(name = "name")
    var name: String = "",

    @Column(name = "description", length = 10240)
    var description: String = "",

    @Column(name = "openaiApiKey", length = 1024)
    var openaiApiKey: String = "demo",
) {
    override fun toString() = "User{$id}"
}
