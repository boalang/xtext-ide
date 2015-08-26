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

import java.io.IOException;
import java.util.List;

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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.NotLoggedInException;
import edu.iastate.cs.boa.ui.handlers.OpenBoaView;

/**
 * @author ssrirama
 */
public class BoaJobsView extends BoaAbstractView {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.iastate.cs.boa.ui.views.BoaJobs";

	/**
	 * The number of jobs to display per page
	 */
	private static final int PAGE_SIZE = 10;

	private TableViewer viewer;
	private Action doubleClickAction;
	private Action prevPage;
	private Action nextPage;
	public static Action refresh;

	/**
	 * Used for keeping track of which job (plus the ten jobs after it) is
	 * currently being viewed
	 */
	private static int jobsOffsetIndex;

	/**
	 * Is used so that the SubmitToBoaHandler has the URL of the current job.
	 * This is so that it can display the webpage in an in-Eclipse tab
	 */
	private ISecurePreferences jobURLs;

	/**
	 * Job IDs stored using this mechanism will be fetched by the
	 * BoaJobsDetailsView in order to display the most recently accessed job
	 */
	private ISecurePreferences forDetailsView;

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

	public BoaJobsView() {
		super();
		jobURLs = secureStorage.node("/boa/jobURLs");
		forDetailsView = secureStorage.node("/boa/jobID");
		jobsOffsetIndex = 0;
	}

