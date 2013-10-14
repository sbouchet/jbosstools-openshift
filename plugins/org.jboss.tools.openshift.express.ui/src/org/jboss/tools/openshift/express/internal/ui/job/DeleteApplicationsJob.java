/*******************************************************************************
 * Copyright (c) 2012 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.jboss.tools.openshift.express.internal.ui.job;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.jboss.tools.openshift.express.internal.ui.OpenShiftUIActivator;

import com.openshift.client.IApplication;
import com.openshift.client.OpenShiftException;

/**
 * @author Andre Dietisheim
 */
public class DeleteApplicationsJob extends AbstractDelegatingMonitorJob {

	private List<IApplication> applications;

	public DeleteApplicationsJob(final List<IApplication> applications) {
		super("Deleting OpenShift Application(s)..."); 
		this.applications = applications;
	}
	
	@Override
	protected IStatus doRun(IProgressMonitor monitor) {
		int totalWork = applications.size();
		monitor.beginTask("Deleting OpenShift Application(s)...", totalWork);
		for (final IApplication application : applications) {
			final String appName = application.getName();
			try {
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				monitor.subTask("Deleting Application " + application.getName());
				application.destroy();
				monitor.worked(1);
			} catch (OpenShiftException e) {
				return OpenShiftUIActivator.createErrorStatus(
						NLS.bind("Failed to delete application \"{0}\"", appName), e);
			} finally {
				monitor.done();
			}
		}
		return Status.OK_STATUS;
	}
}