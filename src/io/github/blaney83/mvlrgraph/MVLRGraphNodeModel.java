package io.github.blaney83.mvlrgraph;

import java.io.File;
import java.io.IOException;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataColumnSpecCreator;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.RowKey;
import org.knime.core.data.def.DefaultRow;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.data.def.IntCell;
import org.knime.core.data.def.StringCell;
import org.knime.core.node.BufferedDataContainer;
import org.knime.core.node.BufferedDataTable;
import org.knime.core.node.CanceledExecutionException;
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
	// default settings fields
	private static final int DEFAULT_COUNT = 100;
	private static final String DEFAULT_COLUMN = "Target Column";
	// settings model
	private final SettingsModelIntegerBounded m_count = new SettingsModelIntegerBounded(MVLRGraphNodeModel.CFGKEY_COUNT,
			MVLRGraphNodeModel.DEFAULT_COUNT, Integer.MIN_VALUE, Integer.MAX_VALUE);
	private final SettingsModelString m_colName = new SettingsModelString(CFGKEY_COL_NAME, DEFAULT_COLUMN);
	// local model fields
	private DataPoint[] m_dataPoints;
	// private RegressionFunction m_regFn;

	protected MVLRGraphNodeModel() {
		// two in-ports, one out-port
		super(2, 1);
	}

	@Override
	protected BufferedDataTable[] execute(final BufferedDataTable[] inData, final ExecutionContext exec)
			throws Exception {

		// TODO do something here
//        logger.info("Node Model Stub... this is not yet implemented !");
//
//        return new BufferedDataTable[]{out};
	}

	@Override
	protected void reset() {
		if (m_regFn != null) {
			m_regFn = null;
		}
		if (m_dataPoints != null) {
			m_dataPoints = null;
		}
	}

	@Override
	protected DataTableSpec[] configure(final DataTableSpec[] inSpecs) throws InvalidSettingsException {
		if (inSpecs[COEFFICIENT_IN_PORT] == null || inSpecs[DATA_TABLE_IN_PORT] == null) {
			throw new InvalidSettingsException(
					"Please provide a regression coefficient table to In-Port #1 and a data table to In-Port #2.");
		} else {
			boolean hasCorrectDataTableSize = false;
			boolean hasMatchingDataColumns = false;
			boolean hasCorrectCoefficientTableTypes = false;
			int dataTableColumns = inSpecs[DATA_TABLE_IN_PORT].getNumColumns();

			return new DataTableSpec[] { null };
		}
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
