package com.vendtech.app.ui.activity.home

import com.vendtech.app.models.profile.ResultProfile

class NavigationListModel(var status:String,var message:String,var result: MutableList<ResultNavigation>)


data class ResultNavigation(var assignUserModuleId : String, var modules: String )
