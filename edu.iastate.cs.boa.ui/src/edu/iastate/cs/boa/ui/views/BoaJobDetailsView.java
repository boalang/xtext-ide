/*
 * Copyright 2015, Hridesh Rajan, Sambhav Srirama,
 *                 and Iowa State University of Science and Technology
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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.security.storage.ISecurePreferences;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.osgi.framework.Bundle;

import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.CompileStatus;
import edu.iastate.cs.boa.ExecutionStatus;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.LoginException;
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
	private ISecurePreferences jobID;
	private JobHandle job;
	private ILog log;

	class ViewContentProvider implements IStructuredContentProvider {
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		public void dispose() {
		}

		public Object[] getElements(Object parent) {
			return new String[] {}; // don't populate the table
		}
	}

	class ViewLabelProvider extends LabelProvider implements ITableLabelProvider {
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
		super();
		jobID = secureStorage.node("/boa/jobID");
		Bundle bundle = Platform.getBundle(BUNDLE_ID);
		log = Platform.getLog(bundle);
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

		final String[] COLUMN_NAMES = { "Job ID", "Date Submitted", "Compilation Status", "Execution Status",
				"Input Dataset" };
		final int[] COLUMN_WIDTHS = { 50, 175, 125, 105, 125 };

		try {
			viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
			viewer.setContentProvider(new ViewContentProvider());
			viewer.setLabelProvider(new ViewLabelProvider());
			viewer.setSorter(new NameSorter());
			viewer.setInput(getViewSite());
			viewer.getTable().setLinesVisible(true);
			viewer.getTable().setHeaderVisible(true);

			for (int i = 0; i < COLUMN_NAMES.length; i++) {
				TableColumn column = new TableColumn(viewer.getTable(), SWT.CENTER);
				column.setText(COLUMN_NAMES[i]);
				column.setWidth(COLUMN_WIDTHS[i]);
				column.setMoveable(true);
				column.setResizable(true);
				column.setData(COLUMN_NAMES[i]);
			}

			TableItem item = new TableItem(viewer.getTable(), SWT.CENTER);
			int id = jobID.getInt("jobID", 0);
			if (id == 0) {
				item.setData(0);
				item.setText(new String[] {
						// Here is where we populate columns
						String.valueOf(0), String.valueOf(new Date()), String.valueOf(CompileStatus.ERROR),
						String.valueOf(ExecutionStatus.ERROR), String.valueOf("UNKNOWN") });
				showMessage("No job selected or unable to fetch job information");
				return;
			}
			job = client.getJob(id);

			item.setData(job.getId());
			item.setText(new String[] {
					// Here is where we populate columns
					String.valueOf(job.getId()), String.valueOf(job.getDate()), String.valueOf(job.getCompilerStatus()),
					String.valueOf(job.getExecutionStatus()), String.valueOf(job.getDataset()) });
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

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							job.stop();
							refreshTable.run();
							log.log(new Status(IStatus.INFO, BoaJobDetailsView.ID,
									"Stop command has been sent for Boa job " + jobID));
						} catch (NotLoggedInException e) {
							e.printStackTrace();
						} catch (BoaException e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
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
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							job.delete();
							viewer.refresh();
							jobID.putInt("jobID", 0, false);
							BoaJobsView.refresh.run();
							log.log(new Status(IStatus.INFO, BoaJobDetailsView.ID,
									"Delete command has been sent for Boa job " + jobID));
						} catch (NotLoggedInException e1) {
							e1.printStackTrace();
						} catch (BoaException e1) {
							e1.printStackTrace();
						} catch (StorageException e) {
							e.printStackTrace();
						}
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
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
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							job.resubmit();
							refreshTable.run();
							log.log(new Status(IStatus.INFO, BoaJobDetailsView.ID,
									"Resubmit command has been sent for Boa job " + jobID));
						} catch (NotLoggedInException e1) {
							e1.printStackTrace();
						} catch (BoaException e1) {
							e1.printStackTrace();
						}
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
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
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						OpenBoaView.openOutputView();
						BoaJobOutputView.refreshDisplay.run();
					}

				});

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		/*
		 * Public/Private button
		 */
		final Button accessStatus = new Button(container, SWT.PUSH);
		String currentAccessStatus = "Unknown Access Status";
		try {
			if (job.getPublic() == true) {
				currentAccessStatus = "Make Private";
			} else
				currentAccessStatus = "Make Public";
		} catch (LoginException e3) {
			e3.printStackTrace();
		} catch (BoaException e1) {
			e1.printStackTrace();
		}
		accessStatus.setText(currentAccessStatus);
		accessStatus.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							if (job.getPublic()) {
								job.setPublic(false);
								accessStatus.setText("Make Public");
							} else {
								job.setPublic(true);
								accessStatus.setText("Make Private");
							}
							accessStatus.pack(); // resize the button
						} catch (LoginException e3) {
							e3.printStackTrace();
						} catch (BoaException e1) {
							e1.printStackTrace();
							try {
								client.close();
							} catch (BoaException e2) {
								e2.printStackTrace();
							}
						}
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		/*
		 * Source Code button
		 */
		Button viewSourceCode = new Button(container, SWT.PUSH);
		viewSourceCode.setText("View Source");
		viewSourceCode.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						try {
							/* Use system time to ensure uniqueness */
							File tempFile = File.createTempFile("boaSource" + System.currentTimeMillis(), ".boa");
							PrintWriter pw = new PrintWriter(tempFile);
							pw.write("# " + tempFile.getAbsolutePath() + "\n");
							String sourceCode = job.getSource();
							if (validString(sourceCode)) {
								pw.write(job.getSource()); // write code to file
							} else {
								pw.write("Unable to fetch source code");
							}
							pw.close();

							/* Fetch path to source code temp file */
							IFileStore input = EFS.getLocalFileSystem().getStore(new Path(tempFile.getAbsolutePath()));

							/* Open the source code file with a Boa editor */
							IDE.openInternalEditorOnFileStore(
									PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), input);
						} catch (IOException e1) {
							e1.printStackTrace();
						} catch (PartInitException e1) {
							e1.printStackTrace();
						} catch (NotLoggedInException e1) {
							e1.printStackTrace();
						} catch (BoaException e1) {
							e1.printStackTrace();
						}
					}

					private boolean validString(String sourceCode) {
						return sourceCode != null && !sourceCode.isEmpty();
					}
				});
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}
		});

		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "edu.iastate.cs.boa.ui.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	/**
	 * Registers this plugin with Eclipse and configures the context menu
	 * manager so we can add items to it later.
	 */
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

	/**
	 * Standard Eclipse configuration stuff, we don't mess with this.
	 */
	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
	}

	private void fillContextMenu(IMenuManager manager) {
	}

	/**
	 * Adds items to the toolbar manager so that "refresh" shows up in the
	 * toolbar
	 * 
	 * @param manager
	 *            The manager that we add toolbar items to
	 */
	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(refreshTable);
	}

	/**
	 * Configures the "refresh" button such that pressing it will fetch the
	 * latest job ID from the cache and display it's details
	 * 
	 * @param client
	 *            The inherited BoaClient object. It should already be logged in
	 *            with a valid session.
	 */
	private void makeActions() {
		refreshTable = new Action() {
			public void run() {
				viewer.refresh();
				TableItem item = new TableItem(viewer.getTable(), SWT.CENTER);
				try {
					/* Grab the job ID from the cache */
					job = client.getJob(jobID.getInt("jobID", 0));
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
						String.valueOf(job.getId()), String.valueOf(job.getDate()),
						String.valueOf(job.getCompilerStatus()), String.valueOf(job.getExecutionStatus()),
						String.valueOf(job.getDataset().getName()) });
			}
		};
		refreshTable.setToolTipText("Refresh");
		refreshTable.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		doubleClickAction = new Action() {
			public void run() {
				ISelection selection = viewer.getSelection();
				Object obj = ((IStructuredSelection) selection).getFirstElement();
				showMessage(obj.toString());
			}
		};
	}

	/**
	 * Configures the behavior of the action taken when a user double-clicks.
	 */
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