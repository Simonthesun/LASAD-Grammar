package lasad.gwt.client.model.organization;

import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;

import lasad.gwt.client.LASAD_Client;
import lasad.gwt.client.communication.LASADActionSender;
import lasad.gwt.client.communication.helper.ActionFactory;
import lasad.gwt.client.logger.Logger;
import lasad.gwt.client.model.ElementInfo;
import lasad.gwt.client.model.GraphMapInfo;
import lasad.gwt.client.model.argument.MVController;
import lasad.gwt.client.model.organization.GrammarNode;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.ui.common.AbstractExtendedElement;
import lasad.gwt.client.ui.common.elements.AbstractExtendedTextElement;
import lasad.gwt.client.ui.link.AbstractLinkPanel;
import lasad.gwt.client.ui.workspace.LASADInfo;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.shared.communication.objects.ActionPackage;

public class NodeCreator {

	// The map that this instance of AutoOrganizer corresponds to
	private AbstractGraphMap map;
	private GraphMapInfo mapInfo;

	// For sending map updates to the server
	private LASADActionSender communicator = LASADActionSender.getInstance();
	private ActionFactory actionBuilder = ActionFactory.getInstance();
	private MVController controller;

	// The organization model that we will update (the actual model the server updates is contained within the controller)
	private ArgumentModel argModel;

	public NodeCreator(AbstractGraphMap map) {
		this.map = map;
		this.mapInfo = map.getMyArgumentMapSpace().getMenuBar().getMyMapInfo();
		this.controller = LASAD_Client.getMVCController(map.getID());
		this.argModel = map.getArgModel();
	}

	public void createNode() {
		if (checkAdjacent(getSelectedBoxes(map))) {
			createFormFunction(mapInfo, getSelectedBoxes(map));
		}
	}

	public void createFormFunction(GraphMapInfo mapInfo, Vector<LinkedBox> selectedWords) {
		Map<String, ElementInfo> boxes = mapInfo.getElementsByType("box");
		ElementInfo form = boxes.get("Conclusion");
		ElementInfo function = boxes.get("Refutation");
		
		communicator.sendActionPackage(actionBuilder.createBoxWithElements(form, mapInfo.getMapID(), calculateX(selectedWords), calculateY(selectedWords, true)));
		communicator.sendActionPackage(actionBuilder.createBoxWithElements(function, mapInfo.getMapID(), calculateX(selectedWords), calculateY(selectedWords, false)));
	}
	
	public int calculateX(Vector<LinkedBox> selectedWords) {
		double total = 0.0;
		
		for (LinkedBox box : selectedWords) {
			total = total + box.getXLeft();
		}
		
		double avg = total / selectedWords.size();
		
		return (int) avg;
	}
	
	public int calculateY(Vector<LinkedBox> selectedWords, boolean form) {
		
		double wordY = selectedWords.firstElement().getYTop();
		double y;
		
		if (form) {
			y = wordY + (1.5 * selectedWords.firstElement().getHeight());
		} else {
			y = wordY - (1.5 * selectedWords.firstElement().getHeight());
		}
		
		return (int) y;
	}
	
	public Vector<LinkedBox> sortWords(Vector<LinkedBox> words) {
		Vector<LinkedBox> sorted = new Vector<LinkedBox>();
		Vector<LinkedBox> unsorted = words;

		for (int i = 0; i < words.size(); i++) {
			LinkedBox least = unsorted.firstElement();

			for (int j = 1; j < unsorted.size(); j++) {
				if (unsorted.get(j).getRootID() < least.getRootID()) {
					least = unsorted.get(j);
				}
			}

			sorted.add(least);
			unsorted.remove(least);
		}

		return sorted;
	}

	public boolean checkAdjacent(Vector<LinkedBox> selectedWords) { //, Set<GrammarNode> selectedNodes) { //not functional with nodes yet
		Vector<LinkedBox> words = new Vector<LinkedBox>();

		for (LinkedBox word : selectedWords) {
			words.add(word);
		}

		//add words from node here

		words = sortWords(words);

		for (int i = 0; i < (words.size() - 1); i++) {
			int j = i + 1;
			
			if (!(words.get(i).getRootID() == (words.get(j).getRootID() - 1))) {
				return false;
			}
		}
		
		return true;
	}
	
	public Vector<LinkedBox> getSelectedBoxes(AbstractGraphMap map) {
		Vector<AbstractBox> aboxes = new Vector<AbstractBox>();
		Vector<LinkedBox> selected = new Vector<LinkedBox>();
		Set<LinkedBox> lboxes = argModel.getBoxes();
		List<Component> mapComponents = map.getItems();
		
		for (Component mapComponent : mapComponents) {
			if (mapComponent instanceof AbstractBox) {
				aboxes.add((AbstractBox) mapComponent);
			} 
		}
		
		for (AbstractBox abox : aboxes) {
			if (abox.getSelected()) {
				for (LinkedBox lbox : lboxes) {
					if (lbox.getBoxID() == abox.getConnectedModel().getId()) {
						selected.add(lbox);
					}
				}
			}
		}
		
		return selected;
	}
}