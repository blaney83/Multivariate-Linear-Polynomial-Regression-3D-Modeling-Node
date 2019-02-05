package io.github.blaney83.mvlrgraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.base.node.preproc.joiner.ColumnSpecListRenderer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.RowKey;
import org.knime.core.data.StringValue;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;
import org.knime.core.node.defaultnodesettings.SettingsModelString;
import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.NodeLogger;
import org.knime.core.node.NodeModel;
import org.knime.core.node.NodeSettingsRO;
import org.knime.core.node.NodeSettingsWO;

/**
 * This is the model implementation of MVLRGraph. This node provides a 3D
 * representation to obtain a visual "closeness of fit" test when provided a
 * co-efficients table output by an upstream multivariate linear regression
 * node.
 *
 * @author Benjamin Laney
 */
public class MVLRGraphNodeModel extends NodeModel {

	// inport fields
	public static final int COEFFICIENT_IN_PORT = 0;
	public static final int DATA_TABLE_IN_PORT = 1;

	// the logger instance
	private static final NodeLogger logger = NodeLogger.getLogger(MVLRGraphNodeModel.class);

	// internal settings fields
	static final String CFGKEY_COUNT = "count";
	static final String CFGKEY_COL_NAME = "columnName";
	static final String CFGKEY_APPEND_CALCULATED_TARGET = "calculatedTarget";
	static final String CFGKEY_APPROXIMATED_COLUMN_NAME = "APPROXCOL"; 
	static final String CFGKEY_NUM_APPROX_COLUMN = "numApproxColumns";
	// if R^k=>R where k>2, then let them pick which columns to assign as mean
	// values (leaving 2 columns for x and y), with z = R
	// give options for mean values as the coefficients table displayed with the
	// coefficient (to help pick less significant values)
	// future: MAYBE append new column with calculated values for each row
	// NOTE: recommend Laplace evaluation (to derive larger coefficients and more
	// values with 0 as coefficient)
	// view options
	// color rainbow- under, on, over
	// opacity of planar field

	// default settings fields
	private static final int DEFAULT_COUNT = 100;
	private static final String DEFAULT_COLUMN = "Target Column";
	private static final boolean DEFAULT_APPEND_CALCULATED_TARGET = false;
	private static final int DEFAULT_NUM_APPROX_COLUMN = 0;
	// settings model
	private final SettingsModelIntegerBounded m_count = new SettingsModelIntegerBounded(MVLRGraphNodeModel.CFGKEY_COUNT,
			MVLRGraphNodeModel.DEFAULT_COUNT, 0, Integer.MAX_VALUE);
	private final SettingsModelString m_colName = new SettingsModelString(CFGKEY_COL_NAME, DEFAULT_COLUMN);
	private final SettingsModelBoolean m_appendCalculatedTarget = new SettingsModelBoolean(
			CFGKEY_APPEND_CALCULATED_TARGET, DEFAULT_APPEND_CALCULATED_TARGET);
	private final SettingsModelIntegerBounded m_numApproxColumns = new SettingsModelIntegerBounded(CFGKEY_NUM_APPROX_COLUMN, DEFAULT_NUM_APPROX_COLUMN, 0, Integer.MAX_VALUE);
	// local model fields
//	private DataPoint[] m_dataPoints;
	// private RegressionFunction m_regFn;
	protected DataType m_targetType;
	protected String[] m_approxColNameArr;
	protected String m_variableColumnName;
	protected String m_coeffColumnName;
	

