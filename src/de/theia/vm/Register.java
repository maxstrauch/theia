package de.theia.vm;

import de.theia.gui.Theia;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import javax.swing.table.AbstractTableModel;

/**
 * Represents the registers of the register machine
 * 
 * @author maximilianstrauch
 */
public class Register extends AbstractTableModel {

    private static final Register THIS = new Register();
    
    /**
     * A hash map is used to store the register value associations
     */
    private final Map<Integer, Integer> memory;
    
    /**
     * A list of keys of the hash map
     */
    private Integer[] keys;
    
    private Register() {
        memory = new HashMap<>();
    }
    
    /**
     * Updates the ordered list of register keys
     */
    private void updateKeyList() {
        Set<Integer> s = memory.keySet();
        keys = new Integer[s.size()];
        s.toArray(keys);
        Arrays.sort(keys);
    }
    
    /**
     * Updates the JTable view
     */
    private void fireContentsChanged() {
        updateKeyList();
        fireTableDataChanged();
    }

    /**
     * Clears all registers
     */
    public void clear() {
        updateKeyList();
        memory.clear();
        fireContentsChanged();
    }
    
    /**
     * Returns the value for a register or zero if not set yet
     * 
     * @param register The register
     * @return The value of the register
     */
    public int getValue(int register) {
        if (!memory.containsKey(register)) {
            setValue(register, 0);
        }
        return memory.get(register);
    }
    
    /**
     * Sets a register value
     * 
     * @param register Number of the register
     * @param value Value to set
     */
    public void setValue(int register, int value) {
        memory.put(register, value);
        fireContentsChanged();
    }
    
//    public String print() {
//        StringBuilder buf = new StringBuilder();
//        buf.append(memory.toString());
//        return buf.toString();
//    }
    
    @Override
    public String getColumnName(int column) {
        ResourceBundle res = Theia.getInstance().getMessages();
        return res.getString("registerCol" + column);
    }
    
    @Override
    public int getRowCount() {
        return memory.size();
    }

    @Override
    public int getColumnCount() {
        return 2;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (keys.length <= rowIndex) {
            updateKeyList();
        }
        
        if (columnIndex == 0) {
            return "x" + keys[rowIndex];
        } else {
            return getValue(keys[rowIndex]);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (columnIndex == 0) {
            return String.class;
        } else {
            return Integer.class;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        try {
            setValue(keys[rowIndex], Integer.parseInt(String.valueOf(aValue)));
        } catch (Exception e) {
            setValue(keys[rowIndex], 0);
        }
    }
    
    public static final Register getInstance() {
        return THIS;
    }
    
}
