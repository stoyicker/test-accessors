package testaccessors.internal;

import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

abstract class AbstractAccessorWriter {
  static final String PARAMETER_NAME_NEW_VALUE = "newValue";
  static final String ERROR_MESSAGE_ILLEGAL_ACCESS = "Accessing this method from this source set is not allowed";
  static final String ERROR_MESSAGE_UNSUPPORTED_STATIC_FINAL_SETTER = "Generating setters for fields that are both static and final is not supported";
  final Elements elementUtils;
  final Types typeUtils;
  final Lazy<Logger> logger;
  final Options options;

  AbstractAccessorWriter(
      final Elements elementUtils,
      final Types typeUtils,
      final Lazy<Logger> logger,
      final Options options) {
    this.elementUtils = elementUtils;
    this.typeUtils = typeUtils;
    this.logger = logger;
    this.options = options;
  }

  final String nameForGeneratedClassFrom(final List<String> enclosingClassSimpleNames) {
    return String.join("", enclosingClassSimpleNames) + "TestAccessors";
  }

  final String[] extractLocation(final Element element) {
    final Element enclosingElement = element.getEnclosingElement();
    final String[] added = enclosingElement == null ? new String[]{} : extractLocation(enclosingElement);
    final String[] current = new String[]{element instanceof PackageElement ? ((PackageElement) element).getQualifiedName().toString() : element.getSimpleName().toString()};
    final String[] ret = new String[added.length + current.length];
    System.arraycopy(added, 0, ret, 0, added.length);
    System.arraycopy(current, 0, ret, added.length, current.length);
    return ret;
  }

  abstract void writeAccessorClass(final Set<Element> annotatedElements, final Filer filer);
}
