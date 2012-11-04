import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.TableCellEditor;


public class CheckBoxEditor extends AbstractCellEditor implements 
TableCellEditor { 
     /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JCheckBox jc = new JCheckBox(); 
     public CheckBoxEditor() { 
     } 
     public Component getTableCellEditorComponent(JTable table, Object 
value, boolean isSelected, int rowIndex, int vColIndex) { 
         jc.setOpaque(false); 
         jc.setHorizontalAlignment(SwingConstants.LEFT); 
         return jc; 
     } 
     public Object getCellEditorValue() { 
         return ((JCheckBox)jc).isSelected(); 
     }
}
