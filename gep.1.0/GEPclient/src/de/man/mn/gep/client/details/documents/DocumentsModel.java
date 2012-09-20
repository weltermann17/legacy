package de.man.mn.gep.client.details.documents;

import java.util.Arrays;
import java.util.Comparator;

import org.restlet.client.data.MediaType;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.json.client.JSONArray;
import com.google.gwt.json.client.JSONObject;
import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.BaseModel;
import com.ibm.de.ebs.plm.gwt.client.mvp.Request;
import com.ibm.de.ebs.plm.gwt.client.mvp.Response;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventReceiver;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.EventSender;
import com.ibm.de.ebs.plm.gwt.client.mvp.annotations.RequestReceiver;
import com.ibm.de.ebs.plm.gwt.client.restlet.FileDownload;
import com.ibm.de.ebs.plm.gwt.client.restlet.JsonResource;
import com.ibm.de.ebs.plm.gwt.client.ui.gwt.Dialogs;
import com.smartgwt.client.data.Record;
import com.smartgwt.client.widgets.grid.ListGridRecord;
import com.smartgwt.client.widgets.grid.events.CellClickEvent;
import com.smartgwt.client.widgets.grid.events.RowOverEvent;

import de.man.mn.gep.client.shared.Context;
import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.event.DetailSelected;
import de.man.mn.gep.client.shared.event.DocumentsSummary;

public abstract class DocumentsModel extends BaseModel {

	@RequestReceiver("FormatSelected") void formatSelected(final Request<RowOverEvent> request) {
		currentrecord = request.gesture().getRecord();
	}

	@RequestReceiver("ExtensionClicked") void extensionClicked(final Request<CellClickEvent> request) {
		if (1 == request.gesture().getColNum()) {
			if (0 < currentrecord.getAttributeAsLong("filesize")) {
				FileDownload.get(currentrecord.getAttribute("url"), MediaType.ALL);
			} else {
				Dialogs.info("You cannot download a file with a size of 0 bytes.");
			}
		}
	}

	@EventReceiver @EventSender(DocumentsSummary.class) void detailSelected(final DetailSelected event,
			final Response<JSONValue, DocumentsSummary> response) {
		if (event.isSuccess()) {
			currentdetail = event.detail;
			response.event().detail = currentdetail;
			JsonResource.get(currentdetail.getLink("formatssummary"), response, true);
		} else {
			currentdetail = null;
			currentrecord = null;
			records = null;
			formats = null;
		}
	}

	private Record[] getDocumentsLoaded(final DocumentsSummary event) {
		if (event.isSuccess() && event.detail.equals(currentdetail)) {
			formats = event.getFormats();
			records = new Record[formats.size()];
			for (int i = 0; i < formats.size(); ++i) {
				final JSONObject format = formats.get(i).isObject();
				final String extension = format.get("extension").isString().stringValue();
				final boolean isnative = format.containsKey("nativeformat");
				final boolean isderived = format.containsKey("derivedformat");
				if (isderived && null != currentdetail) {
					final String page = "";
					final String location = Context.get().derivedLocation();
					String url = format.get("url").isString().stringValue();
					if (DataType.snapshots.equals(event.detail.getDataType())) {
						final String parenttype = url.contains("/versions/") ? "/versions/" : url
								.contains("/products/") ? "/products/" : url.contains("/instances/") ? "/instances/"
								: url.contains("/partnerversions/") ? "/partnerversions/" : "invalid";
						final int b = url.indexOf(parenttype);
						final int e = url.indexOf("/", b + parenttype.length());
						final String prefix = url.substring(0, b) + "/snapshots/";
						final String postfix = url.substring(e);
						url = prefix + currentdetail.getId() + postfix;
					}
					records[i] = new Format(extension, page, -1, "", location, "", url, formatInfo(extension), true);
				} else if (isnative) {
					String page = format.get("page").isString().stringValue();
					page = "CATDrawing".equals(extension) ? page : !page.equals("0001") ? page : "";
					final long filesize = (long) format.get("filesize").isNumber().doubleValue();
					final String filesizestring = 1024 * 1024 > format.get("filesize").isNumber().doubleValue() ? format
							.get("filesize_kb").toString() + " kb"
							: format.get("filesize_mb").toString() + " mb";
					final String location = format.get("location").isString().stringValue();
					final String vaultlocation = location.substring(0, 1).toUpperCase() + location.substring(1);
					final String url = format.get("url").isString().stringValue();
					records[i] = new Format(extension, page, filesize, filesizestring, location, vaultlocation, url,
							formatInfo(extension), false);
				}
			}
		} else {
			formats = null;
			records = new Record[0];
		}
		return records;
	}

