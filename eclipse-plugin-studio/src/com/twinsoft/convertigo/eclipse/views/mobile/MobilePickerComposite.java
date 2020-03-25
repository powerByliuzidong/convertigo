/*
 * Copyright (c) 2001-2020 Convertigo SA.
 * 
 * This program  is free software; you  can redistribute it and/or
 * Modify  it  under the  terms of the  GNU  Affero General Public
 * License  as published by  the Free Software Foundation;  either
 * version  3  of  the  License,  or  (at your option)  any  later
 * version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY;  without even the implied warranty of
 * MERCHANTABILITY  or  FITNESS  FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program;
 * if not, see <http://www.gnu.org/licenses/>.
 */

package com.twinsoft.convertigo.eclipse.views.mobile;

import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaObject;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.twinsoft.convertigo.beans.connectors.CouchDbConnector;
import com.twinsoft.convertigo.beans.core.Connector;
import com.twinsoft.convertigo.beans.core.DatabaseObject;
import com.twinsoft.convertigo.beans.core.MobileComponent;
import com.twinsoft.convertigo.beans.core.Project;
import com.twinsoft.convertigo.beans.core.RequestableObject;
import com.twinsoft.convertigo.beans.core.Transaction;
import com.twinsoft.convertigo.beans.core.Variable;
import com.twinsoft.convertigo.beans.couchdb.DesignDocument;
import com.twinsoft.convertigo.beans.mobile.components.ApplicationComponent;
import com.twinsoft.convertigo.beans.mobile.components.IAction;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSource;
import com.twinsoft.convertigo.beans.mobile.components.PageComponent;
import com.twinsoft.convertigo.beans.mobile.components.UIActionStack;
import com.twinsoft.convertigo.beans.mobile.components.UIComponent;
import com.twinsoft.convertigo.beans.mobile.components.UIControlDirective;
import com.twinsoft.convertigo.beans.mobile.components.UICustomAction;
import com.twinsoft.convertigo.beans.mobile.components.UIDynamicAction;
import com.twinsoft.convertigo.beans.mobile.components.UIDynamicMenu;
import com.twinsoft.convertigo.beans.mobile.components.UIForm;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSource.Filter;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSource.SourceData;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSource.SourceModel;
import com.twinsoft.convertigo.beans.mobile.components.MobileSmartSourceType;
import com.twinsoft.convertigo.beans.transactions.couchdb.GetViewTransaction;
import com.twinsoft.convertigo.eclipse.ConvertigoPlugin;
import com.twinsoft.convertigo.eclipse.dnd.MobileSource;
import com.twinsoft.convertigo.eclipse.dnd.MobileSourceTransfer;
import com.twinsoft.convertigo.eclipse.editors.connector.ConnectorEditor;
import com.twinsoft.convertigo.eclipse.editors.connector.ConnectorEditorInput;
import com.twinsoft.convertigo.eclipse.swt.SwtUtils;
import com.twinsoft.convertigo.eclipse.views.mobile.MobilePickerContentProvider.TVObject;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.model.MobileComponentTreeObject;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.model.MobilePageComponentTreeObject;
import com.twinsoft.convertigo.eclipse.views.projectexplorer.model.MobileUIComponentTreeObject;
import com.twinsoft.convertigo.engine.ConvertigoError;
import com.twinsoft.convertigo.engine.Engine;
import com.twinsoft.convertigo.engine.enums.CouchParam;
import com.twinsoft.convertigo.engine.enums.JsonOutput.JsonRoot;
import com.twinsoft.convertigo.engine.enums.SchemaMeta;
import com.twinsoft.convertigo.engine.util.EngineListenerHelper;
import com.twinsoft.convertigo.engine.util.GenericUtils;
import com.twinsoft.convertigo.engine.util.SchemaUtils;
import com.twinsoft.convertigo.engine.util.XMLUtils;
import com.twinsoft.convertigo.engine.util.XmlSchemaUtils;

public class MobilePickerComposite extends Composite {

	Composite content, headerComposite;
	private ToolItem btnAction, btnSequence, btnDatabase, btnIteration, btnForm, btnGlobal;
	private CheckboxTreeViewer checkboxTreeViewer;
	private TreeViewer modelTreeViewer;
	private Button b_custom;
	private Control l_source;
	private Text t_custom, t_prefix, t_data, t_suffix;
	private Label message;
	private String currentSource = null;
	private MobileComponent currentMC = null;
	private Object lastSelected;
	private List<TVObject> checkedList = new ArrayList<TVObject>();
	private boolean isParentDialog = false;
	private boolean isUpdating = false;
	
