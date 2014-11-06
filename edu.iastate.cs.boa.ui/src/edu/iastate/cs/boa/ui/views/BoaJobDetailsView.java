/*
 * Copyright 2014, Hridesh Rajan, Robert Dyer, Sambhav Srirama,
 *                 Iowa State University of Science and Technology
 *                 and Bowling Green State University
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package edu.iastate.cs.boa.ui.views;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.xtext.ui.XtextProjectHelper;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.NotLoggedInException;
import edu.iastate.cs.boa.ui.handlers.OpenBoaView;

/**
 * @author ssrirama
 */

public class BoaJobDetailsView extends BoaAbstractView {

	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.iastate.cs.boa.ui.views.BoaJobDetails";

	protected TableViewer viewer;
	private Action doubleClickAction;
	protected static Action refreshTable;
	private ISecurePreferences secureStorage;
	private ISecurePreferences credentials;
	private ISecurePreferences jobID;
	private BoaClient client;
	private JobHandle job;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return new String[] {}; // don't populate the table
		}
	}

	class ViewLabelProvider extends LabelProvider implements
			ITableLabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}
	}

	/*
	 * Later, for making the columns sortable.
	 */
	class NameSorter extends ViewerSorter {
	}

	public BoaJobDetailsView() {
		client = new BoaClient();
		secureStorage = SecurePreferencesFactory.getDefault();
		credentials = secureStorage.node("/boa/credentials");
		jobID = secureStorage.node("/boa/jobID");
	}

	/**
	 * This method renders the columns in the table, then populates them with
	 * the user's Boa job information.
	 * 
	 * @param parent
	 *            The parent GUI object
	 */
	public void createPartControl(final Composite parent) {
		int NUM_BUTTONS = 6;

		final String[] COLUMN_NAMES = { "Job ID", "Date Submitted",
				"Compilation Status", "Execution Status", "Input Dataset" };
		final int[] COLUMN_WIDTHS = { 50, 175, 125, 105, 125 };

		try {
			client.login(credentials.get("username", ""),
					credentials.get("password", ""));

			viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
					| SWT.V_SCROLL);
			viewer.setContentProvider(new ViewContentProvider());
			viewer.setLabelProvider(new ViewLabelProvider());
			viewer.setSorter(new NameSorter());
			viewer.setInput(getViewSite());
			viewer.getTable().setLinesVisible(true);
			viewer.getTable().setHeaderVisible(true);

			for (int i = 0; i < COLUMN_NAMES.length; i++) {
				TableColumn column = new TableColumn(viewer.getTable(),
						SWT.CENTER);
				column.setText(COLUMN_NAMES[i]);
				column.setWidth(COLUMN_WIDTHS[i]);
				column.setMoveable(true);
				column.setResizable(true);
				column.setData(COLUMN_NAMES[i]);
			}

			TableItem item = new TableItem(viewer.getTable(), SWT.CENTER);

			job = client.getJob(jobID.getInt("jobID", 0));

			item.setData(job.getId());
			item.setText(new String[] {
					// Here is where we populate columns
					String.valueOf(job.getId()), String.valueOf(job.getDate()),
					String.valueOf(job.getCompilerStatus()),
					String.valueOf(job.getExecutionStatus()),
					String.valueOf(job.getDataset()) });

			client.close();

		} catch (StorageException e) {
			e.printStackTrace();
		} catch (NotLoggedInException e) {
			e.printStackTrace();
		} catch (BoaException e) {
			e.printStackTrace();
			try {
				client.close();
			} catch (BoaException e2) {
				showMessage("Please restart Eclipse!");
			}
		}

		Composite container = new Composite(parent, SWT.LEFT_TO_RIGHT);
		container.setLayout(new GridLayout(NUM_BUTTONS, false));

		/*
		 * Stop button
		 */
		Button stop = new Button(container, SWT.PUSH);
		stop.setText("Stop");
		stop.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					client.login(credentials.get("username", ""),
							credentials.get("password", ""));
					job.stop();
					client.close();
					refreshTable.run();
					showMessage("Job has been stopped!");
				} catch (NotLoggedInException e1) {
					e1.printStackTrace();
				} catch (BoaException e1) {
					e1.printStackTrace();
					try {
						client.close();
					} catch (BoaException e2) {
						/*
						 * If the program reaches here, you've got bigger
						 * problems...
						 */
						showMessage("Please restart Eclipse!");
					}
				} catch (StorageException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		/*
		 * Delete button
		 */
		Button delete = new Button(container, SWT.PUSH);
		delete.setText("Delete");
		delete.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					client.login(credentials.get("username", ""),
							credentials.get("password", ""));
					job.delete();
					client.close();
					viewer.refresh();
					BoaJobsView.refresh.run();
					showMessage("Job has been deleted!");
				} catch (NotLoggedInException e1) {
					e1.printStackTrace();
				} catch (BoaException e1) {
					e1.printStackTrace();
					try {
						client.close();
					} catch (BoaException e2) {
						showMessage("Please restart Eclipse!");
					}
				} catch (StorageException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		/*
		 * Resubmit button
		 */
		Button resubmit = new Button(container, SWT.PUSH);
		resubmit.setText("Resubmit");
		resubmit.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				try {
					client.login(credentials.get("username", ""),
							credentials.get("password", ""));
					job.resubmit();
					client.close();
					refreshTable.run();
					showMessage("Job has been resubmitted!");
				} catch (NotLoggedInException e1) {
					e1.printStackTrace();
				} catch (BoaException e1) {
					e1.printStackTrace();
					try {
						client.close();
					} catch (BoaException e2) {
						showMessage("Please restart Eclipse!");
					}
				} catch (StorageException e1) {
					e1.printStackTrace();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		/*
		 * Output button
		 */
		Button output = new Button(container, SWT.PUSH);
		output.setText("Output");
		output.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				OpenBoaView.openOutputView();
				BoaJobOutputView.refreshDisplay.run();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		/*
		 * Public/Private button
		 */

		/*
		 * final Button accessStatus = new Button(container, SWT.PUSH); String
		 * currentAccessStatus = "Unknown Access Status"; try {
		 * client.login(credentials.get("username", ""),
		 * credentials.get("password", "")); if (job.getPublic()==true) {
		 * currentAccessStatus = "Make Private"; } else currentAccessStatus =
		 * "Make Public"; client.close(); } catch (LoginException e3) {
		 * e3.printStackTrace(); } catch (StorageException e3) {
		 * e3.printStackTrace(); } catch (BoaException e1) {
		 * e1.printStackTrace(); try { client.close(); } catch (BoaException e2)
		 * { e2.printStackTrace(); } }
		 * accessStatus.setText(currentAccessStatus);
		 * accessStatus.addSelectionListener(new SelectionListener() {
		 * 
		 * @Override public void widgetSelected(SelectionEvent e) { try {
		 * client.login(credentials.get("username", ""),
		 * credentials.get("password", "")); if (job.getPublic()) {
		 * job.setPublic(false); accessStatus.setText("Make Public"); } else {
		 * job.setPublic(true); accessStatus.setText("Make Private"); }
		 * client.close(); accessStatus.pack(); // resize the button
		 * client.close(); } catch (LoginException e3) { e3.printStackTrace(); }
		 * catch (StorageException e3) { e3.printStackTrace(); } catch
		 * (BoaException e1) { e1.printStackTrace(); try { client.close(); }
		 * catch (BoaException e2) { e2.printStackTrace(); } } }
		 * 
		 * @Override public void widgetDefaultSelected(SelectionEvent e) {
		 * 
		 * }
		 * 
		 * });
		 */

		/*
		 * Source Code button
		 */
		Button viewSourceCode = new Button(container, SWT.PUSH);
		viewSourceCode.setText("View Source");
		viewSourceCode.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
				// String projectName = createProjectName(root);
				IProject project = root.getProject("Boa_Source_Code");
				try {

					if (!project.exists()) {
						project.create(null);
						project.open(null);

						IProjectDescription description = project
								.getDescription();
						String[] natureIDs = { XtextProjectHelper.NATURE_ID };
						description.setNatureIds(natureIDs);

						IFolder sourceFolder = project.getFolder("src");
						sourceFolder.create(false, true, null);
					}
					IFile sourceFile = project.getFile("src/Code_"
							+ System.currentTimeMillis() + ".boa");

					InputStream inputStream = new ByteArrayInputStream(job
							.getSource().getBytes());

					sourceFile.create(inputStream, false, null);

				} catch (CoreException e1) {
					e1.printStackTrace();
				} catch (NotLoggedInException e1) {
					e1.printStackTrace();
				} catch (BoaException e1) {
					e1.printStackTrace();
				}
			}

			private String createProjectName(IWorkspaceRoot root) {

				String toReturn = "project_" + System.currentTimeMillis();

				/*
				 * Double check that a project with the name doesn't already
				 * exist
				 */
				for (IProject project : root.getProjects()) {
					if (project.getName().equalsIgnoreCase(toReturn))
						// try again
						toReturn = "project_" + System.currentTimeMillis();
				}

				return toReturn;
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}

		});

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "edu.iastate.cs.boa.ui.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				BoaJobDetailsView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {

	}

	private void fillContextMenu(IMenuManager manager) {
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshTable);
	}

	private void makeActions() {
		refreshTable = new Action() {
			public void run() {
				/*
				 * Clear the table
				 */
				viewer.refresh();

				TableItem item = new TableItem(viewer.getTable(), SWT.CENTER);

				try {
					/*
					 * Grab the job ID from the cache
					 */
					client.login(credentials.get("username", ""),
							credentials.get("password", ""));
					job = client.getJob(jobID.getInt("jobID", 0));
					client.close();
				} catch (NotLoggedInException e) {
					e.printStackTrace();
				} catch (BoaException e) {
					e.printStackTrace();
					try {
						client.close();
					} catch (BoaException e1) {
						e1.printStackTrace();
					}
				} catch (StorageException e) {
					e.printStackTrace();
				}

				item.setData(job.getId());
				item.setText(new String[] {
						// Here is where we populate columns
						String.valueOf(job.getId()),
						String.valueOf(job.getDate()),
						String.valueOf(job.getCompilerStatus()),
						String.valueOf(job.getExecutionStatus()),
						String.valueOf(job.getDataset().getName()) });

			}
		};
		refreshTable.setToolTipText("Refresh");
		refreshTable.setImageDescriptor(PlatformUI.getWorkbench()
				.getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection)
						.getFirstElement();
				showMessage(obj.toString());
			}
		};
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}