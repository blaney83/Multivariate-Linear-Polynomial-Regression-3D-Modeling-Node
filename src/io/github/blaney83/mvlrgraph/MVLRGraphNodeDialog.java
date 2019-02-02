package io.github.blaney83.mvlrgraph;

import org.knime.core.node.defaultnodesettings.DefaultNodeSettingsPane;
import org.knime.core.node.defaultnodesettings.DialogComponentNumber;
import org.knime.core.node.defaultnodesettings.SettingsModelIntegerBounded;

/**
 * <code>NodeDialog</code> for the "MVLRGraph" Node.
 * This node provides a 3D representation to obtain a visual "closeness of fit" test when provided a co-efficients table output by an upstream multivariate linear regression node.
 *
 * This node dialog derives from {@link DefaultNodeSettingsPane} which allows
 * creation of a simple dialog with standard components. If you need a more 
 * complex dialog please derive directly from 
 * {@link org.knime.core.node.NodeDialogPane}.
 * 
 * @author Benjamin Laney
 */
public class MVLRGraphNodeDialog extends DefaultNodeSettingsPane {

    /**
     * New pane for configuring MVLRGraph node dialog.
     * This is just a suggestion to demonstrate possible default dialog
     * components.
     */
    protected MVLRGraphNodeDialog() {
        super();
        
        addDialogComponent(new DialogComponentNumber(
                new SettingsModelIntegerBounded(
                    MVLRGraphNodeModel.CFGKEY_COUNT,
                    MVLRGraphNodeModel.DEFAULT_COUNT,
                    Integer.MIN_VALUE, Integer.MAX_VALUE),
                    "Counter:", /*step*/ 1, /*componentwidth*/ 5));
                    
    }
}

