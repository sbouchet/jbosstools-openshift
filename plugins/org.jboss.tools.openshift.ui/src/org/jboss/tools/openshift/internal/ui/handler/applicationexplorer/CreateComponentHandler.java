/*******************************************************************************
 * Copyright (c) 2020 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.internal.ui.handler.applicationexplorer;

import java.io.IOException;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;
import org.jboss.tools.common.ui.WizardUtils;
import org.jboss.tools.common.ui.notification.LabelNotification;
import org.jboss.tools.openshift.core.odo.Odo;
import org.jboss.tools.openshift.internal.common.ui.utils.UIUtils;
import org.jboss.tools.openshift.internal.ui.OpenShiftUIActivator;
import org.jboss.tools.openshift.internal.ui.models.AbstractOpenshiftUIElement;
import org.jboss.tools.openshift.internal.ui.models.applicationexplorer.ApplicationElement;
import org.jboss.tools.openshift.internal.ui.models.applicationexplorer.ApplicationExplorerUIModel;
import org.jboss.tools.openshift.internal.ui.models.applicationexplorer.ProjectElement;
import org.jboss.tools.openshift.internal.ui.wizard.applicationexplorer.CreateComponentModel;
import org.jboss.tools.openshift.internal.ui.wizard.applicationexplorer.CreateComponentWizard;

/**
 * @author Red Hat Developers
 */
public class CreateComponentHandler extends OdoHandler {

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		ApplicationElement application = null;
		ProjectElement project = UIUtils.getFirstElement(selection, ProjectElement.class);
		if (project == null) {
			application = UIUtils.getFirstElement(selection, ApplicationElement.class);
			if (application == null) {
				return OpenShiftUIActivator.statusFactory().cancelStatus("No project or application selected"); //$NON-NLS-1$
			}
			project = application.getParent();
		}
    final Shell parent = HandlerUtil.getActiveShell(event);
		try {
			openDialog(application, project, parent);
			return Status.OK_STATUS;
		} catch (IOException e) {
			return OpenShiftUIActivator.statusFactory().errorStatus(e);
		}
	}

  private static void openDialog(ApplicationElement application, ProjectElement project, final Shell parent)
      throws IOException {
    Odo odo = project.getParent().getOdo();
    final CreateComponentModel model = new CreateComponentModel(odo, odo.getComponentTypes(),
            project.getWrapped().getMetadata().getName(),
            application == null ? "" : application.getWrapped().getName());
    final IWizard createComponentWizard = new CreateComponentWizard(model);
    if (WizardUtils.openWizardDialog(createComponentWizard, parent) == Window.OK) {
    	AbstractOpenshiftUIElement<?, ?, ApplicationExplorerUIModel> element = application==null?project:application;
    	executeInJob("Creating component", monitor -> execute(parent, model, element));
    }
  }

	/**
	 * @param shell
	 * @param model
	 * @return
	 */
	private static void execute(Shell shell, CreateComponentModel model,
	        AbstractOpenshiftUIElement<?, ?, ApplicationExplorerUIModel> element) {
		LabelNotification notification = LabelNotification.openNotification(shell, "Creating component " + model.getComponentName());
		try {
			model.getOdo().createComponentLocal(model.getProjectName(), model.getApplicationName(),
			        model.getSelectedComponentType().getName(), model.getSelectedComponentVersion(),
			        model.getComponentName(), model.getEclipseProject().getLocation().toOSString(),
			        model.isEclipseProjectHasDevfile()?CreateComponentModel.DEVFILE_NAME:null, 
			        model.getSelectedComponentStarter()==null?null:model.getSelectedComponentStarter().getName(), model.isPushAfterCreate());
			LabelNotification.openNotification(notification, shell, "Component " + model.getComponentName() + " created");
			element.getRoot().addContext(model.getEclipseProject());
			element.refresh();
		} catch (IOException e) {
			shell.getDisplay().asyncExec(() -> {
				notification.close();
				MessageDialog.openError(shell, "Create component",
				        "Error creating component " + model.getComponentName());
			});
		}
	}

  /**
   * @param shell
   * @param parent
   */
  public static void openDialog(Shell shell, AbstractOpenshiftUIElement<?, ?, ApplicationExplorerUIModel> parent) {
    try {
      if (parent instanceof ProjectElement) {
        openDialog(null, (ProjectElement) parent, shell);
      } else if (parent instanceof ApplicationElement) {
        ApplicationElement application = (ApplicationElement) parent;
        openDialog(application, application.getParent(), shell);
      }
    } catch (IOException e) {
      MessageDialog.openError(shell, "Create component",
          "Error creating component");
    }
    
  }
}
