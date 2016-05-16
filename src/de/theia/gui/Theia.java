package de.theia.gui;

import de.theia.vm.Register;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.text.AbstractDocument;
import de.theia.vm.Language;
import de.theia.vm.Lexer;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.text.AttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.text.TabSet;
import javax.swing.text.TabStop;
import de.theia.vm.Compiler;
import de.theia.vm.PrettyPrint;
import de.theia.vm.RecognitionException;
import de.theia.vm.VM;
import de.theia.vm.VMException;
import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import javax.swing.InputMap;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;

/**
 * The main GUI
 * 
 * @author maximilianstrauch
 */
public class Theia extends JPanel implements ActionListener {
    
    /**
     * Name of this application
     */
    public static final String APP_NAME = "Theia";
    
    /**
     * Version of this application
     */
    public static final String APP_VERSION = "1.0";
    
    /**
     * This frame
     */
    private static JFrame frame;
    
    /**
     * Program messages
     */
    private final ResourceBundle messages;
    
    /**
     * Source code editor text pane
     */
    private JTextPane source;
    
    /**
     * Table containing all currently used registers
     */
    private JTable registers;
    
    /**
     * Used to set the programming language used for the register machine
     */
    private JComboBox<Language> modeSelection;
    
    /**
     * Status bar at the bottom
     */
    private JLabel statusBar;
    
    /**
     * Buttons to alter the registers
     */
    private JButton add, clear, run, stop, preview;
    
    /**
     * Main execution object
     */
    private ProgramExecutor executor;
    
    /**
     * Singelton object
     */
    private static Theia THIS;
    
    /**
     * Creates a new instance
     */
    public Theia() {
        super.setLayout(new BorderLayout(5, 5));
        super.setBorder(null);
        THIS = this;
        
        // Load the GUI strings
        messages = ResourceBundle.getBundle("de.theia.res.MessagesBundle");
        
        initGui();
        setStatus("welcome");
    }

