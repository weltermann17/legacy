package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.JSONValueEvent;
import com.smartgwt.client.data.DataSource;
import com.smartgwt.client.data.Record;

import de.man.mn.gep.client.shared.dao.DataType;

public abstract class SearchResult extends JSONValueEvent {

	public String url;

	public String alias;

	public String delimiter;

	public boolean recycle = true;

	public DataSource datasource;

	public boolean onelocation = false;

	public boolean startediting = false;

	public Record editingrecord;

	public DataType datatype;

}
