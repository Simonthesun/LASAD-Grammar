package lasad.gwt.client.model.organization;

import java.util.HashSet;
import java.util.Set;

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

	boolean root;
	boolean single;

	private GraphMapSpace space;
	private AbstractGraphMap map;
	
	private LinkedBox form;
	private LinkedBox function;
	private Set<LinkedBox> words;

	public GrammarNode(GraphMapSpace space, boolean root, boolean single, Set<LinkedBox> words, LinkedBox form, LinkedBox function) {
		this.space = space;
		this.map = space.getMyMap();

		this.root = root;
		this.single = single;

		//ArgumentModel argModel = map.getArgModel();

		this.form = form;
		this.function = function;
		this.words = words;

		this.selected = false;
	}

	public LinkedBox getForm() {
		return form;
	}

	public LinkedBox getFunction() {
		return function;
	}

	public Set<LinkedBox> getWords() {
		return words;
	}

	public boolean isRoot() {
		return root;
	}

	public boolean isSingle() {
		return single;
	}

	public boolean getSelected() {
		return selected;
	}

	public void setWords(Set<LinkedBox> words) {
		this.words = words;
	}

	public void setRoot(boolean root) {
		this.root = root;
	}

	public void setSingle(boolean single) {
		this.single = single;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}
	
	public Set<Integer> getBoxIDs() {
		HashSet<Integer> ids = new HashSet<Integer>();
		
		for (LinkedBox word : words) {
			ids.add(word.getBoxID());
		}
		
		ids.add(form.getBoxID());
		ids.add(function.getBoxID());
		
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
	
	public boolean boxInNode(LinkedBox box) {
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
	
	public boolean abstractBoxInNode(AbstractBox box) {
		if (box.getConnectedModel().getId() == form.getBoxID() || box.getConnectedModel().getId() == function.getBoxID()) {
			return true;
		} else {
			return false;
		}
	}

}