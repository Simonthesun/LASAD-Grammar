package lasad.gwt.client.model.organization;

import java.util.HashSet;
import java.util.Vector;

import lasad.gwt.client.model.organization.ArgumentGrid;
import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.ui.box.AbstractBox;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMapSpace;

public class GrammarNode {

	boolean selected;

	private GraphMapSpace space;
	private AbstractGraphMap map;
	
	private LinkedBox form;
	private LinkedBox function;
	private Vector<LinkedBox> words;
	private Vector<GrammarNode> subNodes; 

	public GrammarNode(GraphMapSpace space, Vector<LinkedBox> words, Vector<GrammarNode> subNodes, LinkedBox form, LinkedBox function) {
		this.space = space;
		this.map = space.getMyMap();

		//ArgumentModel argModel = map.getArgModel();

		this.form = form;
		this.function = function;
		this.words = words;
		this.subNodes = subNodes;

		this.selected = false;
	}
	
	public GrammarNode(GraphMapSpace space, Vector<LinkedBox> words, Vector<GrammarNode> subNodes) {
		this.space = space;
		this.map = space.getMyMap();

		//ArgumentModel argModel = map.getArgModel();

		this.form = null;
		this.function = null;
		this.words = words;
		this.subNodes = subNodes;

		this.selected = false;
	}

	public LinkedBox getForm() {
		return form;
	}

	public LinkedBox getFunction() {
		return function;
	}
	

	public Vector<LinkedBox> getWords() {
		return words;
	}
	
	public Vector<GrammarNode> getSubNodes() {
		return subNodes;
	}

	public boolean isRoot() {
		if (subNodes == null) {
			return true;
		} else {
			return false;
		}
	}

/*	public boolean isSingle() {
		if (words == null && subNodes.size() == 1) {
			return true;
		} else if ()
	}
*/
	public boolean getSelected() {
		return selected;
	}
	
	public void setForm(LinkedBox box) {
		this.form = box;
	}
	
	public void setFunction(LinkedBox box) {
		this.function = box;
	}

	public void setWords(Vector<LinkedBox> words) {
		this.words = words;
	}
	
	public void setSubNodes(Vector<GrammarNode> nodes) {
		this.subNodes = nodes;
	}

/*	public void setRoot(boolean root) {
		this.root = root;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}
*/
	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public Vector<Integer> getAllBoxIDs() {
		Vector<Integer> ids = new Vector<Integer>();
		
		for (LinkedBox word : words) {
			ids.add(word.getBoxID());
		}
		
		ids.add(form.getBoxID());
		ids.add(function.getBoxID());
		
		if (subNodes != null) {
			for (GrammarNode subNode : subNodes) {
				ids.addAll(subNode.getAllBoxIDs());
			}
		}
		
		return ids;
	}
	
	public boolean wordInNode(LinkedBox searchWord) {
		for (LinkedBox word : words) {
			if (word.getBoxID() == searchWord.getBoxID()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean boxIsFormFunction(LinkedBox box) {
		if (box.getBoxID() == form.getBoxID() || box.getBoxID() == function.getBoxID()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean abstractWordInNode(AbstractBox searchWord) {
		for (LinkedBox word : words) {
			if (word.getBoxID() == searchWord.getConnectedModel().getId()) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean abstractBoxIsFormFunction(AbstractBox box) {
		if (box.getConnectedModel().getId() == form.getBoxID() || box.getConnectedModel().getId() == function.getBoxID()) {
			return true;
		} else {
			return false;
		}
	}
	
	public boolean containsBox(LinkedBox box) {
		Vector<Integer> allIDs = getAllBoxIDs();
		
		for (int i : allIDs) {
			if (box.getBoxID() == i) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean containsAbstractBox(AbstractBox box) {
		Vector<Integer> allIDs = getAllBoxIDs();
		
		for (int i : allIDs) {
			if (box.getConnectedModel().getId() == i) {
				return true;
			}
		}
		
		return false;
	}
	
	public boolean nodeInNode(GrammarNode node) {
		for (GrammarNode compare : subNodes) {
			if (compare == node) {
				return true;
			}
		}
		
		return false;
	}
	
	public Vector<LinkedBox> getAllWords() {
		Vector<LinkedBox> allWords = getWords();
		
		for (GrammarNode subNode : getSubNodes()) {
			allWords.addAll(subNode.getAllWords());
		}
		
		return allWords;
	}

}