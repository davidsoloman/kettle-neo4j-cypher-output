package com.neo4j.kettle.steps.graph;


import com.neo4j.model.GraphPropertyType;
import org.apache.commons.lang.StringUtils;
import org.pentaho.di.core.annotations.Step;
import org.pentaho.di.core.database.DatabaseMeta;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.exception.KettleXMLException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.core.xml.XMLHandler;
import org.pentaho.di.repository.ObjectId;
import org.pentaho.di.repository.Repository;
import org.pentaho.di.trans.Trans;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDataInterface;
import org.pentaho.di.trans.step.StepInterface;
import org.pentaho.di.trans.step.StepMeta;
import org.pentaho.di.trans.step.StepMetaInterface;
import org.pentaho.metastore.api.IMetaStore;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

@Step(
  id = "Neo4jGraphOutput",
  name = "Neo4j Graph Output",
  description = "Write to a Neo4j graph using an input field mapping",
  image = "neo4j_logo.svg",
  categoryDescription = "Neo4j"
)
public class GraphOutputMeta extends BaseStepMeta implements StepMetaInterface {

  private String connectionName;
  private String model;
  private String batchSize;
  private List<FieldModelMapping> fieldModelMappings;


 public GraphOutputMeta() {
   super();
   fieldModelMappings = new ArrayList<>();

  }

  @Override public void setDefault() {

  }

  @Override public StepInterface getStep( StepMeta stepMeta, StepDataInterface stepDataInterface, int i, TransMeta transMeta, Trans trans ) {
    return new GraphOutput( stepMeta, stepDataInterface, i, transMeta, trans );
  }

  @Override public StepDataInterface getStepData() {
    return new GraphOutputData();
  }

  @Override public String getDialogClassName() {
    return GraphOutputDialog.class.getName();
  }

  @Override public void getFields( RowMetaInterface rowMeta, String name, RowMetaInterface[] info, StepMeta nextStep, VariableSpace space,
                                   Repository repository, IMetaStore metaStore ) {

   // No output fields for now
  }

  @Override public String getXML() {
    StringBuilder xml = new StringBuilder( );
    xml.append( XMLHandler.addTagValue( "connection", connectionName) );
    xml.append( XMLHandler.addTagValue( "model", model) );
    xml.append( XMLHandler.addTagValue( "batch_size", batchSize) );

    xml.append( XMLHandler.openTag( "mappings") );
    for (FieldModelMapping fieldModelMapping : fieldModelMappings ) {
      xml.append( XMLHandler.openTag( "mapping") );
      xml.append( XMLHandler.addTagValue( "source_field", fieldModelMapping.getField() ) );
      xml.append( XMLHandler.addTagValue( "target_type", ModelTargetType.getCode( fieldModelMapping.getTargetType()) ) );
      xml.append( XMLHandler.addTagValue( "target_name", fieldModelMapping.getTargetName() ) );
      xml.append( XMLHandler.addTagValue( "target_property", fieldModelMapping.getTargetProperty() ) );
      xml.append( XMLHandler.closeTag( "mapping") );
    }
    xml.append( XMLHandler.closeTag( "mappings") );

    return xml.toString();
  }

  @Override public void loadXML( Node stepnode, List<DatabaseMeta> databases, IMetaStore metaStore ) throws KettleXMLException {
    connectionName = XMLHandler.getTagValue( stepnode, "connection" );
    model = XMLHandler.getTagValue( stepnode, "model" );
    batchSize = XMLHandler.getTagValue( stepnode, "batch_size" );

    // Parse parameter mappings
    //
    Node mappingsNode = XMLHandler.getSubNode( stepnode, "mappings" );
    List<Node> mappingNodes = XMLHandler.getNodes( mappingsNode, "mapping" );
    fieldModelMappings = new ArrayList<>();
    for (Node mappingNode : mappingNodes) {
      String field = XMLHandler.getTagValue( mappingNode, "source_field" );
      ModelTargetType targetType = ModelTargetType.parseCode( XMLHandler.getTagValue( mappingNode, "target_type" ) );
      String targetName = XMLHandler.getTagValue( mappingNode, "target_name" );
      String targetProperty = XMLHandler.getTagValue( mappingNode, "target_property" );

      fieldModelMappings.add(new FieldModelMapping( field, targetType, targetName, targetProperty));
    }

    super.loadXML( stepnode, databases, metaStore );
  }

  @Override public void saveRep( Repository rep, IMetaStore metaStore, ObjectId id_transformation, ObjectId id_step ) throws KettleException {
    rep.saveStepAttribute( id_transformation, id_step, "connection", connectionName);
    rep.saveStepAttribute( id_transformation, id_step, "model", model );
    rep.saveStepAttribute( id_transformation, id_step, "batch_size", batchSize);
    for ( int i = 0; i< fieldModelMappings.size(); i++) {
      FieldModelMapping fieldModelMapping = fieldModelMappings.get( i );
      rep.saveStepAttribute( id_transformation, id_step, i, "source_field",  fieldModelMapping.getField());
      rep.saveStepAttribute( id_transformation, id_step, i, "target_type",  ModelTargetType.getCode( fieldModelMapping.getTargetType() ));
      rep.saveStepAttribute( id_transformation, id_step, i, "target_name",  fieldModelMapping.getField());
      rep.saveStepAttribute( id_transformation, id_step, i, "target_property",  fieldModelMapping.getField());
    }

  }

  @Override public void readRep( Repository rep, IMetaStore metaStore, ObjectId id_step, List<DatabaseMeta> databases ) throws KettleException {
    connectionName = rep.getStepAttributeString( id_step, "connection" );
    model = rep.getStepAttributeString( id_step, "model" );
    batchSize = rep.getStepAttributeString( id_step, "batch_size" );
    fieldModelMappings = new ArrayList<>();
    int nrMappings = rep.countNrStepAttributes( id_step, "source_field" );
    for (int i=0;i<nrMappings;i++) {
      String field = rep.getStepAttributeString( id_step, i, "source_field" );
      ModelTargetType targetType = ModelTargetType.parseCode( rep.getStepAttributeString( id_step, i, "target_type" ) );
      String targetName = rep.getStepAttributeString( id_step, i, "target_name" );
      String targetProperty = rep.getStepAttributeString( id_step, i, "target_property" );

      fieldModelMappings.add(new FieldModelMapping( field, targetType, targetName, targetProperty));
    }

  }

  public String getConnectionName() {
    return connectionName;
  }

  public void setConnectionName( String connectionName ) {
    this.connectionName = connectionName;
  }

  public String getModel() {
    return model;
  }

  public void setModel( String model ) {
    this.model = model;
  }

  public List<FieldModelMapping> getFieldModelMappings() {
    return fieldModelMappings;
  }

  public void setFieldModelMappings( List<FieldModelMapping> fieldModelMappings ) {
    this.fieldModelMappings = fieldModelMappings;
  }

  public String getBatchSize() {
    return batchSize;
  }

  public void setBatchSize( String batchSize ) {
    this.batchSize = batchSize;
  }


}
