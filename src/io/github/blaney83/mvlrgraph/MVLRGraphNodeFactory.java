package io.github.blaney83.mvlrgraph;

import org.knime.core.node.NodeDialogPane;
import org.knime.core.node.NodeFactory;
import org.knime.core.node.NodeView;

/**
 * <code>NodeFactory</code> for the "MVLRGraph" Node.
 * This node provides a 3D representation to obtain a visual "closeness of fit" test when provided a co-efficients table output by an upstream multivariate linear regression node.
 *
 * @author Benjamin Laney
 */
public class MVLRGraphNodeFactory 
        extends NodeFactory<MVLRGraphNodeModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public MVLRGraphNodeModel createNodeModel() {
        return new MVLRGraphNodeModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNrNodeViews() {
        return 1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeView<MVLRGraphNodeModel> createNodeView(final int viewIndex,
            final MVLRGraphNodeModel nodeModel) {
        return new MVLRGraphNodeView(nodeModel);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasDialog() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public NodeDialogPane createNodeDialogPane() {
        return new MVLRGraphNodeDialog();
    }

}

