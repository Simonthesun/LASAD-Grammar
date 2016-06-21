package lasad.gwt.client.ui.workspace.argumentmap;

import java.util.Map;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.model.organization.AutoOrganizer;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.model.ElementInfo;
import lasad.gwt.client.model.GraphMapInfo;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.model.organization.ArgumentGrid;
import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.argument.MVCViewSession;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMapSpace;
import lasad.gwt.client.ui.workspace.graphmap.elements.AbstractCreateBoxDialog;
import lasad.gwt.client.ui.workspace.graphmap.elements.AbstractCreateBoxDialogListener;
import lasad.gwt.client.ui.workspace.transcript.TranscriptLinkData;
import lasad.shared.communication.objects.Action;
import lasad.shared.communication.objects.ActionPackage;

import com.extjs.gxt.ui.client.Style.HorizontalAlignment;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.widget.Slider;
import com.extjs.gxt.ui.client.widget.Window;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.form.FormButtonBinding;
import com.extjs.gxt.ui.client.widget.form.FormPanel;
import com.extjs.gxt.ui.client.widget.form.SliderField;
import com.extjs.gxt.ui.client.widget.layout.FormData;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Element;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.TextBox;

/**
 *	Creates the new diagram menu that appears when selected from the LASAD menu, found in ArgumentMapMenuBar.
 *	The menu lets the user to input a string, which will then be parsed and created as boxes (one per word)
 *	@author Simon Sun
 *	@since 31 May 2016, Last Updated 7 June 2016
 */
public class CreateNewMapDialog extends Window
{
	private String mapID;
	private GraphMapInfo mapInfo;
	private AbstractGraphMap map;

	private FormData formData;

	public CreateNewMapDialog(GraphMapSpace space, GraphMapInfo mapInfo)
	{
		this.mapID = space.getMyMap().getID();
		this.mapInfo = mapInfo;
		this.map = space.getMyMap();
	}

	@Override
	protected void onRender(Element parent, int index)
	{
		super.onRender(parent, index);
		this.setAutoHeight(true);
		this.setWidth(500);
		this.setHeading("Create New Diagram");
		formData = new FormData("");
		createForm();
	}

	// Changes the font size and box size in live time
	private void createForm()
	{

		FormPanel thisForm = new FormPanel();
		thisForm.setFrame(true);
		thisForm.setHeaderVisible(false);
		thisForm.setAutoHeight(true);

		final TextBox tb = new TextBox();

		thisForm.addText("WARNING: Creating a new sentence will replace all existing data");
		thisForm.add(tb, formData);

		// Okay Button
		Button btnDone = new Button("Create Diagram");
		btnDone.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
			@Override
			public void componentSelected(ButtonEvent ce) {

				String sentence = tb.getValue();
				String[] words = sentence.split("[ .,?!]+");

				LASADActionSender communicator = LASADActionSender.getInstance();
				ActionFactory actionBuilder = ActionFactory.getInstance();
				ActionPackage ap = new ActionPackage();

				Map<String, ElementInfo> boxes = mapInfo.getElementsByType("box");
				ElementInfo info = boxes.get("Premise");

				ArgumentModel argModel = map.getArgModel();

				int wordCounter = 0;

				AutoOrganizer myOrganizer = map.getAutoOrganizer();

				for (String word : words) {
					communicator.sendActionPackage(actionBuilder.createBoxWithElements(info, mapID, 0, 0, word));
				}

				myOrganizer.organizeMap(); 
				CreateNewMapDialog.this.hide();
			}
		});
		thisForm.addButton(btnDone);

		// Cancel Button
		Button btnCancel = new Button("Cancel");
		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>() {
			@Override
			public void componentSelected(ButtonEvent ce)
			{
				CreateNewMapDialog.this.hide();
			}
		});
		thisForm.addButton(btnCancel);

		thisForm.setButtonAlign(HorizontalAlignment.CENTER);
		FormButtonBinding binding = new FormButtonBinding(thisForm);
		binding.addButton(btnDone);

		this.add(thisForm);
	}
}