package org.example;

import org.chocosolver.solver.Model;
import org.chocosolver.solver.Solver;
import org.chocosolver.solver.constraints.extension.Tuples;
import org.chocosolver.solver.search.strategy.Search;
import org.chocosolver.solver.search.strategy.selectors.values.IntDomainMin;
import org.chocosolver.solver.search.strategy.selectors.variables.MaxRegret;
import org.chocosolver.solver.variables.IntVar;

public class App 
{
    public static void main( String[] args )
    {
        String cities[] = new String[] {"School", "Gym", "Planter's Farm" , "Movies" , "Snell's Farm"};
        int numberOfCities = cities.length;
        int max = 2000;

        // matrix of distances
        int[][] distancesBetweenCities = new int[][] {
                { 0, 10, -1, 10, -1},
                { 10, 0, 8, 15, -1},
                { -1, 8, 0, 12, 20},
                { 10, 15, 12, 0, 7},
                { -1, -1, 20, 7, 0}
        };

        // Creation of the Model
        Model model = new Model("Traveling Salesman");

        // Defining the VARIABLES
        IntVar[] successiveCity = model.intVarArray("Successive City", numberOfCities, 0, numberOfCities - 1);
        // For each city, the distance to the succ visited one
        IntVar[] distanceBetweenVisitedSuccessiveCity = model.intVarArray("Distance Between Visited Successive City", numberOfCities, 0, max);
        // Total distance of the route
        IntVar totalDistance = model.intVar("Total distance", 0, max * numberOfCities);

        // Defining the CONSTRAINTS
        for (int i = 0; i < numberOfCities; i++) {
            Tuples tuples = new Tuples(true);
            for (int j = 0; j < numberOfCities; j++) {
                if (j != i)
                    tuples.add(j, distancesBetweenCities[i][j]);
            }
            model.table(successiveCity[i], distanceBetweenVisitedSuccessiveCity[i], tuples).post();
        }
        // Defining a single circuit of size number of cities, visiting all cities
        model.subCircuit(successiveCity, 0, model.intVar(numberOfCities)).post();
        // Defining the total distance
        model.sum(distanceBetweenVisitedSuccessiveCity, "=", totalDistance).post();

        // Set the Objective of the Model. Minimizing the total distance
        model.setObjective(Model.MINIMIZE, totalDistance);
        Solver solver = model.getSolver();
        solver.setSearch(
                Search.intVarSearch(
                        new MaxRegret(),
                        new IntDomainMin(),
                        distanceBetweenVisitedSuccessiveCity));
        // Display the Statistics
        solver.showStatistics();

        // Solve the Model and Display the Path
        while (solver.solve()) {
            System.out.println("*************************************");
            int current = 0;
            String currentCity = cities[current];
            System.out.printf("%s ", currentCity);
            for (int j = 0; j < numberOfCities; j++) {
                int auxCurrent = successiveCity[current].getValue();
                currentCity = cities[auxCurrent];
                System.out.printf("-> %s " , currentCity);
                current = successiveCity[current].getValue();
            }
            System.out.printf("\nTotal distance = %d\n", totalDistance.getValue());
            System.out.println("*************************************");
        }
        System.out.println("*************************************");
    }
}
