package ala.postie

enum FrequencyType {
  daily, weekly, monthly

  static FrequencyType getByName(String name){
    if(daily.name() == name) return daily
    if(weekly.name() == name) return weekly
    if(monthly.name() == name) return monthly
    return null

  }


  static list() {
    [daily, weekly, monthly]
  }
}
