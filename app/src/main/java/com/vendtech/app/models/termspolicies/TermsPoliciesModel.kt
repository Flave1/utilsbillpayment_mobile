package com.vendtech.app.models.termspolicies

class TermsPoliciesModel (var status:String,var message:String,var result:ResultPolcies)

data class ResultPolcies(var termsHtml:String,var privacyPolicyHtml:String)