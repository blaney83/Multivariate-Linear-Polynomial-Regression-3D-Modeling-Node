package io.github.blaney83.mvlrgraph;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.TitlePaneLayout;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils.Collections;

public class MVLRGraphNodeViewPanel extends JPanel {
	// v1
	private static final long serialVersionUID = 1L;
	// default view size
	private static final int WIDTH = 800;
	private static final int HEIGHT = 650;
	// graph instance
	private MVLRGraphPanel graphPanel;

	// external view data
	private Set<FunctionTerm> m_termSet;
	private CalculatedPoint[] m_calcPoints;

	public MVLRGraphNodeViewPanel(final MVLRGraphNodeModel nodeModel) {
		this.m_termSet = nodeModel.m_termSet;
		this.m_calcPoints = nodeModel.m_calcPoints;

		setPreferredSize(new Dimension(WIDTH, HEIGHT));
		setLayout(new BorderLayout());

		TitlePanel titlePanel = new TitlePanel(MVLRGraphNodeView.DEFAULT_GRAPH_TITLE);
		add(titlePanel, BorderLayout.NORTH);
		if (MVLRGraphNodeView.DEFAULT_GRAPH_EQUATION) {
			EquationPanel eqPanel = new EquationPanel(nodeModel.m_termSet, nodeModel.m_colName.getStringValue(),
					nodeModel.m_xAxisVarColumn.getStringValue(), nodeModel.m_yAxisVarColumn.getStringValue());
			add(eqPanel, BorderLayout.SOUTH);
		}
		graphPanel = new MVLRGraphPanel(termSet, calcPoints);
		add(graphPanel);
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		graphPanel.repaint();
	}

	public void updateView(final Set<FunctionTerm> termSet, final CalculatedPoint[] calcPoints) {
		graphPanel.updateView(termSet, calcPoints);
	}

	private final class TitlePanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final String graphTitle;
		private static final int WIDTH = 25;
		private static final int HEIGHT = 0;

		public TitlePanel(final String title) {
			this.graphTitle = title;
			setPreferredSize(new Dimension(WIDTH, HEIGHT));
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2D = (Graphics2D) g;

			Font font = new Font("Arial", Font.BOLD, 20);
			FontMetrics metrics = g2D.getFontMetrics(font);
			int width = metrics.stringWidth(this.graphTitle);

			g2D.setFont(font);

			g2D.drawString(this.graphTitle, (getWidth() - width) / 2, 20);
		}
	}

	private final class EquationPanel extends JPanel {
		private static final long serialVersionUID = 1L;
		private final Set<FunctionTerm> panelTerms;
		private final String equationString;
		private final String targetCol;
		private final String xCol;
		private final String yCol;
		private boolean hasConstants = false;

		public EquationPanel(final Set<FunctionTerm> panelTerms, final String targetCol, final String xCol,
				final String yCol) {
			this.panelTerms = new LinkedHashSet<FunctionTerm>(panelTerms);
			this.targetCol = targetCol;
			this.xCol = xCol;
			this.yCol = yCol;
			StringBuilder newBuilder = new StringBuilder("\u0192(" + targetCol + "\u209A) =");
			int expIterator = 0;
			for (FunctionTerm fnTerm : this.panelTerms) {
				if (fnTerm.getExponent() > expIterator) {
					expIterator = fnTerm.getExponent();
				}
			}
			String interceptString = "";
			for (int i = 1; i < expIterator + 1; i++) {
				for (FunctionTerm fnTerm : this.panelTerms) {
					if (fnTerm.getExponent() == i && !fnTerm.getVarName().toLowerCase().trim().contains("intercept")) {
						String operand = " +";
						if (fnTerm.getCoefficient() < 0) {
							operand = " -";
						}
						if (fnTerm.getVarName().equals(this.xCol)) {
							newBuilder.append(" %.2f" + fnTerm.getCoefficient() + fnTerm.getVarName() + "\u2098"
									+ toSuperScript(fnTerm.getExponent()) + operand);
						} else if (fnTerm.getVarName().equals(this.yCol)) {
							newBuilder.append(" %.2f" + fnTerm.getCoefficient() + fnTerm.getVarName() + "\u2099"
									+ toSuperScript(fnTerm.getExponent()) + operand);
						} else if (fnTerm.getIsConstant()) {
							this.hasConstants = true;
							newBuilder.append(" %.2f" + fnTerm.getCoefficient() + fnTerm.getVarName() + "(\0304)"
									+ valToSubscript(fnTerm.getValue()) + toSuperScript(fnTerm.getExponent())
									+ operand);
						}
					} else if (i == expIterator && fnTerm.getVarName().toLowerCase().trim().contains("intercept")) {
						String operand = " +";
						if (fnTerm.getCoefficient() < 0) {
							operand = " -";
						}
						interceptString = operand + fnTerm.getCoefficient();
					}
				}
			}

			newBuilder.append(interceptString);
			this.equationString = newBuilder.toString();
		}

		@Override
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2D = (Graphics2D) g;

			Font font = new Font("Arial", Font.BOLD, 20);
			FontMetrics metrics = g2D.getFontMetrics(font);
			int width = metrics.stringWidth(this.equationString);

			g2D.setFont(font);

			g2D.drawString(this.equationString, (getWidth() - width) / 2, 20);
			if(this.hasConstants) {
				font = new Font("Arial", Font.BOLD, 10);
				String disclaimer = "*Variables set to their arithmetic mean display the constant used to calculate the equation in their subscripts.";
				width = metrics.stringWidth(disclaimer);
				g2D.drawString(disclaimer, (getWidth() - width) / 2, 30);
			}
		}

		private String toSuperScript(final int num) {
			switch (num) {
			case (1):
				return "\u2071";
			case (2):
				return "\u00B2";
			case (3):
				return "\u00B3";
			case (4):
				return "\u2074";
			case (5):
				return "\u2075";
			case (6):
				return "\u2076";
			case (7):
				return "\u2077";
			case (8):
				return "\u2078";
			default:
				return "\u2079";
			}
		}

		private String valToSubscript(final double val) {
			if (val != Double.NaN) {
				String sign = " ";
				if (val < 0) {
					sign = " \208B";
				}
				StringBuilder subscriptBuilder = new StringBuilder(sign);
				String[] valueSplit = ("%.2f" + Math.abs(val)).split("");
				for (String str : valueSplit) {
					if (str == ".") {
						subscriptBuilder.append(str);
						continue;
					}
					subscriptBuilder.append(toSuperScript(Integer.valueOf(str)));
				}

				return subscriptBuilder.toString();
			}
			return "";
		}
	}
}
