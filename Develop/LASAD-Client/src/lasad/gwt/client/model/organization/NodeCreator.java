package lasad.gwt.client.model.organization;

import java.util.Vector;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.extjs.gxt.ui.client.widget.Component;
import com.google.gwt.user.client.Timer;

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

		Vector<LinkedBox> words = getSelectedWords(map);
		Vector<GrammarNode> nodes = getSelectedNodes(map);
		
		startCreateNode(mapInfo, words, nodes);
		
		deselectAll(map);
	}
	
	private void startCreateNode(GraphMapInfo mapInfo, Vector<LinkedBox> words, Vector<GrammarNode> nodes) {
/*		if (checkAdjacent(words, nodes)) {
			boolean sameNode = true;
			GrammarNode division = null;
			
			for (GrammarNode node : map.getArgModel().getNodes()) {
				if (node.wordInNode(words.firstElement()) || node.nodeInNode(nodes.firstElement())) {
					for (LinkedBox word : words) {
						if (!node.wordInNode(word)) {
							sameNode = false;
							division = node;
						}
					}
					
					for (GrammarNode subNode : nodes) {
						if (!node.nodeInNode(subNode)) {
							sameNode = false;
							division = node;
						}
					}
				}
			}
			
			if (sameNode) {
				runCreateNodeAlgorithm(mapInfo, words, nodes);
			} else {
				runCreateNodeAlgorithm(mapInfo, getDividedWords(division, words), getDividedNodes(division, nodes));
				words.removeAll(getDividedWords(division, words));
				nodes.removeAll(getDividedNodes(division, nodes));
				startCreateNode(mapInfo, words, nodes);
			}
		} else {
			runCreateNodeAlgorithm(mapInfo, getDividedWords(nodes, words), getDividedNodes(nodes, words));
			words.removeAll(getDividedWords(nodes, words));
			nodes.removeAll(getDividedNodes(nodes, words));
			startCreateNode(mapInfo, words, nodes);
		}*/
		
		runCreateNodeAlgorithm(mapInfo, words, nodes);
	}
	
	private void runCreateNodeAlgorithm(GraphMapInfo mapInfo, Vector<LinkedBox> selectedWords, Vector<GrammarNode> selectedNodes) {
//		Vector<LinkedBox> wordsInSelection = selectedWords;
		
/*		for (GrammarNode node : selectedNodes) {
			Vector<LinkedBox> wordsInNode = node.getWords();
			
			for (LinkedBox word : wordsInNode) {
				if (!wordsInSelection.contains(word)) {
					wordsInSelection.add(word);
				}
			}
		}
*/		
		boolean inNode = false;
		GrammarNode node = new GrammarNode(map.getMyArgumentMapSpace(), selectedWords, selectedNodes);
		Map<String, ElementInfo> links = mapInfo.getElementsByType("relation");
		ElementInfo link = links.get("Instructor-Pointer");
		
		if (selectedNodes.isEmpty()) {
			GrammarNode superNode = null;
			Vector<GrammarNode> nodes = argModel.getNodes();
			
			for (GrammarNode test : nodes) {
				if (test.wordInNode(selectedWords.firstElement())) {
					inNode = true;
					superNode = test;
				}
			}
			
			if (inNode) {
				moveNodeBoxesForInsert(superNode);
				
				for (LinkedBox word : superNode.getWords()) {
					deleteLinkToBox(word);
				}
				
				for (LinkedBox word : selectedWords) {
					superNode.removeWord(word);
				}
				
				Vector<LinkedBox> unselected = superNode.getWords();
				unselected.removeAll(selectedWords);
				
				for (LinkedBox word : unselected) {
					communicator.sendActionPackage(actionBuilder.createLinkWithElements(link, mapInfo.getMapID(), Integer.toString(superNode.getForm().getBoxID()), Integer.toString(word.getBoxID())));
				}
				
				superNode.addSubNode(node);
				
				createForm(mapInfo, selectedWords, true, true, node);
			} else {
				createForm(mapInfo, selectedWords, true, false, node);
			}
			
			createFunction(mapInfo, selectedWords);
			
		} else {
			Vector<LinkedBox> nodeForms = new Vector<LinkedBox>();
			Vector<LinkedBox> nodeFunctions = new Vector<LinkedBox>();
			
			for (GrammarNode selectedNode : selectedNodes) {
				nodeForms.add(selectedNode.getForm());
				nodeFunctions.add(selectedNode.getFunction());
			}
			
			for (LinkedBox selectedWord : selectedWords) {
				nodeForms.add(selectedWord);
				nodeFunctions.add(selectedWord);
			}
			
			if (selectedNodes.firstElement().getSuperNode() != null) {
				GrammarNode superNode = selectedNodes.firstElement().getSuperNode();
				
				moveNodeBoxesForInsert(superNode);
				
				for (LinkedBox box : nodeForms) {
					deleteLinkToBox(box);
				}
				
				for (GrammarNode selectedNode : selectedNodes) {
					superNode.removeSubNode(selectedNode);
					deleteLinkToBox(selectedNode.getForm());
				}
				
				for (LinkedBox selectedWord : selectedWords) {
					superNode.removeWord(selectedWord);
					deleteLinkToBox(selectedWord);
				}
				
				superNode.addSubNode(node);
				
				createForm(mapInfo, nodeForms, false, true, node);
			} else {
				createForm(mapInfo, nodeForms, false, false, node);
			}
			
			createFunction(mapInfo, nodeFunctions);
		}
		
		argModel.addNode(node);
		
	}

	public void createForm(GraphMapInfo mapInfo, Vector<LinkedBox> selectedWords, boolean span, boolean insert, GrammarNode node) {
		Map<String, ElementInfo> boxes = mapInfo.getElementsByType("box");
		ElementInfo form = boxes.get("Conclusion");
		Map<String, ElementInfo> links = mapInfo.getElementsByType("relation");
		ElementInfo link = links.get("Instructor-Pointer");
		
		Vector<String> elements = new Vector<String>();
		
		if (span) {
			elements.add(Integer.toString(selectedWords.firstElement().getBoxID()));
			if (selectedWords.size() > 1) {
				elements.add(Integer.toString(selectedWords.lastElement().getBoxID()));
			}
			
		} else {
			
			for (LinkedBox box : selectedWords) {
				elements.add(Integer.toString(box.getBoxID()));
			}
			
		}
		
		if (insert) {
			communicator.sendActionPackage(actionBuilder.createFormInsert(form, link, mapInfo.getMapID(), calculateX(selectedWords), calculateY(selectedWords, true), elements, Integer.toString(node.getSuperNode().getForm().getBoxID())));
		} else {
			communicator.sendActionPackage(actionBuilder.createBoxAndLinks(form, link, mapInfo.getMapID(), calculateX(selectedWords), calculateY(selectedWords, true), elements));
		}
		
		//communicator.sendActionPackage(actionBuilder.createLinkWithElements(link, mapInfo.getMapID(), "SECOND-LAST-ID", Integer.toString(selectedWords.get(selectedWords.size() - 1).getBoxID())));
	}
	
	public void createFunction(GraphMapInfo mapInfo, Vector<LinkedBox> selectedWords) {
		Map<String, ElementInfo> boxes = mapInfo.getElementsByType("box");
		ElementInfo function = boxes.get("Refutation");
		
		communicator.sendActionPackage(actionBuilder.createBoxWithElements(function, mapInfo.getMapID(), calculateX(selectedWords), calculateY(selectedWords, false)));
		
	}
	
	public int calculateX(Vector<LinkedBox> selectedWords) {
		double avg = (selectedWords.firstElement().getXLeft() + selectedWords.get(selectedWords.size() - 1).getXLeft() + selectedWords.get(selectedWords.size() - 1).getWidth()) / 2;
		double xleft;
		
		if (selectedWords.size() == 1) {
			xleft = selectedWords.firstElement().getXLeft();
		} else {
			xleft = avg - 150;
		}
		
		return (int) xleft;
	}
	
	public int calculateY(Vector<LinkedBox> selectedWords, boolean form) {
		
		double wordY = selectedWords.firstElement().getYTop();
		double y;
		
		if (form) {
			for (LinkedBox box : selectedWords) {
				if (box.getYTop() < wordY) {
					wordY = box.getYTop();
				}
			}
			
			y = wordY - (1.0 * selectedWords.firstElement().getHeight());
		} else {
			for (LinkedBox box : selectedWords) {
				if (box.getYTop() > wordY) {
					wordY = box.getYTop();
				}
			}
			
			y = wordY + (1.0 * selectedWords.firstElement().getHeight());
		}
		
		return (int) y;
	}
	
	public Vector<LinkedBox> sortWords(Vector<LinkedBox> words) {
		Vector<LinkedBox> sorted = new Vector<LinkedBox>();
		Vector<LinkedBox> unsorted = words;

		for (int i = 0; i < words.size(); i++) {
			LinkedBox least = unsorted.firstElement();

			for (LinkedBox compare : unsorted) {
				if (compare.getRootID() < least.getRootID()) {
					least = compare;
				}
			}

			sorted.add(least);
			unsorted.remove(least);
		}

		return sorted;
	}

	public boolean checkAdjacent(Vector<LinkedBox> selectedWords, Vector<GrammarNode> selectedNodes) {
		Vector<LinkedBox> words = new Vector<LinkedBox>();

		for (LinkedBox word : selectedWords) {
			words.add(word);
		}
		
		for (GrammarNode node : selectedNodes) {
			words.addAll(node.getAllWords());
		}

		int min = words.get(0).getRootID();
		int max = words.get(0).getRootID();
		
		for (LinkedBox word : words) {
			if (word.getRootID() < min) {
				min = word.getRootID();
			} else if (word.getRootID() > max) {
				max = word.getRootID();
			}
		}
		
		if (max - min == words.size() - 1) {
			return true;
		} else {
			return false;
		}
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
	
	public Vector<GrammarNode> getSelectedNodes(AbstractGraphMap map) {
		Vector<GrammarNode> allNodes = map.getArgModel().getNodes();
		Vector<GrammarNode> selectedNodes = new Vector<GrammarNode>();
		
		for (GrammarNode node : allNodes) {
			if (node.getSelected()) {
				selectedNodes.add(node);
			}
		}
		
		return selectedNodes;
	}
	
	public Vector<LinkedBox> getSelectedWords(AbstractGraphMap map) {
		Vector<LinkedBox> allSelected = getSelectedBoxes(map);
		Vector<GrammarNode> selectedNodes = getSelectedNodes(map);
		Vector<LinkedBox> wordsInNodes = new Vector<LinkedBox>();
		
		for (GrammarNode node : selectedNodes) {
			Vector<LinkedBox> wordsInNode = node.getWords();
			
			for (LinkedBox word : wordsInNode) {
				if (!wordsInNodes.contains(word)) {
					wordsInNodes.add(word);
				}
			}
			
			if (allSelected.contains(node.getForm())) {
				allSelected.remove(node.getForm());
			}
			
			if (allSelected.contains(node.getFunction())) {
				allSelected.remove(node.getFunction());
			}
		}
		
		for (LinkedBox wordInNode : wordsInNodes) {
			if (allSelected.contains(wordInNode)) {
				allSelected.remove(wordInNode);
			}
		}
		
		return allSelected;
	}
	
	public void deselectAll(AbstractGraphMap map) {
		Vector<AbstractBox> aboxes = new Vector<AbstractBox>();
		List<Component> mapComponents = map.getItems();
		
		for (Component mapComponent : mapComponents) {
			if (mapComponent instanceof AbstractBox) {
				aboxes.add((AbstractBox) mapComponent);
			} 
		}
		
		for (AbstractBox abox : aboxes) {
			abox.deselect();
		}
		
		for (GrammarNode node : map.getArgModel().getNodes()) {
			node.setSelected(false);
		}
	}
	
	public Vector<LinkedBox> getDividedWords(GrammarNode node, Vector<LinkedBox> words) {
		Vector<LinkedBox> division = new Vector<LinkedBox>();
		
		for (LinkedBox word : words) {
			if (node.wordInNode(word)) {
				division.add(word);
			}
		}
		
		return division;
	}
	
	public Vector<GrammarNode> getDividedNodes(GrammarNode node, Vector<GrammarNode> nodes) {
		Vector<GrammarNode> division = new Vector<GrammarNode>();
		
		for (GrammarNode subNode : nodes) {
			if (node.nodeInNode(subNode)) {
				division.add(subNode);
			}
		}
		
		return division;
	}
	
	public Vector<GrammarNode> getDividedNodes(Vector<GrammarNode> nodes, Vector<LinkedBox> words) {
		Vector<GrammarNode> division = new Vector<GrammarNode>();
		Vector<LinkedBox> allWords = new Vector<LinkedBox>();

		for (LinkedBox word : words) {
			allWords.add(word);
		}
		
		for (GrammarNode node : nodes) {
			allWords.addAll(node.getAllWords());
		}

		int n = allWords.get(0).getRootID();
		
		for (GrammarNode node : nodes) {
			Vector<LinkedBox> wordsInNode = node.getAllWords();
			
			if (wordsInNode.contains(allWords.get(0))) {
				division.add(node);
			}
		} 
		
		for (int i = 1; i < allWords.size(); i++) {
			for (LinkedBox word : allWords) {
				if (word.getRootID() == n + 1) {
					
					for (GrammarNode node : nodes) {
						Vector<LinkedBox> wordsInNode = node.getAllWords();
						
						if (wordsInNode.contains(word)) {
							if (!division.contains(node)) {
								division.add(node);
							}
						}
					}
					
					n++;
				}
			}
		}
		
		n = allWords.get(0).getRootID();
		
		for (int i = 1; i < allWords.size(); i++) { //again, the other way
			for (LinkedBox word : allWords) {
				if (word.getRootID() == n - 1) {
					
					for (GrammarNode node : nodes) {
						Vector<LinkedBox> wordsInNode = node.getAllWords();
						
						if (wordsInNode.contains(word)) {
							if (!division.contains(node)) {
								division.add(node);
							}
						}
					
					n--;
					}
				}
			}
		}
		
		return division;
	}
	
	public Vector<LinkedBox> getDividedWords(Vector<GrammarNode> nodes, Vector<LinkedBox> words) {
		Vector<LinkedBox> division = new Vector<LinkedBox>();
		Vector<LinkedBox> allWords = new Vector<LinkedBox>();

		for (LinkedBox word : words) {
			allWords.add(word);
		}
		
		for (GrammarNode node : nodes) {
			allWords.addAll(node.getAllWords());
		}

		int n = allWords.get(0).getRootID();
		
		if (words.contains(allWords.get(0))) {
			division.add(allWords.get(0));
		}
		
		for (int i = 1; i < allWords.size(); i++) {
			for (LinkedBox word : allWords) {
				if (word.getRootID() == n + 1) {
					
					division.add(word);
					
					n++;
				}
			}
		}
		
		n = allWords.get(0).getRootID();
		
		for (int i = 1; i < allWords.size(); i++) {
			for (LinkedBox word : allWords) {
				if (word.getRootID() == n - 1) {
					
					division.add(word);
					
					n--;
				}
			}
		}
		
		return division;
	}
	
	public GrammarNode getTopLevelNode(GrammarNode subNode) {
		Vector<GrammarNode> allNodes = argModel.getNodes();
		GrammarNode top = subNode;
		
		for (GrammarNode node : allNodes) {
			if (node.nodeInNode(subNode)) {
				top = getTopLevelNode(node);
			}
		}
		
		return top;
	}
	
	public GrammarNode getTopLevelNode(LinkedBox word) {
		Vector<GrammarNode> allNodes = argModel.getNodes();
		GrammarNode top = null;
		
		for (GrammarNode node : allNodes) {
			if (node.wordInNode(word)) {
				top = getTopLevelNode(node);
			}
		}
		
		return top;
	}
	
	public void deleteLinkToBox(LinkedBox end) {
		if (argModel.getLinkData().containsKey(end)) {
			communicator.sendActionPackage(actionBuilder.removeElement(mapInfo.getMapID(), argModel.getLinkIDByEndBox(end)));
			argModel.removeLinkData(end);
		}
	}
	
	public void moveNodeBoxesForInsert(GrammarNode base) {
		if (base.getSuperNode() == null) {
			communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), base.getForm().getBoxID(), (int) base.getForm().getXLeft(), (int) (base.getForm().getYTop() - base.getForm().getHeight())));
			communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), base.getFunction().getBoxID(), (int) base.getFunction().getXLeft(), (int) (base.getFunction().getYTop() + base.getFunction().getHeight())));
		} else if (base.getSuperNode().getSubNodes().size() == 1) {
			moveNodeBoxesForInsert(base.getSuperNode());
		} /*else {
			for (GrammarNode subNode : base.getSuperNode().getSubNodes()) {
				if (subNode != base) {
					communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), subNode.getForm().getBoxID(), (int) subNode.getForm().getXLeft(), (int) (subNode.getForm().getYTop() - subNode.getForm().getHeight())));
					communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), subNode.getFunction().getBoxID(), (int) subNode.getFunction().getXLeft(), (int) (subNode.getFunction().getYTop() + subNode.getFunction().getHeight())));
				}
			}
			
			moveNodeBoxesForInsert(base.getSuperNode());
		}*/
	}
	
	public void moveNodeBoxesForDelete(GrammarNode base) {
		if (base.getSuperNode() == null) {
			communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), base.getForm().getBoxID(), (int) base.getForm().getXLeft(), (int) (base.getForm().getYTop() + base.getForm().getHeight())));
			communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), base.getFunction().getBoxID(), (int) base.getFunction().getXLeft(), (int) (base.getFunction().getYTop() - base.getFunction().getHeight())));
		} else if (base.getSuperNode().getSubNodes().size() == 1) {
			moveNodeBoxesForDelete(base.getSuperNode());
		} /*else {
			for (GrammarNode subNode : base.getSuperNode().getSubNodes()) {
				if (subNode != base) {
					communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), subNode.getForm().getBoxID(), (int) subNode.getForm().getXLeft(), (int) (subNode.getForm().getYTop() + subNode.getForm().getHeight())));
					communicator.sendActionPackage(actionBuilder.updateBoxPosition(mapInfo.getMapID(), subNode.getFunction().getBoxID(), (int) subNode.getFunction().getXLeft(), (int) (subNode.getFunction().getYTop() - subNode.getFunction().getHeight())));
				}
			}
			
			moveNodeBoxesForDelete(base.getSuperNode());
		}*/
	}
	
	public void deleteNodes() {

		Vector<GrammarNode> nodes = getSelectedNodes(map);
		
		for (GrammarNode node : nodes) {
			startDeleteNode(mapInfo, node);
		}
		
		deselectAll(map);
	}
	
	private void startDeleteNode(GraphMapInfo mapInfo, GrammarNode node) { 
		Vector<GrammarNode> subNodes = node.getSubNodes();
		Vector<LinkedBox> subWords = node.getWords();
		GrammarNode superNode = node.getSuperNode();
		Map<String, ElementInfo> links = mapInfo.getElementsByType("relation");
		ElementInfo link = links.get("Instructor-Pointer");
		//boolean hasSubNode = false;
		
		if (superNode == null) {
			/*for (GrammarNode subNode : subNodes) {
				deleteLinkToBox(subNode.getForm());
			}
			
			for (LinkedBox word : subWords) {
				deleteLinkToBox(word);
			}*/
			
			communicator.sendActionPackage(actionBuilder.removeElement(mapInfo.getMapID(), node.getForm().getBoxID()));
			communicator.sendActionPackage(actionBuilder.removeElement(mapInfo.getMapID(), node.getFunction().getBoxID()));
			
			argModel.removeNode(node);
		} else {
			for (GrammarNode subNode : subNodes) {
				//deleteLinkToBox(subNode.getForm());
				
				superNode.addSubNode(subNode);
				//hasSubNode = true;
				communicator.sendActionPackage(actionBuilder.createLinkWithElements(link, mapInfo.getMapID(), Integer.toString(superNode.getForm().getBoxID()), Integer.toString(subNode.getForm().getBoxID())));				
			}
			
			for (LinkedBox word : subWords) {
				//deleteLinkToBox(word);
				
				superNode.addWord(word);
				
				//if (hasSubNode) {
					communicator.sendActionPackage(actionBuilder.createLinkWithElements(link, mapInfo.getMapID(), Integer.toString(superNode.getForm().getBoxID()), Integer.toString(word.getBoxID())));
				//}
			}
			
			communicator.sendActionPackage(actionBuilder.removeElement(mapInfo.getMapID(), node.getForm().getBoxID()));
			communicator.sendActionPackage(actionBuilder.removeElement(mapInfo.getMapID(), node.getFunction().getBoxID()));
			
			//for re-drawing the links if it requires a "span" rather than a link to each element, but buggy
			/*if (!hasSubNode) {
				int least = superNode.getWords().firstElement().getRootID();
				int greatest = superNode.getWords().firstElement().getRootID();
				
				for (LinkedBox word : superNode.getWords()) {
					deleteLinkToBox(word);
					
					if (word.getRootID() < least) {
						least = word.getRootID();
					} else if (word.getRootID() > greatest) {
						greatest = word.getRootID();
					}
					
				}
				
				communicator.sendActionPackage(actionBuilder.createLinkWithElements(link, mapInfo.getMapID(), Integer.toString(superNode.getForm().getBoxID()), Integer.toString(least)));
				
				if (superNode.getWords().size() > 1) {
					communicator.sendActionPackage(actionBuilder.createLinkWithElements(link, mapInfo.getMapID(), Integer.toString(superNode.getForm().getBoxID()), Integer.toString(greatest)));
				}
			}*/
			
			superNode.removeSubNode(node);
			argModel.removeNode(node);
			
			if (superNode != null) {
				if (superNode.getSubNodes().size() > 0) {
					moveNodeBoxesForDelete(superNode);
				}
			}
		}
	}
}