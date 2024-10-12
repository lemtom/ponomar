package net.ponomar.utility;

import javax.swing.table.DefaultTableModel;

public class SearchTableModel extends DefaultTableModel {

	@Override
	public boolean isCellEditable(int row, int column) {
		return false;
	}

}