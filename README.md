# Automatic Timetable Generator

A Java Swing-based desktop application that automatically generates class timetables based on teachers, subjects, and classroom constraints.

## Features

### 🧑‍🏫 Teacher Management
- Add teachers with their qualified subjects
- Set maximum hours per day for each teacher
- Manage subject assignments
- Remove teachers from the system

### 📚 Subject Management
- Add subjects with weekly hour requirements
- Specify lab requirements for subjects
- Track which subjects need special lab facilities

### 🏫 Classroom Management
- Add classrooms with capacity information
- Mark classrooms as lab facilities
- Manage room availability

### 🗓️ Automatic Timetable Generation
- Constraint-based scheduling algorithm
- Ensures no teacher/classroom conflicts
- Respects lab requirements for subjects
- Distributes classes across the week (Monday-Friday)
- 8 periods per day with lunch break

## Getting Started

### Prerequisites
- Java 8 or higher
- Java Swing (included in JDK)

### Running the Application
1. Compile the Java file:
   ```bash
   javac src/TimetableGeneratorApp.java
   ```

2. Run the application:
   ```bash
   java -cp src TimetableGeneratorApp
   ```

## How to Use

### 1. Add Teachers
- Go to the "Teachers" tab
- Click "Add Teacher"
- Enter teacher name, maximum hours per day, and select subjects they can teach
- Use "Manage Subjects" to add new subjects to the system

### 2. Add Subjects
- Go to the "Subjects" tab
- Click "Add Subject"
- Enter subject name, weekly hours required, and whether it needs a lab

### 3. Add Classrooms
- Go to the "Classrooms" tab
- Click "Add Classroom"
- Enter room ID, capacity, and mark if it's a lab facility

### 4. Generate Timetable
- Go to the "Generate" tab
- Click "Generate Timetable"
- The system will automatically create a schedule based on all constraints

## Algorithm Features

The timetable generator uses a constraint-satisfaction approach:
- **Teacher Availability**: Ensures no teacher is double-booked
- **Classroom Availability**: Prevents room conflicts
- **Subject-Teacher Matching**: Only assigns qualified teachers
- **Lab Requirements**: Matches lab subjects with lab facilities
- **Hour Distribution**: Spreads classes across the week

## Time Slots

- **Days**: Monday to Friday
- **Periods**: 8 periods per day (8-9, 9-10, 10-11, 11-12, Lunch, 1-2, 2-3, 3-4)
- **Lunch Break**: Period 5 is reserved for lunch

## Default Subjects

The system comes with pre-loaded subjects:
- Math
- Science
- History
- English
- Physics
- Chemistry

## Project Structure

```
src/
└── TimetableGeneratorApp.java    # Main application file containing all classes
```

### Key Classes
- `TimetableGeneratorApp`: Main application window
- `TeacherPanel`, `SubjectPanel`, `ClassroomPanel`: UI panels for data management
- `GeneratePanel`: Timetable generation interface
- `Teacher`, `Subject`, `Classroom`: Data model classes
- `TimetableGenerator`: Core scheduling algorithm
- `Timetable`: Manages scheduled entries and conflicts

## Limitations

- Currently supports single-class scheduling (no multiple classes per subject)
- Fixed 5-day, 8-period schedule
- Simple random-based algorithm (may not find optimal solutions for complex constraints)
- No persistence (data is lost when application closes)

## Future Enhancements

- [ ] Save/load timetables to file
- [ ] Multiple class sections support
- [ ] Advanced optimization algorithms
- [ ] Export to PDF/Excel
- [ ] Conflict resolution suggestions
- [ ] Teacher preference settings