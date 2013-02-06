package org.openmrs.module.mohappointment.observerpattern;

public interface ISubject {
	void addObserver(IObserver observer);

	void delObserver(IObserver observer);

	void notifyObservers();
}
