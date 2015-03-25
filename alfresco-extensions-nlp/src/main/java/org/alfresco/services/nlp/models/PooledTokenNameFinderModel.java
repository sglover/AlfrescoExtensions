/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.nlp.models;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import opennlp.model.AbstractModel;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;

/** A variant of {@link opennlp.tools.namefind.TokenNameFinderModel} that will
 *  use <code>intern()</code> when reading strings from the model files via 
 *  {@link PooledGenericModelSerializer}.
 *  <p>
 *  The assumption here is that there is enough duplication in the strings in
 *  multiple models being loaded that we will benefit from maintaining only a 
 *  single copy of each string.
 *
 */
public class PooledTokenNameFinderModel extends TokenNameFinderModel {
  
  public PooledTokenNameFinderModel(InputStream in) throws IOException,
      InvalidFormatException {
    super(in);
  }
  
  public PooledTokenNameFinderModel(String languageCode,
      AbstractModel nameFinderModel, Map<String,Object> resources,
      Map<String,String> manifestInfoEntries) {
    super(languageCode, nameFinderModel, resources, manifestInfoEntries);
  }
  
  public PooledTokenNameFinderModel(String languageCode,
      AbstractModel nameFinderModel, byte[] generatorDescriptor,
      Map<String,Object> resources, Map<String,String> manifestInfoEntries) {
    super(languageCode, nameFinderModel, generatorDescriptor, resources,
        manifestInfoEntries);
  }

  @SuppressWarnings("rawtypes")
  @Override
  protected void createArtifactSerializers(
      Map<String,ArtifactSerializer> serializers) {
    super.createArtifactSerializers(serializers);
    
    PooledGenericModelSerializer.register(serializers);
  }
}
