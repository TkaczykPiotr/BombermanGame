package game;
/**
 * klasa zawierajace informacje o obiekcie bomb takie jak pozycja  bomby,
 * moc bomby w kazdym kierunki, wartosc logiczna o tym czy eksplodowala,
 * zmienne liczace do wybuchu
 */
public class Bomb {
    int x, y;
    int leftPower=1, rightPower=1,upPower=1,downPower=1;
    boolean exploded;
    int countToExplode, intervalToExplode = 4;


}
