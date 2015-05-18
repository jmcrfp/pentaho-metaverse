/*
 * PENTAHO CORPORATION PROPRIETARY AND CONFIDENTIAL
 *
 * Copyright 2002 - 2015 Pentaho Corporation (Pentaho). All rights reserved.
 *
 * NOTICE: All information including source code contained herein is, and
 * remains the sole property of Pentaho and its licensors. The intellectual
 * and technical concepts contained herein are proprietary and confidential
 * to, and are trade secrets of Pentaho and may be covered by U.S. and foreign
 * patents, or patents in process, and are protected by trade secret and
 * copyright laws. The receipt or possession of this source code and/or related
 * information does not convey or imply any rights to reproduce, disclose or
 * distribute its contents, or to manufacture, use, or sell anything that it
 * may describe, in whole or in part. Any reproduction, modification, distribution,
 * or public display of this information without the express written authorization
 * from Pentaho is strictly prohibited and in violation of applicable laws and
 * international treaties. Access to the source code contained herein is strictly
 * prohibited to anyone except those individuals and entities who have executed
 * confidentiality and non-disclosure agreements or other agreements with Pentaho,
 * explicitly covering such access.
 *
 */

package com.pentaho.metaverse.api.analyzer.kettle.step;

import com.pentaho.dictionary.DictionaryConst;
import com.pentaho.metaverse.api.IAnalysisContext;
import com.pentaho.metaverse.api.IMetaverseNode;
import com.pentaho.metaverse.api.MetaverseAnalyzerException;
import com.pentaho.metaverse.api.MetaverseException;
import com.pentaho.metaverse.api.model.IExternalResourceInfo;
import org.apache.commons.collections.MapUtils;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.row.ValueMetaInterface;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public abstract class ExternalResourceStepAnalyzer<T extends BaseStepMeta> extends StepAnalyzer<T> {

  private static final Logger LOGGER = LoggerFactory.getLogger( ExternalResourceStepAnalyzer.class );
  public static final String RESOURCE = "_resource_";

  private IStepExternalResourceConsumer externalResourceConsumer;

  @Override
  protected void customAnalyze( T meta, IMetaverseNode node ) throws MetaverseAnalyzerException {
    // handle all of the external resources
    if ( getExternalResourceConsumer() != null ) {
      Collection<IExternalResourceInfo> resources = getExternalResourceConsumer().getResourcesFromMeta( meta );
      for ( IExternalResourceInfo resource : resources ) {

        try {
          if ( resource.isInput() ) {
            String label = DictionaryConst.LINK_READBY;
            IMetaverseNode fileNode = createResourceNode( resource );
            getMetaverseBuilder().addNode( fileNode );
            getMetaverseBuilder().addLink( fileNode, label, node );
          }
          if ( resource.isOutput() ) {
            String label = DictionaryConst.LINK_WRITESTO;
            IMetaverseNode fileNode = createResourceNode( resource );
            getMetaverseBuilder().addNode( fileNode );
            getMetaverseBuilder().addLink( node, label, fileNode );
          }
        } catch ( MetaverseException e ) {
          LOGGER.error( e.getMessage(), e );
        }
      }
    }
  }

  @Override
  protected Map<String, RowMetaInterface> getOutputRowMetaInterfaces( T meta ) {
    Map<String, RowMetaInterface> outputRows = super.getOutputRowMetaInterfaces( meta );
    if ( MapUtils.isNotEmpty( outputRows ) ) {
      // if this is an output step analyzer, we always need to write the resource fields out
      if ( isOutput() ) {
        RowMetaInterface out = null;
        for ( RowMetaInterface rowMetaInterface : outputRows.values() ) {
          out = rowMetaInterface;
          break;
        }
        outputRows.put( RESOURCE, out );
      }
    }

    return outputRows;
  }

  @Override
  protected Map<String, RowMetaInterface> getInputRowMetaInterfaces( T meta ) {
    Map<String, RowMetaInterface> inputRows = super.getInputRowMetaInterfaces( meta );
    if ( inputRows == null ) {
      inputRows = new HashMap<>();
    }
    // assume that the output fields are defined in the step and are based on the resource inputs
    if ( isInput() ) {
      RowMetaInterface stepFields = getOutputFields( meta );
      inputRows.put( RESOURCE, stepFields );
    }
    return inputRows;
  }

  @Override
  protected IMetaverseNode createOutputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                  String targetStepName, String nodeType ) {

    // if the targetStepName is 'resource' then this is HAS to be a resource field
    nodeType = RESOURCE.equals( targetStepName ) ? getResourceOutputNodeType() : nodeType;
    return super.createOutputFieldNode( context, fieldMeta, targetStepName, nodeType );
  }

  @Override
  protected IMetaverseNode createInputFieldNode( IAnalysisContext context, ValueMetaInterface fieldMeta,
                                                 String previousStepName, String nodeType ) {

    // if the previousStepName is 'resource' then this is HAS to be a resource field
    boolean isResource = RESOURCE.equals( previousStepName );
    nodeType = isResource ? getResourceInputNodeType() : nodeType;
    IMetaverseNode inputFieldNode = super.createInputFieldNode( context, fieldMeta, previousStepName, nodeType );
    inputFieldNode.setType( nodeType );
    if ( isResource ) {
      // add the node so it's not virtual
      getMetaverseBuilder().addNode( inputFieldNode );
    }
    return inputFieldNode;
  }

  public IStepExternalResourceConsumer getExternalResourceConsumer() {
    return externalResourceConsumer;
  }

  public void setExternalResourceConsumer( IStepExternalResourceConsumer externalResourceConsumer ) {
    this.externalResourceConsumer = externalResourceConsumer;
  }

  @Override
  protected void linkChangeNodes( IMetaverseNode inputNode, IMetaverseNode outputNode ) {
    // figure out the correct label to add based on the type of nodes we are dealing with
    boolean nodeTypesMatch = inputNode.getType().equals( outputNode.getType() );
    if ( !nodeTypesMatch && ( isInput() || isOutput() ) ) {
      getMetaverseBuilder().addLink( inputNode, DictionaryConst.LINK_POPULATES, outputNode );
    } else {
      getMetaverseBuilder().addLink( inputNode, getInputToOutputLinkLabel(), outputNode );
    }
  }

  public abstract IMetaverseNode createResourceNode( IExternalResourceInfo resource ) throws MetaverseException;
  public abstract String getResourceInputNodeType();
  public abstract String getResourceOutputNodeType();
  public abstract boolean isOutput();
  public abstract boolean isInput();

}
