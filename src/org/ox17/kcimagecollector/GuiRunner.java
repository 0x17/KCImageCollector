package org.ox17.kcimagecollector;

public class GuiRunner {

	public static void main(String[] args) {
		try {
			if(!Helpers.getOSName().equals("OSX"))
				Helpers.enableNimbusLAF();

			SettingsView sv = new SettingsView();
			sv.setVisible(true);
		} catch(Exception e) {
			Helpers.showException(e);
			e.printStackTrace();
		}
	}

}
