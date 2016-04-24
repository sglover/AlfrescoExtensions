package org.alfresco.service.common.elasticsearch.repoquery;

import java.io.IOException;

import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.search.ConstantScoreScorer;
import org.apache.lucene.search.ConstantScoreWeight;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Weight;

/**
 * 
 * @author sglover
 *
 */
public abstract class RepoQuery extends Query
{
    private String username;

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result
                + ((username == null) ? 0 : username.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
        RepoQuery other = (RepoQuery) obj;
        if (username == null) {
            if (other.username != null)
                return false;
        } else if (!username.equals(other.username))
            return false;
        return true;
    }


    @Override
    public String toString(String field) {
        // TODO Auto-generated method stub
        return null;
    }

    protected abstract DocIdSet getDocIdSet(LeafReaderContext context) throws IOException;

    @Override
    public Weight createWeight(IndexSearcher searcher, boolean needsScores) throws IOException {
      return new ConstantScoreWeight(this) {
        @Override
        public Scorer scorer(LeafReaderContext context) throws IOException {
          DocIdSet docSet = getDocIdSet(context);
          if (docSet == null) {
            return null;
          }
          DocIdSetIterator disi = docSet.iterator();
          if (disi == null) {
            return null;
          }
          return new ConstantScoreScorer(this, score(), disi);
        }
      };
    }
}
