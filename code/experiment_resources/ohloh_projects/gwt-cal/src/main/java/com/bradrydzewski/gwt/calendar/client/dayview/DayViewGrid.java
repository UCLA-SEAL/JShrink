package com.bradrydzewski.gwt.calendar.client.dayview;

import com.bradrydzewski.gwt.calendar.client.HasSettings;
import com.bradrydzewski.gwt.calendar.client.util.FormattingUtil;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.ComplexPanel;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 * The DayGrid draws the grid that displays days / time intervals in the
 * body of the calendar.
 * 
 * @author Brad
 * 
 */
public class DayViewGrid /*Impl*/ extends Composite {
	
    
        class Div extends ComplexPanel {
            	
	  public Div() {
            setElement(DOM.createDiv());
	  }
          @Override
          public boolean remove(Widget w) {
            boolean removed = super.remove(w);
            return removed;
          }
            @Override
              public void add(Widget w) {
                super.add(w, getElement());
              }
         }
    
    
    
	private static final int CELL_HEIGHT = 50;
	private static final String INTERVAL_MAJOR_STYLE = "major-time-interval";
	private static final String INTERVAL_MINOR_STYLE = "minor-time-interval";
	private static final String WORKING_HOUR_STYLE = "working-hours";
	// private FlexTable grid = new FlexTable();
	protected AbsolutePanel grid = new AbsolutePanel();
    protected SimplePanel gridOverlay = new SimplePanel();
        
	private HasSettings settings = null;
	//private FormattingImpl impl = GWT.create(FormattingImpl.class);
	//protected int offset = -1;
	
	
	private static final int HOURS_PER_DAY = 24;
	//private int intervalsPerHour = settings.getIntervalsPerHour();//2; //30 minute intervals
	//private float intervalSize = settings.getPixelsPerInterval();//25f; //25 pixels per interval

	public DayViewGrid(HasSettings settings) { //was DayViewGridImpl
		initWidget(grid);
		this.settings = settings;
		//System.out.println("DayViewGrid loaded, with offset = " + impl.getBorderOffset());
		// grid.setCellPadding(0);
		// grid.setCellSpacing(0);
//		 DOM.setStyleAttribute(grid.getElement(), "width", "100%");
		// DOM.setStyleAttribute(grid.getElement(), "height", "1200px");
//		 DOM.setStyleAttribute(grid.getElement(), "position", "absolute");
//		 DOM.setStyleAttribute(grid.getElement(), "top", "0px");
//		 DOM.setStyleAttribute(grid.getElement(), "left", "0px");
		// DOM.setStyleAttribute(grid.getElement(), "emptyCells", "show");
		//init();
	}

	public void build(int workingHourStart, int workingHourStop, int days) {

		grid.clear();
                
		
		int intervalsPerHour = settings.getSettings().getIntervalsPerHour();//2; //30 minute intervals
		float intervalSize = settings.getSettings().getPixelsPerInterval();

		this.setHeight( (intervalsPerHour * (intervalSize) * 24 ) +"px" );
		
					
		float dayWidth = 100f / days;
		float dayLeft = 0f;

		// for(int days=0;days<3;days++) {

		for (int i = 0; i < HOURS_PER_DAY; i++) {
			
			boolean isWorkingHours = (i >= workingHourStart && i <= workingHourStop);
			
			//create major interval
			SimplePanel sp1 = new SimplePanel();
			sp1.setStyleName("major-time-interval");
			sp1.setHeight(intervalSize+FormattingUtil.getBorderOffset()+"px");
			
			//if working hours set
			if (isWorkingHours) {
				sp1.addStyleName("working-hours");
			}
			
			//add to body
			grid.add(sp1);
			
			for(int x=0;x<intervalsPerHour-1;x++) {
				SimplePanel sp2 = new SimplePanel();
				sp2.setStyleName("minor-time-interval");
				
				sp2.setHeight(intervalSize+FormattingUtil.getBorderOffset()+"px");
				if (isWorkingHours) {
					sp2.addStyleName("working-hours");
				}
				// sp1.add(new Label("-"));
				grid.add(sp2);
			}
			
			
		}
		// }

		for (int day = 0; day < days; day++) {

			dayLeft = dayWidth * day;

			SimplePanel dayPanel = new SimplePanel();
			dayPanel.setStyleName("day-separator");
			grid.add(dayPanel);
			DOM.setStyleAttribute(dayPanel.getElement(), "left", dayLeft
					+ "%");
		}
                
                
                gridOverlay.setHeight("100%");
                gridOverlay.setWidth("100%");
                DOM.setStyleAttribute(gridOverlay.getElement(), "position", "absolute");
                DOM.setStyleAttribute(gridOverlay.getElement(), "left", "0px");
                DOM.setStyleAttribute(gridOverlay.getElement(), "top", "0px");
                grid.add(gridOverlay);
	}


}

