package lasad.gwt.client.model.organization;

import java.util.HashSet;
import java.util.Set;

import lasad.gwt.client.model.organization.ArgumentGrid;
import lasad.gwt.client.model.organization.ArgumentModel;
import lasad.gwt.client.model.organization.ArgumentThread;
import lasad.gwt.client.model.organization.LinkedBox;
import lasad.gwt.client.ui.workspace.graphmap.AbstractGraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMap;
import lasad.gwt.client.ui.workspace.graphmap.GraphMapSpace;

public class GrammarNode {

	boolean selected;

	boolean root;
	boolean single;

	private GraphMapSpace space;
	private AbstractGraphMap map;
	
//	private LinkedBox form;
//	private LinkedBox function;
	private Set<LinkedBox> words;

	public GrammarNode(GraphMapSpace space, boolean root, boolean single, Set<LinkedBox> words) {
		this.space = space;
		this.map = space.getMyMap();

		this.root = root;
		this.single = single;

		//ArgumentModel argModel = map.getArgModel();

//		this.form = new LinkedBox();
//		this.function = new LinkedBox();
		this.words = words;

		this.selected = false;
	}

//	public LinkedBox getForm() {
//		return form;
//	}

//	public LinkedBox getFunction() {
//		return function;
//	}

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

}