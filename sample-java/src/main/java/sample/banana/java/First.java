package sample.banana.java;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import testaccessors.RequiresAccessor;

public final class First<A, B, C, D, E, F, G> {
  @RequiresAccessor
  private final String aField = null;
  private final String anotherTopLevelField = null;

  public static class Second {
    class Third<B> {
      @RequiresAccessor(requires = RequiresAccessor.AccessorType.TYPE_SETTER)
      private final Void yetAnotherField = null;

      class Fourth {
        @RequiresAccessor(requires = RequiresAccessor.AccessorType.TYPE_SETTER)
        private final B yetAnotherField = null;

        class Fifth<A> {
          @RequiresAccessor
          private final Set<A> yetAnotherField = Collections.emptySet();
        }
      }
    }

    class Sixth {
      class Seventh<T, J extends Set<List<?>>, Q extends Collection<? extends T>> {
        @RequiresAccessor(name = "middleFieldThatHasBeenRenamed")
        private final Set<J> anotherField = Collections.emptySet();
      }
    }
  }
}
