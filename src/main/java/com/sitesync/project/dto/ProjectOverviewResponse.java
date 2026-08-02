package com.sitesync.project.dto;

public class ProjectOverviewResponse {
    public int totalEnrolledWorkers;
    public int presentToday;

    public ProjectOverviewResponse(int totalEnrolledWorkers, int presentToday) {
        this.totalEnrolledWorkers = totalEnrolledWorkers;
        this.presentToday = presentToday;
    }
}