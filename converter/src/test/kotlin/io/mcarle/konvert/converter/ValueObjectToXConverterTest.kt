package io.mcarle.konvert.converter
import com.tschuchort.compiletesting.SourceFile
import io.mcarle.konvert.converter.utils.ConverterITest
import io.mcarle.konvert.converter.utils.VerificationData
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties
import kotlin.test.assertEquals

val KClass<String>.nullableQualifiedName: String?
    get() = qualifiedName?.let { "$it?" }

@OptIn(ExperimentalCompilerApi::class)
class ValueObjectToBasicTypeConverterTest : ConverterITest() {

    companion object {
        @JvmStatic
        fun argumentList(): List<Arguments> = listOf(
            Arguments.of("Name", String::class.qualifiedName, String::class.qualifiedName),
            Arguments.of("Name", String::class.nullableQualifiedName, String::class.nullableQualifiedName),
            Arguments.of("Name?", String::class.nullableQualifiedName, String::class.qualifiedName),
            Arguments.of("Name", String::class.nullableQualifiedName, String::class.qualifiedName),
        )
    }

    @ParameterizedTest
    @MethodSource("argumentList")
    fun testValueObjectToString(sourceTypeName: String, targetTypeName: String, valueObjectValueType: String) {
        executeTest(
            sourceTypeName = sourceTypeName,
            targetTypeName = targetTypeName,
            converter = ValueObjectToXConverter(),
            additionalCode = generateAdditionalCode(valueObjectValueType)
        )
    }

    private fun generateAdditionalCode(valueObjectValueType: String): List<SourceFile> = listOf(
        SourceFile.kotlin(
            name = "UserId.kt",
            contents =
            """
            class Name(val value: $valueObjectValueType)
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
