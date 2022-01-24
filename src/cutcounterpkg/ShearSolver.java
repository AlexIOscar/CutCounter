package cutcounterpkg;

import java.util.ArrayList;
import java.util.List;

public class ShearSolver {

    public static boolean debugMode = false;

    public static float angAccuracy = 1f;
    //TODO в класс нужно добавить метод, который помимо базиса позволяет получать информацию о
    // группах ребер, объединенных признаком взаимной параллельности или перпендикулярности

    /* 1 класс деталей - прямоугольники, базис = 4
     * 2 класс - трапеция с одной косой стороной, базис = 3
     * 3 класс - параллелограмм, базис равен 2, есть 2 группы параллельных ребер
     * 4 класс - трапеция с непараллельными боковыми сторонами, или опирающийся на прямой угол
     * N-угольник, базис 2, одна группа параллельных или перпендикулярных ребер
     * 5 класс - все остальное */

    //legacy
    public static int getBasis(Piece piece) {
        // этот метод вычисляет базис в том его терминологическом смысле, в котором он введен в
        // ТЗ. В контексте гильотины, необходимо проверить многоугольник на выпуклость ДО
        // подсчета базиса, поскольку в невыпуклом многоугольнике базис может быть сколь угодно
        // велик, в выпуклом же он не может превысить 4. В расчет базиса передается только
        // наружный контур.

        // -1 будем считать ошибкой расчета
        //если список углов контура равен нулю, возвращаем ошибку
        if (piece.cornerList.size() < 1) return -1;

        //счетчик размера базы. Ее минимальное значение =1 заносим сразу
        int counter = 1;

        // создаем контейнер списка линий
        List<Line> lineSet = new ArrayList<>();


        // заполняем его линиями
        for (int i = 0; i < piece.cornerList.size() - 1; i++) {
            try {
                lineSet.add(new Line(piece.cornerList.get(i), piece.cornerList.get(i + 1)));
            } catch (WrongLineExeption wle) {
                System.out.println(wle.getMessage());
            }
        }

        // и еще одна, замыкающая контур с последней на первую точку
        try {
            lineSet.add(new Line(piece.cornerList.get(piece.cornerList.size() - 1), piece.cornerList.get(0)));
        } catch (WrongLineExeption wle) {
            System.out.println(wle.getMessage());
        }

        /*
         фиксируемся на одной линии, и последовательно проверяем ее угол с каждой последующей. Если он без остатка
         делится на 90, с точностью до angAccuracy, значит, условие параллельности/перпендикулярности выполняется.
         В этом случае инкрементируем значение locCounter. Каждая линия, проверка которой дала
         положительный результат,
         может быть сразу удалена, так как она уже не сможет войти в другой "базис". По той же причине, после каждой
         итерации удаляем и текущую линию (отношение взаимной перпендикулярности/параллельности коммутативно - если
         тест не дал положительного результата в первый же проход, перестановка операндов проверки не даст его и при
         последующих)
         */

        // перебираем линии парами, пока в наборе есть хотя бы 2 стороны
        while (lineSet.size() > 1) {

            //создаем локальный счетчик, который будет фиксировать количество хитов (попаданий в группу) в этом проходе
            int locCounter = 1;
            float lastAng = 0;

            //массив хитов
            boolean[] isHit = new boolean[lineSet.size()];

            //первый отрезок сразу же записываем как хит
            isHit[0] = true;

            //перебираем все элементы массива и вычисляем углы по отношению к нулевому
            for (int i = 1; i < lineSet.size(); i++) {

                float angle = Line.getAngle(lineSet.get(0), lineSet.get(i));

                if (Math.abs(angle - lastAng) < angAccuracy) {
                    System.out.println("найдено вырожденное ребро!");
                    continue;
                }

                lastAng = angle;

                if (debugMode) {
                    System.out.println(angle);
                }

                //если угол меньше заданного интервала точности
                if (angle % 90 < angAccuracy) {
                    // инкрементируем локальный счетчик
                    locCounter++;
                    // и регистрируем хит
                    isHit[i] = true;
                }
            }

            if (debugMode) {
                System.out.println("____");
            }

            //если накопленное в этом проходе значение базы больше, чем общий counter, то фиксируем его в counter
            if (locCounter > counter) {
                counter = locCounter;
            }

            // удаляем все хиты, чтоб подготовить массив к следующему проходу. дизайн ArrayList в
            // Java.util таков, что удалять надо с конца, иначе все индексы будут каждый раз
            // съезжать - массив всякий раз двигается на освободившееся место. Решение через
            // обычные массивы было бы лишено этой особенности, зато приобрело бы другие проблемы.

            for (int i = isHit.length - 1; i >= 0; i--) {
                if (isHit[i]) {
                    lineSet.remove(i);
                }
            }
        }
        return counter;
    }

