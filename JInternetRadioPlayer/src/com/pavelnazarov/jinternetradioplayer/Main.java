package com.pavelnazarov.jinternetradioplayer;

import com.pavelnazarov.jinternetradioplayer.ui.PlayerForm;
import com.pavelnazarov.jradio.JLRadioPlayer;
import com.pavelnazarov.jradio.RadioPlayer;

public class Main {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		RadioPlayer player = new JLRadioPlayer();
		PlayerForm form = new PlayerForm(player);
		form.setVisible(true);
	}

}
