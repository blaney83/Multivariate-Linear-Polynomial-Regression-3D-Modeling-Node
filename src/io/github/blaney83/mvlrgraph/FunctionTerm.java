package io.github.blaney83.mvlrgraph;

import java.io.IOException;

import org.knime.core.data.DataColumnDomain;
import org.knime.core.data.DataColumnDomainCreator;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.def.DoubleCell;
import org.knime.core.node.InvalidSettingsException;
import org.knime.core.node.ModelContentRO;
import org.knime.core.node.ModelContentWO;

public class FunctionTerm {

	private static String CFGKEY_TERM_NAME = "termName";
	private static String CFGKEY_TERM_COEFF = "termCoeff";
	private static String CFGKEY_TERM_EXP = "termExp";
	private static String CFGKEY_TERM_CONSTANT = "isConst";
	private static String CFGKEY_TERM_VALUE = "termVal";
	private static String CFGKEY_TERM_LOWER_BOUND = "termLowerBound";
	private static String CFGKEY_TERM_UPPER_BOUND = "termUpperBound";

	private String varName;
	private double coefficient;
	private int exponent;
	private boolean isConstant;
	private double value;
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
			this.value = Math.pow(coefficient, exponent);
		}
	}

	public FunctionTerm(final String varName, final double coefficient, final int exponent) {
		this.varName = varName;
		this.coefficient = coefficient;
		this.exponent = exponent;
		isConstant = false;
		if (this.varName.toLowerCase().trim().equals("intercept")) {
			isConstant = true;
			this.value = Math.pow(coefficient, exponent);
		}
	}

	public double evaluateTerm(final double val) {
		if (this.isConstant) {
			return coefficient * (Math.pow(this.value, exponent));
		} else {
			return coefficient * (Math.pow(val, exponent));
		}
	}

	public void setValue(final double value) {
		this.value = value;
		this.isConstant = true;
	}

	public void setDomain(final DataColumnDomain domain) {
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

	public double getLowerBound() {
		return ((DoubleValue) this.domain.getLowerBound()).getDoubleValue();
	}

	public double getUpperBound() {
		return ((DoubleValue) this.domain.getUpperBound()).getDoubleValue();
	}

	public void saveTo(final ModelContentWO modelContent) {
		modelContent.addString(CFGKEY_TERM_NAME, this.varName);
		modelContent.addDouble(CFGKEY_TERM_COEFF, this.coefficient);
		modelContent.addInt(CFGKEY_TERM_EXP, this.exponent);
		modelContent.addBoolean(CFGKEY_TERM_CONSTANT, this.isConstant);
		modelContent.addDouble(CFGKEY_TERM_VALUE, this.value);
		modelContent.addDouble(CFGKEY_TERM_LOWER_BOUND, this.getLowerBound());
		modelContent.addDouble(CFGKEY_TERM_UPPER_BOUND, this.getUpperBound());
	}

	@SuppressWarnings("static-access")
	public void loadFrom(final ModelContentRO modelContent) {
		try {
		this.varName = modelContent.getString(CFGKEY_TERM_NAME);
		this.coefficient = modelContent.getDouble(CFGKEY_TERM_COEFF);
		this.exponent = modelContent.getInt(CFGKEY_TERM_EXP);
		this.isConstant = modelContent.getBoolean(CFGKEY_TERM_CONSTANT);
		this.value = modelContent.getDouble(CFGKEY_TERM_VALUE);
		DataColumnDomainCreator loadedDomainCreator = new DataColumnDomainCreator();
		loadedDomainCreator.setLowerBound(new DoubleCell(modelContent.getDouble(CFGKEY_TERM_LOWER_BOUND)));
		loadedDomainCreator.setUpperBound(new DoubleCell(modelContent.getDouble(CFGKEY_TERM_UPPER_BOUND)));
		this.domain = loadedDomainCreator.createDomain();
		}catch (InvalidSettingsException e) {
			e.addSuppressed(new IOException("There was a problem loading the internal state of this node. Please reset the node and execute again."));
		}
	}

	@Override
	public int hashCode() {
		int boolSwitch = 0;
		if(this.isConstant) {
			boolSwitch = 1;
		}
		return  this.getVarName().hashCode() + 
				(int) this.getCoefficient() +
				this.getExponent() +  
				boolSwitch 
//				+ 
//				this.domain.hashCode()
				;
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
		if (!this.getVarName().equals(otherTerm.getVarName())) {
			return false;
		}
		if (Double.compare(this.getCoefficient(), otherTerm.getCoefficient()) != 0) {
			return false;
		}

		if (this.getExponent() != otherTerm.getExponent()) {
			return false;
		}
		if (this.isConstant != otherTerm.getIsConstant()) {
			return false;
		}
//		if(!this.domain.getLowerBound().equals(otherTerm.getLowerBound()) ||
//				this.domain.getUpperBound().equals(otherTerm.getUpperBound())) {
//			return false;
//		}
		return true;
	}

}
