package com.vendtech.app.models.meter

import java.io.Serializable

class GetMetersModel (var status:String,var message:String,var result: MutableList<MeterListResults>)

data class MeterListResults(var userName:String,var createdAt:String,var userId:String,var meterId:String,var name:String,var number:String,var address:String,var meterMake:String,var isEdit:Boolean, var alias: String, var isVerified: Boolean, var platformDisabled: Boolean, var numberType: Number, var platformId: Number):Serializable