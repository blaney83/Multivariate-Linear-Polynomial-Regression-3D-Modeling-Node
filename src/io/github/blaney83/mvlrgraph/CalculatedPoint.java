package io.github.blaney83.mvlrgraph;

import java.io.IOException;

import org.knime.core.data.RowKey;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

public class CalculatedPoint {

	private final static String CFGKEY_X_VAL = "xVal";
	private final static String CFGKEY_Y_VAL = "yVal";
	private final static String CFGKEY_Z_VAL = "zVal";
	private final static String CFGKEY_ROW_KEY = "rowKey";
	private final static String CFGKEY_MISSING = "isMissing";

	private double xValue;
	private double yValue;
	private double zValue;
	private double percentError;
	private RowKey rowKey;
	private boolean isSelected;
	private boolean isHilited;
	private boolean isMissing;

	public CalculatedPoint() {
		xValue = 0;
		yValue = 0;
		zValue = 0;
		this.isSelected = false;
		this.isHilited = false;
		this.isMissing = true;
	}

	public CalculatedPoint(final double xValue, final double yValue, final double zValue, final RowKey rowKey) {
		this.xValue = xValue;
		this.yValue = yValue;
		this.zValue = zValue;
		this.rowKey = rowKey;
		this.isSelected = false;
		this.isHilited = false;
	}

	public double getXValue() {
		return xValue;
	}

	public double getYValue() {
		return yValue;
	}

	public double getZValue() {
		return zValue;
	}

	public RowKey getRowKey() {
		return rowKey;
	}
	
	public void setPercentError(final double err) {
		percentError = err;
	}
	
	public double getPercentError() {
		return percentError;
	}

	public void saveTo(final ModelContentWO modelContent) {
		modelContent.addDouble(CFGKEY_X_VAL, this.xValue);
		modelContent.addDouble(CFGKEY_Y_VAL, this.yValue);
		modelContent.addDouble(CFGKEY_Z_VAL, this.zValue);
		modelContent.addRowKey(CFGKEY_ROW_KEY, this.rowKey);
		modelContent.addBoolean(CFGKEY_MISSING, this.isMissing);
	}

	public void loadFrom(final ModelContentRO modelContent) {
		try {
			this.xValue = modelContent.getDouble(CFGKEY_X_VAL);
			this.yValue = modelContent.getDouble(CFGKEY_Y_VAL);
			this.zValue = modelContent.getDouble(CFGKEY_Z_VAL);
			this.rowKey = modelContent.getRowKey(CFGKEY_ROW_KEY);
			this.isMissing = modelContent.getBoolean(CFGKEY_MISSING);
		} catch (InvalidSettingsException e) {
			e.addSuppressed(new IOException(
					"There was a problem loading the internal state of this node. Please reset the node and execute again."));
		}
	}

	public boolean isMissing() {
		return this.isMissing;
	}

	public void setMissing(final boolean isMissing) {
		this.isMissing = isMissing;
	}

	public boolean isSelected() {
		return isSelected;
	}

	public void setSelected(boolean isSelected) {
		this.isSelected = isSelected;
	}

	public boolean isHilited() {
		return isHilited;
	}

	public void setHilited(boolean isHilited) {
		this.isHilited = isHilited;
	}

}
