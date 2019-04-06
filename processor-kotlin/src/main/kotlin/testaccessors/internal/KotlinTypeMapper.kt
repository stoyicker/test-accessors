package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

fun TypeName.kotlinize(): TypeName =
		when (this) {
			is ParameterizedTypeName -> ParameterizedTypeName.get(
					rawType.kotlinize() as ClassName,
					*typeArguments.map { it.kotlinize() }.toTypedArray())
			else -> JavaToKotlinClassMap.INSTANCE
					.mapJavaToKotlin(FqName(toString()))
					?.asSingleFqName()
					?.asString().run {
						when (this) {
							null -> this@kotlinize
							else -> ClassName.bestGuess(this)
						}
					}
		}
