package de.man.mn.gep.client.shared.event;

import com.ibm.de.ebs.plm.gwt.client.mvp.BusEvent;

import de.man.mn.gep.client.shared.dao.DataType;

public abstract class RemoteSearch extends BusEvent<Object> {

	public DataType datatype;

	public String criterion;

	public Object[] values;

}