	Record[] getDocumentsSummary(final DocumentsSummary event) {
		getDocumentsLoaded(event);
		if (event.isSuccess() && event.detail.equals(currentdetail)) {
			for (int i = 0; i < records.length; ++i) {
				if (0 >= records[i].getAttributeAsLong("filesize")) {
					final String extension = mapExtension(records[i].getAttribute("extension"));
					final long filesize = event.getFileSize(extension);
					final String filesizestring = 0 >= filesize ? "0 kb" : 1024 * 1024 > filesize ? event
							.getFileSizeKb(extension) + " kb" : event.getFileSizeMb(extension) + " mb";
					records[i].setAttribute("filesize", filesize);
					records[i].setAttribute("filesizestring", filesizestring);
					records[i].setAttribute("vaultlocation", event.getVaults(extension));
				}
			}
			int count = 0;
			for (int i = 0; i < records.length; ++i) {
				final String extension = mapExtension(records[i].getAttribute("extension"));
				final long filesize = event.getFileSize(extension);
				if (0 < filesize) {
					++count;
				}
			}
			if (count < records.length) {
				final Record[] newrecords = new Record[count];
				int j = 0;
				for (int i = 0; i < records.length; ++i) {
					if (0 < records[i].getAttributeAsLong("filesize")) {
						newrecords[j++] = records[i];
					}
				}
				records = newrecords;
			}
			final Comparator<Record> comparerecords = new Comparator<Record>() {
				@Override public int compare(final Record a, final Record b) {
					final String extensiona = a.getAttribute("extension");
					final String extensionb = b.getAttribute("extension");
					if ("CATDrawing".equals(extensiona) && "CATDrawing".equals(extensionb)) {
						final String left = a.getAttribute("page");
						final String right = b.getAttribute("page");
						return left.compareTo(right);
					} else {
						final long left = a.getAttributeAsLong("filesize");
						final long right = b.getAttributeAsLong("filesize");
						return left <= right ? -1 : 1;
					}
				}
			};
			Arrays.sort(records, comparerecords);
		}
		return records;
	}

	private String mapExtension(final String extension) {
		if ("CATProduct.jar".equalsIgnoreCase(extension)) {
			return "CATPart";
		}
		if ("3dxml".equalsIgnoreCase(extension)) {
			return "3dxml";
		}
		if ("vfz".equalsIgnoreCase(extension)) {
			return "jt";
		}
		return "Unknown";
	}

	private String formatInfo(final String extension) {
		if ("3dxml".equalsIgnoreCase(extension)) {
			return "A zip-compressed assembly file containing CGR files for the geometries and a 3DXML file for the assembly. <br>It can be opened with Dassault Syst\u00e8mes 3DXML Player and CATIA V5.";
		} else if ("jpg".equalsIgnoreCase(extension)) {
			return "An image format.";
		} else if ("png".equalsIgnoreCase(extension)) {
			return "An image format.";
		} else if ("tif".equalsIgnoreCase(extension)) {
			return "An image format.";
		} else if ("pdf".equalsIgnoreCase(extension)) {
			return "A compressed read-only document format. <br>It can be opened with Adobe Acrobat Reader and compatible tools.";
		} else if ("CATProduct.jar".equalsIgnoreCase(extension)) {
			return "A zip-compressed assembly file containing CATPart files for the geometries and CATProduct files for the assemblies. <br>It can be opened with Dassault Syst\u00e8mes CATIA V5.";
		} else if ("CATPart".equalsIgnoreCase(extension)) {
			return "A native 3D format. <br>It can be opened with Dassault Syst\u00e8mes CATIA V5 and some CATIA V5 converter tools.";
		} else if ("cgr".equalsIgnoreCase(extension)) {
			return "A native light 3D format. <br>It can be opened with Dassault Syst\u00e8mes CATIA V5 and some CATIA V5 converter tools.";
		} else if ("CATDrawing".equalsIgnoreCase(extension)) {
			return "A native 2D format. <br>It can be opened with Dassault Syst\u00e8mes CATIA V5 and some CATIA V5 converter tools.";
		} else if ("vfz".equalsIgnoreCase(extension)) {
			return "A zip-compressed assembly file containing JT files for the geometries and a PLMXML file for the assembly. <br>It can be opened with Siemens PLM JT2GO Viewer and other TeamCenter tools.";
		} else if ("jt".equalsIgnoreCase(extension)) {
			return "A native light 3D format. <br>It can be opened with Siemens PLM JT2GO Viewer and other TeamCenter tools.";
		} else if ("xls".equalsIgnoreCase(extension)) {
			return "A Microsoft Excel file.";
		} else if ("doc".equalsIgnoreCase(extension)) {
			return "A Microsoft Word file.";
		} else if ("ppt".equalsIgnoreCase(extension)) {
			return "A Microsoft Powerpoint file.";
		} else {
			return "No information available for this format.";
		}
	}

	private class Format extends ListGridRecord {
		Format(final String extension, final String page, final long filesize, final String filesizestring,
				final String location, final String vaultlocation, final String url, final String formatinfo,
				final boolean derived) {
			super(JavaScriptObject.createObject());
			setAttribute("extension", extension);
			setAttribute("page", page);
			setAttribute("filesize", filesize);
			setAttribute("filesizestring", filesizestring);
			setAttribute("location", location);
			setAttribute("vaultlocation", vaultlocation);
			setAttribute("url", url);
			setAttribute("formatinfo", formatinfo);
			setAttribute("derived", derived);
		}
	}

	private Detail currentdetail;
	private Record currentrecord;
	private Record[] records;
	private JSONArray formats;

}
