/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package adp.ux;

import adp.ux.purdy.PurdyWindow;
import adp.ux.purdy.components.ColorPanel;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.JFrame;

import static com.sun.java.accessibility.util.AWTEventMonitor.addMouseListener;

/**
 *
 * @author antho
 */
public class BinAndBarChart extends ColorPanel {

    {

        addMouseListener(new MouseAdapter() {
            
            @Override
            public void mouseClicked(MouseEvent e) {
                
                int mouseX = e.getX(), mouseY = e.getY();
                
                clickableAreas.forEach((name, area) -> { 
                
                    if(mouseX > area.x && mouseY > area.y && mouseX < area.x + area.width && mouseY < area.y + area.height) {
                        area.clicked = true;
                        barSetSelected(name);
                    } else 
                        area.clicked = false;
                
                });
                
                invalidate();
                repaint();
                
            }
            
        });
        
    }
    
    public void barSelected(String barName) {
        
    }
    
    public void barSetSelected(String binName) {
        // to be overwritten but not mandatory.
    }
    
    
    public Font textFont = new Font("default", Font.BOLD, 16);
    
    Color 
        textColor      =  Color.black,
        zeroLineColor  =  Color.lightGray;
    
        
    Map<String, Color> barColorsViaLabel = new LinkedHashMap();
    List<String> barLabels = new LinkedList();    
    
    public void setBarLabelsAndColors(String[] labels, Color[] colors) {
        
        for(int i = 0; i < labels.length; i++) {
            
            String barLabel = labels[i];
            
            barColorsViaLabel.put(barLabel, colors[i]);
            barLabels.add(barLabel);
            
        }
        
    }
        
    List<BarSet> barSets = new LinkedList();
    
    int lowestValue = 0, highestValue = 0;//zero is appropriate default here
        
    public void addSet(String setLabel, Integer[] barValues) {
        
        barSets.add(new BarSet(setLabel, new LinkedHashMap() {{
            
            for(int i = 0; i < barValues.length; i++) 
                put(barLabels.get(i), new Bar(barValues[i]));
            
        }}));
        
        //- compute some numbers for rendering
       
        for(BarSet barSet : barSets) { //determine lowest-underside-value, this doesn't need to be done every paint-cycle... move it to the addSet function
            for(Bar bar : barSet.barValuesViaLabels.values()) {
                lowestValue   =  Math.min(bar.barValue,  lowestValue);
                highestValue  =  Math.max(bar.barValue, highestValue);
            }
        }
        
    }
    
    //-
    
    int mostNewLines = 1;
        
    @Override
    protected void paintComponent(Graphics g) {
        
        super.paintComponent(g); 
        
        //todo: draw bar color keys w/ labels along top left-to-right, flat not stacked
                 
        int numSets    =  barSets.size(),
            winWidth   =  getWidth(),
            winHeight  =  getHeight(),//needed for scaling values. Height = Max Value, yZero = Zero. Pixel-Scale values accordingly
            minPxBetweenSets  =   5,
            minPxBarWidth     =  15,
            minPxBetweenBars  =   4,
            strHeight = g.getFontMetrics().getHeight(),
            strNewlineGap     =   6;
        
        
        int pxBetweenMidpoints = winWidth / (numSets+1),//
            pxSpaceForText = (strHeight*mostNewLines) + (strNewlineGap * mostNewLines-1),
            yZero =  (int) ((((double)(winHeight - pxSpaceForText)/(highestValue-lowestValue))*highestValue)),//40 is to allow room for text but we must actually track how many lines we're going to be accounting for and the height of each line
            running_X_midpoint = 0;
        
        int newMostNewLines = 0;
        
        if(barSets.size() == 1) {
            
            int x_midpoint = winWidth/2,
                pxWidth = winWidth - 10;
            
            BarSet barSet = barSets.get(0);
            
            
            int numBars = barSet.barValuesViaLabels.size(),
            spaceBetweenBarCenters = pxWidth / (numBars+1),
            barWidth = minPxBarWidth;

            double pixelsPerUnit = Math.max((double)(yZero - 10)/100, 1);

            int running_x_midpoint = (x_midpoint - (pxWidth/2));
            for(Entry<String, Bar> individualBar : barSet.barValuesViaLabels.entrySet()) {

                Bar b = individualBar.getValue();

                int thisBarCenter = running_x_midpoint+=spaceBetweenBarCenters;

                clickableAreas.compute(individualBar.getKey(), (String name, ClickableArea cArea) -> {

                    ClickableArea toReturn = cArea;

                    if(cArea == null)
                        toReturn = new ClickableArea(thisBarCenter - (barWidth/2), 0, barWidth, winHeight);
                    else 
                        toReturn.update(thisBarCenter - (barWidth/2), 0, barWidth, winHeight );

                    return toReturn;

                });
                
            }
            
            
            newMostNewLines = Math.max(newMostNewLines, barSet.setLabel.split("\\+").length);
            
            paintBarSet(barSet, false, (Graphics2D)g, x_midpoint, pxWidth, yZero, minPxBarWidth, minPxBetweenBars);
            
            
        } else 
        for(BarSet barSet : barSets) {
            
            int x_midpoint = running_X_midpoint+=pxBetweenMidpoints,
                pxWidth = pxBetweenMidpoints - (minPxBetweenSets*2),
                halfWidth = pxWidth/2;
                        
            clickableAreas.compute(barSet.setLabel, (String name, ClickableArea area) -> {
                
                ClickableArea toReturn = area;
                
                if(area == null)
                    toReturn = new ClickableArea(x_midpoint-halfWidth, yZero - highestValue, pxWidth, highestValue - lowestValue);
                else 
                    toReturn.update(x_midpoint-halfWidth, yZero - highestValue, pxWidth, highestValue - lowestValue);
                
                return toReturn;
                
            });
            
            newMostNewLines = Math.max(newMostNewLines, barSet.setLabel.split("\\+").length);
            
            paintBarSet(barSet, clickableAreas.get(barSet.setLabel).clicked, (Graphics2D)g, x_midpoint, pxWidth, yZero, minPxBarWidth, minPxBetweenBars);
            
        }
        
        if(mostNewLines != newMostNewLines) {
            mostNewLines = newMostNewLines;
            repaint();
        }
        
    }
    
