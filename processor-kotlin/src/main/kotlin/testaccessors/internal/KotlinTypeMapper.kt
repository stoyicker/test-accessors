package testaccessors.internal

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.STAR
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName
import com.squareup.kotlinpoet.WildcardTypeName
import com.squareup.kotlinpoet.asTypeName
import kotlinx.metadata.Flag
import kotlinx.metadata.Flags
import kotlinx.metadata.KmClassVisitor
import kotlinx.metadata.KmTypeParameterVisitor
import kotlinx.metadata.KmTypeVisitor
import kotlinx.metadata.KmVariance
import kotlinx.metadata.jvm.KotlinClassHeader
import kotlinx.metadata.jvm.KotlinClassMetadata
import javax.lang.model.element.Element
import kotlin.reflect.jvm.internal.impl.builtins.jvm.JavaToKotlinClassMap
import kotlin.reflect.jvm.internal.impl.name.FqName

/**
 * @see <a href="https://github.com/square/kotlinpoet/issues/236">TypeMirror.asTypeName() returns java.lang.String when receiver type is kotlin.String</a>
 */
internal fun Element.kotlinize() = getAnnotation(Metadata::class.java).run { this ?: asType().getAnnotation(Metadata::class.java) }.toKotlinTypeName(asType().asTypeName())

internal fun TypeName.kotlinize() = (null as Metadata?).toKotlinTypeName(this)

private fun Metadata?.toKotlinTypeName(fallback: TypeName): TypeName = this?.run {
  KotlinClassMetadata.read(KotlinClassHeader(
      kind,
      metadataVersion,
      bytecodeVersion,
      data1,
      data2,
      extraString,
      packageName,
      extraInt))!!.let {
    when (it) {
      is KotlinClassMetadata.Class -> ClassVisitor().apply { it.accept(this) }.resolve()
      else -> throw RuntimeException("Received metadata of type ${it::class.java.name} when class was expected")
    }
  }
} ?: when (fallback) {
  is WildcardTypeName -> when {
    fallback.outTypes.isNotEmpty() -> WildcardTypeName.producerOf(fallback.outTypes.map { it.kotlinize() }.first())
    fallback.inTypes.isNotEmpty() -> WildcardTypeName.consumerOf(fallback.outTypes.map { it.kotlinize() }.first())
    else -> throw IllegalArgumentException("$fallback is WildcardTypeName but produces and consumes nothing?!")
  }
  is ParameterizedTypeName -> (fallback.rawType.run { extractKotlinTypeFromMapIfExists(this, toString()) } as ClassName).parameterizedBy(*fallback.typeArguments.map { it.kotlinize() }.toTypedArray())
  else -> extractKotlinTypeFromMapIfExists(fallback, fallback.toString())
} // FIXME #12

private class ClassVisitor : KmClassVisitor(), Resolver<TypeName> {
  private lateinit var root: ClassName
  private val typeParameterVisitors by lazy { mutableListOf<Resolver<TypeVariableName>>() }
  private val typeParameterMap = mutableMapOf<Int, TypeVariableName>()

  override fun visit(flags: Flags, name: String) = super.visit(flags, name).also {
    root = ClassName.bestGuess(name.replace('/', '.'))
  }

  override fun visitTypeParameter(flags: Flags, name: String, id: Int, variance: KmVariance) =
      TypeParameterVisitor(
          id,
          name,
          variance,
          Flag.TypeParameter.IS_REIFIED(flags),
          typeParameterMap).also {
        typeParameterVisitors += it
      }

  override fun resolve() = root.run {
    if (typeParameterVisitors.isNotEmpty()) {
      parameterizedBy(*typeParameterVisitors.map {
        it.resolve()
      }.toTypedArray())
    } else {
      this
    }
  }
}

private class TypeParameterVisitor(
    private val id: Int,
    private val name: String,
    private val variance: KmVariance,
    private val reified: Boolean,
    private val typeParameterMap: MutableMap<Int, TypeVariableName>) : KmTypeParameterVisitor(), Resolver<TypeVariableName> {
  private val boundVisitors by lazy { mutableListOf<Resolver<TypeName>>() }

  override fun visitUpperBound(flags: Flags) = TypeVisitor(Flag.Type.IS_NULLABLE(flags), typeParameterMap).also {
    boundVisitors += it
  }

  override fun resolve() = TypeVariableName(
      name = name,
      variance = when (variance) {
        KmVariance.IN -> KModifier.IN
        KmVariance.OUT -> KModifier.OUT
        else -> null
      })
      .run { copy(reified = reified) }
      .also { typeParameterMap[id] = it }
      .run {
        copy(bounds = boundVisitors.map { it.resolve() }.takeIf { it.isNotEmpty() }
            ?: listOf(NULLABLE_ANY))
      }
}

private class TypeVisitor(
    private val nullable: Boolean,
    private val typeParameterMap: MutableMap<Int, TypeVariableName>) : KmTypeVisitor(), Resolver<TypeName> {
  private var typeParameterId: Int? = null
  private var root: TypeName? = null
  private val typeArgumentVisitors by lazy { mutableListOf<Resolver<TypeName>>() }

  override fun visitTypeParameter(id: Int) {
    typeParameterId = id
  }

  override fun visitClass(name: String) {
    root = ClassName.bestGuess(name.replace('/', '.'))
  }

  override fun visitStarProjection() =
      if (root == null) {
        root = STAR
      } else {
        root = (root as ClassName).parameterizedBy(STAR)
      }

  override fun visitArgument(flags: Flags, variance: KmVariance) =
      TypeVisitor(Flag.Type.IS_NULLABLE(flags), typeParameterMap).also { typeArgumentVisitors += it }

  override fun resolve() = root.run {
    when (this) {
      null -> typeParameterMap[typeParameterId!!]!!
      is ClassName -> if (typeArgumentVisitors.isNotEmpty()) {
        parameterizedBy(*typeArgumentVisitors.map { it.resolve() }.toTypedArray())
      } else {
        throw IllegalStateException("A type visitor does not have its root correctly processed. Type aliases may have caused this.")
      }
      is ParameterizedTypeName -> if (typeArgumentVisitors.isNotEmpty()) {
        assert(typeArgumentVisitors.size == 1) { "Unexpected amount of visited arguments ${typeArgumentVisitors.size}" }
        plusParameter(typeArgumentVisitors.first().resolve())
      } else {
        this
      }
      else -> this
    }
  }.copy(nullable = nullable)
}

private interface Resolver<out T> {
  fun resolve(): T
}

private val NULLABLE_ANY = ClassName.bestGuess("kotlin.Any").copy(nullable = true)

private fun extractKotlinTypeFromMapIfExists(fallback: TypeName, name: String) =
    JavaToKotlinClassMap.INSTANCE.mapJavaToKotlin(FqName(name)).run {
      when (this) {
        null -> fallback
        else -> ClassName.bestGuess(asSingleFqName().asString())
      }
    }

