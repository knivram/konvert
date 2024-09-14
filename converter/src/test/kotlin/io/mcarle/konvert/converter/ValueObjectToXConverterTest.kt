package io.mcarle.konvert.converter
import com.tschuchort.compiletesting.SourceFile
import io.mcarle.konvert.converter.utils.ConverterITest
import io.mcarle.konvert.converter.utils.VerificationData
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

@OptIn(ExperimentalCompilerApi::class)
class ValueObjectToBasicTypeConverterTest : ConverterITest() {

    @Test
    fun testValueObjectToString() {
        executeTest(
            sourceTypeName = "Name",
            targetTypeName = "String",
            converter = ValueObjectToXConverter(),
            additionalCode = generateAdditionalCode()
        )
    }

    private fun generateAdditionalCode(): List<SourceFile> = listOf(
        SourceFile.kotlin(
            name = "UserId.kt",
            contents =
            """
            class Name(val value: String)
            """.trimIndent()
        )
    )

    override fun verify(verificationData: VerificationData) {
        val name = "name"
        val targetInstance = verificationData.targetKClass.constructors.first().call(name)
        val nameInstance = (verificationData.sourceKClass.memberProperties.first().returnType.classifier as KClass<*>)
            .constructors.first().call(name)
        val sourceInstance = verificationData.sourceKClass.constructors.first().call(nameInstance)

        val result = verificationData.mapperFunction.call(verificationData.mapperInstance, sourceInstance)

        verificationData.targetKClass.memberProperties.forEach { property ->
            @Suppress("UNCHECKED_CAST")
            val prop = property as KProperty1<Any?, Any?>
            val targetValue = prop.get(targetInstance)
            val resultValue = prop.get(result)
            assertEquals(targetValue, resultValue)
        }
    }
}
