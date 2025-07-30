import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class TimetableGeneratorApp extends JFrame {
    private JTabbedPane tabbedPane;

    // Data models
    private TeacherTableModel teacherModel = new TeacherTableModel();
    private SubjectTableModel subjectModel = new SubjectTableModel();
    private ClassroomTableModel classroomModel = new ClassroomTableModel();

    public TimetableGeneratorApp() {
        setTitle("Automatic Timetable Generator");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        tabbedPane = new JTabbedPane();

        // Add tabs
        tabbedPane.addTab("Teachers", new TeacherPanel(teacherModel));
        tabbedPane.addTab("Subjects", new SubjectPanel(subjectModel));
        tabbedPane.addTab("Classrooms", new ClassroomPanel(classroomModel));
        tabbedPane.addTab("Generate", new GeneratePanel(teacherModel, subjectModel, classroomModel));

        add(tabbedPane);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new TimetableGeneratorApp().setVisible(true);
        });
    }
}

// Teacher Panel

class TeacherPanel extends JPanel {
    private JTable teacherTable;
    private TeacherTableModel tableModel;
    private DefaultListModel<String> allSubjectsModel = new DefaultListModel<>();

    public TeacherPanel(TeacherTableModel model) {
        this.tableModel = model;
        initializeDefaultSubjects();
        setupUI();
    }

    private void initializeDefaultSubjects() {
        String[] defaultSubjects = {"Math", "Science", "History", "English", "Physics", "Chemistry"};
        for (String subject : defaultSubjects) {
            allSubjectsModel.addElement(subject);
        }
    }

    private void setupUI() {
        setLayout(new BorderLayout());

        // Teacher Table
        teacherTable = new JTable(tableModel);
        teacherTable.setRowHeight(30);

        // Buttons Panel
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(createAddTeacherButton());
        buttonPanel.add(createRemoveTeacherButton());
        buttonPanel.add(createManageSubjectsButton());
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(teacherTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private JButton createAddTeacherButton() {
        JButton button = new JButton("Add Teacher");
        button.addActionListener(e -> showAddTeacherDialog());
        return button;
    }

    private JButton createRemoveTeacherButton() {
        JButton button = new JButton("Remove Selected");
        button.addActionListener(e -> removeSelectedTeacher());
        return button;
    }

    private JButton createManageSubjectsButton() {
        JButton button = new JButton("Manage Subjects");
        button.addActionListener(e -> showSubjectManagementDialog());
        return button;
    }

    private void showSubjectManagementDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Subject Management");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 250);
        dialog.setModal(true);

        // Subject List
        JList<String> subjectList = new JList<>(allSubjectsModel);
        JScrollPane scrollPane = new JScrollPane(subjectList);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(createAddSubjectButton(dialog));
        buttonPanel.add(createRemoveSubjectButton(dialog, subjectList));

        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton createAddSubjectButton(JDialog parent) {
        JButton button = new JButton("Add Subject");
        button.addActionListener(e -> {
            String subjectName = JOptionPane.showInputDialog(parent, "Enter subject name:");
            if (subjectName != null && !subjectName.trim().isEmpty()) {
                if (!allSubjectsModel.contains(subjectName.trim())) {
                    allSubjectsModel.addElement(subjectName.trim());
                } else {
                    JOptionPane.showMessageDialog(parent, "Subject already exists!");
                }
            }
        });
        return button;
    }

    private JButton createRemoveSubjectButton(JDialog parent, JList<String> subjectList) {
        JButton button = new JButton("Remove Subject");
        button.addActionListener(e -> {
            int selectedIndex = subjectList.getSelectedIndex();
            if (selectedIndex >= 0) {
                String subject = allSubjectsModel.getElementAt(selectedIndex);
                if (isSubjectAssigned(subject)) {
                    JOptionPane.showMessageDialog(parent,
                            "Cannot remove: Subject is assigned to one or more teachers");
                } else {
                    allSubjectsModel.remove(selectedIndex);
                }
            } else {
                JOptionPane.showMessageDialog(parent, "Please select a subject first");
            }
        });
        return button;
    }

    private boolean isSubjectAssigned(String subject) {
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            String teacherSubjects = (String) tableModel.getValueAt(i, 1);
            if (teacherSubjects.contains(subject)) {
                return true;
            }
        }
        return false;
    }

    private void showAddTeacherDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add Teacher");
        dialog.setLayout(new GridLayout(0, 2, 5, 5));
        dialog.setSize(400, 300);
        dialog.setModal(true);

        // Form components
        JTextField nameField = new JTextField();
        JTextField hoursField = new JTextField("5");
        JList<String> subjectsList = new JList<>(allSubjectsModel);
        subjectsList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

        // Add components to dialog
        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Max Hours/Day:"));
        dialog.add(hoursField);
        dialog.add(new JLabel("Subjects:"));
        dialog.add(new JScrollPane(subjectsList));

        // Save button
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> saveTeacher(
                nameField.getText().trim(),
                hoursField.getText().trim(),
                subjectsList.getSelectedValuesList(),
                dialog
        ));

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void saveTeacher(String name, String hoursStr, List<String> subjects, JDialog dialog) {
        try {
            if (name.isEmpty()) {
                throw new IllegalArgumentException("Teacher name cannot be empty");
            }
            if (subjects.isEmpty()) {
                throw new IllegalArgumentException("Please select at least one subject");
            }

            int maxHours = Integer.parseInt(hoursStr);
            tableModel.addTeacher(new Teacher(name, subjects, maxHours));
            dialog.dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(dialog, "Please enter valid hours (number)");
        } catch (IllegalArgumentException e) {
            JOptionPane.showMessageDialog(dialog, e.getMessage());
        }
    }