    class ClickableArea {
        
        boolean clicked = false;
        
        int x, y, width, height;

        public ClickableArea(int x, int y, int width, int height) {
            update(x, y, width, height);
        }

        private void update(int x, int y, int width, int height) {            
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
        }
        
    }
    
    Map<String, ClickableArea> clickableAreas = new HashMap();
    
    void paintBarSet(BarSet barSet, boolean selected, Graphics2D g, int centerX, int pxWidth, int yZero, int pxBarWidth, int pxBetweenBars) {
        
        int numBars = barSet.barValuesViaLabels.size(),
            spaceBetweenBarCenters = pxWidth / (numBars+1),
            barWidth = pxBarWidth;
        
        double pixelsPerUnit = (double)(yZero - 10)/highestValue;//Math.max((double)(yZero - 10)/highestValue, 1);
            
        int running_x_midpoint = (centerX - (pxWidth/2));
        for(Entry<String, Bar> barViaLabel : barSet.barValuesViaLabels.entrySet()) {
            
            Bar b = barViaLabel.getValue();
                        
            int thisBarCenter = running_x_midpoint+=spaceBetweenBarCenters;
            
            g.setColor(barColorsViaLabel.get(barViaLabel.getKey()));
            g.fillRect(thisBarCenter - (barWidth/2), yZero, barWidth, (int) -(b.barValue * pixelsPerUnit));

            if(barSets.size() == 1) {
                //paint labels
                g.setFont(textFont);
                g.drawString(barViaLabel.getKey(), thisBarCenter - (barWidth/2), yZero+g.getFontMetrics().getHeight());
                
            }
        }
        
        g.setColor(zeroLineColor);
        int halfWidth = pxWidth/2;
        g.drawLine(centerX-halfWidth, yZero, centerX+halfWidth, yZero);

        g.setFont(textFont);
        
        g.setColor(selected ? Color.white : textColor);
        
        if(barSets.size() > 1) {
            String[] labels = barSet.setLabel.split("\\+"); // "+" is our line-break character that's also Folder Name Friendly.
            int stringHeight = g.getFontMetrics().getHeight();
            int yToPaint = (int) ((yZero + stringHeight) - (lowestValue*pixelsPerUnit));
            for(int strIter = 0; strIter<labels.length; strIter++ ) {
                String toPaint = labels[strIter];
                int stringWidth = g.getFontMetrics().stringWidth(toPaint);
                g.drawString(toPaint, centerX - (stringWidth/2), yToPaint);            
                yToPaint += stringHeight;
            }
        }
        // draw highlight
        
        if(selected) 
            g.drawRect((centerX-halfWidth)-4, (int)(yZero - (highestValue*pixelsPerUnit)) - 5, pxWidth+8, (int)((highestValue-lowestValue)*pixelsPerUnit) + 8);
        
    }
    
    
    //- private classes
    
    
    class Bar {
        
        final int 
            barValue;

        Bar(int barValue) {
            
            this.barValue      =  barValue;
            
        }
        
    }
    
    class BarSet {
        
        final String setLabel;
        final Map<String, Bar> barValuesViaLabels;

        BarSet(String setLabel, Map<String, Bar> barValuesViaLabels) {
            
            this.setLabel = setLabel;
            this.barValuesViaLabels = barValuesViaLabels;
            
        }

    }

}
