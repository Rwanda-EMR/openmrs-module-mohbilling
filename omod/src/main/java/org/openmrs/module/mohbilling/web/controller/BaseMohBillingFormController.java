/**
 * 
 */
package org.openmrs.module.mohbilling.web.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.ParameterizableViewController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author rbcemr
 *
 */
public class BaseMohBillingFormController extends ParameterizableViewController {

	public BaseMohBillingFormController() {
		setSupportedMethods(
				ParameterizableViewController.METHOD_POST,
				ParameterizableViewController.METHOD_GET,
				ParameterizableViewController.METHOD_HEAD
		);
	}
}
