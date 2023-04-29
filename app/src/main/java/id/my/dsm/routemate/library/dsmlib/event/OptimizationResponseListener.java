package id.my.dsm.routemate.library.dsmlib.event;

import java.util.List;

import id.my.dsm.routemate.library.dsmlib.model.Solution;

public interface OptimizationResponseListener {
        void onOptimizationSuccess(List<Solution> solutions);
        void onOptimizationFailed(OptimizationResponseError error);
}