	/**
	 * This method renders the columns in the table, then populates them with
	 * the user's Boa job information.
	 * 
	 * @param parent
	 *            The parent GUI object
	 */
	public void createPartControl(Composite parent) {
		// if(true == true){
		// debuggerJobsStorage.clear();
		// return;
		// }
		
		String[] COLUMN_NAMES = { "Job ID", "Date Submitted", "Compilation Status", "Execution Status",
				"Input Dataset" };
		int[] COLUMN_WIDTHS = { 50, 175, 150, 125, 150 };

		// Table appearance configuration
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(new ViewLabelProvider());
		viewer.setSorter(new NameSorter());
		viewer.setInput(getViewSite());
		viewer.getTable().setLinesVisible(true);
		viewer.getTable().setHeaderVisible(true);

		// Add all the columns into the table
		for (int i = 0; i < COLUMN_NAMES.length; i++) {
			TableColumn column = new TableColumn(viewer.getTable(), SWT.CENTER);
			column.setText(COLUMN_NAMES[i]);
			column.setWidth(COLUMN_WIDTHS[i]);
			column.setMoveable(true);
			column.setResizable(true);
			column.setData(COLUMN_NAMES[i]);
		}

		/* Populate table with 10 most recent jobs */
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				paginate(0);
			}
		});

		PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(), "edu.iastate.cs.boa.ui.viewer");
		makeActions(client);
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
				BoaJobsView.this.fillContextMenu(manager);
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

	private void fillLocalPullDown(final IMenuManager manager) {
	}

	/**
	 * Adds items to the context menu manager so that "previous", "next", and
	 * "refresh" show up when a user right clicks
	 * 
	 * @param manager
	 *            The manager that we add menu items to
	 */
	private void fillContextMenu(final IMenuManager manager) {
		manager.add(prevPage);
		manager.add(nextPage);
		manager.add(refresh);
	}
	/**
	 * Adds items to the toolbar manager so that "previous", "next", and
	 * "refresh" show up in the toolbar
	 * 
	 * @param manager
	 *            The manager that we add toolbar items to
	 */
	private void fillLocalToolBar(final IToolBarManager manager) {
		manager.add(prevPage);
		manager.add(nextPage);
		manager.add(refresh);
	}

	/**
	 * Configures the "previous", "next", and "refresh" buttons such that
	 * pressing them will, respectively, display the previous 10 jobs, the next
	 * 10 jobs, or reset the view to the first 10 jobs available.
	 * 
	 * @param client
	 *            The inherited BoaClient object. It should already be logged in
	 *            with a valid session.
	 */
	private void makeActions(final BoaClient client) {
		refresh = new Action() {
			public void run() {
				jobsOffsetIndex = 0;
				jobURLs.clear();

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						paginate(jobsOffsetIndex);
					}
				});

			}
		};
		refresh.setText("Refresh");
		refresh.setToolTipText("Refresh");
		refresh.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		prevPage = new Action() {
			public void run() {
				if (jobsOffsetIndex - PAGE_SIZE < 0) {
					jobsOffsetIndex = 0;
				} else {
					jobsOffsetIndex -= PAGE_SIZE;
				}

				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						paginate(jobsOffsetIndex);
					}
				});
			}
		};
		prevPage.setText("Previous Page");
		prevPage.setToolTipText("Previous Page");
		prevPage.setActionDefinitionId("Prev Page");
		prevPage.setDescription("Go to previous page of Boa jobs");
		prevPage.setId("Previous Boa Jobs Page");
		prevPage.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_BACK));

		nextPage = new Action() {
			public void run() {
				try {
					if (jobsOffsetIndex + PAGE_SIZE > client.getJobCount())
						return;
				} catch (final NotLoggedInException e) {
					e.printStackTrace();
				} catch (final BoaException e) {
					e.printStackTrace();
					try {
						client.close();
					} catch (final BoaException e2) {
						showMessage("Please restart Eclipse!");
					}
				}
				jobsOffsetIndex += PAGE_SIZE;
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						paginate(jobsOffsetIndex);
					}
				});
			}
		};
		nextPage.setText("Next Page");
		nextPage.setToolTipText("Next Page");
		nextPage.setActionDefinitionId("Next Page");
		nextPage.setDescription("Go to next page of Boa jobs");
		nextPage.setId("Next Boa Jobs Page");
		nextPage.setImageDescriptor(
				PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));

		doubleClickAction = new Action() {
			public void run() {
				try {
					ISelection selection = viewer.getSelection();
					Object obj = ((IStructuredSelection) selection).getFirstElement();

					/* Cache the job ID selected */
					forDetailsView.putInt("jobID", Integer.valueOf(obj.toString()), false);
					OpenBoaView.openDetailsView();
					BoaJobDetailsView.refreshTable.run();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				} catch (final StorageException e) {
					e.printStackTrace();
				}
			}
		};
	}

	/**
	 * Populates the table with new Boa jobs information.
	 * 
	 * @param offset
	 *            the integer supplied to the BoaClient.getJobList(int offset)
	 *            method call. Supply 0 for full table refresh.
	 */
	private void paginate(int offset) {
		try {
			viewer.refresh();
			List<JobHandle> jobs = client.getJobList(offset, PAGE_SIZE);

			for (int i = 0; i < jobs.size(); i++) {
				// Cache the job URL
				jobURLs.put(String.valueOf(jobs.get(i).getId()), jobs.get(i).getUrl().toString(), false);

				Runnable update = new UpdateJobsListTask(i, jobs.get(i));
				Display.getDefault().asyncExec(update);
			}
			jobURLs.flush();
		} catch (final NotLoggedInException e) {
			e.printStackTrace();
		} catch (final BoaException e) {
			e.printStackTrace();
			try {
				client.close();
			} catch (final BoaException e2) {
				showMessage("Please restart Eclipse!");
			}
		} catch (final StorageException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	public class UpdateJobsListTask implements Runnable {
		public TableItem item;
		public JobHandle job;
		public int iter;

		public UpdateJobsListTask(int i, JobHandle job) {
			this.job = job;
			this.iter = i;
		}

		public UpdateJobsListTask() {
		}

		public void run() {
			if (iter == 0) {
				viewer.refresh();
			}
			this.item = new TableItem(viewer.getTable(), SWT.CENTER);
			item.setData(String.valueOf(job.getId()));
			item.setText(new String[] {
					// Here is where we populate columns
					String.valueOf(job.getId()), String.valueOf(job.getDate()),
					String.valueOf(job.getId() < 0 ? "FINISHED" : job.getCompilerStatus()),
					String.valueOf(job.getExecutionStatus()),
					String.valueOf(job.getId() < 0 ? "2013 September (small)" : job.getDataset()) });
		}
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
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