    /**
     * Creates all components and lays them out
     */
    private void initGui() {
        modeSelection = new JComboBox<>(Language.values());
        statusBar = new JLabel();registers = new JTable();
        registers.setModel(Register.getInstance());
        
        // Create register container
        JTabbedPane registerPane = new JTabbedPane();
        {
            JPanel regContainer = new JPanel(new BorderLayout(5, 5));
            regContainer.setOpaque(false);
            regContainer.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));
            regContainer.add(new JScrollPane(registers), BorderLayout.CENTER);
            Box buttons = Box.createHorizontalBox();
            buttons.add(add = createButton(null, Icon.ADD));
            buttons.add(Box.createHorizontalStrut(4));
            buttons.add(clear = createButton(null, Icon.CLEAR));
            buttons.add(Box.createHorizontalGlue());
            regContainer.add(buttons, BorderLayout.NORTH);
            registerPane.addTab(messages.getString("registerTab"), regContainer);
        }
        
        // The source text pane
        JPanel sourceWrap = new JPanel(new BorderLayout());
        {
            source = new JTextPane();
            source.setFont(Font.decode(Font.MONOSPACED).deriveFont(14f));

            // Set the tab size to two characters
            StyleContext sc = StyleContext.getDefaultStyleContext();
            TabSet tabs = new TabSet(new TabStop[] {new TabStop(
                    source.getFontMetrics(source.getFont()).charWidth('x')*2) 
            });
            AttributeSet paraSet = sc.addAttribute(
                    SimpleAttributeSet.EMPTY, 
                    StyleConstants.TabSet, 
                    tabs
            );
            source.setParagraphAttributes(paraSet, false);
            
            // Apply the filter
            ((AbstractDocument) source.getDocument()).setDocumentFilter(
                    new KeywordDocumentFilter(source)
            );
            
            sourceWrap.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
            sourceWrap.add(new JScrollPane(source), BorderLayout.CENTER);
        }
        
        // Show a demo program
        source.setText(
            "x1 := 5 ;\n" +
            "\n" +
            "x3 := x1 ;\n" +
            "x2 := 1 ;\n" +
            "loop x1 do\n" +
            "	x2 := x2 * x3 ;\n" +
            "	x3 := x3 - 1\n" +
            "end\n" +
            "x3 := 0"
        );

        // Main split
        JSplitPane main = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, 
                sourceWrap, 
                registerPane
        );
        main.setDividerLocation(550);
        main.setBorder(null);
        statusBar.setBorder(BorderFactory.createEmptyBorder(0, 4, 4, 4));    
        add(createToolBar(), BorderLayout.NORTH);
        add(main, BorderLayout.CENTER);
        add(statusBar, BorderLayout.SOUTH);
    }

    /**
     * Returns the resource bundle with the GUI strings
     */
    public ResourceBundle getMessages() {
        return messages;
    }
    
    /**
     * Sets the text for the status bar
     * 
     * @param status Either a string, a language key or <code>null</code> to
     * clear the current text
     */
    public void setStatus(String status) {
        if (status != null && messages.containsKey("status." + status)) {
            statusBar.setText(messages.getString("status." + status));
            return;
        }
        statusBar.setText(status);
    }
    
    /**
     * Creates the {@link JToolBar} for this GUI
     * 
     * @return The created toolbar
     */
    private JToolBar createToolBar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);
        toolBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
        
        // Populate with buttons
        toolBar.add(run = createButton("run", Icon.RUN));
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(stop = createButton("stop", Icon.STOP));
        stop.setEnabled(false);
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.addSeparator();
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(preview = createButton("previewBytecode", Icon.BYTECODE));
        toolBar.add(Box.createHorizontalStrut(4));
        toolBar.add(createButton("info", Icon.INFO));
        toolBar.add(Box.createHorizontalGlue());
        
        // Add operation mode selector
        toolBar.add(new JLabel(messages.getString("operationMode")));
        modeSelection.setMaximumSize(new Dimension(60, 60));
        toolBar.add(modeSelection);
        
        return toolBar;
    }    
    
    /**
     * Creats a button to display on this GUI and attaches it to
     * the {@link ActionListener} of this class. The action command
     * is the supplied title in uppercase letters
     * 
     * @param title Title of the button or language key
     * @param icon Icon of the button or <code>null</code>
     * @return The created button
     */
    private JButton createButton(String title, ImageIcon icon) {
        JButton btn = new JButton(
                messages.containsKey("button." + title) ? 
                        messages.getString("button." + title) : title, 
                icon
        );
        if (icon != null) {
            btn.setVerticalTextPosition(SwingConstants.BOTTOM);
            btn.setHorizontalTextPosition(SwingConstants.CENTER);
        }
        if (title != null) {
            btn.setActionCommand(title.toUpperCase());
        }
        btn.addActionListener(this);
        return btn;
    }
    
    @Override
    public void actionPerformed(ActionEvent e) {
        String cmd = e.getActionCommand();
        
        // Register actions
        
        if (e.getSource() == clear) {
            // Clear all registers memory
            Register.getInstance().clear();
        }
        
        if (e.getSource() == add) {
            // Add a new registry entry or update an existing one
            JIntegerTextField reg = new JIntegerTextField(1, Integer.MAX_VALUE);
            JIntegerTextField val = new JIntegerTextField();
            
            // Show an option pane modal dialog
            int retval = JOptionPane.showConfirmDialog(
                    this, 
                    SForm
                        .create()
                        .addRow(messages.getString("caption.register"), reg)
                        .addRow(messages.getString("caption.value"), val), 
                    messages.getString("title.setRegister"), 
                    JOptionPane.OK_CANCEL_OPTION, 
                    JOptionPane.QUESTION_MESSAGE, 
                    Icon.DIALOG_QUESTION
            );
            
            if (retval == JOptionPane.OK_OPTION) {
                // Apply operation
                Register
                    .getInstance()
                    .setValue((int) reg.getValue(), (int) val.getValue());
            }
            return;
        }
        
        // Execution action
        
        if ("RUN".equals(cmd)) {
            if (executor != null) {
                executor.skipDone();
                executor.terminate();
                executor.cancel(true);
                executor = null;
            }
            
            // Compile it
            int[] bytecode = compile();
            if (bytecode == null) {
                return; // Error during compilation
            }

            // Run the program
            executor = new ProgramExecutor(bytecode);
            executor.before();
            executor.execute();
        } else if ("STOP".equals(cmd)) {
            if (executor != null) {
                executor.terminate();
                executor.cancel(true);
                executor = null;
            }
        } else if ("PREVIEWBYTECODE".equals(cmd)) {
            // Get the bytecode
            int[] bytecode = compile();
            if (bytecode == null) {
                return; // Error during compilation
            }
            
            // Show the bytecode
            String src = PrettyPrint.print(bytecode);
            JTextArea ta = new JTextArea(src, 18, 36);
            ta.setFont(Font.decode(Font.MONOSPACED));
            ta.setEditable(false);
            showInfoModal(new JScrollPane(ta));
        } else if ("INFO".equals(cmd)) {
            showInfoModal(MessageFormat.format(
                    messages.getString("about"), 
                    APP_NAME, APP_VERSION
            ));
        }
    }
    
    /**
     * Compiles the current source and displays an error message if needed
     * 
     * @return The compiled bytecode
     */
    private int[] compile() {
        try {
            String txt = source.getText();
            Lexer l = new Lexer(txt);
            Compiler c = new Compiler(l, (Language) modeSelection.getSelectedItem());
            return (int[]) c.compile();
        } catch (RecognitionException e) {
            if (e.hasLineInfo()) {
                showAlertModal(MessageFormat.format(
                        messages.getString("alert.regonizeErrLn"), 
                        e.getLine(),
                        e.getPos(),
                        e.getMessage()
                )); 
            } else {
                showAlertModal(MessageFormat.format(
                        messages.getString("alert.regonizeErr"), 
                        e.getMessage()
                )); 
            }
            
            if (e.hasSelectionInfo()) {
                source.select(e.getStart(), e.getEnd());
                source.requestFocus();
            }
        } catch (Exception e) {
            showAlertModal(MessageFormat.format(
                    messages.getString("alert.auxCompilation"), 
                    String.valueOf(e)
            ));
        }
        return null;
    }
    
    
    public boolean showQuestionModal(Object message) {
        return JOptionPane.showConfirmDialog(
                frame, 
                message, 
                messages.getString("title.questionModal"), 
                JOptionPane.OK_CANCEL_OPTION, 
                JOptionPane.QUESTION_MESSAGE, 
                Icon.DIALOG_QUESTION
        ) == JOptionPane.OK_OPTION;
    }
    
    public void showAlertModal(String message) {
        JOptionPane.showMessageDialog(
                frame, 
                message, 
                messages.getString("title.alertModal"), 
                JOptionPane.ERROR_MESSAGE, 
                Icon.DIALOG_ALERT
        );
    }
    public void showInfoModal(Object message) {
        JOptionPane.showMessageDialog(
                frame, 
                message, 
                messages.getString("title.infoModal"), 
                JOptionPane.INFORMATION_MESSAGE, 
                Icon.DIALOG_INFO
        );
    }
    
    /**
     * {@link SwingWorker} to execute the VM and run the program
     */
    private class ProgramExecutor extends SwingWorker<String, Void> {
    
        /**
         * The virtual machine to execute
         */
        private final VM vm;
        
        /**
         * The VM exception object if any
         */
        private VMException exception;
        
        /**
         * State indicators
         */
        private boolean userTerminated, skipDone;
        
        /**
         * Duration of program execution
         */
        private long duration;
        
        public ProgramExecutor(int[] bytecode) {
            this.vm = new VM(bytecode);
            this.userTerminated = false;
        }
        
        public void skipDone() {
            skipDone = true;
        }
        
        public void before() {
            run.setEnabled(false);
            stop.setEnabled(true);
            registers.setEnabled(false);
            source.setEnabled(false);
            modeSelection.setEnabled(false);
            preview.setEnabled(false);
            add.setEnabled(false);
            clear.setEnabled(false);
            setStatus("executing");
        }
        
        public void terminate() {
            vm.stop();
            userTerminated = true;
        }
        
        @Override
        protected String doInBackground() throws Exception {
            duration = System.currentTimeMillis();
            try {
                vm.execute();
            } catch (VMException e) {
                this.exception = e;
            }
            duration = System.currentTimeMillis() - duration;
            return null;
        }

        @Override
        protected void done() {
            if (skipDone) {
                return;
            }
            
            if (exception != null) {
                showAlertModal(MessageFormat.format(
                        messages.getString("alert.vmErr"), 
                        exception.toString()
                ));
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("ss.SSS");
            Date d = new Date(duration - TimeZone.getDefault().getRawOffset());
            String msg = messages.getString(userTerminated ?
                    "info.terminated" : "info.finished");
            
            showInfoModal(MessageFormat.format(msg, sdf.format(d)));
            
            // Release controls
            run.setEnabled(true);
            stop.setEnabled(false);
            registers.setEnabled(true);
            source.setEnabled(true);
            modeSelection.setEnabled(true);
            preview.setEnabled(true);
            add.setEnabled(true);
            clear.setEnabled(true);
            setStatus("finished");
        }
        
    }
    
    /**
     * Returns the currently running GUI instance
     * 
     * @return This instance
     */
    public static final Theia getInstance() {
        return THIS;
    }
    
    /**
     * Create the GUI and show it
     */
    private static void createAndShowGUI() {
        // Create and set up the window
        frame = new JFrame(APP_NAME + " (v " + APP_VERSION + ")");
        frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);

        //Create and set up the content pane.
        final Theia newContentPane = new Theia();
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        
        // Display the window
        frame.pack();
        frame.setSize(800, 600);
        frame.setVisible(true);
    }
    
    /**
     * @param args The command line arguments
     */
    public static void main(String[] args) {
        
        // The Nimbus LaF is nice ...
        try {
            for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Silence is gold
        }
        
        // Rewire keys
        InputMap im = (InputMap) UIManager.get("TextPane.focusInputMap");
        im.put(KeyStroke.getKeyStroke(
                KeyEvent.VK_C, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), 
                DefaultEditorKit.copyAction
        );
        im.put(KeyStroke.getKeyStroke(
                KeyEvent.VK_V, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), 
                DefaultEditorKit.pasteAction
        );
        im.put(KeyStroke.getKeyStroke(
                KeyEvent.VK_X, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), 
                DefaultEditorKit.cutAction
        );
        im.put(KeyStroke.getKeyStroke(
                KeyEvent.VK_A, 
                Toolkit.getDefaultToolkit().getMenuShortcutKeyMask()), 
                DefaultEditorKit.selectAllAction
        );

        // Show GUI
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
    }
    
}
