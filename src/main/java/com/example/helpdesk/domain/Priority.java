package com.example.helpdesk.domain;

public enum Priority {
    LOW,
    NORMAL,
    HIGH,
    URGENT;

    //return the next priority in the list
    public Priority next(){
        Priority[] values = values();
        int idx = ordinal();
        if(idx == values.length - 1){
            return this;
        }
        return values[idx+1];
    }
}


