import javax.swing.JOptionPane;
import java.util.ArrayList;
import java.util.Random;
import java.util.Arrays;
public class is15155528 {
    public static void main(String [] args){
        /*
         * Input Indexes;
         * [0] = Number of generations(G) -- 3.
         * [1] = population size(P) -- 5.
         * [2] = number of students(S) -- 5.
         * [3] = total number of modules(E) -- 24.
         * [4] = number of modules in course(C) -- 8.
         * [5] = number of exam sessions(D) -- 7.
         * [6] = crossover population(Cr) -- 20.
         * [7] = mutation probability(Mu) -- 10.
         */

        //taking in user inputs
        int [] inputs = input();


        //hardcoded inputs here for debugging
        //int[] inputs = {15,5,9,41,4,4,20,10};


        /* generating a timetable for each student entered, passing the no of students,
         * number of modules in the course and the number of modules in total.
         */
        int [][] studentTimeTable = generateStudentTimeTable(inputs[2],inputs[4],inputs[3]);

        //hardcoded timeTable here for debugging
        //int [][] studentTimeTable = {{13 ,6 ,9 ,5 },{16 ,6 ,5 ,7},{4 ,15 ,1,10},{3,1,10,7},{9, 1, 13, 11}};


        for(int i = 0; i < studentTimeTable.length; i++){
            System.out.print("Student "+(i+1)+": ");
            for(int j = 0; j < studentTimeTable[i].length; j++){
                System.out.print(studentTimeTable[i][j]+" ");
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Hit Enter to continue.");
        try {
            System.in.read();
        }catch(Exception e){
            e.printStackTrace();
        }

        //now generating the ordering
        //reusing the student timetable to produce a population of orderings.

        int [][] population;
        /* After interim submission Farshad said I was marked down due to orderings
         * being identical in the population. This do - while loop stops this.
         */
        do{
            population = generateStudentTimeTable(inputs[1],inputs[3],inputs[3]);
        }while(!validatePopulation(population));


        //hardcoded population here for debugging
       /* int [][] population = {{17,10,2,13,4,5,7,11,1,8,6,15,9,16,14,3,12},
        {10,13,9,8,12,1,7,17,6,16,3,4,14,11,5,2,15},
        {1,10,12,4,13,16,2,7,6,9,17,15,8,5,3,11,14},
        {10,16,7,12,6,11,8,17,9,5,3,15,2,13,14,4,1},
        {7,12,9,13,6,8,2,5,14,3,4,10,11,16,1,15,17}};*/

        //determining fitness of population
        int [] orderingFitnessScores = fitness(population,inputs[5],studentTimeTable);

        //print results of initial population
        System.out.println();
        System.out.println("Initial Population");
        System.out.println();
        for(int i = 0; i < population.length; i++ ){
            System.out.print("Ordering "+(i+1)+": ");
            for(int j = 0; j < population[i].length; j++){
                System.out.print(population[i][j]+" ");
            }
            System.out.print(": Fitness Cost: "+orderingFitnessScores[i]);
            System.out.println();
        }

        //declarations for GA techniques
        boolean [] repeatBlocker;
        int randomValue;
        int mutationIndex1,mutationIndex2;
        int techniqueOffset;
        int temp;
        int orderingToChoose1;
        int orderingToChoose2;
        int[] childTemp;
        int[] child1;
        int[] child2;
        Random r = new Random();
        //for loop running through each generation
        for (int i = 0; i < inputs[0]; i++) {
            population = selectionProcess(population, orderingFitnessScores);
            /*As per project spec we can only apply one GA technique per ordering every generation.
             * This function resets the boolean array we use to check each ordering hasn't been worked on before.
             */
            repeatBlocker = initialiseRepeatBlocker(population.length);
            /*When crossover technique is used we change two orderings so we need to decrement the array by one loop to
             * avoid any infinite loops in the do-while checkers.
             */
            techniqueOffset = 0;
            //running through each ordering in the
            for (int j = 0; j < population.length - techniqueOffset; j++) {
                //check if there is anymore orderings that haven't had a GA technique applied to them.
                if (isRepeatBlockerFull(repeatBlocker)) {
                    //pick a number between 0-99;
                    randomValue = r.nextInt(100) + 1;
                    /* We are applying 3 different GA techniques so we must designate zones on the percentage scale
                     * for each of these to be fairly picked. the first zone is designated to the probability of
                     * mutation.
                     *
                     * Further down in the else block we have the chance of crossover so we check that the number
                     * generated is greater than the upper limit of mutation and is below the upper limit of the
                     * chance of mutation + crossover avoiding any overlap.
                     *
                     * other wise we can say that the technique is reproduction and we simply index that index
                     * on the repeatBlocker variable.
                     */
                    if (randomValue < inputs[7]) {
                        //generate a random index of the ordering that hasn't changed already.
                        do {
                            orderingToChoose1 = r.nextInt(population.length);
                        } while (repeatBlocker[orderingToChoose1] == true);
                        repeatBlocker[orderingToChoose1] = true;
                        //find a random index of the ordering selected to be swapped
                        mutationIndex1 = r.nextInt(population[orderingToChoose1].length);
                        //generate another index that isn't the first index selected for the swap
                        do {
                            mutationIndex2 = r.nextInt(population[orderingToChoose1].length);
                        } while (mutationIndex1 == mutationIndex2);
                        //commence swap
                        temp = population[orderingToChoose1][mutationIndex1];
                        population[orderingToChoose1][mutationIndex1] = population[orderingToChoose1][mutationIndex2];
                        population[orderingToChoose1][mutationIndex2] = temp;

                    } else if (randomValue < (inputs[6] + inputs[7]) && randomValue > inputs[7]) {
                        //crossover has been randomly selected.
                        //find a random ordering
                        do {
                            orderingToChoose1 = r.nextInt(population.length);
                        } while (repeatBlocker[orderingToChoose1] == true);
                        repeatBlocker[orderingToChoose1] = true;
                        //check to see if we have another ordering to crossover with. If not we go around again.
                        if (isRepeatBlockerFull(repeatBlocker)) {
                            //choose another random ordering to crossover with
                            do {
                                orderingToChoose2 = r.nextInt(population.length);
                            } while (repeatBlocker[orderingToChoose2] == true);
                            repeatBlocker[orderingToChoose2] = true;
                            //since we are modifying two orderings here we have to decrement the limit of the for loop.
                            techniqueOffset++;
                            //initialising the children
                            childTemp = new int[population[orderingToChoose1].length];
                            child1 = population[orderingToChoose1];
                            child2 = population[orderingToChoose2];
                            //choosing a random range between sizes 2 and the size of (E - 2)
                            int cutPointRange = r.nextInt(population[orderingToChoose1].length - 2) + 2;
                            int cutPointStart;
                            //randomly select a cutting start point that won't cause out of bounds exceptions
                            do {
                                cutPointStart = r.nextInt(population[orderingToChoose1].length);
                            } while (cutPointStart > (population[orderingToChoose1].length - cutPointRange));
                            //running the swap process for the arrays
                            for (int k = 0; k < 2; k++) {
                                for (int l = cutPointStart; l < (cutPointStart + cutPointRange); l++) {
                                    if (k == 0) {
                                        childTemp[l] = child1[l];
                                    } else {
                                        child1[l] = child2[l];
                                        child2[l] = childTemp[l];
                                    }
                                }
                            }
                            //now updating the orderings with the crossover children.
                            population[orderingToChoose1] = child1;
                            population[orderingToChoose2] = child2;
                        } else {
                            /*This triggers if we couldn't find a valid second ordering to apply the crossover too
                             * so we get the loop to run again and it should fall into reproduction or mutation then.
                             */
                            repeatBlocker[orderingToChoose1] = false;
                            j--;
                        }
                    } else {
                        /*if none of the other techniques are selected we assume we go for reproduction which wont change
                         * any orderings but it will mark them as being affected by a technique.
                         */
                        do {
                            orderingToChoose1 = r.nextInt(population.length);
                        } while (repeatBlocker[orderingToChoose1] == true);
                        repeatBlocker[orderingToChoose1] = true;
                    }

                }
            }
            //generate the fitness score of the generation.
            orderingFitnessScores = fitness(population, inputs[5], studentTimeTable);
            //output the data formatted.
            printGeneration(i + 1, population, inputs[5], orderingFitnessScores);
        }

    }
    private static boolean isRepeatBlockerFull(boolean [] repeatBlocker){
        //This function returns a boolean value to say whether we have orderings we haven't applied GA techniques to yet.
        for(int i = 0; i < repeatBlocker.length; i++){
            if(repeatBlocker[i] == false){
                return true;
            }
        }
        return false;
    }
    private static boolean [] initialiseRepeatBlocker(int numberOfOrderings){
        //This function resets the repeatBlocker array every generation.
        boolean [] repeatBlocker = new boolean[numberOfOrderings];
        for(int i = 0; i < numberOfOrderings; i++){
            repeatBlocker[i] = false;
        }
        return  repeatBlocker;
    }
    private static void printGeneration(int generation,int [][] population, int sessions, int [] orderingFitnessScores){
        //this function prints a formatted output to the user of each generation
        int bestOrderingValue = orderingFitnessScores[0];
        int bestIndex =0;
        //get the best ordering in the generation
        for(int i =0; i < orderingFitnessScores.length; i++){
            if(bestOrderingValue > orderingFitnessScores[i]){
                bestOrderingValue = orderingFitnessScores[i];
                  bestIndex = i;
            }
        }
        int [][] examSessions = new int[sessions+1][(population[bestIndex].length/sessions)];
        int sessionCount =0,indexCount =0;
        System.out.println("-------------------------------------------------------");
        System.out.println("Generation "+generation);
        System.out.println();
        System.out.print("Ordering: ");
        //formatting each exam session as specified in the project specification
        for(int i = 0; i < population[bestIndex].length; i++){
            System.out.print(population[bestIndex][i]+" ");
            if(i!=0 && i % examSessions[0].length == 0){
                sessionCount++;
            }
            if(indexCount > (population[bestIndex].length/sessions)-1){
                indexCount =0;
            }
            //System.out.println(examSessions.length+" : "+sessionCount);
            if(sessionCount == examSessions.length){
                sessionCount--;
            }
            examSessions[sessionCount][indexCount] = population[bestIndex][i];
            indexCount++;
        }
        System.out.println();
        System.out.println();
        //fill excess slots with zeros.
        while(indexCount < (population[bestIndex].length/sessions)-1){
            examSessions[sessionCount][indexCount] = 0;
            indexCount++;
        }
        for(int i = 0; i <= sessions; i++){
            System.out.print("Session "+(i+1)+"\t");
        }
        System.out.println();
        String formatter;
        //printing formatted data
        for(int i = 0; i < examSessions[0].length; i++){
            for(int j =0; j <=  sessions; j++){
                formatter = "Session "+j;
                //print out so its all inline
                if(examSessions[j][i] != 0) {
                    System.out.print(examSessions[j][i] + String.format("%" + formatter.length() + "s", "") + "\t");
                }else{
                    System.out.print(" " + String.format("%" + formatter.length() + "s", "") + "\t");
                }
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("Fitness cost: "+bestOrderingValue);
    }
    private static int [][] selectionProcess(int [][] population, int [] orderingFitnessScores){
        /* This function replaces the worst performing orderings of a generation with a copy
         * of the best performing orderings.
         */
        ArrayList<ArrayList<Integer>> s1 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> s2 = new ArrayList<>();
        ArrayList<ArrayList<Integer>> s3 = new ArrayList<>();
        for (int i=1; i<population.length; ++i) {
            int[] key = population[i];
            int fitnessKey = orderingFitnessScores[i];
            int j = i-1;

            /* Move elements of arr[0..i-1], that are
               greater than key, to one position ahead
               of their current position */
            while (j>=0 && orderingFitnessScores[j] > fitnessKey)
            {
                orderingFitnessScores[j+1] = orderingFitnessScores[j];
                population[j+1] = population[j];
                j = j-1;
            }
            population[j+1] = key;
            orderingFitnessScores[j+1] = fitnessKey;
        }
        //divide up orderings into best,average and worst
        double dividedPopulation = population.length/3;
        int count = 0;
        //now populating the Arraylists with the divided orderings.
        for(int i =0; i < 3; i++){
            for(int j = 0; j <= dividedPopulation && count < population.length; j++){
                ArrayList<Integer> innerList = new ArrayList<>();
                for(int k =0; k < population[count].length; k++) {
                    innerList.add(population[count][k]);
                }
                count++;
                if(i == 0){
                    s1.add(innerList);
                }else if(i==1){
                    s2.add(innerList);
                }else {
                    s3.add(innerList);
                }
            }

        }

        s3 = s1;
        //generate a new population
        int [][] newPopulation = new int[s1.size()+s2.size()+s3.size()][population[0].length];
        ArrayList<ArrayList<Integer>> temp = s1;
        temp.addAll(s2);
        temp.addAll(s3);
        System.out.println();
        //set then values of this population
        for(int i = 0; i < newPopulation.length; i++){
            for(int j =0; j < newPopulation[i].length; j++){
                newPopulation[i][j] = temp.get(i).get(j);
            }
        }
        return newPopulation;
    }
    private static boolean validatePopulation(int [][] population){
        for(int i = 0; i < population.length; i++){
            for(int j = 0; j < population.length; j++){
                if(Arrays.equals(population[i], population[j]) && i != j){
                    return false;
                }
            }
        }
        return true;
    }
    private static int [] fitness(int [][] population, int sessions, int [][] studentTimeTable){
        int count;
        int [] fitnessOfOrderings = new int [population.length];
        int overlaps;
        //going through each ordering in the population
        for(int i =0; i < population.length; i++){
            int [][] currentSessionsList;
            //dividing up the ordering into sessions, if there is a remainder we add an extra session to accommodate that.
            if(population[i].length%sessions != 0){
                currentSessionsList = new int[sessions+1][(population[i].length/sessions)];
            }else{
                currentSessionsList = new int[sessions][population[i].length/sessions];
            }
            count = 0;
            //populating the sessions
            for(int j = 0; j < currentSessionsList.length; j++){
                for(int k = 0; k < currentSessionsList[j].length; k++){
                    //if we have unfilled space in the session set them to -1
                    if(count > population[i].length-1){
                        currentSessionsList[j][k] = -1;
                    }else {
                        currentSessionsList[j][k] = population[i][count];
                    }
                    count++;
                }
                //after population of sessions we check and see if there are any overlaps with student Modules
                for(int m = 0; m < studentTimeTable.length; m++){
                    overlaps = 0;
                    //using a named block to break out of once we discover an overlap in the session.
                    search :
                    {
                        for (int n = 0; n < studentTimeTable[m].length; n++) {
                            for (int o = 0; o < currentSessionsList[j].length; o++) {
                                if (studentTimeTable[m][n] == currentSessionsList[j][o]) {
                                    overlaps++;
                                    /* One match is good, but more than one is an over lap
                                     * so we increment the fitness cost and break out to the next student.
                                     */
                                    if (overlaps > 1) {
                                        fitnessOfOrderings[i]++;
                                        break search;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return fitnessOfOrderings;
    }
    private static int [] [] generateStudentTimeTable(int numberOfStudents,int numberOfModulesInCourse, int totalNumberOfModules){
        int [][] timetable = new int[numberOfStudents][numberOfModulesInCourse];
        //now populating the timetable with random values
        for(int i = 0; i < timetable.length; i++){
            timetable[i] = generateArrayOfModules(timetable[i].length, totalNumberOfModules);
        }
        return timetable;
    }
    private static int [] generateArrayOfModules(int numberOfModulesInArray,int totalNumberOfModules){
        int [] arrayOfModules = new int[numberOfModulesInArray];
        Random rand = new Random();
        //now populating the array with random values
        int randomModule;
        for(int i = 0; i < numberOfModulesInArray; i++){
            //generate a number that isn't already in the array
            do {
                randomModule = rand.nextInt(totalNumberOfModules + 1);
            }while(!checkForDuplicates(arrayOfModules,randomModule));
            arrayOfModules[i] = randomModule;
        }
        return arrayOfModules;
    }
    private static boolean checkForDuplicates(int [] arrayToBeChecked, int newNumber){
        for(int i = 0; i < arrayToBeChecked.length; i++){
            if(arrayToBeChecked[i] == newNumber){
                return  false;
            }
        }
        return true;
    }
    private static int [] input(){
        //setting the text for the pop ups the user will see
        String [] inputPrompts = {"the number of generations [+ int]","the population size [+ int]","the number of students [+ int]","the total number of modules [+ int]",
                "the number of modules in the course [+ int][modules in course <= total number of modules]","the number of exam sessions/days [+ int]", "the crossover probability in the range [0-100]","the mutation probability [0-100]"};
        int [] validInput = new int[inputPrompts.length];
        //running through each input for the user
        for(int i = 0; i < inputPrompts.length; i++){
            String input = JOptionPane.showInputDialog(null,"Please enter "+inputPrompts[i]+".");
            boolean valid = true;
            do {
                //if validation failed show them another pop up to rectify input
                if(!valid){
                    input = JOptionPane.showInputDialog(null,"Error!: Invalid input! - Please enter "+inputPrompts[i]+".");
                }
                if (i < 6) {
                    valid = evaluatePositiveInteger(input);
                    //check to see that the number of modules in the course is less than the total number of modules
                    if(i == 4 && valid){
                        if(validInput[3] <= Integer.parseInt(input)){
                            valid = false;
                        }
                    }
                }else {
                    //last two inputs require a integer between 0 - 100
                    valid = evaluateIntegerBetweenTwoValues(0, 100, input);
                }
            }while(!valid);
            validInput[i] = Integer.parseInt(input);
        }
        return validInput;
    }
    private static boolean evaluatePositiveInteger(String input){
        //assuming input is integer, if not catch block will return false
        try {
            int number = Integer.parseInt(input);
            //checking to see if the integer is positive
            if(number < 1){
                return false;
            }else {
                return true;
            }
        }catch(Exception e){
            return false;
        }
    }
    private static boolean evaluateIntegerBetweenTwoValues(int min, int max, String input){
        //assuming input is integer, if not catch block will return false
        try {
            int number = Integer.parseInt(input);
            //number is between the min max we provided inclusively
            if(number <= max && number >= min){
                return true;
            }else {
                return false;
            }
        }catch(Exception e){
            return false;
        }
    }
}