	private EngineListenerHelper engineListener = new EngineListenerHelper() {

		@Override
		public void documentGenerated(Document document) {
			final Element documentElement = document.getDocumentElement();
			if (documentElement != null) {
				String project = documentElement.getAttribute("project");
				String connector = documentElement.getAttribute("connector");
				String transaction = documentElement.getAttribute("transaction");
				if (CouchDbConnector.internalView.equals(transaction)) {
					if (lastSelected !=null && lastSelected instanceof TVObject) {
						TVObject tvObject = (TVObject)lastSelected;
						Object object = tvObject.getObject();
						if (object != null && object instanceof DatabaseObject) {
							
							Map<String, Object> data = lookupModelData(tvObject);
							DatabaseObject dbo = (DatabaseObject) data.get("databaseObject");
							//Map<String, String> params = GenericUtils.cast(data.get("params"));
							String dataPath = (String) data.get("searchPath");
							
							if (dbo instanceof DesignDocument) {
								DesignDocument dd = (DesignDocument)dbo;
								CouchDbConnector cc = dd.getConnector();
								if (cc.getName().equals(connector) && cc.getProject().getName().equals(project)) {
									GetViewTransaction gvt = (GetViewTransaction) cc.getTransactionByName(CouchDbConnector.internalView);
									if (gvt != null) {
										try {
											String responseEltName = gvt.getXsdTypePrefix() + gvt.getName() + "Response";
											String xsdTypes = gvt.generateXsdTypes(document, true);
											String xsdDom = gvt.generateXsd(xsdTypes);
											
											XmlSchemaCollection collection = new XmlSchemaCollection();
											XmlSchema xmlSchema = SchemaUtils.loadSchema(xsdDom, collection);
											SchemaMeta.setCollection(xmlSchema, collection);
											ConvertigoError.updateXmlSchemaObjects(xmlSchema);
											
											QName responseTypeQName = new QName(xmlSchema.getTargetNamespace(), gvt.getXsdResponseTypeName());
											XmlSchemaComplexType cType = (XmlSchemaComplexType) xmlSchema.getSchemaTypes().getItem(responseTypeQName);
											Transaction.addSchemaResponseObjects(xmlSchema, cType);
											
											QName responseQName = new QName(xmlSchema.getTargetNamespace(), gvt.getXsdResponseElementName());
											XmlSchemaElement xse = xmlSchema.getElementByName(responseQName);
											SchemaMeta.setSchema(xse, xmlSchema);
											
											Document doc = XmlSchemaUtils.getDomInstance(xse);
											//System.out.println(XMLUtils.prettyPrintDOM(doc));
											
											String jsonString = XMLUtils.XmlToJson(doc.getDocumentElement(), true, true);
											JSONObject jsonObject = new JSONObject(jsonString);
											//System.out.println(jsonString);
											
											String searchPath = "document."+ responseEltName +".response.couchdb_output";
											searchPath += dataPath;
											JSONObject jsonOutput = findJSONObject(jsonObject, searchPath);
	
											Display.getDefault().asyncExec(new Runnable() {
												public void run() {
													if (modelTreeViewer != null && !modelTreeViewer.getTree().isDisposed()) {
														modelTreeViewer.setInput(jsonOutput);
														initTreeSelection(modelTreeViewer, null);
														updateMessage();
													}
												}
											});
											
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				}
			}
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					setWidgetsEnabled(true);
				}
			});
		}
	};
	
	public MobilePickerComposite(Composite parent, boolean isParentDialog) {
		super(parent, SWT.NONE);
		this.isParentDialog = isParentDialog;
		makeUI(this);
		updateMessage();
		ConvertigoPlugin.runAtStartup(new Runnable() {
			@Override
			public void run() {
				Engine.theApp.addEngineListener(engineListener);
			}
		});
	}
	
	@Override
	public void dispose() {
		try {
			Engine.theApp.removeEngineListener(engineListener);
		}
		catch (Exception e) {};
		super.dispose();
	}

	private void makeUI(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		parent.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				final MobilePickerContentProvider contentProvider = (MobilePickerContentProvider) checkboxTreeViewer.getContentProvider();
				if (contentProvider != null) {
					btnSequence.setSelection(false);
					btnDatabase.setSelection(false);
					btnAction.setSelection(false);
					btnIteration.setSelection(false);
					btnForm.setSelection(false);
					btnGlobal.setSelection(false);
					
					ToolItem button = (ToolItem) e.widget;
					button.setSelection(true);
					
					if (btnSequence.getSelection()) {
						contentProvider.setFilterBy(Filter.Sequence);
					} else if (btnDatabase.getSelection()) {
						contentProvider.setFilterBy(Filter.Database);
					} else if (btnAction.getSelection()) {
						contentProvider.setFilterBy(Filter.Action);
					} else if (btnIteration.getSelection()) {
						contentProvider.setFilterBy(Filter.Iteration);
					} else if (btnForm.getSelection()) {
						contentProvider.setFilterBy(Filter.Form);
					} else if (btnGlobal.getSelection()) {
						contentProvider.setFilterBy(Filter.Global);
					}
					modelTreeViewer.setInput(null);
					checkboxTreeViewer.getTree().removeAll();
					checkboxTreeViewer.refresh();
					initTreeSelection(checkboxTreeViewer, null);
				}
			}
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		};
		
		SelectionListener c_listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean isCustom = b_custom.getSelection();
				
				boolean doSetCustomText = false;
				if (isParentDialog) {
					MobileSmartSource cs = MobileSmartSource.valueOf(currentSource);
					if (cs != null && cs.getModel() != null && cs.getModel().getCustom().isEmpty()) {
						doSetCustomText = true;
					}
				} else {
					doSetCustomText = true;
				}
				
				if (doSetCustomText) {
					MobileSmartSource nmss = MobileSmartSource.valueOf(getSmartSourceString());
					if (nmss != null) {
						if (isCustom && !t_custom.getEnabled()) {
							t_custom.setText(nmss.getModel().computeValue());
						}
						if (!isCustom && t_custom.getEnabled()) {
							t_custom.setText("");
						}
					}
				}
				t_custom.setEnabled(isCustom);
				t_prefix.setEnabled(!isCustom);
				t_data.setEnabled(!isCustom);
				t_suffix.setEnabled(!isCustom);
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		};
		
		headerComposite = new Composite(parent, SWT.NONE);
		headerComposite.setLayout(SwtUtils.newGridLayout(2, false, 0, 0, 0, 0));
		headerComposite.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 1, 1));
		
		ToolBar toolbar = new ToolBar(headerComposite, SWT.NONE);
		
		int btnStyle = SWT.CHECK;
		Image image = null;
				
		btnSequence = new ToolItem(toolbar, btnStyle);
		try {
			image = ConvertigoPlugin.getDefault().getIconFromPath("/com/twinsoft/convertigo/beans/core/images/sequence_color_16x16.png", BeanInfo.ICON_COLOR_16x16);
		} catch (Exception e) {
			btnSequence.setText("SQ");
		}
		btnSequence.setImage(image);
		btnSequence.setToolTipText("Show Sequence Sources");
		btnSequence.addSelectionListener(listener);
		btnSequence.setSelection(true);
		
		btnDatabase = new ToolItem(toolbar, btnStyle);
		try {
			image = ConvertigoPlugin.getDefault().getIconFromPath("/com/twinsoft/convertigo/beans/connectors/images/fullsyncconnector_color_16x16.png", BeanInfo.ICON_COLOR_16x16);
		} catch (Exception e) {
			btnDatabase.setText("FS");
		}
		btnDatabase.setImage(image);
		btnDatabase.setToolTipText("Show FullSync Databases Sources");
		btnDatabase.addSelectionListener(listener);
		
		btnAction = new ToolItem(toolbar, btnStyle);
		try {
			image = ConvertigoPlugin.getDefault().getIconFromPath("/com/twinsoft/convertigo/beans/mobile/components/images/uicustomaction_color_16x16.png", BeanInfo.ICON_COLOR_16x16);
		} catch (Exception e) {
			btnAction.setText("AC");
		}
		btnAction.setImage(image);
		btnAction.setToolTipText("Show Action Sources");
		btnAction.addSelectionListener(listener);
		
		btnIteration = new ToolItem(toolbar, btnStyle);
		try {
			image = ConvertigoPlugin.getDefault().getIconFromPath("/com/twinsoft/convertigo/beans/steps/images/iterator_16x16.png", BeanInfo.ICON_COLOR_16x16);
		} catch (Exception e) {
			btnIteration.setText("IT");
		}
		btnIteration.setImage(image);
		btnIteration.setToolTipText("Show Iterators on current page Sources");
		btnIteration.addSelectionListener(listener);
		
		btnForm = new ToolItem(toolbar, btnStyle);
		try {
			image = ConvertigoPlugin.getDefault().getIconFromPath("/com/twinsoft/convertigo/beans/mobile/components/images/uiform_color_16x16.png", BeanInfo.ICON_COLOR_16x16);
		} catch (Exception e) {
			btnForm.setText("FM");
		}
		btnForm.setImage(image);
		btnForm.setToolTipText("Show Forms on current page Sources");
		btnForm.addSelectionListener(listener);
		
		btnGlobal = new ToolItem(toolbar, btnStyle);
		try {
			image = ConvertigoPlugin.getDefault().getIconFromPath("/com/twinsoft/convertigo/beans/mobile/components/dynamic/images/setglobalaction_color_16x16.png", BeanInfo.ICON_COLOR_16x16);
		} catch (Exception e) {
			btnGlobal.setText("GS");
		}
		btnGlobal.setImage(image);
		btnGlobal.setToolTipText("Show Global Shared objects");
		btnGlobal.addSelectionListener(listener);

		message = new Label(headerComposite, SWT.NONE);
		message.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		SashForm treesSashForm = new SashForm(parent, SWT.NONE);
		treesSashForm.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		checkboxTreeViewer = new CheckboxTreeViewer(treesSashForm, SWT.BORDER | SWT.SINGLE);
		checkboxTreeViewer.setContentProvider(new MobilePickerContentProvider());
		checkboxTreeViewer.addCheckStateListener(new ICheckStateListener() {
			@Override
			public void checkStateChanged(CheckStateChangedEvent event) {
				Object element = event.getElement();
				if (element instanceof TVObject) {
					if (btnIteration.getSelection() || btnForm.getSelection() || btnGlobal.getSelection()) {
						checkboxTreeViewer.setChecked(element, !event.getChecked());
						return;
					}
					
					TVObject tvoChecked = (TVObject)element;
					if (event.getChecked())
						checkedList.add(tvoChecked);
					else
						checkedList.remove(tvoChecked);
					updateGrayChecked();
					updateTexts();
				}
			}
		});
		checkboxTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selected instanceof TVObject && !selected.equals(lastSelected)) {
					TVObject tvoSelected = (TVObject)selected;
					lastSelected = selected;
					
					checkedList.clear();
					checkedList.add(tvoSelected);
					modelTreeViewer.setInput(null);
					updateModel(tvoSelected);
					updateGrayChecked();
					updateTexts();
				}
			}
		});
	
		
		modelTreeViewer = new TreeViewer(treesSashForm, SWT.BORDER);
		modelTreeViewer.setContentProvider(new MobilePickerContentProvider());
		modelTreeViewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				Object selected = ((IStructuredSelection) event.getSelection()).getFirstElement();
				if (selected instanceof TVObject) {
					updateTexts();
				}
			}
			
		});
		
		treesSashForm.setWeights(new int[] {1, 1});

		Composite sourceComposite = new Composite(parent, SWT.NONE);
		sourceComposite.setLayout(new GridLayout(2, false));
		sourceComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		if (isParentDialog) {
			l_source = new Label(sourceComposite, SWT.NONE);
			((Label) l_source).setText(" SOURCE ");
		} else {
			l_source = new Button(sourceComposite, SWT.NONE);
			((Button) l_source).setText(" SOURCE ");
			l_source.setToolTipText("Drag me on a Mobile UI component in the project tree to bind this source to an UI component property");
		}
		
		Composite dataComposite = new Composite(sourceComposite, SWT.NONE);
		dataComposite.setLayout(new GridLayout(2, false));
		dataComposite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l_prefix = new Label(dataComposite, SWT.NONE);
		l_prefix.setText("Prefix");
		
		t_prefix = new Text(dataComposite, SWT.BORDER);
		t_prefix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l_data = new Label(dataComposite, SWT.NONE);
		l_data.setText("Data");
		
		t_data = new Text(dataComposite, SWT.BORDER | SWT.READ_ONLY);
		t_data.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Label l_suffix = new Label(dataComposite, SWT.NONE);
		l_suffix.setText("Suffix");
		
		t_suffix = new Text(dataComposite, SWT.BORDER);
		t_suffix.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		b_custom = new Button(dataComposite, SWT.CHECK);
		b_custom.setText("Custom");
		b_custom.addSelectionListener(c_listener);
		
		t_custom = new Text(dataComposite, SWT.BORDER);
		t_custom.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		t_custom.setEnabled(false);
		
		// Add DND support
		boolean dragEnabled = !isParentDialog;
		if (dragEnabled) {
		    Transfer[] dragTransfers = new Transfer[] { MobileSourceTransfer.getInstance() };
		    int operations = DND.DROP_COPY | DND.DROP_MOVE;
			
			DragSourceAdapter dragAdapter = new DragSourceAdapter() {
		    	@Override
		    	public void dragStart(DragSourceEvent event) {
					try {
						String jsonString = getSmartSourceString();
						if (jsonString != null && !jsonString.isEmpty()) {
							event.doit = true;
							MobileSourceTransfer.getInstance().setMobileSource(new MobileSource(jsonString));
						}
					} catch (Exception e) {
						ConvertigoPlugin.logException(e, "Cannot drag");
					}
		    	}
			};
			
			DragSource source = null;
	
			source = new DragSource(modelTreeViewer.getTree(), operations);
			source.setTransfer(dragTransfers);
			source.addDragListener(dragAdapter);
			
			source = new DragSource(l_source, operations);
			source.setTransfer(dragTransfers);
			source.addDragListener(dragAdapter);
		}
	}
	
	private Filter getFilter() {
		Filter filter = null;
		if (btnSequence.getSelection())
			filter = Filter.Sequence;
		else if (btnDatabase.getSelection())
			filter = Filter.Database;
		else if (btnAction.getSelection())
			filter = Filter.Action;
		else if (btnIteration.getSelection())
			filter = Filter.Iteration;
		else if (btnForm.getSelection())
			filter = Filter.Form;
		else if (btnGlobal.getSelection())
			filter = Filter.Global;
		return filter;
	}
	
	public String getSmartSourceString() {
		try {
			Filter filter = getFilter();
			String projectName = currentMC.getProject().getName();
			
			MobileSmartSource cmss = MobileSmartSource.valueOf(currentSource);
			String input = cmss == null ? "": cmss.getInput();
			
			String path = getModelPath();
			String searchPath = "root";
			int index = path.indexOf(searchPath);
			if (index != -1) {
				path = path.substring(index + searchPath.length());
			}

			SourceModel model = MobileSmartSource.emptyModel(filter);
			model.setCustom(t_custom.getText());
			model.setPrefix(t_prefix.getText());
			model.setSuffix(t_suffix.getText());
			model.setUseCustom(b_custom.getSelection());
			model.setSourceData(getModelData());
			model.setPath(path);
			
			JSONObject jsonModel = model.toJson();
			//System.out.println(jsonModel.toString(1));
			
			MobileSmartSource nmss = new MobileSmartSource(filter, projectName, input, jsonModel);
			//System.out.println(nmss.toJsonString(1));
			return nmss.toJsonString();
		}
		catch (Exception e) {
			return "";
		}
	}
	
	private void resetViewers() {
		checkboxTreeViewer.setInput(null);
		modelTreeViewer.setInput(null);
		currentSource = null;
		lastSelected = null;
		checkedList.clear();
		t_prefix.setText("");
		t_suffix.setText("");
		t_data.setText("");
		t_custom.setText("");
	}
	
	private void setWidgetsEnabled(boolean enabled) {
		try {
			btnSequence.setEnabled(enabled);
			btnDatabase.setEnabled(enabled);
			btnAction.setEnabled(enabled);
			btnIteration.setEnabled(enabled);
			btnForm.setEnabled(enabled);
			btnGlobal.setEnabled(enabled);
			checkboxTreeViewer.getTree().setEnabled(enabled);
		} catch (Exception e) {
			
		}
	}

	private void updateGrayChecked() {
		checkboxTreeViewer.setCheckedElements(new Object[]{});
		checkboxTreeViewer.setGrayedElements(new Object[]{});
		for (TVObject tvo: checkedList) {
			checkboxTreeViewer.setParentsGrayed(tvo, true);
		}
		for (Object ob: checkboxTreeViewer.getGrayedElements()) {
			checkboxTreeViewer.setChecked(ob, true);
			if (ob instanceof TVObject && !((TVObject)ob).getSource().isEmpty()) {
				if (btnIteration.getSelection() || btnForm.getSelection() || btnGlobal.getSelection()) {
					checkboxTreeViewer.setGrayed(ob, !ob.equals(lastSelected));
				} else {
					checkboxTreeViewer.setGrayed(ob, false);
				}
			}
		}
	}
	
	private void initTreeSelection(TreeViewer treeViewer, Object object) {
		if (treeViewer != null) {
			if (object == null) {
				TreeItem[] treeItems = treeViewer.getTree().getItems();
				if (treeItems.length > 0) {
					object = treeItems[0].getData();
				}
			}
			
			StructuredSelection structuredSelection = null;
			if (object != null) {
				structuredSelection = new StructuredSelection(object);
			}
			
			MobileSmartSource cs = MobileSmartSource.valueOf(currentSource);
			if (treeViewer.equals(checkboxTreeViewer)) {
				checkboxTreeViewer.expandAll();
				if (cs != null) {
					checkedList.clear();
					fillCheckedList(null, cs.getSources());
					if (checkedList.size() > 0) {
						structuredSelection = new StructuredSelection(checkedList.get(0));
					}
				}
				if (checkedList.isEmpty() && structuredSelection != null) {
					checkedList.add((TVObject) structuredSelection.getFirstElement());
				}
				checkboxTreeViewer.setSelection(structuredSelection);
				updateGrayChecked();
			} else if (treeViewer.equals(modelTreeViewer)) {
				modelTreeViewer.refresh();
				if (cs != null) {
					String modelPath = cs.getModelPath();
					if (!modelPath.isEmpty()) {
						modelTreeViewer.expandAll();
						TVObject tvo = findModelItem(null, modelPath.replaceAll("\\?\\.", "."));
						if (tvo != null) {
							modelTreeViewer.collapseAll();
							modelTreeViewer.expandToLevel(tvo, 0);
							modelTreeViewer.setSelection(new StructuredSelection(tvo));
						} else {
							modelTreeViewer.collapseAll();
							modelTreeViewer.expandToLevel(1);
						}
					}
				}
			}
			
			if (cs != null) {
				if (treeViewer.equals(checkboxTreeViewer)) {
					checkedList.clear();
					fillCheckedList(null, cs.getSources());
					updateGrayChecked();
					//updateText(cs.getInput());
					updateTexts(cs);
				} else {
					updateTexts();
				}
			} else {
				updateTexts();
			}
		}
	}
	
	private void updateMessage() {
		updateMessage(null);
	}
	
	private void updateMessage(String msg) {
		String msgTxt = "      ";
		if (currentMC == null) {
			msgTxt = msgTxt + "Please select any Application sub component";
		} else {
			if (currentMC instanceof PageComponent)
				msgTxt = msgTxt + "Page : "+ currentMC.getName() + (msg != null ? " -> "+msg:"");
			else if (currentMC instanceof UIDynamicMenu)
				msgTxt = msgTxt + "Menu : "+ currentMC.getName() + (msg != null ? " -> "+msg:"");
			else if (currentMC instanceof ApplicationComponent)
				msgTxt = msgTxt + "App : "+ currentMC.getName() + (msg != null ? " -> "+msg:"");
		}
		message.setText(msgTxt);
	}
	
