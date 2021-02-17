package com.j4velin.pedometer;

public class LeaderBoard {
    String Name;
    String Steps;
    String Time;
    String Email;

    public LeaderBoard() {
    }


    public String getName() {
        return Name;
    }

    public String getSteps() {
        return Steps;
    }

    public String getTime() {
        return Time;
    }

    public String getEmail() {
        return Email;
    }

    public LeaderBoard(String name, String steps, String time, String email) {
        Name = name;
        Steps = steps;
        Time = time;
        Email = email;
    }
}
