/*
 * Copyright 2015 Alfresco Software, Ltd.  All rights reserved.
 *
 * License rights for this program may be obtained from Alfresco Software, Ltd. 
 * pursuant to a written agreement and any use of this program without such an 
 * agreement is prohibited. 
 */
package org.alfresco.services.solr;

import org.alfresco.repo.dictionary.M2Model;

/**
 * Represents an alfresco model and checksum.
 * 
 * @since 4.0
 */
public class AlfrescoModel
{
    private M2Model model;
    private Long checksum;
    
    public AlfrescoModel(M2Model model, Long checksum)
    {
        this.model = model;
        this.checksum = checksum;
    }

    public M2Model getModel()
    {
        return model;
    }

    public Long getChecksum()
    {
        return checksum;
    }
    
    public boolean equals(Object other)
    {
        if (this == other)
        {
            return true;
        }

        if(!(other instanceof AlfrescoModel))
        {
            return false;
        }

        AlfrescoModel model = (AlfrescoModel)other;
        return (this.model.getName().equals(model.getModel().getName()) &&
                checksum.equals(model.getChecksum()));
    }

    public int hashcode()
    {
        int result = 17;
        result = 31 * result + model.hashCode();
        result = 31 * result + Long.valueOf(checksum).hashCode();
        return result;
    }

//    private ModelDefinition modelDef;
//    private long checksum;
//
//    protected AlfrescoModel(ModelDefinition modelDef)
//    {
//        this.modelDef = modelDef;
//        this.checksum = modelDef.getChecksum(ModelDefinition.XMLBindingType.DEFAULT);
//    }
//
//    public ModelDefinition getModelDef()
//    {
//        return modelDef;
//    }
//
//    public long getChecksum()
//    {
//        return checksum;
//    }
//    
//    @Override
//    public boolean equals(Object other)
//    {
//        if (this == other)
//        {
//            return true;
//        }
//
//        if(!(other instanceof AlfrescoModel))
//        {
//            return false;
//        }
//
//        AlfrescoModel model = (AlfrescoModel)other;
//        return (modelDef.getName().equals(model.getModelDef().getName()) &&
//                checksum == model.getChecksum());
//    }
//
//    @Override
//    public int hashCode()
//    {
//        int result = 17;
//        result = 31 * result + modelDef.hashCode();
//        result = 31 * result + Long.valueOf(checksum).hashCode();
//        return result;
//    }
}
