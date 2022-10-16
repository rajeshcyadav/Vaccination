package com.example.vaccination.myutils;

public class Haversine {
    public static final double R = 6372.8; // In kilometers
    public static double getHaversine(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        lat1 = Math.toRadians(lat1);
        lat2 = Math.toRadians(lat2);

        double a = Math.pow(Math.sin(dLat / 2),2) + Math.pow(Math.sin(dLon / 2),2) * Math.cos(lat1) * Math.cos(lat2);
        double c = 2 * Math.asin(Math.sqrt(a));
        return R * c;
    }


    public static String getHaversine(String locationOne,String locationTwo)
    {
        String[] loc1=locationOne.split(",");
        String[] loc2=locationTwo.split(",");
        double[] latLong={Double.parseDouble(loc1[0]),
                Double.parseDouble(loc1[1]),
                Double.parseDouble(loc2[0]),
                Double.parseDouble(loc2[1]),};

        return String.valueOf(getHaversine(latLong[0],latLong[1],latLong[2],latLong[3]));

    }

    public static void main(String[] args) {
        System.out.println( getHaversine(10,10,10.1,10.1));
    }
}
