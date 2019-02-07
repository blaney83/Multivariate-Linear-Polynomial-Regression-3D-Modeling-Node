package io.github.blaney83.mvlrgraph;

import org.knime.core.data.DoubleValue;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "MVLRGraph" Node.
 * This node provides a 3D representation to obtain a visual "closeness of fit" test when provided a co-efficients table output by an upstream multivariate linear regression node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Benjamin Laney
 */
public class MVLRGraphNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring MVLRGraph node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
	
	//next to do:
	//set up this dialog w/ options
	//create view panel (probably border pane first)
	//then create internal graph (accessing fn set and tar, x and y columns to get domain)
	//then create scatter graph
	//set up repainting etc.
	//add hiliting
	//finish .xml options
	//test and debug
	
	@SuppressWarnings("unchecked")
    protected MVLRGraphNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentColumnNameSelection(
        		new SettingsModelColumnName(MVLRGraphNodeModel.CFGKEY_COL_NAME, ""),
        		"Select the target column used in the Regression Learner Node (z-axis): ",
        		MVLRGraphNodeModel.DATA_TABLE_IN_PORT, DoubleValue.class));
        
        addDialogComponent(new DialogComponentColumnNameSelection(
        		new SettingsModelColumnName(MVLRGraphNodeModel.CFGKEY_X_AXIS_VAR_COLUMN, ""),
        		"Select the first of two variables to plotted on the regression plane (x-axis): ",
        		MVLRGraphNodeModel.DATA_TABLE_IN_PORT, DoubleValue.class));
        
        addDialogComponent(new DialogComponentColumnNameSelection(
        		new SettingsModelColumnName(MVLRGraphNodeModel.CFGKEY_Y_AXIS_VAR_COLUMN, ""),
        		"Select the second of two variables to plotted on the regression plane (y-axis): ",
        		MVLRGraphNodeModel.DATA_TABLE_IN_PORT, DoubleValue.class));
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    MVLRGraphNodeModel.CFGKEY_COUNT,
                    MVLRGraphNodeModel.DEFAULT_COUNT,
                    1, Integer.MAX_VALUE),
                    "Number of rows to plot over regression plane: ",1,5));
        
        addDialogComponent(new DialogComponentBoolean(
        		new SettingsModelBoolean(
        				MVLRGraphNodeModel.CFGKEY_APPEND_CALCULATED_TARGET,
        				MVLRGraphNodeModel.DEFAULT_APPEND_CALCULATED_TARGET), 
        				"Append new column with calculated values?"));
        
        addDialogComponent(new DialogComponentBoolean(
        		new SettingsModelBoolean(
        				MVLRGraphNodeModel.CFGKEY_IS_H2O_NODE,
        				MVLRGraphNodeModel.DEFAULT_IS_H2O_NODE), 
        				"Are you using an H2O learner?"));
                    
    }
}

