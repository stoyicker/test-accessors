package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

/**
 * @see <a href="https://github.com/square/kotlinpoet/issues/236">TypeMirror.asTypeName() returns java.lang.String when receiver type is kotlin.String</a>
 */
internal fun TypeName.kotlinize(): TypeName = when (this) {
  is ParameterizedTypeName -> (rawType.kotlinize() as ClassName).run {
    if (typeArguments.isNotEmpty()) {
      parameterizedBy(*typeArguments.map { it.kotlinize() }.toTypedArray())
    } else {
      this
    }
  }
  else -> extractKotlinTypeFromMapIfExists(this, toString())
}.copy(nullable = isNullable)

private fun extractKotlinTypeFromMapIfExists(fallback: TypeName, name: String) =
    JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(name)).run {
      when (this) {
        null -> fallback
        else -> ClassName.bestGuess(asSingleFqName().asString())
      }
    }
