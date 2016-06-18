/*
 * Copyright 2016 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.entities.dao.titan;

import java.util.List;

import org.sglover.alfrescoextensions.common.Node;
import org.sglover.entities.dao.SimilarityDAO;
import org.sglover.entities.values.Similarity;
import org.springframework.stereotype.Component;

/**
 * 
 * @author sglover
 *
 */
@Component(value="titanSimilarityDAO")
public class TitanSimilarityDAO implements SimilarityDAO
{

    @Override
    public void saveSimilarity(Node node1, Node node2, double similarity)
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public double getSimilarity(Node node1, Node node2)
    {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Similarity> getSimilar(Node node)
    {
        // TODO Auto-generated method stub
        return null;
    }

}
