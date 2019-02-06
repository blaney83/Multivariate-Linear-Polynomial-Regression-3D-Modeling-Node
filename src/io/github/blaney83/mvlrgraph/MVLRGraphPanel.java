package io.github.blaney83.mvlrgraph;

import java.awt.Graphics;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.swing.JPanel;

import org.jzy3d.analysis.AbstractAnalysis;
import org.jzy3d.bridge.swing.SimpleBufferedPanelSwing;
import org.jzy3d.chart.factories.AWTChartComponentFactory;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.primitives.AbstractDrawable;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.primitives.Surface;
import org.jzy3d.plot3d.rendering.canvas.Quality;


public class MVLRGraphPanel extends AbstractAnalysis{
	// v1
	private static final long serialVersionUID = 1L;
	//panel internal fields
	private String m_targetName;
	private String m_xName;
	private String m_yName;

	// external view data
	private Set<FunctionTerm> m_termSet;
	private CalculatedPoint[] m_calcPoints;
	
	public MVLRGraphPanel(final Set<FunctionTerm> termSet,
			final CalculatedPoint[] calcPoints,
			final String targetName,
			final String xName,
			final String yName) {
		m_termSet = termSet;
		m_calcPoints = calcPoints;
		m_targetName = targetName;
		m_xName = xName;
		m_yName = yName;
		//setPreferredSize(new Dimension(width, height));
	}
	
	public void updateView(final Set<FunctionTerm> termSet,
			final CalculatedPoint[] calcPoints,
			final String targetName,
			final String xName,
			final String yName) {
		m_termSet = termSet;
		m_calcPoints = calcPoints;
		m_targetName = targetName;
		m_xName = xName;
		m_yName = yName;
	}
	
	@Override
	public void init() {
		FunctionTerm xTerm = new FunctionTerm();
		FunctionTerm yTerm = new FunctionTerm();
		
		for(FunctionTerm fnTerm : m_termSet) {
			if(fnTerm.getVarName() != null) {
				if(fnTerm.getVarName().equals(m_xName)) {
    				xTerm = fnTerm;
    			}else if(fnTerm.getVarName().equals(m_yName)) {
    				yTerm = fnTerm;
    			}
			}
		}
		
		Mapper mapper = new Mapper() {
            @Override
            public double f(double x, double y) {
            	double z = 0;
            	for(FunctionTerm fnTerm : m_termSet) {
            		if(fnTerm.getVarName() != null) {
            			if(!fnTerm.getVarName().equals(m_xName) &&
            					!fnTerm.getVarName().equals(m_yName) &&
            					fnTerm.getIsConstant()) {
            				z += fnTerm.evaluateTerm(0);
            			}else if(fnTerm.getVarName().equals(m_xName)) {
            				z += fnTerm.evaluateTerm(x);
            			}else if(fnTerm.getVarName().equals(m_yName)) {
            				z += fnTerm.evaluateTerm(y);
            			}
            		}
            	}
                return z;
            }
        };
        double xMin = -100;
        double xMax = 100;
        
        double yMin = -100;
        double yMax = 100;
        
        if(xTerm.getVarName() != null) {
        	xMin = xTerm.getLowerBound();
        	xMax = xTerm.getUpperBound();
        }
        if(yTerm.getVarName() != null) {
        	yMin = yTerm.getLowerBound();
        	yMax = yTerm.getUpperBound();
        }
        
        Range xRange = new Range((float) xMin, (float)xMax);
        Range yRange = new Range((float) yMin, (float)yMax);
        int steps = 80;
        
        Shape surface = Builder.buildOrthonormal(new OrthonormalGrid(xRange, steps, yRange, steps), mapper);
        surface.setColorMapper(new ColorMapper(new ColorMapRainbow(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
        surface.setFaceDisplayed(true);
        surface.setWireframeDisplayed(false);

        chart = AWTChartComponentFactory.chart(Quality.Advanced, getCanvasType());
        chart.getScene().getGraph().add(surface);
	}
	
}
