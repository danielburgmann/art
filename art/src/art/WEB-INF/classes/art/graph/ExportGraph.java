package art.graph;

import art.servlets.ArtDBCP;
import art.utils.ArtQuery;
import art.utils.ArtQueryParam;
import de.laures.cewolf.cpp.HeatmapEnhancer;
import de.laures.cewolf.cpp.RotatedAxisLabels;
import de.laures.cewolf.taglib.CewolfChartFactory;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.beanutils.RowSetDynaClass;
import org.jfree.chart.*;
import org.jfree.chart.axis.*;
import org.jfree.chart.labels.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.*;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.DefaultValueDataset;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.TextAnchor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Output charts as png or pdf files. Only used when charts are scheduled to run
 * as jobs. Files that are created when charts are executed interactively are
 * generated by the processChart method of the respective graph class
 *
 * @author Timothy Anyona
 * @author Enrico Liboni
 */
public class ExportGraph {

	final static Logger logger = LoggerFactory.getLogger(ExportGraph.class);
	String fullFileName = "-No File";
	String fullFileNameWithoutExt;
	String queryName;
	String fileUserName; //username portion of output file name
	String outputFormat;
	String y_m_d;
	String h_m_s;
	String xAxisLabel;
	String yAxisLabel;
	String graphOptions;
	String title;
	String exportPath;
	boolean showData;
	private boolean showLegend;
	private boolean showLabels;
	private boolean showDataPoints;
	Map<Integer, ArtQueryParam> displayParameters = null; //to enable display of graph parameters in pdf output	
	int queryId;

	/**
	 * Set the query id for the graph
	 *
	 * @param value
	 */
	public void setQueryId(int value) {
		queryId = value;
	}

	/**
	 * Determine if legend should be shown
	 *
	 * @param showLegend 	 <code>true</true> if legend should be shown
	 */
	public void setShowLegend(boolean showLegend) {
		this.showLegend = showLegend;
	}

	/**
	 * Determine if labels should be shown
	 *
	 * @param showLabels 	 <code>true</true> if labels should be shown
	 */
	public void setShowLabels(boolean showLabels) {
		this.showLabels = showLabels;
	}

	/**
	 * Determine if data points should be highlighted
	 *
	 * @param showDataPoints 	 <code>true</true> if data points should be highlighted
	 */
	public void setShowDataPoints(boolean showDataPoints) {
		this.showDataPoints = showDataPoints;
	}

	/**
	 * Set parameters to be displayed with the graph output
	 *
	 * @param value parameters to be displayed with the graph output
	 */
	public void setDisplayParameters(Map<Integer, ArtQueryParam> value) {
		displayParameters = value;
	}

	/**
	 * Determine if graph data should be included below graph for pdf output
	 *
	 * @param value
	 */
	public void setShowData(boolean value) {
		showData = value;
	}

	/**
	 * Set the export directory base path
	 *
	 * @param s export directory base path
	 */
	public void setExportPath(String s) {
		exportPath = s;
	}

	/**
	 * Get the full file name used to save the chart
	 *
	 * @return the full file name used to save the chart
	 */
	public String getFileName() {
		return fullFileName;
	}

	/**
	 * Set the query name to be used in the filename
	 *
	 * @param s query name to be used in the filename
	 */
	public void setQueryName(String s) {
		queryName = s;
	}

	/**
	 * Set the username to be used in the filename
	 *
	 * @param s username to be used in the filename
	 */
	public void setFileUserName(String s) {
		fileUserName = s;
	}

	/**
	 * Set the file format of the chart
	 *
	 * @param s file format of the chart. pdf or png
	 */
	public void setOutputFormat(String s) {
		outputFormat = s;
	}

	/**
	 * Set the x-axis label
	 *
	 * @param value x-axis label
	 */
	public void setXAxisLabel(String value) {
		xAxisLabel = value;
	}

	/**
	 * Set the y-axis label
	 *
	 * @param value y-axis label
	 */
	public void setYAxisLabel(String value) {
		yAxisLabel = value;
	}

