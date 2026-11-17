package com.kleberandrade.committeetimetable.app;

import com.kleberandrade.committeetimetable.domain.*;
import com.kleberandrade.committeetimetable.domain.Planning.*;
import com.kleberandrade.committeetimetable.io.PlanFiles;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.nio.file.*;
import java.util.Comparator;

/** Desktop demonstration; file input and the solver run off the event dispatch thread. */
public final class PlannerWindow extends JFrame {
    private static final Color INK = new Color(24,42,58), MUTED = new Color(72,91,105), PAPER = new Color(244,247,249), ACCENT = new Color(0,103,102);
    private Path input = Path.of("examples/feasible");
    private final JTextArea source = textArea(input.toString());
    private final JTextArea status = textArea("No schedule yet. Choose a dataset and generate a plan.");
    private final JLabel count = new JLabel("—"), outcome = new JLabel("Ready to plan");
    private final JButton run = button("Generate schedule",'G'), choose = button("Open dataset…",'O'), export = button("Export CSV…",'E');
    private final JRadioButton feasible = new JRadioButton("Feasible example",true), infeasible = new JRadioButton("Infeasible example");
    private final DefaultTableModel rows = new DefaultTableModel(new String[]{"Defense / project","Date","Time","Room","Professors","Students"},0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(rows);
    private Result result;
    private PlannerWindow() {
        super("Committee Timetable · Defense planning");
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(1000,680)); setSize(1200,760); setLocationRelativeTo(null);
        var root = new JPanel(new BorderLayout(24,24)); root.setBackground(PAPER); root.setBorder(new EmptyBorder(28,32,24,32)); setContentPane(root);
        root.add(header(),BorderLayout.NORTH); root.add(sidebar(),BorderLayout.WEST); root.add(schedule(),BorderLayout.CENTER);
        var footer = label("LOCAL WORKSPACE  ·  Synthetic examples  ·  Fixed committees of three  ·  No database required",12,MUTED);
        root.add(footer,BorderLayout.SOUTH);
        export.setEnabled(false);
        var group = new ButtonGroup(); group.add(feasible); group.add(infeasible);
        feasible.addActionListener(e -> selectExample("feasible")); infeasible.addActionListener(e -> selectExample("infeasible"));
        choose.addActionListener(e -> chooseDirectory()); run.addActionListener(e -> generate()); export.addActionListener(e -> export());
        source.getAccessibleContext().setAccessibleName("Selected dataset directory"); status.getAccessibleContext().setAccessibleName("Planning status and diagnostics");
        table.getAccessibleContext().setAccessibleName("Generated defense timetable");
    }
    public static void open() {
        UIManager.put("defaultFont",new Font(Font.SANS_SERIF,Font.PLAIN,14));
        new PlannerWindow().setVisible(true);
    }
    private JPanel header() {
        var panel = new JPanel(new GridLayout(3,1,0,7)); panel.setOpaque(false);
        panel.add(label("COMMITTEE TIMETABLE / PLANNING STUDIO",12,ACCENT));
        panel.add(label("A clear plan for every defense.",32,INK));
        panel.add(label("Match committees, students and rooms within the availability you provide.",15,MUTED));
        return panel;
    }
    private JScrollPane sidebar() {
        var scroll = new JScrollPane(controls());
        scroll.setBorder(null); scroll.setPreferredSize(new Dimension(285,400));
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        return scroll;
    }
    private JPanel controls() {
        var panel = new SidebarPanel(); panel.setLayout(new BoxLayout(panel,BoxLayout.Y_AXIS)); panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(20,22,20,22));
        add(panel,label("01  Prepare your data",19,INK));
        add(panel,textArea("Choose a synthetic example or a folder with three UTF-8 TSV files."));
        feasible.setOpaque(false); infeasible.setOpaque(false);
        add(panel,feasible); add(panel,infeasible); add(panel,choose); add(panel,source);
        add(panel,label("02  Generate a plan",19,INK));
        add(panel,textArea("Three professors including the advisor. Participant and room conflicts are checked."));
        add(panel,run); add(panel,export);
        panel.add(Box.createVerticalGlue());
        return panel;
    }
    private JPanel schedule() {
        var panel = new JPanel(new BorderLayout(0,18)); panel.setOpaque(false);
        var cards = new JPanel(new GridLayout(1,2,16,0)); cards.setOpaque(false);
        cards.add(card("SCHEDULED DEFENSES",count)); cards.add(card("PLAN STATUS",outcome)); panel.add(cards,BorderLayout.NORTH);
        table.setRowHeight(46); table.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); table.setForeground(INK);
        table.setShowGrid(false); table.setIntercellSpacing(new Dimension(12,0)); table.setFillsViewportHeight(true); table.setAutoCreateRowSorter(true);
        table.getTableHeader().setFont(new Font(Font.SANS_SERIF,Font.BOLD,12)); table.getTableHeader().setPreferredSize(new Dimension(0,40));
        table.setSelectionBackground(new Color(211,235,233)); table.setSelectionForeground(INK);
        table.getColumnModel().getColumn(0).setPreferredWidth(210);
        var scroll = new JScrollPane(table); scroll.setBorder(BorderFactory.createLineBorder(new Color(217,225,231))); panel.add(scroll,BorderLayout.CENTER);
        status.setBorder(new EmptyBorder(16,18,16,18)); status.setBackground(Color.WHITE); status.setOpaque(true); status.setRows(3); panel.add(status,BorderLayout.SOUTH);
        return panel;
    }
    private JPanel card(String title, JLabel value) {
        var panel = new JPanel(new GridLayout(2,1,0,8)); panel.setBackground(Color.WHITE); panel.setBorder(new EmptyBorder(18,20,18,20));
        panel.add(label(title,11,MUTED)); value.setFont(new Font(Font.SANS_SERIF,Font.BOLD,23)); value.setForeground(INK); panel.add(value); return panel;
    }
    private void selectExample(String directory) { input = Path.of("examples",directory); reset(); }
    private void reset() {
        result = null; rows.setRowCount(0); source.setText(input.toString()); count.setText("—"); outcome.setText("Ready to plan"); export.setEnabled(false);
        status.setText("No schedule yet. Generate a plan for the selected dataset.");
    }
    private void chooseDirectory() {
        var picker = new JFileChooser(input.toAbsolutePath().toFile()); picker.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (picker.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) { input = picker.getSelectedFile().toPath(); reset(); }
    }
    private void busy(boolean value) { run.setEnabled(!value); choose.setEnabled(!value); feasible.setEnabled(!value); infeasible.setEnabled(!value); export.setEnabled(!value && exportable()); }
    private boolean exportable() { if (result == null) return false; return result.status() == Status.FEASIBLE; }
    private void generate() {
        reset(); busy(true); outcome.setText("Planning…"); status.setText("Reading files and checking candidate slots…");
        new SwingWorker<Result,Void>() {
            @Override protected Result doInBackground() throws Exception { return new Scheduler().solve(PlanFiles.read(input),100_000); }
            @Override protected void done() {
                try { display(get()); }
                catch (Exception failure) { outcome.setText("Check input"); status.setText("Could not generate a plan: " + rootMessage(failure)); }
                finally { busy(false); }
            }
        }.execute();
    }
    private void display(Result plan) {
        result = plan; count.setText(Integer.toString(plan.assignments().size()));
        outcome.setText(PlanPresentation.title(plan.status()));
        plan.assignments().stream().sorted(Comparator.comparing((Assignment a) -> a.slot().window().day()).thenComparing(a -> a.slot().window().start()).thenComparing(a -> a.defense().id())).forEach(a -> {
            var w = a.slot().window(); var d = a.defense();
            rows.addRow(new Object[]{d.id() + " · " + d.title(),w.day(),w.start() + "–" + w.end(),a.slot().room(),PlanFiles.joined(d.professors()),PlanFiles.joined(d.students())});
        });
        status.setText(PlanPresentation.message(plan));
    }
    private void export() {
        var picker = new JFileChooser(); picker.setSelectedFile(new java.io.File("committee-schedule.csv"));
        if (picker.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) exportTo(picker.getSelectedFile().toPath());
    }
    private void exportTo(Path path) {
        if (!replaceAllowed(path)) return;
        writeExport(path);
    }
    private void writeExport(Path path) {
        try { PlanFiles.write(path,result); status.setText("Schedule exported to " + path.toAbsolutePath()); }
        catch (Exception failure) { status.setText("Export failed: " + rootMessage(failure)); }
    }
    private boolean replaceAllowed(Path path) {
        if (!Files.exists(path)) return true;
        return JOptionPane.showConfirmDialog(this,"Replace existing file?","Export CSV",JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }

    private static final class SidebarPanel extends JPanel implements Scrollable {
        @Override public Dimension getPreferredScrollableViewportSize() { return getPreferredSize(); }
        @Override public int getScrollableUnitIncrement(Rectangle visible, int orientation, int direction) { return 16; }
        @Override public int getScrollableBlockIncrement(Rectangle visible, int orientation, int direction) { return visible.height; }
        @Override public boolean getScrollableTracksViewportWidth() { return true; }
        @Override public boolean getScrollableTracksViewportHeight() { return false; }
    }
    private static String rootMessage(Throwable error) { while (error.getCause() != null) error = error.getCause(); return error.getMessage(); }
    private static JLabel label(String text, int size, Color color) { var label = new JLabel(text); label.setFont(new Font(Font.SANS_SERIF,Font.BOLD,size)); label.setForeground(color); return label; }
    private static JTextArea textArea(String text) { var area = new JTextArea(text); area.setFont(new Font(Font.SANS_SERIF,Font.PLAIN,13)); area.setForeground(MUTED); area.setEditable(false); area.setLineWrap(true); area.setWrapStyleWord(true); area.setOpaque(false); area.setColumns(24); area.setRows(Math.max(1,(text.length()+32)/33)); return area; }
    private static JButton button(String text, char mnemonic) { var button = new JButton(text); button.setMnemonic(mnemonic); button.setFont(new Font(Font.SANS_SERIF,Font.BOLD,14)); return button; }
    private static void add(JPanel parent, JComponent child) { child.setAlignmentX(Component.LEFT_ALIGNMENT); child.setMinimumSize(new Dimension(0,child.getPreferredSize().height)); child.setMaximumSize(new Dimension(Integer.MAX_VALUE,child.getPreferredSize().height)); parent.add(child); parent.add(Box.createVerticalStrut(8)); }
}
