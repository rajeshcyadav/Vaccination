package com.example.vaccination.data;

import java.io.Serializable;

public class Vaccine implements Serializable {
    private String name;
    private String uid;
    private Long stock;
    private String disease;
    private int minimumAge;
    private String description;
    private String hospitalUid;
    private String hospitalName;

    public Vaccine(String name, String uid, Long stock, String disease, int minimumAge, String description) {
        this.name = name;
        this.uid = uid;
        this.stock = stock;
        this.disease = disease;
        this.minimumAge = minimumAge;
        this.description = description;
    }

    public Vaccine() {

    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getMinimumAge() {
        return minimumAge;
    }

    public String getMinimumAgeFormattedString(){
        if (minimumAge<0){
            return "For all Age group";
        }
        else{
            return minimumAge+" and Above";
        }
    }

    public void setMinimumAge(int minimumAge) {
        this.minimumAge = minimumAge;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Long getStock() {
        return stock;
    }

    public void setStock(Long stock) {
        this.stock = stock;
    }

    @Override
    public String toString() {
        return "Vaccine{" +
                "name='" + name + '\'' +
                ", uid='" + uid + '\'' +
                ", stock=" + stock +
                '}';
    }

    public String getDisease() {
        return disease;
    }

    public void setDisease(String disease) {
        this.disease = disease;
    }

    public String getHospitalUid() {
        return hospitalUid;
    }

    public void setHospitalUid(String hospitalUid) {
        this.hospitalUid = hospitalUid;
    }

    public String getHospitalName() {
        return hospitalName;
    }

    public void setHospitalName(String hospitalName) {
        this.hospitalName = hospitalName;
    }
}

