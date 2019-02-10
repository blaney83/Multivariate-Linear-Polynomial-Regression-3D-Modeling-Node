package io.github.blaney83.mvlrgraph;

import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.SettingsModelString;


/**
 * <code>NodeView</code> for the "MVLRGraph" Node. This node provides a 3D
 * representation to obtain a visual "closeness of fit" test when provided a
 * co-efficients table output by an upstream multivariate linear regression
 * node.
 *
 * @author Benjamin Laney
 */
public class MVLRGraphNodeView extends NodeView<MVLRGraphNodeModel> {

	// view config variables
	public static final String CFGKEY_GRAPH_TITLE = "graphTitle";
	public static final String CFGKEY_GRAPH_EQUATION = "graphEquation";

	// view defaults
	static final String DEFAULT_GRAPH_TITLE = "Regression Model (plane) & Real Data (scatter)";
	static final boolean DEFAULT_GRAPH_EQUATION = true;

	// settings models
	final SettingsModelString m_graphTitle = new SettingsModelString(CFGKEY_GRAPH_EQUATION, DEFAULT_GRAPH_TITLE);
	
	private MVLRGraphNodeViewPanel m_borderLayout;

	protected MVLRGraphNodeView(final MVLRGraphNodeModel nodeModel) {
		super(nodeModel);
		m_borderLayout = new MVLRGraphNodeViewPanel(nodeModel);
		setComponent(m_borderLayout);
	}

	@Override
	protected void modelChanged() {

		MVLRGraphNodeModel updatedModel = getNodeModel();
		if(updatedModel != null) {
			m_borderLayout.updateView(updatedModel);
			setComponent(m_borderLayout);
		}

	}

	@Override
	protected void onClose() {

	}

	@Override
	protected void onOpen() {

	}

}
