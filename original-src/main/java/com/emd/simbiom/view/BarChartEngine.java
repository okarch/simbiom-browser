package com.emd.simbiom.view;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Paint;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.GradientBarPainter;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.zkoss.zkex.zul.impl.JFreeChartEngine;
import org.zkoss.zul.Chart;

/*
 * you are able to do many advanced chart customization by extending ChartEngine
 */
public class BarChartEngine extends JFreeChartEngine {

	public boolean prepareJFreeChart(JFreeChart jfchart, Chart chart) {
		// jfchart.setTitle("Samples");		
		CategoryPlot plot = jfchart.getCategoryPlot();		
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
		renderer.setBarPainter(new StandardBarPainter());
        
        CategoryAxis domainAxis = plot.getDomainAxis();
        //Rotation 
        domainAxis.setCategoryLabelPositions(
            CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 8.0)
        );

		renderer.setSeriesPaint(0, ChartColors.COLOR_1);
		renderer.setSeriesPaint(1, ChartColors.COLOR_2);
		renderer.setSeriesPaint(2, ChartColors.COLOR_3);		
		
		return false;
	}
}