//	private List<String> getSourceList() {
//		TVObject tvoSelected = null;
//		Object selected = checkboxTreeViewer.getStructuredSelection().getFirstElement();
//		if (selected != null && selected instanceof TVObject) {
//			tvoSelected = (TVObject)selected;
//		}
//		
//		List<String> sourceList =  new ArrayList<String>();
//		List<TVObject> tvoList = GenericUtils.cast(Arrays.asList(checkboxTreeViewer.getCheckedElements()));
//		for (TVObject tvo : tvoList) {
//			if (tvo.equals(tvoSelected)) {
//				sourceList.add(0, tvo.getSource());
//			}
//			else {
//				sourceList.add(tvo.getSource());
//			}
//		}
//		return sourceList;
//	}
	
	private List<SourceData> getModelData() {
		TVObject tvoSelected = null;
		Object selected = checkboxTreeViewer.getStructuredSelection().getFirstElement();
		if (selected != null && selected instanceof TVObject) {
			tvoSelected = (TVObject)selected;
		}
		
		List<SourceData> sourceList =  new ArrayList<SourceData>();
		List<TVObject> tvoList = GenericUtils.cast(Arrays.asList(checkboxTreeViewer.getCheckedElements()));
		for (TVObject tvo : tvoList) {
			SourceData sd = tvo.getSourceData();
			if (sd != null) {
				if (tvo.equals(tvoSelected)) {
					sourceList.add(0, sd);
				}
				else {
					sourceList.add(sd);
				}
			}
		}
		return sourceList;
	}
	
	private String getModelPath() {
		String path = "";
		ITreeSelection selection = modelTreeViewer.getStructuredSelection();
		if (selection != null && !selection.isEmpty()) {
			TVObject tvo = (TVObject)selection.getFirstElement();
			path = tvo.getPath();
		}
		return path;
	}
	
