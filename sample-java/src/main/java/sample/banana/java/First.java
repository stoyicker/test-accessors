package sample.banana.java;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import testaccessors.RequiresAccessor;
import testaccessors.RequiresAccessor.AccessorType;

public final class First<A, B, C, D, E, F, G> {
  @RequiresAccessor(requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER})
  private final String aField = null;
  private final String anotherTopLevelField = null;

  public static class Second {
    class Third<B> {
      @RequiresAccessor(requires = RequiresAccessor.AccessorType.TYPE_SETTER)
      private final String yetAnotherField = null;

      class Fourth {
        @RequiresAccessor(requires = RequiresAccessor.AccessorType.TYPE_SETTER)
        private final B yetAnotherField = null;
        @RequiresAccessor(requires = RequiresAccessor.AccessorType.TYPE_SETTER)
        private final Map<B, Set<B>> nestedTypeField = null;

        class Fifth<A> {
          @RequiresAccessor(requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER})
          private final Set<A> yetAnotherField = Collections.emptySet();
        }
      }
    }

    class Sixth {
      class Seventh<T, J extends Set<List<?>>, Q extends Collection<? extends T>> {
        @RequiresAccessor(name = "fieldThatHasBeenRenamed", requires = {RequiresAccessor.AccessorType.TYPE_GETTER, RequiresAccessor.AccessorType.TYPE_SETTER})
        private final Set<J> anotherField = Collections.emptySet();
        @RequiresAccessor(requires = {AccessorType.TYPE_GETTER, AccessorType.TYPE_SETTER})
        private final Map<? super T, Map<? extends J, ? super Q>> fieldWithWildcardTypes = Collections.emptyMap();
      }
    }
  }
}
