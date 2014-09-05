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

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
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
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWebBrowser;
import org.eclipse.ui.part.ViewPart;

import edu.iastate.cs.boa.BoaClient;
import edu.iastate.cs.boa.BoaException;
import edu.iastate.cs.boa.JobHandle;
import edu.iastate.cs.boa.LoginException;
import edu.iastate.cs.boa.NotLoggedInException;

/**
 * @author ssrirama
 */
public class BoaJobsView extends ViewPart {
	/**
	 * The ID of the view as specified by the extension.
	 */
	public static final String ID = "edu.iastate.cs.boa.ui.views.BoaJobs";

	private static final int PAGE_SIZE = 10;

	private TableViewer viewer;
	private Action doubleClickAction;
	private Action prevPage;
	private Action nextPage;
	private Action refresh;
	private static int jobsOffsetIndex;
	ISecurePreferences secureStorage;
	ISecurePreferences credentials;
	ISecurePreferences jobURLs;
	BoaClient client;

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

	class NameSorter extends ViewerSorter {
	}

	public BoaJobsView() {
		secureStorage = SecurePreferencesFactory.getDefault();
		credentials = secureStorage.node("/boa/credentials");
		jobURLs = secureStorage.node("/boa/jobURLs");
		client = new BoaClient();
		jobsOffsetIndex = 0;
	}

	/**
	 * This method renders the columns in the table, then populates them with
	 * the user's Boa job information.
	 * 
	 * @param parent
	 *            the parent GUI object
	 */
	public void createPartControl(Composite parent) {
		String[] COLUMN_NAMES = { "Job ID", "Date Submitted",
				"Compilation Status", "Execution Status", "Input Dataset" };
		int[] COLUMN_WIDTHS = { 50, 175, 150, 125, 150 };

		try {
			client.login(credentials.get("username", ""),
					credentials.get("password", ""));
		} catch (final LoginException e) {
			e.printStackTrace();
		} catch (final StorageException e) {
			e.printStackTrace();
		}

		// Table appearance configuration
		viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
				| SWT.V_SCROLL);
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

		try {
			List<JobHandle> jobs = client
					.getJobList(jobsOffsetIndex, PAGE_SIZE);
			for (int i = 0; i < jobs.size(); i++) {
				// Cache the job URL
				jobURLs.put(String.valueOf(jobs.get(i).getId()), jobs.get(i)
						.getUrl().toString(), false);
				TableItem item = new TableItem(viewer.getTable(), SWT.CENTER);
				item.setData(String.valueOf(jobs.get(i).getId()));
				item.setText(new String[] {
						// Here is where we populate columns
						String.valueOf(jobs.get(i).getId()),
						String.valueOf(jobs.get(i).getDate()),
						String.valueOf(jobs.get(i).getCompilerStatus()),
						String.valueOf(jobs.get(i).getExecutionStatus()),
						String.valueOf(jobs.get(i).getDataset()) });
			}
			jobURLs.flush();
			client.close();
		} catch (final NotLoggedInException e) {
			e.printStackTrace();
		} catch (final BoaException e) {
			e.printStackTrace();
			try {
				client.close();
			} catch (BoaException e2) {
				showMessage("Please restart Eclipse!");
			}
		} catch (final StorageException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}

		PlatformUI.getWorkbench().getHelpSystem()
				.setHelp(viewer.getControl(), "edu.iastate.cs.boa.ui.viewer");
		makeActions(client);
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(final IMenuManager manager) {
				BoaJobsView.this.fillContextMenu(manager);
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

	private void fillLocalPullDown(final IMenuManager manager) {
	}

	private void fillContextMenu(final IMenuManager manager) {
		manager.add(prevPage);
		manager.add(nextPage);
		manager.add(refresh);
	}

	private void fillLocalToolBar(final IToolBarManager manager) {
		manager.add(prevPage);
		manager.add(nextPage);
		manager.add(refresh);
	}

	private void makeActions(final BoaClient client) {
		refresh = new Action() {
			public void run() {
				jobsOffsetIndex = 0;
				jobURLs.clear();
				paginate(jobsOffsetIndex);
			}
		};
		refresh.setText("Refresh");
		refresh.setToolTipText("Refresh");
		refresh.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_ELCL_SYNCED));

