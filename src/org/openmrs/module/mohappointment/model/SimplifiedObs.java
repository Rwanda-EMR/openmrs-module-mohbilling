/**
 * 
 */
package org.openmrs.module.mohappointment.model;

import java.util.Date;

import org.openmrs.Obs;
import org.openmrs.api.context.Context;

/**
 * @author Yves GAKUBA
 * 
 */
public class SimplifiedObs {

	private Date obsDatetime;
	private String value;

	public SimplifiedObs(Obs o) {
		this.obsDatetime = o.getObsDatetime();
		this.value = o.getValueAsString(Context.getLocale());
	}

	public Date getObsDatetime() {
		return obsDatetime;
	}

	public void setObsDatetime(Date obsDatetime) {
		this.obsDatetime = obsDatetime;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
