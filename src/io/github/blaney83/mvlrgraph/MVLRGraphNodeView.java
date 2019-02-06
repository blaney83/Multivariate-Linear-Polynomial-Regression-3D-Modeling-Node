package io.github.blaney83.mvlrgraph;

import java.util.Set;

import javax.swing.JMenuItem;

import org.knime.core.node.NodeView;
import org.knime.core.node.defaultnodesettings.SettingsModelBoolean;
import org.knime.core.node.property.hilite.HiLiteHandler;
import org.knime.core.node.property.hilite.HiLiteListener;

/**
 * <code>NodeView</code> for the "MVLRGraph" Node.
 * This node provides a 3D representation to obtain a visual "closeness of fit" test when provided a co-efficients table output by an upstream multivariate linear regression node.
 *
 * @author Benjamin Laney
 */
public class MVLRGraphNodeView extends NodeView<MVLRGraphNodeModel> implements HiLiteListener {
	
	//Selected Points
	private final Set<CalculatedPoint> m_selected;
	
	//Dialog View Variables
	//dynamic point coloring
	//custom plane color
	
	// JMenu items
	private final JMenuItem m_hilite;
	private final JMenuItem m_unhilite;
	//rotate
	
	// Local HiLiteHandler
	private HiLiteHandler m_hiliteHandler = null;

    protected MVLRGraphNodeView(final MVLRGraphNodeModel nodeModel) {
        super(nodeModel);

        // TODO instantiate the components of the view here.

    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void modelChanged() {

        // TODO retrieve the new model from your nodemodel and 
        // update the view.
        MVLRGraphNodeModel nodeModel = 
            (MVLRGraphNodeModel)getNodeModel();
        assert nodeModel != null;
        
        // be aware of a possibly not executed nodeModel! The data you retrieve
        // from your nodemodel could be null, emtpy, or invalid in any kind.
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onClose() {
    
        // TODO things to do when closing the view
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onOpen() {

        // TODO things to do when opening the view
    }

}

