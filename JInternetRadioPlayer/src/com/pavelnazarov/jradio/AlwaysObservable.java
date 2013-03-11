package com.pavelnazarov.jradio;

import java.util.Observable;
/*
 * Always notifies observers instead of Observable that notifies when it is changed only
 */
public class AlwaysObservable extends Observable {
	
	@Override
	public void notifyObservers(Object arg) {
		super.setChanged();
		super.notifyObservers(arg);
	}
}
