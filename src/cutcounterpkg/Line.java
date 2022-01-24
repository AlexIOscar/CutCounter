package cutcounterpkg;

public class Line {

    // line common expression: a*x + b*y + c = 0
    private final float a_coeff;
    private final float b_coeff;
    // "с" является частью "правильной" полной модели прямой, но здесь избыточно: не влияет на угол между прямыми
    //private final float c_coeff;

    public Line(Point p1, Point p2) throws WrongLineExeption {

        if ((p1.getY_coord() == p2.getY_coord()) & (p2.getX_coord() == p1.getX_coord())) {
            throw new WrongLineExeption("Вырожденная линия: совпадающие точки");
        }

        a_coeff = p1.getY_coord() - p2.getY_coord();
        b_coeff = p2.getX_coord() - p1.getX_coord();
        //c_coeff = p1.getX_coord() * p2.getY_coord() - p2.getX_coord() * p1.getY_coord();
    }

    // вычисление угла данной прямой к другой прямой. угол от 0 до 180, при этом направление
    // отклонения не влияет на результат
    public float getAngle(Line anotherLine) {

        return (float) Math.toDegrees(Math.acos((a_coeff * anotherLine.a_coeff + b_coeff * anotherLine.b_coeff) /
                (Math.sqrt(Math.pow(a_coeff, 2) + Math.pow(b_coeff, 2)) *
                        Math.sqrt(Math.pow(anotherLine.a_coeff, 2) + Math.pow(anotherLine.b_coeff, 2)))));
    }

    // вычисление угла между двумя прямыми
    // Важно: если хотя бы одна линия вырождена в точку, то в этом методе произойдет division-by-zero. Конкретно в этой
    // программе невозможно создать вырожденную линию, поскольку в конструкторе в этом случае генерируется исключение
    // (это позволяет не проверять набор точек в классе CutCounterPkg.Solver, и не вычищать его, создание вырожденного сегмента
    // просто пропускается)
    // Какой-то способ защиты необходимо предусмотреть в любом случае, например, проверка точек перед конструированием
    // новой линии (что, в общем-то, не менее громоздко, чем try-catch)

    public static float getAngle(Line line1, Line line2) {
        return (float) Math.toDegrees(Math.acos((line1.a_coeff * line2.a_coeff + line1.b_coeff * line2.b_coeff) /
                (Math.sqrt(Math.pow(line1.a_coeff, 2) + Math.pow(line1.b_coeff, 2)) *
                        Math.sqrt(Math.pow(line2.a_coeff, 2) + Math.pow(line2.b_coeff, 2)))));
    }

    private static double angleOfRefVec(final double vecX, final double vecY) {
        return Math.toDegrees(Math.atan2(vecY, vecX));
    }

    public static double getFullAngle(Line line1, Line line2) {
        // преобразуем в векторы
        final double vecX1 = -line1.b_coeff;
        final double vecY1 = line1.a_coeff;
        final double vecX2 = -line2.b_coeff;
        final double vecY2 = line2.a_coeff;

        double angle = angleOfRefVec(vecX1, vecY1) - angleOfRefVec(vecX2, vecY2);
        if (angle > 180) angle -= 360;
        if (angle <= -180) angle += 360;
        return angle;
    }

    public static class Point {

        private final float x_coord;
        private final float y_coord;

        public float getX_coord() {
            return x_coord;
        }

        public float getY_coord() {
            return y_coord;
        }

        public Point(float x_coord, float y_coord) {
            this.x_coord = x_coord;
            this.y_coord = y_coord;
        }
    }
}