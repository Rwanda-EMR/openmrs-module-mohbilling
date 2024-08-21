package org.openmrs.module.mohbilling.businesslogic;

public class SimpleData {

    private String name;
    private Integer age;
    private String studentNumber;

    // Constructors, getters, and setters

    public SimpleData(String name, Integer age, String studentNumber) {
        this.name = name;
        this.age = age;
        this.studentNumber = studentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getStudentNumber() {
        return studentNumber;
    }

    public void setStudentNumber(String studentNumber) {
        this.studentNumber = studentNumber;
    }
}