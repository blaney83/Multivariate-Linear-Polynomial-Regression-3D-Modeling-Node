package io.github.blaney83.mvlrgraph;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.knime.base.node.io.filereader.InterruptedExecutionException;
import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.StringValue;
import org.knime.core.data.container.CellFactory;
import org.knime.core.data.container.ColumnRearranger;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.defaultnodesettings.SettingsModelColumnName;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

import sun.security.action.GetBooleanAction;

import org.knime.core.node.ExecutionContext;
import org.knime.core.node.ExecutionMonitor;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContent;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;
//import org.knime.core.node.NodeLogger;
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

	// Model Content File
	private static final String FILE_NAME = "mvlrGraphInternals.xml";

	// the logger instance
//	private static final NodeLogger logger = NodeLogger.getLogger(MVLRGraphNodeModel.class);

	// internal config keys
	static final String CFGKEY_COUNT = "count";
	static final String CFGKEY_COL_NAME = "columnName";
	static final String CFGKEY_X_AXIS_VAR_COLUMN = "xAxisVarColumn";
	static final String CFGKEY_Y_AXIS_VAR_COLUMN = "yAxisVarColumn";
	static final String CFGKEY_APPEND_CALCULATED_TARGET = "calculatedTarget";
	static final String CFGKEY_NUM_APPROX_COLUMN = "numApproxColumns";
	static final String CFGKEY_IS_H2O_NODE = "isH2ONode";
	// enum for intercept?
	// save/load cfg keys
	static final String INTERNAL_MODEL_NAME_KEY = "internalModel";
	static final String INTERNAL_MODEL_NUM_FUNCTION_TERM_KEY = "numFnTerms";
	static final String INTERNAL_MODEL_NUM_CALC_POINT_KEY = "numCalcPoints";
	static final String INTERNAL_MODEL_TERM_KEY = "fnTerm";
	static final String INTERNAL_MODEL_POINT_KEY = "calcPoint";
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
	static final int DEFAULT_COUNT = 100;
	static final boolean DEFAULT_APPEND_CALCULATED_TARGET = false;
	static final boolean DEFAULT_IS_H2O_NODE = false;

	// settings model
	private final SettingsModelIntegerBounded m_count = new SettingsModelIntegerBounded(MVLRGraphNodeModel.CFGKEY_COUNT,
			MVLRGraphNodeModel.DEFAULT_COUNT, 0, Integer.MAX_VALUE);
	protected final SettingsModelColumnName m_colName = new SettingsModelColumnName(CFGKEY_COL_NAME, "");
	protected final SettingsModelColumnName m_xAxisVarColumn = new SettingsModelColumnName(CFGKEY_X_AXIS_VAR_COLUMN,
			"");
	protected final SettingsModelColumnName m_yAxisVarColumn = new SettingsModelColumnName(CFGKEY_Y_AXIS_VAR_COLUMN,
			"");
	protected final SettingsModelBoolean m_appendCalculatedTarget = new SettingsModelBoolean(
			CFGKEY_APPEND_CALCULATED_TARGET, DEFAULT_APPEND_CALCULATED_TARGET);
	private final SettingsModelBoolean m_isH2ONode = new SettingsModelBoolean(CFGKEY_IS_H2O_NODE, DEFAULT_IS_H2O_NODE);

	// INTERNAL NODE FIELDS
	// model inter-method variables
	protected List<String> m_variableColNames;
	protected List<String> m_dataTableColumnNames;
	protected int m_inPort1VariableColumnIndex;
	protected int m_inPort1CoeffColumnIndex;
	protected int m_inPort1ExponentIndex = -1;
	protected DataType m_targetType;

	// view dependent fields
	protected Set<FunctionTerm> m_termSet;
	protected CalculatedPoint[] m_calcPoints;

	protected MVLRGraphNodeModel() {
		// two in-ports, one out-port
		super(2, 1);
	}

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

		// CURRENT STOPPING POINT- need to create the column appender, etc., if append =
		// true
		// use the Calculated point zValue in the loop to create the values for the new
		// cells
		// then set a buffered data table = the input and conditionally reassign it to
		// the new
		// column appender before returning it to finish out the execute method.
		// may need to create some global storage for some of the information I created
		// (such as the calcpointArr so the view can see it and maybe a global formula,
		// holidng
		// the domains for the x,y,z and the function for the graph mapping
		m_variableColNames = new ArrayList<String>();
		m_termSet = new LinkedHashSet<FunctionTerm>();

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
		m_calcPoints = new CalculatedPoint[m_count.getIntValue()];
		int iterations = 0;
		for (DataRow dataRow : dataTable) {
			m_calcPoints[iterations] = pointFactory(dataRow, dataTable.getDataTableSpec());
			if (iterations >= m_count.getIntValue()) {
				break;
			}
			iterations++;
		}

		BufferedDataTable bufferedOutput;
		//calculate values anyways, so as to color points on graph accordingly
		CellFactory cellFactory = new MVLRGraphCellFactory(createCalcValsOutputColumnSpec(),
				inData[DATA_TABLE_IN_PORT].getDataTableSpec(), m_termSet, m_calcPoints);
		ColumnRearranger outputTable = new ColumnRearranger(inData[DATA_TABLE_IN_PORT].getDataTableSpec());
		outputTable.append(cellFactory);
		bufferedOutput = exec.createColumnRearrangeTable(inData[DATA_TABLE_IN_PORT], outputTable, exec);
		
		if (!m_appendCalculatedTarget.getBooleanValue()) {
			bufferedOutput = exec.createBufferedDataTable(inData[DATA_TABLE_IN_PORT], exec);
		}
		return new BufferedDataTable[] { bufferedOutput };
	}

	private FunctionTerm validateCoeffVariables(final DataRow dataRow) throws InterruptedExecutionException {
		if (dataRow != null) {
			double coeff = Double.parseDouble(dataRow.getCell(m_inPort1CoeffColumnIndex).toString());
			int exponent = 1;
			if (m_inPort1ExponentIndex != -1) {
				System.out.println("Firing");
				System.out.println(m_inPort1ExponentIndex);
				exponent = Integer.parseInt(dataRow.getCell(m_inPort1ExponentIndex).toString());
				System.out.println(exponent);
			}
			if (!m_isH2ONode.getBooleanValue()) {
				String varName = dataRow.getCell(m_inPort1VariableColumnIndex).toString();
				System.out.println(varName);
				if (m_dataTableColumnNames.contains(varName) || varName.toLowerCase().trim().equals("intercept")) {
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
		if (colIndex > -1) {
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
			if (!colName.equals(m_colName.getStringValue()) && !colName.equals(m_xAxisVarColumn.getStringValue())
					&& !colName.equals(m_yAxisVarColumn.getStringValue())) {
				fnTerm.setValue(meanSum / totalRows);
			}
		} else {
			// handle intercept
			fnTerm.setValue(1);
			DataColumnDomainCreator colDomainCreator = new DataColumnDomainCreator();
			colDomainCreator.setLowerBound(new DoubleCell(fnTerm.getCoefficient()));
			colDomainCreator.setUpperBound(new DoubleCell(fnTerm.getCoefficient()));
			fnTerm.setDomain(colDomainCreator.createDomain());
		}
	}

	private CalculatedPoint pointFactory(final DataRow dataRow, final DataTableSpec tableSpec) {
		int targetIndex = tableSpec.findColumnIndex(m_colName.getStringValue());
		int xIndex = tableSpec.findColumnIndex(m_xAxisVarColumn.getStringValue());
		int yIndex = tableSpec.findColumnIndex(m_yAxisVarColumn.getStringValue());
		double xValue = ((DoubleValue) dataRow.getCell(xIndex)).getDoubleValue();
		double yValue = ((DoubleValue) dataRow.getCell(yIndex)).getDoubleValue();
		double outPutValue = ((DoubleValue) dataRow.getCell(targetIndex)).getDoubleValue();
//		for (FunctionTerm fnTerm : m_termSet) {
//			int colIndex = tableSpec.findColumnIndex(fnTerm.getVarName());
//			if (colIndex > -1) {
//				DataCell currentCell = dataRow.getCell(colIndex);
//				// skip missing cells
//				if (currentCell.isMissing()) {
//					return new CalculatedPoint();
//				}
//				double cellValue = ((DoubleValue) dataRow.getCell(colIndex)).getDoubleValue();
//				if (fnTerm.getVarName().contentEquals(m_xAxisVarColumn.getStringValue())) {
//					xValue = cellValue;
//				}
//				if (fnTerm.getVarName().contentEquals(m_yAxisVarColumn.getStringValue())) {
//					yValue = cellValue;
//				}
////				if (fnTerm.getVarName().contentEquals(m_colName.getStringValue())) {
////					outPutValue = cellValue;
////				}
////				outPutValue += fnTerm.evaluateTerm(cellValue);
//			} else {
//				// intercept
////				outPutValue += fnTerm.evaluateTerm(0);
//			}
//		}
		return new CalculatedPoint(xValue, yValue, outPutValue, dataRow.getKey());
	}

	@Override
	protected void reset() {
		if (m_termSet != null) {
			m_termSet = null;
		}
		if (m_calcPoints != null) {
			m_calcPoints = null;
		}
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
						m_inPort1ExponentIndex = inSpecs[COEFFICIENT_IN_PORT].findColumnIndex(colName);
//						throw new InvalidSettingsException(
//								"The function you are trying to graph appears to be a polynomial regression line. Please use only linear regression for this node.");
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
					System.out.println(colName);
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
//		DataColumnSpecCreator newColSpecCreator = new DataColumnSpecCreator("Calculated " + m_colName, m_targetType);
		//testing non dynamic output column type
		DataColumnSpecCreator newColSpecCreator = new DataColumnSpecCreator("Calculated " + m_colName, DoubleCell.TYPE);
		DataColumnSpec newColSpec = newColSpecCreator.createSpec();
		return newColSpec;
	}

	@Override
	protected void saveSettingsTo(final NodeSettingsWO settings) {
		m_count.saveSettingsTo(settings);
		m_colName.saveSettingsTo(settings);
		m_xAxisVarColumn.saveSettingsTo(settings);
		m_yAxisVarColumn.saveSettingsTo(settings);
		m_appendCalculatedTarget.saveSettingsTo(settings);
		m_isH2ONode.saveSettingsTo(settings);
	}

	@Override
	protected void loadValidatedSettingsFrom(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_count.loadSettingsFrom(settings);
		m_colName.loadSettingsFrom(settings);
		m_xAxisVarColumn.loadSettingsFrom(settings);
		m_yAxisVarColumn.loadSettingsFrom(settings);
		m_appendCalculatedTarget.loadSettingsFrom(settings);
		m_isH2ONode.loadSettingsFrom(settings);
	}

	@Override
	protected void validateSettings(final NodeSettingsRO settings) throws InvalidSettingsException {
		m_count.validateSettings(settings);
		m_colName.validateSettings(settings);
		m_xAxisVarColumn.validateSettings(settings);
		m_yAxisVarColumn.validateSettings(settings);
		m_appendCalculatedTarget.validateSettings(settings);
		m_isH2ONode.validateSettings(settings);
	}

	@Override
	protected void loadInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		File file = new File(internDir, FILE_NAME);
		try (FileInputStream fis = new FileInputStream(file)) {
			ModelContentRO modelContent = ModelContent.loadFromXML(fis);
			try {
				int numFnTerms = modelContent.getInt(INTERNAL_MODEL_NUM_FUNCTION_TERM_KEY);
				int numCalcPoints = modelContent.getInt(INTERNAL_MODEL_NUM_CALC_POINT_KEY);
				m_termSet = new LinkedHashSet<FunctionTerm>();
				m_calcPoints = new CalculatedPoint[numCalcPoints];
				for (int i = 0; i < numFnTerms; i++) {
					FunctionTerm newTerm = new FunctionTerm();
					ModelContentRO subContent = modelContent.getModelContent(INTERNAL_MODEL_TERM_KEY + i);
					newTerm.loadFrom(subContent);
					m_termSet.add(newTerm);
				}

				for (int i = 0; i < numCalcPoints; i++) {
					CalculatedPoint newPoint = new CalculatedPoint();
					ModelContentRO subContent = modelContent.getModelContent(INTERNAL_MODEL_POINT_KEY + i);
					newPoint.loadFrom(subContent);
					m_calcPoints[i] = newPoint;
				}

			} catch (InvalidSettingsException e) {
				throw new IOException("There was a problem loading the internal state of this node.");
			}

		}
	}

	@Override
	protected void saveInternals(final File internDir, final ExecutionMonitor exec)
			throws IOException, CanceledExecutionException {
		if (m_termSet != null && m_calcPoints != null) {
			ModelContent modelContent = new ModelContent(INTERNAL_MODEL_NAME_KEY);
			modelContent.addInt(INTERNAL_MODEL_NUM_FUNCTION_TERM_KEY, m_termSet.size());
			modelContent.addInt(INTERNAL_MODEL_NUM_CALC_POINT_KEY, m_calcPoints.length);
			int count = 0;
			for (FunctionTerm fnTerm : m_termSet) {
				ModelContentWO subContentWO = modelContent.addModelContent(INTERNAL_MODEL_TERM_KEY + count);
				fnTerm.saveTo(subContentWO);
				count++;
			}
			count = 0;
			for (CalculatedPoint calcPoint : m_calcPoints) {
				ModelContentWO subContentWO = modelContent.addModelContent(INTERNAL_MODEL_POINT_KEY + count);
				calcPoint.saveTo(subContentWO);
				count++;
			}
			File file = new File(internDir, FILE_NAME);
			FileOutputStream fos = new FileOutputStream(file);
			modelContent.saveToXML(fos);
		}
	}
}
