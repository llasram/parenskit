package parenskit;

import clojure.lang.IFn;
import javax.annotation.Nonnull;
import org.grouplens.lenskit.basic.AbstractItemScorer;
import org.grouplens.lenskit.vectors.MutableSparseVector;

public class ParenskitItemScorer extends AbstractItemScorer {
  private final IFn score;

  public ParenskitItemScorer(IFn score) {
    this.score = score;
  }

  @Override
  public void score(long user, @Nonnull MutableSparseVector scores) {
    if (score instanceof IFn.LOO) {
      ((IFn.LOO) score).invokePrim(user, scores);
    } else {
      score.invoke(user, scores);
    }
  }
}
