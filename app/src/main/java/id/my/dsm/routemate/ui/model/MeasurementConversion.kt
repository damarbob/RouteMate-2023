package id.my.dsm.routemate.ui.model

import cern.jet.math.Functions

class MeasurementConversion {

    companion object {

        fun metersToKilometers(meter: Double): Double {
            return Functions.round(0.1).apply(meter / 1000)
        }
        fun metersToMiles(meter: Double): Double {
            return Functions.round(0.1).apply(metersToKilometers(meter) * 0.6213711922)
        }

    }

}