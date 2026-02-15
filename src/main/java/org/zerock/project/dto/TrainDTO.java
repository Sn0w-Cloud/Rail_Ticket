package org.zerock.project.dto;

public class TrainDTO {
    private Integer trainNo;
    private String trainGradeName;
    private String depPlaceName;
    private String depPlanTime;
    private String arrPlaceName;
    private String arrPlanTime;
    private int adultCharge;
    private String duration;

    public Integer getTrainNo() { return trainNo; }
    public void setTrainNo(Integer trainNo) { this.trainNo = trainNo; }
    public String getTrainGradeName() { return trainGradeName; }
    public void setTrainGradeName(String trainGradeName) { this.trainGradeName = trainGradeName; }
    public String getDepPlaceName() { return depPlaceName; }
    public void setDepPlaceName(String depPlaceName) { this.depPlaceName = depPlaceName; }
    public String getDepPlanTime() { return depPlanTime; }
    public void setDepPlanTime(String depPlanTime) { this.depPlanTime = depPlanTime; }
    public String getArrPlaceName() { return arrPlaceName; }
    public void setArrPlaceName(String arrPlaceName) { this.arrPlaceName = arrPlaceName; }
    public String getArrPlanTime() { return arrPlanTime; }
    public void setArrPlanTime(String arrPlanTime) { this.arrPlanTime = arrPlanTime; }
    public int getAdultCharge() { return adultCharge; }
    public void setAdultCharge(int adultCharge) { this.adultCharge = adultCharge; }
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

}

