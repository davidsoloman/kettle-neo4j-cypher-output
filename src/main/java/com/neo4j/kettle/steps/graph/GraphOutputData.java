package com.neo4j.kettle.steps.graph;

import com.neo4j.model.GraphModel;
import com.neo4j.shared.NeoConnection;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.Transaction;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.step.BaseStepData;
import org.pentaho.di.trans.step.StepDataInterface;

public class GraphOutputData extends BaseStepData implements StepDataInterface {

  public RowMetaInterface outputRowMeta;
  public NeoConnection neoConnection;
  public String url;
  public Driver driver;
  public Session session;
  public int[] fieldIndexes;
  public long batchSize;
  public Transaction transaction;
  public long outputCount;
  public boolean hasInput;
  public GraphModel graphModel;
  public int nodeCount;
}
