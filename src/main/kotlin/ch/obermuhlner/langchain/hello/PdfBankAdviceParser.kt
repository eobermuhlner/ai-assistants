package ch.obermuhlner.langchain.hello

import dev.langchain4j.model.openai.OpenAiChatModel
import dev.langchain4j.model.output.structured.Description
import dev.langchain4j.service.AiServices.*
import dev.langchain4j.service.UserMessage
import java.time.Duration
import java.time.LocalDate


data class BankAdvice(
    val clientName: String,
    @Description("regex to extract the client name in the group(1) of the given text")
    val clientNameRegex: String,
    val clientAccount: String,
    val currency: String,
    val tradeDate: LocalDate
)

interface BankAdviceExtractor {
    @UserMessage("Extract information about a bank advice from {{it}}")
    fun parseBankAdvice(text: String): BankAdvice
}

fun main() {
    val apiKey = System.getenv("OPENAI_API_KEY") ?: "demo"

    val question1 = """
SECURITY EVENT
INTEREST PAYMENT
Ordinary interest
0A01 Banque Pictet & Cie SA / Route des Acacias 60 / 1211 Genève 73 / Switzerland
Tel. +41 58 323 23 23 / Fax +41 58 323 23 24 / group.pictet
1/1
Client: Some Private Person
Account no.: I-123277.001
Transaction no.: 764938848 | Publication date: 15.03.2022
E. & O. E.
Yours faithfully
Banque Pictet & Cie SA
Advice without signature
ADDITIONAL INFORMATION
General
Trade date 01.04.2021
Value date 08.04.2021
Booking date 15.03.2022
Security event
Ex date 01.04.2021
Payment date 08.04.2021
Interest - ISHARES NATIONAL MUNI BOND ETF USD
Quantity held 300
Income * USD 0.18208
* for a quantity of 1.00
Gross amount USD 54.62
Other
Deposit CITIBANK NA
Comment
DIVIDEND RECLASSIFICATION
QUANTITY HELD in portfolio I-123277.001
ISHARES NATIONAL MUNI BOND ETF USD
300
ISIN: US4642884146    Telekurs ID: 3398490
CASH EFFECT in portfolio I-123277.001
Gross amount USD 54.62
Net amount
USD
54.62
Current account I-630277.001.00.USD/Ordinary IBAN?CH0908755063027700100
    """.trimIndent()

    val question2 = """
TRAFIC DES PAIEMENTS
TRANSFERT DE LIQUIDITÉS
0A01 Banque Pictet & Cie SA / Route des Acacias 60 / 1211 Genève 73 / Suisse
Tél. +41 58 323 23 23 / Fax +41 58 323 23 24 / groupe.pictet
1/1
Client: Monsieur Bizarre
No de compte: P-123503.001
No de transaction: 765000468 | Date de publication: 15.03.2022
S.E. & O.
Avec nos compliments
Banque Pictet & Cie SA
Avis sans signature
INFORMATIONS COMPLÉMENTAIRES
Général
Date de transaction 17.03.2022
Date valeur 14.03.2022
Date comptable 15.03.2022
Date de l'ordre 16.03.2022 à 09:47:29
Transfert
Bénéficiaire BABAC WORLDWIDE LTD
No de compte Z-123503.003
Montant brut EUR 648.89
EFFET CASH dans le portefeuille  P-123503.001
Montant brut EUR -648.89
Taux de change (EUR/USD): 1.11489558
Montant net
USD
-723.44
Compte courant P-123503.001.00.USD/Ordinaire IBAN?CH4708755058650300100
    """.trimIndent()

    val model = OpenAiChatModel.builder().apiKey(apiKey).timeout(Duration.ofSeconds(60)).build()

    val bankAdviceExtractor = create(BankAdviceExtractor::class.java, model)
    val advice = bankAdviceExtractor.parseBankAdvice(question2)
    println(advice)
}