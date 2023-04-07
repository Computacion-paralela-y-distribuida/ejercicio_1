package co.edu.unal.paralela;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

//La clase implementa un método de suma de recíprocos
public final class ReciprocalArraySum {

    //Número de tareas
    public static int numOfTasks;
    
    //Constructor
    private ReciprocalArraySum() {
    }

     //@param input Arreglo de entrada
     //@return La suma de los recíprocos del arreglo de entrada
     //Método que calcula la suma de recíprocos
    protected static double seqArraySum(final double[] input) {
        double sum = 0;
        // Calcula la suma de los recíprocos de los elementos del arreglo
        for (int i = 0; i < input.length; i++) {
            sum += 1 / input[i];
        }

        return sum;
    }

     //@param nChunks El número de secciones (chunks) en el que dividirá el número de elementos
     //@param nElements El número de elementos para dividir en chunks
     //@return El tamaño por defecto de cada chunk (Cuantos elementos tendrá)
    private static int getChunkSize(final int nChunks, final int nElements) {
        // Función techo entera
        return (nElements + nChunks - 1) / nChunks;
    }

     //@param chunk #x
     //@param nChunks: Cantidad de chunks creados
     //@param nElements: Número de elementos del arreglo
     //@return El índice inclusivo donde inicia el chunk dentro del conjunto nElements
    private static int getChunkStartInclusive(final int chunk,
            final int nChunks, final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        return chunk * chunkSize;
    }

     //@param chunk #x
     //@param nChunks: Cantidad de chunks creados
     //@param nElements: Número de elementos del arreglo
     //@return El índice exclusivo donde termina el chunk, lo que es lo mismo 
     // al índice de inicio del siguiente chunk
    private static int getChunkEndExclusive(final int chunk, final int nChunks,
            final int nElements) {
        final int chunkSize = getChunkSize(nChunks, nElements);
        final int end = (chunk + 1) * chunkSize;
        if (end > nElements) {
            return nElements;
        } else {
            return end;
        }
    }


     //Este pedazo de clase puede ser completada para implementar el cuerpo de cada tarea creada para 
     // realizar la suma de los recíprocos del arreglo en paralelo.
    private static class ReciprocalArraySumTask extends RecursiveAction {
        //índice de inicio inclusivo
        private final int startIndexInclusive;
        //índice de finalización exclusivo
        private final int endIndexExclusive;
        //Arreglo de entrada para la suma de recíprocos.
        private final double[] input;
        //Valor producido por esta tarea.
        private double value;

        //Constructor.
        ReciprocalArraySumTask(final int setStartIndexInclusive,
                final int setEndIndexExclusive, final double[] setInput) {
            this.startIndexInclusive = setStartIndexInclusive;
            this.endIndexExclusive = setEndIndexExclusive;
            this.input = setInput;
        }

        //Retorno del valor producido por la tarea
        public double getValue() {
            return value;
        }
        
        //Uno de los métodos probablemete ya hechos por Jhon, paraleliza la tarea si el tamaño 
        // del arreglo es mayor a 2000
        @Override
        protected void compute() {
            if( endIndexExclusive - startIndexInclusive <= 2000){
                for (int i = startIndexInclusive; i < endIndexExclusive; ++i) {
                    value += (1 / input[i]);
                }
            }else{
                ReciprocalArraySumTask left = new ReciprocalArraySumTask(startIndexInclusive, (startIndexInclusive+endIndexExclusive)/2, input);
                ReciprocalArraySumTask right = new ReciprocalArraySumTask((startIndexInclusive+endIndexExclusive)/2,endIndexExclusive , input);
                left.fork();
                right.compute();
                left.join();
                value = left.getValue() + right.getValue();
            }

        }
    }


    /**
     * Para hacer: Modificar este método para calcular la misma suma de recíprocos como le realizada en
     * seqArraySum, pero utilizando dos tareas ejecutándose en paralelo dentro del framework ForkJoin de Java
     * Se puede asumir que el largo del arreglo de entrada 
     * es igualmente divisible por 2.
     *
     * @param input Arreglo de entrada
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parArraySum(final double[] input){
        assert input.length % 2 == 0;
        numOfTasks = 2;

        ReciprocalArraySumTask t = new ReciprocalArraySumTask(
                                                               0,
                                                                input.length,
                                                                input
                                                                );
        
        //Crea la psicina de hilos para gestionar de mejor manera el parelismo
        ForkJoinPool.commonPool().invoke(t);
        
        return t.getValue();
    }




    /**
     * Para hacer: extender el trabajo hecho para implementar parArraySum que permita utilizar un número establecido
     * de tareas para calcular la suma del arreglo recíproco. 
     * getChunkStartInclusive y getChunkEndExclusive pueden ser útiles para cacular 
     * el rango de elementos índice que pertenecen a cada sección/trozo (chunk).
     *
     * @param input Arreglo de entrada
     * @param numTasks El número de tareas para crear
     * @return La suma de los recíprocos del arreglo de entrada
     */
    protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
        numOfTasks = numTasks;
        double sum = 0;
        ForkJoinPool pool = new ForkJoinPool(numOfTasks);
        ReciprocalArraySumTask[] tasks = new ReciprocalArraySumTask[numOfTasks];
        for (int i = 0; i < numOfTasks; i++) {
            int start = getChunkStartInclusive(i, numOfTasks, input.length);
            int end = getChunkEndExclusive(i, numOfTasks, input.length);
            tasks[i] = new ReciprocalArraySumTask(start, end, input);
            pool.invoke(tasks[i]);
            tasks[i].getValue();
            sum += tasks[i].getValue();

        }
        return sum;
    }


}
