public class Course {
    private String sectionNumber;
    private String coursePrefix;
    private int courseNumber;
    private String title;
    private String crn;
    private double units;
    private int courseLengthWeeks;

    public Course(String sectionNumber, String coursePrefix, int courseNumber,
                  String title, String crn, double units, int courseLengthWeeks) {
        this.sectionNumber = sectionNumber;
        this.coursePrefix = coursePrefix;
        this.courseNumber = courseNumber;
        this.title = title;
        this.crn = crn;
        this.units = units;
        this.courseLengthWeeks = courseLengthWeeks;
    }

    public String getSectionNumber() {
        return sectionNumber;
    }

    public void setSectionNumber(String sectionNumber) {
        this.sectionNumber = sectionNumber;
    }

    public String getCoursePrefix() {
        return coursePrefix;
    }

    public void setCoursePrefix(String coursePrefix) {
        this.coursePrefix = coursePrefix;
    }

    public int getCourseNumber() {
        return courseNumber;
    }

    public void setCourseNumber(int courseNumber) {
        this.courseNumber = courseNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCrn() {
        return crn;
    }

    public void setCrn(String crn) {
        this.crn = crn;
    }

    public double getUnits() {
        return units;
    }

    public void setUnits(double units) {
        this.units = units;
    }

    public int getCourseLengthWeeks() {
        return courseLengthWeeks;
    }

    public void setCourseLengthWeeks(int courseLengthWeeks) {
        this.courseLengthWeeks = courseLengthWeeks;
    }

    public String getCourseCode() {
        return coursePrefix + " " + courseNumber;
    }

    public String getFullCourseName() {
        return getCourseCode() + " - " + title;
    }

    @Override
    public String toString() {
        return "Course{" +
                "sectionNumber='" + sectionNumber + '\'' +
                ", coursePrefix='" + coursePrefix + '\'' +
                ", courseNumber=" + courseNumber +
                ", title='" + title + '\'' +
                ", crn='" + crn + '\'' +
                ", units=" + units +
                ", courseLengthWeeks=" + courseLengthWeeks +
                '}';
    }
}