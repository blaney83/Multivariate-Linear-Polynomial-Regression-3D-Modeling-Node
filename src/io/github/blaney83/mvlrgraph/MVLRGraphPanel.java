package io.github.blaney83.mvlrgraph;

import java.awt.Graphics;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import javax.swing.JPanel;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.Scatter;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

public class MVLRGraphPanel extends AbstractAnalysis {
	// v1
	private static final long serialVersionUID = 1L;
	// panel internal fields
	private String m_targetName;
	private String m_xName;
	private String m_yName;

	// external view data
	private Set<FunctionTerm> m_termSet;
	private CalculatedPoint[] m_calcPoints;

	public MVLRGraphPanel(final Set<FunctionTerm> termSet, final CalculatedPoint[] calcPoints, final String targetName,
			final String xName, final String yName) {
		m_termSet = termSet;
		m_calcPoints = calcPoints;
		m_targetName = targetName;
		m_xName = xName;
		m_yName = yName;
		// setPreferredSize(new Dimension(width, height));
	}

	public void updateView(final Set<FunctionTerm> termSet, final CalculatedPoint[] calcPoints, final String targetName,
			final String xName, final String yName) {
		m_termSet = termSet;
		m_calcPoints = calcPoints;
		m_targetName = targetName;
		m_xName = xName;
		m_yName = yName;
	}

	@Override
	public void init() {
		// Define a function to plot
//        Mapper mapper = new Mapper() {
//            @Override
//            public double f(double x, double y) {
//                return x * Math.sin(x * y);
//            }
//        };
//
//        // Define range and precision for the function to plot
//        Range range = new Range(-3, 3);
//        int steps = 80;
//
//        // Create the object to represent the function over the given range.
//        final Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(range, steps, range, steps), mapper);
//        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
//        surface.setFaceDisplayed(true);
//        surface.setWireframeDisplayed(false);
//
//        // Create a chart
//        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
//        chart.getScene().getGraph().add(surface);
		FunctionTerm xTerm = new FunctionTerm();
		FunctionTerm yTerm = new FunctionTerm();
		int count = 0;
		for (FunctionTerm fnTerm : m_termSet) {
			if (fnTerm.getVarName() != null) {
				System.out.println("FN" + count + " " + fnTerm.getVarName() + " val: " +
//						fnTerm.getValue() + 
						" coeff: " + fnTerm.getCoefficient() + " exp: " + fnTerm.getExponent() + " f(1): "
						+ fnTerm.evaluateTerm(1) + " lower bound: " + fnTerm.getLowerBound() + " upper bound : "
						+ fnTerm.getUpperBound() + " const: " + fnTerm.getIsConstant());
				if (fnTerm.getVarName().equals(m_xName)) {
					xTerm = fnTerm;
				} else if (fnTerm.getVarName().equals(m_yName)) {
					yTerm = fnTerm;
				}
			}
			count++;
		}
		// the problem is that all of the values are currently set to constant and are
		// being evaluated below as 0
		// because I coded them to be evaluated as 0. need to either eliminate isConst
		// and check against name, fix the
		// is const auto set with value, or re-write the evaluate method so that it can
		// be passed a check.

		// also- need to give intercept a domain- min=self, max=self
		// goodnight
		Mapper mapper = new Mapper() {
			@Override
			public double f(double x, double y) {
				double z = 0;
				for (FunctionTerm fnTerm : m_termSet) {
					if (fnTerm.getVarName() != null) {
						if (!fnTerm.getVarName().equals(m_xName) && !fnTerm.getVarName().equals(m_yName)
								&& fnTerm.getIsConstant()) {
            				System.out.println(fnTerm.getValue());
            				System.out.println(fnTerm.evaluateTerm(0));
            				System.out.println(z);
            				System.out.println("should fire for constants");
            					z += fnTerm.evaluateTerm(0);
						} else if (fnTerm.getVarName().equals(m_xName) && !fnTerm.getIsConstant()) {
            				System.out.println(fnTerm.evaluateTerm(x));
            				System.out.println(fnTerm.evaluateTerm(0));
            				System.out.println(z);
            				System.out.println("should fire for x");
							z += fnTerm.evaluateTerm(x);
						} else if (fnTerm.getVarName().equals(m_yName) && !fnTerm.getIsConstant()) {
            				System.out.println(fnTerm.evaluateTerm(y));
            				System.out.println(fnTerm.evaluateTerm(0));
            				System.out.println(z);
            				System.out.println("should fire for y");
							z += fnTerm.evaluateTerm(y);
						}
					}
				}
//    			System.out.println(z);
				return z;
			}
		};
		double xMin = -100;
		double xMax = 100;

		double yMin = -100;
		double yMax = 100;

		if (xTerm != null) {
			xMin = xTerm.getLowerBound();
			xMax = xTerm.getUpperBound();
		}
		if (yTerm != null) {
			yMin = yTerm.getLowerBound();
			yMax = yTerm.getUpperBound();
		}

		System.out.println(xMax);
		System.out.println(xMin);
		System.out.println(yMax);
		System.out.println(yMin);
		Range xRange = new Range((float) xMin, (float) xMax);
		Range yRange = new Range((float) yMin, (float) yMax);

		int steps = 80;

		Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, steps, yRange, steps), mapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(),
				surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeDisplayed(false);

		chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
		chart.getScene().getGraph().add(surface);

		// scatter
		Coord3d[] points = new Coord3d[m_calcPoints.length];
		Color[] colors = new Color[m_calcPoints.length];

		float x = 0;
		float y = 0;
		float z = 0;

		for (int i = 0; i < m_calcPoints.length; i++) {
//			System.out.println(m_calcPoints[i].getXValue());
//			System.out.println(m_calcPoints[i].getYValue());
//			System.out.println(m_calcPoints[i].getZValue());
			System.out.println(m_calcPoints[i].getPercentError());
			x = (float) m_calcPoints[i].getXValue();
			y = (float) m_calcPoints[i].getYValue();
			z = (float) m_calcPoints[i].getZValue();
			Coord3d pointCoord = new Coord3d(x, y, z);
			points[i] = pointCoord;
			colors[i] = new Color((int)(255*Math.abs(m_calcPoints[i].getPercentError())), (int)(255*(1-Math.abs(m_calcPoints[i].getPercentError()))), 0);
		}

		Scatter scatter = new Scatter(points, colors);
		scatter.setWidth(5);
//        chart = AWTChartComponentFactory.chart(Quality.Advanced, "newt");
		chart.getScene().getGraph().add(scatter);
		chart.getAxeLayout().setXAxeLabel(m_xName);
		chart.getAxeLayout().setYAxeLabel(m_yName);
		chart.getAxeLayout().setZAxeLabel(m_targetName);
	}

}
