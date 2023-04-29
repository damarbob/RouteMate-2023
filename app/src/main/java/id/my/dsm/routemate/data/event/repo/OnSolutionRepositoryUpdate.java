package id.my.dsm.routemate.data.event.repo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

import id.my.dsm.routemate.library.dsmlib.model.MatrixElement;
import id.my.dsm.routemate.library.dsmlib.model.Solution;

public final class OnSolutionRepositoryUpdate extends OnRepositoryUpdate<MatrixElement> {

    private Solution solution;
    private ArrayList<Solution> solutions;

    public OnSolutionRepositoryUpdate(@NonNull Event event) {
        super(event);
    }

    public OnSolutionRepositoryUpdate(@NonNull Event event, Solution solution) {
        super(event);
        this.solution = solution;
    }

    public OnSolutionRepositoryUpdate(@NonNull Event event, @NonNull ArrayList<Solution> solutions) {
        super(event);
        this.solutions = solutions;
    }

    @NonNull
    @Override
    public Event getStatus() {
        return super.getStatus();
    }

    @Nullable
    public Solution getSolution() {
        return solution;
    }

    @Nullable
    public ArrayList<Solution> getSolutions() {
        return solutions;
    }

}
