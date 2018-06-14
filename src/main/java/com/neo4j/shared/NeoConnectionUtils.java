package com.neo4j.shared;

import com.neo4j.core.Neo4jDefaults;
import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.pentaho.di.core.variables.VariableSpace;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.metastore.api.IMetaStore;
import org.pentaho.metastore.persist.MetaStoreFactory;
import org.pentaho.metastore.util.PentahoDefaults;

public class NeoConnectionUtils {
  private static Class<?> PKG = NeoConnectionUtils.class; // for i18n purposes, needed by Translator2!!
  
  private static MetaStoreFactory<NeoConnection> staticFactory;
  public static MetaStoreFactory<NeoConnection> getConnectionFactory(IMetaStore metaStore) {
    if (staticFactory==null) {
      staticFactory = new MetaStoreFactory<>(NeoConnection.class, metaStore, Neo4jDefaults.NAMESPACE );
    }
    return staticFactory;
  }

  public static NeoConnection newConnection( Shell shell, VariableSpace space, MetaStoreFactory<NeoConnection> factory) {
    NeoConnection connection = new NeoConnection(space);
    boolean ok = false;
    while (!ok) {
      NeoConnectionDialog dialog = new NeoConnectionDialog(shell, connection);
      if (dialog.open()) {
        // write to metastore...
        try {
          if (factory.loadElement(connection.getName())!=null) {
            MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_ERROR);
            box.setText(BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ConnectionExists.Title"));
            box.setMessage(BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ConnectionExists.Message"));
            int answer = box.open();      
            if ((answer&SWT.YES)!=0) {
              factory.saveElement(connection);
              ok=true;
            }
          } else {
            factory.saveElement(connection);
            ok=true;
          }
        } catch(Exception exception) {
          new ErrorDialog(shell,
              BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ErrorSavingConnection.Title"),
              BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ErrorSavingConnection.Message"),
              exception);
          return null;
        }
      } else {
        // Cancel
        return null;
      }
    }
    return connection;
  }

  public static void editConnection(Shell shell, VariableSpace space, MetaStoreFactory<NeoConnection> factory, String connectionName) {
    if (StringUtils.isEmpty(connectionName)) {
      return;
    }
    try {
      NeoConnection NeoConnection = factory.loadElement(connectionName);
      if (NeoConnection==null) {
        newConnection(shell, space, factory);
      } else {
        NeoConnectionDialog NeoConnectionDialog = new NeoConnectionDialog(shell, NeoConnection);
        if (NeoConnectionDialog.open()) {
          factory.saveElement(NeoConnection);
        }
      }
    } catch(Exception exception) {
      new ErrorDialog(shell,
          BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ErrorEditingConnection.Title"),
          BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ErrorEditingConnection.Message"),
          exception);
    }
  }

  public static void deleteConnection(Shell shell, MetaStoreFactory<NeoConnection> factory, String connectionName) {
    if (StringUtils.isEmpty(connectionName)) {
      return;
    }
    
    MessageBox box = new MessageBox(shell, SWT.YES | SWT.NO | SWT.ICON_ERROR);
    box.setText(BaseMessages.getString(PKG, "NeoConnectionUtils.DeleteConnectionConfirmation.Title"));
    box.setMessage(BaseMessages.getString(PKG, "NeoConnectionUtils.DeleteConnectionConfirmation.Message", connectionName));
    int answer = box.open();      
    if ((answer&SWT.YES)!=0) {
      try {
        factory.deleteElement(connectionName);
      } catch(Exception exception) {
        new ErrorDialog(shell,
            BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ErrorDeletingConnection.Title"),
            BaseMessages.getString(PKG, "NeoConnectionUtils.Error.ErrorDeletingConnection.Message", connectionName),
            exception);
      }
    }
  }

}
