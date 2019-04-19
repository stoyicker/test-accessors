package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import me.eugeniomarletti.kotlin.metadata.KotlinClassMetadata
import me.eugeniomarletti.kotlin.metadata.KotlinMetadata
import me.eugeniomarletti.kotlin.metadata.extractFullName
import me.eugeniomarletti.kotlin.metadata.kotlinMetadata
import me.eugeniomarletti.kotlin.metadata.shadow.metadata.ProtoBuf
import java.util.Stack
import javax.lang.model.element.Element
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

/**
 * @see <a href="https://github.com/square/kotlinpoet/issues/236">TypeMirror.asTypeName() returns java.lang.String when receiver type is kotlin.String</a>
 */
internal fun Element.kotlinize(): TypeName = kotlinMetadata?.run {
  if (this !is KotlinClassMetadata) {
    throw IllegalArgumentException("Got ${KotlinMetadata::class.simpleName} other than ${KotlinClassMetadata::class.simpleName}. This should never happen.")
  }
  val classData = data
  val (nameResolver, classProto) = classData
  fun ProtoBuf.Type.extractFullName() = extractFullName(classData)
  val fqClassName = nameResolver.getString(classProto.fqName).replace('/', '.')

  val typeArguments = classProto.typeParameterList
      .map { typeArgument ->
        Pair(
            nameResolver.getString(typeArgument.name),
            typeArgument.upperBoundList.map { it.extractFullName() })
      }
  ClassName.bestGuess(fqClassName).apply {
    if (typeArguments.isNotEmpty()) {
      parameterizedBy(*typeArguments.map { typeArgumentToTypeName(it) }.toTypedArray())
    }
  }
} ?: asType().asTypeName().kotlinize()

private fun typeArgumentToTypeName(typeArgument: Pair<String, List<String>>) = typeArgument.run {
  if (second.isEmpty()) {
    ClassName.bestGuess(first)
  } else {
    val stringStack = second.joinToString(separator = "").replace("`", "").split(Regex("(?<=[<>])|(?=[<>])"))
    val parameterStartStack = Stack<Int>()
    val typeStack = Stack<TypeName>()
    stringStack.forEach {
      when (it) {
        "*" -> typeStack.push(ClassName.bestGuess("kotlin.Any").copy(nullable = true))
        "<" -> {
          parameterStartStack.push(typeStack.size)
        }
        ">" -> {
          val parameters = mutableListOf<TypeName>()
          val parameterAmount = typeStack.size - parameterStartStack.pop()
          repeat(parameterAmount) {
            parameters.add(typeStack.pop())
          }
          typeStack.pop().let { typeStack.push((it as ClassName).parameterizedBy(*parameters.toTypedArray())) }
        }
        "?" -> typeStack.push(typeStack.pop().copy(nullable = true))
        else -> typeStack.push(ClassName.bestGuess(it))
      }
    }
    typeStack.pop()
  }
}

internal fun TypeName.kotlinize(): TypeName = when (this) {
  is ParameterizedTypeName -> (rawType.kotlinize() as ClassName).run {
    if (typeArguments.isNotEmpty()) {
      parameterizedBy(*typeArguments.map { it.kotlinize() }.toTypedArray())
    } else {
      this
    }
  }
  else -> extractKotlinTypeFromMapIfExists(this, toString())
}

private fun extractKotlinTypeFromMapIfExists(fallback: TypeName, name: String) =
    JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(name)).run {
      when (this) {
        null -> fallback
        else -> ClassName.bestGuess(asSingleFqName().asString())
      }
    }
