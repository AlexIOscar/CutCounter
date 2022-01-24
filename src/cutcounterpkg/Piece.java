package cutcounterpkg;

import java.util.ArrayList;
import java.util.List;

public class Piece {

    //важная особенность: в этом классе для корректной работы с Solver необходимо
    protected List<Line.Point> cornerList = new ArrayList<>();

    public void addCorner(Line.Point point){
        cornerList.add(point);
    } 
}