	/**
	 * Set the graph tile
	 *
	 * @param value graph tile
	 */
	public void setTitle(String value) {
		title = value;
	}

	/**
	 * Set graph display options
	 *
	 * @param value graph display options
	 */
	public void setGraphOptions(String value) {
		graphOptions = value;
	}

	//Build filename for output file
	private void buildOutputFileName() {
		java.util.Date today = new java.util.Date();

		String dateFormat = "yyyy_MM_dd";
		SimpleDateFormat dateFormatter = new SimpleDateFormat(dateFormat);
		y_m_d = dateFormatter.format(today);

		String timeFormat = "HH_mm_ss";
		SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
		h_m_s = timeFormatter.format(today);
		
		fullFileNameWithoutExt=fileUserName + "-" + queryName + "-" + y_m_d + "-" + h_m_s + ArtDBCP.getRandomString();
		fullFileNameWithoutExt=ArtDBCP.cleanFileName(fullFileNameWithoutExt);

		fullFileNameWithoutExt = exportPath + fullFileNameWithoutExt;
	}

	/**
	 * Generate chart from query results and save to a file
	 *
	 * @param rs query's resultset
	 * @param queryType query type
	 */
	public void createFile(ResultSet rs, int queryType) {

		JFreeChart chart = null;
		int width;
		int height;
		ResultSetMetaData rsmd;
		boolean showTooltips = false;
		boolean showUrls = false;
		String bgColor;
		double from; //y axis range minimum
		double to; //y axis range maximum

		ChartTheme currentChartTheme = null;
		BarPainter currentBarPainter = null;

		try {
			//use legacy theme to ensure you have white plot backgrounds. this was changed in jfreechart 1.0.11 to default to grey
			//save current settings
			currentChartTheme = ChartFactory.getChartTheme();
			currentBarPainter = BarRenderer.getDefaultBarPainter();

			BarRenderer.setDefaultBarPainter(new StandardBarPainter());
			StandardChartTheme chartTheme = (StandardChartTheme) StandardChartTheme.createJFreeTheme(); //if using createLegacyTheme custom font isn't applied in pdf output for some reason

			//change default colours. default "jfree" theme has plot background colour of light grey
			chartTheme.setPlotBackgroundPaint(Color.white); //default is grey
			chartTheme.setDomainGridlinePaint(Color.lightGray); //default is white
			chartTheme.setRangeGridlinePaint(Color.lightGray); //default is white

			//enable use of custom font in pdf output
			String fontName = "SansSerif"; //for use in speedometer chart creation
			if (ArtDBCP.isUseCustomPdfFont()) {
				fontName = ArtDBCP.getArtSetting("pdf_font_name");
				Font oldExtraLargeFont = chartTheme.getExtraLargeFont();
				Font oldLargeFont = chartTheme.getLargeFont();
				Font oldRegularFont = chartTheme.getRegularFont();

				Font extraLargeFont = new Font(fontName, oldExtraLargeFont.getStyle(), oldExtraLargeFont.getSize());
				Font largeFont = new Font(fontName, oldLargeFont.getStyle(), oldLargeFont.getSize());
				Font regularFont = new Font(fontName, oldRegularFont.getStyle(), oldRegularFont.getSize());

				chartTheme.setExtraLargeFont(extraLargeFont);
				chartTheme.setLargeFont(largeFont);
				chartTheme.setRegularFont(regularFont);
			}
			ChartFactory.setChartTheme(chartTheme);

			//build file name to use for output
			buildOutputFileName();

			//set chart options									
			rsmd = rs.getMetaData();
			String seriesName = rsmd.getColumnLabel(2);

			if (yAxisLabel == null) {
				yAxisLabel = rsmd.getColumnLabel(1);
			}

			//process graph options	string to get custom width, height (only used for png output), bgcolour, y-min, y-max			

			//set graph options from query definition
			ArtQuery aq = new ArtQuery();
			aq.create(queryId, false); //populate query attributes, but don't build parameter list

			//override any custom graph options defined
			if (graphOptions != null) {
				aq.setGraphDisplayOptions(graphOptions, false); //sets graph width, height, y-min, y-max, bgcolour
			}

			//get show options from job settings and not from query settings
			/*
			 * showData=aq.isShowGraphData(); showLegend=aq.isShowLegend();
			 * showLabels=aq.isShowLabels(); showDataPoints=aq.isShowPoints();
			 */

			//graph options have now been set
			bgColor = aq.getGraphBgColor();
			width = aq.getGraphWidth();
			height = aq.getGraphHeight();
			from = aq.getGraphYMin();
			to = aq.getGraphYMax();

			// build the chart object
			switch (queryType) {
				case -2:
					//pie chart				

					//create dataset for the pie chart
					ArtPie pieChart = new ArtPie();
					pieChart.setSeriesName(seriesName);
					pieChart.prepareDataset(rs);
					DefaultPieDataset pieDataset = (DefaultPieDataset) pieChart.produceDataset(null);

					//create chart
					chart = ChartFactory.createPieChart3D(title, pieDataset, showLegend, showTooltips, showUrls);

					//set chart section labels. to have name of category, data value and percentage e.g. laptops = 21 (54%)
					PiePlot3D piePlot = (PiePlot3D) chart.getPlot();
					if (showLabels) {
						piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} = {1} ({2})"));
					} else {
						piePlot.setLabelGenerator(null);
					}

					break;
				case -1:
					//xy chart

					//create dataset for the xy chart
					ArtXY xyChart = new ArtXY();
					xyChart.setSeriesName(seriesName);
					xyChart.prepareDataset(rs);

					XYSeriesCollection xyDataset = (XYSeriesCollection) xyChart.produceDataset(null);

					//create chart								
					chart = ChartFactory.createXYLineChart(title, xAxisLabel, yAxisLabel, xyDataset, PlotOrientation.VERTICAL, showLegend, showTooltips, showUrls);

					XYPlot xyPlot = (XYPlot) chart.getPlot();

					//show data points if required
					if (showDataPoints) {
						XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) xyPlot.getRenderer();
						renderer.setBaseShapesVisible(true);
					}

					//set y axis range if required
					if (from != 0 && to != 0) {
						NumberAxis rangeAxis = (NumberAxis) xyPlot.getRangeAxis();
						rangeAxis.setRange(from, to);
					}

					break;
				case -6:
					//time series

					//create dataset for time series chart
					ArtTimeSeries timeSeriesChart = new ArtTimeSeries();
					timeSeriesChart.setSeriesName(seriesName);
					timeSeriesChart.prepareDataset(rs);
					TimeSeriesCollection timeSeriesDataset = (TimeSeriesCollection) timeSeriesChart.produceDataset(null);

					//create chart				
					chart = ChartFactory.createTimeSeriesChart(title, xAxisLabel, yAxisLabel, timeSeriesDataset, showLegend, showTooltips, showUrls);

					XYPlot timePlot = (XYPlot) chart.getPlot();

					//set y axis range if required
					if (from != 0 && to != 0) {
						NumberAxis rangeAxis = (NumberAxis) timePlot.getRangeAxis();
						rangeAxis.setRange(from, to);
					}

					//show data points if required
					if (showDataPoints) {
						XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) timePlot.getRenderer();
						renderer.setBaseShapesVisible(true);
					}

