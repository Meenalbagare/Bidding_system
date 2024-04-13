package comModels;

import java.util.List;
import javax.swing.table.AbstractTableModel;
import companyCore.Item;

public class ItemModel extends AbstractTableModel {

    public static final int OBJECT_COL = -1;
    private String[] colName;
    private List<Item> symWithD;

    public ItemModel(List<Item> theSymWithD, String[] colName) {
        this.symWithD = theSymWithD;
        this.colName = colName;
    }

    @Override
    public int getColumnCount() {
        return colName.length;
    }

    @Override
    public int getRowCount() {
        return symWithD.size();
    }

    @Override
    public String getColumnName(int col) {
        return colName[col];
    }

    @Override
    public Object getValueAt(int row, int col) {
        Item tmpItem = symWithD.get(row);

        switch (col) {
            case 0:
                return tmpItem.getSym();
            case 1:
                return tmpItem.getPrice();
            case 2:
                return tmpItem.getProfit();
            case OBJECT_COL:
                return tmpItem;
            default:
                return tmpItem.getSym();
        }
    }

    @Override
    public Class getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }
}
