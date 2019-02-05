package io.github.blaney83.mvlrgraph;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knime.base.node.io.filereader.InterruptedExecutionException;
import org.knime.base.node.preproc.joiner.ColumnSpecListRenderer;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DomainCreatorColumnSelection;
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
	static final String CFGKEY_X_AXIS_VAR_COLUMN = "xAxisVarColumn";
	static final String CFGKEY_Y_AXIS_VAR_COLUMN = "yAxisVarColumn";
	static final String CFGKEY_APPEND_CALCULATED_TARGET = "calculatedTarget";
	static final String CFGKEY_APPROXIMATED_COLUMN_NAME = "APPROXCOL";
	static final String CFGKEY_NUM_APPROX_COLUMN = "numApproxColumns";
	static final String CFGKEY_IS_H2O_NODE = "isH2ONode";
	// enum for intercept?

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
	private static final boolean DEFAULT_APPEND_CALCULATED_TARGET = false;
	private static final int DEFAULT_NUM_APPROX_COLUMN = 0;
	private static final boolean DEFAULT_IS_H2O_NODE = false;
	// settings model
	private final SettingsModelIntegerBounded m_count = new SettingsModelIntegerBounded(MVLRGraphNodeModel.CFGKEY_COUNT,
			MVLRGraphNodeModel.DEFAULT_COUNT, 0, Integer.MAX_VALUE);
	private final SettingsModelString m_colName = new SettingsModelString(CFGKEY_COL_NAME, "");
	private final SettingsModelString m_xAxisVarColumn = new SettingsModelString(CFGKEY_X_AXIS_VAR_COLUMN, "");
	private final SettingsModelString m_yAxisVarColumn = new SettingsModelString(CFGKEY_Y_AXIS_VAR_COLUMN, "");
	private final SettingsModelBoolean m_appendCalculatedTarget = new SettingsModelBoolean(
			CFGKEY_APPEND_CALCULATED_TARGET, DEFAULT_APPEND_CALCULATED_TARGET);
	private final SettingsModelIntegerBounded m_numApproxColumns = new SettingsModelIntegerBounded(
			CFGKEY_NUM_APPROX_COLUMN, DEFAULT_NUM_APPROX_COLUMN, 0, Integer.MAX_VALUE);
	private final SettingsModelBoolean m_isH2ONode = new SettingsModelBoolean(CFGKEY_IS_H2O_NODE, DEFAULT_IS_H2O_NODE);

	// local model fields
