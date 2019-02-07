package io.github.blaney83.mvlrgraph;

import java.util.LinkedHashSet;
import java.util.Set;

import org.knime.core.data.DataCell;
import org.knime.core.data.DataColumnSpec;
import org.knime.core.data.DataRow;
import org.knime.core.data.DataTableSpec;
import org.knime.core.data.DataType;
import org.knime.core.data.DoubleValue;
import org.knime.core.data.container.SingleCellFactory;
import org.knime.core.data.def.DoubleCell;

public class MVLRGraphCellFactory extends SingleCellFactory {
	private final DataTableSpec tableSpec;
	private final Set<FunctionTerm> functionTerms;
	private final CalculatedPoint[] calcPoints;

	public MVLRGraphCellFactory(final DataColumnSpec newColSpec, final DataTableSpec tableSpec,
			final Set<FunctionTerm> functionTerms, final CalculatedPoint[] calcPoints) {
		super(newColSpec);
		this.tableSpec = tableSpec;
		this.functionTerms = new LinkedHashSet<FunctionTerm>(functionTerms);
		this.calcPoints = calcPoints;
	}

	@Override
	public DataCell getCell(final DataRow row) {
		double outPutValue = 0;
		for (FunctionTerm fnTerm : functionTerms) {
			int colIndex = tableSpec.findColumnIndex(fnTerm.getVarName());
			if (colIndex > -1) {
				DataCell currentCell = row.getCell(colIndex);
				if (currentCell.isMissing()) {
					return DataType.getMissingCell();
				}
				double cellValue = ((DoubleValue) row.getCell(colIndex)).getDoubleValue();

				outPutValue += fnTerm.evaluateTerm(cellValue);
			}else {
				//handle intercept
				outPutValue += fnTerm.evaluateTerm(0);
			}
		}
		for(CalculatedPoint point : calcPoints) {
			if(point.getRowKey().equals(row.getKey())) {
				point.setPercentError((point.getZValue() - outPutValue)/ outPutValue);
			}
		}
		return new DoubleCell(outPutValue);
	}

}
