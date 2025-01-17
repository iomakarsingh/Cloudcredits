package com.library.models;

import java.util.Date;

public class Member {
    private int memberId;
    private String name;
    private String email;
    private String phone;
    private Date joinDate;

    // Constructor for new members
    public Member(String name, String email, String phone, Date joinDate) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.joinDate = joinDate;
    }

    // Constructor for database retrieval
    public Member(int memberId, String name, String email, String phone, Date joinDate) {
        this(name, email, phone, joinDate);  // Call the first constructor
        this.memberId = memberId;
    }
   
    // Getters and setters
    public int getMemberId() { return memberId; }
    public void setMemberId(int memberId) { this.memberId = memberId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public Date getJoinDate() { return joinDate; }
} 