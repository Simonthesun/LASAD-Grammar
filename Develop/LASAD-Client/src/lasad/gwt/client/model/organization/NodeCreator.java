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
		
		deselectAllBoxes(map);
	}
	
	private void startCreateNode(GraphMapInfo mapInfo, Vector<LinkedBox> words, Vector<GrammarNode> nodes) {
		if (checkAdjacent(words, nodes)) {
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
		}
	}
	
	private void runCreateNodeAlgorithm(GraphMapInfo mapInfo, Vector<LinkedBox> selectedWords, Vector<GrammarNode> selectedNodes) {
		Vector<LinkedBox> wordsInSelection = selectedWords;
		
/*		for (GrammarNode node : selectedNodes) {
			Vector<LinkedBox> wordsInNode = node.getWords();
			
			for (LinkedBox word : wordsInNode) {
				if (!wordsInSelection.contains(word)) {
					wordsInSelection.add(word);
				}
			}
		}
*/		
		GrammarNode node = new GrammarNode(map.getMyArgumentMapSpace(), selectedWords, selectedNodes);
		argModel.addNode(node);
		
		if (selectedNodes.isEmpty()) {
			createForm(mapInfo, selectedWords, true);
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
			
			createForm(mapInfo, nodeForms, false);
			createFunction(mapInfo, nodeFunctions);
		}
		
	}

	public void createForm(GraphMapInfo mapInfo, Vector<LinkedBox> selectedWords, boolean span) {
		Map<String, ElementInfo> boxes = mapInfo.getElementsByType("box");
		ElementInfo form = boxes.get("Conclusion");
		Map<String, ElementInfo> links = mapInfo.getElementsByType("relation");
		ElementInfo link = links.get("Support");
		
		if (span) {
			Vector<String> firstlast = new Vector<String>();
			firstlast.add(Integer.toString(selectedWords.get(0).getBoxID()));
			if (selectedWords.size() > 1) {
				firstlast.add(Integer.toString(selectedWords.get(selectedWords.size() - 1).getBoxID()));
			}
			
			communicator.sendActionPackage(actionBuilder.createBoxAndLinks(form, link, mapInfo.getMapID(), calculateX(selectedWords), calculateY(selectedWords, true), firstlast));
		} else {
			Vector<String> elements = new Vector<String>();
			
			for (LinkedBox box : selectedWords) {
				elements.add(Integer.toString(box.getBoxID()));
			}
			
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
		double xleft = avg - 100;
		
		return (int) xleft;
	}
	
	public int calculateY(Vector<LinkedBox> selectedWords, boolean form) {
		
		double wordY = selectedWords.firstElement().getYTop();
		double y;
		
		if (form) {
			y = wordY - (1.5 * selectedWords.firstElement().getHeight());
		} else {
			y = wordY + (1.5 * selectedWords.firstElement().getHeight());
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
	
	public void deselectAllBoxes(AbstractGraphMap map) {
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
}