package com.example.vaccination.data;

import java.util.List;
import java.util.Locale;

public class Hospital extends BaseModel {
    private String speciality;
    private String time;
    private boolean isOpen24Hours;
    private List<Vaccine> vaccineList;
    private List<Request> requestList;
    private double stars;
    private double distanceFromUser = 0;
    private long requestCounter = 0;
    private long vaccinatedCounter = 0; //person vaccinated


    public Hospital(String speciality, String time, boolean isOpen24Hours, List<Vaccine> vaccineList, List<Request> requestList, double stars, double distanceFromUser, long requestCounter, long vaccinatedCounter) {
        this.speciality = speciality;
        this.time = time;
        this.isOpen24Hours = isOpen24Hours;
        this.vaccineList = vaccineList;
        this.requestList = requestList;
        this.stars = stars;
        this.distanceFromUser = distanceFromUser;
        this.requestCounter = requestCounter;
        this.vaccinatedCounter = vaccinatedCounter;
    }

    public Hospital() {
    }

    public double getDistanceFromUser() {
        return distanceFromUser;
    }

    public void setDistanceFromUser(double distanceFromUser) {
        this.distanceFromUser = distanceFromUser;
    }

    public String getSpeciality() {
        return speciality;
    }

    public void setSpeciality(String speciality) {
        this.speciality = speciality;
    }

    public List<Vaccine> getVaccineList() {
        return vaccineList;
    }

    public void setVaccineList(List<Vaccine> vaccineList) {
        this.vaccineList = vaccineList;
    }

    public List<Request> getRequestList() {
        return requestList;
    }

    public void setRequestList(List<Request> requestList) {
        this.requestList = requestList;
    }

    public String getSingleTime() {
        return time.split("\n")[0];
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isOpen24Hours() {
        return isOpen24Hours;
    }

    public void setOpen24Hours(boolean open24Hours) {
        isOpen24Hours = open24Hours;
    }

    public double getStars() {
        return stars;
    }

    public void setStars(double stars) {
        this.stars = stars;
    }

    public long getRequestCounter() {
        return requestCounter;
    }

    public void setRequestCounter(long requestCounter) {
        this.requestCounter = requestCounter;
    }

    public long getVaccinatedCounter() {
        return vaccinatedCounter;
    }

    public void setVaccinatedCounter(long vaccinatedCounter) {
        this.vaccinatedCounter = vaccinatedCounter;
    }

    public void updateRequestCounter(boolean increment){
        if (increment){
            requestCounter++;
        }
        else
            requestCounter--;
    }


    public void updateRecordCounter(boolean increment){
        if (increment){
            vaccinatedCounter++;
        }
        else
            vaccinatedCounter--;
    }

    public String getFormattedDistance() {
        if (distanceFromUser > 1)
            return String.format(new Locale("en", "IN"), "%.2f Km", distanceFromUser);
        else
            return String.format(new Locale("en", "IN"), "%d M", (int) distanceFromUser);
    }

    @Override
    public String toString() {
        return "Hospital{" +
                "speciality='" + speciality + '\'' +
                ", vaccineList=" + vaccineList +
                ", requestList=" + requestList +
                '}';
    }
}
