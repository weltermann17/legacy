package de.man.mn.gep.client.workspace.millertree;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;
import com.ibm.de.ebs.plm.gwt.client.mvp.MVP;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.smartgwt.client.data.Criterion;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.fields.DataSourceIntegerField;
import com.smartgwt.client.data.fields.DataSourceTextField;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.OperatorId;
import com.smartgwt.client.types.Overflow;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.Canvas;
import com.smartgwt.client.widgets.events.HasMouseOutHandlers;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.CellClickHandler;
import com.smartgwt.client.widgets.grid.events.HasCellClickHandlers;
import com.smartgwt.client.widgets.grid.events.HasCellOutHandlers;
import com.smartgwt.client.widgets.grid.events.HasCellOverHandlers;
import com.smartgwt.client.widgets.layout.HStack;
import com.smartgwt.client.widgets.tab.events.HasTabSelectedHandlers;

import de.man.mn.gep.client.shared.event.OpenMillerTree;
import de.man.mn.gep.client.workspace.WorkspaceTabView;
import de.man.mn.gep.client.workspace.millertree.menu.MillerTreeMenuModel;
import de.man.mn.gep.client.workspace.millertree.menu.MillerTreeMenuPresenter;
import de.man.mn.gep.client.workspace.millertree.menu.MillerTreeMenuView;

public abstract class MillerTreeView extends WorkspaceTabView implements MillerTreePresenter.View {

	public MillerTreeView() {
		final Canvas canvas = asCanvas();
		canvas.setOverflow(com.smartgwt.client.types.Overflow.AUTO);
		final MillerTreeMenuModel model = GWT.create(MillerTreeMenuModel.class);
		final MillerTreeMenuView view = GWT.create(MillerTreeMenuView.class);
		final MillerTreeMenuPresenter presenter = GWT.create(MillerTreeMenuPresenter.class);
		MVP.create(model, view, presenter);
		model.setId(asTab().getID());
		asTab().setAttribute("toolbar", view.asCanvas());
	}

