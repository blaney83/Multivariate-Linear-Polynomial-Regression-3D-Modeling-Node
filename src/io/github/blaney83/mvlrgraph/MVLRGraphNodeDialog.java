package io.github.blaney83.mvlrgraph;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.LinkedHashSet;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;

import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DoubleValue;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeSettings;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;
import org.knime.core.node.NotConfigurableException;
import org.knime.core.node.util.ColumnSelectionComboxBox;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterConfiguration;
import org.knime.core.node.util.filter.column.DataColumnSpecFilterPanel;

/**
 * <code>NodeDialog</code> for the "MVLRGraph" Node. This node provides a 3D
 * representation to obtain a visual "closeness of fit" test when provided a
 * co-efficients table output by an upstream multivariate linear regression
 * node.
 * 
 * @author Benjamin Laney
 */
public class MVLRGraphNodeDialog extends NodeDialogPane {

	private final MVLRGraphSettings m_settings = new MVLRGraphSettings();
	
	private static String CFGKEY_COL_FILTER = "colFilter";
	
	@SuppressWarnings("unchecked")
	private final ColumnSelectionComboxBox m_colName = new ColumnSelectionComboxBox(DoubleValue.class);
	
	private final DataColumnSpecFilterPanel m_colSelectionPanel = new DataColumnSpecFilterPanel();
	private JCheckBox m_appendColumn = new JCheckBox();

	private JCheckBox m_showRegModel = new JCheckBox();
	private JCheckBox m_showAllData = new JCheckBox();

	private final JSpinner m_count = new JSpinner(new SpinnerNumberModel(MVLRGraphNodeModel.DEFAULT_COUNT, 1, Integer.MAX_VALUE, 1));
	
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
				if (m_colName.getSelectedItem() != null) {
					m_colSelectionPanel.resetHiding();
					m_colSelectionPanel.hideNames((DataColumnSpec) m_colName.getSelectedItem());
				}

			}
		});

		constraints.insets = new Insets(4, 0, 0, 0);
		constraints.gridy++;
		constraints.gridx = 0;
		constraints.gridwidth = 2;

		panel.add(new JLabel("Select the two independent variables to model"), constraints);
		constraints.gridy++;

		panel.add(m_colSelectionPanel, constraints);
		constraints.gridy++;
		constraints.gridx = 0;

		panel.add(new JSeparator(SwingConstants.HORIZONTAL), constraints);

		constraints.gridwidth = 1;
		constraints.gridy++;
		constraints.gridx = 0;
		panel.add(new JLabel("Append Column of Calculated Values"), constraints);
		constraints.gridx = 1;
		panel.add(m_appendColumn, constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		panel.add(new JLabel("Plot all real data points"), constraints);
		constraints.gridx = 1;
		panel.add(m_showAllData, constraints);
		m_showAllData.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(final ActionEvent e) {

				if (m_showAllData.isSelected()) {
					m_count.setEnabled(false);
				} else {
					m_count.setEnabled(true);
				}

			}
		});

		constraints.gridy++;
		constraints.gridx = 0;
		panel.add(new JLabel("Number of data points to plot"), constraints);
		constraints.gridx = 1;
		panel.add(m_count, constraints);

		constraints.gridy++;
		constraints.gridx = 0;
		panel.add(new JLabel("Display Regression model?"), constraints);
		constraints.gridx = 1;
		panel.add(m_showRegModel, constraints);

		addTab("General", panel);
		
//		future view tab
//		panel = new JPanel(new GridBagLayout());

	}

	
	
	@Override
	protected void loadSettingsFrom(NodeSettingsRO settings, DataTableSpec[] specs) throws NotConfigurableException {
		DataTableSpec tableSpec = specs[MVLRGraphNodeModel.DATA_TABLE_IN_PORT];
		try {
			m_settings.loadSettingsInDialog(settings, tableSpec);
		}catch(Exception e) {
			LinkedHashSet<String> inclSet = new LinkedHashSet<String>();
			for(DataColumnSpec colSpec : tableSpec) {
				if(colSpec.getType().isCompatible(DoubleValue.class)) {
					inclSet.add(colSpec.getName());
				}
			}
			NodeSettings defaultSettings = MVLRGraphSettings.createDefaults(CFGKEY_COL_FILTER, inclSet.toArray(new String[0]), new String[0], false);
			DataColumnSpecFilterConfiguration filterConfig = new DataColumnSpecFilterConfiguration(CFGKEY_COL_FILTER);
			filterConfig.loadConfigurationInDialog(defaultSettings, tableSpec);
			m_settings.getFilterConfiguration().loadConfigurationInDialog(defaultSettings, tableSpec);
		}
		
		m_colName.update(tableSpec, m_settings.getColName());
		m_colSelectionPanel.loadConfiguration(m_settings.getFilterConfiguration(), tableSpec);
		m_colSelectionPanel.resetHiding();
		m_colSelectionPanel.hideNames((DataColumnSpec)m_colName.getSelectedItem());
		m_appendColumn.setSelected(m_settings.getAppendColumn());
		m_count.setValue(m_settings.getCount());
		m_showAllData.setSelected(m_settings.getShowAllData());
		m_showRegModel.setSelected(m_settings.getShowRegModel());
		
	}



	@Override
	protected void saveSettingsTo(NodeSettingsWO settings) throws InvalidSettingsException {
		
		String[] nameArr = new String[2];
		try {
		nameArr = m_colSelectionPanel.getIncludedNamesAsSet().toArray(nameArr);
		} catch(Exception e) {
			throw new InvalidSettingsException("You must have two columns included: first for the X-Axis, second for the Y-Axis."
					+ " Please exclude/include additional columns until there are exactly 2 on the right hand side.");
		}
		
		m_settings.setColName(m_colName.getSelectedColumn());
		m_settings.setXAxisVarColumn(nameArr[0]);
		m_settings.setYAxisVarColumn(nameArr[1]);
		m_colSelectionPanel.saveConfiguration(m_settings.getFilterConfiguration());
		m_settings.setAppendColumn(m_appendColumn.isSelected());
		m_settings.setCount((Integer)m_count.getModel().getValue());
		m_settings.setShowAllData(m_showAllData.isSelected());
		m_settings.setShowRegModel(m_showRegModel.isSelected());
		
		m_settings.saveSettingsTo(settings);

	}
}

// Old default dialog config
//
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