					break;
				case -7:
					//date series

					//create dataset for date series chart
					ArtDateSeries dateSeriesChart = new ArtDateSeries();
					dateSeriesChart.setSeriesName(seriesName);
					dateSeriesChart.prepareDataset(rs);
					TimeSeriesCollection dateSeriesDataset = (TimeSeriesCollection) dateSeriesChart.produceDataset(null);

					//create chart				
					chart = ChartFactory.createTimeSeriesChart(title, xAxisLabel, yAxisLabel, dateSeriesDataset, showLegend, showTooltips, showUrls);

					XYPlot datePlot = (XYPlot) chart.getPlot();

					//set y axis range if required
					if (from != 0 && to != 0) {
						NumberAxis rangeAxis = (NumberAxis) datePlot.getRangeAxis();
						rangeAxis.setRange(from, to);
					}

					//show data points if required
					if (showDataPoints) {
						XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) datePlot.getRenderer();
						renderer.setBaseShapesVisible(true);
					}

					break;
				case -10:
					//speedometer

					//create dataset for speedometer chart
					ArtSpeedometer speedometerChart = new ArtSpeedometer();
					speedometerChart.prepareDataset(rs);
					DefaultValueDataset speedometerDataset = (DefaultValueDataset) speedometerChart.produceDataset(null);

					//create chart. chartfactory doesn't have a method for creating a meter chart
					Font titleFont = new Font(fontName, Font.BOLD, 18); //font same as in cewolf to achieve similar look
					MeterPlot speedometerPlot = new MeterPlot(speedometerDataset);
					chart = new JFreeChart(title, titleFont, speedometerPlot, showLegend);

					//apply current theme to charts not created using the chartfactory
					chartTheme.apply(chart);

					//add ranges and any custom formatting
					speedometerChart.finalizePlot(speedometerPlot);

					break;
				case -11: //bubble chart
				case -12: //heat map

					//bubble chart or heat map chart

					//create dataset 
					ArtXYZChart xyz = new ArtXYZChart();
					xyz.setSeriesName(seriesName);
					xyz.setQueryType(queryType);
					xyz.prepareDataset(rs);
					DefaultXYZDataset xyzDataset = (DefaultXYZDataset) xyz.produceDataset(null);

					if (queryType == -11) {
						//bubble chart
						
						//create chart
						chart = ChartFactory.createBubbleChart(title, xAxisLabel, yAxisLabel, xyzDataset, PlotOrientation.VERTICAL, showLegend, showTooltips, showUrls);

						//set y axis range if required
						XYPlot bubblePlot = (XYPlot) chart.getPlot();
						if (from != 0 && to != 0) {
							NumberAxis rangeAxis = (NumberAxis) bubblePlot.getRangeAxis();
							rangeAxis.setRange(from, to);
						}
					} else if (queryType == -12) {
						//heat map
						
						//create chart
						chart=CewolfChartFactory.getChartInstance("heatmap", title, xAxisLabel, yAxisLabel, xyzDataset, showLegend);
						
						//process chart
						Map<String, String> heatmapOptions=xyz.getHeatmapOptions();
						HeatmapEnhancer heatmapPP=new HeatmapEnhancer();
						heatmapPP.processChart(chart, heatmapOptions);
						
						//set y axis range if required
						XYPlot heatmapPlot = (XYPlot) chart.getPlot();
						if (from != 0 && to != 0) {
							NumberAxis rangeAxis = (NumberAxis) heatmapPlot.getRangeAxis();
							rangeAxis.setRange(from, to);
						}
					}

					break;

				default:
					//charts that use the category dataset. bar and line graphs	

					//create dataset for chart
					ArtCategorySeries categoryChart = new ArtCategorySeries();
					categoryChart.setSeriesName(seriesName);
					categoryChart.prepareDataset(rs);
					DefaultCategoryDataset chartDataset = (DefaultCategoryDataset) categoryChart.produceDataset(null);

					//create appropriate chart
					switch (queryType) {
						case -3:
							//horizontal bar graph 3d
							chart = ChartFactory.createBarChart3D(title, xAxisLabel, yAxisLabel, chartDataset, PlotOrientation.HORIZONTAL, showLegend, showTooltips, showUrls);
							break;
						case -4:
							//vertical bar graph 3d
							chart = ChartFactory.createBarChart3D(title, xAxisLabel, yAxisLabel, chartDataset, PlotOrientation.VERTICAL, showLegend, showTooltips, showUrls);
							break;
						case -5:
							//line graph
							chart = ChartFactory.createLineChart(title, xAxisLabel, yAxisLabel, chartDataset, PlotOrientation.VERTICAL, showLegend, showTooltips, showUrls);

							//show data points if required
							if (showDataPoints) {
								CategoryPlot linePlot = (CategoryPlot) chart.getPlot();
								LineAndShapeRenderer renderer = (LineAndShapeRenderer) linePlot.getRenderer();
								renderer.setBaseShapesVisible(true);
							}
							break;
						case -8:
							//stacked vertical bar graph 3d
							chart = ChartFactory.createStackedBarChart3D(title, xAxisLabel, yAxisLabel, chartDataset, PlotOrientation.VERTICAL, showLegend, showTooltips, showUrls);
							break;
						case -9:
							//stacked horizontal bar graph 3d
							chart = ChartFactory.createStackedBarChart3D(title, xAxisLabel, yAxisLabel, chartDataset, PlotOrientation.HORIZONTAL, showLegend, showTooltips, showUrls);
							break;
					}

					if (chart != null) {
						CategoryPlot categoryPlot = (CategoryPlot) chart.getPlot();

						//display labels if required
						if (showLabels) {
							DecimalFormat valueFormatter;
							NumberFormat nf = NumberFormat.getInstance();
							valueFormatter = (DecimalFormat) nf;

							CategoryItemRenderer renderer = categoryPlot.getRenderer(); //could be a version of BarRenderer or LineAndShapeRenderer for line graphs
							CategoryItemLabelGenerator generator = new StandardCategoryItemLabelGenerator("{2}", valueFormatter);
							renderer.setBaseItemLabelGenerator(generator);
							renderer.setBaseItemLabelsVisible(true);

							renderer.setBasePositiveItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));
							renderer.setBaseNegativeItemLabelPosition(new ItemLabelPosition(ItemLabelAnchor.OUTSIDE12, TextAnchor.TOP_CENTER));

							categoryPlot.setRenderer(renderer);
						}

						//set y axis range if required
						if (from != 0 && to != 0) {
							NumberAxis rangeAxis = (NumberAxis) categoryPlot.getRangeAxis();
							rangeAxis.setRange(from, to);
						}

						//make x axis labels more readable by breaking them into 3 lines
						categoryPlot.getDomainAxis().setMaximumCategoryLabelLines(3);
					}
			}

			//save chart as png or pdf file	
			if (chart != null) {
				//set chart background colour. doesn't include plot background
				chart.setBackgroundPaint(Color.decode(bgColor));

				//display x-axis labels vertically if too many categories present
				RotatedAxisLabels labelRotation=new RotatedAxisLabels();
				
				//set rotate_at and remove_at parameters. set to same as in showGraph.jsp
				Map<String,String> params=new HashMap<String,String>();
				params.put("rotate_at","5");
				params.put("remove_at","10000");
				labelRotation.processChart(chart, params);
				
				
				if (outputFormat.equals("png")) {
					fullFileName = fullFileNameWithoutExt + ".png";
					ChartUtilities.saveChartAsPNG(new File(fullFileName), chart, width, height);
				} else if (outputFormat.equals("pdf")) {
					fullFileName = fullFileNameWithoutExt + ".pdf";

					//include graph data if applicable
					RowSetDynaClass graphData;
					if (showData) {
						int rsType = rs.getType();
						if (rsType == ResultSet.TYPE_SCROLL_INSENSITIVE || rsType == ResultSet.TYPE_SCROLL_SENSITIVE) {
							rs.beforeFirst();
						}
						graphData = new RowSetDynaClass(rs, false, true);
					} else {
						graphData = null;
					}

					PdfGraph.createPdf(chart, fullFileName, title, graphData, displayParameters);
				}
			}

		} catch (Exception e) {
			logger.error("Error", e);

			fullFileName = "-<b>Error:</b> " + e;
			if (fullFileName.length() > 4000) {
				fullFileName = fullFileName.substring(0, 4000);
			}
		} finally {
			//restore chart theme
			if (currentChartTheme != null) {
				ChartFactory.setChartTheme(currentChartTheme);
			}
			if (currentBarPainter != null) {
				BarRenderer.setDefaultBarPainter(currentBarPainter);
			}
		}
	}

}