	@Override protected void onInit(final BusEvent<?> event) {
		final OpenMillerTree e = (OpenMillerTree) event;
		maxlevelchildren = e.maxLevelChildren();
		maxlevelparents = e.maxLevelParents();
		modifyDataSource(e.datasource);
		treecolumns = new LinkedHashMap<Integer, TreeColumn>();
		layout = new ColumnStack();
		for (int i = maxlevelparents; i <= maxlevelchildren; i++) {
			final TreeColumn newcolumn = new TreeColumn(i);
			layout.addMember(newcolumn);
			treecolumns.put(i, newcolumn);
		}
		Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {
			@Override public boolean execute() {
				if (i <= maxlevelchildren) {
					treecolumns.get(i).setDataSource(e.datasource);
					++i;
					return true;
				} else {
					setField(MillerTreeView.DESCRIPTION_DE);
					asCanvas().addChild(layout);
					layout.resize();
					scrollToCenter();
					return false;
				}
			}

			private int i = maxlevelparents;
		});
	}

	void scrollToCenter() {
		asCanvas().scrollTo(
				(treecolumns.get(0).getLeft() - (asCanvas().getWidth() - MillerTreeView.MILLERCOLUMNSWIDTH) / 2), 0);
	}

	@Override public HasCellClickHandlers[] cellClick() {
		return toArray(new HasCellClickHandlers[treecolumns.size()]);
	}

	@Override public HasCellOverHandlers[] cellOver() {
		return toArray(new HasCellOverHandlers[treecolumns.size()]);
	}

	@Override public HasCellOutHandlers[] cellOut() {
		return toArray(new HasCellOutHandlers[treecolumns.size()]);
	}

	@Override public HasMouseOutHandlers[] mouseOut() {
		return toArray(new HasMouseOutHandlers[treecolumns.size()]);
	}

	@EventReceiver protected void attributeChanged(final AttributeChanged event) {
		if (event.id == asTab().getID()) {
			setField(event.attributename.equals("Partnumber") ? MillerTreeView.NAME : event.attributename.toLowerCase()
					.replace(" ", "_"));
		}
	}

	@Override public HasTabSelectedHandlers exportableSelected() {
		return asTab();
	}

	@SuppressWarnings({ "unchecked" }) private <T> T[] toArray(final T[] array) {
		int i = 0;
		for (final TreeColumn c : treecolumns.values()) {
			array[i++] = (T) c;
		}
		return array;
	}

	private void modifyDataSource(final DataSource datasource) {
		final DataSourceTextField partnumber = new DataSourceTextField(MillerTreeView.NAME);
		final DataSourceTextField description_de = new DataSourceTextField(MillerTreeView.DESCRIPTION_DE);
		final DataSourceTextField description_en = new DataSourceTextField(MillerTreeView.DESCRIPTION_EN);
		final DataSourceTextField description_fr = new DataSourceTextField(MillerTreeView.DESCRIPTION_FR);
		final DataSourceTextField description_pl = new DataSourceTextField(MillerTreeView.DESCRIPTION_PL);
		final DataSourceTextField description_tr = new DataSourceTextField(MillerTreeView.DESCRIPTION_TR);
		final DataSourceIntegerField id = new DataSourceIntegerField(MillerTreeView.ID);
		final DataSourceIntegerField parentid = new DataSourceIntegerField(MillerTreeView.PARENT_ID);
		final DataSourceIntegerField level = new DataSourceIntegerField(MillerTreeView.LEVEL);
		id.setHidden(true);
		parentid.setHidden(true);
		level.setHidden(true);
		datasource.setFields(id, parentid, level, partnumber, description_de, description_en, description_fr,
				description_pl, description_tr);
	}

	private void setField(final String fieldname) {
		for (int i = maxlevelparents; i <= maxlevelchildren; i++) {
			final TreeColumn grid = treecolumns.get(i);
			for (final ListGridField field : grid.getFields()) {
				grid.hideField(field.getName());
			}
			grid.showField(fieldname);
		}
	}

	class TreeColumn extends ListGrid {

		TreeColumn(final int level) {
			this.level = level;
			setMinWidth(MillerTreeView.MILLERCOLUMNSWIDTH);
			setAlternateRecordStyles(true);
			setCanEdit(false);
			setCanFreezeFields(false);
			setCanGroupBy(false);
			setCanReorderFields(false);
			setCanResizeFields(false);
			setCanSort(false);
			setWrapCells(true);
			setShowRollOver(true);
			setSelectionType(SelectionStyle.SINGLE);
			setCanHover(true);
			setShowHover(false);
			setCanSelectText(true);
			setCanPickFields(false);
			setShowHeaderContextMenu(false);
			setShowHeaderMenuButton(false);
			setLoadingDataMessage("");
			setEmptyMessage("");
			setLeaveScrollbarGap(false);
			setAlign(Alignment.LEFT);
			setFixedRecordHeights(false);
			setOverflow(Overflow.CLIP_V);
			addCellClickHandler(new InnerCellClickHandler());
		}

		@Override public void setDataSource(final DataSource datasource) {
			super.setDataSource(datasource);
			final Criterion levelcriterion = new Criterion(MillerTreeView.LEVEL, OperatorId.EQUALS, level);
			if (level != 0) {
				final Criterion parentcriterion = new Criterion(MillerTreeView.PARENT_ID, OperatorId.EQUALS, 0);
				levelcriterion.addCriteria(parentcriterion);
			}
			filterData(levelcriterion);
			final ArrayList<ListGridField> newfields = new ArrayList<ListGridField>();
			for (final ListField field : ListField.values()) {
				final ListGridField newfield = new ListGridField(field.toString());
				newfield.setTitle(Integer.toString(level));
				newfield.setAlign(Alignment.LEFT);
				hideField(field.toString());
				newfields.add(newfield);
			}
			setFields(newfields.toArray(new ListGridField[0]));
		}

		void showMembers(final int parentid) {
			final Criterion levelcriterion = new Criterion(MillerTreeView.LEVEL, OperatorId.EQUALS, level);
			final Criterion parentcriterion = new Criterion(MillerTreeView.PARENT_ID, OperatorId.EQUALS, parentid);
			levelcriterion.addCriteria(parentcriterion);
			filterData(levelcriterion);
		}

		void hideMembers() {
			final Criterion parentcriterion = new Criterion(MillerTreeView.PARENT_ID, OperatorId.EQUALS, 0);
			filterData(parentcriterion);
		}

		void resetSelectedFlag() {
			for (final ListGridRecord cell : getRecords()) {
				cell.setAttribute("selected", false);
			}
		}

		private int level = 0;
	}

	private class ColumnStack extends HStack {

		ColumnStack() {
			setWidth100();
			setHeight100();
		}

		void resize() {
			final int count = getMembers().length;
			if (count * MillerTreeView.MILLERCOLUMNSWIDTH < getWidth()) {
				final int newwidth = getWidth() / count;
				for (int i = maxlevelparents; i <= maxlevelchildren; i++) {
					final TreeColumn grid = treecolumns.get(i);
					grid.setWidth(newwidth);
				}
				treecolumns.get(0).setWidth(getWidth() - newwidth * (count - 1));
			}
		}
	}

	private class InnerCellClickHandler implements CellClickHandler {

		@Override public void onCellClick(final CellClickEvent event) {
			final int level = event.getRecord().getAttributeAsInt(MillerTreeView.LEVEL);
			final int id = event.getRecord().getAttributeAsInt(MillerTreeView.ID);

			if (level == 0) {
				redrawParents(level, id);
				redrawChildren(level, id);
				asCanvas()
						.scrollTo(
								(treecolumns.get(0).getLeft() - (asCanvas().getWidth() - MillerTreeView.MILLERCOLUMNSWIDTH) / 2),
								0);
			} else {
				if (level > 0 && level < maxlevelchildren) {
					redrawChildren(level, id);
					redrawParents(-1, id);

					asCanvas().scrollTo(treecolumns.get(level + 1).getRight() - asCanvas().getWidth(), 0);
				} else if (level < 0 && level > maxlevelparents) {
					redrawParents(level, id);
					redrawChildren(1, id);

					asCanvas().scrollTo(treecolumns.get(level - 1).getLeft(), 0);
				} else if (level > 0 && level == maxlevelchildren) {
					treecolumns.get(level).resetSelectedFlag();
				}
			}
			event.getRecord().setAttribute("selected", true);
		}

		void redrawChildren(final int level, final int id) {
			if (level <= maxlevelchildren) {
				Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {

					@Override public boolean execute() {
						if (i <= maxlevelchildren) {
							treecolumns.get(i).hideMembers();
							++i;
							return true;
						} else {
							treecolumns.get(level + 1).showMembers(id);
							return false;
						}
					}

					private int i = level + 1;
				});
				if (level != 0) {
					treecolumns.get(level).resetSelectedFlag();
				}
			}
		}

		void redrawParents(final int level, final int id) {
			if (level > maxlevelparents) {
				Scheduler.get().scheduleIncremental(new Scheduler.RepeatingCommand() {

					@Override public boolean execute() {
						if (i >= maxlevelparents) {
							treecolumns.get(i).hideMembers();
							--i;
							return true;
						} else {
							treecolumns.get(level - 1).showMembers(id);
							return false;
						}
					}

					private int i = level - 1;
				});
			}
			if (level != 0) {
				treecolumns.get(level).resetSelectedFlag();
			}
		}
	}

	static final String NAME = "name";
	static final String DESCRIPTION_DE = "description_de";
	static final String DESCRIPTION_EN = "description_en";
	static final String DESCRIPTION_FR = "description_fr";
	static final String DESCRIPTION_PL = "description_pl";
	static final String DESCRIPTION_TR = "description_tr";
	static final String ID = "id";
	static final String URL = "url";

	private ColumnStack layout;
	private int maxlevelchildren;
	private int maxlevelparents;
	private Map<Integer, TreeColumn> treecolumns;
	private static final String PARENT_ID = "parentid";
	private static final String LEVEL = "level";
	private static final int MILLERCOLUMNSWIDTH = 160;

	private static enum ListField {
		name, description_de, description_en, description_fr, description_pl, description_tr
	}

}
