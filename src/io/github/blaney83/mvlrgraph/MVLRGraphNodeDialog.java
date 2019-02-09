package io.github.blaney83.mvlrgraph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentBoolean;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnFilter2;
import org.knime.core.node.defaultnodesettings.DialogComponentColumnNameSelection;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnFilter2;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.filter.InputFilter;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;
import org.knime.core.node.util.filter.column.DataTypeColumnFilter;

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
public class MVLRGraphNodeDialog extends NodeDialogPane {
	
	 @SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox m_colName =
		        new ColumnSelectionComboxBox(DoubleValue.class);
	
	 
	 private final JSpinner m_count = new JSpinner(new SpinnerNumberModel(MVLRGraphNodeModel.DEFAULT_COUNT, 1,
	            10, 1));
	 
	    @SuppressWarnings("unchecked")
	    private final DataTypeColumnFilter m_colSelectionPanel = new DataTypeColumnFilter(DoubleValue.class);
	    private JRadioButton m_appendColumn;
	    
	    private JRadioButton m_showRegModel;
	    private JRadioButton m_showAllData;


	 
	 
	@SuppressWarnings("unchecked")
    protected MVLRGraphNodeDialog() {
		
		JPanel panel = new JPanel(new GridBagLayout());
		
		GridBagConstraints constraints = new GridBagConstraints();
		
		constraints.anchor = GridBagConstraints.NORTHWEST;
		constraints.gridx = 0;
		constraints.gridy = 0;
		
		panel.add(new JLabel("Target Column (same as in Regression Learner; z-axis)"), constraints);
		constraints.gridx = 1;
		panel.add(m_colName);
		m_colName.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				if(m_colName.getSelectedItem() != null) {
//					m_colSelectionPanel.resetHiding();
//					m_colSelectionPanel.hideNames((DataColumnSpec) m_colName.getSelectedItem());
				}
				
			}
		});
		
		constraints.insets = new Insets(4, 0, 0, 0);
		constraints.gridy ++;
		constraints.gridx = 0;
		constraints.gridwidth = 2;
		
		panel.add(new JLabel("Select the two independent variables to model (x, y axes)"), constraints);
		constraints.gridy ++;
		
		panel.add(m_colSelectionPanel, constraints);
		constraints.gridy ++;
		constraints.gridx = 0;
		
		panel.add(new JSeparator(SwingConstants.HORIZONTAL), constraints);
		
		constraints.gridwidth = 1;
		constraints.gridy ++;
		constraints.gridx = 0;
		panel.add(new JLabel("Append Column of Calculated Values?"), constraints);
		constraints.gridx = 1;
		panel.add(m_appendColumn, constraints);
		
		constraints.gridy ++;
		constraints.gridx = 0;
		panel.add(new JLabel("Plot all real data points?"), constraints);
		constraints.gridx = 1;
		panel.add(m_showAllData, constraints);
		m_showAllData.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				
				if(m_showAllData.isSelected()) {
					m_count.setEnabled(false);
				}else {
					m_count.setEnabled(true);
				}
				
			}
		});
    }

	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		// TODO Auto-generated method stub
		
	}
}

//addDialogComponent(new DialogComponentColumnNameSelection(
//		new SettingsModelColumnName(MVLRGraphNodeModel.CFGKEY_COL_NAME, ""),
//		"Select the target column used in the Regression Learner Node (z-axis): ",
//		MVLRGraphNodeModel.DATA_TABLE_IN_PORT, DoubleValue.class));
//
//
//DialogComponentColumnFilter2 dialogCompFilter2 = new DialogComponentColumnFilter2(
//		new SettingsModelColumnFilter2(MVLRGraphNodeModel.CFGKEY_COL_FILTER, DoubleValue.class), MVLRGraphNodeModel.DATA_TABLE_IN_PORT);
//dialogCompFilter2.setIncludeTitle("Model Variables (max 2)");
//dialogCompFilter2.setExcludeTitle("Excluded from Model/Use Mean Value Approx.");
//addDialogComponent(dialogCompFilter2);
//
//addDialogComponent(new DialogComponentColumnNameSelection(
//		new SettingsModelColumnName(MVLRGraphNodeModel.CFGKEY_X_AXIS_VAR_COLUMN, ""),
//		"Select the first of two variables to plotted on the regression plane (x-axis): ",
//		MVLRGraphNodeModel.DATA_TABLE_IN_PORT, DoubleValue.class));
//
//addDialogComponent(new DialogComponentColumnNameSelection(
//		new SettingsModelColumnName(MVLRGraphNodeModel.CFGKEY_Y_AXIS_VAR_COLUMN, ""),
//		"Select the second of two variables to plotted on the regression plane (y-axis): ",
//		MVLRGraphNodeModel.DATA_TABLE_IN_PORT, DoubleValue.class));
//
//addDialogComponent(new DialogComponentNumber(
//        new SettingsModelIntegerBounded(
//            MVLRGraphNodeModel.CFGKEY_COUNT,
//            MVLRGraphNodeModel.DEFAULT_COUNT,
//            1, Integer.MAX_VALUE),
//            "Number of rows to plot over regression plane: ",1,5));
//
//addDialogComponent(new DialogComponentBoolean(
//		new SettingsModelBoolean(
//				MVLRGraphNodeModel.CFGKEY_SHOW_ALL,
//				MVLRGraphNodeModel.DEFAULT_SHOW_ALL_DATA), 
//				"Plot all real data points?"));
//
//addDialogComponent(new DialogComponentBoolean(
//		new SettingsModelBoolean(
//				MVLRGraphNodeModel.CFGKEY_SHOW_REG_MODEL,
//				MVLRGraphNodeModel.DEFAULT_SHOW_REG_MODEL), 
//				"Graph regression model? (no will just show data points)"));
//
//addDialogComponent(new DialogComponentBoolean(
//		new SettingsModelBoolean(
//				MVLRGraphNodeModel.CFGKEY_APPEND_CALCULATED_TARGET,
//				MVLRGraphNodeModel.DEFAULT_APPEND_CALCULATED_TARGET), 
//				"Append new column with calculated values?"));
//
//
//addDialogComponent(new DialogComponentBoolean(
//		new SettingsModelBoolean(
//				MVLRGraphNodeModel.CFGKEY_IS_H2O_NODE,
//				MVLRGraphNodeModel.DEFAULT_IS_H2O_NODE), 
//				"Are you using an H2O learner?"));
//            

