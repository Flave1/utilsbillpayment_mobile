package com.vendtech.app.models.authentications

class GetCitiesModel (var status:String,var message:String,var result:List<ResultCities>)

data class ResultCities(var cityId:String,var countryId:String,var name:String,var countryName:String)