package com.vendtech.app.models.authentications

class GetUserTypesModel(var status:String,var message:String,var result:Result)

data class Result(var result1:ArrayList<ResultUserTypes>,var result2:ArrayList<ResultUserTypes>)

data class ResultUserTypes(var disabled:String,var group:String,var selected:String,var text:String,var value:String)