    public static int getCutsIII(Piece piece) {
        // этот метод вычисляет базис в том его терминологическом смысле, в котором он введен в
        // ТЗ. В контексте гильотины, необходимо проверить многоугольник на выпуклость ДО
        // подсчета базиса, поскольку в невыпуклом многоугольнике базис может быть сколь угодно
        // велик, в выпуклом же он не может превысить 4. В расчет базиса передается только
        // наружный контур.

        // -1 будем считать ошибкой расчета
        //если список углов контура равен нулю, возвращаем ошибку
        if (piece.cornerList.size() < 1) return -1;

        //счетчик числа сторон
        int edgeCounter = 1;
        //специальный переключатель, который сбрасывается при первом проходе цикла по ребрам, и
        // не дает инкрементировать счетчик в последующих проходах по тем же ребрам
        boolean blockEdgeCounter = false;
        //счетчик размера базы. Ее минимальное значение =1 заносим сразу
        int basisCounter = 1;

        // создаем контейнер списка линий
        List<Line> lineSet = new ArrayList<>();

        // заполняем его линиями
        for (int i = 0; i < piece.cornerList.size() - 1; i++) {
            try {
                lineSet.add(new Line(piece.cornerList.get(i), piece.cornerList.get(i + 1)));
            } catch (WrongLineExeption wle) {
                System.out.println(wle.getMessage());
            }
        }

        // и еще одна, замыкающая контур с последней на первую точку
        try {
            lineSet.add(new Line(piece.cornerList.get(piece.cornerList.size() - 1), piece.cornerList.get(0)));
        } catch (WrongLineExeption wle) {
            System.out.println(wle.getMessage());
        }

        /*
         фиксируемся на одной линии, и последовательно проверяем ее угол с каждой последующей. Если он без остатка
         делится на 90, с точностью до angAccuracy, значит, условие параллельности/перпендикулярности выполняется.
         В этом случае инкрементируем значение locCounter. Каждая линия, проверка которой дала
         положительный результат,
         может быть сразу удалена, так как она уже не сможет войти в другой "базис". По той же причине, после каждой
         итерации удаляем и текущую линию (отношение взаимной перпендикулярности/параллельности коммутативно - если
         тест не дал положительного результата в первый же проход, перестановка операндов проверки не даст его и при
         последующих)
         */

        // перебираем линии парами, пока в наборе есть хотя бы 2 стороны
        while (lineSet.size() > 1) {

            //создаем локальный счетчик, который будет фиксировать количество хитов (попаданий в группу) в этом проходе
            int locCounter = 1;
            float lastAng = 0;

            //массив хитов
            boolean[] isHit = new boolean[lineSet.size()];

            //первый отрезок сразу же записываем как хит
            isHit[0] = true;

            //перебираем все элементы массива и вычисляем углы по отношению к нулевому
            for (int i = 1; i < lineSet.size(); i++) {

                float angle = Line.getAngle(lineSet.get(0), lineSet.get(i));

                if (Math.abs(angle - lastAng) < angAccuracy) {
                    System.out.println("найдено вырожденное ребро!");
                    continue;
                }

                if (!blockEdgeCounter) {
                    edgeCounter++;
                }

                lastAng = angle;

                if (debugMode) {
                    System.out.println(angle);
                }

                //если угол меньше заданного интервала точности
                if (angle % 90 < angAccuracy) {
                    // инкрементируем локальный счетчик
                    locCounter++;
                    // и регистрируем хит
                    isHit[i] = true;
                }
            }

            if (debugMode) {
                System.out.println("____");
            }

            //блокируем счетчик ребер
            blockEdgeCounter = true;
            if (debugMode) {
                System.out.println("ребер всего:" + edgeCounter);
            }

            //если накопленное в этом проходе значение базы больше, чем общий basisCounter, то фиксируем его в basisCounter
            if (locCounter > basisCounter) {
                basisCounter = locCounter;
            }

            // удаляем все хиты, чтоб подготовить массив к следующему проходу. дизайн ArrayList в
            // Java.util таков, что удалять надо с конца, иначе все индексы будут каждый раз
            // съезжать - массив всякий раз двигается на освободившееся место. Решение через
            // обычные массивы было бы лишено этой особенности, зато приобрело бы другие проблемы.

            for (int i = isHit.length - 1; i >= 0; i--) {
                if (isHit[i]) {
                    lineSet.remove(i);
                }
            }
        }

        if (debugMode) {
            System.out.println("базис: " + basisCounter);
        }

        return edgeCounter - basisCounter;
    }

    //запускатор-тестировщик
    public static void main(String[] args) {

        debugMode = true;

        Piece piece = new Piece();
        //комплект для тестирования 1
        piece.addCorner(new Line.Point(0f, 0f));
        piece.addCorner(new Line.Point(0f, 0.5f));
        piece.addCorner(new Line.Point(0f, 1.01f));
        piece.addCorner(new Line.Point(0f, 1.01f));
        piece.addCorner(new Line.Point(0f, 1.01f));
        piece.addCorner(new Line.Point(1f, 1.5f));
        piece.addCorner(new Line.Point(2f, 1f));
        piece.addCorner(new Line.Point(1f, 0f));

        System.out.println("рубов III типа:" + getCutsIII(piece));
    }
}