    private void removeSelectedTeacher() {
        int row = teacherTable.getSelectedRow();
        if (row >= 0) {
            tableModel.removeTeacher(row);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a teacher first");
        }
    }
}
// Subject Panel
class SubjectPanel extends JPanel {
    private JTable subjectTable;
    private SubjectTableModel tableModel;

    public SubjectPanel(SubjectTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        subjectTable = new JTable(tableModel);
        subjectTable.setRowHeight(30);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Subject");
        JButton removeButton = new JButton("Remove Selected");

        addButton.addActionListener(e -> showAddSubjectDialog());
        removeButton.addActionListener(e -> removeSelectedSubject());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(subjectTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showAddSubjectDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Subject");
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField nameField = new JTextField();
        JTextField hoursField = new JTextField("3");
        JCheckBox labCheckbox = new JCheckBox("Requires Lab");

        dialog.add(new JLabel("Name:"));
        dialog.add(nameField);
        dialog.add(new JLabel("Weekly Hours:"));
        dialog.add(hoursField);
        dialog.add(new JLabel("Lab Requirement:"));
        dialog.add(labCheckbox);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String name = nameField.getText().trim();
                if (name.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter subject name");
                    return;
                }

                int hours = Integer.parseInt(hoursField.getText());
                boolean requiresLab = labCheckbox.isSelected();

                tableModel.addSubject(new Subject(name, hours, requiresLab));
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid weekly hours");
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void removeSelectedSubject() {
        int row = subjectTable.getSelectedRow();
        if (row != -1) {
            tableModel.removeSubject(row);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a subject to remove");
        }
    }
}

// Classroom Panel
class ClassroomPanel extends JPanel {
    private JTable classroomTable;
    private ClassroomTableModel tableModel;

    public ClassroomPanel(ClassroomTableModel model) {
        this.tableModel = model;
        setLayout(new BorderLayout());

        classroomTable = new JTable(tableModel);
        classroomTable.setRowHeight(30);

        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Classroom");
        JButton removeButton = new JButton("Remove Selected");

        addButton.addActionListener(e -> showAddClassroomDialog());
        removeButton.addActionListener(e -> removeSelectedClassroom());

        buttonPanel.add(addButton);
        buttonPanel.add(removeButton);
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(classroomTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void showAddClassroomDialog() {
        JDialog dialog = new JDialog();
        dialog.setTitle("Add New Classroom");
        dialog.setModal(true);
        dialog.setSize(400, 200);
        dialog.setLayout(new GridLayout(0, 2, 5, 5));

        JTextField roomIdField = new JTextField();
        JCheckBox labCheckbox = new JCheckBox("Is Lab");
        JTextField capacityField = new JTextField("30");

        dialog.add(new JLabel("Room ID:"));
        dialog.add(roomIdField);
        dialog.add(new JLabel("Lab Room:"));
        dialog.add(labCheckbox);
        dialog.add(new JLabel("Capacity:"));
        dialog.add(capacityField);

        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> {
            try {
                String roomId = roomIdField.getText().trim();
                if (roomId.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Please enter room ID");
                    return;
                }

                boolean isLab = labCheckbox.isSelected();
                int capacity = Integer.parseInt(capacityField.getText());

                tableModel.addClassroom(new Classroom(roomId, isLab, capacity));
                dialog.dispose();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Please enter valid capacity");
            }
        });

        dialog.add(new JLabel());
        dialog.add(saveButton);
        dialog.setVisible(true);
    }

    private void removeSelectedClassroom() {
        int row = classroomTable.getSelectedRow();
        if (row != -1) {
            tableModel.removeClassroom(row);
        } else {
            JOptionPane.showMessageDialog(this, "Please select a classroom to remove");
        }
    }
}

// Generate Panel
class GeneratePanel extends JPanel {
    private JTable timetableTable;
    private TimetableTableModel timetableModel = new TimetableTableModel();
    private TeacherTableModel teacherModel;
    private SubjectTableModel subjectModel;
    private ClassroomTableModel classroomModel;

    public GeneratePanel(TeacherTableModel tModel, SubjectTableModel sModel, ClassroomTableModel cModel) {
        this.teacherModel = tModel;
        this.subjectModel = sModel;
        this.classroomModel = cModel;

        setLayout(new BorderLayout());

        timetableTable = new JTable(timetableModel);
        timetableTable.setRowHeight(60);

        JPanel buttonPanel = new JPanel();
        JButton generateButton = new JButton("Generate Timetable");
        generateButton.addActionListener(e -> generateTimetable());

        buttonPanel.add(generateButton);
        buttonPanel.setBackground(Color.blue);

        add(new JScrollPane(timetableTable), BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void generateTimetable() {
        List<Teacher> teachers = teacherModel.getTeachers();
        List<Subject> subjects = subjectModel.getSubjects();
        List<Classroom> classrooms = classroomModel.getClassrooms();

        if (teachers.isEmpty() || subjects.isEmpty() || classrooms.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please add at least one teacher, subject and classroom first");
            return;
        }

        TimetableGenerator generator = new TimetableGenerator();
        Timetable timetable = generator.generateTimetable(teachers, subjects, classrooms);

        timetableModel.setTimetable(timetable);
    }
}

// Model Classes
class Teacher {
    private String name;
    private List<String> subjects;
    private int maxHoursPerDay;

    public Teacher(String name, List<String> subjects, int maxHoursPerDay) {
        this.name = name;
        this.subjects = new ArrayList<>(subjects);
        this.maxHoursPerDay = maxHoursPerDay;
    }

    public String getName() { return name; }
    public List<String> getSubjects() { return subjects; }
    public int getMaxHoursPerDay() { return maxHoursPerDay; }
}

class Subject {
    private String name;
    private int weeklyHours;
    private boolean requiresLab;

    public Subject(String name, int weeklyHours, boolean requiresLab) {
        this.name = name;
        this.weeklyHours = weeklyHours;
        this.requiresLab = requiresLab;
    }

    public String getName() { return name; }
    public int getWeeklyHours() { return weeklyHours; }
    public boolean requiresLab() { return requiresLab; }
}

class Classroom {
    private String roomId;
    private boolean isLab;
    private int capacity;

    public Classroom(String roomId, boolean isLab, int capacity) {
        this.roomId = roomId;
        this.isLab = isLab;
        this.capacity = capacity;
    }

    public String getRoomId() { return roomId; }
    public boolean isLab() { return isLab; }
    public int getCapacity() { return capacity; }
}

enum DayOfWeek { MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY }

class Timeslot {
    private DayOfWeek day;
    private int period;

    public Timeslot(DayOfWeek day, int period) {
        this.day = day;
        this.period = period;
    }

    public DayOfWeek getDay() { return day; }
    public int getPeriod() { return period; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return period == timeslot.period && day == timeslot.day;
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, period);
    }
}

class TimetableEntry {
    private Teacher teacher;
    private Classroom classroom;
    private Timeslot timeslot;
    private Subject subject;

    public TimetableEntry(Teacher teacher, Classroom classroom, Timeslot timeslot, Subject subject) {
        this.teacher = teacher;
        this.classroom = classroom;
        this.timeslot = timeslot;
        this.subject = subject;
    }

    public Teacher getTeacher() { return teacher; }
    public Classroom getClassroom() { return classroom; }
    public Timeslot getTimeslot() { return timeslot; }
    public Subject getSubject() { return subject; }
}

class Timetable {
    private Map<Timeslot, TimetableEntry> entries = new HashMap<>();

    public void addEntry(Teacher teacher, Classroom classroom, Timeslot slot, Subject subject) {
        entries.put(slot, new TimetableEntry(teacher, classroom, slot, subject));
    }

    public TimetableEntry getEntry(Timeslot slot) {
        return entries.get(slot);
    }

    public boolean isTeacherAvailable(Teacher teacher, Timeslot slot) {
        return entries.values().stream()
                .noneMatch(e -> e.getTeacher().equals(teacher) && e.getTimeslot().equals(slot));
    }

    public boolean isClassroomAvailable(Classroom classroom, Timeslot slot) {
        return entries.values().stream()
                .noneMatch(e -> e.getClassroom().equals(classroom) && e.getTimeslot().equals(slot));
    }
}

// Table Models
class TeacherTableModel extends AbstractTableModel {
    private List<Teacher> teachers = new ArrayList<>();
    private String[] columns = {"Name", "Subjects", "Max Hours/Day"};

    public void addTeacher(Teacher teacher) {
        teachers.add(teacher);
        fireTableRowsInserted(teachers.size()-1, teachers.size()-1);
    }

    public void removeTeacher(int row) {
        teachers.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<Teacher> getTeachers() { return teachers; }

    @Override public int getRowCount() { return teachers.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int column) {
        Teacher teacher = teachers.get(row);
        switch(column) {
            case 0: return teacher.getName();
            case 1: return String.join(", ", teacher.getSubjects());
            case 2: return teacher.getMaxHoursPerDay();
            default: return null;
        }
    }
}

class SubjectTableModel extends AbstractTableModel {
    private List<Subject> subjects = new ArrayList<>();
    private String[] columns = {"Name", "Weekly Hours", "Requires Lab"};

    public void addSubject(Subject subject) {
        subjects.add(subject);
        fireTableRowsInserted(subjects.size()-1, subjects.size()-1);
    }

    public void removeSubject(int row) {
        subjects.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<Subject> getSubjects() { return subjects; }

    @Override public int getRowCount() { return subjects.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int column) {
        Subject subject = subjects.get(row);
        switch(column) {
            case 0: return subject.getName();
            case 1: return subject.getWeeklyHours();
            case 2: return subject.requiresLab() ? "Yes" : "No";
            default: return null;
        }
    }
}

class ClassroomTableModel extends AbstractTableModel {
    private List<Classroom> classrooms = new ArrayList<>();
    private String[] columns = {"Room ID", "Is Lab", "Capacity"};

    public void addClassroom(Classroom classroom) {
        classrooms.add(classroom);
        fireTableRowsInserted(classrooms.size()-1, classrooms.size()-1);
    }

    public void removeClassroom(int row) {
        classrooms.remove(row);
        fireTableRowsDeleted(row, row);
    }

    public List<Classroom> getClassrooms() { return classrooms; }

    @Override public int getRowCount() { return classrooms.size(); }
    @Override public int getColumnCount() { return columns.length; }
    @Override public String getColumnName(int column) { return columns[column]; }

    @Override
    public Object getValueAt(int row, int column) {
        Classroom classroom = classrooms.get(row);
        switch(column) {
            case 0: return classroom.getRoomId();
            case 1: return classroom.isLab() ? "Yes" : "No";
            case 2: return classroom.getCapacity();
            default: return null;
        }
    }
}

class TimetableTableModel extends AbstractTableModel {
    private Timetable timetable;
    private String[] days = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday"};
    private String[] periods = {"8-9", "9-10", "10-11", "11-12", "Lunch", "1-2", "2-3", "3-4"};

    public void setTimetable(Timetable timetable) {
        this.timetable = timetable;
        fireTableDataChanged();
    }

    @Override public int getRowCount() { return periods.length; }
    @Override public int getColumnCount() { return days.length + 1; }
    @Override public String getColumnName(int column) { return column == 0 ? "Time" : days[column-1]; }

    @Override
    public Object getValueAt(int row, int column) {
        if (column == 0) return periods[row];
        if (timetable == null) return "";

        Timeslot slot = new Timeslot(DayOfWeek.values()[column-1], row+1);
        TimetableEntry entry = timetable.getEntry(slot);

        return entry != null ?
                String.format("<html>%s<br>%s<br>%s</html>",
                        entry.getSubject().getName(),
                        entry.getTeacher().getName(),
                        entry.getClassroom().getRoomId()) :
                "";
    }
}

// Timetable Generator Algorithm
class TimetableGenerator {
    public Timetable generateTimetable(List<Teacher> teachers, List<Subject> subjects, List<Classroom> classrooms) {
        Timetable timetable = new Timetable();
        Random random = new Random();

        // Create a list of all possible timeslots
        List<Timeslot> allTimeslots = new ArrayList<>();
        for (DayOfWeek day : DayOfWeek.values()) {
            for (int period = 1; period <= 8; period++) {
                if (period != 5) { // Skip lunch period
                    allTimeslots.add(new Timeslot(day, period));
                }
            }
        }

        // Try to schedule each subject for its required hours
        for (Subject subject : subjects) {
            int hoursScheduled = 0;

            // Get qualified teachers
            List<Teacher> qualifiedTeachers = teachers.stream()
                    .filter(t -> t.getSubjects().contains(subject.getName()))
                    .collect(Collectors.toList());

            if (qualifiedTeachers.isEmpty()) {
                System.out.println("No qualified teacher for: " + subject.getName());
                continue;
            }

            // Get suitable classrooms
            List<Classroom> suitableRooms = classrooms.stream()
                    .filter(r -> !subject.requiresLab() || r.isLab())
                    .collect(Collectors.toList());

            if (suitableRooms.isEmpty()) {
                System.out.println("No suitable room for: " + subject.getName());
                continue;
            }

            // Try to schedule required hours
            while (hoursScheduled < subject.getWeeklyHours()) {
                boolean scheduled = false;

                // Shuffle to try different combinations
                Collections.shuffle(qualifiedTeachers);
                Collections.shuffle(suitableRooms);
                Collections.shuffle(allTimeslots);

                for (Teacher teacher : qualifiedTeachers) {
                    for (Classroom room : suitableRooms) {
                        for (Timeslot slot : allTimeslots) {
                            if (timetable.isTeacherAvailable(teacher, slot) &&
                                    timetable.isClassroomAvailable(room, slot)) {

                                timetable.addEntry(teacher, room, slot, subject);
                                hoursScheduled++;
                                scheduled = true;
                                break;
                            }
                        }
                        if (scheduled) break;
                    }
                    if (scheduled) break;
                }

                if (!scheduled) {
                    System.out.println("Could not schedule all hours for: " + subject.getName());
                    break;
                }
            }
        }

        return timetable;
    }
}