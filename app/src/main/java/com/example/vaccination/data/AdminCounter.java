package com.example.vaccination.data;

public class AdminCounter {
    private long REQUESTCOUNTER, USERCOUNTER, VALUE;

    public AdminCounter(long REQUESTCOUNTER, long USERCOUNTER, long value) {
        this.REQUESTCOUNTER = REQUESTCOUNTER;
        this.USERCOUNTER = USERCOUNTER;
        VALUE = value;
    }

    public AdminCounter() {
    }

    public long getREQUESTCOUNTER() {
        return REQUESTCOUNTER;
    }

    public void setREQUESTCOUNTER(long REQUESTCOUNTER) {
        this.REQUESTCOUNTER = REQUESTCOUNTER;
    }

    public long getUSERCOUNTER() {
        return USERCOUNTER;
    }

    public void setUSERCOUNTER(long USERCOUNTER) {
        this.USERCOUNTER = USERCOUNTER;
    }

    public long getVALUE() {
        return VALUE;
    }

    public void setVALUE(long VALUE) {
        this.VALUE = VALUE;
    }
}
