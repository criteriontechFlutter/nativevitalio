class Vital {
    var uhid: String? = null
    var pmId = 0
    var vitalID = 0
    var vitalName: String? = null
    var vitalValue = 0.0
    var unit: String? = null
    var vitalDateTime: String? = null
    var userId = 0
    var rowId = 0
}

class VitalsResponse {
    var status = 0
    var message: String? = null
    var responseValue: List<Vital>? = null
}