	protected MVLRGraphNodeModel() {
		// two in-ports, one out-port
		super(2, 1);
	}

	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		//finish validation
		//then build a formula factory
		//that returns an array of points
		//then build the cell factory (maybe in the formula factory, or maybe the formula factory returns itself and it has 
		//the array of points, the formula itself, and the column to append, it necessary)
		inData[DATA_TABLE_IN_PORT].g
		// TODO do something here
//        logger.info("Node Model Stub... this is not yet implemented !");
//
		return new BufferedDataTable[] { out };
	}

	@Override
	protected void reset() {
//		if (m_regFn != null) {
//			m_regFn = null;
//		}
//		if (m_dataPoints != null) {
//			m_dataPoints = null;
//		}
	}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (inSpecs[COEFFICIENT_IN_PORT] == null || inSpecs[DATA_TABLE_IN_PORT] == null) {
			throw new InvalidSettingsException(
					"Please provide a regression coefficient table to In-Port #1 and a data table to In-Port #2.");
		} else {
			if (!inSpecs[COEFFICIENT_IN_PORT].containsCompatibleType(StringValue.class)
					&& !inSpecs[COEFFICIENT_IN_PORT].containsCompatibleType(DoubleValue.class)) {
				throw new InvalidSettingsException(
						"The coefficient table provided does not meet the requirements for this node. It should contain at least a column containing the row keys (variables of the regression equation) in the associated data column and a numeric coefficients column.");
			}
			// checking input ind. 0
			boolean hasCoefficientColumn = false;
			boolean hasVariableColumn = false;
			String[] coeffTableColumns = inSpecs[COEFFICIENT_IN_PORT].getColumnNames();
			// to be taken out if node expanded for polynomial regression
			if (coeffTableColumns != null && coeffTableColumns.length > 1) {
				for (String colName : coeffTableColumns) {
					if (colName.toLowerCase().contains("exponent")) {
						throw new InvalidSettingsException(
								"The function you are trying to graph appears to be a polynomial regression line. Please use only linear regression for this node.");
					}
					// alternately check for "coeff."
					if (colName.toLowerCase().trim().contains("coeff") && inSpecs[COEFFICIENT_IN_PORT]
							.getColumnSpec(colName).getType().isCompatible(DoubleValue.class)) {
						m_coeffColumnName = colName;
						hasCoefficientColumn = true;
					}
					if (colName.toLowerCase().trim().equals("variable") && inSpecs[COEFFICIENT_IN_PORT]
							.getColumnSpec(colName).getType().isCompatible(StringValue.class)) {
						m_variableColumnName = colName;
						hasVariableColumn = true;
					}
				}
			} else if (coeffTableColumns != null && // checking for H20 regression output
					coeffTableColumns.length == 1 && coeffTableColumns[0].toLowerCase().trim() == "beta"
					&& inSpecs[COEFFICIENT_IN_PORT].getColumnSpec(coeffTableColumns[0]).getType()
							.isCompatible(DoubleValue.class)) {
				m_coeffColumnName = coeffTableColumns[0];
				hasCoefficientColumn = true;
			}
			if (!hasCoefficientColumn) {
				throw new InvalidSettingsException(
						"Cannot find coefficient's from the provided table. Please make sure the coefficient table is connected to the top In-Port (0) and that the column containing coefficients is correctly named 'Coeff.'");
			}
			if (!hasVariableColumn) {
				throw new InvalidSettingsException(
						"Cannot find coefficient variables from the provided table. Please make sure the coefficient table is connected to the top In-Port (0) and that the column containing the associated column names from the data table (bottom In-Port (1)) is correctly named 'Variable'");
			}
			// checking input ind 1
			boolean hasCompatibleDataTableFormat = false;
			boolean containsTargetColumn = false;
			int numNumericColumns = 0;
			for (int i = 0; i < inSpecs[DATA_TABLE_IN_PORT].getNumColumns(); i++) {
				DataColumnSpec columnSpec = inSpecs[DATA_TABLE_IN_PORT].getColumnSpec(i);
				if (columnSpec.getType().isCompatible(DoubleValue.class)) {
					numNumericColumns++;
				}
				if (m_colName != null && columnSpec.getName().contentEquals(m_colName.getStringValue())) {
					m_targetType = columnSpec.getType();
					containsTargetColumn = true;
				}
			}
			if (numNumericColumns < 3) {
				throw new InvalidSettingsException(
						"The data table provided must contain 2 or more numeric columns which correspond to the variables in the regression equation and 1 numeric target column (same as was selected during regression learning).");
			}
			//for R^k=>R, where k > 2
			//may run into issues when not all numeric columns were used in the regression learner (fix later)
			else if(numNumericColumns-m_numApproxColumns.getIntValue() > 3) {
				throw new InvalidSettingsException("Please reduce the number of calculated columns in the selection area to 2 by transfering additional variables to the 'Approximate with mean value' category.");
			}

			if (m_appendCalculatedTarget.getBooleanValue()) {
				DataColumnSpec calcValsColumnSpec = createCalcValsOutputColumnSpec();
				DataTableSpec appendSpec = new DataTableSpec(calcValsColumnSpec);
				DataTableSpec outputSpec = new DataTableSpec(inSpecs[DATA_TABLE_IN_PORT], appendSpec);
				return new DataTableSpec[] { outputSpec };
			} else {
				return new DataTableSpec[] { inSpecs[DATA_TABLE_IN_PORT] };
			}
		}
	}

	private DataColumnSpec createCalcValsOutputColumnSpec() {
		DataColumnSpecCreator newColSpecCreator = new DataColumnSpecCreator("Calculated " + m_colName, m_targetType);
		DataColumnSpec newColSpec = newColSpecCreator.createSpec();
		return newColSpec;
	}
	
	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_count.saveSettingsTo(settings);
		m_colName.saveSettingsTo(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_count.loadSettingsFrom(settings);
		m_colName.loadSettingsFrom(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_count.validateSettings(settings);
		m_colName.validateSettings(settings);
	}

	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {

	}

}