//	private void updateText() {
//		boolean isDirective = btnIteration.getSelection();
//		boolean isForm = btnForm.getSelection();
//		boolean isGlobal = btnGlobal.getSelection();
//		List<String> sourceData = getSourceList();
//		int size = sourceData.size();
//		
//		StringBuffer buf = new StringBuffer();
//		if ((isDirective || isForm || isGlobal) && size > 0) {
//			String data = sourceData.get(0);
//			if (!data.isEmpty()) {
//				buf.append(data);
//			}
//		}
//		else {
//			for (String data : sourceData) {
//				if (!data.isEmpty()) {
//					buf.append(buf.length() > 0 ? ", ":"").append(data);
//				}
//			}
//		}
//		
//		String path = getModelPath();
//		String searchPath = "root";
//		int index = path.indexOf(searchPath);
//		if (index != -1) {
//			path = path.substring(index + searchPath.length());
//		}
//		
//		String computedText = buf.length() > 0 ? (isDirective || isForm || isGlobal ? buf + path : "listen(["+ buf +"])" + path):"";
//		t_data.setText(computedText);
//	}

//	private void updateText(String s) {
//		t_custom.setText(s);
//	}
	
	private void updateTexts() {
		Filter filter = getFilter();
		
		String path = getModelPath();
		String searchPath = "root";
		int index = path.indexOf(searchPath);
		if (index != -1) {
			path = path.substring(index + searchPath.length());
		}
		
		SourceModel model = MobileSmartSource.emptyModel(filter);
		model.setSourceData(getModelData());
		model.setPath(path);
		t_data.setText(model.computeValue());
	}
	
	private void updateTexts(MobileSmartSource cs) {
		if (cs != null) {
			SourceModel sm = cs.getModel();
			if (sm != null) {
				t_prefix.setText(sm.getPrefix());
				t_data.setText(sm.getData());
				t_suffix.setText(sm.getSuffix());
				t_custom.setText(sm.getCustom());
				if (sm.getUseCustom()) {
					b_custom.setSelection(true);
					b_custom.notifyListeners(SWT.Selection, null);
				}
			} else {
				//t_custom.setText(cs.getInput());
				t_custom.setText(cs.getValue(false));
				b_custom.setSelection(false);
				b_custom.notifyListeners(SWT.Selection, null);
			}
		}
	}
	
	private Map<String, Object> lookupModelData(TVObject tvObject) {
		Map<String, Object> data = new HashMap<String, Object>();
		Map<String, String> params = new HashMap<String, String>();
		DatabaseObject dbo = null;
		String searchPath = "";
		
		Object object = tvObject.getObject();
		JSONObject infos = tvObject.getInfos();
		if (object != null) {
			try {
				if (object instanceof RequestableObject) {
					dbo = (RequestableObject)object;
					searchPath = "";
				} else if (object instanceof DesignDocument) {
					dbo = (DesignDocument)object;
					DesignDocument dd = (DesignDocument)dbo;
					params.put("ddoc", dd.getName());
					params.put("view", tvObject.getParent().getName());
					params.put("include_docs", infos.has("include_docs") ? infos.getString("include_docs"):"false");
					searchPath = tvObject.getName().startsWith("get") ? ".rows.value":"";
				} else if (object instanceof UIControlDirective) {
					dbo = (UIControlDirective)object;
					do {
						UIControlDirective directive = (UIControlDirective)dbo;					
						
						String dboName = "";
						if (directive.getPage() != null) {
							dboName = directive.getPage().getName();
						} else if (directive.getMenu() != null) {
							dboName = directive.getMenu().getName();
						}
						
						MobileSmartSourceType msst = directive.getSourceSmartType();
						MobileSmartSource mss = msst.getSmartSource();
						if (mss != null) {
						dbo = mss.getDatabaseObject(dboName);
						params.putAll(mss.getParameters());
						searchPath = mss.getModelPath().replaceAll("\\?\\.", ".") + searchPath;
						} else {
							dbo = null;
						}
					} while (dbo != null && dbo instanceof UIControlDirective);
				} else if (object instanceof UIForm) {
					dbo = (UIForm)object;
					searchPath = "";
				} else if (object instanceof ApplicationComponent) {
					dbo = (ApplicationComponent)object;
					params.put("json", infos.toString());
					searchPath = "";
				} else if (object instanceof UIActionStack) {
					dbo = (UIActionStack)object;
					searchPath = "";
				} else if (object instanceof IAction) {
					if (object instanceof UIDynamicAction) {
						dbo = (UIDynamicAction)object;
						searchPath = "";
					} else if (object instanceof UICustomAction) {
						dbo = (UICustomAction)object;
						searchPath = "";
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		data.put("databaseObject", dbo);
		data.put("params", params);
		data.put("searchPath", searchPath);
		return data;
	}
	
	private void updateModel(TVObject tvObject) {
		Object object = tvObject.getObject();
		if (object != null) {
			Thread t = new Thread(new Runnable() {
				public void run() {
					isUpdating = true;
					
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							setWidgetsEnabled(false);
							updateMessage("generating model...");
						}
					});
					
					try {
						Map<String, Object> data = lookupModelData(tvObject);
						DatabaseObject dbo = (DatabaseObject) data.get("databaseObject");
						Map<String, String> params = GenericUtils.cast(data.get("params"));
						String dataPath = (String) data.get("searchPath");
						
						if (dbo != null) {
							// case of requestable
							if (dbo instanceof RequestableObject) {
								RequestableObject ro = (RequestableObject)dbo;
								
								Project project = ro.getProject();
								String responseEltName = ro.getXsdTypePrefix() + ro.getName() + "Response";
								boolean isDocumentNode = JsonRoot.docNode.equals(project.getJsonRoot()) && dataPath.isEmpty();
								
								XmlSchema schema = Engine.theApp.schemaManager.getSchemaForProject(project.getName());
								XmlSchemaObject xso = SchemaMeta.getXmlSchemaObject(schema, ro);
								if (xso != null) {
									Document document = XmlSchemaUtils.getDomInstance(xso);
									//System.out.println(XMLUtils.prettyPrintDOM(document));
									
									String jsonString = XMLUtils.XmlToJson(document.getDocumentElement(), true, true);
									JSONObject jsonObject = new JSONObject(jsonString);
									
									String searchPath = "document."+ responseEltName +".response";
									searchPath += isDocumentNode || !dataPath.startsWith(".document")? dataPath : dataPath.replaceFirst("\\.document", "");
									
									JSONObject jsonOutput = findJSONObject(jsonObject,searchPath);
									
									JSONObject jsonResponse = isDocumentNode ? new JSONObject().put("document", jsonOutput) : jsonOutput;
									
									Display.getDefault().asyncExec(new Runnable() {
										public void run() {
											modelTreeViewer.setInput(jsonResponse);
											initTreeSelection(modelTreeViewer, null);
											setWidgetsEnabled(true);
											updateMessage();
										}
									});
								}
							}
							// case of design document
							else if (dbo instanceof DesignDocument) {
								DesignDocument dd = (DesignDocument)dbo;
								Connector connector = dd.getConnector();
								String ddoc = params.get("ddoc");
								String view = params.get("view");
								String viewName = ddoc + "/" + view;
								String includeDocs = params.get("include_docs");
								
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
	
										ConnectorEditor connectorEditor = ConvertigoPlugin.getDefault().getConnectorEditor(connector);
										if (connectorEditor == null) {
											try {
												connectorEditor = (ConnectorEditor) activePage.openEditor(new ConnectorEditorInput(connector),
																"com.twinsoft.convertigo.eclipse.editors.connector.ConnectorEditor");
											} catch (PartInitException e) {
												ConvertigoPlugin.logException(e,
														"Error while loading the connector editor '"
																+ connector.getName() + "'");
											}
										}
										
					    				if (connectorEditor != null) {
					    					// activate connector's editor
					    					activePage.activate(connectorEditor);
					    					
					    					// set transaction's parameters
					    					Transaction transaction = connector.getTransactionByName(CouchDbConnector.internalView);
					    					((GetViewTransaction)transaction).setViewname(viewName);
					   						((GetViewTransaction)transaction).setQ_include_docs(includeDocs);
					   										    					
					    					Variable view_reduce = ((GetViewTransaction)transaction).getVariable(CouchParam.prefix + "reduce");
					   						view_reduce.setValueOrNull(false);
					    					
					    					// execute view transaction
					    					connectorEditor.getDocument(CouchDbConnector.internalView, false);
					    				}
									}
								});
							}
							// case of UIForm
							else if (dbo instanceof UIForm) {
								//JSONObject jsonObject = new JSONObject("{\"controls\":{\"['area']\":{\"value\":\"\"}}}");
								JSONObject jsonObject = new JSONObject(((UIForm)dbo).computeJsonModel());
								
								String searchPath = dataPath;
								
								JSONObject jsonOutput = findJSONObject(jsonObject,searchPath);
								
								JSONObject jsonResponse = jsonOutput;
								
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										modelTreeViewer.setInput(jsonResponse);
										initTreeSelection(modelTreeViewer, null);
										setWidgetsEnabled(true);
										updateMessage();
									}
								});
							}
							// case of UIACtionStack
							else if (dbo instanceof UIActionStack) {
								JSONObject jsonObject = new JSONObject(((UIActionStack)dbo).computeJsonModel());
								
								String searchPath = dataPath;
								
								JSONObject jsonOutput = findJSONObject(jsonObject,searchPath);
								
								JSONObject jsonResponse = jsonOutput;
								
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										modelTreeViewer.setInput(jsonResponse);
										initTreeSelection(modelTreeViewer, null);
										setWidgetsEnabled(true);
										updateMessage();
									}
								});
							}
							// case of UIDynamicAction or UICustomAction
							else if (dbo instanceof IAction) {
								JSONObject jsonObject = new JSONObject();
								
								if (dbo instanceof UIDynamicAction) {
									jsonObject = new JSONObject(((UIDynamicAction)dbo).computeJsonModel());
								}
								if (dbo instanceof UICustomAction) {
									jsonObject = new JSONObject(((UICustomAction)dbo).computeJsonModel());
								}
								
								String searchPath = dataPath;
								
								JSONObject jsonOutput = findJSONObject(jsonObject,searchPath);
								
								JSONObject jsonResponse = jsonOutput;
								
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										modelTreeViewer.setInput(jsonResponse);
										initTreeSelection(modelTreeViewer, null);
										setWidgetsEnabled(true);
										updateMessage();
									}
								});
							}
							// case of ApplicationComponent
							else if (dbo instanceof ApplicationComponent) {
								String json = params.get("json");
								JSONObject jsonModel = new JSONObject(json);
								
								Display.getDefault().asyncExec(new Runnable() {
									public void run() {
										modelTreeViewer.setInput(jsonModel);
										initTreeSelection(modelTreeViewer, null);
										setWidgetsEnabled(true);
										updateMessage();
									}
								});
							}
							// should not happened
							else {
								throw new Exception("DatabaseObject "+ dbo.getClass().getName() +" not supported!");
							}
						} else {
							Display.getDefault().asyncExec(new Runnable() {
								public void run() {
									modelTreeViewer.setInput(new JSONObject());
									initTreeSelection(modelTreeViewer, null);
									setWidgetsEnabled(true);
									updateMessage();
								}
							});
						}
					} catch (Exception e) {
						e.printStackTrace();
						
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								setWidgetsEnabled(true);
								updateMessage();
							}
						});
					} finally {
						isUpdating = false;
					}
				}
			});
			t.start();
		} else {
			modelTreeViewer.setInput(null);
		}
	}
	
	private JSONObject findJSONObject(JSONObject jsonParent, String searchPath) {
		try {
			JSONObject jsonObject = jsonParent;
			String path = searchPath;
			path = path.replaceAll("\\['", ".");
			path = path.replaceAll("'\\]", ".");
			
			String[] keys = path.split("\\.");
			for (String key: keys) {
				if (key.startsWith("[") && key.endsWith("]"))
					continue;
				if (jsonObject.has(key)) {
					Object ob = jsonObject.get(key);
					if (ob instanceof JSONObject) {
						jsonObject = (JSONObject)ob;
					} else if (ob instanceof JSONArray) {
						JSONArray jsonArray = (JSONArray)ob;
						jsonObject = jsonArray.getJSONObject(0);
					} else {
						break;
					}
				} else {
					break;
				}
			}
			return jsonObject;
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void fillCheckedList(TreeItem parent, List<String> csSourceList) {
		if (csSourceList != null && !csSourceList.isEmpty()) {
			TreeItem[] items = null;
			
			if (parent == null) {
				items = checkboxTreeViewer.getTree().getItems();
			}
			else {
				items = parent.getItems();
				
				TVObject tvo = (TVObject) parent.getData();
				String tvoSource = tvo.getSource();
				if (csSourceList.contains(tvoSource)) {
					int index = csSourceList.indexOf(tvoSource);
					if (index == 0)
						checkedList.add(0,tvo);
					else
						checkedList.add(tvo);
				}
			}
			
			for (int i=0; i<items.length; i++) {
				fillCheckedList(items[i], csSourceList);
			}
		}
	}
	
	private TVObject findModelItem(TreeItem parent, String modelPath) {
		if (modelPath != null && !modelPath.isEmpty()) {
			TreeItem[] items = null;
			
			items = parent == null ? modelTreeViewer.getTree().getItems() : parent.getItems();
			for (int i=0; i<items.length; i++) {
				TreeItem treeItem = items[i];
				TVObject tvo = (TVObject) treeItem.getData();
				if (tvo != null) {
					String tvoPath = tvo.getPath().replaceAll("\\?\\.", ".");
					if (modelPath.startsWith(tvoPath.replaceFirst("root", ""))) {
						if (modelPath.equals(tvoPath.replaceFirst("root", ""))) {
							return tvo;
						}
						return findModelItem(items[i], modelPath);
					}
				}
			}
		}
		return null;
	}
	
	@Override
	public boolean setFocus() {
		return checkboxTreeViewer.getTree().setFocus();
	}
	
	public void setCurrentInput(Object selected, String source) {
		if (isUpdating) return;
		
		currentMC = null;
		setWidgetsEnabled(true);
		
		if (selected instanceof MobileComponentTreeObject) {
			UIComponent uic = null;
			if (selected instanceof MobilePageComponentTreeObject) {
				currentMC = ((MobilePageComponentTreeObject) selected).getObject();
			} else if (selected instanceof MobileUIComponentTreeObject) {
				uic = ((MobileUIComponentTreeObject) selected).getObject();
				currentMC = uic.getPage() != null ? uic.getPage() : (uic.getMenu() != null ?  uic.getMenu() : uic.getApplication());
			}
			
			if (currentMC == null) {
				resetViewers();
			} else {
				if (!currentMC.equals(checkboxTreeViewer.getInput())) {
					resetViewers();
					checkboxTreeViewer.setInput(currentMC);
					initTreeSelection(checkboxTreeViewer, null);
				}
				
				MobileSmartSource cs = MobileSmartSource.valueOf(source);
				if (cs != null) {
					MobilePickerContentProvider contentProvider = (MobilePickerContentProvider) checkboxTreeViewer.getContentProvider();
					if (isParentDialog) { // when dbo's property edition
						contentProvider.setSelectedDbo(uic);
					}
					
					ToolItem buttonToSelect = btnSequence;
					currentSource = source;
					Filter filter = cs.getFilter();
					if (Filter.Sequence.equals(filter)) {
						buttonToSelect = btnSequence;
					}
					if (Filter.Database.equals(filter)) {
						buttonToSelect = btnDatabase;
					}
					if (Filter.Action.equals(filter)) {
						buttonToSelect = btnAction;
					}
					if (Filter.Iteration.equals(filter)) {
						buttonToSelect = btnIteration;
					}
					if (Filter.Form.equals(filter)) {
						buttonToSelect = btnForm;
					}
					if (Filter.Global.equals(filter)) {
						buttonToSelect = btnGlobal;
					}
					buttonToSelect.notifyListeners(SWT.Selection, null);
				}
				
			}
			updateMessage();
		} else {
			resetViewers();
			updateMessage();
		}
	}
}
