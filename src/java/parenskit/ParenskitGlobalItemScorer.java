package parenskit;

import java.util.Collection;
import clojure.lang.IFn;
import javax.annotation.Nonnull;
import org.grouplens.lenskit.basic.AbstractGlobalItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public class ParenskitGlobalItemScorer extends AbstractGlobalItemScorer {
  private final IFn globalScore;

  public ParenskitGlobalItemScorer(IFn globalScore) {
    this.globalScore = globalScore;
  }

  @Override
  public void globalScore(@Nonnull Collection<Long> items,
                          @Nonnull MutableSparseVector scores) {
    globalScore.invoke(items, scores);
  }
}
