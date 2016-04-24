/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.sglover.nlp.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import opennlp.model.AbstractModel;
import opennlp.model.BinaryFileDataReader;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.GenericModelSerializer;

/** A variant of {@link opennlp.tools.util.model.GenericModelSerializer} that
 *  conserves memory by interning the strings read as a part of a model
 *  by using a {@link org.alfresco.services.nlp.models.tamingtext.opennlp.PooledGenericModelReader} to read the model.
 */
public class PooledGenericModelSerializer extends GenericModelSerializer {

  @Override
  public AbstractModel create(InputStream in) throws IOException,
      InvalidFormatException {
    return new PooledGenericModelReader(new BinaryFileDataReader(in)).getModel();
  }
  
  @SuppressWarnings("rawtypes")
  public static void register(Map<String, ArtifactSerializer> factories) {
    factories.put("model", new PooledGenericModelSerializer());
   }
}
