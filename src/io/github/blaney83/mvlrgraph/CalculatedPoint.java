package io.github.blaney83.mvlrgraph;

import org.knime.core.data.RowKey;

import com.sun.rowset.internal.Row;

public class CalculatedPoint {
	private final double xValue;
	private final double yValue;
	private final double zValue;
	private RowKey rowKey;
	private boolean isSelected;
	private boolean isHilited;
	private boolean isMissing = false;
	
	public CalculatedPoint() {
		xValue = 0;
		yValue = 0;
		zValue = 0;
		this.isSelected = false;
		this.isHilited = false;
		isMissing = true;
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
	
	public boolean getIsMissing() {
		return this.isMissing;
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
