package io.mcarle.konvert.converter

import com.google.auto.service.AutoService
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.symbol.KSType
import com.squareup.kotlinpoet.CodeBlock
import io.mcarle.konvert.converter.api.AbstractTypeConverter
import io.mcarle.konvert.converter.api.TypeConverter
import io.mcarle.konvert.converter.api.classDeclaration

@AutoService(TypeConverter::class)
class XToValueObjectConverter : AbstractTypeConverter(){
    override val enabledByDefault: Boolean = true

    override fun matches(source: KSType, target: KSType): Boolean =
        handleNullable(source, target) { _, targetNotNullable ->
            targetNotNullable.classDeclaration()?.let {
                it.getConstructors().any { constructor ->
                    val valueConstructor = constructor.parameters.singleOrNull() { parameter ->
                        parameter.type.resolve().isAssignableFrom(source)
                    }
                    valueConstructor != null
                }
            } ?: false
        }

    override fun convert(fieldName: String, source: KSType, target: KSType): CodeBlock {
        TODO("Not yet implemented")
    }

}
