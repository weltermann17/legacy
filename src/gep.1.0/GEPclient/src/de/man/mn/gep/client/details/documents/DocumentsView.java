package de.man.mn.gep.client.details.documents;

import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.smartgwt.SectionStackSectionView;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.types.Alignment;
import com.smartgwt.client.types.ListGridFieldType;
import com.smartgwt.client.types.SelectionStyle;
import com.smartgwt.client.widgets.grid.CellFormatter;
import com.smartgwt.client.widgets.grid.HoverCustomizer;
import com.smartgwt.client.widgets.grid.ListGrid;
import com.smartgwt.client.widgets.grid.ListGridField;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.HasCellClickHandlers;
import com.smartgwt.client.widgets.grid.events.HasRowOverHandlers;

import de.man.mn.gep.client.shared.event.DetailSelected;

public abstract class DocumentsView extends SectionStackSectionView implements DocumentsPresenter.View {

	public DocumentsView() {
		getSectionStackSection().setID(DocumentsView.id);
		getSectionStackSection().setTitle("Documents");
		getSectionStackSection().setExpanded(false);
		getSectionStackSection().setCanCollapse(true);
		getSectionStackSection().setResizeable(true);
		asCanvas().setSize("100%", DocumentsView.maxheight + "px");
		getSectionStackSection().addItem(asCanvas());
		listgrid = new FormatsGrid();
		asCanvas().addChild(listgrid);
	}

	@Override public HasRowOverHandlers rowover() {
		return listgrid;
	}

	@Override public HasCellClickHandlers extension() {
		return listgrid;
	}

	@EventReceiver void detailSelected(final DetailSelected event) {
		if (event.isSuccess()) {
			getSectionStackSection().setTitle("Documents: " + event.detail.getDisplayName());
			getSectionStack().expandSection(DocumentsView.id);
			detailsselected = true;
		} else {
			getSectionStackSection().setTitle("Documents");
			getSectionStack().collapseSection(DocumentsView.id);
			listgrid.setData(new Record[0]);
			detailsselected = false;
		}
	}

	void setDocuments(final Record[] documents, final boolean showemtpymessage) {
		if (detailsselected) {
			listgrid.setData(documents);
			if (showemtpymessage) {
				listgrid.setShowEmptyMessage(true);
				int h = 40 + 24 * documents.length;
				if (40 == h) {
					h = 60;
				}
				asCanvas().setHeight(Math.min(h, DocumentsView.maxheight));
			} else {
				listgrid.setShowEmptyMessage(false);
			}
		}
	}

	private class FormatsGrid extends ListGrid {

		FormatsGrid() {
			setSize("100%", "100%");
			setShowRollOverCanvas(false);
			setShowRollOver(true);
			setLeaveScrollbarGap(false);
			setAutoFetchData(false);
			setUseAllDataSourceFields(true);
			setShowAllRecords(true);
			setAlternateRecordStyles(true);
			setShowHeaderMenuButton(false);
			setShowHeaderContextMenu(false);
			setCanResizeFields(false);
			setCanReorderFields(false);
			setCanSort(false);
			setCanGroupBy(false);
			setSelectionType(SelectionStyle.NONE);
			setShowSelectedStyle(false);
			setCanHover(true);
			setShowHover(true);
			setHoverAlign(Alignment.LEFT);
			setHoverWrap(true);
			setHoverWidth(240);
			setPrompt("Press Ctrl-J to show the 'Downloads' window.");
			setCanSelectText(false);
			setShowEmptyMessage(true);
			setEmptyMessage("No documents");

			final ListGridField icon = new ListGridField("extension", " ");
			icon.setAlign(Alignment.CENTER);
			icon.setType(ListGridFieldType.IMAGE);
			icon.setImageURLPrefix("/content/icons/");
			icon.setImageURLSuffix(".png");
			extension = new ListGridField("extension", "Extension");
			extension.setType(ListGridFieldType.LINK);
			extension.setTarget("javascript");
			final ListGridField page = new ListGridField("page", "Page");
			final ListGridField filesizestring = new ListGridField("filesizestring", "Filesize");
			final ListGridField vaultlocation = new ListGridField("vaultlocation", "Locations");
			vaultlocation.setCellFormatter(new CellFormatter() {
				@Override public String format(final Object value, final ListGridRecord record, final int rowNum,
						final int colNum) {
					final String s = value.toString();
					return s.contains("%") ? s.substring(0, s.indexOf(",")) + " ..." : s;
				}
			});

			icon.setCanSort(false);
			page.setCanSort(false);
			vaultlocation.setCanSort(false);

			icon.setWidth(24);
			extension.setWidth(80);
			page.setWidth(36);
			filesizestring.setWidth(70);
			vaultlocation.setWidth("*");

			page.setAlign(Alignment.CENTER);
			filesizestring.setAlign(Alignment.RIGHT);
			vaultlocation.setAlign(Alignment.CENTER);

			icon.setHoverCustomizer(new HoverCustomizer() {
				@Override public String hoverHTML(final Object value, final ListGridRecord record, final int rowNum,
						final int colNum) {
					return record.getAttribute("formatinfo");
				}
			});
			filesizestring.setHoverCustomizer(new HoverCustomizer() {
				@Override public String hoverHTML(final Object value, final ListGridRecord record, final int rowNum,
						final int colNum) {
					return record.getAttributeAsBoolean("derived") ? "Compressed filesize can be 40 to 80% smaller."
							: "";
				}
			});
			extension.setHoverCustomizer(new HoverCustomizer() {
				@Override public String hoverHTML(final Object value, final ListGridRecord record, final int rowNum,
						final int colNum) {
					return DocumentsView.downloadprompt;
				}
			});
			vaultlocation.setHoverCustomizer(new HoverCustomizer() {
				@Override public String hoverHTML(final Object value, final ListGridRecord record, final int rowNum,
						final int colNum) {
					final String v = record.getAttribute("vaultlocation");
					return v.contains(", ") ? v : "";
				}
			});
			page.setShowHover(false);

			setFields(icon, extension, page, filesizestring, vaultlocation);
		}
	}

	private ListGridField extension;
	private final ListGrid listgrid;
	private boolean detailsselected;
	private static final String id = "formatssection";
	private static final String downloadprompt = "Save or open file.";
	private static final int maxheight = 40 + 30 * 4;

}
