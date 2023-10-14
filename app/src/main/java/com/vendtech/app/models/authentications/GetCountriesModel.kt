package com.vendtech.app.models.authentications

class GetCountriesModel (var status: String,var message:String,var result:List<ResultCountries>)

class ResultCountries(var countryId:String,var name:String)