		prevPage = new Action() {
			public void run() {
				if (jobsOffsetIndex - PAGE_SIZE < 0) {
					jobsOffsetIndex = 0;
				} else {
					jobsOffsetIndex -= PAGE_SIZE;
				}
				paginate(jobsOffsetIndex);
			}
		};
		prevPage.setText("Previous Page");
		prevPage.setToolTipText("Previous Page");
		prevPage.setActionDefinitionId("Prev Page");
		prevPage.setDescription("Go to previous page of Boa jobs");
		prevPage.setId("Previous Boa Jobs Page");
		prevPage.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_BACK));

		nextPage = new Action() {
			public void run() {
				try {
					client.login(credentials.get("username", ""),
							credentials.get("password", ""));

					if (jobsOffsetIndex + PAGE_SIZE > client.getJobCount()) {
						client.close();
						return;
					}
					client.close();
				} catch (final NotLoggedInException e) {
					e.printStackTrace();
				} catch (final BoaException e) {
					e.printStackTrace();
					try {
						client.close();
					} catch (BoaException e2) {
						showMessage("Please restart Eclipse!");
					}
				} catch (final StorageException e) {
					e.printStackTrace();
				}
				jobsOffsetIndex += PAGE_SIZE;
				paginate(jobsOffsetIndex);
			}
		};
		nextPage.setText("Next Page");
		nextPage.setToolTipText("Next Page");
		nextPage.setActionDefinitionId("Next Page");
		nextPage.setDescription("Go to next page of Boa jobs");
		nextPage.setId("Next Boa Jobs Page");
		nextPage.setImageDescriptor(PlatformUI.getWorkbench().getSharedImages()
				.getImageDescriptor(ISharedImages.IMG_TOOL_FORWARD));

		doubleClickAction = new Action() {
			public void run() {
				try {
					client.login(credentials.get("username", ""),
							credentials.get("password", ""));
					ISelection selection = viewer.getSelection();
					Object obj = ((IStructuredSelection) selection)
							.getFirstElement();

					final IWebBrowser browser = PlatformUI.getWorkbench()
							.getBrowserSupport().createBrowser("Boa");

					String URL = jobURLs.get(obj.toString(), "");
					browser.openURL(new URL(URL));
					client.close();
				} catch (final PartInitException e) {
					e.printStackTrace();
				} catch (final NumberFormatException e) {
					e.printStackTrace();
				} catch (final NotLoggedInException e) {
					e.printStackTrace();
				} catch (final BoaException e) {
					e.printStackTrace();
				} catch (final StorageException e) {
					e.printStackTrace();
				} catch (final MalformedURLException e) {
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
			client.login(credentials.get("username", ""),
					credentials.get("password", ""));

			viewer.refresh();
			List<JobHandle> jobs = client.getJobList(offset, PAGE_SIZE);

			for (int i = 0; i < jobs.size(); i++) {
				// Cache the job URL
				jobURLs.put(String.valueOf(jobs.get(i).getId()), jobs.get(i)
						.getUrl().toString(), false);

				TableItem item = new TableItem(viewer.getTable(), SWT.CENTER);
				item.setData(String.valueOf(jobs.get(i).getId()));
				item.setText(new String[] {
						// Here is where we populate columns
						String.valueOf(jobs.get(i).getId()),
						String.valueOf(jobs.get(i).getDate()),
						String.valueOf(jobs.get(i).getCompilerStatus()),
						String.valueOf(jobs.get(i).getExecutionStatus()),
						jobs.get(i).getDataset().getName() });
			}
			jobURLs.flush();
			client.close();
		} catch (final NotLoggedInException e) {
			e.printStackTrace();
		} catch (final BoaException e) {
			e.printStackTrace();
			try {
				client.close();
			} catch (BoaException e2) {
				showMessage("Please restart Eclipse!");
			}
		} catch (final StorageException e) {
			e.printStackTrace();
		} catch (final IOException e) {
			e.printStackTrace();
		}
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(final DoubleClickEvent event) {
				doubleClickAction.run();
			}
		});
	}

	private void showMessage(String message) {
		MessageDialog.openInformation(null, "Boa View", message);
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}
}
