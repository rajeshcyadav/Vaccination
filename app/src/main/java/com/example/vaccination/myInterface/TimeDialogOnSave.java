package com.example.vaccination.myInterface;

import java.util.List;


//callback when "save button is click in Time Dialog (dialog_doctor-timings.xml)"
public interface TimeDialogOnSave {
    void onSave(List<String> days,List<String> times);
}
