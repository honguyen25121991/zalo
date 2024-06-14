package com.zalo.Spring_Zalo.request;

public class BillRequest {
   
    private String status;
    private int point;

    // Các phương thức getter và setter
    

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }
}