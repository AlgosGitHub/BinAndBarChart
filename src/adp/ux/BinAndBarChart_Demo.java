package adp.ux;

import adp.ux.purdy.PurdyWindow;

import java.awt.*;

public class BinAndBarChart_Demo {

    public static void main(String[] args) throws Exception {

        PurdyWindow.quickWindow("BinAndBarChart Demo", new BinAndBarChart() {{

            setBackground(new Color(66,66,66,66));

            setBarLabelsAndColors(new String[] {"Bronze", "Silver", "Gold", "Platinum"}, new Color[] {new Color(120,89,59), new Color(178,113,9), new Color(18, 178, 76), new Color(148, 25, 255)});

            //these values will all be percentages at first, but we'll have to scale them to pixels anyway

            addSet("",     new Integer[] { 90,  40, 20, 10});
            addSet("A",    new Integer[] { 80,  50, 20, 10});
            addSet("B",    new Integer[] { 40,  80, 20, 10});
            addSet("A+B",  new Integer[] { 10,  60, 20, 10});

        }});

    }

}
