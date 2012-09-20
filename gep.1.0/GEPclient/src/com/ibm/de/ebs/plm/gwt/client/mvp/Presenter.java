package com.ibm.de.ebs.plm.gwt.client.mvp;

public interface Presenter<M extends Model, V extends View> extends Generated {

	M model();

	V view();

	V bind(V view);

	void unbind();

	EventBus eventBus();

}
