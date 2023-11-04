package ch.obermuhlner.langchain.document

import java.nio.ByteBuffer
import java.util.*


fun floatArrayToBase64(floatArray: FloatArray): String {
    fun floatArrayToByteArray(floatArray: FloatArray): ByteArray {
        val byteBuffer = ByteBuffer.allocate(floatArray.size * 4)
        for (value in floatArray) {
            byteBuffer.putFloat(value)
        }
        return byteBuffer.array()
    }

    val byteArray = floatArrayToByteArray(floatArray)
    return Base64.getEncoder().encodeToString(byteArray)
}

fun base64ToFloatArray(compact: String): FloatArray {
    val byteArray = Base64.getDecoder().decode(compact)

    val floatArray = FloatArray(byteArray.size / 4)
    val byteBuffer = ByteBuffer.wrap(byteArray)
    for (i in floatArray.indices) {
        floatArray[i] = byteBuffer.float
    }
    return floatArray
}
