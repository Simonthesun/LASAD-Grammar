package lasad.gwt.client.ui.workspace.argumentmap;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.model.organization.AutoOrganizer;

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
 *	Creates the preferences menu that appears when selected from the LASAD menu, found in ArgumentMapMenuBar.
 *	The preferences menu allows the user to select the font size for LASAD, as well as the default box width and size for autoOrganizer.
 *	@author Kevin Loughlin
 *	@since 31 July 2015, Last Updated 11 August 2015
 */
public class CreateNewMapDialog extends Window
{
	private String mapID;

	private FormData formData;

	public CreateNewMapDialog(String mapID)
	{
		this.mapID = mapID;
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

		TextBox sentence = new TextBox();
		sentence.setText("Enter sentence:");

		thisForm.add(sentence, formData);

		// Okay Button
		Button btnDone = new Button("Create Diagram");
		btnDone.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
			@Override
			public void componentSelected(ButtonEvent ce)
			{


				AutoOrganizer myOrganizer = LASAD_Client.getMapTab(mapID).getMyMapSpace().getMyMap().getAutoOrganizer();
				CreateNewMapDialog.this.hide();
				myOrganizer.organizeMap(); 
			}
		});
		thisForm.addButton(btnDone);

		// Cancel Button
		Button btnCancel = new Button("Cancel");
		btnCancel.addSelectionListener(new SelectionListener<ButtonEvent>()
		{
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