//	private DataPoint[] m_dataPoints;
	// private RegressionFunction m_regFn;
	protected Set<FunctionTerm> m_termSet;
	protected DataType m_targetType;
	protected List<String> m_variableColNames;
	protected List<String> m_dataTableColumnNames;
	protected List<String> m_approxColNameArr;
	protected int m_inPort1VariableColumnIndex;
	protected int m_inPort1CoeffColumnIndex;
	protected int m_inPort1ExponentIndex = -1;

	protected MVLRGraphNodeModel() {
		// two in-ports, one out-port
		super(2, 1);
	}

	// view needs
	// switch box for columns that exlude the target column and add columns to the
	// m_approxColNameArr
	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {
		// finish validation
		//
		// at this point we are assuming that m_variableColNames.length -
		// m_approxColNameArr = 2
		//
		// determine domain for each variable (also get the mean if the variable is
		// contained in the m_approxColNameArr
		// and also gets the domain of the target column?)
		//
		// then build a formula factory
		// that returns an array of points
		// then build the cell factory (maybe in the formula factory, or maybe the
		// formula factory returns itself and it has
		// the array of points, the formula itself, and the column to append, if
		// necessary)
		
		//CURRENT STOPPING POINT- need to create the column appender, etc., if append = true
		//use the Calculated point zValue in the loop to create the values for the new cells
		//then set a buffered data table = the input and conditionally reassign it to the new
		//column appender before returning it to finish out the execute method.
		// may need to create some global storage for some of the information I created 
		//(such as the calcpointArr so the view can see it and maybe a global formula, holidng
		//the domains for the x,y,z and the function for the graph mapping
		BufferedDataTable coeffTable = inData[COEFFICIENT_IN_PORT];
		for (DataRow row : coeffTable) {
			m_termSet.add(validateCoeffVariables(row));
		}
		BufferedDataTable dataTable = inData[DATA_TABLE_IN_PORT];
		for (FunctionTerm fnTerm : m_termSet) {
			processColumn(dataTable, fnTerm);
		}

		if (m_count.getIntValue() > dataTable.size() || m_appendCalculatedTarget.getBooleanValue()) {
			m_count.setIntValue((int) dataTable.size());
		}
		CalculatedPoint[] calcPoints = new CalculatedPoint[m_count.getIntValue()];
		int iterations = 0;
		for (DataRow dataRow : dataTable) {
			calcPoints[iterations] = pointFactory(dataRow, dataTable.getDataTableSpec());
			if (m_appendCalculatedTarget.getBooleanValue()) {
				calcPoints[iterations].getZValue();
			}
			if (iterations >= m_count.getIntValue()) {
				break;
			}
			iterations++;
		}

//		Set<FunctionTerm> fnVarSet = new LinkedHashSet<FunctionTerm>();
//		fnVarSet.addAll(m_termSet);

		// TODO do something here
//        logger.info("Node Model Stub... this is not yet implemented !");
//
		return new BufferedDataTable[] { out };
	}

	private FunctionTerm validateCoeffVariables(final DataRow dataRow) throws InterruptedExecutionException {
		if (dataRow != null) {
			double coeff = Double.parseDouble(dataRow.getCell(m_inPort1CoeffColumnIndex).toString());
			int exponent = 1;
			if (m_inPort1ExponentIndex != -1) {
				exponent = Integer.parseInt(dataRow.getCell(m_inPort1ExponentIndex).toString());
			}
			if (!m_isH2ONode.getBooleanValue()) {
				String varName = dataRow.getCell(m_inPort1VariableColumnIndex).toString();
				if (m_dataTableColumnNames.contains(varName) || varName.toLowerCase().trim() == "intercept") {
					m_variableColNames.add(varName);
					return new FunctionTerm(varName, coeff, exponent);
				}
			} else {
				String rowKeyValue = dataRow.getKey().getString();
				if (m_dataTableColumnNames.contains(rowKeyValue) || rowKeyValue.toLowerCase().trim() == "intercept") {
					m_variableColNames.add(rowKeyValue);
					return new FunctionTerm(rowKeyValue, coeff, exponent);
				}
			}
			throw new InterruptedExecutionException(
					"The coefficient table you provided does not match with the columns of the data table. Please ensure that the column names and variables fields have not been altered by a previous node.");
		}
		return new FunctionTerm();
	}

	// may switch to iterating through columns 10 times for every 1000 rows, instead
	// of
	// 1000 rows for each of 10 columns
	private void processColumn(final BufferedDataTable input, final FunctionTerm fnTerm) {
		String colName = fnTerm.getVarName();
		int colIndex = input.getDataTableSpec().findColumnIndex(colName);

		double meanSum = 0;
		double lowerBound = Double.MAX_VALUE;
		double upperBound = Double.MIN_VALUE;
		int totalRows = 0;
		for (DataRow row : input) {
			DataCell cell = row.getCell(colIndex);
			double value = ((DoubleValue) cell).getDoubleValue();
			meanSum += value;
			lowerBound = Math.min(lowerBound, value);
			upperBound = Math.max(upperBound, value);

			totalRows++;
		}
		DataColumnDomainCreator colDomainCreator = new DataColumnDomainCreator();
		colDomainCreator.setLowerBound(new DoubleCell(lowerBound));
		colDomainCreator.setUpperBound(new DoubleCell(upperBound));
		fnTerm.setDomain(colDomainCreator.createDomain());
		if (colName != m_colName.getStringValue() && colName != m_xAxisVarColumn.getStringValue()
				&& colName != m_yAxisVarColumn.getStringValue()) {
			fnTerm.setValue(meanSum / totalRows);
		}
	}

	private CalculatedPoint pointFactory(final DataRow dataRow, final DataTableSpec tableSpec) {
		double xValue = 0;
		double yValue = 0;
		double outPutValue = 0;
		for (FunctionTerm fnTerm : m_termSet) {
			int colIndex = tableSpec.findColumnIndex(fnTerm.getVarName());
			DataCell currentCell = dataRow.getCell(colIndex);
			//skip missing cells
			if (currentCell.isMissing()) {
				continue;
			}
			double cellValue = ((DoubleValue) dataRow.getCell(colIndex)).getDoubleValue();
			if (fnTerm.getVarName().contentEquals(m_xAxisVarColumn.getStringValue())) {
				xValue = cellValue;
			}
			if (fnTerm.getVarName().contentEquals(m_yAxisVarColumn.getStringValue())) {
				yValue = cellValue;
			}
			outPutValue += cellValue;
		}
		return new CalculatedPoint(xValue, yValue, outPutValue, dataRow.getKey());
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
//						m_inPort1ExponentIndex = inSpecs[COEFFICIENT_IN_PORT].findColumnIndex(colName);
						throw new InvalidSettingsException(
								"The function you are trying to graph appears to be a polynomial regression line. Please use only linear regression for this node.");
					}
					// alternately check for "coeff."
					if (colName.toLowerCase().trim().contains("coeff") && inSpecs[COEFFICIENT_IN_PORT]
							.getColumnSpec(colName).getType().isCompatible(DoubleValue.class)) {
						m_inPort1CoeffColumnIndex = inSpecs[COEFFICIENT_IN_PORT].findColumnIndex(colName);
						hasCoefficientColumn = true;
					}
					if (colName.toLowerCase().trim().equals("variable") && inSpecs[COEFFICIENT_IN_PORT]
							.getColumnSpec(colName).getType().isCompatible(StringValue.class)) {
						m_inPort1VariableColumnIndex = inSpecs[COEFFICIENT_IN_PORT].findColumnIndex(colName);
						hasVariableColumn = true;
					}
				}
			} else if (coeffTableColumns != null && // checking for H20 regression output
					coeffTableColumns.length == 1 && coeffTableColumns[0].toLowerCase().trim() == "beta"
					&& inSpecs[COEFFICIENT_IN_PORT].getColumnSpec(coeffTableColumns[0]).getType()
							.isCompatible(DoubleValue.class)) {
				m_inPort1CoeffColumnIndex = inSpecs[COEFFICIENT_IN_PORT].findColumnIndex(coeffTableColumns[0]);
				m_isH2ONode.setBooleanValue(true);
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
			boolean containsXCol = false;
			boolean containsYCol = false;
			int numDataColumns = inSpecs[DATA_TABLE_IN_PORT].getNumColumns();
			int numNumericColumns = 0;

			m_dataTableColumnNames = new ArrayList<String>();

			if (numDataColumns > 0) {
				for (int i = 0; i < numDataColumns; i++) {
					DataColumnSpec columnSpec = inSpecs[DATA_TABLE_IN_PORT].getColumnSpec(i);
					String colName = columnSpec.getName();
					m_dataTableColumnNames.add(colName);
					if (columnSpec.getType().isCompatible(DoubleValue.class)) {
						numNumericColumns++;
						if (m_colName != null && colName.contentEquals(m_colName.getStringValue())) {
							m_targetType = columnSpec.getType();
							containsTargetColumn = true;
						}
						if (m_xAxisVarColumn != null && colName.contentEquals(m_xAxisVarColumn.getStringValue())) {
							containsXCol = true;
						}
						if (m_yAxisVarColumn != null && colName.contentEquals(m_yAxisVarColumn.getStringValue())) {
							containsYCol = true;
						}
					}
				}
			}
			if (numNumericColumns < 3) {
				throw new InvalidSettingsException(
						"The data table provided must contain 2 or more numeric columns which correspond to the variables in the regression equation and 1 numeric target column (same as was selected during regression learning).");
			}
			// for R^k=>R, where k > 2
			// may run into issues when not all numeric columns were used in the regression
			// learner (fix later)
//			else if (numNumericColumns - m_numApproxColumns.getIntValue() > 3) {
//				throw new InvalidSettingsException(
//						"Please reduce the number of calculated columns in the selection area to 2 by transfering additional variables to the 'Approximate with mean value' category.");
//			}
			if (!containsTargetColumn) {
				throw new InvalidSettingsException(
						"You must select the column targeted in the Regression Learning Node for the target column.");
			}
			if (!containsXCol || !containsYCol) {
				throw new InvalidSettingsException("You must select a numeric column for both axis.");
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
