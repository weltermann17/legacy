package de.man.mn.gep.client.shared.event;

import com.google.gwt.json.client.JSONValue;
import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.ibm.de.ebs.plm.gwt.client.restlet.RestletDataSource;
import com.smartgwt.client.widgets.grid.ListGrid;

import de.man.mn.gep.client.shared.dao.DataType;
import de.man.mn.gep.client.shared.dao.Detail;
import de.man.mn.gep.client.shared.dao.Instance;
import de.man.mn.gep.client.shared.dao.Product;
import de.man.mn.gep.client.shared.dao.Snapshot;
import de.man.mn.gep.client.shared.dao.Version;

public abstract class DetailSelected extends JSONValueEvent {

	public Detail rowdetail;

	public Detail detail;

	public RestletDataSource datasource;

	public String delimiter;

	public int currentrow;

	public ListGrid listgrid;

	@Override public void setData(final JSONValue data) {
		super.setData(data);
		DataType datatype;
		if (null == rowdetail) {
			datatype = DataType.valueOf(getDataArray().get(0).isObject().get("datatype").isString().stringValue());
		} else {
			datatype = rowdetail.getDataType();
		}
		if (isSuccess()) {
			switch (datatype) {
			case versions:
			case bom:
			case whereused:
				detail = new Version(getDataArray().get(0).isObject());
				break;
			case instances:
				detail = new Instance(getDataArray().get(0).isObject());
				break;
			case products:
				detail = new Product(getDataArray().get(0).isObject());
				break;
			case snapshots:
				detail = new Snapshot(getDataArray().get(0).isObject());
				break;
			default:
				getLogger().severe("Unhandled datatype " + detail.getDataType().name());
				break;
			}
			if (null == rowdetail) {
				rowdetail = detail;
			}
		}
	}

}
