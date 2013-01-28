package com.samples.toolkit.ui.test;

import net.rim.device.api.ui.Graphics;

import com.samples.toolkit.ui.container.*;

public class ForegroundManager extends NegativeMarginVerticalFieldManager {
    
    public ForegroundManager() {
        super( USE_ALL_HEIGHT | VERTICAL_SCROLL );
    }
    
    protected void paintBackground( Graphics g )
    {
        int oldColor = g.getColor();
        try {
            g.setColor( 0xDDDDDD );
            g.fillRect( 0, getVerticalScroll(), getWidth(), getHeight() );
        } finally {
            g.setColor( oldColor );
        }
    }
}