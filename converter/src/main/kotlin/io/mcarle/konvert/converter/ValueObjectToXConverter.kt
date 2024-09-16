package io.mcarle.konvert.converter

import com.google.auto.service.AutoService
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import io.mcarle.konvert.converter.api.AbstractTypeConverter
import io.mcarle.konvert.converter.api.TypeConverter
import io.mcarle.konvert.converter.api.classDeclaration
import io.mcarle.konvert.converter.api.isNullable

@AutoService(TypeConverter::class)
class ValueObjectToXConverter : AbstractTypeConverter() {

    override val enabledByDefault: Boolean = true

    override fun matches(source: KSType, target: KSType): Boolean =
        handleNullable(source, target) { sourceNotNullable, _ ->
            sourceNotNullable.classDeclaration()?.let {
                val valueProperty = it.getAllProperties().singleOrNull() ?: return@handleNullable false
                return@handleNullable valueProperty.simpleName.asString() == "value" && target.isAssignableFrom(valueProperty.type.resolve())
            } ?: false
        }


    override fun convert(fieldName: String, source: KSType, target: KSType): CodeBlock {
        return if (source.isNullable()) {
            CodeBlock.of("$fieldName?.value")
        } else {
            CodeBlock.of("$fieldName.value")
        }
    }
}
