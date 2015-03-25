/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.semantics;

import java.io.IOException;
import java.net.URL;

import org.alfresco.nlp.Entities;
import org.alfresco.nlp.Entity;
import org.alfresco.nlp.EntityTagger;
import org.alfresco.nlp.EntityTaggerCallback;
import org.alfresco.semantics.dropwizard.resources.NamedEntity;

/**
 * 
 * @author sglover
 *
 */
public class SemanticsProcessor
{
	private EntityTagger entityTagger;
	private MongoSemantics semantics;

	public SemanticsProcessor(EntityTagger entityTagger,
            MongoSemantics semantics)
    {
	    super();
	    this.entityTagger = entityTagger;
	    this.semantics = semantics;
    }

	public void process(NamedEntity namedEntity) throws IOException
	{
		final String name = namedEntity.getName();
		String urlString = namedEntity.getUrl();
		URL url = new URL(urlString);

		EntityTaggerCallback callback = new EntityTaggerCallback()
        {
            
            @Override
            public void onSuccess(Entities entities)
            {
                for(Entity<String> entity : entities.getNames())
                {
                    String toName = entity.getEntity();
                    String category = "name";
                    semantics.addRelation(name, toName, category);
                }

                for(Entity<String> entity : entities.getOrgs())
                {
                    String toName = entity.getEntity();
                    String category = "org";
                    semantics.addRelation(name, toName, category);
                }

                for(Entity<String> entity : entities.getLocations())
                {
                    String toName = entity.getEntity();
                    String category = "location";
                    semantics.addRelation(name, toName, category);
                }

                for(Entity<String> entity : entities.getMisc())
                {
                    String toName = entity.getEntity();
                    String category = "misc";
                    semantics.addRelation(name, toName, category);
                }

                for(Entity<String> entity : entities.getMoney())
                {
                    String toName = entity.getEntity();
                    String category = "money";
                    semantics.addRelation(name, toName, category);
                }
            }
            
            @Override
            public void onFailure(Throwable ex) 
            {
                // TODO Auto-generated method stub
                
            }
        };

		entityTagger.getEntities(url, callback);
	}
}
