package io.github.blaney83.mvlrgraph;

import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DoubleValue;

public class FunctionTerm {
	private final String varName;
	private final double coefficient;
	private int exponent;
	private boolean isConstant;
	private double value;
	private double mean;
	private DataColumnDomain domain;

	public FunctionTerm() {
		this.varName = null;
		this.coefficient = Double.NaN;
		this.exponent = 0;
		this.isConstant = false;
		this.value = Double.NaN;
	}

	public FunctionTerm(final String varName, final double coefficient) {
		this.varName = varName;
		this.coefficient = coefficient;
		this.exponent = 1;
		isConstant = false;
		if (this.varName.toLowerCase().trim().equals("intercept")) {
			isConstant = true;
			double value = Math.pow(coefficient, exponent);
		}
	}

	public FunctionTerm(final String varName, final double coefficient, final int exponent) {
		this.varName = varName;
		this.coefficient = coefficient;
		this.exponent = exponent;
		isConstant = false;
		if (this.varName.toLowerCase().trim().equals("intercept")) {
			isConstant = true;
			double value = Math.pow(coefficient, exponent);
		}
	}
	

	public double evaluateTerm(final double val) {
		if (this.isConstant) {
			return coefficient * (Math.pow(this.value, exponent));
		}else {
			return coefficient * (Math.pow(val, exponent));
		}
	}

	public void setValue(final double value) {
		this.value = value;
		this.isConstant = true;
	}
	
	public void setDomain (final DataColumnDomain domain) {
		this.domain = domain;
	}

	public String getVarName() {
		return varName;
	}

	public double getCoefficient() {
		return coefficient;
	}

	public int getExponent() {
		return exponent;
	}

	public boolean getIsConstant() {
		return this.isConstant;
	}

	public double getValue() {
		return this.value;
	}
	
	public double getLowerBound () {
		return ((DoubleValue) this.domain.getLowerBound()).getDoubleValue();
	}
	
	public double getUpperBound () {
		return ((DoubleValue) this.domain.getUpperBound()).getDoubleValue();
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return this.getExponent() + (int) this.getCoefficient() + this.getVarName().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (this.getClass() != obj.getClass()) {
			return false;
		}
		FunctionTerm otherTerm = (FunctionTerm) obj;
		if (this.isConstant != otherTerm.getIsConstant()) {
			return false;
		}
		if (!this.getVarName().equals(otherTerm.getVarName())) {
			return false;
		}
		if (Double.compare(this.getCoefficient(), otherTerm.getCoefficient()) != 0) {
			return false;
		}
		if (this.getExponent() != otherTerm.getExponent()) {
			return false;
		}
		return true;
	}

}
